package org.codehaus.groovy.classgen.asm

/**
 * @author Guillaume Laforge
 */
class BinaryOperationsTest extends AbstractBytecodeTestCase {
    
    void testIntPlus() {
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
        assert compile("""\
            long a = 1
            long b = a << 32
        """).hasStrictSequence([
                "BIPUSH 32",
                "LSHL"
        ])
    }

    void testIntConstants() {
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
        assert compile("""
            int i = ('a' as char) ^ ('b' as char) 
        """).hasStrictSequence ([
            "IXOR"
        ])
    }
}
