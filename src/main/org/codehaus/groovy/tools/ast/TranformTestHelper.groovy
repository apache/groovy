package org.codehaus.groovy.ast.builder

import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import java.security.CodeSource
import org.codehaus.groovy.control.CompilationUnit.PrimaryClassNodeOperation
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.ast.ClassNode

/*
* This TestHarness exists so that a global transform can be run without
* using the Jar services mechanism, which requires building a jar.
* 
* To use this simply create an instance of TransformTestHelper with the
* an ASTTransformation and CompilePhase, then invoke parse(File). 
* 
* This test harness is not exactly the same as executing a global transformation
* but can greatly aide in debugging and testing a transform. You should still
* test your global transformation when packaged as a jar service before
* releasing it. 
* 
* @author Hamlet D'Arcy
*/

class TranformTestHelper {

    private ASTTransformation transform
    private CompilePhase phase

    /**
     * Creates the test helper.
     * @param transform
     *      the transform to run when compiling the file later
     * @param phase
     *      the phase to run the transform in 
     */
    def TranformTestHelper(ASTTransformation transform, CompilePhase phase) {
        this.transform = transform
        this.phase = phase
    }

    /**
     * Compiles the File into a Class applying the tranform specified in the constructor.
     * @input input
     *      must be a groovy source file
     */
    public Class parse(File input) {
        TestHarnessClassLoader loader = new TestHarnessClassLoader(transform, phase)
        return loader.parseClass(input)
    }
}

/**
* ClassLoader exists so that TestHarnessOperation can be wired into the compile. 
*
* @author Hamlet D'Arcy
*/
private class TestHarnessClassLoader extends GroovyClassLoader {

    private ASTTransformation transform
    private CompilePhase phase

    TestHarnessClassLoader(ASTTransformation transform, CompilePhase phase) {
        this.transform = transform
        this.phase = phase
    }

    protected CompilationUnit createCompilationUnit(CompilerConfiguration config, CodeSource codeSource) {

        CompilationUnit cu = super.createCompilationUnit(config, codeSource)
        cu.addPhaseOperation(new TestHarnessOperation(transform), phase.getPhaseNumber())
        return cu
    }
}

/**
* Operation exists so that an AstTransformation can be run against the SourceUnit.
*
* @author Hamlet D'Arcy
*/
private class TestHarnessOperation extends PrimaryClassNodeOperation {

    private ASTTransformation transform

    def TestHarnessOperation(transform) {
        this.transform = transform;
    }

    public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
        transform.visit(null, source)
    }
}
