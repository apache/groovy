package org.codehaus.groovy.ast;

import junit.framework.TestCase;

import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;

/**
 * Base class for every TestCase that uses an AST
 * 
 * @author <a href="mailto:martin.kempf@gmail.com">Martin Kempf</a>
 *
 */
public class ASTTest extends TestCase {

	public ModuleNode getAST(String source, int untilPhase) {
		SourceUnit unit = SourceUnit.create("Test",source);
		CompilationUnit compUnit = new CompilationUnit();
		compUnit.addSource(unit);
	    compUnit.compile(untilPhase);
	    return unit.getAST();
	}
	
	public ModuleNode getAST(String source) {
		return getAST(source, Phases.SEMANTIC_ANALYSIS);
	}
}
