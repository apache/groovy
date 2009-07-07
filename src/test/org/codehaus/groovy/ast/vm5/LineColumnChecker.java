package org.codehaus.groovy.ast.vm5;

import org.codehaus.groovy.ast.ASTTest;

/**
 * Tests the LineColumn information of the groovy source obtained in the
 * source parameter of the constructor
 * 
 * @author <a href="mailto:martin.kempf@gmail.com">Martin Kempf</a>
 *
 */
public class LineColumnChecker extends ASTTest {
	
	private LineCheckVisitor visitor;
	private String name;
	private String source;
	private String[] expected;
	
	public LineColumnChecker(String name, String source, String expected) {
		this.name = name;
		this.source = source;
		this.expected = expected.split(";");
		// Set Method to call for JUnit
		setName("testLineColumn");
	}
	
	public void setUp() {
		visitor = new LineCheckVisitor();
	}
	
	public String getName() {
		return name;
	}
	
	public void testLineColumn() {
		visitor.visitModuleNode(getAST(source));
		String was = visitor.getASTString();
		//comment out next line to view the output of the visitor
		//System.out.println(name + ": " + was);
		for (int i = 0; i < expected.length; i++) {
			assertTrue("'"+ expected[i] + "' not found in '" + was + "'", was.indexOf(expected[i].trim()) != -1);
		}
	}
}

