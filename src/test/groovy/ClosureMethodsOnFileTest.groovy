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
package groovy

/**
 * Tests various Closure methods in Groovy on file
 */
class ClosureMethodsOnFileTest extends GroovyTestCase {
    private File file = new File("src/test/groovy/Bar.groovy")
    private File dir = new File("src/test/groovy")

    protected void setUp() {
        if (!file.exists()) {
            file = new File("Bar.groovy")
        }
        if (!dir.exists()) {
            dir = new File(".")
        }
    }

    void testEachLine() {
        file.eachLine { line -> assert line != null }
    }

    void testEachLineWithCount() {
        int i = 0
        file.eachLine { line, count ->
            assert count == ++i && line != null
        }
    }

    void testReadLines() {
        def lines = file.readLines()
        assert lines != null
        assert lines.size() > 0, "File has: ${lines.size()} line(s)"
    }

    void testEachFile() {
        dir.eachFile { f -> assert f.getName() }
    }
}
