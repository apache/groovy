package org.codehaus.groovy.tools.shell.util

import groovy.transform.TypeChecked
import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.DynamicVariable
import org.codehaus.groovy.ast.GroovyClassVisitor
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.*

import java.security.CodeSource

/**
 * Class to Class parsing a script to detect all bound and unbound variables.
 * Based on http://glaforge.appspot.com/article/knowing-which-variables-are-bound-or-not-in-a-groovy-script
 */
@TypeChecked
class ScriptVariableAnalyzer {

    protected final Logger log = Logger.create(ScriptVariableAnalyzer.class)

    /**
     * define a visitor that visits all variable expressions
     */
    static class VariableVisitor extends ClassCodeVisitorSupport implements GroovyClassVisitor {
        Set<String> bound = new HashSet<String>()
        Set<String> unbound = new HashSet<String>()

        void visitVariableExpression(VariableExpression expression) {
            // we're not interested in some special implicit variables
            if (!(expression.variable in ['args', 'context', 'this', 'super'])) {
                // thanks to this instanceof
                // we know if the variable is bound or not
                if (expression.accessedVariable instanceof DynamicVariable) {
                    unbound << expression.variable
                } else {
                    bound << expression.variable
                }
            }
            super.visitVariableExpression(expression)
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }
    }

    /**
     * custom PrimaryClassNodeOperation
     * to be able to hook our code visitor
     */
    static class VisitorSourceOperation extends CompilationUnit.PrimaryClassNodeOperation {

        GroovyClassVisitor visitor

        VisitorSourceOperation(GroovyClassVisitor visitor) {
            this.visitor = visitor
        }

        void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
            classNode.visitContents(visitor)
        }
    }

    /**
     * class loader to add our phase operation
     */
    static class VisitorClassLoader extends GroovyClassLoader {
        GroovyClassVisitor visitor

        public VisitorClassLoader(GroovyClassVisitor visitor) {
            this.visitor = visitor
        }

        protected CompilationUnit createCompilationUnit(CompilerConfiguration config, CodeSource source) {
            CompilationUnit cu = super.createCompilationUnit(config, source)
            cu.addPhaseOperation(new VisitorSourceOperation(visitor), Phases.CLASS_GENERATION)
            return cu
        }
    }

    static Set<String> getBoundVars(String scriptText) {
        assert scriptText != null
        GroovyClassVisitor visitor = new VariableVisitor()
        VisitorClassLoader myCL = new VisitorClassLoader(visitor)
        // simply by parsing the script with our classloader
        // our visitor will be called and will visit all the variables
        myCL.parseClass(scriptText)
        return visitor.bound
    }

}
