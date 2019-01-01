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
 * check that text def is available on...
 *
 * myFile.text,  myFile.text(charset),  
 * myURL.text,  myURL.text(charset),
 * myInputStream.text,  myInputStream.text(charset),
 * myReader.text,
 * myBufferedReader.text,
 * myProcess.text
 */
class TextPropertyTest extends GroovyTestCase {
    def myReader
    def myInputStream
    def myBigEndianEncodedInputStream

    void setUp() {
        myReader = new StringReader("digestive")
        myInputStream = new ByteArrayInputStream("chocolate chip".bytes)
        myBigEndianEncodedInputStream = new ByteArrayInputStream("shortbread".getBytes("UTF-16BE"))
    }

    void testBigEndianEncodedInputStreamText() {
        assert "shortbread" == myBigEndianEncodedInputStream.getText("UTF-16BE")
    }

    void testInputStreamText() {
        assert "chocolate chip" == myInputStream.text
    }

    void testReaderText() {
        assert "digestive" == myReader.text
    }

    void tearDown() {
        myInputStream = null
        myReader = null
    }
}
