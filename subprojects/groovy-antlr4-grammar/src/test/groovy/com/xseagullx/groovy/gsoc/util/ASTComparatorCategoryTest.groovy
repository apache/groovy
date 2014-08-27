package com.xseagullx.groovy.gsoc.util

import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.control.CompilePhase

class ASTComparatorCategoryTest extends GroovyTestCase {
    class A {
        String a
    }

    void testReflexiveEquality() {
        assert ASTComparatorCategory.reflexiveEquals(new A(a:"1234"), new A(a:"1234"))
        //noinspection GroovyPointlessBoolean
        assert ASTComparatorCategory.reflexiveEquals(new A(a:"1234"), new A(a:"5678")) == false // It's more readable than !
    }

    void testSimpleAST() {
        def c = new AstBuilder().buildFromCode { 1 + 3 }
        def a = new AstBuilder().buildFromCode { 1 + 2 }
        def b = new AstBuilder().buildFromCode { 1 + 2 }

        use(ASTComparatorCategory) {
            assert a == b
            assert a != c
        }
    }

    void testClassDeclarationAST() {
        def source1 = """
            class A {
                def a() {}
                def c = 5;
            }
        """

        def source2 = """
            class A {
                abstract def a()
                def c = 6
            }
        """

        def a = new AstBuilder().buildFromString CompilePhase.SEMANTIC_ANALYSIS, source1
        def b = new AstBuilder().buildFromString CompilePhase.SEMANTIC_ANALYSIS, source1
        def c = new AstBuilder().buildFromString CompilePhase.SEMANTIC_ANALYSIS, source2

        ASTComparatorCategory.apply {
            assert a == b
        }
        ASTComparatorCategory.apply {
            assert a != c
        }
    }

}
