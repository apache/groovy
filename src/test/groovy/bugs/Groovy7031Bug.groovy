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

import groovy.test.GroovyTestCase

class Groovy7031Bug extends GroovyTestCase {

    void test() {
        assertScript """
        @groovy.transform.CompileStatic
        class StaticClass {
            StringHolder holder = new StringHolder()
            String str

            StaticClass(String s) {
                str = holder.str = s
            }
        }

        class StringHolder {
            String str
        }

        def s = new StaticClass('test')
        assert s.holder.str == 'test'
        assert s.str == 'test'
        """
    }
}
