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

import gls.CompilableTestSupport
import groovy.test.NotYetImplemented

class Groovy8409Bug extends CompilableTestSupport {
    @NotYetImplemented
    void test() {
        assertScript '''
        import groovy.transform.CompileStatic
        import java.util.function.BiFunction

        @CompileStatic
        class Groovy8409Bug {
        
            static <T> T actionWrapperT(BiFunction<Date, URL, T> action) {
                T result = action.apply(new Date(), new URL('http://www.example.com'))
                // do something else here
                return result
            }
        
            static void main(String[] args) {
                Groovy8409Bug t = actionWrapperT { Date date, URL url -> new Groovy8409Bug() }
            }
        
        }
        '''
    }

    void testWorkaround() {
        assertScript '''
        import groovy.transform.CompileStatic
        import java.util.function.BiFunction

        @CompileStatic
        class Groovy8409Bug {
        
            static <X> X actionWrapperT(BiFunction<Date, URL, X> action) {
                X result = action.apply(new Date(), new URL('http://www.example.com'))
                // do something else here
                return result
            }
        
            static void main(String[] args) {
                Groovy8409Bug t = actionWrapperT { Date date, URL url -> new Groovy8409Bug() }
            }
        
        }
        '''
    }
}
