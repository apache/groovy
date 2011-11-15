package groovy.bugs

class Groovy5122Bug extends GroovyTestCase {
    void testInterfaceFieldShouldBeInitialized() {
        assertScript '''
            import java.lang.reflect.Field

            interface A {
                public static X x = new X() {
                    public void foo() {}
                }
            }

            interface X {
                void foo()
            }

            class B implements A {
                public B() {
                    for (Field f in getClass().getFields()) {
                        println f
                        println f.get(this)
                    }
                }
            }

            new B()
        '''
    }
}
