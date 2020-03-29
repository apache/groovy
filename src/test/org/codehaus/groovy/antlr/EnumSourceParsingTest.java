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

    public void testParseEnumConstants1() {
        parse(getMethodName(), new StringReader(
            "enum E { X, Y, Z }\n"
        ));
    }

    public void testParseEnumConstants2() {
        parse(getMethodName(), new StringReader(
            "enum E {\n" +
            "    X, Y, Z\n" +
            "}\n"
        ));
    }

    public void testParseEnumConstants3() {
        parse(getMethodName(), new StringReader(
            "enum E {\n" +
            "    X,\n" +
            "    Y,\n" +
            "    Z,\n" +
            "}\n"
        ));
    }

    public void testParseEnumConstants4() {
        parse(getMethodName(), new StringReader(
            "enum E {\n" +
            "    X,\n" +
            "    Y,\n" +
            "    Z,\n" +
            "     ;\n" +
            "}\n"
        ));
    }

    public void testParseEnumConstants5() {
        parse(getMethodName(), new StringReader(
            "enum E\n" +
            "{\n" +
            "    X,\n" +
            "    Y,\n" +
            "    Z\n" +
            "}\n"
        ));
    }

    public void testParseEnumImplements1() {
        parse(getMethodName(), new StringReader(
            "enum E implements I {\n" +
            "    X, Y, Z\n" +
            "}\n"
        ));
    }

    public void testParseEnumImplements2() {
        parse(getMethodName(), new StringReader(
            "enum E implements I\n" +
            "{\n" +
            "    X,\n" +
            "    Y,\n" +
            "    Z\n" +
            "}\n"
        ));
    }

    public void testParseEnumImplements3() {
        parse(getMethodName(), new StringReader(
            "enum E\n" +
            "implements I\n" +
            "{\n" +
            "    X,\n" +
            "    Y,\n" +
            "    Z\n" +
            "}\n"
        ));
    }

    public void testParseEnumWithValues() {
        parse(getMethodName(), new StringReader(
            "enum E {\n" +
            "    X(1), Y(2)\n\n" +
            "    E(value) {\n" +
            "        this.value = value\n" +
            "    }\n\n" +
            "    private final int value\n" +
            "}\n"
        ));
    }

    public void testParseEnumWithValues2() {
        parse(getMethodName(), new StringReader(
            "enum E {\n" +
            "    @Annotation X(1), Y(2)\n\n" +
            "    E(value) {\n" +
            "        this.value = value\n" +
            "    }\n\n" +
            "    private final int value\n" +
            "}\n"
        ));
    }

    public void testParseEnumWithValues3() {
        parse(getMethodName(), new StringReader(
            "enum E {\n" +
            "    X(1), Y(2)\n" +
            "    Object value\n" + // different parsing without leading keyword
            "    E(value) {\n" +
            "        this.value = value\n" +
            "    }\n" +
            "}\n"
        ));
    }

    // GROOVY-9301
    public void testParseEnumWithValues3a() {
        parse(getMethodName(), new StringReader(
            "enum E {\n" +
            "    X(1), Y(2),\n" + // trailing comma
            "    Object value\n" +
            "    E(value) {\n" +
            "        this.value = value\n" +
            "    }\n" +
            "}\n"
        ));
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
            "    SPADES(Color.BLACK),\n" + // trailing comma
            "    \n" +
            "    final Color color\n" +
            "    Suit(Color color) {\n" +
            "        this.color = color\n" +
            "    }\n" +
            "}\n"
        ));
    }

    public void testParseEnumWithMethodDefinitions1() {
        parse(getMethodName(), new StringReader(
            "enum E {\n" +
            "    X, Y, Z\n\n" +
            "    def m1() { }\n" +
            "    public m2(args) { }\n" +
            "    int m3(String arg) { }\n" +
            "}\n"
        ));
    }

    public void testParseEnumWithMethodDefinitions2() {
        parse(getMethodName(), new StringReader(
            "enum E {\n" +
            "    X, Y, Z\n\n" +
            "    def <T> T m() { }\n" +
            "}\n"
        ));
    }

    public void testParseEnumWithMethodDefinitions2a() {
        parse(getMethodName(), new StringReader(
            "enum E {\n" +
            "    X, Y, Z\n\n" +
            "    final <T> T m() { }\n" +
            "}\n"
        ));
    }

    public void testParseEnumWithMethodDefinitions2b() {
        parse(getMethodName(), new StringReader(
            "enum E {\n" +
            "    X, Y, Z\n\n" +
            "    public <T> T m() { }\n" +
            "}\n"
        ));
    }

    public void testParseEnumWithAnnotatedMethodDefinition1() {
        parse(getMethodName(), new StringReader(
            "enum Orientation {\n" +
            "    LANDSCAPE, PORTRAIT\n" +
            "    \n" +
            "    @Override\n" +
            "    String toString() {\n" +
            "        name().toLowerCase().capitalize()\n" +
            "    }\n" +
            "}\n"
        ));
    }

    // GROOVY-9301
    public void testParseEnumWithAnnotatedMethodDefinition2() {
        parse(getMethodName(), new StringReader(
            "enum Orientation {\n" +
            "    LANDSCAPE, PORTRAIT,\n" + // trailing comma
            "    \n" +
            "    @Override\n" +
            "    String toString() {\n" +
            "        name().toLowerCase().capitalize()\n" +
            "    }\n" +
            "}\n"
        ));
    }

    // GROOVY-9301
    public void testParseEnumWithAnnotatedMethodDefinition3() {
        parse(getMethodName(), new StringReader(
            "enum Orientation {\n" +
            "    LANDSCAPE, PORTRAIT,\n" + // trailing comma
            "    \n" +
            "    @Deprecated <T> T whatever() {\n" +
            "    }\n" +
            "}\n"
        ));
    }

    public void testParseCompleteEnum() {
        parse(getMethodName(), new StringReader(
            "enum E {\n" +
            "    X { double eval(int v) { return (double) v } }, \n" +
            "    Y {\n" +
            "        double eval(int v) { return (double) v + 1 }\n" +
            "    }, Z\n" +
            "}"
        ));
    }

    public void testParseEnumWithInnerClass1() {
        parse(getMethodName(), new StringReader(
            "enum E {\n" +
            "    X, Y, Z\n" +
            "    class C { }\n" +
            "}\n"
        ));
    }

    // GROOVY-8507
    public void testParseEnumWithInnerClass1a() {
        parse(getMethodName(), new StringReader(
            "enum E {\n" +
            "    X, Y, Z,\n" + // trailing comma
            "    class C { }\n" +
            "}\n"
        ));
    }

    public void testParseEnumWithInnerClass2() {
        parse(getMethodName(), new StringReader(
            "enum E {\n" +
            "    X, Y, Z\n" +
            "    enum E2 { A, B, C }\n" +
            "}\n"
        ));
    }

    // GROOVY-8507
    public void testParseEnumWithInnerClass2a() {
        parse(getMethodName(), new StringReader(
            "enum E {\n" +
            "    X, Y, Z,\n" + // trailing comma
            "    enum Another { A, B, C }\n" +
            "}\n"
        ));
    }

    public void testParseEnumWithInnerClass3() {
        parse(getMethodName(), new StringReader(
            "enum E {\n" +
            "    X, Y, Z\n" +
            "    interface I { }\n" +
            "}\n"
        ));
    }

    // GROOVY-8507
    public void testParseEnumWithInnerClass3a() {
        parse(getMethodName(), new StringReader(
            "enum E {\n" +
            "    X, Y, Z,\n" + // trailing comma
            "    interface I { }\n" +
            "}\n"
        ));
    }

    public void _FIXME_testParseEnumWithInnerClass4() {
        parse(getMethodName(), new StringReader(
            "enum E {\n" +
            "    X, Y, Z\n" +
            "    @interface A { }\n" +
            "}\n"
        ));
    }

    public void _FIXME_testParseEnumWithInnerClass4a() {
        parse(getMethodName(), new StringReader(
            "enum E {\n" +
            "    X, Y, Z,\n" + // trailing comma
            "    @interface A { }\n" +
            "}\n"
        ));
    }

    public void testParseEnumWithInnerClass5() {
        parse(getMethodName(), new StringReader(
            "enum E {\n" +
            "    X, Y, Z\n" +
            "    trait T { }\n" +
            "}\n"
        ));
    }

    // GROOVY-8507
    public void testParseEnumWithInnerClass5a() {
        parse(getMethodName(), new StringReader(
            "enum E {\n" +
            "    X, Y, Z,\n" + // trailing comma
            "    trait T { }\n" +
            "}\n"
        ));
    }
}
