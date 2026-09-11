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
package org.apache.groovy.parser.antlr4

import groovy.transform.AutoFinal
import org.junit.jupiter.api.Test

import static org.apache.groovy.parser.antlr4.TestUtils.expectAst

/**
 * Minimal complete <em>positive</em> covering set for the Antlr4 parser,
 * distilled from the groovy-parser (Parrot) lab tests.
 * <p>
 * Each snippet is compiled through {@code CONVERSION} and asserted against
 * a pretty-printed XML AST dump ({@link org.apache.groovy.parser.antlr4.util.AstXmlDumper})
 * that includes source positions on every node.
 */
@AutoFinal
final class ParserPositiveSyntaxTest {

    @Test
    void 'empty compilation unit'() {
        expectAst '', '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ReturnStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |      <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="null"/>
            |    </ReturnStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
        expectAst '', '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ReturnStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |      <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="null"/>
            |    </ReturnStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
        expectAst '''\
            |;
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ReturnStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |      <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="null"/>
            |    </ReturnStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'shebang comment'() {
        expectAst '''\
            |#!/usr/bin/env groovy
            |def x = 1
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ExpressionStatement line="2" column="1" lastLine="2" lastColumn="10">
            |      <DeclarationExpression line="2" column="1" lastLine="2" lastColumn="10" token="=">
            |        <VariableExpression line="2" column="5" lastLine="2" lastColumn="6" name="x"/>
            |        <ConstantExpression line="2" column="9" lastLine="2" lastColumn="10" value="1"/>
            |      </DeclarationExpression>
            |    </ExpressionStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'package declaration'() {
        expectAst '''\
            |package com.example.core
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <PackageNode line="1" column="1" lastLine="1" lastColumn="25" name="com.example.core"/>
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ReturnStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |      <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="null"/>
            |    </ReturnStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
        expectAst '''\
            |@Deprecated
            |package com.example.core
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <AnnotationNode line="1" column="1" lastLine="1" lastColumn="12" class="Deprecated"/>
            |  <PackageNode line="1" column="1" lastLine="2" lastColumn="25" name="com.example.core"/>
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ReturnStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |      <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="null"/>
            |    </ReturnStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'import declaration'() {
        expectAst '''\
            |import java.util.Map
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <ImportNode line="1" column="1" lastLine="1" lastColumn="21" type="java.util.Map"/>
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ReturnStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |      <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="null"/>
            |    </ReturnStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
        expectAst '''\
            |import java.util.*
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <ImportNode line="1" column="1" lastLine="1" lastColumn="19" package="java.util" star="true"/>
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ReturnStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |      <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="null"/>
            |    </ReturnStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
        expectAst '''\
            |import static java.lang.Math.PI
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <ImportNode line="1" column="1" lastLine="1" lastColumn="32" type="java.lang.Math" field="PI" static="true"/>
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ReturnStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |      <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="null"/>
            |    </ReturnStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
        expectAst '''\
            |import static java.lang.Math.*
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <ImportNode line="1" column="1" lastLine="1" lastColumn="31" type="java.lang.Math" static="true" star="true"/>
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ReturnStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |      <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="null"/>
            |    </ReturnStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
        expectAst '''\
            |import java.sql.Blob as Bb
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <ImportNode line="1" column="1" lastLine="1" lastColumn="27" type="java.sql.Blob" alias="Bb"/>
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ReturnStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |      <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="null"/>
            |    </ReturnStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
        expectAst '''\
            |import static java.lang.Math.pow as pw
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <ImportNode line="1" column="1" lastLine="1" lastColumn="39" type="java.lang.Math" field="pow" static="true" alias="pw"/>
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ReturnStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |      <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="null"/>
            |    </ReturnStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
        expectAst '''\
            |@Deprecated
            |import java.util.Map
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <AnnotationNode line="1" column="1" lastLine="1" lastColumn="12" class="Deprecated"/>
            |  <ImportNode line="1" column="1" lastLine="2" lastColumn="21" type="java.util.Map"/>
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ReturnStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |      <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="null"/>
            |    </ReturnStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'class declaration'() {
        expectAst '''\
            |class A {}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <ClassNode line="1" column="1" lastLine="1" lastColumn="11" kind="class" name="A"/>
            |</Module>
            |'''.stripMargin()
        expectAst '''\
            |class B<T> {}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <ClassNode line="1" column="1" lastLine="1" lastColumn="14" kind="class" name="B" generics="&lt;T&gt;"/>
            |</Module>
            |'''.stripMargin()
        expectAst '''\
            |class C<T extends Number & Comparable> extends ArrayList implements Runnable {}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <ClassNode line="1" column="1" lastLine="1" lastColumn="80" kind="class" name="C" generics="&lt;T extends Number &amp; Comparable&gt;" extends="ArrayList" implements="Runnable"/>
            |</Module>
            |'''.stripMargin()
        expectAst '''\
            |public class D {
            |    {
            |        def x = 1
            |    }
            |    static {
            |        def y = 2
            |    }
            |}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <ClassNode line="1" column="1" lastLine="8" lastColumn="2" kind="class" name="D">
            |    <ObjectInitializer>
            |      <BlockStatement line="2" column="5" lastLine="4" lastColumn="6">
            |        <BlockStatement line="2" column="5" lastLine="4" lastColumn="6">
            |          <ExpressionStatement line="3" column="9" lastLine="3" lastColumn="18">
            |            <DeclarationExpression line="3" column="9" lastLine="3" lastColumn="18" token="=">
            |              <VariableExpression line="3" column="13" lastLine="3" lastColumn="14" name="x"/>
            |              <ConstantExpression line="3" column="17" lastLine="3" lastColumn="18" value="1"/>
            |            </DeclarationExpression>
            |          </ExpressionStatement>
            |        </BlockStatement>
            |      </BlockStatement>
            |    </ObjectInitializer>
            |    <StaticInitializer line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |      <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |        <BlockStatement line="5" column="12" lastLine="7" lastColumn="6">
            |          <ExpressionStatement line="6" column="9" lastLine="6" lastColumn="18">
            |            <DeclarationExpression line="6" column="9" lastLine="6" lastColumn="18" token="=">
            |              <VariableExpression line="6" column="13" lastLine="6" lastColumn="14" name="y"/>
            |              <ConstantExpression line="6" column="17" lastLine="6" lastColumn="18" value="2"/>
            |            </DeclarationExpression>
            |          </ExpressionStatement>
            |        </BlockStatement>
            |      </BlockStatement>
            |    </StaticInitializer>
            |  </ClassNode>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'interface declaration'() {
        expectAst '''\
            |interface I {}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <ClassNode line="1" column="1" lastLine="1" lastColumn="15" kind="interface" name="I"/>
            |</Module>
            |'''.stripMargin()
        expectAst '''\
            |interface J<T extends CharSequence> extends Runnable, Closeable {}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <ClassNode line="1" column="1" lastLine="1" lastColumn="67" kind="interface" name="J" generics="&lt;T extends CharSequence&gt;" implements="Runnable,Closeable"/>
            |</Module>
            |'''.stripMargin()
        expectAst '''\
            |interface Greetable {
            |    String name()
            |    default String hello() {
            |        'hello'
            |    }
            |}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <ClassNode line="1" column="1" lastLine="6" lastColumn="2" kind="interface" name="Greetable">
            |    <MethodNode line="2" column="5" lastLine="2" lastColumn="18" name="name" modifiers="abstract" returnType="String"/>
            |    <MethodNode line="3" column="5" lastLine="5" lastColumn="6" name="hello" returnType="String">
            |      <BlockStatement line="3" column="28" lastLine="5" lastColumn="6">
            |        <ExpressionStatement line="4" column="9" lastLine="4" lastColumn="16">
            |          <ConstantExpression line="4" column="9" lastLine="4" lastColumn="16" value="hello"/>
            |        </ExpressionStatement>
            |      </BlockStatement>
            |    </MethodNode>
            |  </ClassNode>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'enum declaration'() {
        expectAst '''\
            |enum E { A, B, C }
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <ClassNode line="1" column="1" lastLine="1" lastColumn="19" kind="enum" name="E" extends="java.lang.Enum&lt;E&gt;">
            |    <FieldNode line="1" column="10" lastLine="1" lastColumn="11" name="A" type="E" modifiers="public static final" enumConstant="true"/>
            |    <FieldNode line="1" column="13" lastLine="1" lastColumn="14" name="B" type="E" modifiers="public static final" enumConstant="true"/>
            |    <FieldNode line="1" column="16" lastLine="1" lastColumn="17" name="C" type="E" modifiers="public static final" enumConstant="true"/>
            |  </ClassNode>
            |</Module>
            |'''.stripMargin()
        expectAst '''\
            |enum F implements Runnable {
            |    X {
            |        void run() {}
            |    },
            |    Y(1)
            |    F() {}
            |    F(int n) {}
            |    void run() {}
            |}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <ClassNode line="1" column="1" lastLine="9" lastColumn="2" kind="enum" name="F" extends="java.lang.Enum&lt;F&gt;" implements="Runnable">
            |    <FieldNode line="2" column="5" lastLine="4" lastColumn="6" name="X" type="F" modifiers="public static final" enumConstant="true"/>
            |    <FieldNode line="5" column="5" lastLine="5" lastColumn="9" name="Y" type="F" modifiers="public static final" enumConstant="true"/>
            |    <ConstructorNode line="6" column="5" lastLine="6" lastColumn="11" name="&lt;init&gt;">
            |      <BlockStatement line="6" column="9" lastLine="6" lastColumn="11"/>
            |    </ConstructorNode>
            |    <ConstructorNode line="7" column="5" lastLine="7" lastColumn="16" name="&lt;init&gt;">
            |      <Parameter line="7" column="7" lastLine="7" lastColumn="12" name="n" type="int"/>
            |      <BlockStatement line="7" column="14" lastLine="7" lastColumn="16"/>
            |    </ConstructorNode>
            |    <MethodNode line="8" column="5" lastLine="8" lastColumn="18" name="run" returnType="void">
            |      <BlockStatement line="8" column="16" lastLine="8" lastColumn="18"/>
            |    </MethodNode>
            |  </ClassNode>
            |  <ClassNode line="2" column="7" lastLine="4" lastColumn="6" kind="enum" name="F$1" extends="F">
            |    <MethodNode line="3" column="9" lastLine="3" lastColumn="22" name="run" returnType="void">
            |      <BlockStatement line="3" column="20" lastLine="3" lastColumn="22"/>
            |    </MethodNode>
            |  </ClassNode>
            |</Module>
            |'''.stripMargin()
        expectAst '''\
            |enum G {
            |    X, Y, Z
            |    trait T {}
            |}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <ClassNode line="1" column="1" lastLine="4" lastColumn="2" kind="enum" name="G" extends="java.lang.Enum&lt;G&gt;">
            |    <FieldNode line="2" column="5" lastLine="2" lastColumn="6" name="X" type="G" modifiers="public static final" enumConstant="true"/>
            |    <FieldNode line="2" column="8" lastLine="2" lastColumn="9" name="Y" type="G" modifiers="public static final" enumConstant="true"/>
            |    <FieldNode line="2" column="11" lastLine="2" lastColumn="12" name="Z" type="G" modifiers="public static final" enumConstant="true"/>
            |  </ClassNode>
            |  <AnnotationNode line="-1" column="-1" lastLine="-1" lastColumn="-1" class="groovy.transform.Trait"/>
            |  <ClassNode line="3" column="5" lastLine="3" lastColumn="15" kind="trait" name="G$T"/>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'annotation declaration'() {
        expectAst '''\
            |@interface A {}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <ClassNode line="1" column="1" lastLine="1" lastColumn="16" kind="annotation" name="A" implements="java.lang.annotation.Annotation"/>
            |</Module>
            |'''.stripMargin()
        expectAst '''\
            |@interface B {
            |    String name() default ''
            |    Class elem() default { 1 + 2 }
            |}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <ClassNode line="1" column="1" lastLine="4" lastColumn="2" kind="annotation" name="B" implements="java.lang.annotation.Annotation">
            |    <MethodNode line="2" column="5" lastLine="2" lastColumn="29" name="name" modifiers="abstract" returnType="String">
            |      <ExpressionStatement line="2" column="27" lastLine="2" lastColumn="29">
            |        <ConstantExpression line="2" column="27" lastLine="2" lastColumn="29"/>
            |      </ExpressionStatement>
            |    </MethodNode>
            |    <MethodNode line="3" column="5" lastLine="3" lastColumn="35" name="elem" modifiers="abstract" returnType="Class">
            |      <ExpressionStatement line="3" column="26" lastLine="3" lastColumn="35">
            |        <ClosureExpression line="3" column="26" lastLine="3" lastColumn="35">
            |          <BlockStatement line="3" column="28" lastLine="3" lastColumn="33">
            |            <ExpressionStatement line="3" column="28" lastLine="3" lastColumn="33">
            |              <BinaryExpression line="3" column="28" lastLine="3" lastColumn="33" token="+">
            |                <ConstantExpression line="3" column="28" lastLine="3" lastColumn="29" value="1"/>
            |                <ConstantExpression line="3" column="32" lastLine="3" lastColumn="33" value="2"/>
            |              </BinaryExpression>
            |            </ExpressionStatement>
            |          </BlockStatement>
            |        </ClosureExpression>
            |      </ExpressionStatement>
            |    </MethodNode>
            |  </ClassNode>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'trait declaration'() {
        expectAst '''\
            |trait T {}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <AnnotationNode line="-1" column="-1" lastLine="-1" lastColumn="-1" class="groovy.transform.Trait"/>
            |  <ClassNode line="1" column="1" lastLine="1" lastColumn="11" kind="trait" name="T"/>
            |</Module>
            |'''.stripMargin()
        expectAst '''\
            |trait U<T> extends Readable implements Closeable {
            |    def m() { 1 }
            |    abstract String n()
            |}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <AnnotationNode line="-1" column="-1" lastLine="-1" lastColumn="-1" class="groovy.transform.Trait"/>
            |  <ClassNode line="1" column="1" lastLine="4" lastColumn="2" kind="trait" name="U" generics="&lt;T&gt;" extends="Readable" implements="Closeable">
            |    <MethodNode line="2" column="5" lastLine="2" lastColumn="18" name="m" returnType="java.lang.Object">
            |      <BlockStatement line="2" column="13" lastLine="2" lastColumn="18">
            |        <ExpressionStatement line="2" column="15" lastLine="2" lastColumn="16">
            |          <ConstantExpression line="2" column="15" lastLine="2" lastColumn="16" value="1"/>
            |        </ExpressionStatement>
            |      </BlockStatement>
            |    </MethodNode>
            |    <MethodNode line="3" column="5" lastLine="3" lastColumn="24" name="n" modifiers="abstract" returnType="String"/>
            |  </ClassNode>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'record declaration'() {
        expectAst '''\
            |record Fruit(String name, double price) {}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <AnnotationNode line="-1" column="-1" lastLine="-1" lastColumn="-1" class="groovy.transform.RecordType"/>
            |  <ClassNode line="1" column="1" lastLine="1" lastColumn="43" kind="record" name="Fruit"/>
            |</Module>
            |'''.stripMargin()
        expectAst '''\
            |record Point(int x, int y, String color) {
            |    public Point {
            |        x = -x
            |    }
            |    public Point(int x, int y) {
            |        this(x, y, 'Blue')
            |    }
            |}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <AnnotationNode line="-1" column="-1" lastLine="-1" lastColumn="-1" class="groovy.transform.RecordType"/>
            |  <AnnotationNode line="-1" column="-1" lastLine="-1" lastColumn="-1" class="groovy.transform.TupleConstructor" members="pre"/>
            |  <ClassNode line="1" column="1" lastLine="8" lastColumn="2" kind="record" name="Point">
            |    <ConstructorNode line="5" column="5" lastLine="7" lastColumn="6" name="&lt;init&gt;">
            |      <Parameter line="5" column="18" lastLine="5" lastColumn="23" name="x" type="int"/>
            |      <Parameter line="5" column="25" lastLine="5" lastColumn="30" name="y" type="int"/>
            |      <BlockStatement line="5" column="32" lastLine="7" lastColumn="6">
            |        <ExpressionStatement line="6" column="9" lastLine="6" lastColumn="27">
            |          <ConstructorCallExpression line="6" column="9" lastLine="6" lastColumn="27" type="java.lang.Object" special="this">
            |            <ArgumentListExpression line="6" column="13" lastLine="6" lastColumn="27">
            |              <VariableExpression line="6" column="14" lastLine="6" lastColumn="15" name="x"/>
            |              <VariableExpression line="6" column="17" lastLine="6" lastColumn="18" name="y"/>
            |              <ConstantExpression line="6" column="20" lastLine="6" lastColumn="26" value="Blue"/>
            |            </ArgumentListExpression>
            |          </ConstructorCallExpression>
            |        </ExpressionStatement>
            |      </BlockStatement>
            |    </ConstructorNode>
            |  </ClassNode>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'sealed type declaration'() {
        expectAst '''\
            |sealed interface Shape permits Circle, Rectangle {}
            |final class Circle implements Shape {}
            |non-sealed class Rectangle implements Shape {}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <ClassNode line="2" column="1" lastLine="2" lastColumn="39" kind="class" name="Circle" modifiers="final" implements="Shape"/>
            |  <AnnotationNode line="-1" column="-1" lastLine="-1" lastColumn="-1" class="groovy.transform.NonSealed"/>
            |  <ClassNode line="3" column="1" lastLine="3" lastColumn="47" kind="class" name="Rectangle" implements="Shape"/>
            |  <AnnotationNode line="1" column="24" lastLine="1" lastColumn="31" class="groovy.transform.Sealed" members="permittedSubclasses"/>
            |  <ClassNode line="1" column="1" lastLine="1" lastColumn="52" kind="interface" name="Shape"/>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'nested and anonymous types'() {
        expectAst '''\
            |class Outer {
            |    class Inner {}
            |    static class Nested {}
            |    interface I {}
            |    enum E { A }
            |    trait T {}
            |    @interface A {}
            |}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <ClassNode line="1" column="1" lastLine="8" lastColumn="2" kind="class" name="Outer"/>
            |  <ClassNode line="7" column="5" lastLine="7" lastColumn="20" kind="annotation" name="Outer$A" modifiers="static" implements="java.lang.annotation.Annotation"/>
            |  <ClassNode line="5" column="5" lastLine="5" lastColumn="17" kind="enum" name="Outer$E" modifiers="static" extends="java.lang.Enum&lt;Outer.E&gt;">
            |    <FieldNode line="5" column="14" lastLine="5" lastColumn="15" name="A" type="Outer$E" modifiers="public static final" enumConstant="true"/>
            |  </ClassNode>
            |  <ClassNode line="4" column="5" lastLine="4" lastColumn="19" kind="interface" name="Outer$I" modifiers="static"/>
            |  <ClassNode line="2" column="5" lastLine="2" lastColumn="19" kind="class" name="Outer$Inner"/>
            |  <ClassNode line="3" column="5" lastLine="3" lastColumn="27" kind="class" name="Outer$Nested" modifiers="static"/>
            |  <AnnotationNode line="-1" column="-1" lastLine="-1" lastColumn="-1" class="groovy.transform.Trait"/>
            |  <ClassNode line="6" column="5" lastLine="6" lastColumn="15" kind="trait" name="Outer$T"/>
            |</Module>
            |'''.stripMargin()
        expectAst '''\
            |def r = new Runnable() {
            |    void run() {}
            |}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ExpressionStatement line="1" column="1" lastLine="3" lastColumn="2">
            |      <DeclarationExpression line="1" column="1" lastLine="3" lastColumn="2" token="=">
            |        <VariableExpression line="1" column="5" lastLine="1" lastColumn="6" name="r"/>
            |        <ConstructorCallExpression line="1" column="9" lastLine="3" lastColumn="2" type="test$1" anonymous="true">
            |          <ArgumentListExpression line="-1" column="-1" lastLine="-1" lastColumn="-1"/>
            |        </ConstructorCallExpression>
            |      </DeclarationExpression>
            |    </ExpressionStatement>
            |  </BlockStatement>
            |  <ClassNode line="1" column="24" lastLine="3" lastColumn="2" kind="class" name="test$1" extends="Runnable">
            |    <MethodNode line="2" column="5" lastLine="2" lastColumn="18" name="run" returnType="void">
            |      <BlockStatement line="2" column="16" lastLine="2" lastColumn="18"/>
            |    </MethodNode>
            |  </ClassNode>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'non-static inner class instantiation'() {
        expectAst '''\
            |class Y {
            |    class X {
            |        X(String name) {}
            |    }
            |    def m() {
            |        this.new X('Daniel')
            |    }
            |}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <ClassNode line="1" column="1" lastLine="8" lastColumn="2" kind="class" name="Y">
            |    <MethodNode line="5" column="5" lastLine="7" lastColumn="6" name="m" returnType="java.lang.Object">
            |      <BlockStatement line="5" column="13" lastLine="7" lastColumn="6">
            |        <ExpressionStatement line="6" column="9" lastLine="6" lastColumn="29">
            |          <ConstructorCallExpression line="6" column="9" lastLine="6" lastColumn="29" type="X">
            |            <ArgumentListExpression line="6" column="19" lastLine="6" lastColumn="29">
            |              <VariableExpression line="6" column="9" lastLine="6" lastColumn="13" name="this"/>
            |              <ConstantExpression line="6" column="20" lastLine="6" lastColumn="28" value="Daniel"/>
            |            </ArgumentListExpression>
            |          </ConstructorCallExpression>
            |        </ExpressionStatement>
            |      </BlockStatement>
            |    </MethodNode>
            |  </ClassNode>
            |  <ClassNode line="2" column="5" lastLine="4" lastColumn="6" kind="class" name="Y$X">
            |    <ConstructorNode line="3" column="9" lastLine="3" lastColumn="26" name="&lt;init&gt;">
            |      <Parameter line="3" column="11" lastLine="3" lastColumn="22" name="name" type="String"/>
            |      <BlockStatement line="3" column="24" lastLine="3" lastColumn="26"/>
            |    </ConstructorNode>
            |  </ClassNode>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'field and property declaration'() {
        expectAst '''\
            |class Person {
            |    public static final SOME_CONSTANT = 'x'
            |    private String name = 'Daniel'
            |    private int age
            |    String country = 'China', location = 'Shanghai'
            |    final String title = 'dev'
            |    protected static def extra
            |}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <ClassNode line="1" column="1" lastLine="8" lastColumn="2" kind="class" name="Person">
            |    <FieldNode line="2" column="5" lastLine="2" lastColumn="44" name="SOME_CONSTANT" type="java.lang.Object" modifiers="public static final">
            |      <ConstantExpression line="2" column="41" lastLine="2" lastColumn="44" value="x"/>
            |    </FieldNode>
            |    <FieldNode line="3" column="5" lastLine="3" lastColumn="35" name="name" type="String" modifiers="private">
            |      <ConstantExpression line="3" column="27" lastLine="3" lastColumn="35" value="Daniel"/>
            |    </FieldNode>
            |    <FieldNode line="4" column="5" lastLine="4" lastColumn="20" name="age" type="int" modifiers="private"/>
            |    <FieldNode line="7" column="5" lastLine="7" lastColumn="31" name="extra" type="java.lang.Object" modifiers="protected static"/>
            |  </ClassNode>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'method and constructor declaration'() {
        expectAst '''\
            |class C {
            |    C() {}
            |    C(String name) throws Exception {}
            |    def <T extends List> T m(final int a, long b = 1, String... rest) {
            |        a
            |    }
            |    def 'hello world'(p1, p2) {}
            |    static m2(a) {}
            |}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <ClassNode line="1" column="1" lastLine="9" lastColumn="2" kind="class" name="C">
            |    <ConstructorNode line="2" column="5" lastLine="2" lastColumn="11" name="&lt;init&gt;">
            |      <BlockStatement line="2" column="9" lastLine="2" lastColumn="11"/>
            |    </ConstructorNode>
            |    <ConstructorNode line="3" column="5" lastLine="3" lastColumn="39" name="&lt;init&gt;" throws="Exception">
            |      <Parameter line="3" column="7" lastLine="3" lastColumn="18" name="name" type="String"/>
            |      <BlockStatement line="3" column="37" lastLine="3" lastColumn="39"/>
            |    </ConstructorNode>
            |    <MethodNode line="4" column="5" lastLine="6" lastColumn="6" name="m" returnType="T">
            |      <Parameter line="4" column="30" lastLine="4" lastColumn="41" name="a" type="int"/>
            |      <Parameter line="4" column="43" lastLine="4" lastColumn="53" name="b" type="long">
            |        <ConstantExpression line="4" column="52" lastLine="4" lastColumn="53" value="1"/>
            |      </Parameter>
            |      <Parameter line="4" column="55" lastLine="4" lastColumn="69" name="rest" type="String[]"/>
            |      <BlockStatement line="4" column="71" lastLine="6" lastColumn="6">
            |        <ExpressionStatement line="5" column="9" lastLine="5" lastColumn="10">
            |          <VariableExpression line="5" column="9" lastLine="5" lastColumn="10" name="a"/>
            |        </ExpressionStatement>
            |      </BlockStatement>
            |    </MethodNode>
            |    <MethodNode line="7" column="5" lastLine="7" lastColumn="33" name="hello world" returnType="java.lang.Object">
            |      <Parameter line="7" column="23" lastLine="7" lastColumn="25" name="p1"/>
            |      <Parameter line="7" column="27" lastLine="7" lastColumn="29" name="p2"/>
            |      <BlockStatement line="7" column="31" lastLine="7" lastColumn="33"/>
            |    </MethodNode>
            |    <MethodNode line="8" column="5" lastLine="8" lastColumn="20" name="m2" modifiers="static" returnType="java.lang.Object">
            |      <Parameter line="8" column="15" lastLine="8" lastColumn="16" name="a"/>
            |      <BlockStatement line="8" column="18" lastLine="8" lastColumn="20"/>
            |    </MethodNode>
            |  </ClassNode>
            |</Module>
            |'''.stripMargin()
        expectAst '''\
            |int plus(int a, int b) {
            |    return a + b
            |}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <MethodNode line="1" column="1" lastLine="3" lastColumn="2" name="plus" returnType="int">
            |    <Parameter line="1" column="10" lastLine="1" lastColumn="15" name="a" type="int"/>
            |    <Parameter line="1" column="17" lastLine="1" lastColumn="22" name="b" type="int"/>
            |    <BlockStatement line="1" column="24" lastLine="3" lastColumn="2">
            |      <ReturnStatement line="2" column="5" lastLine="2" lastColumn="17">
            |        <BinaryExpression line="2" column="12" lastLine="2" lastColumn="17" token="+">
            |          <VariableExpression line="2" column="12" lastLine="2" lastColumn="13" name="a"/>
            |          <VariableExpression line="2" column="16" lastLine="2" lastColumn="17" name="b"/>
            |        </BinaryExpression>
            |      </ReturnStatement>
            |    </BlockStatement>
            |  </MethodNode>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'local variable declaration'() {
        expectAst '''\
            |int a
            |int b = 1
            |final d = 1
            |def e, f = 2
            |var name = 'Daniel'
            |def (int x, int y) = [1, 2]
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ExpressionStatement line="1" column="1" lastLine="1" lastColumn="6">
            |      <DeclarationExpression line="1" column="1" lastLine="1" lastColumn="6" token="=">
            |        <VariableExpression line="1" column="5" lastLine="1" lastColumn="6" name="a" type="java.lang.Integer"/>
            |        < line="-1" column="-1" lastLine="-1" lastColumn="-1"/>
            |      </DeclarationExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="2" column="1" lastLine="2" lastColumn="10">
            |      <DeclarationExpression line="2" column="1" lastLine="2" lastColumn="10" token="=">
            |        <VariableExpression line="2" column="5" lastLine="2" lastColumn="6" name="b" type="java.lang.Integer"/>
            |        <ConstantExpression line="2" column="9" lastLine="2" lastColumn="10" value="1"/>
            |      </DeclarationExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="3" column="1" lastLine="3" lastColumn="12">
            |      <DeclarationExpression line="3" column="1" lastLine="3" lastColumn="12" token="=">
            |        <VariableExpression line="3" column="7" lastLine="3" lastColumn="8" name="d"/>
            |        <ConstantExpression line="3" column="11" lastLine="3" lastColumn="12" value="1"/>
            |      </DeclarationExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="4" column="1" lastLine="4" lastColumn="6">
            |      <DeclarationExpression line="4" column="1" lastLine="4" lastColumn="6" token="=">
            |        <VariableExpression line="4" column="5" lastLine="4" lastColumn="6" name="e"/>
            |        < line="-1" column="-1" lastLine="-1" lastColumn="-1"/>
            |      </DeclarationExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="4" column="8" lastLine="4" lastColumn="13">
            |      <DeclarationExpression line="4" column="8" lastLine="4" lastColumn="13" token="=">
            |        <VariableExpression line="4" column="8" lastLine="4" lastColumn="9" name="f"/>
            |        <ConstantExpression line="4" column="12" lastLine="4" lastColumn="13" value="2"/>
            |      </DeclarationExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="5" column="1" lastLine="5" lastColumn="20">
            |      <DeclarationExpression line="5" column="1" lastLine="5" lastColumn="20" token="=">
            |        <VariableExpression line="5" column="5" lastLine="5" lastColumn="9" name="name"/>
            |        <ConstantExpression line="5" column="12" lastLine="5" lastColumn="20" value="Daniel"/>
            |      </DeclarationExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="6" column="1" lastLine="6" lastColumn="28">
            |      <DeclarationExpression line="6" column="1" lastLine="6" lastColumn="28" token="=">
            |        <TupleExpression line="6" column="5" lastLine="6" lastColumn="19">
            |          <VariableExpression line="6" column="6" lastLine="6" lastColumn="11" name="x" type="java.lang.Integer"/>
            |          <VariableExpression line="6" column="13" lastLine="6" lastColumn="18" name="y" type="java.lang.Integer"/>
            |        </TupleExpression>
            |        <ListExpression line="6" column="22" lastLine="6" lastColumn="28">
            |          <ConstantExpression line="6" column="23" lastLine="6" lastColumn="24" value="1"/>
            |          <ConstantExpression line="6" column="26" lastLine="6" lastColumn="27" value="2"/>
            |        </ListExpression>
            |      </DeclarationExpression>
            |    </ExpressionStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'block and empty statement'() {
        expectAst '''\
            |{
            |    ;
            |    def x = 1
            |    {
            |        def y = 2
            |    }
            |}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <BlockStatement line="1" column="1" lastLine="7" lastColumn="2">
            |      <ExpressionStatement line="3" column="5" lastLine="6" lastColumn="6">
            |        <DeclarationExpression line="3" column="5" lastLine="6" lastColumn="6" token="=">
            |          <VariableExpression line="3" column="9" lastLine="3" lastColumn="10" name="x"/>
            |          <MethodCallExpression line="3" column="13" lastLine="6" lastColumn="6">
            |            <ConstantExpression line="3" column="13" lastLine="3" lastColumn="14" value="1"/>
            |            <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="call"/>
            |            <ClosureExpression line="4" column="5" lastLine="6" lastColumn="6">
            |              <BlockStatement line="5" column="9" lastLine="6" lastColumn="1">
            |                <ExpressionStatement line="5" column="9" lastLine="5" lastColumn="18">
            |                  <DeclarationExpression line="5" column="9" lastLine="5" lastColumn="18" token="=">
            |                    <VariableExpression line="5" column="13" lastLine="5" lastColumn="14" name="y"/>
            |                    <ConstantExpression line="5" column="17" lastLine="5" lastColumn="18" value="2"/>
            |                  </DeclarationExpression>
            |                </ExpressionStatement>
            |              </BlockStatement>
            |            </ClosureExpression>
            |          </MethodCallExpression>
            |        </DeclarationExpression>
            |      </ExpressionStatement>
            |    </BlockStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'if else statement'() {
        expectAst '''\
            |if (true) 1 else 0
            |if (true) {
            |    1
            |} else if (false) {
            |    0
            |} else {
            |    -1
            |}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <IfStatement line="1" column="1" lastLine="1" lastColumn="19">
            |      <BooleanExpression line="1" column="5" lastLine="1" lastColumn="9">
            |        <ConstantExpression line="1" column="5" lastLine="1" lastColumn="9" value="true"/>
            |      </BooleanExpression>
            |      <ExpressionStatement line="1" column="11" lastLine="1" lastColumn="12">
            |        <ConstantExpression line="1" column="11" lastLine="1" lastColumn="12" value="1"/>
            |      </ExpressionStatement>
            |      <else>
            |        <ExpressionStatement line="1" column="18" lastLine="1" lastColumn="19">
            |          <ConstantExpression line="1" column="18" lastLine="1" lastColumn="19" value="0"/>
            |        </ExpressionStatement>
            |      </else>
            |    </IfStatement>
            |    <IfStatement line="2" column="1" lastLine="8" lastColumn="2">
            |      <BooleanExpression line="2" column="5" lastLine="2" lastColumn="9">
            |        <ConstantExpression line="2" column="5" lastLine="2" lastColumn="9" value="true"/>
            |      </BooleanExpression>
            |      <BlockStatement line="2" column="11" lastLine="4" lastColumn="2">
            |        <ExpressionStatement line="3" column="5" lastLine="3" lastColumn="6">
            |          <ConstantExpression line="3" column="5" lastLine="3" lastColumn="6" value="1"/>
            |        </ExpressionStatement>
            |      </BlockStatement>
            |      <else>
            |        <IfStatement line="4" column="8" lastLine="8" lastColumn="2">
            |          <BooleanExpression line="4" column="12" lastLine="4" lastColumn="17">
            |            <ConstantExpression line="4" column="12" lastLine="4" lastColumn="17" value="false"/>
            |          </BooleanExpression>
            |          <BlockStatement line="4" column="19" lastLine="6" lastColumn="2">
            |            <ExpressionStatement line="5" column="5" lastLine="5" lastColumn="6">
            |              <ConstantExpression line="5" column="5" lastLine="5" lastColumn="6" value="0"/>
            |            </ExpressionStatement>
            |          </BlockStatement>
            |          <else>
            |            <BlockStatement line="6" column="8" lastLine="8" lastColumn="2">
            |              <ExpressionStatement line="7" column="5" lastLine="7" lastColumn="7">
            |                <ConstantExpression line="7" column="5" lastLine="7" lastColumn="7" value="-1"/>
            |              </ExpressionStatement>
            |            </BlockStatement>
            |          </else>
            |        </IfStatement>
            |      </else>
            |    </IfStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'switch statement'() {
        expectAst '''\
            |switch (a) {
            |    case 1:
            |        break
            |    case 2 + 3:
            |    case 4:
            |        break
            |    default:
            |        break
            |}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <SwitchStatement line="1" column="1" lastLine="9" lastColumn="2">
            |      <VariableExpression line="1" column="9" lastLine="1" lastColumn="10" name="a"/>
            |      <CaseStatement line="2" column="5" lastLine="2" lastColumn="9">
            |        <ConstantExpression line="2" column="10" lastLine="2" lastColumn="11" value="1"/>
            |        <BlockStatement line="3" column="9" lastLine="4" lastColumn="1">
            |          <BreakStatement line="3" column="9" lastLine="3" lastColumn="14"/>
            |        </BlockStatement>
            |      </CaseStatement>
            |      <CaseStatement line="4" column="5" lastLine="4" lastColumn="9">
            |        <BinaryExpression line="4" column="10" lastLine="4" lastColumn="15" token="+">
            |          <ConstantExpression line="4" column="10" lastLine="4" lastColumn="11" value="2"/>
            |          <ConstantExpression line="4" column="14" lastLine="4" lastColumn="15" value="3"/>
            |        </BinaryExpression>
            |        < line="-1" column="-1" lastLine="-1" lastColumn="-1"/>
            |      </CaseStatement>
            |      <CaseStatement line="4" column="5" lastLine="4" lastColumn="9">
            |        <ConstantExpression line="5" column="10" lastLine="5" lastColumn="11" value="4"/>
            |        <BlockStatement line="6" column="9" lastLine="7" lastColumn="1">
            |          <BreakStatement line="6" column="9" lastLine="6" lastColumn="14"/>
            |        </BlockStatement>
            |      </CaseStatement>
            |      <default>
            |        <BlockStatement line="8" column="9" lastLine="9" lastColumn="1">
            |          <BreakStatement line="8" column="9" lastLine="8" lastColumn="14"/>
            |        </BlockStatement>
            |      </default>
            |    </SwitchStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'for statement'() {
        expectAst '''\
            |for (i in someList) {}
            |for (final String i in someList) { break }
            |for (int i : someList) { continue }
            |for (int i = 0, j = 10; i < j; i++, j--) {}
            |for (;;) {}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ForStatement line="1" column="1" lastLine="1" lastColumn="23">
            |      <Parameter line="1" column="6" lastLine="1" lastColumn="7" name="i"/>
            |      <VariableExpression line="1" column="11" lastLine="1" lastColumn="19" name="someList"/>
            |      <BlockStatement line="1" column="21" lastLine="1" lastColumn="23"/>
            |    </ForStatement>
            |    <ForStatement line="2" column="1" lastLine="2" lastColumn="43">
            |      <Parameter line="2" column="19" lastLine="2" lastColumn="20" name="i" type="String"/>
            |      <VariableExpression line="2" column="24" lastLine="2" lastColumn="32" name="someList"/>
            |      <BlockStatement line="2" column="34" lastLine="2" lastColumn="43">
            |        <BreakStatement line="2" column="36" lastLine="2" lastColumn="41"/>
            |      </BlockStatement>
            |    </ForStatement>
            |    <ForStatement line="3" column="1" lastLine="3" lastColumn="36">
            |      <Parameter line="3" column="10" lastLine="3" lastColumn="11" name="i" type="int"/>
            |      <VariableExpression line="3" column="14" lastLine="3" lastColumn="22" name="someList"/>
            |      <BlockStatement line="3" column="24" lastLine="3" lastColumn="36">
            |        <ContinueStatement line="3" column="26" lastLine="3" lastColumn="34"/>
            |      </BlockStatement>
            |    </ForStatement>
            |    <ForStatement line="4" column="1" lastLine="4" lastColumn="44">
            |      <ClosureListExpression line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |        <ClosureListExpression line="4" column="6" lastLine="4" lastColumn="23">
            |          <DeclarationExpression line="4" column="6" lastLine="4" lastColumn="15" token="=">
            |            <VariableExpression line="4" column="10" lastLine="4" lastColumn="11" name="i" type="java.lang.Integer"/>
            |            <ConstantExpression line="4" column="14" lastLine="4" lastColumn="15" value="0"/>
            |          </DeclarationExpression>
            |          <DeclarationExpression line="4" column="17" lastLine="4" lastColumn="23" token="=">
            |            <VariableExpression line="4" column="17" lastLine="4" lastColumn="18" name="j" type="java.lang.Integer"/>
            |            <ConstantExpression line="4" column="21" lastLine="4" lastColumn="23" value="10"/>
            |          </DeclarationExpression>
            |        </ClosureListExpression>
            |        <BinaryExpression line="4" column="25" lastLine="4" lastColumn="30" token="&lt;">
            |          <VariableExpression line="4" column="25" lastLine="4" lastColumn="26" name="i"/>
            |          <VariableExpression line="4" column="29" lastLine="4" lastColumn="30" name="j"/>
            |        </BinaryExpression>
            |        <ClosureListExpression line="4" column="32" lastLine="4" lastColumn="40">
            |          <PostfixExpression line="4" column="32" lastLine="4" lastColumn="35" token="++">
            |            <VariableExpression line="4" column="32" lastLine="4" lastColumn="33" name="i"/>
            |          </PostfixExpression>
            |          <PostfixExpression line="4" column="37" lastLine="4" lastColumn="40" token="--">
            |            <VariableExpression line="4" column="37" lastLine="4" lastColumn="38" name="j"/>
            |          </PostfixExpression>
            |        </ClosureListExpression>
            |      </ClosureListExpression>
            |      <BlockStatement line="4" column="42" lastLine="4" lastColumn="44"/>
            |    </ForStatement>
            |    <ForStatement line="5" column="1" lastLine="5" lastColumn="12">
            |      <ClosureListExpression line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |        < line="-1" column="-1" lastLine="-1" lastColumn="-1"/>
            |        < line="-1" column="-1" lastLine="-1" lastColumn="-1"/>
            |        < line="-1" column="-1" lastLine="-1" lastColumn="-1"/>
            |      </ClosureListExpression>
            |      <BlockStatement line="5" column="10" lastLine="5" lastColumn="12"/>
            |    </ForStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'while statement'() {
        expectAst '''\
            |while (true) break
            |out:
            |while (true) {
            |    continue out
            |}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <WhileStatement line="1" column="1" lastLine="1" lastColumn="19">
            |      <BooleanExpression line="1" column="8" lastLine="1" lastColumn="12">
            |        <ConstantExpression line="1" column="8" lastLine="1" lastColumn="12" value="true"/>
            |      </BooleanExpression>
            |      <BreakStatement line="1" column="14" lastLine="1" lastColumn="19"/>
            |    </WhileStatement>
            |    <WhileStatement line="3" column="1" lastLine="5" lastColumn="2" label="out">
            |      <BooleanExpression line="3" column="8" lastLine="3" lastColumn="12">
            |        <ConstantExpression line="3" column="8" lastLine="3" lastColumn="12" value="true"/>
            |      </BooleanExpression>
            |      <BlockStatement line="3" column="14" lastLine="5" lastColumn="2">
            |        <ContinueStatement line="4" column="5" lastLine="4" lastColumn="17" label="out"/>
            |      </BlockStatement>
            |    </WhileStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'do while statement'() {
        expectAst '''\
            |do {
            |    i++
            |} while (i < 5)
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <DoWhileStatement line="1" column="1" lastLine="3" lastColumn="16">
            |      <BlockStatement line="1" column="4" lastLine="3" lastColumn="2">
            |        <ExpressionStatement line="2" column="5" lastLine="2" lastColumn="8">
            |          <PostfixExpression line="2" column="5" lastLine="2" lastColumn="8" token="++">
            |            <VariableExpression line="2" column="5" lastLine="2" lastColumn="6" name="i"/>
            |          </PostfixExpression>
            |        </ExpressionStatement>
            |      </BlockStatement>
            |      <BooleanExpression line="3" column="10" lastLine="3" lastColumn="15">
            |        <BinaryExpression line="3" column="10" lastLine="3" lastColumn="15" token="&lt;">
            |          <VariableExpression line="3" column="10" lastLine="3" lastColumn="11" name="i"/>
            |          <ConstantExpression line="3" column="14" lastLine="3" lastColumn="15" value="5"/>
            |        </BinaryExpression>
            |      </BooleanExpression>
            |    </DoWhileStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'try catch finally'() {
        expectAst '''\
            |try {
            |    1
            |} catch (e) {
            |    0
            |}
            |try {
            |    1
            |} catch (final IOException | IllegalArgumentException e) {
            |    0
            |} finally {
            |    2
            |}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <TryCatchStatement line="1" column="1" lastLine="5" lastColumn="2">
            |      <BlockStatement line="1" column="5" lastLine="3" lastColumn="2">
            |        <ExpressionStatement line="2" column="5" lastLine="2" lastColumn="6">
            |          <ConstantExpression line="2" column="5" lastLine="2" lastColumn="6" value="1"/>
            |        </ExpressionStatement>
            |      </BlockStatement>
            |      <CatchStatement line="3" column="3" lastLine="5" lastColumn="2" type="java.lang.Object" variable="e">
            |        <BlockStatement line="3" column="13" lastLine="5" lastColumn="2">
            |          <ExpressionStatement line="4" column="5" lastLine="4" lastColumn="6">
            |            <ConstantExpression line="4" column="5" lastLine="4" lastColumn="6" value="0"/>
            |          </ExpressionStatement>
            |        </BlockStatement>
            |      </CatchStatement>
            |      <finally>
            |        < line="-1" column="-1" lastLine="-1" lastColumn="-1"/>
            |      </finally>
            |    </TryCatchStatement>
            |    <TryCatchStatement line="6" column="1" lastLine="12" lastColumn="2">
            |      <BlockStatement line="6" column="5" lastLine="8" lastColumn="2">
            |        <ExpressionStatement line="7" column="5" lastLine="7" lastColumn="6">
            |          <ConstantExpression line="7" column="5" lastLine="7" lastColumn="6" value="1"/>
            |        </ExpressionStatement>
            |      </BlockStatement>
            |      <CatchStatement line="8" column="3" lastLine="10" lastColumn="2" type="IOException" variable="e">
            |        <BlockStatement line="8" column="58" lastLine="10" lastColumn="2">
            |          <ExpressionStatement line="9" column="5" lastLine="9" lastColumn="6">
            |            <ConstantExpression line="9" column="5" lastLine="9" lastColumn="6" value="0"/>
            |          </ExpressionStatement>
            |        </BlockStatement>
            |      </CatchStatement>
            |      <CatchStatement line="8" column="3" lastLine="10" lastColumn="2" type="IllegalArgumentException" variable="e">
            |        <BlockStatement line="8" column="58" lastLine="10" lastColumn="2">
            |          <ExpressionStatement line="9" column="5" lastLine="9" lastColumn="6">
            |            <ConstantExpression line="9" column="5" lastLine="9" lastColumn="6" value="0"/>
            |          </ExpressionStatement>
            |        </BlockStatement>
            |      </CatchStatement>
            |      <finally>
            |        <BlockStatement line="10" column="3" lastLine="12" lastColumn="2">
            |          <BlockStatement line="10" column="11" lastLine="12" lastColumn="2">
            |            <ExpressionStatement line="11" column="5" lastLine="11" lastColumn="6">
            |              <ConstantExpression line="11" column="5" lastLine="11" lastColumn="6" value="2"/>
            |            </ExpressionStatement>
            |          </BlockStatement>
            |        </BlockStatement>
            |      </finally>
            |    </TryCatchStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'try with resources'() {
        expectAst '''\
            |try (Resource r1 = new Resource(1)) {
            |    r1
            |}
            |try (r1) {
            |    r1
            |}
            |try (Resource r1 = new Resource(1); Resource r2 = new Resource(2)) {
            |    r1
            |}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <BlockStatement line="1" column="1" lastLine="3" lastColumn="2">
            |      <ExpressionStatement line="1" column="6" lastLine="1" lastColumn="35">
            |        <DeclarationExpression line="1" column="6" lastLine="1" lastColumn="35" token="=">
            |          <VariableExpression line="1" column="15" lastLine="1" lastColumn="17" name="r1" type="Resource"/>
            |          <ConstructorCallExpression line="1" column="20" lastLine="1" lastColumn="35" type="Resource">
            |            <ConstantExpression line="1" column="33" lastLine="1" lastColumn="34" value="1"/>
            |          </ConstructorCallExpression>
            |        </DeclarationExpression>
            |      </ExpressionStatement>
            |      <ExpressionStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |        <DeclarationExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" token="=">
            |          <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="__$$primaryExc0" type="java.lang.Throwable"/>
            |          <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="null"/>
            |        </DeclarationExpression>
            |      </ExpressionStatement>
            |      <TryCatchStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |        <BlockStatement line="1" column="37" lastLine="3" lastColumn="2">
            |          <ExpressionStatement line="2" column="5" lastLine="2" lastColumn="7">
            |            <VariableExpression line="2" column="5" lastLine="2" lastColumn="7" name="r1"/>
            |          </ExpressionStatement>
            |        </BlockStatement>
            |        <CatchStatement line="-1" column="-1" lastLine="-1" lastColumn="-1" type="java.lang.Throwable" variable="__$$t0">
            |          <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |            <ExpressionStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |              <BinaryExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" token="=">
            |                <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="__$$primaryExc0"/>
            |                <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="__$$t0"/>
            |              </BinaryExpression>
            |            </ExpressionStatement>
            |            <ThrowStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |              <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="__$$t0"/>
            |            </ThrowStatement>
            |          </BlockStatement>
            |        </CatchStatement>
            |        <finally>
            |          <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |            <IfStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |              <BooleanExpression line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                <BinaryExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" token="!=">
            |                  <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="null"/>
            |                  <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="__$$primaryExc0"/>
            |                </BinaryExpression>
            |              </BooleanExpression>
            |              <TryCatchStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                  <ExpressionStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                    <MethodCallExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" safe="true">
            |                      <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="r1"/>
            |                      <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="close"/>
            |                      < line="-1" column="-1" lastLine="-1" lastColumn="-1"/>
            |                    </MethodCallExpression>
            |                  </ExpressionStatement>
            |                </BlockStatement>
            |                <CatchStatement line="-1" column="-1" lastLine="-1" lastColumn="-1" type="java.lang.Throwable" variable="__$$suppressedExc0">
            |                  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                    <ExpressionStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                      <MethodCallExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" safe="true">
            |                        <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="__$$primaryExc0"/>
            |                        <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="addSuppressed"/>
            |                        <TupleExpression line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                          <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="__$$suppressedExc0"/>
            |                        </TupleExpression>
            |                      </MethodCallExpression>
            |                    </ExpressionStatement>
            |                  </BlockStatement>
            |                </CatchStatement>
            |                <finally>
            |                  < line="-1" column="-1" lastLine="-1" lastColumn="-1"/>
            |                </finally>
            |              </TryCatchStatement>
            |              <else>
            |                <ExpressionStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                  <MethodCallExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" safe="true">
            |                    <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="r1"/>
            |                    <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="close"/>
            |                    < line="-1" column="-1" lastLine="-1" lastColumn="-1"/>
            |                  </MethodCallExpression>
            |                </ExpressionStatement>
            |              </else>
            |            </IfStatement>
            |          </BlockStatement>
            |        </finally>
            |      </TryCatchStatement>
            |    </BlockStatement>
            |    <BlockStatement line="4" column="1" lastLine="6" lastColumn="2">
            |      <ExpressionStatement line="4" column="6" lastLine="4" lastColumn="8">
            |        <DeclarationExpression line="4" column="6" lastLine="4" lastColumn="8" token="=">
            |          <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="__$$resource0"/>
            |          <VariableExpression line="4" column="6" lastLine="4" lastColumn="8" name="r1"/>
            |        </DeclarationExpression>
            |      </ExpressionStatement>
            |      <ExpressionStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |        <DeclarationExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" token="=">
            |          <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="__$$primaryExc1" type="java.lang.Throwable"/>
            |          <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="null"/>
            |        </DeclarationExpression>
            |      </ExpressionStatement>
            |      <TryCatchStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |        <BlockStatement line="4" column="10" lastLine="6" lastColumn="2">
            |          <ExpressionStatement line="5" column="5" lastLine="5" lastColumn="7">
            |            <VariableExpression line="5" column="5" lastLine="5" lastColumn="7" name="r1"/>
            |          </ExpressionStatement>
            |        </BlockStatement>
            |        <CatchStatement line="-1" column="-1" lastLine="-1" lastColumn="-1" type="java.lang.Throwable" variable="__$$t1">
            |          <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |            <ExpressionStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |              <BinaryExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" token="=">
            |                <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="__$$primaryExc1"/>
            |                <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="__$$t1"/>
            |              </BinaryExpression>
            |            </ExpressionStatement>
            |            <ThrowStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |              <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="__$$t1"/>
            |            </ThrowStatement>
            |          </BlockStatement>
            |        </CatchStatement>
            |        <finally>
            |          <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |            <IfStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |              <BooleanExpression line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                <BinaryExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" token="!=">
            |                  <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="null"/>
            |                  <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="__$$primaryExc1"/>
            |                </BinaryExpression>
            |              </BooleanExpression>
            |              <TryCatchStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                  <ExpressionStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                    <MethodCallExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" safe="true">
            |                      <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="__$$resource0"/>
            |                      <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="close"/>
            |                      < line="-1" column="-1" lastLine="-1" lastColumn="-1"/>
            |                    </MethodCallExpression>
            |                  </ExpressionStatement>
            |                </BlockStatement>
            |                <CatchStatement line="-1" column="-1" lastLine="-1" lastColumn="-1" type="java.lang.Throwable" variable="__$$suppressedExc1">
            |                  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                    <ExpressionStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                      <MethodCallExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" safe="true">
            |                        <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="__$$primaryExc1"/>
            |                        <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="addSuppressed"/>
            |                        <TupleExpression line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                          <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="__$$suppressedExc1"/>
            |                        </TupleExpression>
            |                      </MethodCallExpression>
            |                    </ExpressionStatement>
            |                  </BlockStatement>
            |                </CatchStatement>
            |                <finally>
            |                  < line="-1" column="-1" lastLine="-1" lastColumn="-1"/>
            |                </finally>
            |              </TryCatchStatement>
            |              <else>
            |                <ExpressionStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                  <MethodCallExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" safe="true">
            |                    <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="__$$resource0"/>
            |                    <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="close"/>
            |                    < line="-1" column="-1" lastLine="-1" lastColumn="-1"/>
            |                  </MethodCallExpression>
            |                </ExpressionStatement>
            |              </else>
            |            </IfStatement>
            |          </BlockStatement>
            |        </finally>
            |      </TryCatchStatement>
            |    </BlockStatement>
            |    <BlockStatement line="7" column="1" lastLine="9" lastColumn="2">
            |      <ExpressionStatement line="7" column="6" lastLine="7" lastColumn="35">
            |        <DeclarationExpression line="7" column="6" lastLine="7" lastColumn="35" token="=">
            |          <VariableExpression line="7" column="15" lastLine="7" lastColumn="17" name="r1" type="Resource"/>
            |          <ConstructorCallExpression line="7" column="20" lastLine="7" lastColumn="35" type="Resource">
            |            <ConstantExpression line="7" column="33" lastLine="7" lastColumn="34" value="1"/>
            |          </ConstructorCallExpression>
            |        </DeclarationExpression>
            |      </ExpressionStatement>
            |      <ExpressionStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |        <DeclarationExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" token="=">
            |          <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="__$$primaryExc2" type="java.lang.Throwable"/>
            |          <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="null"/>
            |        </DeclarationExpression>
            |      </ExpressionStatement>
            |      <TryCatchStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |        <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |          <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |            <ExpressionStatement line="7" column="37" lastLine="7" lastColumn="66">
            |              <DeclarationExpression line="7" column="37" lastLine="7" lastColumn="66" token="=">
            |                <VariableExpression line="7" column="46" lastLine="7" lastColumn="48" name="r2" type="Resource"/>
            |                <ConstructorCallExpression line="7" column="51" lastLine="7" lastColumn="66" type="Resource">
            |                  <ConstantExpression line="7" column="64" lastLine="7" lastColumn="65" value="2"/>
            |                </ConstructorCallExpression>
            |              </DeclarationExpression>
            |            </ExpressionStatement>
            |            <ExpressionStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |              <DeclarationExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" token="=">
            |                <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="__$$primaryExc3" type="java.lang.Throwable"/>
            |                <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="null"/>
            |              </DeclarationExpression>
            |            </ExpressionStatement>
            |            <TryCatchStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |              <BlockStatement line="7" column="68" lastLine="9" lastColumn="2">
            |                <ExpressionStatement line="8" column="5" lastLine="8" lastColumn="7">
            |                  <VariableExpression line="8" column="5" lastLine="8" lastColumn="7" name="r1"/>
            |                </ExpressionStatement>
            |              </BlockStatement>
            |              <CatchStatement line="-1" column="-1" lastLine="-1" lastColumn="-1" type="java.lang.Throwable" variable="__$$t3">
            |                <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                  <ExpressionStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                    <BinaryExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" token="=">
            |                      <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="__$$primaryExc3"/>
            |                      <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="__$$t3"/>
            |                    </BinaryExpression>
            |                  </ExpressionStatement>
            |                  <ThrowStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                    <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="__$$t3"/>
            |                  </ThrowStatement>
            |                </BlockStatement>
            |              </CatchStatement>
            |              <finally>
            |                <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                  <IfStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                    <BooleanExpression line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                      <BinaryExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" token="!=">
            |                        <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="null"/>
            |                        <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="__$$primaryExc3"/>
            |                      </BinaryExpression>
            |                    </BooleanExpression>
            |                    <TryCatchStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                      <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                        <ExpressionStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                          <MethodCallExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" safe="true">
            |                            <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="r2"/>
            |                            <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="close"/>
            |                            < line="-1" column="-1" lastLine="-1" lastColumn="-1"/>
            |                          </MethodCallExpression>
            |                        </ExpressionStatement>
            |                      </BlockStatement>
            |                      <CatchStatement line="-1" column="-1" lastLine="-1" lastColumn="-1" type="java.lang.Throwable" variable="__$$suppressedExc3">
            |                        <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                          <ExpressionStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                            <MethodCallExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" safe="true">
            |                              <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="__$$primaryExc3"/>
            |                              <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="addSuppressed"/>
            |                              <TupleExpression line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                                <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="__$$suppressedExc3"/>
            |                              </TupleExpression>
            |                            </MethodCallExpression>
            |                          </ExpressionStatement>
            |                        </BlockStatement>
            |                      </CatchStatement>
            |                      <finally>
            |                        < line="-1" column="-1" lastLine="-1" lastColumn="-1"/>
            |                      </finally>
            |                    </TryCatchStatement>
            |                    <else>
            |                      <ExpressionStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                        <MethodCallExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" safe="true">
            |                          <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="r2"/>
            |                          <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="close"/>
            |                          < line="-1" column="-1" lastLine="-1" lastColumn="-1"/>
            |                        </MethodCallExpression>
            |                      </ExpressionStatement>
            |                    </else>
            |                  </IfStatement>
            |                </BlockStatement>
            |              </finally>
            |            </TryCatchStatement>
            |          </BlockStatement>
            |        </BlockStatement>
            |        <CatchStatement line="-1" column="-1" lastLine="-1" lastColumn="-1" type="java.lang.Throwable" variable="__$$t2">
            |          <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |            <ExpressionStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |              <BinaryExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" token="=">
            |                <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="__$$primaryExc2"/>
            |                <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="__$$t2"/>
            |              </BinaryExpression>
            |            </ExpressionStatement>
            |            <ThrowStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |              <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="__$$t2"/>
            |            </ThrowStatement>
            |          </BlockStatement>
            |        </CatchStatement>
            |        <finally>
            |          <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |            <IfStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |              <BooleanExpression line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                <BinaryExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" token="!=">
            |                  <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="null"/>
            |                  <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="__$$primaryExc2"/>
            |                </BinaryExpression>
            |              </BooleanExpression>
            |              <TryCatchStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                  <ExpressionStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                    <MethodCallExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" safe="true">
            |                      <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="r1"/>
            |                      <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="close"/>
            |                      < line="-1" column="-1" lastLine="-1" lastColumn="-1"/>
            |                    </MethodCallExpression>
            |                  </ExpressionStatement>
            |                </BlockStatement>
            |                <CatchStatement line="-1" column="-1" lastLine="-1" lastColumn="-1" type="java.lang.Throwable" variable="__$$suppressedExc2">
            |                  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                    <ExpressionStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                      <MethodCallExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" safe="true">
            |                        <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="__$$primaryExc2"/>
            |                        <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="addSuppressed"/>
            |                        <TupleExpression line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                          <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="__$$suppressedExc2"/>
            |                        </TupleExpression>
            |                      </MethodCallExpression>
            |                    </ExpressionStatement>
            |                  </BlockStatement>
            |                </CatchStatement>
            |                <finally>
            |                  < line="-1" column="-1" lastLine="-1" lastColumn="-1"/>
            |                </finally>
            |              </TryCatchStatement>
            |              <else>
            |                <ExpressionStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |                  <MethodCallExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" safe="true">
            |                    <VariableExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" name="r1"/>
            |                    <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="close"/>
            |                    < line="-1" column="-1" lastLine="-1" lastColumn="-1"/>
            |                  </MethodCallExpression>
            |                </ExpressionStatement>
            |              </else>
            |            </IfStatement>
            |          </BlockStatement>
            |        </finally>
            |      </TryCatchStatement>
            |    </BlockStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'synchronized return throw assert'() {
        expectAst '''\
            |synchronized (lock) {
            |    assert true : 'ok'
            |    assert true, 'ok'
            |    throw e
            |    return 1
            |}
            |return
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <SynchronizedStatement line="1" column="1" lastLine="6" lastColumn="2">
            |      <VariableExpression line="1" column="15" lastLine="1" lastColumn="19" name="lock"/>
            |      <BlockStatement line="1" column="21" lastLine="6" lastColumn="2">
            |        <AssertStatement line="2" column="5" lastLine="2" lastColumn="23">
            |          <BooleanExpression line="2" column="12" lastLine="2" lastColumn="16">
            |            <ConstantExpression line="2" column="12" lastLine="2" lastColumn="16" value="true"/>
            |          </BooleanExpression>
            |          <ConstantExpression line="2" column="19" lastLine="2" lastColumn="23" value="ok"/>
            |        </AssertStatement>
            |        <AssertStatement line="3" column="5" lastLine="3" lastColumn="22">
            |          <BooleanExpression line="3" column="12" lastLine="3" lastColumn="16">
            |            <ConstantExpression line="3" column="12" lastLine="3" lastColumn="16" value="true"/>
            |          </BooleanExpression>
            |          <ConstantExpression line="3" column="18" lastLine="3" lastColumn="22" value="ok"/>
            |        </AssertStatement>
            |        <ThrowStatement line="4" column="5" lastLine="4" lastColumn="12">
            |          <VariableExpression line="4" column="11" lastLine="4" lastColumn="12" name="e"/>
            |        </ThrowStatement>
            |        <ReturnStatement line="5" column="5" lastLine="5" lastColumn="13">
            |          <ConstantExpression line="5" column="12" lastLine="5" lastColumn="13" value="1"/>
            |        </ReturnStatement>
            |      </BlockStatement>
            |    </SynchronizedStatement>
            |    <ReturnStatement line="7" column="1" lastLine="7" lastColumn="7">
            |      <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="null"/>
            |    </ReturnStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'labeled statement'() {
        expectAst '''\
            |a: assert true
            |outer:
            |for (def i in [1]) {
            |    break outer
            |}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <AssertStatement line="1" column="4" lastLine="1" lastColumn="15" label="a">
            |      <BooleanExpression line="1" column="11" lastLine="1" lastColumn="15">
            |        <ConstantExpression line="1" column="11" lastLine="1" lastColumn="15" value="true"/>
            |      </BooleanExpression>
            |    </AssertStatement>
            |    <ForStatement line="3" column="1" lastLine="5" lastColumn="2" label="outer">
            |      <Parameter line="3" column="10" lastLine="3" lastColumn="11" name="i"/>
            |      <ListExpression line="3" column="15" lastLine="3" lastColumn="18">
            |        <ConstantExpression line="3" column="16" lastLine="3" lastColumn="17" value="1"/>
            |      </ListExpression>
            |      <BlockStatement line="3" column="20" lastLine="5" lastColumn="2">
            |        <BreakStatement line="4" column="5" lastLine="4" lastColumn="16" label="outer"/>
            |      </BlockStatement>
            |    </ForStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'literals'() {
        expectAst '''\
            |null
            |true
            |false
            |1
            |1.2
            |1.2f
            |1.2D
            |1.2G
            |12e-10
            |0xabcdef
            |01234567
            |0b010101
            |1__2
            |2147483647I
            |9223372036854775807L
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ExpressionStatement line="1" column="1" lastLine="1" lastColumn="5">
            |      <ConstantExpression line="1" column="1" lastLine="1" lastColumn="5" value="null"/>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="2" column="1" lastLine="2" lastColumn="5">
            |      <ConstantExpression line="2" column="1" lastLine="2" lastColumn="5" value="true"/>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="3" column="1" lastLine="3" lastColumn="6">
            |      <ConstantExpression line="3" column="1" lastLine="3" lastColumn="6" value="false"/>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="4" column="1" lastLine="4" lastColumn="2">
            |      <ConstantExpression line="4" column="1" lastLine="4" lastColumn="2" value="1"/>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="5" column="1" lastLine="5" lastColumn="4">
            |      <ConstantExpression line="5" column="1" lastLine="5" lastColumn="4" value="1.2"/>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="6" column="1" lastLine="6" lastColumn="5">
            |      <ConstantExpression line="6" column="1" lastLine="6" lastColumn="5" value="1.2"/>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="7" column="1" lastLine="7" lastColumn="5">
            |      <ConstantExpression line="7" column="1" lastLine="7" lastColumn="5" value="1.2"/>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="8" column="1" lastLine="8" lastColumn="5">
            |      <ConstantExpression line="8" column="1" lastLine="8" lastColumn="5" value="1.2"/>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="9" column="1" lastLine="9" lastColumn="7">
            |      <ConstantExpression line="9" column="1" lastLine="9" lastColumn="7" value="1.2E-9"/>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="10" column="1" lastLine="10" lastColumn="9">
            |      <ConstantExpression line="10" column="1" lastLine="10" lastColumn="9" value="11259375"/>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="11" column="1" lastLine="11" lastColumn="9">
            |      <ConstantExpression line="11" column="1" lastLine="11" lastColumn="9" value="342391"/>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="12" column="1" lastLine="12" lastColumn="9">
            |      <ConstantExpression line="12" column="1" lastLine="12" lastColumn="9" value="21"/>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="13" column="1" lastLine="13" lastColumn="5">
            |      <ConstantExpression line="13" column="1" lastLine="13" lastColumn="5" value="12"/>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="14" column="1" lastLine="14" lastColumn="12">
            |      <ConstantExpression line="14" column="1" lastLine="14" lastColumn="12" value="2147483647"/>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="15" column="1" lastLine="15" lastColumn="21">
            |      <ConstantExpression line="15" column="1" lastLine="15" lastColumn="21" value="9223372036854775807"/>
            |    </ExpressionStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'string literals'() {
        expectAst '''\
            |'abc'
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ExpressionStatement line="1" column="1" lastLine="1" lastColumn="6">
            |      <ConstantExpression line="1" column="1" lastLine="1" lastColumn="6" value="abc"/>
            |    </ExpressionStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
        expectAst '''\
            |"abc"
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ExpressionStatement line="1" column="1" lastLine="1" lastColumn="6">
            |      <ConstantExpression line="1" column="1" lastLine="1" lastColumn="6" value="abc"/>
            |    </ExpressionStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
        expectAst '\'\'\'abc\'\'\'', '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ExpressionStatement line="1" column="1" lastLine="1" lastColumn="10">
            |      <ConstantExpression line="1" column="1" lastLine="1" lastColumn="10" value="abc"/>
            |    </ExpressionStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
        expectAst '''\
            |"""abc"""
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ExpressionStatement line="1" column="1" lastLine="1" lastColumn="10">
            |      <ConstantExpression line="1" column="1" lastLine="1" lastColumn="10" value="abc"/>
            |    </ExpressionStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
        expectAst '''\
            |/abc/
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ExpressionStatement line="1" column="1" lastLine="1" lastColumn="6">
            |      <ConstantExpression line="1" column="1" lastLine="1" lastColumn="6" value="abc"/>
            |    </ExpressionStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
        expectAst '''\
            |$/abc/$
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ExpressionStatement line="1" column="1" lastLine="1" lastColumn="8">
            |      <ConstantExpression line="1" column="1" lastLine="1" lastColumn="8" value="abc"/>
            |    </ExpressionStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'gstring'() {
        expectAst '''\
            |"a:$a"
            |"abc${'123'}def"
            |"${-> 12}"
            |"""$a.b.c"""
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ExpressionStatement line="1" column="1" lastLine="1" lastColumn="7">
            |      <GStringExpression line="1" column="1" lastLine="1" lastColumn="7" text="a:$a">
            |        <VariableExpression line="1" column="5" lastLine="1" lastColumn="6" name="a"/>
            |      </GStringExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="2" column="1" lastLine="2" lastColumn="17">
            |      <GStringExpression line="2" column="1" lastLine="2" lastColumn="17" text="abc${123}def">
            |        <ConstantExpression line="2" column="7" lastLine="2" lastColumn="12" value="123"/>
            |      </GStringExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="3" column="1" lastLine="3" lastColumn="11">
            |      <GStringExpression line="3" column="1" lastLine="3" lastColumn="11" text="${{  -&gt; ... }}">
            |        <ClosureExpression line="3" column="3" lastLine="3" lastColumn="10" implicitIt="true">
            |          <BlockStatement line="3" column="7" lastLine="3" lastColumn="9">
            |            <ExpressionStatement line="3" column="7" lastLine="3" lastColumn="9">
            |              <ConstantExpression line="3" column="7" lastLine="3" lastColumn="9" value="12"/>
            |            </ExpressionStatement>
            |          </BlockStatement>
            |        </ClosureExpression>
            |      </GStringExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="4" column="1" lastLine="4" lastColumn="13">
            |      <GStringExpression line="4" column="1" lastLine="4" lastColumn="13" text="${a.b.c}">
            |        <PropertyExpression line="4" column="5" lastLine="4" lastColumn="10">
            |          <PropertyExpression line="4" column="6" lastLine="4" lastColumn="8">
            |            <VariableExpression line="4" column="5" lastLine="4" lastColumn="6" name="a"/>
            |            <ConstantExpression line="4" column="6" lastLine="4" lastColumn="8" value="b"/>
            |          </PropertyExpression>
            |          <ConstantExpression line="4" column="8" lastLine="4" lastColumn="10" value="c"/>
            |        </PropertyExpression>
            |      </GStringExpression>
            |    </ExpressionStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'list and map'() {
        expectAst '''\
            |[]
            |[1, 2, 3,]
            |[1, *[2, 3], 4]
            |[:]
            |[a: 1, b: 2]
            |[(k): v, *: other]
            |[private: 1, class: 2]
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ExpressionStatement line="1" column="1" lastLine="1" lastColumn="3">
            |      <ListExpression line="1" column="1" lastLine="1" lastColumn="3"/>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="2" column="1" lastLine="2" lastColumn="11">
            |      <ListExpression line="2" column="1" lastLine="2" lastColumn="11">
            |        <ConstantExpression line="2" column="2" lastLine="2" lastColumn="3" value="1"/>
            |        <ConstantExpression line="2" column="5" lastLine="2" lastColumn="6" value="2"/>
            |        <ConstantExpression line="2" column="8" lastLine="2" lastColumn="9" value="3"/>
            |      </ListExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="3" column="1" lastLine="3" lastColumn="16">
            |      <ListExpression line="3" column="1" lastLine="3" lastColumn="16">
            |        <ConstantExpression line="3" column="2" lastLine="3" lastColumn="3" value="1"/>
            |        <SpreadExpression line="3" column="5" lastLine="3" lastColumn="12">
            |          <ListExpression line="3" column="6" lastLine="3" lastColumn="12">
            |            <ConstantExpression line="3" column="7" lastLine="3" lastColumn="8" value="2"/>
            |            <ConstantExpression line="3" column="10" lastLine="3" lastColumn="11" value="3"/>
            |          </ListExpression>
            |        </SpreadExpression>
            |        <ConstantExpression line="3" column="14" lastLine="3" lastColumn="15" value="4"/>
            |      </ListExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="4" column="1" lastLine="4" lastColumn="4">
            |      <MapExpression line="4" column="1" lastLine="4" lastColumn="4"/>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="5" column="1" lastLine="5" lastColumn="13">
            |      <MapExpression line="5" column="1" lastLine="5" lastColumn="13">
            |        <MapEntryExpression line="5" column="2" lastLine="5" lastColumn="6">
            |          <ConstantExpression line="5" column="2" lastLine="5" lastColumn="3" value="a"/>
            |          <ConstantExpression line="5" column="5" lastLine="5" lastColumn="6" value="1"/>
            |        </MapEntryExpression>
            |        <MapEntryExpression line="5" column="8" lastLine="5" lastColumn="12">
            |          <ConstantExpression line="5" column="8" lastLine="5" lastColumn="9" value="b"/>
            |          <ConstantExpression line="5" column="11" lastLine="5" lastColumn="12" value="2"/>
            |        </MapEntryExpression>
            |      </MapExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="6" column="1" lastLine="6" lastColumn="19">
            |      <MapExpression line="6" column="1" lastLine="6" lastColumn="19">
            |        <MapEntryExpression line="6" column="2" lastLine="6" lastColumn="8">
            |          <VariableExpression line="6" column="2" lastLine="6" lastColumn="5" name="k"/>
            |          <VariableExpression line="6" column="7" lastLine="6" lastColumn="8" name="v"/>
            |        </MapEntryExpression>
            |        <MapEntryExpression line="6" column="10" lastLine="6" lastColumn="18">
            |          <SpreadMapExpression line="6" column="10" lastLine="6" lastColumn="18">
            |            <VariableExpression line="6" column="13" lastLine="6" lastColumn="18" name="other"/>
            |          </SpreadMapExpression>
            |          <VariableExpression line="6" column="13" lastLine="6" lastColumn="18" name="other"/>
            |        </MapEntryExpression>
            |      </MapExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="7" column="1" lastLine="7" lastColumn="23">
            |      <MapExpression line="7" column="1" lastLine="7" lastColumn="23">
            |        <MapEntryExpression line="7" column="2" lastLine="7" lastColumn="12">
            |          <ConstantExpression line="7" column="2" lastLine="7" lastColumn="9" value="private"/>
            |          <ConstantExpression line="7" column="11" lastLine="7" lastColumn="12" value="1"/>
            |        </MapEntryExpression>
            |        <MapEntryExpression line="7" column="14" lastLine="7" lastColumn="22">
            |          <ConstantExpression line="7" column="14" lastLine="7" lastColumn="19" value="class"/>
            |          <ConstantExpression line="7" column="21" lastLine="7" lastColumn="22" value="2"/>
            |        </MapEntryExpression>
            |      </MapExpression>
            |    </ExpressionStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'closure'() {
        expectAst '''\
            |{ -> }
            |{ it }
            |{ int a -> a }
            |{ a, b = 1 -> a + b }
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ExpressionStatement line="1" column="1" lastLine="4" lastColumn="22">
            |      <MethodCallExpression line="1" column="1" lastLine="4" lastColumn="22">
            |        <ClosureExpression line="1" column="1" lastLine="1" lastColumn="7" implicitIt="true">
            |          <BlockStatement line="1" column="6" lastLine="1" lastColumn="5"/>
            |        </ClosureExpression>
            |        <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="call"/>
            |        <ArgumentListExpression line="2" column="1" lastLine="2" lastColumn="7">
            |          <ClosureExpression line="2" column="1" lastLine="2" lastColumn="7">
            |            <BlockStatement line="2" column="3" lastLine="2" lastColumn="5">
            |              <ExpressionStatement line="2" column="3" lastLine="2" lastColumn="5">
            |                <VariableExpression line="2" column="3" lastLine="2" lastColumn="5" name="it"/>
            |              </ExpressionStatement>
            |            </BlockStatement>
            |          </ClosureExpression>
            |          <ClosureExpression line="3" column="1" lastLine="3" lastColumn="15" parameters="a">
            |            <BlockStatement line="3" column="12" lastLine="3" lastColumn="13">
            |              <ExpressionStatement line="3" column="12" lastLine="3" lastColumn="13">
            |                <VariableExpression line="3" column="12" lastLine="3" lastColumn="13" name="a"/>
            |              </ExpressionStatement>
            |            </BlockStatement>
            |          </ClosureExpression>
            |          <ClosureExpression line="4" column="1" lastLine="4" lastColumn="22" parameters="a,b">
            |            <BlockStatement line="4" column="15" lastLine="4" lastColumn="20">
            |              <ExpressionStatement line="4" column="15" lastLine="4" lastColumn="20">
            |                <BinaryExpression line="4" column="15" lastLine="4" lastColumn="20" token="+">
            |                  <VariableExpression line="4" column="15" lastLine="4" lastColumn="16" name="a"/>
            |                  <VariableExpression line="4" column="19" lastLine="4" lastColumn="20" name="b"/>
            |                </BinaryExpression>
            |              </ExpressionStatement>
            |            </BlockStatement>
            |          </ClosureExpression>
            |        </ArgumentListExpression>
            |      </MethodCallExpression>
            |    </ExpressionStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'lambda'() {
        expectAst '''\
            |def a = e -> e + 1
            |def b = (e) -> e + 1
            |def c = (int e, int f) -> e + f
            |def d = () -> 2
            |def e = (x, y) -> { x + y }
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ExpressionStatement line="1" column="1" lastLine="1" lastColumn="19">
            |      <DeclarationExpression line="1" column="1" lastLine="1" lastColumn="19" token="=">
            |        <VariableExpression line="1" column="5" lastLine="1" lastColumn="6" name="a"/>
            |        <LambdaExpression line="1" column="9" lastLine="1" lastColumn="19" parameters="e">
            |          <ExpressionStatement line="1" column="14" lastLine="1" lastColumn="19">
            |            <BinaryExpression line="1" column="14" lastLine="1" lastColumn="19" token="+">
            |              <VariableExpression line="1" column="14" lastLine="1" lastColumn="15" name="e"/>
            |              <ConstantExpression line="1" column="18" lastLine="1" lastColumn="19" value="1"/>
            |            </BinaryExpression>
            |          </ExpressionStatement>
            |        </LambdaExpression>
            |      </DeclarationExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="2" column="1" lastLine="2" lastColumn="21">
            |      <DeclarationExpression line="2" column="1" lastLine="2" lastColumn="21" token="=">
            |        <VariableExpression line="2" column="5" lastLine="2" lastColumn="6" name="b"/>
            |        <LambdaExpression line="2" column="9" lastLine="2" lastColumn="21" parameters="e">
            |          <ExpressionStatement line="2" column="16" lastLine="2" lastColumn="21">
            |            <BinaryExpression line="2" column="16" lastLine="2" lastColumn="21" token="+">
            |              <VariableExpression line="2" column="16" lastLine="2" lastColumn="17" name="e"/>
            |              <ConstantExpression line="2" column="20" lastLine="2" lastColumn="21" value="1"/>
            |            </BinaryExpression>
            |          </ExpressionStatement>
            |        </LambdaExpression>
            |      </DeclarationExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="3" column="1" lastLine="3" lastColumn="32">
            |      <DeclarationExpression line="3" column="1" lastLine="3" lastColumn="32" token="=">
            |        <VariableExpression line="3" column="5" lastLine="3" lastColumn="6" name="c"/>
            |        <LambdaExpression line="3" column="9" lastLine="3" lastColumn="32" parameters="e,f">
            |          <ExpressionStatement line="3" column="27" lastLine="3" lastColumn="32">
            |            <BinaryExpression line="3" column="27" lastLine="3" lastColumn="32" token="+">
            |              <VariableExpression line="3" column="27" lastLine="3" lastColumn="28" name="e"/>
            |              <VariableExpression line="3" column="31" lastLine="3" lastColumn="32" name="f"/>
            |            </BinaryExpression>
            |          </ExpressionStatement>
            |        </LambdaExpression>
            |      </DeclarationExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="4" column="1" lastLine="4" lastColumn="16">
            |      <DeclarationExpression line="4" column="1" lastLine="4" lastColumn="16" token="=">
            |        <VariableExpression line="4" column="5" lastLine="4" lastColumn="6" name="d"/>
            |        <LambdaExpression line="4" column="9" lastLine="4" lastColumn="16" implicitIt="true">
            |          <ExpressionStatement line="4" column="15" lastLine="4" lastColumn="16">
            |            <ConstantExpression line="4" column="15" lastLine="4" lastColumn="16" value="2"/>
            |          </ExpressionStatement>
            |        </LambdaExpression>
            |      </DeclarationExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="5" column="1" lastLine="5" lastColumn="28">
            |      <DeclarationExpression line="5" column="1" lastLine="5" lastColumn="28" token="=">
            |        <VariableExpression line="5" column="5" lastLine="5" lastColumn="6" name="e"/>
            |        <LambdaExpression line="5" column="9" lastLine="5" lastColumn="28" parameters="x,y">
            |          <BlockStatement line="5" column="19" lastLine="5" lastColumn="28">
            |            <ExpressionStatement line="5" column="21" lastLine="5" lastColumn="26">
            |              <BinaryExpression line="5" column="21" lastLine="5" lastColumn="26" token="+">
            |                <VariableExpression line="5" column="21" lastLine="5" lastColumn="22" name="x"/>
            |                <VariableExpression line="5" column="25" lastLine="5" lastColumn="26" name="y"/>
            |              </BinaryExpression>
            |            </ExpressionStatement>
            |          </BlockStatement>
            |        </LambdaExpression>
            |      </DeclarationExpression>
            |    </ExpressionStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'method pointer and method reference'() {
        expectAst '''\
            |String.&toUpperCase
            |'abc'.&toUpperCase
            |Integer::toString
            |String::toUpperCase
            |obj::greet
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ExpressionStatement line="1" column="1" lastLine="1" lastColumn="20">
            |      <MethodPointerExpression line="1" column="1" lastLine="1" lastColumn="20">
            |        <VariableExpression line="1" column="1" lastLine="1" lastColumn="7" name="String"/>
            |        <ConstantExpression line="1" column="9" lastLine="1" lastColumn="20" value="toUpperCase"/>
            |      </MethodPointerExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="2" column="1" lastLine="2" lastColumn="19">
            |      <MethodPointerExpression line="2" column="1" lastLine="2" lastColumn="19">
            |        <ConstantExpression line="2" column="1" lastLine="2" lastColumn="6" value="abc"/>
            |        <ConstantExpression line="2" column="8" lastLine="2" lastColumn="19" value="toUpperCase"/>
            |      </MethodPointerExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="3" column="1" lastLine="3" lastColumn="18">
            |      <MethodReferenceExpression line="3" column="1" lastLine="3" lastColumn="18">
            |        <VariableExpression line="3" column="1" lastLine="3" lastColumn="8" name="Integer"/>
            |        <ConstantExpression line="3" column="10" lastLine="3" lastColumn="18" value="toString"/>
            |      </MethodReferenceExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="4" column="1" lastLine="4" lastColumn="20">
            |      <MethodReferenceExpression line="4" column="1" lastLine="4" lastColumn="20">
            |        <VariableExpression line="4" column="1" lastLine="4" lastColumn="7" name="String"/>
            |        <ConstantExpression line="4" column="9" lastLine="4" lastColumn="20" value="toUpperCase"/>
            |      </MethodReferenceExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="5" column="1" lastLine="5" lastColumn="11">
            |      <MethodReferenceExpression line="5" column="1" lastLine="5" lastColumn="11">
            |        <VariableExpression line="5" column="1" lastLine="5" lastColumn="4" name="obj"/>
            |        <ConstantExpression line="5" column="6" lastLine="5" lastColumn="11" value="greet"/>
            |      </MethodReferenceExpression>
            |    </ExpressionStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'new expression and array initializer'() {
        expectAst '''\
            |new String('x')
            |new int[5]
            |new int[5][6][]
            |new int[] {1, 2}
            |new int[][] { new int[] {1, 2}, new int[] {3, 4} }
            |new double[] {}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ExpressionStatement line="1" column="1" lastLine="1" lastColumn="16">
            |      <ConstructorCallExpression line="1" column="1" lastLine="1" lastColumn="16" type="String">
            |        <ConstantExpression line="1" column="12" lastLine="1" lastColumn="15" value="x"/>
            |      </ConstructorCallExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="2" column="1" lastLine="2" lastColumn="11">
            |      <ArrayExpression line="2" column="1" lastLine="2" lastColumn="11" elementType="int">
            |        <ConstantExpression line="2" column="9" lastLine="2" lastColumn="10" value="5"/>
            |      </ArrayExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="3" column="1" lastLine="3" lastColumn="16">
            |      <ArrayExpression line="3" column="1" lastLine="3" lastColumn="16" elementType="int">
            |        <ConstantExpression line="3" column="9" lastLine="3" lastColumn="10" value="5"/>
            |        <ConstantExpression line="3" column="12" lastLine="3" lastColumn="13" value="6"/>
            |        <ConstantExpression line="-1" column="-1" lastLine="-1" lastColumn="-1" value="null"/>
            |      </ArrayExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="4" column="1" lastLine="4" lastColumn="17">
            |      <ArrayExpression line="4" column="1" lastLine="4" lastColumn="17" elementType="int">
            |        <ConstantExpression line="4" column="12" lastLine="4" lastColumn="13" value="1"/>
            |        <ConstantExpression line="4" column="15" lastLine="4" lastColumn="16" value="2"/>
            |      </ArrayExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="5" column="1" lastLine="5" lastColumn="51">
            |      <ArrayExpression line="5" column="1" lastLine="5" lastColumn="51" elementType="int[]">
            |        <ArrayExpression line="5" column="15" lastLine="5" lastColumn="31" elementType="int">
            |          <ConstantExpression line="5" column="26" lastLine="5" lastColumn="27" value="1"/>
            |          <ConstantExpression line="5" column="29" lastLine="5" lastColumn="30" value="2"/>
            |        </ArrayExpression>
            |        <ArrayExpression line="5" column="33" lastLine="5" lastColumn="49" elementType="int">
            |          <ConstantExpression line="5" column="44" lastLine="5" lastColumn="45" value="3"/>
            |          <ConstantExpression line="5" column="47" lastLine="5" lastColumn="48" value="4"/>
            |        </ArrayExpression>
            |      </ArrayExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="6" column="1" lastLine="6" lastColumn="16">
            |      <ArrayExpression line="6" column="1" lastLine="6" lastColumn="16" elementType="double"/>
            |    </ExpressionStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'this super and parentheses'() {
        expectAst '''\
            |this
            |super
            |(1 + 2)
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ExpressionStatement line="1" column="1" lastLine="1" lastColumn="5">
            |      <VariableExpression line="1" column="1" lastLine="1" lastColumn="5" name="this"/>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="2" column="1" lastLine="2" lastColumn="6">
            |      <VariableExpression line="2" column="1" lastLine="2" lastColumn="6" name="super"/>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="3" column="1" lastLine="3" lastColumn="8">
            |      <BinaryExpression line="3" column="1" lastLine="3" lastColumn="8" token="+">
            |        <ConstantExpression line="3" column="2" lastLine="3" lastColumn="3" value="1"/>
            |        <ConstantExpression line="3" column="6" lastLine="3" lastColumn="7" value="2"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'cast as and instanceof'() {
        expectAst '''\
            |(int) x
            |(List & Serializable) x
            |x as String
            |x instanceof String
            |x !instanceof Integer
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ExpressionStatement line="1" column="1" lastLine="1" lastColumn="8">
            |      <CastExpression line="1" column="1" lastLine="1" lastColumn="8" kind="cast" type="int">
            |        <VariableExpression line="1" column="7" lastLine="1" lastColumn="8" name="x"/>
            |      </CastExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="2" column="1" lastLine="2" lastColumn="24">
            |      <CastExpression line="2" column="1" lastLine="2" lastColumn="24" kind="cast" type="List &amp; Serializable">
            |        <VariableExpression line="2" column="23" lastLine="2" lastColumn="24" name="x"/>
            |      </CastExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="3" column="1" lastLine="3" lastColumn="12">
            |      <CastExpression line="3" column="1" lastLine="3" lastColumn="12" kind="as" type="String">
            |        <VariableExpression line="3" column="1" lastLine="3" lastColumn="2" name="x"/>
            |      </CastExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="4" column="1" lastLine="4" lastColumn="20">
            |      <BinaryExpression line="4" column="1" lastLine="4" lastColumn="20" token="instanceof">
            |        <VariableExpression line="4" column="1" lastLine="4" lastColumn="2" name="x"/>
            |        <ClassExpression line="4" column="14" lastLine="4" lastColumn="20" type="String"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="5" column="1" lastLine="5" lastColumn="22">
            |      <BinaryExpression line="5" column="1" lastLine="5" lastColumn="22" token="!instanceof">
            |        <VariableExpression line="5" column="1" lastLine="5" lastColumn="2" name="x"/>
            |        <ClassExpression line="5" column="15" lastLine="5" lastColumn="22" type="Integer"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'unary and arithmetic operators'() {
        expectAst '''\
            |+a
            |-a
            |~a
            |!a
            |++a
            |--a
            |a++
            |a--
            |a ** b
            |a * b / c % d
            |a + b - c
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ExpressionStatement line="1" column="1" lastLine="1" lastColumn="3">
            |      <UnaryPlusExpression line="1" column="1" lastLine="1" lastColumn="3">
            |        <VariableExpression line="1" column="2" lastLine="1" lastColumn="3" name="a"/>
            |      </UnaryPlusExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="2" column="1" lastLine="2" lastColumn="3">
            |      <UnaryMinusExpression line="2" column="1" lastLine="2" lastColumn="3">
            |        <VariableExpression line="2" column="2" lastLine="2" lastColumn="3" name="a"/>
            |      </UnaryMinusExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="3" column="1" lastLine="3" lastColumn="3">
            |      <BitwiseNegationExpression line="3" column="1" lastLine="3" lastColumn="3">
            |        <VariableExpression line="3" column="2" lastLine="3" lastColumn="3" name="a"/>
            |      </BitwiseNegationExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="4" column="1" lastLine="4" lastColumn="3">
            |      <NotExpression line="4" column="1" lastLine="4" lastColumn="3">
            |        <VariableExpression line="4" column="2" lastLine="4" lastColumn="3" name="a"/>
            |      </NotExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="5" column="1" lastLine="5" lastColumn="4">
            |      <PrefixExpression line="5" column="1" lastLine="5" lastColumn="4" token="++">
            |        <VariableExpression line="5" column="3" lastLine="5" lastColumn="4" name="a"/>
            |      </PrefixExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="6" column="1" lastLine="6" lastColumn="4">
            |      <PrefixExpression line="6" column="1" lastLine="6" lastColumn="4" token="--">
            |        <VariableExpression line="6" column="3" lastLine="6" lastColumn="4" name="a"/>
            |      </PrefixExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="7" column="1" lastLine="7" lastColumn="4">
            |      <PostfixExpression line="7" column="1" lastLine="7" lastColumn="4" token="++">
            |        <VariableExpression line="7" column="1" lastLine="7" lastColumn="2" name="a"/>
            |      </PostfixExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="8" column="1" lastLine="8" lastColumn="4">
            |      <PostfixExpression line="8" column="1" lastLine="8" lastColumn="4" token="--">
            |        <VariableExpression line="8" column="1" lastLine="8" lastColumn="2" name="a"/>
            |      </PostfixExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="9" column="1" lastLine="9" lastColumn="7">
            |      <BinaryExpression line="9" column="1" lastLine="9" lastColumn="7" token="**">
            |        <VariableExpression line="9" column="1" lastLine="9" lastColumn="2" name="a"/>
            |        <VariableExpression line="9" column="6" lastLine="9" lastColumn="7" name="b"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="10" column="1" lastLine="10" lastColumn="14">
            |      <BinaryExpression line="10" column="1" lastLine="10" lastColumn="14" token="%">
            |        <BinaryExpression line="10" column="1" lastLine="10" lastColumn="10" token="/">
            |          <BinaryExpression line="10" column="1" lastLine="10" lastColumn="6" token="*">
            |            <VariableExpression line="10" column="1" lastLine="10" lastColumn="2" name="a"/>
            |            <VariableExpression line="10" column="5" lastLine="10" lastColumn="6" name="b"/>
            |          </BinaryExpression>
            |          <VariableExpression line="10" column="9" lastLine="10" lastColumn="10" name="c"/>
            |        </BinaryExpression>
            |        <VariableExpression line="10" column="13" lastLine="10" lastColumn="14" name="d"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="11" column="1" lastLine="11" lastColumn="10">
            |      <BinaryExpression line="11" column="1" lastLine="11" lastColumn="10" token="-">
            |        <BinaryExpression line="11" column="1" lastLine="11" lastColumn="6" token="+">
            |          <VariableExpression line="11" column="1" lastLine="11" lastColumn="2" name="a"/>
            |          <VariableExpression line="11" column="5" lastLine="11" lastColumn="6" name="b"/>
            |        </BinaryExpression>
            |        <VariableExpression line="11" column="9" lastLine="11" lastColumn="10" name="c"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'shift range and relational operators'() {
        expectAst '''\
            |a << 1
            |a >> 1
            |a >>> 1
            |1..10
            |1..<10
            |1<..10
            |1<..<10
            |a < b
            |a <= b
            |a > b
            |a >= b
            |a in list
            |a !in list
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ExpressionStatement line="1" column="1" lastLine="1" lastColumn="7">
            |      <BinaryExpression line="1" column="1" lastLine="1" lastColumn="7" token="&lt;&lt;">
            |        <VariableExpression line="1" column="1" lastLine="1" lastColumn="2" name="a"/>
            |        <ConstantExpression line="1" column="6" lastLine="1" lastColumn="7" value="1"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="2" column="1" lastLine="2" lastColumn="7">
            |      <BinaryExpression line="2" column="1" lastLine="2" lastColumn="7" token="&gt;&gt;">
            |        <VariableExpression line="2" column="1" lastLine="2" lastColumn="2" name="a"/>
            |        <ConstantExpression line="2" column="6" lastLine="2" lastColumn="7" value="1"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="3" column="1" lastLine="3" lastColumn="8">
            |      <BinaryExpression line="3" column="1" lastLine="3" lastColumn="8" token="&gt;&gt;&gt;">
            |        <VariableExpression line="3" column="1" lastLine="3" lastColumn="2" name="a"/>
            |        <ConstantExpression line="3" column="7" lastLine="3" lastColumn="8" value="1"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="4" column="1" lastLine="4" lastColumn="6">
            |      <RangeExpression line="4" column="1" lastLine="4" lastColumn="6" op="..">
            |        <ConstantExpression line="4" column="1" lastLine="4" lastColumn="2" value="1"/>
            |        <ConstantExpression line="4" column="4" lastLine="4" lastColumn="6" value="10"/>
            |      </RangeExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="5" column="1" lastLine="5" lastColumn="7">
            |      <RangeExpression line="5" column="1" lastLine="5" lastColumn="7" op="..&lt;">
            |        <ConstantExpression line="5" column="1" lastLine="5" lastColumn="2" value="1"/>
            |        <ConstantExpression line="5" column="5" lastLine="5" lastColumn="7" value="10"/>
            |      </RangeExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="6" column="1" lastLine="6" lastColumn="7">
            |      <RangeExpression line="6" column="1" lastLine="6" lastColumn="7" op="&lt;..">
            |        <ConstantExpression line="6" column="1" lastLine="6" lastColumn="2" value="1"/>
            |        <ConstantExpression line="6" column="5" lastLine="6" lastColumn="7" value="10"/>
            |      </RangeExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="7" column="1" lastLine="7" lastColumn="8">
            |      <RangeExpression line="7" column="1" lastLine="7" lastColumn="8" op="&lt;..&lt;">
            |        <ConstantExpression line="7" column="1" lastLine="7" lastColumn="2" value="1"/>
            |        <ConstantExpression line="7" column="6" lastLine="7" lastColumn="8" value="10"/>
            |      </RangeExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="8" column="1" lastLine="8" lastColumn="6">
            |      <BinaryExpression line="8" column="1" lastLine="8" lastColumn="6" token="&lt;">
            |        <VariableExpression line="8" column="1" lastLine="8" lastColumn="2" name="a"/>
            |        <VariableExpression line="8" column="5" lastLine="8" lastColumn="6" name="b"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="9" column="1" lastLine="9" lastColumn="7">
            |      <BinaryExpression line="9" column="1" lastLine="9" lastColumn="7" token="&lt;=">
            |        <VariableExpression line="9" column="1" lastLine="9" lastColumn="2" name="a"/>
            |        <VariableExpression line="9" column="6" lastLine="9" lastColumn="7" name="b"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="10" column="1" lastLine="10" lastColumn="6">
            |      <BinaryExpression line="10" column="1" lastLine="10" lastColumn="6" token="&gt;">
            |        <VariableExpression line="10" column="1" lastLine="10" lastColumn="2" name="a"/>
            |        <VariableExpression line="10" column="5" lastLine="10" lastColumn="6" name="b"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="11" column="1" lastLine="11" lastColumn="7">
            |      <BinaryExpression line="11" column="1" lastLine="11" lastColumn="7" token="&gt;=">
            |        <VariableExpression line="11" column="1" lastLine="11" lastColumn="2" name="a"/>
            |        <VariableExpression line="11" column="6" lastLine="11" lastColumn="7" name="b"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="12" column="1" lastLine="12" lastColumn="10">
            |      <BinaryExpression line="12" column="1" lastLine="12" lastColumn="10" token="in">
            |        <VariableExpression line="12" column="1" lastLine="12" lastColumn="2" name="a"/>
            |        <VariableExpression line="12" column="6" lastLine="12" lastColumn="10" name="list"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="13" column="1" lastLine="13" lastColumn="11">
            |      <BinaryExpression line="13" column="1" lastLine="13" lastColumn="11" token="!in">
            |        <VariableExpression line="13" column="1" lastLine="13" lastColumn="2" name="a"/>
            |        <VariableExpression line="13" column="7" lastLine="13" lastColumn="11" name="list"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'equality regex and bitwise operators'() {
        expectAst '''\
            |a == b
            |a != b
            |a === b
            |a !== b
            |a <=> b
            |a =~ /x/
            |a ==~ /x/
            |a & b
            |a ^ b
            |a | b
            |a && b
            |a || b
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ExpressionStatement line="1" column="1" lastLine="1" lastColumn="7">
            |      <BinaryExpression line="1" column="1" lastLine="1" lastColumn="7" token="==">
            |        <VariableExpression line="1" column="1" lastLine="1" lastColumn="2" name="a"/>
            |        <VariableExpression line="1" column="6" lastLine="1" lastColumn="7" name="b"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="2" column="1" lastLine="2" lastColumn="7">
            |      <BinaryExpression line="2" column="1" lastLine="2" lastColumn="7" token="!=">
            |        <VariableExpression line="2" column="1" lastLine="2" lastColumn="2" name="a"/>
            |        <VariableExpression line="2" column="6" lastLine="2" lastColumn="7" name="b"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="3" column="1" lastLine="3" lastColumn="8">
            |      <BinaryExpression line="3" column="1" lastLine="3" lastColumn="8" token="===">
            |        <VariableExpression line="3" column="1" lastLine="3" lastColumn="2" name="a"/>
            |        <VariableExpression line="3" column="7" lastLine="3" lastColumn="8" name="b"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="4" column="1" lastLine="4" lastColumn="8">
            |      <BinaryExpression line="4" column="1" lastLine="4" lastColumn="8" token="!==">
            |        <VariableExpression line="4" column="1" lastLine="4" lastColumn="2" name="a"/>
            |        <VariableExpression line="4" column="7" lastLine="4" lastColumn="8" name="b"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="5" column="1" lastLine="5" lastColumn="8">
            |      <BinaryExpression line="5" column="1" lastLine="5" lastColumn="8" token="&lt;=&gt;">
            |        <VariableExpression line="5" column="1" lastLine="5" lastColumn="2" name="a"/>
            |        <VariableExpression line="5" column="7" lastLine="5" lastColumn="8" name="b"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="6" column="1" lastLine="6" lastColumn="9">
            |      <BinaryExpression line="6" column="1" lastLine="6" lastColumn="9" token="=~">
            |        <VariableExpression line="6" column="1" lastLine="6" lastColumn="2" name="a"/>
            |        <ConstantExpression line="6" column="6" lastLine="6" lastColumn="9" value="x"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="7" column="1" lastLine="7" lastColumn="10">
            |      <BinaryExpression line="7" column="1" lastLine="7" lastColumn="10" token="==~">
            |        <VariableExpression line="7" column="1" lastLine="7" lastColumn="2" name="a"/>
            |        <ConstantExpression line="7" column="7" lastLine="7" lastColumn="10" value="x"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="8" column="1" lastLine="8" lastColumn="6">
            |      <BinaryExpression line="8" column="1" lastLine="8" lastColumn="6" token="&amp;">
            |        <VariableExpression line="8" column="1" lastLine="8" lastColumn="2" name="a"/>
            |        <VariableExpression line="8" column="5" lastLine="8" lastColumn="6" name="b"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="9" column="1" lastLine="9" lastColumn="6">
            |      <BinaryExpression line="9" column="1" lastLine="9" lastColumn="6" token="^">
            |        <VariableExpression line="9" column="1" lastLine="9" lastColumn="2" name="a"/>
            |        <VariableExpression line="9" column="5" lastLine="9" lastColumn="6" name="b"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="10" column="1" lastLine="10" lastColumn="6">
            |      <BinaryExpression line="10" column="1" lastLine="10" lastColumn="6" token="|">
            |        <VariableExpression line="10" column="1" lastLine="10" lastColumn="2" name="a"/>
            |        <VariableExpression line="10" column="5" lastLine="10" lastColumn="6" name="b"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="11" column="1" lastLine="11" lastColumn="7">
            |      <BinaryExpression line="11" column="1" lastLine="11" lastColumn="7" token="&amp;&amp;">
            |        <VariableExpression line="11" column="1" lastLine="11" lastColumn="2" name="a"/>
            |        <VariableExpression line="11" column="6" lastLine="11" lastColumn="7" name="b"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="12" column="1" lastLine="12" lastColumn="7">
            |      <BinaryExpression line="12" column="1" lastLine="12" lastColumn="7" token="||">
            |        <VariableExpression line="12" column="1" lastLine="12" lastColumn="2" name="a"/>
            |        <VariableExpression line="12" column="6" lastLine="12" lastColumn="7" name="b"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'conditional and assignment operators'() {
        expectAst '''\
            |a ? b : c
            |a ?: b
            |a = 1
            |a += 1
            |a -= 1
            |a *= 1
            |a /= 1
            |a %= 1
            |a **= 2
            |a ?= 1
            |(x, y) = [1, 2]
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ExpressionStatement line="1" column="1" lastLine="1" lastColumn="10">
            |      <TernaryExpression line="1" column="1" lastLine="1" lastColumn="10">
            |        <BooleanExpression line="1" column="1" lastLine="1" lastColumn="2">
            |          <VariableExpression line="1" column="1" lastLine="1" lastColumn="2" name="a"/>
            |        </BooleanExpression>
            |        <VariableExpression line="1" column="5" lastLine="1" lastColumn="6" name="b"/>
            |        <VariableExpression line="1" column="9" lastLine="1" lastColumn="10" name="c"/>
            |      </TernaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="2" column="1" lastLine="2" lastColumn="7">
            |      <ElvisOperatorExpression line="2" column="1" lastLine="2" lastColumn="7">
            |        <VariableExpression line="2" column="1" lastLine="2" lastColumn="2" name="a"/>
            |        <VariableExpression line="2" column="6" lastLine="2" lastColumn="7" name="b"/>
            |      </ElvisOperatorExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="3" column="1" lastLine="3" lastColumn="6">
            |      <BinaryExpression line="3" column="1" lastLine="3" lastColumn="6" token="=">
            |        <VariableExpression line="3" column="1" lastLine="3" lastColumn="2" name="a"/>
            |        <ConstantExpression line="3" column="5" lastLine="3" lastColumn="6" value="1"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="4" column="1" lastLine="4" lastColumn="7">
            |      <BinaryExpression line="4" column="1" lastLine="4" lastColumn="7" token="+=">
            |        <VariableExpression line="4" column="1" lastLine="4" lastColumn="2" name="a"/>
            |        <ConstantExpression line="4" column="6" lastLine="4" lastColumn="7" value="1"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="5" column="1" lastLine="5" lastColumn="7">
            |      <BinaryExpression line="5" column="1" lastLine="5" lastColumn="7" token="-=">
            |        <VariableExpression line="5" column="1" lastLine="5" lastColumn="2" name="a"/>
            |        <ConstantExpression line="5" column="6" lastLine="5" lastColumn="7" value="1"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="6" column="1" lastLine="6" lastColumn="7">
            |      <BinaryExpression line="6" column="1" lastLine="6" lastColumn="7" token="*=">
            |        <VariableExpression line="6" column="1" lastLine="6" lastColumn="2" name="a"/>
            |        <ConstantExpression line="6" column="6" lastLine="6" lastColumn="7" value="1"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="7" column="1" lastLine="7" lastColumn="7">
            |      <BinaryExpression line="7" column="1" lastLine="7" lastColumn="7" token="/=">
            |        <VariableExpression line="7" column="1" lastLine="7" lastColumn="2" name="a"/>
            |        <ConstantExpression line="7" column="6" lastLine="7" lastColumn="7" value="1"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="8" column="1" lastLine="8" lastColumn="7">
            |      <BinaryExpression line="8" column="1" lastLine="8" lastColumn="7" token="%=">
            |        <VariableExpression line="8" column="1" lastLine="8" lastColumn="2" name="a"/>
            |        <ConstantExpression line="8" column="6" lastLine="8" lastColumn="7" value="1"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="9" column="1" lastLine="9" lastColumn="8">
            |      <BinaryExpression line="9" column="1" lastLine="9" lastColumn="8" token="**=">
            |        <VariableExpression line="9" column="1" lastLine="9" lastColumn="2" name="a"/>
            |        <ConstantExpression line="9" column="7" lastLine="9" lastColumn="8" value="2"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="10" column="1" lastLine="10" lastColumn="7">
            |      <BinaryExpression line="10" column="1" lastLine="10" lastColumn="7" token="?=">
            |        <VariableExpression line="10" column="1" lastLine="10" lastColumn="2" name="a"/>
            |        <ConstantExpression line="10" column="6" lastLine="10" lastColumn="7" value="1"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="11" column="1" lastLine="11" lastColumn="16">
            |      <BinaryExpression line="11" column="1" lastLine="11" lastColumn="16" token="=">
            |        <TupleExpression line="11" column="1" lastLine="11" lastColumn="7">
            |          <VariableExpression line="11" column="2" lastLine="11" lastColumn="3" name="x"/>
            |          <VariableExpression line="11" column="5" lastLine="11" lastColumn="6" name="y"/>
            |        </TupleExpression>
            |        <ListExpression line="11" column="10" lastLine="11" lastColumn="16">
            |          <ConstantExpression line="11" column="11" lastLine="11" lastColumn="12" value="1"/>
            |          <ConstantExpression line="11" column="14" lastLine="11" lastColumn="15" value="2"/>
            |        </ListExpression>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'path expression'() {
        expectAst '''\
            |a.b
            |a?.b
            |a*.b
            |a??.b.c
            |a.@b
            |a?.@b
            |a*.@b
            |a.'name'
            |a."$name"
            |a[1]
            |a[1, 2]
            |a?[1]
            |a[x: 1]
            |a(1, 2)
            |a { it }
            |a.<Integer>m(1)
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ExpressionStatement line="1" column="1" lastLine="1" lastColumn="4">
            |      <PropertyExpression line="1" column="1" lastLine="1" lastColumn="4">
            |        <VariableExpression line="1" column="1" lastLine="1" lastColumn="2" name="a"/>
            |        <ConstantExpression line="1" column="3" lastLine="1" lastColumn="4" value="b"/>
            |      </PropertyExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="2" column="1" lastLine="2" lastColumn="5">
            |      <PropertyExpression line="2" column="1" lastLine="2" lastColumn="5" safe="true">
            |        <VariableExpression line="2" column="1" lastLine="2" lastColumn="2" name="a"/>
            |        <ConstantExpression line="2" column="4" lastLine="2" lastColumn="5" value="b"/>
            |      </PropertyExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="3" column="1" lastLine="3" lastColumn="5">
            |      <PropertyExpression line="3" column="1" lastLine="3" lastColumn="5" safe="true" spreadSafe="true">
            |        <VariableExpression line="3" column="1" lastLine="3" lastColumn="2" name="a"/>
            |        <ConstantExpression line="3" column="4" lastLine="3" lastColumn="5" value="b"/>
            |      </PropertyExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="4" column="1" lastLine="4" lastColumn="8">
            |      <PropertyExpression line="4" column="1" lastLine="4" lastColumn="8" safe="true">
            |        <PropertyExpression line="4" column="2" lastLine="4" lastColumn="6" safe="true">
            |          <VariableExpression line="4" column="1" lastLine="4" lastColumn="2" name="a"/>
            |          <ConstantExpression line="4" column="5" lastLine="4" lastColumn="6" value="b"/>
            |        </PropertyExpression>
            |        <ConstantExpression line="4" column="7" lastLine="4" lastColumn="8" value="c"/>
            |      </PropertyExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="5" column="1" lastLine="5" lastColumn="5">
            |      <AttributeExpression line="5" column="1" lastLine="5" lastColumn="5">
            |        <VariableExpression line="5" column="1" lastLine="5" lastColumn="2" name="a"/>
            |        <ConstantExpression line="5" column="4" lastLine="5" lastColumn="5" value="b"/>
            |      </AttributeExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="6" column="1" lastLine="6" lastColumn="6">
            |      <AttributeExpression line="6" column="1" lastLine="6" lastColumn="6" safe="true">
            |        <VariableExpression line="6" column="1" lastLine="6" lastColumn="2" name="a"/>
            |        <ConstantExpression line="6" column="5" lastLine="6" lastColumn="6" value="b"/>
            |      </AttributeExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="7" column="1" lastLine="7" lastColumn="6">
            |      <AttributeExpression line="7" column="1" lastLine="7" lastColumn="6" safe="true" spreadSafe="true">
            |        <VariableExpression line="7" column="1" lastLine="7" lastColumn="2" name="a"/>
            |        <ConstantExpression line="7" column="5" lastLine="7" lastColumn="6" value="b"/>
            |      </AttributeExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="8" column="1" lastLine="8" lastColumn="9">
            |      <PropertyExpression line="8" column="1" lastLine="8" lastColumn="9">
            |        <VariableExpression line="8" column="1" lastLine="8" lastColumn="2" name="a"/>
            |        <ConstantExpression line="8" column="3" lastLine="8" lastColumn="9" value="name"/>
            |      </PropertyExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="9" column="1" lastLine="9" lastColumn="10">
            |      <PropertyExpression line="9" column="1" lastLine="9" lastColumn="10">
            |        <VariableExpression line="9" column="1" lastLine="9" lastColumn="2" name="a"/>
            |        <GStringExpression line="9" column="3" lastLine="9" lastColumn="10" text="$name">
            |          <VariableExpression line="9" column="5" lastLine="9" lastColumn="9" name="name"/>
            |        </GStringExpression>
            |      </PropertyExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="10" column="1" lastLine="10" lastColumn="5">
            |      <BinaryExpression line="10" column="1" lastLine="10" lastColumn="5" token="[">
            |        <VariableExpression line="10" column="1" lastLine="10" lastColumn="2" name="a"/>
            |        <ConstantExpression line="10" column="3" lastLine="10" lastColumn="4" value="1"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="11" column="1" lastLine="11" lastColumn="8">
            |      <BinaryExpression line="11" column="1" lastLine="11" lastColumn="8" token="[">
            |        <VariableExpression line="11" column="1" lastLine="11" lastColumn="2" name="a"/>
            |        <ListExpression line="11" column="2" lastLine="11" lastColumn="8">
            |          <ConstantExpression line="11" column="3" lastLine="11" lastColumn="4" value="1"/>
            |          <ConstantExpression line="11" column="6" lastLine="11" lastColumn="7" value="2"/>
            |        </ListExpression>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="12" column="1" lastLine="12" lastColumn="6">
            |      <BinaryExpression line="12" column="1" lastLine="12" lastColumn="6" token="?[">
            |        <VariableExpression line="12" column="1" lastLine="12" lastColumn="2" name="a"/>
            |        <ConstantExpression line="12" column="4" lastLine="12" lastColumn="5" value="1"/>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="13" column="1" lastLine="13" lastColumn="8">
            |      <BinaryExpression line="13" column="1" lastLine="13" lastColumn="8" token="[">
            |        <VariableExpression line="13" column="1" lastLine="13" lastColumn="2" name="a"/>
            |        <ListExpression line="13" column="2" lastLine="13" lastColumn="8">
            |          <MapEntryExpression line="13" column="3" lastLine="13" lastColumn="7">
            |            <ConstantExpression line="13" column="3" lastLine="13" lastColumn="4" value="x"/>
            |            <ConstantExpression line="13" column="6" lastLine="13" lastColumn="7" value="1"/>
            |          </MapEntryExpression>
            |        </ListExpression>
            |      </BinaryExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="14" column="1" lastLine="14" lastColumn="8">
            |      <MethodCallExpression line="14" column="1" lastLine="14" lastColumn="8" implicitThis="true">
            |        <ConstantExpression line="14" column="1" lastLine="14" lastColumn="2" value="a"/>
            |        <ArgumentListExpression line="14" column="2" lastLine="14" lastColumn="8">
            |          <ConstantExpression line="14" column="3" lastLine="14" lastColumn="4" value="1"/>
            |          <ConstantExpression line="14" column="6" lastLine="14" lastColumn="7" value="2"/>
            |        </ArgumentListExpression>
            |      </MethodCallExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="15" column="1" lastLine="15" lastColumn="9">
            |      <MethodCallExpression line="15" column="1" lastLine="15" lastColumn="9" implicitThis="true">
            |        <ConstantExpression line="15" column="1" lastLine="15" lastColumn="2" value="a"/>
            |        <ClosureExpression line="15" column="3" lastLine="15" lastColumn="9">
            |          <BlockStatement line="15" column="5" lastLine="15" lastColumn="7">
            |            <ExpressionStatement line="15" column="5" lastLine="15" lastColumn="7">
            |              <VariableExpression line="15" column="5" lastLine="15" lastColumn="7" name="it"/>
            |            </ExpressionStatement>
            |          </BlockStatement>
            |        </ClosureExpression>
            |      </MethodCallExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="16" column="1" lastLine="16" lastColumn="16">
            |      <MethodCallExpression line="16" column="1" lastLine="16" lastColumn="16">
            |        <VariableExpression line="16" column="1" lastLine="16" lastColumn="2" name="a"/>
            |        <ConstantExpression line="16" column="12" lastLine="16" lastColumn="13" value="m"/>
            |        <ConstantExpression line="16" column="14" lastLine="16" lastColumn="15" value="1"/>
            |      </MethodCallExpression>
            |    </ExpressionStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'command expression'() {
        expectAst '''\
            |println a
            |a 1, 2
            |a x: 1, y: 2
            |a.b 1, 2
            |task someTask() {}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ExpressionStatement line="1" column="1" lastLine="1" lastColumn="10">
            |      <MethodCallExpression line="1" column="1" lastLine="1" lastColumn="10" implicitThis="true">
            |        <ConstantExpression line="1" column="1" lastLine="1" lastColumn="8" value="println"/>
            |        <VariableExpression line="1" column="9" lastLine="1" lastColumn="10" name="a"/>
            |      </MethodCallExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="2" column="1" lastLine="2" lastColumn="7">
            |      <MethodCallExpression line="2" column="1" lastLine="2" lastColumn="7" implicitThis="true">
            |        <ConstantExpression line="2" column="1" lastLine="2" lastColumn="2" value="a"/>
            |        <ArgumentListExpression line="2" column="3" lastLine="2" lastColumn="7">
            |          <ConstantExpression line="2" column="3" lastLine="2" lastColumn="4" value="1"/>
            |          <ConstantExpression line="2" column="6" lastLine="2" lastColumn="7" value="2"/>
            |        </ArgumentListExpression>
            |      </MethodCallExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="3" column="1" lastLine="3" lastColumn="13">
            |      <MethodCallExpression line="3" column="1" lastLine="3" lastColumn="13" implicitThis="true">
            |        <ConstantExpression line="3" column="1" lastLine="3" lastColumn="2" value="a"/>
            |        <TupleExpression line="3" column="3" lastLine="3" lastColumn="13">
            |          <NamedArgumentListExpression line="3" column="3" lastLine="3" lastColumn="13">
            |            <MapEntryExpression line="3" column="3" lastLine="3" lastColumn="7">
            |              <ConstantExpression line="3" column="3" lastLine="3" lastColumn="4" value="x"/>
            |              <ConstantExpression line="3" column="6" lastLine="3" lastColumn="7" value="1"/>
            |            </MapEntryExpression>
            |            <MapEntryExpression line="3" column="9" lastLine="3" lastColumn="13">
            |              <ConstantExpression line="3" column="9" lastLine="3" lastColumn="10" value="y"/>
            |              <ConstantExpression line="3" column="12" lastLine="3" lastColumn="13" value="2"/>
            |            </MapEntryExpression>
            |          </NamedArgumentListExpression>
            |        </TupleExpression>
            |      </MethodCallExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="4" column="1" lastLine="4" lastColumn="9">
            |      <MethodCallExpression line="4" column="1" lastLine="4" lastColumn="9">
            |        <VariableExpression line="4" column="1" lastLine="4" lastColumn="2" name="a"/>
            |        <ConstantExpression line="4" column="3" lastLine="4" lastColumn="4" value="b"/>
            |        <ArgumentListExpression line="4" column="5" lastLine="4" lastColumn="9">
            |          <ConstantExpression line="4" column="5" lastLine="4" lastColumn="6" value="1"/>
            |          <ConstantExpression line="4" column="8" lastLine="4" lastColumn="9" value="2"/>
            |        </ArgumentListExpression>
            |      </MethodCallExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="5" column="1" lastLine="5" lastColumn="19">
            |      <MethodCallExpression line="5" column="1" lastLine="5" lastColumn="19" implicitThis="true">
            |        <ConstantExpression line="5" column="1" lastLine="5" lastColumn="5" value="task"/>
            |        <MethodCallExpression line="5" column="6" lastLine="5" lastColumn="19" implicitThis="true">
            |          <ConstantExpression line="5" column="6" lastLine="5" lastColumn="14" value="someTask"/>
            |          <ClosureExpression line="5" column="17" lastLine="5" lastColumn="19">
            |            <BlockStatement line="5" column="17" lastLine="5" lastColumn="19"/>
            |          </ClosureExpression>
            |        </MethodCallExpression>
            |      </MethodCallExpression>
            |    </ExpressionStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'switch expression'() {
        expectAst '''\
            |def r = switch (a) {
            |    case 8 -> 'b'
            |    default -> 'z'
            |}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ExpressionStatement line="1" column="1" lastLine="4" lastColumn="2">
            |      <DeclarationExpression line="1" column="1" lastLine="4" lastColumn="2" token="=">
            |        <VariableExpression line="1" column="5" lastLine="1" lastColumn="6" name="r"/>
            |        <SwitchExpression line="1" column="9" lastLine="4" lastColumn="2">
            |          <VariableExpression line="1" column="17" lastLine="1" lastColumn="18" name="a"/>
            |          <CaseStatement line="2" column="5" lastLine="2" lastColumn="9">
            |            <ConstantExpression line="2" column="10" lastLine="2" lastColumn="11" value="8"/>
            |            <BlockStatement line="2" column="15" lastLine="2" lastColumn="18">
            |              <YieldStatement line="2" column="15" lastLine="2" lastColumn="18">
            |                <ConstantExpression line="2" column="15" lastLine="2" lastColumn="18" value="b"/>
            |              </YieldStatement>
            |            </BlockStatement>
            |          </CaseStatement>
            |          <default>
            |            <BlockStatement line="3" column="16" lastLine="3" lastColumn="19">
            |              <YieldStatement line="3" column="16" lastLine="3" lastColumn="19">
            |                <ConstantExpression line="3" column="16" lastLine="3" lastColumn="19" value="z"/>
            |              </YieldStatement>
            |            </BlockStatement>
            |          </default>
            |        </SwitchExpression>
            |      </DeclarationExpression>
            |    </ExpressionStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
        expectAst '''\
            |def r = switch (a) {
            |    case 6, 9: yield 'a'
            |    default: yield 'b'
            |}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ExpressionStatement line="1" column="1" lastLine="4" lastColumn="2">
            |      <DeclarationExpression line="1" column="1" lastLine="4" lastColumn="2" token="=">
            |        <VariableExpression line="1" column="5" lastLine="1" lastColumn="6" name="r"/>
            |        <SwitchExpression line="1" column="9" lastLine="4" lastColumn="2">
            |          <VariableExpression line="1" column="17" lastLine="1" lastColumn="18" name="a"/>
            |          <CaseStatement line="2" column="5" lastLine="2" lastColumn="9">
            |            <ConstantExpression line="2" column="10" lastLine="2" lastColumn="11" value="6"/>
            |            < line="-1" column="-1" lastLine="-1" lastColumn="-1"/>
            |          </CaseStatement>
            |          <CaseStatement line="2" column="5" lastLine="2" lastColumn="9">
            |            <ConstantExpression line="2" column="13" lastLine="2" lastColumn="14" value="9"/>
            |            <BlockStatement line="2" column="16" lastLine="3" lastColumn="1">
            |              <YieldStatement line="2" column="16" lastLine="2" lastColumn="25">
            |                <ConstantExpression line="2" column="22" lastLine="2" lastColumn="25" value="a"/>
            |              </YieldStatement>
            |            </BlockStatement>
            |          </CaseStatement>
            |          <default>
            |            <BlockStatement line="3" column="14" lastLine="4" lastColumn="1">
            |              <YieldStatement line="3" column="14" lastLine="3" lastColumn="23">
            |                <ConstantExpression line="3" column="20" lastLine="3" lastColumn="23" value="b"/>
            |              </YieldStatement>
            |            </BlockStatement>
            |          </default>
            |        </SwitchExpression>
            |      </DeclarationExpression>
            |    </ExpressionStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
        expectAst '''\
            |def r = switch (month) {
            |    case JANUARY, FEBRUARY, MARCH -> {
            |        yield 'Q1'
            |    }
            |    default -> 'other'
            |}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ExpressionStatement line="1" column="1" lastLine="6" lastColumn="2">
            |      <DeclarationExpression line="1" column="1" lastLine="6" lastColumn="2" token="=">
            |        <VariableExpression line="1" column="5" lastLine="1" lastColumn="6" name="r"/>
            |        <SwitchExpression line="1" column="9" lastLine="6" lastColumn="2">
            |          <VariableExpression line="1" column="17" lastLine="1" lastColumn="22" name="month"/>
            |          <CaseStatement line="2" column="5" lastLine="2" lastColumn="9">
            |            <VariableExpression line="2" column="10" lastLine="2" lastColumn="17" name="JANUARY"/>
            |            < line="-1" column="-1" lastLine="-1" lastColumn="-1"/>
            |          </CaseStatement>
            |          <CaseStatement line="2" column="5" lastLine="2" lastColumn="9">
            |            <VariableExpression line="2" column="19" lastLine="2" lastColumn="27" name="FEBRUARY"/>
            |            < line="-1" column="-1" lastLine="-1" lastColumn="-1"/>
            |          </CaseStatement>
            |          <CaseStatement line="2" column="5" lastLine="2" lastColumn="9">
            |            <VariableExpression line="2" column="29" lastLine="2" lastColumn="34" name="MARCH"/>
            |            <BlockStatement line="3" column="9" lastLine="3" lastColumn="19">
            |              <YieldStatement line="3" column="9" lastLine="3" lastColumn="19">
            |                <ConstantExpression line="3" column="15" lastLine="3" lastColumn="19" value="Q1"/>
            |              </YieldStatement>
            |            </BlockStatement>
            |          </CaseStatement>
            |          <default>
            |            <BlockStatement line="5" column="16" lastLine="5" lastColumn="23">
            |              <YieldStatement line="5" column="16" lastLine="5" lastColumn="23">
            |                <ConstantExpression line="5" column="16" lastLine="5" lastColumn="23" value="other"/>
            |              </YieldStatement>
            |            </BlockStatement>
            |          </default>
            |        </SwitchExpression>
            |      </DeclarationExpression>
            |    </ExpressionStatement>
            |  </BlockStatement>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'named arguments and varargs'() {
        expectAst '''\
            |m debit: 30, credit: 40
            |def foo(String... strs) {}
            |foo('a', 'b')
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <BlockStatement line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |    <ExpressionStatement line="1" column="1" lastLine="1" lastColumn="24">
            |      <MethodCallExpression line="1" column="1" lastLine="1" lastColumn="24" implicitThis="true">
            |        <ConstantExpression line="1" column="1" lastLine="1" lastColumn="2" value="m"/>
            |        <TupleExpression line="1" column="3" lastLine="1" lastColumn="24">
            |          <NamedArgumentListExpression line="1" column="3" lastLine="1" lastColumn="24">
            |            <MapEntryExpression line="1" column="3" lastLine="1" lastColumn="12">
            |              <ConstantExpression line="1" column="3" lastLine="1" lastColumn="8" value="debit"/>
            |              <ConstantExpression line="1" column="10" lastLine="1" lastColumn="12" value="30"/>
            |            </MapEntryExpression>
            |            <MapEntryExpression line="1" column="14" lastLine="1" lastColumn="24">
            |              <ConstantExpression line="1" column="14" lastLine="1" lastColumn="20" value="credit"/>
            |              <ConstantExpression line="1" column="22" lastLine="1" lastColumn="24" value="40"/>
            |            </MapEntryExpression>
            |          </NamedArgumentListExpression>
            |        </TupleExpression>
            |      </MethodCallExpression>
            |    </ExpressionStatement>
            |    <ExpressionStatement line="3" column="1" lastLine="3" lastColumn="14">
            |      <MethodCallExpression line="3" column="1" lastLine="3" lastColumn="14" implicitThis="true">
            |        <ConstantExpression line="3" column="1" lastLine="3" lastColumn="4" value="foo"/>
            |        <ArgumentListExpression line="3" column="4" lastLine="3" lastColumn="14">
            |          <ConstantExpression line="3" column="5" lastLine="3" lastColumn="8" value="a"/>
            |          <ConstantExpression line="3" column="10" lastLine="3" lastColumn="13" value="b"/>
            |        </ArgumentListExpression>
            |      </MethodCallExpression>
            |    </ExpressionStatement>
            |  </BlockStatement>
            |  <MethodNode line="2" column="1" lastLine="2" lastColumn="27" name="foo" returnType="java.lang.Object">
            |    <Parameter line="2" column="9" lastLine="2" lastColumn="23" name="strs" type="String[]"/>
            |    <BlockStatement line="2" column="25" lastLine="2" lastColumn="27"/>
            |  </MethodNode>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'annotations'() {
        expectAst '''\
            |@Deprecated
            |@SuppressWarnings(['rawtypes'])
            |@Ann(v = 1, w = [1, 2])
            |class C {
            |    @Override
            |    String toString() { 'C' }
            |}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <AnnotationNode line="1" column="1" lastLine="1" lastColumn="12" class="Deprecated"/>
            |  <AnnotationNode line="2" column="1" lastLine="2" lastColumn="32" class="SuppressWarnings" members="value"/>
            |  <AnnotationNode line="3" column="1" lastLine="3" lastColumn="24" class="Ann" members="v,w"/>
            |  <ClassNode line="1" column="1" lastLine="7" lastColumn="2" kind="class" name="C">
            |    <AnnotationNode line="5" column="5" lastLine="5" lastColumn="14" class="Override"/>
            |    <MethodNode line="5" column="5" lastLine="6" lastColumn="30" name="toString" returnType="String">
            |      <BlockStatement line="6" column="23" lastLine="6" lastColumn="30">
            |        <ExpressionStatement line="6" column="25" lastLine="6" lastColumn="28">
            |          <ConstantExpression line="6" column="25" lastLine="6" lastColumn="28" value="C"/>
            |        </ExpressionStatement>
            |      </BlockStatement>
            |    </MethodNode>
            |  </ClassNode>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'comments and groovydoc'() {
        expectAst '''\
            |// line comment
            |/* block comment */
            |/** groovydoc */
            |/**@ runtime groovydoc */
            |class AA {}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <ClassNode line="5" column="1" lastLine="5" lastColumn="12" kind="class" name="AA"/>
            |</Module>
            |'''.stripMargin()
    }

    @Test
    void 'script method'() {
        expectAst '''\
            |protected String getGroovySql() {
            |    return 'abc'
            |}
            |'''.stripMargin(), '''\
            |<Module line="-1" column="-1" lastLine="-1" lastColumn="-1">
            |  <MethodNode line="1" column="1" lastLine="3" lastColumn="2" name="getGroovySql" modifiers="protected" returnType="String">
            |    <BlockStatement line="1" column="33" lastLine="3" lastColumn="2">
            |      <ReturnStatement line="2" column="5" lastLine="2" lastColumn="17">
            |        <ConstantExpression line="2" column="12" lastLine="2" lastColumn="17" value="abc"/>
            |      </ReturnStatement>
            |    </BlockStatement>
            |  </MethodNode>
            |</Module>
            |'''.stripMargin()
    }

}
