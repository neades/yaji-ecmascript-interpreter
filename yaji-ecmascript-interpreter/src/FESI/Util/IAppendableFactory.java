/**
 * @author Stephen J. Muir
 */

package FESI.Util;

public interface IAppendableFactory {
    public IAppendable create(int estimatedNumberOfAppends,
            int estimatedNumberOfCharacters);
}
