package uni.freiburg.sparqljoin.model.db;

import java.util.HashMap;

/**
 * Dictionary object used to map string terms to unique integers to compress operations
 */
public class Dictionary {

    private HashMap<Integer, String> values;

    private HashMap<String, Integer> invertedValues;

    private int index;

    public Dictionary() {
        this.values = new HashMap<>();
        this.invertedValues = new HashMap<>();
        this.index = 1;
    }

    /**
     * Put value into the dictionary and get unique integer instead
     * @param value to save
     * @return unique integer that represent putted value
     */
    public int put(String value) {
        // save value into the dictionary
        if (!this.invertedValues.containsKey(value)) {
            this.values.put(this.index, value);
            this.invertedValues.put(value, this.index);
            return this.index++;
        }
        // return existing index for the given value
        return this.invertedValues.get(value);
    }

    /**
     * Get string value from the dictionary by its index
     * @param key index representation of the value
     * @return value from the dictionary
     */
    public String get(int key) {
        return this.values.get(key);
    }
}
