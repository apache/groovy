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

import groovy.transform.CompileStatic
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

@CompileStatic
final class Groovy9790 {
    @Test
    void "test GROOVY-9790 - 1"() {
        assertScript '''
            import java.util.stream.IntStream
            
            @groovy.transform.CompileStatic
            def x() {
                IntStream.range(0, 2).forEach((Integer i) -> { assert 0 <= i && i < 2})
            }
            
            x()
        '''
    }

    @Test
    void "test GROOVY-9790 - 2"() {
        assertScript '''
            import java.util.stream.IntStream
            
            @groovy.transform.CompileStatic
            def x() {
                IntStream.range(0, 2).forEach((int i) -> { assert 0 <= i && i < 2})
            }
            
            x()
        '''
    }

    @Test
    void "test GROOVY-9790 - 3"() {
        def err = shouldFail '''
            import java.util.stream.IntStream
            
            @groovy.transform.CompileStatic
            def x() {
                IntStream.range(0, 2).forEach((String i) -> { return i })
            }
            
            x()
        '''

        assert err.toString().contains('The inferred type[int] is not compatible with the parameter type[java.lang.String]\n. At [6:48]')
    }
}
