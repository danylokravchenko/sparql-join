package uni.freiburg.sparqljoin.join;

import uni.freiburg.sparqljoin.model.db.ComplexTable;
import uni.freiburg.sparqljoin.model.db.DataType;
import uni.freiburg.sparqljoin.model.db.Dictionary;
import uni.freiburg.sparqljoin.model.db.Item;
import uni.freiburg.sparqljoin.model.join.BuildOutput;
import uni.freiburg.sparqljoin.model.join.JoinedItems;

import java.util.HashMap;
import java.util.List;

/**
 * This interface provides functionality for joins
 */
public interface AbstractJoin {

    /**
     * Join 2 tables
     *
     * @param R             R relation join table
     * @param S             S relation join table
     * @param joinPropertyR name of the property to join on from table R
     * @param joinOnR       join field in property from R
     * @param joinPropertyS name of the property to join on from table S
     * @param joinOnS       join field in property from S
     * @return new joined table
     */
    default ComplexTable join(ComplexTable R, ComplexTable S,
                              int joinPropertyR, JoinOn joinOnR,
                              int joinPropertyS, JoinOn joinOnS) {
        BuildOutput buildOutput = build(R, joinPropertyR, joinOnR);
        ComplexTable probeOutput = probe(buildOutput, R, S, joinPropertyR, joinOnR, joinPropertyS, joinOnS);

        // Remove unnecessary dictionary entries
        ComplexTable joinResult = new ComplexTable(new Dictionary());
        joinResult.insertComplexTable(probeOutput);

        return joinResult;
    }

    /**
     * Build phase of join
     *
     * @param table    build input
     * @param property property value to join on name of the property to join on from the reference table
     * @param joinOn   property field to join on
     * @return build output
     */
    BuildOutput build(ComplexTable table, int property, JoinOn joinOn);

    /**
     * Probe phase of join
     *
     * @param partition     partition from the build phase
     * @param R             R relation table for the reference
     * @param S             S relation table to join
     * @param joinPropertyR name of the property to join on from table R
     * @param joinOnR       join field in property from R
     * @param joinPropertyS name of the property to join on from table S
     * @param joinOnS       join field in property from S
     * @return joined values in new table
     */
    ComplexTable probe(BuildOutput partition, ComplexTable R, ComplexTable S,
                       int joinPropertyR, JoinOn joinOnR,
                       int joinPropertyS, JoinOn joinOnS);


    /**
     * Merge 2 tuples into a single with all properties. Merge the dictionaries.
     *
     * @param joinedItems              Output relation where the new tuple should be added
     * @param outputObjectDictionary   Dictionary of the output relation. Entries from the R relation must have been added before.
     * @param outputPropertyDictionary Dictionary containing the properties. Entries from the R relation must have been added before.
     * @param joinedItemsR             Items from R relation
     * @param joinedItemsS             Items from S relation
     * @param dictionaryS              S table dictionary
     */
    default void mergeTuplesAndDictionaries(List<JoinedItems> joinedItems, Dictionary outputPropertyDictionary, Dictionary propertyDictionaryS, Dictionary outputObjectDictionary, JoinedItems joinedItemsR, JoinedItems joinedItemsS, Dictionary dictionaryS) {
        // clone reference item values to avoid overwriting values by reference
        @SuppressWarnings("unchecked") HashMap<Integer, Item> outputValues = (HashMap<Integer, Item>) joinedItemsR.values().clone();

        // add new property values
        joinedItemsS.values().forEach((propertyIntS, propertyItem) -> {
            String property = propertyDictionaryS.getValues().get(propertyIntS);
            Integer outputPropertyInt = outputPropertyDictionary.getInvertedValues().get(property);
            if (outputPropertyInt == null) {
                outputPropertyInt = outputPropertyDictionary.put(property);
            }

            if (propertyItem.type().equals(DataType.STRING)) {
                // object was a string -> put value into new dictionary, update item value index
                // TODO write a test for this

                int propertyItemObjectIntRepresentation = outputObjectDictionary.put(dictionaryS.get(propertyItem.object()));
                outputValues.put(outputPropertyInt, new Item(
                        propertyItem.subject(),
                        propertyItemObjectIntRepresentation,
                        propertyItem.type()
                ));
            } else {
                // else put as it is
                outputValues.put(outputPropertyInt, new Item(
                        propertyItem.subject(),
                        propertyItem.object(),
                        propertyItem.type())
                );
            }
        });
        joinedItems.add(new JoinedItems(joinedItemsR.subject(), outputValues));
    }
}
