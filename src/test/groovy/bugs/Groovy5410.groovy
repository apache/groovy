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

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized)
final class Groovy5410 {

    @Parameterized.Parameter
    public boolean invokeDynamic

    @Parameterized.Parameters
    static Iterable parameters() {
        [Boolean.FALSE, Boolean.TRUE]
    }

    @Test
    void testDynamicProxy() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            optimizationOptions: [indy: invokeDynamic],
            jointCompilationOptions: [memStub: Boolean.TRUE]
        )
        def parentDir = File.createTempDir()
        try {
            def a = new File(parentDir, 'A.java')
            a.write '''
                interface JavaParentIfc<X> {
                    void parentMethod(X input);
                    void parentMethod2(ChildGroovyObject input);
                }

                interface JavaChildExtendsJavaParentIfc extends JavaParentIfc<ChildGroovyObject> {
                    void childMethod(ChildGroovyObject input);
                }

                class JavaChildExtendsJavaParentImpl implements JavaChildExtendsJavaParentIfc {
                    @Override
                    public void childMethod(ChildGroovyObject input) {
                        System.out.println("in child method impl with input: " + input.getTestString());
                    }
                    @Override
                    public void parentMethod(ChildGroovyObject input) {
                        System.out.println("in parent method impl with input: " + input.getTestString());
                    }
                    @Override
                    public void parentMethod2(ChildGroovyObject input) {
                        System.out.println("in parent method 2(not generic) impl with input: " + input.getTestString());
                    }
                }

                class ProxyInvocationHandler implements java.lang.reflect.InvocationHandler {
                    public ProxyInvocationHandler(Object realChild) {
                        this.realChild = realChild;
                    }
                    Object realChild;

                    @Override
                    public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
                        try {
                            return method.invoke(realChild, args);
                        } catch (Exception e) {
                            return null;
                        }
                    }
                }
            '''
            def b = new File(parentDir, 'B.groovy')
            b.write '''
                class ChildGroovyObject {
                    String testString
                }

                interface GroovyParentIfc<Y> {
                    void parentMethod(Y input)
                    void parentMethod2(ChildGroovyObject input)
                }

                interface GroovyChildExtendsGroovyParentIfc extends GroovyParentIfc<ChildGroovyObject> {
                    void childMethod(ChildGroovyObject input)
                }

                class GroovyChildExtendsGroovyParentImpl implements GroovyChildExtendsGroovyParentIfc {
                    @Override
                    void childMethod(ChildGroovyObject input) {
                        println "in child method impl with input: $input.testString"
                    }
                    @Override
                    void parentMethod(ChildGroovyObject input) {
                        println "in parent method impl with input: $input.testString"
                    }
                    @Override
                    void parentMethod2(ChildGroovyObject input) {
                        println "in parent method 2(not generic) impl with input: $input.testString"
                    }
                }

                interface GroovyChildExtendsJavaParentIfc extends JavaParentIfc<ChildGroovyObject> {
                    void childMethod(ChildGroovyObject input)
                }

                class GroovyChildExtendsJavaParentImpl implements GroovyChildExtendsJavaParentIfc {
                    @Override
                    void childMethod(ChildGroovyObject input) {
                        println "in child method impl with input: $input.testString"
                    }
                    @Override
                    void parentMethod(ChildGroovyObject input) {
                        println "in parent method impl with input: $input.testString"
                    }
                    @Override
                    void parentMethod2(ChildGroovyObject input) {
                        println "in parent method 2(not generic) impl with input: $input.testString"
                    }
                }
            '''
            def c = new File(parentDir, 'C.groovy')
            c.write '''
                import static java.lang.reflect.Proxy.newProxyInstance

                void 'WORKS - call concrete child method'() {
                    def proxiedChild = new GroovyChildExtendsGroovyParentImpl()
                    ProxyInvocationHandler handler = new ProxyInvocationHandler(proxiedChild)
                    Class[] interfacesArray = [GroovyChildExtendsGroovyParentIfc, GroovyParentIfc, GroovyObject]
                    GroovyChildExtendsGroovyParentIfc child = (GroovyChildExtendsGroovyParentIfc) newProxyInstance(GroovyChildExtendsGroovyParentIfc.class.classLoader, interfacesArray, handler)

                    child.childMethod(new ChildGroovyObject(testString: 'calling child method from spec'))
                }

                void 'WORKS - call concrete parent method'() {
                    def proxiedChild = new GroovyChildExtendsGroovyParentImpl()
                    ProxyInvocationHandler handler = new ProxyInvocationHandler(proxiedChild)
                    Class[] interfacesArray = [GroovyChildExtendsGroovyParentIfc, GroovyParentIfc, GroovyObject]
                    GroovyChildExtendsGroovyParentIfc child = (GroovyChildExtendsGroovyParentIfc) newProxyInstance(GroovyChildExtendsGroovyParentIfc.class.classLoader, interfacesArray, handler)

                    child.parentMethod2(new ChildGroovyObject(testString: 'calling child method from spec'))
                }

                void 'ISSUE - call generic groovy parent interface method that throws a ClassCastException but should not'() {
                    def proxiedChild = new GroovyChildExtendsGroovyParentImpl()
                    ProxyInvocationHandler handler = new ProxyInvocationHandler(proxiedChild)
                    Class[] interfacesArray = [GroovyChildExtendsGroovyParentIfc, GroovyParentIfc, GroovyObject]
                    GroovyChildExtendsGroovyParentIfc child = (GroovyChildExtendsGroovyParentIfc) newProxyInstance(GroovyChildExtendsGroovyParentIfc.class.classLoader, interfacesArray, handler)

                    child.parentMethod(new ChildGroovyObject(testString: 'calling parent method from spec')) // line 28
                }

                void 'ISSUE - call generic java parent interface method that throws a ClassCastException but should not'() {
                    def proxiedChild = new GroovyChildExtendsJavaParentImpl()
                    ProxyInvocationHandler handler = new ProxyInvocationHandler(proxiedChild)
                    Class[] interfacesArray = [GroovyChildExtendsJavaParentIfc, JavaParentIfc, GroovyObject]
                    GroovyChildExtendsJavaParentIfc child = (GroovyChildExtendsJavaParentIfc) newProxyInstance(GroovyChildExtendsJavaParentIfc.class.classLoader, interfacesArray, handler)

                    child.parentMethod(new ChildGroovyObject(testString: 'calling parent method from spec')) // line 37
                }

                void 'ISSUE - call generic java parent interface method when the child interface is also java and this also throws a ClassCastException because GroovyObject is passed as an interface to the Proxy'() {
                    def proxiedChild = new JavaChildExtendsJavaParentImpl()
                    ProxyInvocationHandler handler = new ProxyInvocationHandler(proxiedChild)

                    // It might seem strange that I'm including GroovyObject as an interface here, but when Spring proxies it's beans,
                    // it looks at the interfaces of the bean class which happens to be written in Groovy and therefore is an implicit
                    // implementation of a GroovyObject
                    Class[] interfacesArray = [JavaChildExtendsJavaParentIfc, JavaParentIfc, GroovyObject]
                    JavaChildExtendsJavaParentIfc child = (JavaChildExtendsJavaParentIfc) newProxyInstance(JavaChildExtendsJavaParentIfc.class.classLoader, interfacesArray, handler)

                    child.parentMethod(new ChildGroovyObject(testString: 'calling parent method from spec')) // line 50
                }

                'WORKS - call concrete child method'()
                'WORKS - call concrete parent method'()
                'ISSUE - call generic groovy parent interface method that throws a ClassCastException but should not'()
                'ISSUE - call generic java parent interface method that throws a ClassCastException but should not'()
                'ISSUE - call generic java parent interface method when the child interface is also java and this also throws a ClassCastException because GroovyObject is passed as an interface to the Proxy'()
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b, c)
            cu.compile()

            loader.loadClass('C', true).main()
        } finally {
            config.targetDirectory.deleteDir()
            parentDir.deleteDir()
        }
    }
}
