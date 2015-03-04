class ClassTest extends GroovyTestCase {

    void testClassDefinition() {
        assertScript '''
            // tag::class_definition[]
            class Person {                       //<1>

                String name                      //<2>
                Integer age

                def increaseAge(Integer years) { //<3>
                    this.age += years
                }
            }
            // end::class_definition[]

            // tag::class_instantiation[]
            def p = new Person()
            // end::class_instantiation[]

        '''
    }

    void testInnerClass() {
        assertScript '''
            // tag::inner_class[]
            class Outer {
                private String privateStr

                def callInnerMethod() {
                    new Inner().methodA()       //<1>
                }

                class Inner {                   //<2>
                    def methodA() {
                        println "${privateStr}." //<3>
                    }
                }
            }
            // end::inner_class[]
            def o = new Outer()
        '''
    }

    void testInnerClass2() {
        assertScript '''
            // tag::inner_class2[]
            class Outer2 {
                private String privateStr = 'some string'

                def startThread() {
                   new Thread(new Inner2()).start()
                }

                class Inner2 implements Runnable {
                    void run() {
                        println "${privateStr}."
                    }
                }
            }
            // end::inner_class2[]
            def o2 = new Outer2()
        '''
    }

    void testAnonymousInnerClass() {
        assertScript '''
            // tag::anonymous_inner_class[]
            class Outer3 {
                private String privateStr = 'some string'

                def startThread() {
                    new Thread(new Runnable() {      //<1>
                        void run() {
                            println "${privateStr}."
                        }
                    }).start()                       //<2>
                }
            }
            // end::anonymous_inner_class[]
            def o3 = new Outer3()
        '''
    }

    void testAbstractClass() {
        assertScript '''
            // tag::abstract_class[]
            abstract class Abstract {         //<1>
                String name

                abstract def abstractMethod() //<2>

                def concreteMethod() {
                    println 'concrete'
                }
            }
            // end::abstract_class[]
            def ac = new Abstract() { def abstractMethod() {} }
        '''
    }

    void testConstructorPositionalParameters() {
        assertScript '''
            // tag::constructor_positional_parameters[]
            class PersonConstructor {
                String name
                Integer age

                PersonConstructor(name, age) {          //<1>
                    this.name = name
                    this.age = age
                }
            }

            def person1 = new PersonConstructor('Marie', 1)  //<2>
            def person2 = ['Marie', 2] as PersonConstructor  //<3>
            PersonConstructor person3 = ['Marie', 3]         //<4>
            // end::constructor_positional_parameters[]

            assert person1.name == 'Marie'
            assert person2.name == 'Marie'
            assert person3.name == 'Marie'
        '''
    }

    void testConstructorNamedParameters() {
        assertScript '''
            // tag::constructor_named_parameters[]
            class PersonWOConstructor {                                  //<1>
                String name
                Integer age
            }

            def person4 = new PersonWOConstructor()                      //<2>
            def person5 = new PersonWOConstructor(name: 'Marie')         //<3>
            def person6 = new PersonWOConstructor(age: 1)                //<4>
            def person7 = new PersonWOConstructor(name: 'Marie', age: 2) //<5>
            // end::constructor_named_parameters[]

            assert person4.name == null
        '''
    }

    void testInterfaceDefinition() {
        assertScript '''
            // tag::interface_def_1[]
            interface Greeter {                                         // <1>
                void greet(String name)                                 // <2>
            }
            // end::interface_def_1[]

            // tag::class_implements[]
            class SystemGreeter implements Greeter {                    // <1>
                void greet(String name) {                               // <2>
                    println "Hello $name"
                }
            }

            def greeter = new SystemGreeter()
            assert greeter instanceof Greeter                           // <3>
            // end::class_implements[]
            greeter.greet('Laura')

            // tag::extended_interface[]
            interface ExtendedGreeter extends Greeter {                 // <4>
                void sayBye(String name)
            }
            // end::extended_interface[]

            // tag::no_structural_interface[]
            class DefaultGreeter {
                void greet(String name) { println "Hello" }
            }

            greeter = new DefaultGreeter()
            assert !(greeter instanceof Greeter)
            // end::no_structural_interface[]

            def coerced
            // tag::interface_coercion[]
            greeter = new DefaultGreeter()                              // <1>
            coerced = greeter as Greeter                                // <2>
            assert coerced instanceof Greeter                           // <3>
            // end::interface_coercion[]
        '''

        def err = shouldFail {
            assertScript '''
                // tag::protected_forbidden[]
                interface Greeter {
                    protected void greet(String name)           // <1>
                }
                // end::protected_forbidden[]
                1
            '''
        }
        assert err.contains("Method 'greet' is protected but should be public in interface 'Greeter'")

        err = shouldFail {
            assertScript '''
                // tag::private_forbidden[]
                interface Greeter {
                    private void greet(String name)
                }
                // end::private_forbidden[]
                1
            '''
        }
        assert err.contains("Method 'greet' is private but should be public in interface 'Greeter'")
    }


}
