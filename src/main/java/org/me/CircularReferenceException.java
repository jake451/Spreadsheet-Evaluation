package org.me;

public class CircularReferenceException extends Exception {

    public CircularReferenceException(int row, int column) {
        super("Circular reference at row " + row + " column " + column);
    }

}
