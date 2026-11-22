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

import static groovy.test.GroovyAssert.assertScript

/**
 * GROOVY-11818: a for-loop variable captured by an anonymous inner class
 * should retain the value from the iteration that created it, matching
 * Java's effectively-final loop variable semantics.
 */
final class Groovy11818 {

    @Test
    void testForLoopVariableCapturedByAnonymousClass() {
        assertScript '''
            class MyCommand {
                String filename
            }
            interface MyFile {
                String getFilename()
            }
            List<MyCommand> commands = [new MyCommand(filename: 'testing1'), new MyCommand(filename: 'testing2')]
            List<MyFile> files = []
            for (MyCommand cmd in commands) {
                MyFile file = new MyFile() {
                    String getFilename() {
                        cmd.filename
                    }
                }
                files.add(file)
            }
            assert files.collect { it.filename } == ['testing1', 'testing2']
        '''
    }
}
