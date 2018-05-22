package org.me;

import java.util.Comparator;
import java.util.function.DoubleBinaryOperator;

public enum Operator {

    PLUS("+", (x, y) -> x + y),
    MINUS("-", (x, y) -> x - y),
    TIMES("*", (x, y) -> x * y),
    DIVIDE("/", (x, y) -> x / y);

    private final String symbol;
    private final DoubleBinaryOperator op;

    Operator(String symbol, DoubleBinaryOperator op) {
        this.symbol = symbol;
        this.op = op;
    }

    @Override
    public String toString() {
        return symbol;
    }

    public static Operator from(String token) {
        switch (token) {
            case "+":
                return PLUS;
            case "-":
                return MINUS;
            case "*":
                return TIMES;
            case "/":
                return DIVIDE;
        }
        throw new IllegalArgumentException(token + " is not a valid operand.");
    }

    public double apply(double operand1, double operand2) {
        return op.applyAsDouble(operand1, operand2);
    }

    public static class PrecedenceComparator implements Comparator<Operator>
    {
        public int compare(Operator o1, Operator o2)
        {
            if (o1.equals(PLUS) || o1.equals(MINUS)) {
                return (o2.equals(PLUS) || o2.equals(MINUS))? 0 : -1;
            } else if (o1.equals(TIMES) || o1.equals(DIVIDE)) {
                return (o2.equals(TIMES) || o2.equals(DIVIDE)) ? 0 : 1;
            }
            throw new AssertionError("Operands " + o1 + " and " + o2 + " not comparable.");
        }
    }

}
