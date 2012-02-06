package groovy.bugs

class Groovy5272Bug extends GroovyTestCase {
    /**
     * In Groovy-5272, there are chances that the following test fails.
     */
    void testShouldNeverFail() {
        10.times {
            assertScript '''
            public interface InterfaceA {
                String FOO="Foo A";
            }
            public interface InterfaceB extends InterfaceA {
                String FOO="Foo B";
            }

            // Fails randomly
            assert InterfaceA.FOO!=InterfaceB.FOO
            '''
        }
    }

    void testResolvingAmbiguousStaticFieldShouldAlwaysReturnTheSameValue() {
        10.times {
        assertScript '''
            public interface InterfaceA {
                String FOO="Foo A";
            }
            public interface InterfaceB extends InterfaceA {
                String FOO="Foo B";
            }
            public interface InterfaceC extends InterfaceA {
                String FOO="Foo C";
            }

            class A implements InterfaceB, InterfaceC {
            }

            assert A.FOO == "Foo C"
            '''
        }

    }
}
