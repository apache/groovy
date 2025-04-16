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
package org.codehaus.groovy.transform

import gls.CompilableTestSupport

class SourceURITransformTest extends CompilableTestSupport {
    void testWorksInClass() {
        def groovy = new GroovyClassLoader()
        def compiledClass = groovy.parseClass '''class CompanionThingy {
   @groovy.transform.SourceURI static def myURI
   @groovy.transform.SourceURI static java.net.URI alsoMyURI
   @groovy.transform.SourceURI def myURIMember
   @groovy.transform.SourceURI java.net.URI alsoMyURIMember

   String toString() {
      assert myURI == alsoMyURI
      assert myURI == myURIMember
      assert myURI == alsoMyURIMember
      "I'm from " + myURI
   }
}'''

        assert compiledClass.newInstance().toString().matches(".*data:.*,class%20CompanionThingy%20%7B.*")
    }

    void testWorksInScript() {
        assertScript '''// This is script for testWorksInScript
   @groovy.transform.SourceURI def myURI
   @groovy.transform.SourceURI java.net.URI alsoMyURI

   assert myURI == alsoMyURI
   assert myURI.toString().matches(".*data:.*,//%20This%20is%20script%20for%20testWorksInScript%0A.*")
'''
    }
}
