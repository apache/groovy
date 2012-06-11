package groovy.transform.stc

/**
 * Unit tests for static type checking : bug fixes.
 *
 * @author Cedric Champeau
 */
class BugsSTCTest extends StaticTypeCheckingTestCase {
    // GROOVY-5456
    void testShouldNotAllowDivOnUntypedVariable() {
        shouldFailWithMessages '''
            def foo(Closure cls) {}
            def bar() { foo { it / 2 } }
        ''', 'Cannot find matching method java.lang.Object#div(int)'
    }

    void testGroovy5444() {
        assertScript '''
                def curr = { System.currentTimeMillis() }

                5.times {
                    @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                        assert node.getNodeMetaData(DECLARATION_INFERRED_TYPE) == long_TYPE
                    })
                    def t0 = curr()
                    100000.times {
                        "echo"
                    }
                    println (curr() - t0)
                }'''

    }

    void testGroovy5487ReturnNull() {
        assertScript '''
        @ASTTest(phase=INSTRUCTION_SELECTION, value= {
            assert node.getNodeMetaData(INFERRED_RETURN_TYPE) == make(List)
        })
        List getList() {
            null
        }
        '''
    }

    void testGroovy5487ReturnNullWithExplicitReturn() {
        assertScript '''
        @ASTTest(phase=INSTRUCTION_SELECTION, value= {
            assert node.getNodeMetaData(INFERRED_RETURN_TYPE) == make(List)
        })
        List getList() {
            return null
        }
        '''
    }

    void testGroovy5487ReturnNullWithEmptyBody() {
        assertScript '''
        @ASTTest(phase=INSTRUCTION_SELECTION, value= {
            assert node.getNodeMetaData(INFERRED_RETURN_TYPE) == make(List)
        })
        List getList() {
        }
        '''
    }
}
