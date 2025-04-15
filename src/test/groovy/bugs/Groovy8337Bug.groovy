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

import gls.CompilableTestSupport
import groovy.test.NotYetImplemented

class Groovy8337Bug extends CompilableTestSupport {
    void testGroovy8337() {
        assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class Static {
              private Number n
              BigDecimal meth() {
                return n == null || n instanceof BigDecimal ? n : new BigDecimal(n.toString())
              }
            }
            assert null == new Static().meth()
        '''
    }

    void testGroovy8337WithFieldValue() {
        assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class Static {
              private Number n = new BigDecimal('1.23')
              BigDecimal meth() {
                return n == null || n instanceof BigDecimal ? n : new BigDecimal(n.toString())
              }
            }
            assert new BigDecimal('1.23') == new Static().meth()
        '''
    }
}
