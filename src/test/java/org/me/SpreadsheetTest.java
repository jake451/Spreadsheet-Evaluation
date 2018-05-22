package org.me;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class SpreadsheetTest {

    @Test
    public void testEvaluateCell() throws Exception {
        String[][] table = {{"1+2*3+5"}};
        Spreadsheet spreadsheet = new Spreadsheet(table);
        int[][] visited = new int[1][1];
        spreadsheet.evaluateCell(0,0,visited);
        assertThat(spreadsheet.getCell(0,0), is("12.0"));
    }

    @Test
    public void testEvaluate() throws Exception {
        String[][] table = {{"B2+2","A1+A2"},{"B2-3","7+5"}};
        Spreadsheet spreadsheet = new Spreadsheet(table);
        spreadsheet.evaluate();
        assertThat(spreadsheet.getCell(0,0), is("14.0"));
        assertThat(spreadsheet.getCell(0,1), is("23.0"));
        assertThat(spreadsheet.getCell(1,0), is("9.0"));
        assertThat(spreadsheet.getCell(1,1), is("12.0"));
    }

    @Test(expected = CircularReferenceException.class)
    public void testEvaluate_CircularReference() throws Exception {
        String[][] table = {{"B2+2","A1"},{"B2-3","A2+5"}};
        Spreadsheet spreadsheet = new Spreadsheet(table);
        spreadsheet.evaluate();
    }

}
