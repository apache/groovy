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

import groovy.test.GroovyTestCase
import groovy.text.GStringTemplateEngine
import groovy.text.Template

class SimpleGStringTemplateEngineTest extends GroovyTestCase {
    void testRegressionCommentBug() {
        final Template template = new GStringTemplateEngine().createTemplate(
                "<% // This is a comment that will be filtered from output %>\n" +
                        "Hello World!"
        )

        final StringWriter sw = new StringWriter()
        template.make().writeTo(sw)
        assertEquals("\nHello World!", sw.toString())
    }

    void testShouldNotShareBinding() {
        String text = "<% println delegate.books; books = books.split(\",\"); out << books %>"

        StringWriter sw = new StringWriter()
        GStringTemplateEngine engine = new GStringTemplateEngine()

        Template template = engine.createTemplate(text)

        Map data = [books: 'a,b,c,d']

        // round one success
        template.make(data).writeTo(sw)
        assert sw.toString() == '[a, b, c, d]'

        sw = new StringWriter()
        // round two fails
        data = [books: 'e,f,g,h']
        template.make(data).writeTo(sw)
        assert sw.toString() == '[e, f, g, h]'
    }
}
