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

import java.lang.reflect.*
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.GroovyBugError

class Groovy3776Bug extends GroovyTestCase {
    void testInvalidListWithMapEntryExpressions() {
        GroovyClassLoader cl = new GroovyClassLoader();
        
        def scriptStr = """
            class InvalidListLiteral {
                def x = [
                    [foo: 1, bar: 2]
                    [foo: 1, bar: 2]
                ]
            }
        """
        try {
            cl.parseClass(scriptStr)
            fail('Compilation should have failed with MultipleCompilationErrorsException')
        } catch(MultipleCompilationErrorsException mcee) {
            // ok if failed with this error.
        } catch(GroovyBugError gbe) {
            fail('Compilation should have failed with MultipleCompilationErrorsException but failed with GroovyBugError')
        }
    }
}
