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
package bugs

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * GROOVY-7444: FactoryBuilderSupport did not expose registered node
 * methods when the builder itself was a static or non-static inner
 * class. Only a top-level builder worked.
 */
final class Groovy7444 {

    @Test
    void testFactoryBuilderSupportAsInnerClass() {
        assertScript '''
            class A {}

            class AFactory extends AbstractFactory {
                Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
                    attributes != null ? new A(attributes) : new A()
                }
            }

            class BuilderOK extends FactoryBuilderSupport {
                BuilderOK(boolean init = true) {
                    super(init)
                }
                def registerObjectFactories() {
                    registerFactory('A', new AFactory())
                    registerFactory('B', new StaticBuilderClassBugTest.BFactory())
                }
            }

            class StaticBuilderClassBugTest {
                static class B {}

                static class BFactory extends AbstractFactory {
                    Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
                        attributes != null ? new B(attributes) : new B()
                    }
                }

                static class BuilderBroken1 extends FactoryBuilderSupport {
                    BuilderBroken1(boolean init = true) {
                        super(init)
                    }
                    def registerObjectFactories() {
                        registerFactory('A', new AFactory())
                        registerFactory('B', new BFactory())
                    }
                }

                class BuilderBroken2 extends FactoryBuilderSupport {
                    BuilderBroken2(boolean init = true) {
                        super(init)
                    }
                    def registerObjectFactories() {
                        registerFactory('A', new AFactory())
                        registerFactory('B', new BFactory())
                    }
                }
            }

            def a = new BuilderOK().A {}
            def b = new BuilderOK().B {}
            assert a instanceof A
            assert b instanceof StaticBuilderClassBugTest.B

            def builderBroken1 = new StaticBuilderClassBugTest.BuilderBroken1()
            def a1 = builderBroken1.A {}
            def b1 = builderBroken1.B {}
            assert a1 instanceof A
            assert b1 instanceof StaticBuilderClassBugTest.B

            def outer = new StaticBuilderClassBugTest()
            def builderBroken2 = new StaticBuilderClassBugTest.BuilderBroken2(outer)
            def a2 = builderBroken2.A {}
            def b2 = builderBroken2.B {}
            assert a2 instanceof A
            assert b2 instanceof StaticBuilderClassBugTest.B
        '''
    }
}
