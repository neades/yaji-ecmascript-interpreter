// JavaAccess.java
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

package FESI.Extensions;

import FESI.Data.BuiltinFunctionObject;
import FESI.Data.ESBeans;
import FESI.Data.ESBoolean;
import FESI.Data.ESLoader;
import FESI.Data.ESObject;
import FESI.Data.ESPackages;
import FESI.Data.ESString;
import FESI.Data.ESUndefined;
import FESI.Data.ESValue;
import FESI.Data.FunctionPrototype;
import FESI.Data.GlobalObject;
import FESI.Exceptions.EcmaScriptException;
import FESI.Interpreter.Evaluator;

public class JavaAccess extends Extension {
    private static final long serialVersionUID = 1639896019075230024L;

    static class GlobalObjectJavaTypeOf extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1436041700328969737L;

        GlobalObjectJavaTypeOf(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {

            if (arguments.length > 0) {
                Object obj = arguments[0].toJavaObject();
                String cn = (obj == null) ? "null" : ESLoader.typeName(obj
                        .getClass());
                return new ESString(cn);
            }
            return ESUndefined.theUndefined;
        }
    }

    static class GlobalObjectLoadExtension extends BuiltinFunctionObject {
        private static final long serialVersionUID = -164035943654712169L;

        GlobalObjectLoadExtension(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {

            Object ext = null;
            if (arguments.length > 0) {
                String pathName = arguments[0].toString();
                ext = this.getEvaluator().addExtension(pathName);
            }
            return ESBoolean.makeBoolean(ext != null);
        }
    }

    public JavaAccess() {
        super();
    }

    public void initializeExtension(Evaluator evaluator)
            throws EcmaScriptException {

        GlobalObject go = evaluator.getGlobalObject();
        FunctionPrototype fp = (FunctionPrototype) evaluator
                .getFunctionPrototype();

        go.putHiddenProperty("javaTypeOf", new GlobalObjectJavaTypeOf(
                "javaTypeOf", evaluator, fp));
        go.putHiddenProperty("loadExtension", new GlobalObjectLoadExtension(
                "loadExtension", evaluator, fp));

        ESPackages packagesObject = (ESPackages) evaluator.getPackageObject();
        String java = ("java").intern();
        ESPackages javaPackages = (ESPackages) packagesObject.getProperty(java,
                java.hashCode());
        go.putHiddenProperty("Packages", packagesObject);
        go.putHiddenProperty(java, javaPackages);
        ESBeans javaBeans = new ESBeans(evaluator);
        go.putHiddenProperty("Beans", javaBeans);

    }
}
