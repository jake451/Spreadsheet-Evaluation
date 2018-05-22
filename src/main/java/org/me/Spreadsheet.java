package org.me;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * A spreadsheet consisting of a two-dimensional array of cells. Columns are identified using letters and rows by
 * numbers (C2 references a cell in column 3, row 2). Each cell contains either an integer or an expression.
 * Expressions contain integers, cell references, and operators ('+', '-', '*', '/') and are evaluated with the
 * usual rules of evaluation.
 */
public class Spreadsheet {

    private static final String FILE_PARAM = "-file";
    private static final String SEPARATOR = ",";
    private static final String OUTPUT_FILE = "output.csv";
    private static final int SCALE = 100;

    /* row, column */
    private String[][] table;

    private Pattern re_isNumber = Pattern.compile("[+\\-]?([0-9]+[.])?[0-9]+");
    private Pattern re_isCellRef = Pattern.compile("[A-Z][0-9]+");
    private Pattern re_isOperand = Pattern.compile("[+\\-*/]");

    public static void main(String[] args) {
        String fileName = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(FILE_PARAM) && ++i < args.length) {
                fileName = args[i];
            }
        }

        if (fileName == null) {
            usage();
            System.exit(1);
        }

        // read in csv file
        Spreadsheet spreadsheet = Spreadsheet.from(Paths.get(fileName));

        // evaluate all cells
        try {
            spreadsheet.evaluate();
        } catch (CircularReferenceException ex) {
            System.out.println(ex.getMessage());
            System.exit(1);
        }

        // output to file
        Spreadsheet.to(Paths.get(OUTPUT_FILE), spreadsheet);
    }

    public Spreadsheet(String[][] table) {
        Objects.requireNonNull(table);
        this.table = table;
    }

    /**
     * Utility factory method for creating a spreadsheet from a file path.
     *
     * @param filePath of csv file
     * @return generated spreadsheet
     */
    public static Spreadsheet from(Path filePath) {
        String[][] table = null;
        try (Stream<String> stream = Files.lines(filePath)) {
            table = stream.map(line -> line.split(SEPARATOR))
                    .toArray(String[][]::new);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return new Spreadsheet(table);
    }

    /**
     * Utility method for writing a spreadsheet to a file.
     *
     * @param filePath for csv file
     * @param spreadsheet to write
     */
    public static void to(Path filePath, Spreadsheet spreadsheet) {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, Charset.forName("UTF-8"))) {
            for (int i = 0; i < spreadsheet.rowCount(); i++) {
                String[] row = spreadsheet.getRow(i);
                for (int j = 0; j < row.length; j++) {
                    double val = Math.round(Double.parseDouble(row[j]) * SCALE) / SCALE;
                    writer.write(val + (j < row.length - 1 ? SEPARATOR : ""));
                }
                writer.newLine();
            }
            System.out.println("Output to file " + OUTPUT_FILE);
        } catch (IOException | RuntimeException ex) {
            ex.printStackTrace();
        }
    }

    public int rowCount() {
        return table.length;
    }

    public int columnCount() {
        if (table.length == 0) return 0;
        return table[0].length;
    }

    public String[] getRow(int ind) {
        if (table != null && ind < rowCount()) {
            return table[ind];
        }
        else {
            throw new NoSuchElementException("Row " + ind + " does not exist.");
        }
    }

    public String getCell(int i_row, int i_col) {
        if (table != null && i_row < rowCount() && i_col < columnCount()) {
            return table[i_row][i_col];
        }
        else {
            throw new NoSuchElementException("Cell [" + i_row + "," + i_col + "] does not exist.");
        }
    }

    /**
     * parsing mathematical expressions specified in infix notation
     * @throws CircularReferenceException  if circular reference detected
     */
    public void evaluate() throws CircularReferenceException {
        if (table == null || table.length == 0 || table[0].length == 0) return;
        int[][] visited = new int[table.length][table[0].length];
        for (int i = 0; i < table.length; i++) {
            String[] row = table[i];
            for (int j = 0; j < row.length; j++) {
                evaluateCell(i, j, visited);
            }
        }
    }

    /**
     * Parses mathematical expressions specified in infix notation and stores in same cell.
     *
     * @param i_row row index of cell
     * @param i_col column index of cell
     * @param visited table to monitor for circular references
     * @return value of cell expression
     * @throws CircularReferenceException if circular reference detected
     */
    protected double evaluateCell(int i_row, int i_col, int[][] visited) throws CircularReferenceException {
        // return value if number
        String expression = table[i_row][i_col];
        if (isNumber(expression)) return Double.parseDouble(expression);

        // check if cell already visited
        if (visited[i_row][i_col] == 1) {
            throw new CircularReferenceException(i_row, i_col);
        }
        visited[i_row][i_col] = 1;

        Deque<Double> valueStack = new ArrayDeque<>();
        Deque<Operator> operatorStack = new ArrayDeque<>();
        String[] tokens = expression.split("((?<=[+\\-*/])|(?=[+\\-*/]))");
        if (tokens[0] != null && tokens[0].equals("+") || tokens[0].equals("-")) {
            String sign = tokens[0];
            tokens = Arrays.copyOfRange(tokens,1, tokens.length);
            tokens[0] = sign + tokens[0];
        }
        Operator.PrecedenceComparator precedenceComparator = new Operator.PrecedenceComparator();

        for (String token : tokens) {
            if (isNumber(token)) {
                valueStack.push(Double.parseDouble(token));
            } else if (isCellRef(token)) {
                valueStack.push(evaluateCell(token, visited));
            } else if (isOperator(token)) {
                Operator thisOp = Operator.from(token);
                while (operatorStack.size() > 0 && precedenceComparator.compare(operatorStack.peek(), thisOp) >= 0) {
                    Operator op = operatorStack.pop();
                    Double operand2 = valueStack.pop();
                    Double operand1 = valueStack.pop();
                    Double result = op.apply(operand1, operand2);
                    valueStack.push(result);
                }
                operatorStack.push(thisOp);
            }
            else {
                throw new IllegalArgumentException(expression + " is not a valid cell expression");
            }
        }
        while (operatorStack.size() > 0) {
            Operator op = operatorStack.pop();
            Double operand2 = valueStack.pop();
            Double operand1 = valueStack.pop();
            Double result = op.apply(operand1, operand2);
            valueStack.push(result);
        }
        assert valueStack.size() == 1;

        Double val = valueStack.pop();
        table[i_row][i_col] = val.toString();
        return val;
    }

    protected double evaluateCell(String cellRef, int[][] visited) throws CircularReferenceException {
        int col = Character.toUpperCase(cellRef.charAt(0)) - 65; // 'A' is 65 in ASCII
        int row = Integer.parseInt(cellRef.substring(1,cellRef.length())) - 1;
        return evaluateCell(row, col, visited);
    }

    protected boolean isNumber(String token) {
        Matcher m = re_isNumber.matcher(token);
        return m.matches();
    }

    protected boolean isCellRef(String token) {
        Matcher m = re_isCellRef.matcher(token);
        return m.matches();
    }

    protected boolean isOperator(String token) {
        Matcher m = re_isOperand.matcher(token);
        return m.matches();
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < rowCount(); i++) {
            String[] row = getRow(i);
            for (int j = 0; j < row.length; j++) {
                double val = Math.round(Double.parseDouble(row[j]) * SCALE) / SCALE;
                strBuilder.append(val);
                if (j < row.length - 1) {
                    strBuilder.append(",");
                }
            }
            strBuilder.append("\n");
        }
        return strBuilder.toString();
    }

    static void usage() {
        System.out.println("Usage: spreadsheet -file <FILE>\n Option -file is required");
    }

}
