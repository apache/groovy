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
 * GROOVY-7174: a user class named {@code HashMap} plus a constructor
 * that takes that type used to work in the enclosing script but fail
 * inside {@code GroovyShell#evaluate} with
 * {@code failed to invoke constructor: public HashMapConsumer(HashMap)
 * with arguments: [] ... wrong number of arguments}.
 */
final class Groovy7174 {

    @Test
    void testShellEvaluateWithUserHashMapAnonymousSubclass() {
        assertScript '''
            public class HashMap<K, V> extends java.util.HashMap<K, V> {
            }

            public class HashMapConsumer {
                HashMap<String, String> map
                public HashMapConsumer(HashMap<String, String> map) {
                    this.map = map
                }
            }

            new HashMapConsumer(new HashMap<String, String>() {{
                put("key", "value")
            }})

            def shell = new GroovyShell(this.getClass().getClassLoader())
            shell.evaluate(\'''
                new HashMapConsumer(new HashMap<String, String>() {{
                    put("key", "value")
                }})
            \''')
        '''
    }
}
