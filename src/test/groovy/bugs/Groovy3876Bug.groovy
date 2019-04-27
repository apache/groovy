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

import org.codehaus.groovy.runtime.typehandling.GroovyCastException

class Groovy3876Bug extends GroovyTestCase {
    void testGStringToNumberConversion() {
        def a

        assert "-1" as Integer == -1
        a = '-1'
        assert "$a" as Integer == -1

        try {
            ((Integer) "$a")
            fail('The cast should have failed with GroovyCastException')
        }catch(GroovyCastException ex) {
            // fine
        }

        assert "-1000" as Integer == -1000
        a = "-1000"
        assert "$a" as Integer == -1000
    }
}
