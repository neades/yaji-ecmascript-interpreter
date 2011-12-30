/*
 * FesiHashtable, adapted from:
 * @(#)Hashtable.java    1.41 97/01/28
 * 
 * Copyright (c) 1995, 1996 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of Sun
 * Microsystems, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sun.
 * 
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 * 
 * CopyrightVersion 1.1_beta
 *
 * Adapted by JM Lugrin for FESI
 * Synchronization removed (speed up, evaluator already synchronized)
 * Data type specific for object property list.
 * Externally provided hash code (to avoid recalculating the hash code.
 * Support the hidden and readonly properties (not enforced by this class).
 * Optimize compare for interned strings and poor hash function.
 * 
 */

package FESI.Interpreter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import org.yaji.json.JsonState;
import org.yaji.json.JsonUtil;

import FESI.Data.ESBoolean;
import FESI.Data.ESObject;
import FESI.Data.ESString;
import FESI.Data.ESUndefined;
import FESI.Data.ESValue;
import FESI.Data.ObjectObject;
import FESI.Data.ObjectPrototype;
import FESI.Data.StandardProperty;
import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.TypeError;
import FESI.Interpreter.FesiHashtable.Flag;

/**
 * Hashtable collision list.
 */
class HashtableEntry implements java.io.Serializable {
    private static final long serialVersionUID = 4541703187276273804L;
    int hash;
    String key;
    ESValue value;          
    HashtableEntry next;
    boolean hidden;         // = !enumerable
    boolean readonly;       // = !writable
    boolean configurable = true;  

    @Override
    protected Object clone() {
        HashtableEntry entry = new HashtableEntry();
        entry.hash = hash;
        entry.key = key;
        entry.value = value;
        entry.hidden = hidden;
        entry.readonly = readonly;  
        entry.configurable = configurable;
        entry.next = (next != null) ? (HashtableEntry) next.clone() : null;
        return entry;
    }

    void set(ESValue value, Flag hidden, Flag readonly, Flag configurable) {
        this.value = value;
        this.hidden = hidden.from(this.hidden);
        this.readonly = readonly.from(this.readonly);
        this.configurable = configurable.from(this.configurable);
    }
}

/**
 * This class implements a hashtable, which maps keys to values. Any non-
 * <code>null</code> object can be used as a key or as a value.
 * <p>
 * To successfully store and retrieve objects from a hashtable, the objects used
 * as keys must implement the <code>hashCode</code> method and the
 * <code>equals</code> method.
 * <p>
 * An instance of <code>Hashtable</code> has two parameters that affect its
 * efficiency: its <i>capacity</i> and its <i>load factor</i>. The load factor
 * should be between 0.0 and 1.0. When the number of entries in the hashtable
 * exceeds the product of the load factor and the current capacity, the capacity
 * is increased by calling the <code>rehash</code> method. Larger load factors
 * use memory more efficiently, at the expense of larger expected time per
 * lookup.
 * <p>
 * If many entries are to be made into a <code>Hashtable</code>, creating it
 * with a sufficiently large capacity may allow the entries to be inserted more
 * efficiently than letting it perform automatic rehashing as needed to grow the
 * table.
 * <p>
 * This example creates a hashtable of numbers. It uses the names of the numbers
 * as keys:
 * <p>
 * <blockquote>
 * 
 * <pre>
 * Hashtable numbers = new Hashtable();
 * numbers.put(&quot;one&quot;, Integer.valueOf(1));
 * numbers.put(&quot;two&quot;, Integer.valueOf(2));
 * numbers.put(&quot;three&quot;, Integer.valueOf(3));
 * </pre>
 * 
 * </blockquote>
 * <p>
 * To retrieve a number, use the following code:
 * <p>
 * <blockquote>
 * 
 * <pre>
 * Integer n = (Integer) numbers.get(&quot;two&quot;);
 * if (n != null) {
 *     System.out.println(&quot;two = &quot; + n);
 * }
 * </pre>
 * 
 * </blockquote>
 * 
 * @author Arthur van Hoff
 * @version 1.41, 01/28/97
 * @see java.lang.Object#equals(java.lang.Object)
 * @see java.lang.Object#hashCode()
 * @see java.util.Hashtable#rehash()
 * @since JDK1.0
 */
public class FesiHashtable implements Cloneable, java.io.Serializable {
    private static final long serialVersionUID = -505893943964068672L;

    /**
     * The hash table data.
     */
    private HashtableEntry table[];

    /**
     * The total number of entries in the hash table.
     */
    private int count;

    /**
     * Rehashes the table when count exceeds this threshold.
     */
    private int threshold;

    /**
     * The load factor for the hashtable.
     */
    private final float loadFactor;

    private final int initialCapacity;

    public static enum Flag {
        False {
            @Override
            public boolean from(boolean current) {
                return false;
            }
        },
        True{
            @Override
            public boolean from(boolean current) {
                return true;
            }
        },
        Default{
            @Override
            public boolean from(boolean current) {
                return current;
            }
        };

        public abstract boolean from(boolean current);

        public static Flag fromBoolean(boolean b) {
            return b?True:False;
        }
    }
    
    /**
     * Constructs a new, empty hashtable with the specified initial capacity and
     * the specified load factor.
     * 
     * @param initialCapacity
     *            the initial capacity of the hashtable.
     * @param loadFactor
     *            a number between 0.0 and 1.0.
     * @exception IllegalArgumentException
     *                if the initial capacity is less than or equal to zero, or
     *                if the load factor is less than or equal to zero.
     * @since JDK1.0
     */
    public FesiHashtable(int initialCapacity, float loadFactor) {
        if ((initialCapacity <= 0) || (loadFactor <= 0.0)) {
            throw new IllegalArgumentException();
        }
        this.initialCapacity = initialCapacity;
        this.loadFactor = loadFactor;
        this.table = null;
        threshold = (int) (initialCapacity * loadFactor);
    }

    /**
     * Constructs a new, empty hashtable with the specified initial capacity and
     * default load factor.
     * 
     * @param initialCapacity
     *            the initial capacity of the hashtable.
     * @since JDK1.0
     */
    public FesiHashtable(int initialCapacity) {
        this(initialCapacity, 0.75f);
    }

    /**
     * Constructs a new, empty hashtable with a default capacity and load
     * factor.
     * 
     * @since JDK1.0
     */
    public FesiHashtable() {
        this(27, 0.75f); // a smaller prime than the original 27
    }

    private HashtableEntry[] getTable() {
        if (table == null) {
            table = new HashtableEntry[initialCapacity];
        }
        return table;
    }

    /**
     * Returns the number of keys in this hashtable.
     * 
     * @return the number of keys in this hashtable.
     * @since JDK1.0
     */
    public int size() {
        return count;
    }

    /**
     * Tests if this hashtable maps no keys to values.
     * 
     * @return <code>true</code> if this hashtable maps no keys to values;
     *         <code>false</code> otherwise.
     * @since JDK1.0
     */
    public boolean isEmpty() {
        return count == 0;
    }

    /**
     * Returns an enumeration of the keys in this hashtable.
     * 
     * @return an enumeration of the keys in this hashtable.
     * @see java.util.Enumeration
     * @see java.util.Hashtable#elements()
     * @since JDK1.0
     */
    public Enumeration<String> keys() {
        return new HashtableKeyEnumerator(getTable());
    }

    /**
     * Returns an enumeration of the values in this hashtable. Use the
     * Enumeration methods on the returned object to fetch the elements
     * sequentially.
     * 
     * @return an enumeration of the values in this hashtable.
     * @see java.util.Enumeration
     * @see java.util.Hashtable#keys()
     * @since JDK1.0
     */
    public Enumeration<ESValue> elements() {
        return new HashtableValueEnumerator(getTable());
    }

    /**
     * Tests if the specified object is a key in this hashtable.
     * 
     * @param key
     *            possible key.
     * @return <code>true</code> if the specified object is a key in this
     *         hashtable; <code>false</code> otherwise.
     */

    public boolean containsKey(String key, int hash) {
        return getHashtableEntry(key, hash) != null;
    }

    /**
     * Returns the value to which the specified key is mapped in this hashtable.
     * 
     * @param key
     *            a key in the hashtable.
     * @param hash
     *            the key hashtable.
     * @return the value to which the key is mapped in this hashtable;
     *         <code>null</code> if the key is not mapped to any value in this
     *         hashtable.
     */

    public ESValue get(String key, int hash) {
        HashtableEntry e = getHashtableEntry(key, hash);
        return e==null?null:e.value;
    }

    private HashtableEntry getHashtableEntry(String key, int hash) {
        HashtableEntry tab[] = getTable();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        HashtableEntry e;
        for (e = tab[index]; e != null; e = e.next) {
            if ((e.key == key) || ((e.hash == hash) && e.key.equals(key))) { // $codepro.audit.disable stringComparison
                break;
            }
        }
        return e;
    }
    

    /**
     * Check if a property is hidden (return false if not present).
     * 
     * @param key
     *            a key in the hashtable.
     * @param hash
     *            the key hashtable.
     * @return true if hidden.
     */

    public boolean isHidden(String key, int hash) {
        HashtableEntry e = getHashtableEntry(key, hash);
        return e==null?false:e.hidden;
    }


    public boolean isEnumerable(String propertyName, int hashCode) {
        HashtableEntry e = getHashtableEntry(propertyName, hashCode);
        return e==null?false:!e.hidden;
    }

    /**
     * Check if a property is readonly (return false if not present).
     * 
     * @param key
     *            a key in the hashtable.
     * @param hash
     *            the key hashtable.
     * @param extensible
     * @return true if hidden.
     */

    public boolean isReadonly(String key, int hash, boolean extensible) {
        HashtableEntry e = getHashtableEntry(key, hash);
        return e==null?!extensible:(!ESValue.isAccessorDescriptor(e.value) && e.readonly);
    }

    /**
     * Rehashes the contents of the hashtable into a hashtable with a larger
     * capacity. This method is called automatically when the number of keys in
     * the hashtable exceeds this hashtable's capacity and load factor.
     * 
     * @since JDK1.0
     */
    protected void rehash() {
        int oldCapacity = getTable().length;
        HashtableEntry oldTable[] = getTable();

        int newCapacity = oldCapacity * 2 + 1;
        if (newCapacity < 101) {
            newCapacity = 101; // Ensure a prime
        }
        HashtableEntry newTable[] = new HashtableEntry[newCapacity];

        threshold = (int) (newCapacity * loadFactor);
        this.table = newTable;

        // System.out.println("rehash old=" + oldCapacity + ", new=" +
        // newCapacity + ", thresh=" + threshold + ", count=" + count);

        for (int i = oldCapacity; i-- > 0;) {
            for (HashtableEntry old = oldTable[i]; old != null;) {
                HashtableEntry e = old;
                old = old.next;

                int index = (e.hash & 0x7FFFFFFF) % newCapacity;
                e.next = newTable[index];
                newTable[index] = e;
            }
        }
    }

    /**
     * Maps the specified <code>key</code> to the specified <code>value</code>
     * in this hashtable. Neither the key nor the value can be <code>null</code>
     * .
     * <p>
     * The value can be retrieved by calling the <code>get</code> method with a
     * key that is equal to the original key.
     * 
     * @param key
     *            the hashtable key.
     * @param hash
     *            the hash value.
     * @param hidden
     *            true if the entry must not be enumerated.
     * @param readonly
     *            true if the entry must not be deleted.
     * @param value
     *            the value.
     * @param configurable 
     * @return the previous value of the specified key in this hashtable, or
     *         <code>null</code> if it did not have one.
     * @exception NullPointerException
     *                if the key or value is <code>null</code>.
     * @see java.lang.Object#equals(java.lang.Object)
     * @see java.util.Hashtable#get(java.lang.Object)
     * @since JDK1.0
     */
    public ESValue put(String key, int hash, Flag hidden, Flag readonly,
            ESValue value, Flag configurable) {
        // Make sure the value is not null
        if (value == null) {
            throw new NullPointerException("value");
        }

        // Makes sure the key is not already in the hashtable.
        HashtableEntry e = getHashtableEntry(key, hash);
        if (e != null) {
            ESValue old = e.value;
            e.set(value, hidden, readonly, configurable);
            return old;
        }

        if (count >= threshold) {
            // Rehash the table if the threshold is exceeded
            rehash();
            return put(key, hash, hidden, readonly, value, configurable);
        }

        // Creates the new entry.
        HashtableEntry[] tab = getTable();
        e = new HashtableEntry();
        e.hash = hash;
        e.key = key;
        e.set(value, hidden, readonly, configurable);
        int index = (hash & 0x7FFFFFFF) % tab.length;
        e.next = tab[index];
        tab[index] = e;
        count++;
        return null;
    }

    /**
     * Removes the key (and its corresponding value) from this hashtable. This
     * method does nothing if the key is not in the hashtable.
     * 
     * @param key
     *            the key that needs to be removed.
     * @return true if value didn't exist or was successfully deleted.
     * @throws EcmaScriptException
     * @since JDK1.0
     */
    public boolean remove(String key, int hash, boolean throwError)
            throws EcmaScriptException {
        HashtableEntry tab[] = getTable();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        for (HashtableEntry e = tab[index], prev = null; e != null; prev = e, e = e.next) {
            if ((e.hash == hash) && e.key.equals(key)) {
                if (e.configurable) {
                    if (prev != null) {
                        prev.next = e.next;
                    } else {
                        tab[index] = e.next;
                    }
                    count--;
                    return true;
                }
                if (throwError) {
                    throw new TypeError("Property " + key
                            + " cannot be deleted.");
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Clears this hashtable so that it contains no keys.
     * 
     * @since JDK1.0
     */
    public void clear() {
        HashtableEntry tab[] = getTable();
        for (int index = tab.length; --index >= 0;) {
            tab[index] = null;
        }
        count = 0;
    }

    /**
     * Creates a shallow copy of this hashtable. The keys and values themselves
     * are not cloned. This is a relatively expensive operation.
     * 
     * @return a clone of the hashtable.
     * @since JDK1.0
     */
    @Override
    public Object clone() {
        try {
            FesiHashtable t = (FesiHashtable) super.clone();
            t.table = (new HashtableEntry[getTable().length]);
            for (int i = getTable().length; i-- > 0;) {
                t.getTable()[i] = (getTable()[i] != null) ? (HashtableEntry) getTable()[i]
                        .clone()
                        : null;
            }
            return t;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }

    /**
     * Returns a rather long string representation of this hashtable.
     * 
     * @return a string representation of this hashtable.
     * @since JDK1.0
     */
    @Override
    public String toString() {
        int max = size() - 1;
        StringBuilder buf = new StringBuilder(max > 0 ? max * 16 : 16);
        Enumeration<String> k = keys();
        Enumeration<ESValue> e = elements();
        buf.append('{');

        for (int i = 0; i <= max; i++) {
            String s1 = k.nextElement().toString();
            String s2 = e.nextElement().toString();
            buf.append(s1).append('=').append(s2);
            if (i < max) {
                buf.append(", ");
            }
        }
        buf.append('}');
        return buf.toString();
    }

    public void setAllNonConfigurable(boolean readOnly) {
        HashtableEntry[] tab = getTable();
        for (HashtableEntry hashtableEntry : tab) {
            for (HashtableEntry e = hashtableEntry; e != null; e = e.next) {
                e.configurable = false;
                if (readOnly) {
                    e.readonly = true;
                }
            }
        }
    }

    public boolean isAllReadOnly() {
        boolean frozen = true;
        HashtableEntry[] tab = getTable();
        for (HashtableEntry hashtableEntry : tab) {
            for (HashtableEntry e = hashtableEntry; e != null && frozen; e = e.next) {
                frozen = e.readonly;
            }
        }
        return frozen;
    }

    public boolean isAnyConfigurable() {
        boolean configurable = false;
        HashtableEntry[] tab = getTable();
        for (HashtableEntry hashtableEntry : tab) {
            for (HashtableEntry e = hashtableEntry; e != null && !configurable; e = e.next) {
                configurable = e.configurable;
            }
        }
        return configurable;
    }

    public void toJson(Appendable appendable, JsonState state, ESObject thisObject) throws IOException, EcmaScriptException {
        
        List<HashtableEntry> sortedList;
        if (state.allowedSize() > 0) {
            sortedList = orderPropertiesByAllowed(state);
        } else {
            sortedList = getOrderedEntries();
        }
        state.indent.push();
        String sep = state.indent.start();
        boolean valueGenerated = false;
        for (HashtableEntry hashtableEntry : sortedList) {
            if (hashtableEntry == null) {
                continue;
            }
            ESValue value = hashtableEntry.value;
            value = state.callReplacerFunction(thisObject, ESString.valueOf(hashtableEntry.key), value );
            if (value.canJson()) {
                appendable.append(sep); sep=state.indent.separator();
                appendable.append('"');
                JsonUtil.escape(appendable, hashtableEntry.key);
                appendable.append("\":").append(state.indent.preValue());
                value.toJson(appendable, state, hashtableEntry.key);
                valueGenerated = true;
            }
        }
        if (valueGenerated) {
            appendable.append(state.indent.end());
        }
        state.indent.pop();
    }

    private List<HashtableEntry> orderPropertiesByAllowed(JsonState state) {
        List<HashtableEntry> sortedList;
        HashtableEntry [] array = new HashtableEntry[state.allowedSize()];
        HashtableEntry[] tab = getTable();
        for (HashtableEntry hashtableEntry : tab) {
            for (HashtableEntry e = hashtableEntry; e != null; e = e.next) {
                long index = state.getAllowedIndex(e.key);
                if (index != -1) {
                    array[(int)index] = e;
                }
            }
        }
        sortedList = Arrays.asList(array);
        return sortedList;
    }

    private List<HashtableEntry> getOrderedEntries() {
        HashtableEntry[] tab = getTable();
        List<HashtableEntry> sortedList = new ArrayList<HashtableEntry>();
        for (HashtableEntry hashtableEntry : tab) {
            for (HashtableEntry e = hashtableEntry; e != null; e = e.next) {
                sortedList.add(e);
            }
        }
        Collections.sort(sortedList, new Comparator<HashtableEntry>() {
            public int compare(HashtableEntry o1, HashtableEntry o2) {
                return o1.key.compareTo(o2.key);
            }
        });
        return sortedList;
    }

    public ESObject getOwnPropertyDescriptor(String propertyName, Evaluator evaluator) throws EcmaScriptException {
        HashtableEntry e = getHashtableEntry(propertyName, propertyName.hashCode());
        if (e == null) {
            return null;
        }
        ESValue value = e.value;
        boolean writable = !e.readonly;
        boolean enumerable = !e.hidden;
        boolean configurable = e.configurable;
        return createPropertyDescriptor(evaluator, value, writable, enumerable,
                configurable);
    }

    public static ESObject createPropertyDescriptor(Evaluator evaluator, ESValue value, boolean writable, boolean enumerable,
            boolean configurable) throws EcmaScriptException {
        ObjectPrototype object = ObjectObject.createObject(evaluator);
        if (ESValue.isAccessorDescriptor(value)) {
            ESValue setter = value.getSetAccessorDescriptor();
            if (setter != null) {
                object.putProperty(StandardProperty.SETstring, setter, StandardProperty.SEThash);
            }
            ESValue getter = value.getGetAccessorDescriptor();
            if (getter != null) {
                object.putProperty(StandardProperty.GETstring, getter, StandardProperty.GEThash);
            }
        } else {
            object.putProperty(StandardProperty.VALUEstring,value,StandardProperty.VALUEhash);
            object.putProperty(StandardProperty.WRITABLEstring, ESBoolean.valueOf(writable), StandardProperty.WRITABLEhash);
        }
        object.putProperty(StandardProperty.ENUMERABLEstring, ESBoolean.valueOf(enumerable), StandardProperty.ENUMERABLEhash);
        object.putProperty(StandardProperty.CONFIGURABLEstring, ESBoolean.valueOf(configurable), StandardProperty.CONFIGURABLEhash);
        return object;
    }

    public interface IReporter {
        public boolean reject(String message) throws TypeError;
    }
    
    public boolean defineProperty(String propertyName, FesiHashtable desc, IReporter reporter, boolean extensible, Evaluator evaluator) throws EcmaScriptException {
        int propertNameHash = propertyName.hashCode();
        HashtableEntry e = getHashtableEntry(propertyName, propertNameHash);
        boolean enumerable = false;
        boolean writable = false;
        boolean configurable = false;
        ESValue value = ESUndefined.theUndefined;
        
        
        ESValue configurableValue = desc.get(StandardProperty.CONFIGURABLEstring, StandardProperty.CONFIGURABLEhash);
        ESValue enumerableValue = desc.get(StandardProperty.ENUMERABLEstring, StandardProperty.ENUMERABLEhash);
        ESValue newValue = desc.get(StandardProperty.VALUEstring, StandardProperty.VALUEhash);
        ESValue writableValue = desc.get(StandardProperty.WRITABLEstring, StandardProperty.WRITABLEhash);
        ESValue getter = desc.get(StandardProperty.GETstring, StandardProperty.GEThash);
        ESValue setter = desc.get(StandardProperty.SETstring, StandardProperty.SEThash);
        
        if (newValue != null || writableValue != null) {
            if (getter != null || setter != null) {
                return reporter.reject("Cannot set both value and set/get for property "+propertyName);
            }
        }
        
        if (e != null) {
            enumerable = !e.hidden;
            writable = !e.readonly;
            configurable = e.configurable;
            value = e.value;
            
            boolean valueIsAccessor = ESValue.isAccessorDescriptor(value);
            boolean newValueIsAccessor = (setter != null || getter != null);
            if (!configurable) {
                if (configurableValue != null && configurableValue.booleanValue()) {
                    return reporter.reject("Cannot change configurable state of property "+ propertyName+ " It is not configurable");
                }
                if (enumerableValue != null && enumerableValue.booleanValue() != enumerable) {
                    return reporter.reject("Cannot change enumerable state of property "+ propertyName+ " It is not configurable");
                }
                if (writableValue != null && writableValue.booleanValue() && !writable) {
                    return reporter.reject("Cannot make property "+propertyName+" writable. It is not configurable.");
                }
                if (valueIsAccessor != newValueIsAccessor) {
                    return reporter.reject("Cannot change accessor state of property "+ propertyName+ " It is not configurable");
                } else if (valueIsAccessor && newValueIsAccessor) {
                    if (ESValue.hasSetAccessorDescriptor(value) && !value.getSetAccessorDescriptor().equalsSameType(setter)) {
                        return reporter.reject("Cannot change \"set\" accessor off property "+ propertyName+ " It is not configurable");
                    }
                    if (ESValue.hasGetAccessorDescriptor(value) && !value.getGetAccessorDescriptor().equalsSameType(getter)) {
                        return reporter.reject("Cannot change \"get\" accessor off property "+ propertyName+ " It is not configurable");
                    }
                } else if (newValue != null && !writable && !value.equalsSameType(newValue)) {
                    return reporter.reject("Cannot change value of property "+propertyName+". It is not writable.");
                }
            }
        } else {
            if (!extensible) {
                return reporter.reject("Object is not extensible - property "+propertyName+" cannot be defined");
            }
        }
        
        configurable = updateBoolean(configurable, configurableValue);
        enumerable = updateBoolean(enumerable, enumerableValue);
        writable = updateBoolean(writable, writableValue);
        if (newValue != null) {
            value = newValue;
        } else if (setter != null || getter != null) {
            value = ObjectObject.createObject(evaluator);
            value.setGetAccessorDescriptor(getter);
            value.setSetAccessorDescriptor(setter);
        }
        put(propertyName,propertNameHash,Flag.fromBoolean(!enumerable),Flag.fromBoolean(!writable),value,Flag.fromBoolean(configurable));
        return true;
    }

    private boolean updateBoolean(boolean configurable,
            ESValue configurableValue) throws EcmaScriptException {
        if (configurableValue != null) {
            configurable = configurableValue.booleanValue();
        }
        return configurable;
    }

    public Enumeration<String> enumerableKeys() {
        return new EnumerableHashtableKeyEnumerator(table);
    }
}

/**
 * A hashtable enumerator class. This class should remain opaque to the client.
 * It will use the Enumeration interface.
 */
abstract class AbstractHashtableEnumerator implements java.io.Serializable {
    private static final long serialVersionUID = -2020372394289196808L;
    private int index;
    private HashtableEntry table[];
    private HashtableEntry entry;

    AbstractHashtableEnumerator(HashtableEntry table[]) {
        this.table = table;
        this.index = table.length;
    }

    public boolean hasMoreElements() {
        if (entry != null) {
            return true;
        }
        advance();
        return (entry != null);
    }
    
    private void advance() {
        while (index-- > 0) {
            if ((entry = table[index]) != null) {
                do {
                    if (allowed(entry)) {
                        return;
                    }
                } while ( (entry = entry.next) != null);
            }
        }
    }
    
    protected boolean allowed(HashtableEntry entry) {
        return true;
    }

    protected HashtableEntry nextEntry() {
        if (entry == null) {
            advance();
        }
        if (entry != null) {
            HashtableEntry e = entry;
            do {
                entry = entry.next;
            } while (!(entry == null || allowed(entry)));
            return e;
        }
        throw new java.util.NoSuchElementException("FesiHashtableEnumerator");
    }
}

class HashtableKeyEnumerator extends AbstractHashtableEnumerator implements
        Enumeration<String> {
    private static final long serialVersionUID = -5151529369306332429L;

    HashtableKeyEnumerator(HashtableEntry table[]) {
        super(table);
    }

    public String nextElement() {
        return nextEntry().key;
    }
}

class EnumerableHashtableKeyEnumerator extends AbstractHashtableEnumerator implements
Enumeration<String> {
    private static final long serialVersionUID = -5151529369306332429L;

    EnumerableHashtableKeyEnumerator(HashtableEntry table[]) {
        super(table);
    }

    public String nextElement() {
        return nextEntry().key;
    }
    
    @Override
    protected boolean allowed(HashtableEntry entry) {
        return !entry.hidden;
    }
}
class HashtableValueEnumerator extends AbstractHashtableEnumerator implements
Enumeration<ESValue> {
    private static final long serialVersionUID = -5151529369306332429L;

    HashtableValueEnumerator(HashtableEntry table[]) {
        super(table);
    }

    public ESValue nextElement() {
        return nextEntry().value;
    }
}
