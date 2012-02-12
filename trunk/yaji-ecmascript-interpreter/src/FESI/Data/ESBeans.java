// ESBeans.java
// FESI Copyright (c) Jean-Marc Lugrin, 1999
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2 of the License, or (at your option) any later version.

// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.

// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

package FESI.Data;

import java.beans.Beans;
import java.io.IOException;

import org.yaji.log.ILog;
import org.yaji.log.Logs;

import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.ProgrammingError;
import FESI.Interpreter.Evaluator;
import FESI.Interpreter.FesiHashtable;
import FESI.Interpreter.LocalClassLoader;

/**
 * Implements the beans loader
 */
public class ESBeans extends ESLoader {
    private static final long serialVersionUID = 3540708121565818932L;
    private static final ILog log = Logs.getLog(ESBeans.class);

    /**
     * Create the top level bean loader (object Bean)
     * 
     * @param evaluator
     *            the evaluator
     */
    public ESBeans(Evaluator evaluator) {
        super(evaluator);
    }

    /**
     * Create a new bean loader or package prefix
     * 
     * @param packageName
     *            The extension of the package name
     * @param previousPackage
     *            Represents the higher level package names
     * @param classLoader
     *            the class loader to use for this loader
     * @param evaluator
     *            the evaluator
     */
    public ESBeans(String packageName, ESBeans previousPackage,
            LocalClassLoader classLoader, Evaluator evaluator) {
        super(packageName, previousPackage, classLoader, evaluator);
    }

    // overrides
    @Override
    public ESObject getPrototype() {
        throw new ProgrammingError("Cannot get prototype of Beans");
    }

    // overrides
    @Override
    public String getESClassName() {
        return "Beans";
    }

    // overrides
    // Getting a property dynamically creates a new Beans prefix object
    @Override
    public ESValue getPropertyIfAvailable(String propertyName, int hash)
            throws EcmaScriptException {
        ESValue value = getPropertyMap().get(propertyName, hash);
        if (value == null) {
            buildPrefix();
            value = new ESBeans(propertyName, this, classLoader, getEvaluator());
            getPropertyMap().put(propertyName, hash, FesiHashtable.Flag.False, FesiHashtable.Flag.False, value, FesiHashtable.Flag.True);
        }
        return value;
    }

    // overrides
    // Establish a bean classloader
    // The parameter is the directory or jar to load from
    @Override
    public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
            throws EcmaScriptException {
        if (previousPackage == null && classLoader == null) {
            // This is the Beans object
            if (arguments.length < 1) {
                throw new EcmaScriptException(
                        "Missing class directory or jar file name");
            }
            String directoryOrJar = arguments[0].toString();
            LocalClassLoader classLoader = LocalClassLoader
                    .makeLocalClassLoader(directoryOrJar);
            return new ESBeans(null, null, classLoader, getEvaluator());
        }
        throw new EcmaScriptException("Java class not found: '" + buildPrefix()
                + "'");
    }
    
    @Override
    public boolean isCallable() {
        return true;
    }

    // overrides
    // instantiates a bean
    @Override
    public ESObject doConstruct(ESValue[] arguments)
            throws EcmaScriptException {

        String beanName = buildPrefix();
        ESObject value = null;

        if (beanName == null) {
            throw new EcmaScriptException(
                    "cannot create beans without a package name");
        }

        try {
            Object bean = Beans.instantiate(classLoader, beanName);
            if (debugJavaAccess) {
                log.asDebug(" ** Bean '" + beanName + "' created");
            }
            value = new ESWrapper(bean, getEvaluator(), true);
        } catch (ClassNotFoundException e) {
            throw new EcmaScriptException("Bean '" + beanName + "' not found: "
                    + e);
        } catch (IOException e) {
            throw new EcmaScriptException("IOexception loading bean '"
                    + beanName + "': " + e);
        }
        return value;
    }

    // overrides
    @Override
    public String getTypeofString() {
        return "JavaBeans";
    }

    // overrides
    @Override
    public String toDetailString() {
        return "ES:<" + getESClassName() + ":'" + buildPrefix() + "'"
                + ((classLoader == null) ? "" : (",@" + classLoader)) + ">";
    }

}
