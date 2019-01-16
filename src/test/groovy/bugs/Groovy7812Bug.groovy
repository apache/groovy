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

import org.codehaus.groovy.tools.GroovyStarter

class Groovy7812Bug extends GroovyTestCase {
    void testResolvingOuterNestedClass() {
        def mainScriptPath = new File(this.getClass().getResource('/groovy/bugs/groovy7812/Main.groovy').toURI()).absolutePath
        runScript(mainScriptPath)
    }

//   Even if try to catch `Throwable`, the expected error is thrown all the same..., as a result, the test fails due to the weired problem...
//
//    org.codehaus.groovy.control.MultipleCompilationErrorsException: startup failed:
//D:\_APPS\git_apps\groovy\out\test\resources\groovy\bugs\groovy7812\MainWithErrors.groovy: 22: unable to resolve class Outer.Inner123
// @ line 22, column 8.
//   assert new Outer.Inner123()
//          ^
//
//1 error
//
//    void testUnexistingInnerClass() {
//        try {
//            def mainScriptPath = new File(this.getClass().getResource('/groovy/bugs/groovy7812/MainWithErrors.groovy').toURI()).absolutePath
//            runScript(mainScriptPath)
//        } catch (Throwable t) {
//            assert t.getMessage().contains('unable to resolve class Outer.Inner123')
//        }
//    }


    static void runScript(String path) {
        GroovyStarter.main(["--main", "groovy.ui.GroovyMain", path] as String[])
    }
}
