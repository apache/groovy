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
package groovy.jmx

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

final class ImportModuleJmxTest {

    @Test
    void testImportModuleAutomatic() {
        // groovy-jmx is an automatic module (Automatic-Module-Name in MANIFEST.MF);
        // JAR path is passed as a system property from build.gradle
        def jmxJar = System.getProperty('groovy.jmx.jar')
        assert jmxJar, 'groovy.jmx.jar system property not set'
        def config = new org.codehaus.groovy.control.CompilerConfiguration()
        config.classpathList = [jmxJar]
        def loader = new GroovyClassLoader(getClass().classLoader, config)
        def shell = new GroovyShell(loader)
        shell.evaluate '''
            import module org.apache.groovy.jmx

            assert GroovyMBean.name == 'groovy.jmx.GroovyMBean'
        '''
    }
}
