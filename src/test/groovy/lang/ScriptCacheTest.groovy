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
package groovy.lang

import groovy.test.GroovyTestCase

class ScriptCacheTest extends GroovyTestCase {

    def packageName = "scriptcachetest"
    def className
    def cl = new GroovyClassLoader(this.class.classLoader);
    def file
    def packageDir

    public void setUp() {
        packageDir = new File(new File("target"), packageName)
        packageDir.mkdir()
        packageDir.deleteOnExit()
        file = File.createTempFile("TestScriptCache", ".groovy", packageDir)
        file.deleteOnExit()
        className = "${packageName}.${file.name - '.groovy'}"

        def currentDir = packageDir.parentFile.absolutePath
        cl.addClasspath(currentDir)
        file.write """
            package ${packageName}

            def greeting = "hello"
        """
    }

    public void tearDown() {
        file.delete()
        packageDir.delete()
    }

    public void testScriptCaching() {
        def groovyClass1 = cl.loadClass(className, true, false)
        def groovyClass2 = cl.loadClass(className, true, false)
        assert groovyClass1 == groovyClass2
    }

    void testScriptNaming() {
        def groovyClass1 = cl.loadClass(className, true, false)
        assert groovyClass1.getName() == className
    }
}
