package org.codehaus.groovy.classgen.asm

import static org.codehaus.groovy.control.CompilerConfiguration.DEFAULT as config

/**
 * @author Guillaume Laforge
 */
class BinaryOperationsTest extends AbstractBytecodeTestCase {
    
    void testIntPlus() {
        if (config.optimizationOptions.indy) return;
        assert compile("""\
            int i = 1
            int j = 2
            int k = i + j
        """).hasSequence([
                "ILOAD",
                "ILOAD",
                "IADD"
        ])
    }
    
    void testIntCompareLessThan() {
        if (config.optimizationOptions.indy) return;
        assert compile("""\
            int i = 0
            if (i < 100) println "true"
        """).hasSequence([
                "ILOAD",
                "BIPUSH 100",
                "IF_ICMPGE"
        ])
    }
    
    void testCompareLessThanInClosure() {
        if (config.optimizationOptions.indy) return;
        // GROOVY-4741
        assert """
            int a = 0
            [].each {
                if (a < 0) {}
            }
            true
        """
    }
    
    void testLongLeftShift() {
        if (config.optimizationOptions.indy) return;
        assert compile("""\
            long a = 1
            long b = a << 32
        """).hasStrictSequence([
                "BIPUSH 32",
                "LSHL"
        ])
    }

    void testIntConstants() {
        if (config.optimizationOptions.indy) return;
        (0..5).each {
            assert compile("""\
                int a = $it
            """).hasStrictSequence([
                    "ICONST_$it",
            ])
        }
        [-1, 6,Byte.MIN_VALUE,Byte.MAX_VALUE].each {
            assert compile("""\
                    int a = $it
                """).hasStrictSequence([
                    "BIPUSH",
            ])
        }
        [Byte.MIN_VALUE-1,Byte.MAX_VALUE+1,Short.MIN_VALUE,Short.MAX_VALUE].each {
            assert compile("""\
                    int a = $it
                """).hasStrictSequence([
                    "SIPUSH",
            ])
        }
        [Short.MAX_VALUE+1,Integer.MAX_VALUE].each {
            assert compile("""\
                    int a = $it
                """).hasStrictSequence([
                    "LDC",
            ])
        }
    }

    void testCharXor() {
        if (config.optimizationOptions.indy) return;
        assert compile("""
            int i = ('a' as char) ^ ('b' as char) 
        """).hasStrictSequence ([
            "IXOR"
        ])
    }

    void testPrimitiveOrAssign() {
        ['byte','int','short','long'].each { type ->
            assertScript """
            $type[] b = new $type[1]
            b[0] = 16
            b[0] |= 2
            assert b[0] == 18:"Failure for type $type"
            """
            }
    }

    void testPrimitiveAndAssign() {
        ['byte','int','short','long'].each { type ->
            assertScript """
            $type[] b = new $type[1]
            b[0] = 18
            b[0] &= 2
            assert b[0] == 2:"Failure for type $type"
            """
            }
    }
}
