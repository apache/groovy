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
package groovy.text;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

public class XmlTemplateEngineTest extends TestCase {

    public void testBinding() throws Exception {
        Map binding = new HashMap();
        binding.put("Christian", "Stein");

        XmlTemplateEngine xmlTemplateEngine = new XmlTemplateEngine();
        String xmlScript = "<!-- Just a comment. -->\n"
                + "<xml xmlns:gsp=\"http://groovy.codehaus.org/2005/gsp\">"
                + "  ${Christian}"
                + "  <gsp:expression>Christian</gsp:expression>"
                + "  <gsp:scriptlet>println Christian</gsp:scriptlet>"
                + "</xml>";
        String xmlResult = "<xml>\n"
                + "  Stein\n"
                + xmlTemplateEngine.getIndentation() + "Stein\n"
                + "Stein" + System.lineSeparator()
                + "</xml>\n";
        Template template = xmlTemplateEngine.createTemplate(xmlScript);
        assertEquals(xmlResult, template.make(binding).toString());
    }

    public void testQuotes() throws Exception {
        Map binding = new HashMap();
        binding.put("Christian", "Stein");

        XmlTemplateEngine xmlTemplateEngine = new XmlTemplateEngine();
        String xmlScript = "<!-- Just a comment. -->\n"
                + "<xml xmlns:mygsp=\"http://groovy.codehaus.org/2005/gsp\">"
                + "  ${Christian + \" \" + Christian}"
                + "  <mygsp:expression>Christian + \" \" + Christian</mygsp:expression>"
                + "  <mygsp:scriptlet>println Christian</mygsp:scriptlet>"
                + "</xml>";
        String xmlResult = "<xml>\n"
                + "  Stein Stein\n"
                + xmlTemplateEngine.getIndentation() + "Stein Stein\n"
                + "Stein" + System.lineSeparator()
                + "</xml>\n";
        Template template = xmlTemplateEngine.createTemplate(xmlScript);
        assertEquals(xmlResult, template.make(binding).toString());
    }

    public void testNamespaces() throws Exception {
        Map binding = new HashMap();
        binding.put("Christian", "Stein");

        XmlTemplateEngine xmlTemplateEngine = new XmlTemplateEngine();
        String xmlScript = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<foo:bar xmlns:foo='urn:baz' xmlns:mygsp=\"http://groovy.codehaus.org/2005/gsp\">"
                + "${Christian + \" \" + Christian}"
                + "<mygsp:expression>Christian + \" \" + Christian</mygsp:expression>"
                + "<nonamespace><mygsp:scriptlet>println Christian</mygsp:scriptlet></nonamespace>"
                + "</foo:bar>";
        String xmlResult = "<foo:bar xmlns:foo='urn:baz'>\n"
                + "  Stein Stein\n"
                + xmlTemplateEngine.getIndentation() + "Stein Stein\n"
                + xmlTemplateEngine.getIndentation() + "<nonamespace>\n"
                + "Stein" + System.lineSeparator()
                + xmlTemplateEngine.getIndentation() + "</nonamespace>\n"
                + "</foo:bar>\n";
        Template template = xmlTemplateEngine.createTemplate(xmlScript);
        assertEquals(xmlResult, template.make(binding).toString());
    }

    public void testDoubleQuotesInAttributeValues() throws Exception{
        XmlTemplateEngine xmlTemplateEngine = new XmlTemplateEngine();
        String xmlScript = "<document a='quoted \"string\"'/>";
        String xmlResult = "<document a='quoted \"string\"'/>\n";
        Template template = xmlTemplateEngine.createTemplate(xmlScript);
        assertEquals(xmlResult, template.make().toString());
    }
}
