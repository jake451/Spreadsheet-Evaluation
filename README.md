# Spreadsheet Challenge [SOLUTION]
Jason Dinger / jakedinger@gmail.com

A program in Java to read in a spreadsheet, evaluate the value of each cell, and output the values to a
file.

A spreadsheet consists of a two-dimensional array of cells. Columns are identified using letters and rows by
numbers (C2 references a cell in column 3, row 2). Each cell contains either an integer or an expression.
Expressions contain integers, cell references, and operators ('+', '-', '*', '/') and are evaluated with the
usual rules of evaluation.

Circular references are detected.

**Input Format:**
- A csv file with m rows and n columns
- The input file will have no headers
- Cells will not be surrounded in double quotes

**Output Format:**
- A csv file (to stdout is fine) with the same dimensions as the input file
- Each cell should be output as a floating point value. Round output values to two decimal places.

**Example:**

_input.csv_
```
B2+2,A1+A2
B2-3,7+5
```

_output.csv_
```
14.00,23.00
9.00,12.00
```

# How to configure and run the application.

A csv file must be read into the program via the required -file parameter.

Once configured, an application server can be built with
  * ./gradlew build
  
and then run with
  * java -jar build/libs/spreadsheet-0.0.1-SNAPSHOT.jar -file FILE
  
  An example file is provided at src/test/resources/input.csv.

# How to run the suite of automated tests.

The automated tests are run with gradlew build and can be run separately at the command line with
  * ./gradlew test

# Additional Comments
This application was built using Java 8.  A compliant JRE should be installed and available to run this application.