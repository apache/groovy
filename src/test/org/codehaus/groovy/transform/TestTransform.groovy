package org.codehaus.groovy.transform

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit

/**
 * Created by IntelliJ IDEA.
 * User: Danno.Ferrin
 * Date: Apr 23, 2008
 * Time: 7:52:24 PM
 * To change this template use File | Settings | File Templates.
 */
class TestTransform implements ASTTransformation {

    static List<ASTNode[]> visitedNodes = []
    static List<CompilePhase> phases = []

    public void visit(ASTNode[] nodes, SourceUnit source) {
        visitedNodes += nodes
        phases += CompilePhase.phases[source.getPhase()]
    }

}

@GroovyASTTransformation(phase=CompilePhase.CONVERSION)
class TestTransformConversion extends TestTransform {

}

@GroovyASTTransformation(phase=CompilePhase.CLASS_GENERATION)
class TestTransformClassGeneration extends TestTransform {

}