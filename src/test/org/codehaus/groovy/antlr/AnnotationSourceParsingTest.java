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
package org.codehaus.groovy.antlr;

import groovy.lang.GroovyShell;
import groovy.test.GroovyTestCase;
import groovy.test.NotYetImplemented;

import java.io.StringReader;

/**
 * Parsing annotations
 */
public class AnnotationSourceParsingTest extends GroovyTestCase {
    private void parse(String methodName, StringReader stringReader) {
        new GroovyShell().parse(stringReader, methodName);
    }

    // GROOVY-9511
    public void testMultiLineAttributes() {
        if (notYetImplemented()) return;
        StringReader reader = new StringReader(
                "class OtherSection\n"
                        + "{\n"
                        + "    @CollectionOfElements\n"
                        + "    @JoinTable\n"
                        + "    (\n"
                        + "        table=@Table(name=\"gaga\"),\n"
                        + "        joinColumns = @JoinColumn(name=\"BoyId\")\n"
                        + "    )\n"
                        + "    @Column(name=\"favoritepoupon\", \n"
                        + "nullable=false)\n"
                        + "    Set<String> questions = new HashSet<String> ()\n\n"
                        + "}");
        parse("testMultiLineAttributes", reader);
    }
}
