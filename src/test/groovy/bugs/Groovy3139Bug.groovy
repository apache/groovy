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

import groovy.mock.interceptor.StubFor

class Groovy3139Bug extends GroovyTestCase {

    void testStubbingIssueDueToCachingWhenUsing2Stubs() {
        def urlStub1 = new StubFor(URL)
        urlStub1.demand.openConnection {""}
        urlStub1.use {
           def get = new Get2(url: "http://localhost")
           def result = get.text            
        }

        def urlStub2 = new StubFor(URL)
        // the following stubbed call is on urlStub2 and its demand cound should be separate.
        // Currently due to caching of MockProxyMetaClass, it gets counted towards urlStub1 demands 
        // and throws "End of demands" exception
        urlStub2.demand.openConnection {""}
        urlStub2.use {
           def get = new Get2(url: "http://localhost")
           def result = get.text
        }
    }
}

class Get2{
    String url
    
    String getText() {
            def aUrl = new URL(toString())
            def conn = aUrl.openConnection()
            return "DUMMY"
    }
        
    String toString(){
        return url
    }
}