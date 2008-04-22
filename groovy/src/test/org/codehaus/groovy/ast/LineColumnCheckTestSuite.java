package org.codehaus.groovy.ast;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import junit.framework.TestSuite;

/**
 * Tests the LineColumn information in file specified in TEST_FILE_PATH
 * 
 * The file in the specified path should look like:
 * 
 * ###<testname>:::
 * <source code from which the AST will be built>
 * :::<expected AST output>
 * 
 * The section above can be repeated for every new TestCase
 * 
 * The AST output from the visitor is quite big. Also for small source code snippets. Therefore
 * it is possible to only specify the nodes that you wan't to check and separate them with a semicolon.
 * A semicolon is also needed when you begin with a new line.
 * Bsp: 
 * [TryCatchStatement,(1:1),(9:2)][BlockStatement,(1:5),(3:3)];
 * [CatchStatement,(3:3),(5:3)][BlockStatement,(3:12),(5:3)];
 * [CatchStatement,(5:3),(7:3)][BlockStatement,(5:12),(7:3)];
 * [BlockStatement,(7:3),(9:2)][BlockStatement,(7:11),(9:2)]
 * 
 * [<NodeType>,(<line>:<column>),(<lastLine>:<lastColumn>)]
 * 
 * @author <a href="mailto:martin.kempf@gmail.com">Martin Kempf</a>
 *
 */
public class LineColumnCheckTestSuite extends TestSuite {

	public static final String TEST_FILE_PATH = "./src/test/org/codehaus/groovy/ast/LineColumnCheck.txt";

	public static TestSuite suite() {
		TestSuite ts = new TestSuite("LineColumnCheckTestSuite");
		String content = "";
		try {
			content = getContent(new FileReader(TEST_FILE_PATH));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		String[] tests = content.split("###");
		String testName = "";
		String source = "";
		String expected = "";
		for (int i = 0; i < tests.length; i++) {
			String[] slicedTests = tests[i].split(":::");
			for (int ii = 0; ii < slicedTests.length; ii++) {
				if (ii%3 == 0) {
					testName = slicedTests[ii];
				} else if (ii%3 == 1) {
					source = slicedTests[ii].trim();
				} else if (ii%3 == 2) {
					expected = slicedTests[ii].trim();
					ts.addTest(new LineColumnChecker(testName, source, expected));
				}
			}
		}
		return ts;
	}

	private static String getContent(Reader reader) {
		int character;
		StringBuffer content = new StringBuffer();
		try {
			while ((character = reader.read()) != -1) {
				content.append((char) character);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content.toString();
	}

}
