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
package groovy.xml

/**
 * Tests for StreamingMarkupBuilder. The tests directly in this file
 * are specific to StreamingMarkupBuilder. Functionality in common with
 * MarkupBuilder is tested in the BuilderTestSupport parent class.
 */
class StreamingMarkupBuilderTest extends BuilderTestSupport {

    // GROOVY-5867
    void testAttributesAndNamespaces() {
        def xml = '''<?xml version="1.0" encoding="UTF-8"?>
        <grammar xml:lang="en-us" xmlns="http://www.w3.org/2001/06/grammar"/>'''
        def grammar = new XmlSlurper().parseText(xml)
        def smb = new StreamingMarkupBuilder()
        assert smb.bindNode(grammar).toString().contains("xml:lang='en-us'")
    }

    // GROOVY-5879
    void testXmlNamespaceWithDefaultNamespace() {
        def xml = '''<?xml version='1.0' encoding='UTF-8'?>
        <root>
            <one xml:lang="en">First</one>
            <one xml:lang="de">Second</one>
        </root>'''
        def root = new XmlSlurper().parseText(xml)
        def smb = new StreamingMarkupBuilder()
        def result = smb.bindNode(root).toString()
        assert result.contains("<root>")
        assert result.contains("<one xml:lang='en'>First</one>")
    }

    // GROOVY-8850
    void testStreamingMarkupBuilderAddingNamespaceCorrectly() {

        def topics = ['<TOPIC>topic1</TOPIC>', '<TOPIC>topic2</TOPIC>', '<TOPIC>topic3</TOPIC>']

        def addTrnUID = { b -> b.TRNUID('01234') }

        def xml = new StreamingMarkupBuilder().bind { builder ->
            mkp.xmlDeclaration()
            mkp.declareNamespace('': 'http://www.dnb.com/GSRL/Vers7/Rls24',
                    'xsi': 'http://www.w3.org/2001/XMLSchema-instance')
            GSRL {
                GSRLMSGSRQV1 {
                    SUBJUPDTRNRQ {
                        addTrnUID(builder)
                        SUBJUPDRQ {
                            topics.each{ topic -> mkp.yieldUnescaped topic }
                        }
                    }
                }
            }
        }

        assert XmlUtil.serialize(xml.toString()).normalize() == '''\
<?xml version="1.0" encoding="UTF-8"?><GSRL xmlns="http://www.dnb.com/GSRL/Vers7/Rls24" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <GSRLMSGSRQV1>
    <SUBJUPDTRNRQ>
      <TRNUID>01234</TRNUID>
      <SUBJUPDRQ>
        <TOPIC>topic1</TOPIC>
        <TOPIC>topic2</TOPIC>
        <TOPIC>topic3</TOPIC>
      </SUBJUPDRQ>
    </SUBJUPDTRNRQ>
  </GSRLMSGSRQV1>
</GSRL>
'''
    }

    protected assertExpectedXml(Closure markup, String expectedXml) {
        assertExpectedXml markup, null, expectedXml
    }

    protected assertExpectedXml(Closure markup, Closure configureBuilder, String expectedXml) {
        def builder = new StreamingMarkupBuilder()
        if (configureBuilder) configureBuilder(builder)
        def writer = new StringWriter()
        writer << builder.bind(markup)
        checkXml(expectedXml, writer)
    }

}