/**
 * @author Stephen J. Muir
 */

package FESI.Util;

public class WrappedStringBuilderFactory implements IAppendableFactory {
    private final int MINIMUM_INITIAL_SIZE = 16;
    private final int MAXIMUM_INITIAL_SIZE = 1024 * 100;

    /**
     * @param estimatedNumberOfAppends
     *            not needed for this implementation.
     */
    public IAppendable create(int estimatedNumberOfAppends,
            int estimatedNumberOfCharacters) {
        int initialSize = estimatedNumberOfCharacters;

        if (initialSize < MINIMUM_INITIAL_SIZE) {
            initialSize = MINIMUM_INITIAL_SIZE;
        }

        if (initialSize > MAXIMUM_INITIAL_SIZE) {
            initialSize = MAXIMUM_INITIAL_SIZE;
        }

        return new WrappedStringBuilder(initialSize);
    }
}
