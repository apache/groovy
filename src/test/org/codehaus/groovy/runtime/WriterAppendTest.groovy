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

import groovy.test.GroovyTestCase

/**
 * Test Writer append and leftShift DGM methods
 */
class WriterAppendTest extends GroovyTestCase {
    /**
     * The following instances are used in testing the file writes
     */
    static text = """
            <groovy>
              <things>
                <thing>Jelly Beans</thing>
              </things>
              <music>
                <tune>The 59th Street Bridge Song</tune>
              </music>
              <characters>
                <character name="Austin Powers">
                   <enemy>Dr. Evil</enemy>
                   <enemy>Mini Me</enemy>
                </character>
              </characters>
            </groovy>
            """
    static gPathResult = new GString(text, new String[]{"Hello ", "!"})
    static gPathWriteTo
    static defaultEncoding
    static UTF8_ENCODING

    static {
        StringWriter sw = new StringWriter()
        gPathResult.writeTo(sw)
        gPathWriteTo = sw.toString()
        UTF8_ENCODING = "UTF-8"
        defaultEncoding = System.getProperty("file.encoding")
    }

    // Our file instance
    def File file;

    void setUp() {
        // Setup guarantees us that we use a non-existent file
        file = File.createTempFile("unitTest", ".txt")
        assert file.exists() == true
        //println file.canonicalPath
        assert file.length() == 0L
    }

    void tearDown() {
        // we remove our temporary file
        def deleted = false
        while (deleted == false)
            deleted = file.delete()
        assert file.exists() == false
    }

    void testAppendStringWithEncoding() {
        def expected
        // test new
        file.withWriterAppend(UTF8_ENCODING) {writer ->
            writer.write(text)
        }
        expected = text
        assert hasContents(file, expected, UTF8_ENCODING)

        // test existing
        file.withWriterAppend(UTF8_ENCODING) {writer ->
            writer.write(text)
        }
        expected += text
        assert hasContents(file, expected, UTF8_ENCODING)
    }

    void testAppendWritableWithEncoding() {
        def expected

        // test new
        file.withWriterAppend(UTF8_ENCODING) {writer ->
            writer.write(gPathResult)
        }
        expected = gPathWriteTo
        assert hasContents(file, expected, UTF8_ENCODING)

        // test existing
        file.withWriterAppend(UTF8_ENCODING) {writer ->
            writer.write(gPathResult)
        }
        expected += gPathWriteTo
        assert hasContents(file, expected, UTF8_ENCODING)
    }


    void testLeftShiftStringWithEncoding() {
        def expected

        // test new
        file.withWriterAppend(UTF8_ENCODING) {writer ->
            writer << text
        }
        expected = text
        assert hasContents(file, expected, UTF8_ENCODING)

        // test existing
        file.withWriterAppend(UTF8_ENCODING) {writer ->
            writer << text
        }
        expected += text
        assert hasContents(file, expected, UTF8_ENCODING)
    }

    void testLeftShiftWritableWithEncoding() {
        def expected

        // test new
        file.withWriterAppend(UTF8_ENCODING) {writer ->
            writer << gPathResult
        }
        expected = gPathWriteTo
        assert hasContents(file, expected, UTF8_ENCODING)

        // test existing
        file.withWriterAppend(UTF8_ENCODING) {writer ->
            writer << gPathResult
        }
        expected += gPathWriteTo
        assert hasContents(file, expected, UTF8_ENCODING)
    }

    void testFileSetText() {
        // test new
        file.text = 'foobar'
        assert hasContents(file, 'foobar', defaultEncoding)

        // test existing
        file.text = 'foobarbaz'
        assert hasContents(file, 'foobarbaz', defaultEncoding)
    }

    void testAppendStringDefaultEncoding() {
        def expected
        // test new
        file.withWriterAppend {writer ->
            writer.write(text)
        }
        expected = text
        assert hasContents(file, expected, defaultEncoding)

        // test existing
        file.withWriterAppend {writer ->
            writer.write(text)
        }
        expected += text
        assert hasContents(file, expected, defaultEncoding)
    }

    void testAppendWritableDefaultEncoding() {
        def expected

        // test new
        file.withWriterAppend {writer ->
            writer.write(gPathResult)
        }
        expected = gPathWriteTo
        assert hasContents(file, expected, defaultEncoding)

        // test existing
        file.withWriterAppend {writer ->
            writer.write(gPathResult)
        }
        expected += gPathWriteTo
        assert hasContents(file, expected, defaultEncoding)
    }


    void testLeftShiftStringDefaultEncoding() {
        def expected

        // test new
        file.withWriterAppend {writer ->
            writer << text
        }
        expected = text
        assert hasContents(file, expected, defaultEncoding)

        // test existing
        file.withWriterAppend {writer ->
            writer << text
        }
        expected += text
        assert hasContents(file, expected, defaultEncoding)
    }


    void testLeftShiftWritableDefaultEncoding() {
        def expected

        // test new
        file.withWriterAppend {writer ->
            writer << gPathResult
        }
        expected = gPathWriteTo
        assert hasContents(file, expected, defaultEncoding)

        // test existing
        file.withWriterAppend {writer ->
            writer << gPathResult
        }
        expected += gPathWriteTo
        assert hasContents(file, expected, defaultEncoding)
    }


    boolean hasContents(File f, String expected, String charSet) {
        // read contents the Java way
        byte[] buf = new byte[expected.length()];

        def fileIS = new FileInputStream(file)
        fileIS.read(buf)
        fileIS.close()
        if (expected != new String(buf, charSet))
            println "EX: " + expected + "------" + new String(buf, charSet) + "\n----"
        return expected == new String(buf, charSet)
    }
}
