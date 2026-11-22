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
package bugs

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.customizers.SecureASTCustomizer
import org.junit.jupiter.api.Test

/**
 * GROOVY-8413: with {@code indirectImportCheckEnabled} and a star-import
 * whitelist, an instance method call such as {@code test.toString()} on an
 * already-imported class used to be rejected as a disallowed static import.
 * A static call on that imported class must also be accepted.
 */
final class Groovy8413 {

    private static GroovyClassLoader loaderWithStarWhitelist() {
        def secure = new SecureASTCustomizer()
        secure.indirectImportCheckEnabled = true
        secure.allowedStarImports = ['bugs.*']
        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(secure)
        new GroovyClassLoader(Groovy8413.classLoader, config)
    }

    @Test
    void testInstanceMethodCallIsNotRejectedAsStaticImport() {
        def loader = loaderWithStarWhitelist()
        try {
            loader.parseClass('''
                import bugs.Groovy8413Helper
                Groovy8413Helper test = new Groovy8413Helper()
                test.toString()
            ''')
        } finally {
            loader.close()
        }
    }

    @Test
    void testStaticMethodCallOnImportedWhitelistedClass() {
        def loader = loaderWithStarWhitelist()
        try {
            loader.parseClass('''
                import bugs.Groovy8413Helper
                Groovy8413Helper.someStaticMethod()
            ''')
        } finally {
            loader.close()
        }
    }

    @Test
    void testIndirectCheckStillRejectsNonWhitelistedType() {
        def loader = loaderWithStarWhitelist()
        try {
            def err = null
            try {
                loader.parseClass('''
                    import bugs.Groovy8413Helper
                    new java.io.File('x')
                ''')
            } catch (MultipleCompilationErrorsException e) {
                err = e
            }
            assert err != null
            assert err.message.contains('java.io.File')
        } finally {
            loader.close()
        }
    }
}

class Groovy8413Helper {
    static someStaticMethod() { 1 }
    String toString() { 'ok' }
}
