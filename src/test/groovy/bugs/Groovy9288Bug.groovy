/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.bugs

import groovy.transform.CompileStatic
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

@CompileStatic
final class Groovy9288Bug {

    @Test
    void "test accessing a protected super class field from inside a closure - different package"() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate('''
            package a
            
            import groovy.transform.CompileStatic
            
            @CompileStatic
            abstract class Abstract_Class {
                protected String protectedField = 'field'
                
                abstract String doThing()
            }
            assert true
        ''')

        shell.evaluate('''
            package b
            
            import a.Abstract_Class
            import groovy.transform.CompileStatic
            
            @CompileStatic
            class ConcreteClass extends Abstract_Class {
               
                @Override
                String doThing() {
                    'something'.with {
                        return protectedField
                    }
                }
            }
            assert true
        ''')

        shell.evaluate("assert new b.ConcreteClass().doThing() == 'field'")
    }

    @Test
    void "test accessing a protected super class field from inside a closure - same package"() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate('''
            package a
            
            import groovy.transform.CompileStatic
            
            @CompileStatic
            abstract class Abstract_Class {
                protected String protectedField = 'field'
                
                abstract String doThing()
            }
            assert true
        ''')

        shell.evaluate('''
            package a
            
            import a.Abstract_Class
            import groovy.transform.CompileStatic
            
            @CompileStatic
            class ConcreteClass extends Abstract_Class {
               
                @Override
                String doThing() {
                    'something'.with {
                        return protectedField
                    }
                }
            }
            assert true
        ''')

        shell.evaluate("assert new a.ConcreteClass().doThing() == 'field'")
    }

    @Test
    void "test accessing a protected super class field from inside a closure - using this - different package"() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate('''
            package a
            
            import groovy.transform.CompileStatic
            
            @CompileStatic
            abstract class Abstract_Class {
                protected String protectedField = 'field'
                
                abstract String doThing()
            }
            assert true
        ''')

        shell.evaluate('''
            package b
            
            import a.Abstract_Class
            import groovy.transform.CompileStatic
            
            @CompileStatic
            class ConcreteClass extends Abstract_Class {
               
                @Override
                String doThing() {
                    'something'.with {
                        return this.protectedField
                    }
                }
            }
            assert true
        ''')

        shell.evaluate("assert new b.ConcreteClass().doThing() == 'field'")
    }


    @Test
    void "test accessing a protected super class field from inside a closure - using thisObject - different package"() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate('''
            package a
            
            import groovy.transform.CompileStatic
            
            @CompileStatic
            abstract class Abstract_Class {
                protected String protectedField = 'field'
                
                abstract String doThing()
            }
            assert true
        ''')

        shell.evaluate('''
            package b
            
            import a.Abstract_Class
            import groovy.transform.CompileStatic
            
            @CompileStatic
            class ConcreteClass extends Abstract_Class {
               
                @Override
                String doThing() {
                    'something'.with {
                        return thisObject.protectedField
                    }
                }
            }
            assert true
        ''')

        shell.evaluate("assert new b.ConcreteClass().doThing() == 'field'")
    }

    @Test
    void "test accessing a protected super class field from inside a closure - using owner - different package"() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate('''
            package a
            
            import groovy.transform.CompileStatic
            
            @CompileStatic
            abstract class Abstract_Class {
                protected String protectedField = 'field'
                
                abstract String doThing()
            }
            assert true
        ''')

        shell.evaluate('''
            package b
            
            import a.Abstract_Class
            import groovy.transform.CompileStatic
            
            @CompileStatic
            class ConcreteClass extends Abstract_Class {
               
                @Override
                String doThing() {
                    'something'.with {
                        return owner.protectedField
                    }
                }
            }
            assert true
        ''')

        shell.evaluate("assert new b.ConcreteClass().doThing() == 'field'")
    }

    @Test
    void "test accessing a protected super class field from inside a closure - using delegate - different package"() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate('''
            package a
            
            import groovy.transform.CompileStatic
            
            @CompileStatic
            abstract class Abstract_Class {
                protected String protectedField = 'field'
                
                abstract String doThing()
            }
            assert true
        ''')

        shell.evaluate('''
            package b
            
            import a.Abstract_Class
            import groovy.transform.CompileStatic
            
            @CompileStatic
            class ConcreteClass extends Abstract_Class {
               
                @Override
                String doThing() {
                    this.with {
                        return delegate.protectedField
                    }
                }
            }
            assert true
        ''')

        shell.evaluate("assert new b.ConcreteClass().doThing() == 'field'")
    }
}
