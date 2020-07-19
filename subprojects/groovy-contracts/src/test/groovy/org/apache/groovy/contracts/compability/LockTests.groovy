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
package org.apache.groovy.contracts.compability

import groovy.test.GroovyShellTestCase

class LockTests extends GroovyShellTestCase {

    void test_withReadAndWriteLock() {

        def result = evaluate """
        import groovy.transform.*;
        import groovy.contracts.*

        public class ResourceProvider {

            private final Map<String, String> data = new HashMap<String, String>();

            @Requires({ key })
            @WithReadLock
            public String getResource(String key) throws Exception {
                return data.get(key)
            }

            @WithWriteLock
            public void refresh() throws Exception {
                data['test'] = 'test'
            }
        }

        def resourceProvider = new ResourceProvider()
        resourceProvider.refresh()

        resourceProvider.getResource('test')
        """

        assert result == 'test'
    }
}
