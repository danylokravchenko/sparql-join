package uni.freiburg.sparqljoin.model.join;

import uni.freiburg.sparqljoin.join.JoinOn;
import uni.freiburg.sparqljoin.model.db.Item;

import java.util.Comparator;
import java.util.HashMap;

/**
 * @param subject RDF subject
 * @param values  HashMap with (key = RDF property, value = (RDF subject (the same as above), RDF object))
 */
public record JoinedItems(int subject, HashMap<Integer, Item> values) {
    @Override
    public boolean equals(Object obj) {
        assert obj instanceof JoinedItems;
        JoinedItems o = (JoinedItems) obj;
        for (int property : values().keySet()) {
            if (!o.values().containsKey(property)) {
                return false;
            }
            Item thisItem = values().get(property);
            Item otherItem = o.values().get(property);
            if (!thisItem.equals(otherItem)) {
                return false;
            }
        }
        return true;
    }

    public JoinedItems clone() {
        //noinspection unchecked
        return new JoinedItems(this.subject, (HashMap<Integer, Item>) this.values.clone());
    }

    public static class JoinedItemsComparator implements Comparator<JoinedItems> {

        // property to sort on
        private final int property;

        private final JoinOn field;

        public JoinedItemsComparator(int property, JoinOn field) {
            this.property = property;
            this.field = field;
        }

        @Override
        public int compare(JoinedItems o1, JoinedItems o2) {
            Item o1Item = o1.values().get(property);
            Item o2Item = o2.values().get(property);

            long o1Key = field == JoinOn.SUBJECT ? o1Item.subject() : o1Item.object();
            long o2Key = field == JoinOn.SUBJECT ? o2Item.subject() : o2Item.object();

            return Long.compare(o1Key, o2Key);
        }
    }

}
