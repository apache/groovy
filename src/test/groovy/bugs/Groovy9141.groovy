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

import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.codehaus.groovy.control.ParserPluginFactory.antlr2

@CompileStatic
final class Groovy9141 {

    @Test
    void testAbstractMethodWithBodyInClass() {
        def err = shouldFail CompilationFailedException, '''
            abstract class Main {
                abstract void meth() {}
            }
        '''
        assert err =~ / You defined an abstract method\[meth\] with a body. Try removing the method body @ line /
    }

    @Test // not a language requirement but script-level check takes precedence in current implementation
    void testAbstractMethodWithBodyInScript() {
        def err = shouldFail CompilationFailedException, '''
            abstract void meth() {}
        '''
        assert err =~ / You cannot define an abstract method\[meth\] in the script. Try removing the 'abstract' /
    }

    @Test
    void testAbstractMethodWithBodyInScript_oldParser() {
        def shell = new GroovyShell(new CompilerConfiguration(pluginFactory: antlr2()))

        def err = shouldFail CompilationFailedException, {
            shell.evaluate '''
                abstract void meth() {}
            '''
        }
        assert err =~ / Abstract methods do not define a body. /
    }
}
