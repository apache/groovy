/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package groovy.bugs.groovy6742

import groovy.transform.CompileStatic
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

@CompileStatic
final class Groovy6742 {

    @Test
    void test1() {
        assertScript '''
            package groovy.bugs.groovy6742

            @groovy.transform.TypeChecked
            class Issue1 {
                public void issue(){
                    Function<String,String> function = new Function<String,String>() {
                        @Override
                        String apply(String input) {
                            return "ok"
                        }
                    }
                }
            }

            assert true
        '''
    }

    @Test
    void test2() {
        assertScript '''
            package groovy.bugs.groovy6742

            @groovy.transform.TypeChecked
            class Issue2 {
                public void issue() {
                    transform(new Function<String, String>() {
                        @Override
                        String apply(String input) {
                            return "ok"
                        }
                    })
                }

                public <I, O> void transform(Function<? super I, ? extends O> function) {
                }
            }

            assert true
        '''
    }

    @Test
    void test3() {
        assertScript '''
            package groovy.bugs.groovy6742

            @groovy.transform.TypeChecked
            class Issue3 {
                public static <F, T> FutureCallback<F> deferredCallback(DeferredResult<T> deferredResult, final Function<F, T> function) {
                    return new FutureCallback<F>() {
                        private F f = null
                        F f2 = null

                        @Override
                        void onSuccess(F result) {
                            deferredResult.setResult(function.apply(result))
                        }
                    }
                }
            }

            assert true
        '''
    }
}
