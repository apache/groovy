package org.codehaus.groovy.ast.builder.testpackage

import org.codehaus.groovy.ast.builder.*
import org.codehaus.groovy.runtime.MethodClosure

/**
 * Test package imports in AstBuilder.
 * 
 * It is important that this class contains an import of builder.* and not 
 * the AstBuilder class individually. 
 *
 * @author Hamlet D'Arcy
 */
@WithAstBuilder
public class AstBuilderFromCodePackageImportTest extends GroovyTestCase {

    public void testPackageImport() {

        def expected = new AstBuilder().buildFromString(""" println "Hello World" """)

        def result = new AstBuilder().buildFromCode {
            println "Hello World"
        }
        AstAssert.assertSyntaxTree(expected, result)
    }

}
