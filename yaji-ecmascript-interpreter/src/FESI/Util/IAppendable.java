/**
 * This interface is for building strings.
 *
 * @author Stephen J. Muir
 */

package FESI.Util;

public interface IAppendable extends Appendable {
    public IAppendable append(boolean b);

    public IAppendable append(char c);

    public IAppendable append(char[] str);

    public IAppendable append(char[] str, int offset, int len);

    // Appends "null" if the parameter is null.
    // Also handles StringBuilder and StringBuffer.
    public IAppendable append(CharSequence csq);

    public IAppendable append(CharSequence csq, int start, int end);

    public IAppendable append(double d);

    public IAppendable append(float f);

    public IAppendable append(int i);

    public IAppendable append(long lng);

    public IAppendable append(Object obj);

    // Appends "null" if the parameter is null.
    public IAppendable append(String str);

    public IAppendable append(IAppendable app);

    public IAppendable append(String... strings);

    public IAppendable clear();

    public IAppendable cloneAppendable();

    // We don't really want to support this, but some rework is required to get
    // rid of it.
    public IAppendable insertAtStart(IAppendable app);

    // We don't really want to support this, but some rework is required to get
    // rid of it.
    public IAppendable insertAtStart(String str);

    public int length();

    public String toString();
}
