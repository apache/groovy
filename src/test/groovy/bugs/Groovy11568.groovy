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

final class Groovy11568 {
    @Test
    void testPrivateMethodWithCustomMetaClass() {
        assertScript '''
            class CMC extends MetaClassImpl {
                CMC(Class theClass) {
                    super(theClass)
                }
                @Override
                Object invokeMethod(Object object, String methodName, Object arguments) {
                    super.invokeMethod(object, methodName, arguments)
                }
                @Override
                Object invokeMethod(Object object, String methodName, Object[] originalArguments) {
                    super.invokeMethod(object, methodName, originalArguments)
                }
                @Override
                Object invokeMethod(Class sender, Object object, String methodName, Object[] arguments, boolean isCallToSuper, boolean fromInsideClass) {
                    super.invokeMethod(sender, object, methodName, arguments, isCallToSuper, fromInsideClass)
                }
            }

            class C {
                def publicMethod() {
                    privateMethod()
                }
                private privateMethod() {
                    'C#privateMethod()'
                }
            }

            class D extends C {
            }

            def cmc = new CMC(D.class)
            cmc.initialize()
            def obj = new D()
            obj.setMetaClass(cmc)
            def str = obj.publicMethod()
            assert str == 'C#privateMethod()'
        '''
    }
}
