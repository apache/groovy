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

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * check that the new filterLine() method on InputStream is ok
 * (and indirectly test newReader() method on InputStream)
 * as specified in GROOVY-624 and GROOVY-625
 */

class FilterLineTest {
    def myFile
    def myInput
    def myOutput

    @BeforeEach
    void setUp() {
        myFile = new File("src/test/groovy/groovy/FilterLineTest.groovy")
        myInput = new FileInputStream(myFile)
        myOutput = new CharArrayWriter()
    }

    @Test
    void testFilterLineOnFileReturningAWritable() {
        def writable = myFile.filterLine() {it.contains("testFilterLineOnFileReturningAWritable")}
        writable.writeTo(myOutput)
        assert 3 == myOutput.toString().count("testFilterLineOnFileReturningAWritable")
    }

    @Test
    void testFilterLineOnFileUsingAnOutputStream() {
        myFile.filterLine(myOutput) {it.contains("testFilterLineOnFileUsingAnOutputStream")}
        assert 3 == myOutput.toString().count("testFilterLineOnFileUsingAnOutputStream")
    }

    @Test
    void testFilterLineOnInputStreamReturningAWritable() {
        def writable = myInput.filterLine() {it.contains("testFilterLineOnInputStreamReturningAWritable")}
        writable.writeTo(myOutput)
        assert 3 == myOutput.toString().count("testFilterLineOnInputStreamReturningAWritable")
    }

    @Test
    void testFilterLineOnInputStreamUsingAnOutputStream() {
        myInput.filterLine(myOutput) {it.contains("testFilterLineOnInputStreamUsingAnOutputStream")}
        assert 3 == myOutput.toString().count("testFilterLineOnInputStreamUsingAnOutputStream")
    }
}
