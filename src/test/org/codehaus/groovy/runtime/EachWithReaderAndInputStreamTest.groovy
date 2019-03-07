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
package org.codehaus.groovy.runtime

/**
 * Test .each with Reader and InputStream
 */
class EachWithReaderAndInputStreamTest extends GroovyTestCase {
    /**
     * The following instances are used in testing the file operations
     */
    String multiLineVal = """
This text
as can be seen
has multiple lines
and not one punctuation mark
"""

    // Our file instance
    def File file

    void setUpFile() {
        // Setup guarantees us that we use a non-existent file
        file = File.createTempFile("unitTest", ".txt")
        assert file.exists() == true
        //println file.canonicalPath
        assert file.length() == 0L
        file << multiLineVal
    }

    void tearDownFile() {
        // we remove our temporary file
        def deleted = false
        while (deleted == false)
            deleted = file.delete()
        assert file.exists() == false
    }

    void testEachForStringBufferInputStream() {
        def ist = new StringBufferInputStream(multiLineVal)
        def readVal = ""
        ist.each {
            readVal += (char) it
        }
        assert readVal == multiLineVal
    }

    void testEachForStringReader() {
        def ir = new StringReader(multiLineVal)
        def readVal = ""
        ir.each { readVal += it + "\n" }
        assert readVal == multiLineVal
    }

    void testEachForFileWithInputStream() {
        setUpFile()
        def readVal = ""
        file.withInputStream { is ->
            is.each { readVal += (char) it }
        }
        tearDownFile()
        assert readVal == multiLineVal
    }

    void testEachForFileWithReader() {
        setUpFile()
        def readVal = ""
        file.withReader { reader ->
            reader.each { readVal += it + "\n" }
        }
        tearDownFile()
        assert readVal == multiLineVal
    }
}
