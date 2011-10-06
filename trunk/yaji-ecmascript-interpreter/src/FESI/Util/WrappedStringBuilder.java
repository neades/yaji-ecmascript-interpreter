/**
 * This allows you to use a StringBuilder where an IAppendable is required.
 * 
 * @author Stephen J. Muir
 */

package FESI.Util;

import java.io.Serializable;

public class WrappedStringBuilder implements IAppendable, Serializable,
        CharSequence {
    private static final long serialVersionUID = 6074813468105579212L;
    StringBuilder stringBuilder;

    public WrappedStringBuilder() {
        stringBuilder = new StringBuilder();
    }

    public WrappedStringBuilder(CharSequence seq) {
        stringBuilder = new StringBuilder(seq);
    }

    public WrappedStringBuilder(int capacity) {
        stringBuilder = new StringBuilder(capacity);
    }

    public WrappedStringBuilder(String str) {
        stringBuilder = new StringBuilder(str);
    }

    public WrappedStringBuilder append(boolean b) {
        stringBuilder.append(b);
        return this;
    }

    public WrappedStringBuilder append(char c) {
        stringBuilder.append(c);
        return this;
    }

    public WrappedStringBuilder append(char[] str) {
        stringBuilder.append(str);
        return this;
    }

    public WrappedStringBuilder append(char[] str, int offset, int len) {
        stringBuilder.append(str, offset, len);
        return this;
    }

    public WrappedStringBuilder append(CharSequence s) {
        stringBuilder.append(s);
        return this;
    }

    public WrappedStringBuilder append(CharSequence csq, int start, int end) {
        stringBuilder.append(csq, start, end);
        return this;
    }

    public WrappedStringBuilder append(double d) {
        stringBuilder.append(d);
        return this;
    }

    public WrappedStringBuilder append(float f) {
        stringBuilder.append(f);
        return this;
    }

    public WrappedStringBuilder append(int i) {
        stringBuilder.append(i);
        return this;
    }

    public WrappedStringBuilder append(long lng) {
        stringBuilder.append(lng);
        return this;
    }

    public WrappedStringBuilder append(Object obj) {
        stringBuilder.append(obj);
        return this;
    }

    public WrappedStringBuilder append(String str) {
        stringBuilder.append(str);
        return this;
    }

    public WrappedStringBuilder append(IAppendable app) {
        if (app instanceof WrappedStringBuilder) {
            append(((WrappedStringBuilder) app).stringBuilder);
        } else {
            append(app.toString());
        }

        return this;
    }

    public WrappedStringBuilder append(String... strings) {
        for (String string : strings) {
            append(string);
        }

        return this;
    }

    public int capacity() {
        return stringBuilder.capacity();
    }

    public char charAt(int index) {
        return stringBuilder.charAt(index);
    }

    public WrappedStringBuilder clear() {
        stringBuilder.setLength(0);
        return this;
    }

    public WrappedStringBuilder cloneAppendable() {
        WrappedStringBuilder clone = new WrappedStringBuilder(stringBuilder
                .capacity());

        clone.stringBuilder.append(stringBuilder);
        return clone;
    }

    public void ensureCapacity(int minimumCapacity) {
        stringBuilder.ensureCapacity(minimumCapacity);
    }

    public int indexOf(String str) {
        return stringBuilder.indexOf(str);
    }

    public int indexOf(String str, int fromIndex) {
        return stringBuilder.indexOf(str, fromIndex);
    }

    public WrappedStringBuilder insert(int index, boolean b) {
        stringBuilder.insert(index, b);
        return this;
    }

    public WrappedStringBuilder insert(int index, char c) {
        stringBuilder.insert(index, c);
        return this;
    }

    public WrappedStringBuilder insert(int index, char[] str) {
        stringBuilder.insert(index, str);
        return this;
    }

    public WrappedStringBuilder insert(int index, char[] str, int offset,
            int len) {
        stringBuilder.insert(index, str, offset, len);
        return this;
    }

    public WrappedStringBuilder insert(CharSequence csq) {
        stringBuilder.insert(0, csq);
        return this;
    }

    public WrappedStringBuilder insert(int index, CharSequence csq) {
        stringBuilder.insert(index, csq);
        return this;
    }

    public WrappedStringBuilder insert(int index, CharSequence csq, int start,
            int end) {
        stringBuilder.insert(index, csq, start, end);
        return this;
    }

    public WrappedStringBuilder insert(int index, double d) {
        stringBuilder.insert(index, d);
        return this;
    }

    public WrappedStringBuilder insert(int index, float f) {
        stringBuilder.insert(index, f);
        return this;
    }

    public WrappedStringBuilder insert(int index, int i) {
        stringBuilder.insert(index, i);
        return this;
    }

    public WrappedStringBuilder insert(int index, long lng) {
        stringBuilder.insert(index, lng);
        return this;
    }

    public WrappedStringBuilder insert(int index, Object obj) {
        stringBuilder.insert(index, obj);
        return this;
    }

    public WrappedStringBuilder insert(int index, String str) {
        stringBuilder.insert(index, str);
        return this;
    }

    public WrappedStringBuilder insertAtStart(IAppendable app) {
        if (app instanceof WrappedStringBuilder) {
            insert(0, ((WrappedStringBuilder) app).stringBuilder);
        } else {
            insert(0, app.toString());
        }

        return this;
    }

    public WrappedStringBuilder insertAtStart(String str) {
        stringBuilder.insert(0, str);
        return this;
    }

    public boolean isEmpty() {
        return stringBuilder.length() == 0;
    }

    public int lastIndexOf(String str) {
        return stringBuilder.lastIndexOf(str);
    }

    public int lastIndexOf(String str, int fromIndex) {
        return stringBuilder.lastIndexOf(str, fromIndex);
    }

    public int length() {
        return stringBuilder.length();
    }

    public CharSequence subSequence(int start, int end) {
        return stringBuilder.subSequence(start, end);
    }

    @Override
    public String toString() {
        return stringBuilder.toString();
    }
}
