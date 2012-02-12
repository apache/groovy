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

    void testShouldNeverFail2() {
        10.times {
            assertScript '''
            public interface InterfaceA {
                String FOO="Foo A";
            }
            public interface AnotherInterface extends InterfaceA {
                String FOO="Foo B";
            }

            // Fails randomly
            assert InterfaceA.FOO!=AnotherInterface.FOO
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
    
    void testResolveConstantInSuperInterfaceWithExpando() {
        assertScript '''
            ExpandoMetaClass.enableGlobally()
            interface Foo {
                String FOO = 'FOO'
            }
            interface Bar extends Foo { }
            assert Bar.FOO == 'FOO'
            ExpandoMetaClass.disableGlobally()
        '''
    }

    void testResolveConstantInSuperInterfaceWithoutExpando() {
        assertScript '''
            interface Foo {
                String FOO = 'FOO'
            }
            interface Bar extends Foo { }
            assert Bar.FOO == 'FOO'
        '''
    }

    void testResolveConstantInClassWithSuperInterfaceWithoutExpando() {
        assertScript '''
            interface Foo {
                String FOO = 'FOO'
            }
            interface Bar extends Foo { }
            class Baz implements Bar {}
            assert Baz.FOO == 'FOO'
        '''
    }

    void testResolveConstantInClassWithSuperInterfaceWithExpando() {
        assertScript '''
            ExpandoMetaClass.enableGlobally()
            interface Foo {
                String FOO = 'FOO'
            }
            interface Bar extends Foo { }
            class Baz implements Bar {}
            assert Baz.FOO == 'FOO'
            ExpandoMetaClass.disableGlobally()
        '''
    }

}
