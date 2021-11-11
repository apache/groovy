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
package groovy.bugs.groovy6742

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

final class Groovy6742 {

    @Test
    void testAssignAIC() {
        assertScript '''
            package groovy.bugs.groovy6742

            @groovy.transform.TypeChecked
            def test() {
                Function<String,String> function = new Function<String,String>() {
                    @Override
                    String apply(String input) {
                        return input + ' world'
                    }
                }
                function.apply('hello')
            }

            assert test() == 'hello world'
        '''
    }

    @Test
    void testReturnAIC() {
        assertScript '''
            package groovy.bugs.groovy6742

            @groovy.transform.TypeChecked
            static <R,T> FutureCallback<R> deferredCallback(DeferredResult<R> deferredResult, final Function<R,T> transformation) {
                new FutureCallback<R>() {
                    @Override
                    void onSuccess(R result) {
                        deferredResult.setResult(transformation.apply(result))
                    }
                }
            }

            assert true
        '''
    }
}
