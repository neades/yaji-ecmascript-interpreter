package FESI.Data;

import java.util.Enumeration;

final class ArrayPropertyNamesEnumeration implements
        Enumeration<String> {
    private final Enumeration<String> superNames;
    private final int size;
    int pos = 0;
    private final boolean includeLength;

    ArrayPropertyNamesEnumeration(Enumeration<String> superNames,
            int size, boolean includeLength) {
        this.superNames = superNames;
        this.size = size;
        this.includeLength = includeLength;
    }

    public boolean hasMoreElements() {
        if (pos < size || (includeLength && pos == size)) {
            return true;
        }
        return superNames.hasMoreElements();
    }

    public String nextElement() {
        if (pos < size) {
            return Integer.toString(pos++);
        } else if (includeLength && pos++ == size) {
            return "length";
        }
        return superNames.nextElement();
    }
}