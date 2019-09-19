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

import groovy.test.GroovyTestCase

class Groovy7753Bug extends GroovyTestCase {

    void testGroovy7753() {
        assertScript '''
        @groovy.transform.Field
        String x = "X"
        
        @groovy.transform.CompileStatic
        public List<String> getStrings() {
            x ? [x] : Collections.emptyList()
        }
        
        getStrings()
        '''
    }

    void testGroovy7753WithReturn() {
        assertScript '''
        @groovy.transform.Field
        String x = "X"
        
        @groovy.transform.CompileStatic
        public List<String> getStrings() {
            return x ? [x] : Collections.emptyList()
        }
        
        getStrings()
        '''
    }

    void testGroovy7753WithReturn2() {
        assertScript '''
        @groovy.transform.Field
        String x = "X"
        
        @groovy.transform.CompileStatic
        public List<String> getStrings() {
            return x ? Collections.emptyList() : [x]
        }
        
        getStrings()
        '''
    }

    void test2() {
        assertScript '''
        @groovy.transform.Field
        String x = "X"
        
        @groovy.transform.CompileStatic
        public List<String> getStrings() {
            Collections.emptyList()
        }
        
        getStrings()
        '''
    }

    void test3() {
        assertScript '''
        @groovy.transform.Field
        String x = "X"
        
        @groovy.transform.CompileStatic
        public List<String> getStrings() {
            [x]
        }
        
        getStrings()
        '''
    }
}
