package org.codehaus.groovy.ast.builder

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.control.CompilerConfiguration

/**
 * This class handles converting Strings to ASTNode lists.
 *
 * @author Hamlet D'Arcy
 */

@PackageScope class AstStringCompiler {
    
    /**
     * Performs the String source to {@link List} of {@link ASTNode}.
     *
     * @param script
     *      a Groovy script in String form
     * @param compilePhase
     *      the int based CompilePhase to compile it to.
     * @param statementsOnly
     */
    List<ASTNode> compile(String script, CompilePhase compilePhase, boolean statementsOnly) {
        def scriptClassName = "script" + System.currentTimeMillis()
        GroovyClassLoader classLoader = new GroovyClassLoader()
        GroovyCodeSource codeSource = new GroovyCodeSource(script, scriptClassName + ".groovy", "/groovy/script")
        CompilationUnit cu = new CompilationUnit(CompilerConfiguration.DEFAULT, codeSource.codeSource, classLoader)
        cu.addSource(codeSource.getName(), script);
        cu.compile(compilePhase.getPhaseNumber())
        // collect all the ASTNodes into the result, possibly ignoring the script body if desired
        return cu.ast.modules.inject([]) {List acc, ModuleNode node ->
            if (node.statementBlock) acc.add(node.statementBlock)
            node.classes?.each {
                if (!(it.name == scriptClassName && statementsOnly)) {
                    acc << it
                }
            }
            acc
        }
    }

}