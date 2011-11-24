package FESI.Data;

import java.util.Enumeration;

final class ArrayPropertyNamesEnumeration implements
        Enumeration<String> {
    private final Enumeration<String> superNames;
    private final int size;
    int pos = 0;

    ArrayPropertyNamesEnumeration(Enumeration<String> superNames,
            int size) {
        this.superNames = superNames;
        this.size = size;
    }

    public boolean hasMoreElements() {
        if (pos < size) {
            return true;
        }
        return superNames.hasMoreElements();
    }

    public String nextElement() {
        if (pos < size) {
            return Integer.toString(pos++);
        }
        return superNames.nextElement();
    }
}