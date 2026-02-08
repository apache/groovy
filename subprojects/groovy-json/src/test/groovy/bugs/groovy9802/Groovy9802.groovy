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
package bugs.groovy9802


import groovy.transform.CompileStatic
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.isAtLeastJdk
import static org.junit.Assume.assumeTrue

@CompileStatic
final class Groovy9802 {
    @Test
    void test() {
        assumeTrue(isAtLeastJdk('11.0'))
        assertScript '''
            import groovy.json.*
            import java.net.http.*
            import java.nio.file.Paths
            import static java.net.http.HttpResponse.*
            
            def req = HttpRequest.newBuilder()
                    .uri(URI.create("https://raw.githubusercontent.com/apache/groovy/master/subprojects/groovy-json/src/test/resources/groovy9802.json"))
                    .build()
    
            def parser = new JsonSlurper()
            parser.parseText('{}')
    
            def res = HttpClient.newHttpClient().sendAsync(req, BodyHandlers.ofString())
                    .thenApply(r -> r.body())
                    .thenApply(parser::parseText)
                    .join()
    
            assert 'response: [userId:1, id:1, title:delectus aut autem, completed:false]' == "response: $res"
        '''
    }
}
