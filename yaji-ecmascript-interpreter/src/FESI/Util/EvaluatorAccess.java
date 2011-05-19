package FESI.Util;

import FESI.Interpreter.Evaluator;

public class EvaluatorAccess {

    private static IEvaluatorAccess evaluatorAccess;

    public static void setAccessor(IEvaluatorAccess evalutorAccess) {
        EvaluatorAccess.evaluatorAccess = evalutorAccess;
    }

    public static Evaluator getEvaluator() {
        return evaluatorAccess == null ? null : evaluatorAccess.getEvaluator();
    }
}
