/******************************************************************
 *
 *   Sulaiman Mohamed / 272 001
 *
 *   Note, additional comments provided throughout this source code
 *   is for educational purposes
 *
 ********************************************************************/

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.lang.Math;

@SuppressWarnings("unchecked")
public class CuckooHash<K, V> {

	private int CAPACITY;  					// Hashmap capacity
	private Bucket<K, V>[] table;			// Hashmap table
	private int a = 37, b = 17;				// Constants used in h2(key)

	/**
	 * Class Bucket
	 *
	 * Inner bucket class which represents a <key,value> pair
	 * within the hash map.
	 *
	 * @param <K> - type of key
	 * @param <V> - type of value
	 */
	private class Bucket<K, V> {
		private K bucKey = null;
		private V value = null;

		public Bucket(K k, V v) {
			bucKey = k;
			value = v;
		}

		/*
		 * Getters and Setters
		 */
		private K getBucKey() {
			return bucKey;
		}
		private V getValue()  { return value;  }
	}

	/*
	 * Hash functions, hash1 and hash2
	 */
	private int hash1(K key) 	{ return Math.abs(key.hashCode()) % CAPACITY; }
	private int hash2(K key) 	{ return (a * b + Math.abs(key.hashCode())) % CAPACITY; }

	/**
	 * Method CuckooHash
	 *
	 * Constructor that initializes and sets the hashmap. A future
	 * optimization would to pass a load factor limit as a target in
	 * maintaining the hashmap before reaching the point where we have
	 * a cycle causing occurring loop.
	 *
	 * @param size user input multimap capacity
	 */
	public CuckooHash(int size) {
		CAPACITY = size;
		table = new Bucket[CAPACITY];
	}

	/**
	 * Method size
	 *
	 * Get the number of elements in the table; the time complexity is O(n).
	 *
	 * @return total key-value pairs
	 */
	public int size() {
		int count = 0;
		for (int i=0; i<CAPACITY; ++i) {
			if (table[i] != null)
				count++;
		}
		return count;
	}

	/**
	 * Method clear
	 *
	 * Removes all elements in the table, it does not rest the size of
	 * the hashmap. Optionally, we could reset the CAPACITY to its
	 * initial value when the object was instantiated.
	 */
	public void clear() {
		table = new Bucket[CAPACITY];
	}

	public int mapSize() { return CAPACITY; }    // used in external testing only

	/**
	 * Method values
	 *
	 * Get a list containing of all values in the table
	 *
	 * @return the values as a list
	 */
	public List<V> values() {
		List<V> allValues = new ArrayList<V>();
		for (int i=0; i<CAPACITY; ++i) {
			if (table[i] != null) {
				allValues.add(table[i].getValue());
			}
		}
		return allValues;
	}

	/**
	 * Method keys
	 *
	 * Get a set containing all the keys in the table
	 *
	 * @return a set of keys
	 */
	public Set<K> keys() {
		Set<K> allKeys = new HashSet<K>();
		for (int i=0; i<CAPACITY; ++i) {
			if (table[i] != null) {
				allKeys.add(table[i].getBucKey());
			}
		}
		return allKeys;
	}

	/**
	 * Method put
	 *
	 * Adds a key-value pair to the table by means of cuckoo hashing.
	 */
	public void put(K key, V value) {
		// Check if this exact key-value pair already exists
		if (get(key) != null && get(key).equals(value)) {
			return;
		}

		// Create a new bucket to insert
		Bucket<K, V> newBucket = new Bucket<>(key, value);

		// Get the two potential hash positions
		int pos1 = hash1(key);
		int pos2 = hash2(key);

		// Try to insert at the first hash position if empty
		if (table[pos1] == null) {
			table[pos1] = newBucket;
			return;
		}

		// Try to insert at the second hash position if empty
		if (table[pos2] == null) {
			table[pos2] = newBucket;
			return;
		}

		// If both positions are occupied, start the cuckoo process
		Bucket<K, V> current = newBucket;
		int currentPos = pos1;
		int iterations = 0;

		// Continue until we either find an empty spot or hit max iterations
		while (iterations <= CAPACITY) {
			// If current position is empty, place the bucket
			if (table[currentPos] == null) {
				table[currentPos] = current;
				return;
			}

			// Swap the current bucket with the existing one at this position
			Bucket<K, V> temp = table[currentPos];
			table[currentPos] = current;
			current = temp;

			// Determine the alternate position for the displaced bucket
			// If it was at hash1 position, move to hash2 position, and vice versa
			currentPos = (currentPos == hash1(current.getBucKey()))
					? hash2(current.getBucKey())
					: hash1(current.getBucKey());

			iterations++;
		}

		// If we've reached max iterations, we need to rehash
		rehash();

		// Recursively try to insert the last displaced bucket
		put(current.getBucKey(), current.getValue());
	}

	/**
	 * Method get
	 *
	 * Retrieve a value in O(1) time based on the key because it can only
	 * be in 1 of 2 locations
	 *
	 * @param key Key to search for
	 * @return the found value or null if it doesn't exist
	 */
	public V get(K key) {
		int pos1 = hash1(key);
		int pos2 = hash2(key);
		if (table[pos1] != null && table[pos1].getBucKey().equals(key))
			return table[pos1].getValue();
		else if (table[pos2] != null && table[pos2].getBucKey().equals(key))
			return table[pos2].getValue();
		return null;
	}

	/**
	 * Method remove
	 *
	 * Removes this key value pair from the table. Its time complexity
	 * is O(1) because the key can only be in 1 of 2 locations.
	 *
	 * @param key the key to remove
	 * @param value the value to remove
	 * @return successful removal
	 */
	public boolean remove(K key, V value) {
		int pos1 = hash1(key);
		int pos2 = hash2(key);
		if (table[pos1] != null && table[pos1].getValue().equals(value)) {
			table[pos1] = null;
			return true;
		}
		else if (table[pos2] != null && table[pos2].getValue().equals(value)) {
			table[pos2] = null;
			return true;
		}
		return false;
	}

	/**
	 * Method printTable
	 *
	 * The method will prepare a String representation of the table of
	 * the format
	 *      [ <k1, v1> <k2. v2> ... <kn, vn> ]
	 * where n is the number of <key, value> pairs.
	 *
	 * @return the table's contents as a String
	 */
	public String printTable() {
		StringBuilder sb = new StringBuilder();
		sb.append("[ ");
		for (int i=0; i<CAPACITY; ++i) {
			if (table[i] != null) {
				sb.append("<");
				sb.append(table[i].getBucKey()); //key
				sb.append(", ");
				sb.append(table[i].getValue()); //value
				sb.append("> ");
			}
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Method rehash
	 *
	 * This method regrows the hashtable to capacity: 2*old capacity + 1
	 * and reinserts (rehashes) all the <key,value> pairs.
	 *
	 * This method invokes the 'put' method, so it is possible that
	 * another cycle is found when rehashing the hashmap. If this occurs,
	 * this function can be invoked recursively via the 'put' method.
	 */
	private void rehash() {
		Bucket<K, V>[] tableCopy = table.clone();
		int OLD_CAPACITY = CAPACITY;
		CAPACITY = (CAPACITY * 2) + 1;
		table = new Bucket[CAPACITY];

		for (int i=0; i<OLD_CAPACITY; ++i) {
			if (tableCopy[i] != null) {
				put(tableCopy[i].getBucKey(), tableCopy[i].getValue());
			}
		}
	}
}
