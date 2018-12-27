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
package groovy.bugs

import groovy.xml.MarkupBuilder

/**
 * Tests that special XML chars are made into entities by MarkupBuilder.
 */
class Groovy593_Bug extends GroovyTestCase {

    StringWriter writer = new StringWriter()
    MarkupBuilder chars = new MarkupBuilder(writer)
    XmlParser parser = new XmlParser()
    String expectedXML =
    """\
<chars>
  <ampersand a='&amp;'>&amp;</ampersand>
  <quote attr='"'>"</quote>
  <apostrophe attr='&apos;'>'</apostrophe>
  <lessthan attr='value'>chars: &amp; &lt; &gt; '</lessthan>
  <element attr='value 1 &amp; 2'>chars: &amp; &lt; &gt; " in middle</element>
  <greaterthan>&gt;</greaterthan>
</chars>"""

    void testBug() {
        // XML characters to test with
        chars.chars {
            ampersand(a: "&", "&")
            quote(attr: "\"", "\"")
            apostrophe(attr: "'", "'")
            lessthan(attr: "value", "chars: & < > '")
            element(attr: "value 1 & 2", "chars: & < > \" in middle")
            greaterthan(">")
        }

        // Test MarkupBuilder state with expectedXML
        // Handling the cr lf characters, depending on operating system.
        def outputValue = writer.toString()
        if (expectedXML.indexOf("\r\n") >= 0) expectedXML = expectedXML.replaceAll("\r\n", "\n");
        if (outputValue.indexOf("\r\n") >= 0) outputValue = outputValue.replaceAll("\r\n", "\n");
        assertEquals(expectedXML.replaceAll("\r\n", "\n"), outputValue)

        // parser will throw a SAXParseException if XML is not valid
        parser.parseText(writer.toString())
    }

}

