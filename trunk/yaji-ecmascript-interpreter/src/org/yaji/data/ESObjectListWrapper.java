package org.yaji.data;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.yaji.util.ArrayUtil;

import FESI.Data.ESObject;
import FESI.Data.ESValue;
import FESI.Exceptions.EcmaScriptException;
import FESI.Interpreter.PackagedException;

class ESObjectListWrapper implements List<ESValue> {

    private final ESObject object;

    public ESObjectListWrapper(ESObject object) {
        this.object = object;
    }
    
    public boolean add(ESValue arg0) {
        throw new NoSuchMethodError();
    }

    public void add(int arg0, ESValue arg1) {
        throw new NoSuchMethodError();
    }

    public boolean addAll(Collection<? extends ESValue> arg0) {
        throw new NoSuchMethodError();
    }

    public boolean addAll(int arg0, Collection<? extends ESValue> arg1) {
        throw new NoSuchMethodError();
    }

    public void clear() {
        throw new NoSuchMethodError();
    }

    public boolean contains(Object arg0) {
        throw new NoSuchMethodError();
    }

    public boolean containsAll(Collection<?> arg0) {
        throw new NoSuchMethodError();
    }

    public ESValue get(int index) {
        try {
            return object.getPropertyIfAvailable((long)index);
        } catch ( EcmaScriptException e ) {
            throw new PackagedException(e, null);
        }
    }

    public int indexOf(Object arg0) {
        throw new NoSuchMethodError();
    }

    public boolean isEmpty() {
        throw new NoSuchMethodError();
    }

    public Iterator<ESValue> iterator() {
        throw new NoSuchMethodError();
    }

    public int lastIndexOf(Object arg0) {
        throw new NoSuchMethodError();
    }

    public ListIterator<ESValue> listIterator() {
        return new ListIterator<ESValue>() {

            private int index = -1;
            private int length = size();
            
            public void add(ESValue e) {
                throw new NoSuchMethodError();
                
            }

            public boolean hasNext() {
                return index < length;
            }

            public boolean hasPrevious() {
                return index > 0L;
            }

            public ESValue next() {
                try {
                    return object.getPropertyIfAvailable((long)++index);
                } catch ( EcmaScriptException ex ) {
                    throw new PackagedException(ex, null);
                }
            }

            public int nextIndex() {
                return index+1;
            }

            public ESValue previous() {
                try {
                    return object.getPropertyIfAvailable((long)index--);
                } catch ( EcmaScriptException ex ) {
                    throw new PackagedException(ex, null);
                }
            }

            public int previousIndex() {
                return index;
            }

            public void remove() {
                throw new NoSuchMethodError();
            }

            public void set(ESValue e) {
                try {
                    if (e == null) {
                        object.deleteProperty(index);
                    } else {
                        object.putProperty((long)index, e);
                    }
                } catch ( EcmaScriptException ex ) {
                    throw new PackagedException(ex, null);
                }
            }
        };
    }

    public ListIterator<ESValue> listIterator(int arg0) {
        throw new NoSuchMethodError();
    }

    public boolean remove(Object arg0) {
        throw new NoSuchMethodError();
    }

    public ESValue remove(int arg0) {
        throw new NoSuchMethodError();
    }

    public boolean removeAll(Collection<?> arg0) {
        throw new NoSuchMethodError();
    }

    public boolean retainAll(Collection<?> arg0) {
        throw new NoSuchMethodError();
    }

    public ESValue set(int index, ESValue value) {
        try {
            ESValue previousValue = object.getPropertyIfAvailable((long)index);
            if (value == null) {
                if (previousValue != null) {
                    object.deleteProperty(index);
                }
            } else {
                object.putProperty((long)index, value);
            }
            return previousValue;
        } catch ( EcmaScriptException e ) {
            throw new PackagedException(e, null);
        }
    }

    public int size() {
        try {
            return (int)ArrayUtil.getArrayLength(object);
        } catch (EcmaScriptException e) {
            throw new PackagedException(e, null);
        }
    }

    public List<ESValue> subList(int arg0, int arg1) {
        throw new NoSuchMethodError();
    }

    public Object[] toArray() {
        try {
            int size = size();
            Object [] result = new Object[size];
            for( int i=0; i<size; i++) {
                result[i] = object.getPropertyIfAvailable((long)i);
            }
            return result;
        } catch (EcmaScriptException e) {
            throw new PackagedException(e, null);
        }
    }

    public <T> T[] toArray(T[] arg0) {
        throw new NoSuchMethodError();
    }
    
}