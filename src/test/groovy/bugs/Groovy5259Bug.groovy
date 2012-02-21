package groovy.bugs

class Groovy5259Bug extends GroovyTestCase {
    void testInnerClassAccessingOuterClassConstant() {
        // using a script because the bug is a compiler error
        assertScript '''
            class InnerAccessOuter {
                static final String OUTER_CONSTANT = 'Constant Value'

                class InnerClass {
                    InnerClass() {
                    }

                    String innerCompiled() {
                        OUTER_CONSTANT
                    }
                }

                void testInnerClassAccessOuter() {
                    def inner = new InnerClass()
                    assert OUTER_CONSTANT == inner.innerCompiled()
                }
            }
            new InnerAccessOuter().testInnerClassAccessOuter()
        '''

    }

    void testInnerClassWithWrongCallToSuperAccessingOuterClassConstant() {
        // using a script because the bug is a compiler error
        shouldFail {
            assertScript '''
            class InnerAccessOuter {
                protected static final String OUTER_CONSTANT = 'Constant Value'

                class InnerClass {
                    InnerClass() {
                        // there's no Object#<init>(String) method, but it throws a VerifyError when a new instance
                        // is created, meaning a wrong super call is generated
                        super(OUTER_CONSTANT)
                    }
                    String m() {
                         OUTER_CONSTANT
                    }
                }

                void testInnerClassAccessOuter() {
                    def inner = new InnerClass()
                    inner.m()
                }
            }
            new InnerAccessOuter().testInnerClassAccessOuter()
        '''
        }
    }

    void testInnerClassWithSuperClassAccessingOuterClassConstant() {
        // using a script because the bug is a compiler error
        assertScript '''
            class Base {
                Base(String str) {}
            }
            class InnerAccessOuter {
                static final String OUTER_CONSTANT = 'Constant Value'

                class InnerClass extends Base {
                    InnerClass() {
                        super(OUTER_CONSTANT)
                    }

                    String innerCompiled() { OUTER_CONSTANT }
                }

                void testInnerClassAccessOuter() {
                    def inner = new InnerClass() // throws a VerifyError
                    assert OUTER_CONSTANT == inner.innerCompiled()
                }
            }
            new InnerAccessOuter().testInnerClassAccessOuter()
        '''

    }

    void testInnerClassWithSuperClassAccessingSuperOuterClassConstant() {
        // using a script because the bug is a compiler error
        assertScript '''
            class Base {
                Base(String str) {}
            }
            class OuterBase {
                protected static final String OUTER_CONSTANT = 'Constant Value'
            }
            class InnerAccessOuter extends OuterBase {

                class InnerClass extends Base {
                    InnerClass() {
                        super(OUTER_CONSTANT)
                    }

                    String innerCompiled() { OUTER_CONSTANT }
                }

                void testInnerClassAccessOuter() {
                    def inner = new InnerClass() // throws a VerifyError
                    assert OUTER_CONSTANT == inner.innerCompiled()
                }
            }
            new InnerAccessOuter().testInnerClassAccessOuter()
        '''

    }
}
