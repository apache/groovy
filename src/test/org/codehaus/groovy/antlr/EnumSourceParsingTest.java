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
public final class EnumSourceParsingTest extends SourceParserTest {

    public void testParseEnumConstants() {
        parse(getMethodName(), new StringReader(
            "enum E {\n" +
            "    ONE, TWO, THREE\n" +
            "}"));
    }

    public void testParseEnumConstantsMultiLine() {
        parse(getMethodName(), new StringReader(
            "enum E {\n" +
            "    ONE,\n" +
            "    TWO,\n" +
            "    THREE,\n" +
            "}"));
    }

    public void testParseEnumConstantsMultiLine2() {
        parse(getMethodName(), new StringReader(
            "enum ParseCode\n" +
            "{\n" +
            "    COMPLETE,\n" +
            "    INCOMPLETE,\n" +
            "    ERROR\n" +
            "}"));
    }

    public void testParseEnumImplementsMultiLine() {
        parse(getMethodName(), new StringReader(
            "enum ParseCode implements I\n" +
            "{\n" +
            "    COMPLETE,\n" +
            "    INCOMPLETE,\n" +
            "    ERROR\n" +
            "}"));
    }

    public void testParseEnumImplementsMultiLine2() {
        parse(getMethodName(), new StringReader(
            "enum ParseCode\n" +
            "implements I\n" +
            "{\n" +
            "    COMPLETE,\n" +
            "    INCOMPLETE,\n" +
            "    ERROR\n" +
            "}"));
    }

    public void testParseEnumConstantsOneLiner() {
        parse(getMethodName(), new StringReader(
            "enum One { ONE, TWO, THREE }"));
    }

    public void testParseEnumImplements() {
        parse(getMethodName(), new StringReader(
            "enum Two implements I1 {\n" +
            "ONE, TWO, THREE\n" +
            "}"));
    }

    public void testParseEnumWithValues() {
        parse(getMethodName(), new StringReader(
            "enum Three1 {\n" +
            "    ONE(1), TWO(2)\n\n" +
            "    Three1(val) {\n" +
            "        value = val\n" +
            "    }\n\n" +
            "    private final int value" +
            "}"));
    }

    public void testParseEnumWithValues2() {
        parse(getMethodName(), new StringReader(
            "enum Three1 {\n" +
            "    @Annotation ONE(1), TWO(2)\n\n" +
            "    Three1(val) {\n" +
            "        value = val\n" +
            "    }\n\n" +
            "    private final int value" +
            "}"));
    }

    public void testParseEnumWithValues3() {
        parse(getMethodName(), new StringReader(
            "enum NonFinal {\n" +
            "    One(1), Two(2)\n" +
            "    Object value\n" + // different parsing without leading keyword
            "    NonFinal(value) {\n" +
            "        this.value = value\n" +
            "    }\n" +
            "}\n"));
    }

    public void testParseEnumWithValues4() {
        parse(getMethodName(), new StringReader(
            "enum Color {\n" +
            "    RED,\n" +
            "    BLACK\n" +
            "}\n" +
            "enum Suit {\n" +
            "    CLUBS(Color.BLACK),\n" +
            "    DIAMONDS(Color.RED),\n" +
            "    HEARTS(Color.RED),\n" +
            "    SPADES(Color.BLACK),\n" +
            "    \n" +
            "    final Color color\n" +
            "    Suit(Color color) {\n" +
            "        this.color = color\n" +
            "    }\n" +
            "}\n"));
    }

    public void testParseEnumWithMethodDefinitions() {
        parse(getMethodName(), new StringReader(
            "enum Four {\n" +
            "    ONE, TWO, THREE\n\n" +
            "    def someMethod() { }\n" +
            "    public m2(args) { }\n" +
            "    int m3(String arg) { }\n" +
            "}"));
    }

    public void testParseEnumWithAnnotatedMethodDefinition() {
        parse(getMethodName(), new StringReader(
            "enum Orientation {\n" +
            "    LANDSCAPE, PORTRAIT\n" +
            "    \n" +
            "    @Override\n" +
            "    String toString() {\n" +
            "        name().toLowerCase().capitalize()\n" +
            "    }\n" +
            "}\n"));
    }

    public void testParseCompleteEnum() {
        parse(getMethodName(), new StringReader(
            "enum Five {\n" +
            "    ONE { double eval(int v) { return (double) v } }, \n" +
            "    TWO {\n" +
            "        double eval(int v) { return (double) v + 1 }\n" +
            "    }, THREE\n" +
            "}"));
    }
}
