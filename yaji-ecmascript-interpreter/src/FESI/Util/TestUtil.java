package FESI.Util;

import FESI.Interpreter.Evaluator;

public abstract class TestUtil {

    public static void setUtilEvaluatorAccessor(final Evaluator localEvaluator) {
        FESI.Util.EvaluatorAccess.setAccessor(new FESI.Util.IEvaluatorAccess() {

            public Evaluator getEvaluator() {
                return localEvaluator;
            }

        });
    }

    public static void clearUtilEvaluatorAccessor() {
        FESI.Util.EvaluatorAccess.setAccessor(null);
    }

}
