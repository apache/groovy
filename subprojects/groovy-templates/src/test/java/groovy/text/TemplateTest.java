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
import org.codehaus.groovy.control.CompilationFailedException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TemplateTest extends TestCase {

    public void testMixedTemplateText() throws CompilationFailedException, ClassNotFoundException, IOException {
        Template template1 = new SimpleTemplateEngine().createTemplate("<%= \"test\" %> of expr and <% test = 1 %>${test} script.");
        assertEquals("test of expr and 1 script.", template1.make().toString());

        Template template2 = new GStringTemplateEngine().createTemplate("<%= \"test\" %> of expr and <% test = 1 %>${test} script.");
        assertEquals("test of expr and 1 script.", template2.make().toString());

    }

    public void testBinding() throws CompilationFailedException, ClassNotFoundException, IOException {
        Map binding = new HashMap();
        binding.put("sam", "pullara");

        Template template1 = new SimpleTemplateEngine().createTemplate("<%= sam %><% print sam %>");
        assertEquals("pullarapullara", template1.make(binding).toString());

        Template template2 = new GStringTemplateEngine().createTemplate("<%= sam %><% out << sam %>");
        assertEquals("pullarapullara", template2.make(binding).toString());

        Template template3 = new GStringTemplateEngine().createTemplate("<%= sam + \" \" + sam %><% out << sam %>");
        assertEquals("pullara pullarapullara", template3.make(binding).toString());
    }

}
