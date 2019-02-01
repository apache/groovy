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

import java.io.StringReader;


/**
 * Parser tests for Enum definitions.
 */
public class EnumSourceParsingTest extends SourceParserTest {
    public void testParseEnumConstants() {
        StringReader reader = new StringReader(
                "enum One {\n"
                        + "  ONE, TWO, THREE\n"
                        + "}");
        parse("testParseEnumConstants", reader);
    }

    public void testParseEnumMultiLine() {
      StringReader reader = new StringReader(
          "enum ParseCode\n" +
          "{\n" +
          "    COMPLETE,\n" +
          "    INCOMPLETE,\n" +
          "    ERROR\n" +
          "}");
      parse("testParseEnumMultiLine", reader);
    }
    
    public void testParseEnumImplementsMultiLine() {
        StringReader reader = new StringReader(
            "enum ParseCode implements I\n" +
            "{\n" +
            "    COMPLETE,\n" +
            "    INCOMPLETE,\n" +
            "    ERROR\n" +
            "}");
        parse("testParseEnumImplementsMultiLine", reader);
    }
    
    public void testParseEnumImplementsMultiLine2() {
        StringReader reader = new StringReader(
            "enum ParseCode\n" +
            "implements I\n" +
            "{\n" +
            "    COMPLETE,\n" +
            "    INCOMPLETE,\n" +
            "    ERROR\n" +
            "}");
        parse("testParseEnumImplementsMultiLine2", reader);
    }
    
    public void testParseEnumConstantsOneLiner() {
        StringReader reader = new StringReader(
                "enum One { ONE, TWO, THREE }");
        parse("testParseEnumConstantsOneLiner", reader);
    }

    public void testParseEnumImplements() {
        StringReader reader = new StringReader(
                "enum Two implements I1 {\n"
                        + "ONE, TWO, THREE\n"
                        + "}");
        parse("testParseEnumImplements", reader);
    }

    public void testParseEnumWithValues() {
        StringReader reader = new StringReader(
                "enum Three1 {\n"
                        + "    ONE(1), TWO(2)\n\n"
                        + "    Three1(val) {\n"
                        + "        value = val\n"
                        + "    }\n\n"
                        + "    private final int value"
                        + "}");
        parse("testParseEnumWithValues", reader);

        reader = new StringReader(
                "enum Three1 {\n"
                        + "    @Annotation ONE(1), TWO(2)\n\n"
                        + "    Three1(val) {\n"
                        + "        value = val\n"
                        + "    }\n\n"
                        + "    private final int value"
                        + "}");
        parse("testParseEnumWithValues2", reader);
    }

    public void testParseEnumWithMethodDefinitions() {
        StringReader reader = new StringReader(
                "enum Four {\n"
                        + "    ONE, TWO, THREE\n\n"
                        + "    def someMethod() { }\n"
                        + "    public m2(args) { }\n"
                        + "    int m3(String arg) { }\n"
                        + "}");
        parse("testParseEnumWithMethodDefinitions", reader);
    }

    public void testParseCompleteEnum() {
        StringReader reader = new StringReader(
                "enum Five {\n"
                        + "    ONE { double eval(int v) { return (double) v } }, \n"
                        + "    TWO {\n"
                        + "        double eval(int v) { return (double) v + 1 }\n"
                        + "    }, THREE\n"
                        + "}");
        parse("testParseCompleteEnum", reader);
    }
}
