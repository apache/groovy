/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package groovy.bugs

import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilationFailedException
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail

@CompileStatic
final class Groovy9176 {

    @Test
    void testGroovyPropertyCovariantMethodCheck() {
        def err = shouldFail CompilationFailedException, '''
            class Pojo {
                private String title;
                public String getTitle() { return this.title; }
                public void setTitle(String title) { this.title = title; }
            }
            class Test extends Pojo {
                // property "title" with type that is incompatible with "String"
                java.util.regex.Pattern title
            }
        '''

        assert err =~ / The return type of java.util.regex.Pattern getTitle\(\) in Test is incompatible with java.lang.String in Pojo\n\. At \[9:17\] /
    }
}
