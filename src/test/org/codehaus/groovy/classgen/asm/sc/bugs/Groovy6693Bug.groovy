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
package org.codehaus.groovy.classgen.asm.sc.bugs

import groovy.transform.stc.StaticTypeCheckingTestCase
import org.codehaus.groovy.classgen.asm.sc.StaticCompilationTestSupport

class Groovy6693Bug extends StaticTypeCheckingTestCase implements StaticCompilationTestSupport {
    void testGenericsInference() {
        assertScript '''
            interface Converter<F, T> {
               T convertC(F from)
            }
              
            class Holder<T> {
               T thing
              
               Holder(T thing) {
                this.thing = thing
               }
              
               def <R> Holder<R> convertH(Converter<? super T, ? extends R> func1) {
                  new Holder(func1.convertC(thing))
               }
            }

            class IntToFloatConverter implements Converter<Integer,Float> {
               public Float convertC(Integer from) { from.floatValue() } 
            }
             
            @groovy.transform.TypeChecked
            void foo() {
            @ASTTest(phase=INSTRUCTION_SELECTION,value={
                def holderType = node.getNodeMetaData(INFERRED_TYPE)
                assert holderType.genericsTypes[0].type == Float_TYPE
            })
            def h1 = new Holder<Integer>(2).convertH(new IntToFloatConverter())
            }
            foo()
'''
    }
}
