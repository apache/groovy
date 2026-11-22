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

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import groovy.ant.AntBuilder

/**
 * GROOVY-8792: {@code JsonSlurper} / {@code JsonBuilder} must work from the
 * Ant {@code groovy} task without requiring {@code contextClassLoader="true"}
 * or failing with {@code Unable to load FastStringService}.
 */
final class Groovy8792 {

    @TempDir
    File tempDir

    @Test
    void testJsonSlurperFromAntGroovyTask() {
        def script = new File(tempDir, 'json.groovy')
        def out = new File(tempDir, 'out.txt')
        script.text = """
            import groovy.json.*
            def json = new JsonSlurper().parseText('{"person":{"name":"Guillaume","age":33,"pets":["dog","cat"]}}')
            new File(${out.absolutePath.inspect()}).text = new JsonBuilder(json).toPrettyString()
        """
        def ant = new AntBuilder()
        ant.project.baseDir = tempDir
        ant.taskdef(name: 'groovy', classname: 'org.codehaus.groovy.ant.Groovy')
        ant.groovy(src: 'json.groovy')
        assert out.file
        def text = out.text
        assert text.contains('"name": "Guillaume"')
        assert text.contains('"age": 33')
        assert text.contains('"dog"')
    }
}
