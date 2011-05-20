/**
 * @author Stephen J. Muir
 */

package FESI.Util;

import java.io.Serializable;

public interface IAppendableFactory extends Serializable {
    public IAppendable create(int estimatedNumberOfAppends,
            int estimatedNumberOfCharacters);
}
