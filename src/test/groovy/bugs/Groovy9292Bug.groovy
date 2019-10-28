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
import org.junit.Ignore
import org.junit.Test

@CompileStatic
final class Groovy9292Bug {

    @Test
    void "test accessing a protected super class field from inside a closure - different package"() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate('''
            package a
            
            import groovy.transform.CompileStatic
            
            @CompileStatic
            abstract class Abstract_Class {
                protected String superField = 'field'
                
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
                        return superField
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
                protected String superField = 'field'
                
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
                        return superField
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
                protected String superField = 'field'
                
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
                        return this.superField
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
                protected String superField = 'field'
                
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
                        return thisObject.superField
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
                protected String superField = 'field'
                
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
                        return owner.superField
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
                protected String superField = 'field'
                
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
                        return delegate.superField
                    }
                }
            }
            assert true
        ''')

        shell.evaluate("assert new b.ConcreteClass().doThing() == 'field'")
    }

    @Test
    void "test accessing a package-private super class field from inside a closure - same package"() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate('''
            package a
            
            import groovy.transform.CompileStatic
            
            @CompileStatic
            abstract class Abstract_Class {
                @groovy.transform.PackageScope String superField = 'field'
                
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
                    this.with {
                        return delegate.superField
                    }
                }
            }
            assert true
        ''')

        shell.evaluate("assert new a.ConcreteClass().doThing() == 'field'")
    }

    @Test
    void "test accessing a package-private super class field from inside a closure - using delegate - same package"() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate('''
            package a
            
            import groovy.transform.CompileStatic
            
            @CompileStatic
            abstract class Abstract_Class {
                @groovy.transform.PackageScope String superField = 'field'
                
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
                    this.with {
                        return delegate.superField
                    }
                }
            }
            assert true
        ''')

        shell.evaluate("assert new a.ConcreteClass().doThing() == 'field'")
    }

    @Ignore // java.lang.IllegalAccessError: class a.ConcreteClass$_doThing_closure1 tried to access field a.Abstract_Class.superField (a.ConcreteClass$_doThing_closure1 is in unnamed module of loader groovy.lang.GroovyClassLoader$InnerLoader @5fa47fea; a.Abstract_Class is in unnamed module of loader groovy.lang.GroovyClassLoader$InnerLoader @28cda624)
    @Test
    void "test accessing a package-private super class field from inside a closure - using this - same package"() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate('''
            package a
            
            import groovy.transform.CompileStatic
            
            @CompileStatic
            abstract class Abstract_Class {
                @groovy.transform.PackageScope String superField = 'field'
                
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
                    this.with {
                        return this.superField
                    }
                }
            }
            assert true
        ''')

        shell.evaluate("assert new a.ConcreteClass().doThing() == 'field'")
    }

    @Test
    void "test accessing a package-private super class field from inside a closure - using thisObject - same package"() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate('''
            package a
            
            import groovy.transform.CompileStatic
            
            @CompileStatic
            abstract class Abstract_Class {
                @groovy.transform.PackageScope String superField = 'field'
                
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
                    this.with {
                        return thisObject.superField
                    }
                }
            }
            assert true
        ''')

        shell.evaluate("assert new a.ConcreteClass().doThing() == 'field'")
    }

    @Test
    void "test accessing a package-private super class field from inside a closure - using owner - same package"() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate('''
            package a
            
            import groovy.transform.CompileStatic
            
            @CompileStatic
            abstract class Abstract_Class {
                @groovy.transform.PackageScope String superField = 'field'
                
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
                    this.with {
                        return owner.superField
                    }
                }
            }
            assert true
        ''')

        shell.evaluate("assert new a.ConcreteClass().doThing() == 'field'")
    }


    @Test
    void "test accessing a public super class field from inside a closure - different package -- regression"() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate('''
            package a
            
            import groovy.transform.CompileStatic
            
            @CompileStatic
            abstract class Abstract_Class {
                public String superField = 'field'
                
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
                        return superField
                    }
                }
            }
            assert true
        ''')

        shell.evaluate("assert new b.ConcreteClass().doThing() == 'field'")
    }

    @Test
    void "test accessing a public super class field from inside a closure - using delegate - different package"() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate('''
            package a
            
            import groovy.transform.CompileStatic
            
            @CompileStatic
            abstract class Abstract_Class {
                public String superField = 'field'
                
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
                        return delegate.superField
                    }
                }
            }
            assert true
        ''')

        shell.evaluate("assert new b.ConcreteClass().doThing() == 'field'")
    }

    @Test
    void "test accessing a public super class field from inside a closure - using this - different package"() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate('''
            package a
            
            import groovy.transform.CompileStatic
            
            @CompileStatic
            abstract class Abstract_Class {
                public String superField = 'field'
                
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
                        return this.superField
                    }
                }
            }
            assert true
        ''')

        shell.evaluate("assert new b.ConcreteClass().doThing() == 'field'")
    }

    @Test
    void "test accessing a public super class field from inside a closure - using thisObject - different package"() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate('''
            package a
            
            import groovy.transform.CompileStatic
            
            @CompileStatic
            abstract class Abstract_Class {
                public String superField = 'field'
                
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
                        return thisObject.superField
                    }
                }
            }
            assert true
        ''')

        shell.evaluate("assert new b.ConcreteClass().doThing() == 'field'")
    }

    @Test
    void "test accessing a public super class field from inside a closure - using owner - different package"() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate('''
            package a
            
            import groovy.transform.CompileStatic
            
            @CompileStatic
            abstract class Abstract_Class {
                public String superField = 'field'
                
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
                        return owner.superField
                    }
                }
            }
            assert true
        ''')

        shell.evaluate("assert new b.ConcreteClass().doThing() == 'field'")
    }


    @Test
    void "test accessing a private super class field from inside a closure via getter - different package -- regression"() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate('''
            package a
            
            import groovy.transform.CompileStatic
            
            @CompileStatic
            abstract class Abstract_Class {
                String superField = 'field'
                
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
                        return superField
                    }
                }
            }
            assert true
        ''')

        shell.evaluate("assert new b.ConcreteClass().doThing() == 'field'")
    }


    @Test
    void "test accessing a private super class field from inside a closure via getter - using delegate - different package -- regression"() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate('''
            package a
            
            import groovy.transform.CompileStatic
            
            @CompileStatic
            abstract class Abstract_Class {
                String superField = 'field'
                
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
                        return delegate.superField
                    }
                }
            }
            assert true
        ''')

        shell.evaluate("assert new b.ConcreteClass().doThing() == 'field'")
    }


    @Test
    void "test accessing a private super class field from inside a closure via getter - using this - different package -- regression"() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate('''
            package a
            
            import groovy.transform.CompileStatic
            
            @CompileStatic
            abstract class Abstract_Class {
                String superField = 'field'
                
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
                        return this.superField
                    }
                }
            }
            assert true
        ''')

        shell.evaluate("assert new b.ConcreteClass().doThing() == 'field'")
    }

    @Test
    void "test accessing a private super class field from inside a closure via getter - using thisObject - different package -- regression"() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate('''
            package a
            
            import groovy.transform.CompileStatic
            
            @CompileStatic
            abstract class Abstract_Class {
                String superField = 'field'
                
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
                        return thisObject.superField
                    }
                }
            }
            assert true
        ''')

        shell.evaluate("assert new b.ConcreteClass().doThing() == 'field'")
    }

    @Test
    void "test accessing a private super class field from inside a closure via getter - using owner - different package -- regression"() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate('''
            package a
            
            import groovy.transform.CompileStatic
            
            @CompileStatic
            abstract class Abstract_Class {
                String superField = 'field'
                
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
                        return owner.superField
                    }
                }
            }
            assert true
        ''')

        shell.evaluate("assert new b.ConcreteClass().doThing() == 'field'")
    }
}
