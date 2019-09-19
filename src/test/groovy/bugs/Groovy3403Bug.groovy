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
import groovy.test.GroovyTestCase

class Groovy3403Bug extends GroovyTestCase {

    void testStubIssueForStaticMethodsDueToCallSiteCachingWhenUsing2Stubs() {
        def stub1 = new StubFor(Main3403)
        stub1.demand.test() {
            return "stubbed call made - 1"
        }

        def ot = new Helper3403()

        stub1.use {
            assert ot.doTest() == "stubbed call made - 1"
        }

        def stub2 = new StubFor(Main3403)
        stub2.demand.test() {
            return "stubbed call made - 2"
        }

        // the following stubbed call is on stub2 and its demand count should be separate.
        // Currently due to caching of MockProxyMetaClass, it gets counted towards stub1 demands 
        // and throws "End of demands" exception
        stub2.use {
            assert ot.doTest() == "stubbed call made - 2"
        }
    }
}

class Main3403 {
   static test(){
   }
}

class Helper3403 {
    def doTest() {
        Main3403.test()
    }
}