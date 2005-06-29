/**
 *
 * Copyright 2005 Jeremy Rayner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **/
package org.codehaus.groovy.antlr.treewalker;

import antlr.collections.AST;
import groovy.util.GroovyTestCase;
import java.io.StringReader;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;

import org.codehaus.groovy.antlr.parser.GroovyRecognizer;
import org.codehaus.groovy.antlr.parser.GroovyLexer;
import org.codehaus.groovy.antlr.AntlrASTProcessor;
import org.codehaus.groovy.antlr.UnicodeEscapingReader;
import org.codehaus.groovy.antlr.SourceBuffer;

/**
 * Testcases for the antlr AST visitor that prints groovy source code.
 *
 * @author <a href="mailto:groovy@ross-rayner.com">Jeremy Rayner</a>
 * @version $Revision$
 */
public class SourcePrinterTest extends GroovyTestCase {


    public void testAnnotations() throws Exception{
        //todo assertEquals("@Property foo", pretty("@Property foo"));
    }

    public void testAssign() throws Exception {
        assertEquals("a = 12", pretty("a = 12"));
    }

    public void testClassDef() throws Exception {
        assertEquals("class Foo {def bar}", pretty("class Foo {def bar}"));
    }

    public void testDot() throws Exception {
        assertEquals("foo.bar.mooky()", pretty("foo.bar.mooky()"));
    }

    public void testElist() throws Exception {
        assertEquals("foo(bar,mooky)", pretty("foo(bar,mooky)"));
    }

    public void testEqual() throws Exception {
        assertEquals("a == b", pretty("a == b"));
    }

    public void testExpr() throws Exception {
        //todo assertEquals("foo(bar);mooky(bar)", pretty("foo(bar);mooky(bar)"));
    }

    public void testExtendsClause() throws Exception {
        assertEquals("class Foo extends Bar {}", pretty("class Foo extends Bar {}"));
    }

    public void testIdent() throws Exception {
        assertEquals("foo.bar", pretty("foo.bar"));
    }

    public void testImplementsClause() throws Exception {
        assertEquals("class Foo implements Bar {}", pretty("class Foo implements Bar {}"));
    }

    public void testImport() throws Exception {
        assertEquals("import foo.bar.Wibble", pretty("import foo.bar.Wibble"));
    }

    public void testListConstructor() throws Exception {
        //todo assertEquals("[a,b]", pretty("[a,b]"));
    }

    public void testLiteralAssert() throws Exception {
        assertEquals("assert a == true", pretty("assert a == true"));
    }

    public void testLiteralBoolean() throws Exception {
        assertEquals("boolean b = true", pretty("boolean b = true"));
    }

    public void testLiteralCatch() throws Exception {
        //todo assertEquals("try {} catch (Exception e) {}", pretty("try {} catch (Exception e) {}"));
    }

    public void testLiteralFloat() throws Exception {
        assertEquals("float x", pretty("float x"));
    }

    public void testLiteralIf() throws Exception {
        assertEquals("if (a == b) {}", pretty("if (a == b) {}"));
    }

    public void testLiteralInt() throws Exception {
        assertEquals("int a", pretty("int a"));
    }

    public void testLiteralNew() throws Exception {
        assertEquals("new Foo()", pretty("new Foo()"));
    }

    public void testLiteralPrivate() throws Exception {
        //todo assertEquals("private bar", pretty("private bar"));
    }

    public void testLiteralStatic() throws Exception {
        assertEquals("static void foo() {}", pretty("static void foo() {}"));
    }

    public void testLiteralTrue() throws Exception {
        assertEquals("foo = true", pretty("foo = true"));
    }

    public void testLiteralTry() throws Exception {
        //todo assertEquals("try {} catch (Exception e) {}", pretty("try {} catch (Exception e) {}"));
    }

    public void testLiteralVoid() throws Exception {
        assertEquals("void foo()", pretty("void foo()"));
    }

    public void testLiteralWhile() throws Exception {
        assertEquals("while (true) {}", pretty("while(true){}"));
    }

    public void testMethodCall() throws Exception {
        assertEquals("foo(bar)", pretty("foo(bar)"));
    }

    public void testMethodDef() throws Exception {
        assertEquals("def foo(int bar) {}", pretty("def foo(int bar) {}"));
    }

    public void testModifiers() throws Exception {
        //todo assertEquals("", pretty(""));
    }

    public void testNumInt() throws Exception {
        assertEquals("a = 12", pretty("a = 12"));
    }

    public void testNumFloat() throws Exception {
        assertEquals("b = 34.4f", pretty("b = 34.4f"));
    }

    public void testObjblock() throws Exception {
        assertEquals("class Foo {def bar}", pretty("class Foo {def bar}"));
    }

    public void testPackageDef() throws Exception {
        assertEquals("package foo.bar", pretty("package foo.bar"));
    }

    public void testParameterDef() throws Exception {
        //todo assertEquals("", pretty(""));
    }

    public void testParameters() throws Exception {
        //todo assertEquals("", pretty(""));
    }

    public void testSlist() throws Exception {
        assertEquals("if (true) {foo}", pretty("if (true) {foo}"));
    }

    public void testStar() throws Exception {
        assertEquals("import foo.*", pretty("import foo.*"));
        assertEquals("a*b", pretty("a*b"));
    }

    public void testStringConstructor() throws Exception {
        //todo assertEquals("\"foo$bar\"", pretty("\"foo$bar\""));
    }

    public void testStringLiteral() throws Exception {
        assertEquals("\"mooky\"", pretty("\"mooky\""));
    }

    public void testType() throws Exception {
        assertEquals("String bar", pretty("String bar"));
    }

    public void testTypecast() throws Exception {
        //todo assertEquals("foo = (String)bar", pretty("foo = (String)bar"));
    }

    public void testVariableDef() throws Exception {
        assertEquals("def x = 1", pretty("def x = 1"));
    }

    public String pretty(String input) throws Exception{
        GroovyRecognizer parser = null;
        SourceBuffer sourceBuffer = new SourceBuffer();
        UnicodeEscapingReader unicodeReader = new UnicodeEscapingReader(new StringReader(input),sourceBuffer);
        GroovyLexer lexer = new GroovyLexer(unicodeReader);
        unicodeReader.setLexer(lexer);
        parser = GroovyRecognizer.make(lexer);
        parser.setSourceBuffer(sourceBuffer);

        String[] tokenNames;
        tokenNames = parser.getTokenNames();
        parser.compilationUnit();
        AST ast = parser.getAST();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Visitor visitor = new SourcePrinter(new PrintStream(baos),tokenNames,false);
        AntlrASTProcessor traverser = new SourceCodeTraversal(visitor);

        traverser.process(ast);

        return new String(baos.toByteArray());
    }

}
