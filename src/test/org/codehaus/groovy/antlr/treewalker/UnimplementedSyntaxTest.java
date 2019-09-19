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
package org.codehaus.groovy.antlr.treewalker;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.test.GroovyTestCase;

/**
 * This tests code that is valid in parser, but has issues further down the line.
 */
public class UnimplementedSyntaxTest extends GroovyTestCase {
    // ------------------------------
    // feature: Annotation Definition
    // ------------------------------

    public void test_AnnotationDef1_FAILS() throws Exception {
        if (notYetImplemented()) return;
        // Unknown type: ANNOTATION_DEF
        assertNotNull(compile("public @interface Foo{}"));
    }

    public void test_AnnotationDef2_FAILS() throws Exception {
        if (notYetImplemented()) return;
        // Unknown type: ANNOTATION_DEF
        assertNotNull(compile("public @interface Foo{int bar() default 123}"));
    }

    // ------------------------------
    // bug: Qualified Exception Types
    // ------------------------------

    public void test_QualifiedExceptionTypes_FAILS() throws Exception {
        if (notYetImplemented()) return;
        // Unexpected node type: '.' found when expecting type: an identifier
        assertNotNull(compile("def foo() throws bar.MookyException{}")); // fails after parser
    }

    // ------------------------------
    // feature: classic Java for loop
    // ------------------------------

    public void test_ClassicJavaForLoop1_FAILS() throws Exception {
        if (notYetImplemented()) return;
        // For statement contains unexpected tokens. Possible attempt to use unsupported Java-style for loop.
        // This syntax now replaced with closure list i.e. for (i=0;j=2;i<10;i++;j--) {...
        assertNotNull(compile("for (i = 0,j = 2;i < 10; i++, j--) {print i}")); // fails after parser
    }

    public void test_ClassicJavaForLoop2() throws Exception {
        // For statement contains unexpected tokens. Possible attempt to use unsupported Java-style for loop.
        assertNotNull(compile("for (i=0;i<10;i++) {println i}")); // fails after parser
    }

    // ------------------------------
    // feature: Enum Definitions
    // ------------------------------
    public void test_EnumDef1_FAILS() throws Exception {
        if (notYetImplemented()) return;
        // Unknown type: ENUM_DEF
        assertNotNull(compile("enum Coin {PENNY(1), DIME(10), QUARTER(25)}")); // fails after parser
    }

    public void test_EnumDef2_FAILS() throws Exception {
        if (notYetImplemented()) return;
        // Unknown type: ENUM_DEF
        assertNotNull(compile("enum Season{WINTER,SPRING,SUMMER,AUTUMN}")); // fails after parser
    }

    public void test_EnumDef3_FAILS() throws Exception {
        if (notYetImplemented()) return;
        // Unknown type: ENUM_DEF
        assertNotNull(compile("enum Operation {ADDITION {double eval(x,y) {return x + y}}}")); // fails after parser
    }

    public void test_EnumDef4_FAILS() throws Exception {
        if (notYetImplemented()) return;
        // Unknown type: ENUM_DEF
        assertNotNull(compile("enum EarthSeason implements Season{SPRING}")); // fails after parser
    }

    // ------------------------------------------------
    // deprecate in parser?: 'break' allowed in methods
    // ------------------------------------------------
    public void test_BreakAllowedInMethods_FAILS() throws Exception {
        if (notYetImplemented()) return;
        // the break statement is only allowed inside loops or switches
        assertNotNull(compile("def myMethod(){break}")); // fails after parser
    }

    // ----------------------------------------------------
    // deprecate in parser?: 'continue' allowed in closures
    // ----------------------------------------------------
    public void test_ContinueAllowedInClosures_FAILS() throws Exception {
        if (notYetImplemented()) return;
        // the continue statement is only allowed inside loops
        assertNotNull(compile("[1,2,3].each{continue}")); // fails after parser
    }

    // ----------------------------------------------------
    // feature?: allow break/continue with value from loops?
    // ----------------------------------------------------
    public void test_BreakWithValueAllowedInLoops_FAILS() throws Exception {
        if (notYetImplemented()) return;
        // Unexpected node type: a numeric literal found when expecting type: an identifier
        assertNotNull(compile("for (i in 1..100) {break 2}")); // fails after parser
    }

    public void test_ContinueWithValueAllowedInLoops_FAILS() throws Exception {
        if (notYetImplemented()) return;
        // Unexpected node type: a numeric literal found when expecting type: an identifier
        assertNotNull(compile("for (i in 1..100) {continue 2}")); // fails after parser
    }

    // ---------------------------------------------------------------
    // feature?: allow break/continue to labeled statement from loops? (is this even right syntax, or parser bug???)
    // ---------------------------------------------------------------
    public void test_BreakToLabeledStatementAllowedInLoops_FAILS() throws Exception {
        if (notYetImplemented()) return;
        // Unexpected node type: LABELED_STAT found when expecting type: an identifier
        assertNotNull(compile("for (i in 1..100) {break label1:}")); // fails after parser
    }

    public void test_ContinueToLabeledStatementAllowedInLoops_FAILS() throws Exception {
        if (notYetImplemented()) return;
        // Unexpected node type: LABELED_STAT found when expecting type: an identifier
        assertNotNull(compile("for (i in 1..100) {continue label1:}")); // fails after parser
    }

    // -----------------------
    // feature: Native Methods
    // -----------------------
    public void test_NativeMethods1_FAILS() throws Exception {
        if (notYetImplemented()) return;
        // You defined a method without body. Try adding a body, or declare it abstract
        assertNotNull(compile("public class R{public native void seek(long pos)}")); // fails after parser
    }

    public void test_NativeMethods2_FAILS() throws Exception {
        if (notYetImplemented()) return;
        // You defined a method without body. Try adding a body, or declare it abstract
        assertNotNull(compile("native foo()")); // fails after parser
    }

    // ---------------------
    // feature: 'threadsafe'
    // ---------------------
    public void test_Threadsafe1_FAILS() throws Exception {
        if (notYetImplemented()) return;
        // Unknown type: "threadsafe"
        assertNotNull(compile("threadsafe foo() {}")); // fails after parser
    }

    public void test_Threadsafe2_FAILS() throws Exception {
        if (notYetImplemented()) return;
        // Unknown type: "threadsafe"
        assertNotNull(compile("public static transient final native threadsafe synchronized volatile strictfp foo() {}")); // fails after parser
    }

    // --------------------------------------------------
    // bugs?: spread expressions in closures and GStrings
    // --------------------------------------------------
    public void test_SpreadExpressionInClosure_FAILS() throws Exception {
        if (notYetImplemented()) return;
        // BUG! exception in phase 'class generation' in source unit 'Script1.groovy'
        // SpreadExpression should not be visited here
        assertNotNull(compile("myList{*name}")); // fails after parser
    }

    public void test_SpreadExpressionInGString1_FAILS() throws Exception {
        if (notYetImplemented()) return;
        // BUG! exception in phase 'conversion' in source unit 'Script1.groovy' null
        assertNotNull(compile("\"foo$*bar\"")); // fails after parser
    }

    public void test_SpreadExpressionInGString2_FAILS() throws Exception {
        if (notYetImplemented()) return;
        // BUG! exception in phase 'class generation' in source unit 'Script1.groovy'
        // SpreadExpression should not be visited here
        assertNotNull(compile("\"foo${*bar}\"")); // fails after parser
    }

    // -----------------------
    // feature: static imports
    // -----------------------
    // TODO: move somewhere else
    public void test_StaticImport1() throws Exception {
        //GROOVY-3711: The following call now results in a valid script class node, so foo.Bar needs to get resolved.
        GroovyShell groovyShell = new GroovyShell();
        compile("package foo; class Bar{}", groovyShell);
        assertNotNull(compile("import static foo.Bar.mooky", groovyShell));
    }

    public void test_StaticImport2() throws Exception {
        //GROOVY-3711: The following call now results in a valid script class node, so foo.Bar needs to get resolved.
        GroovyShell groovyShell = new GroovyShell();
        compile("package foo; class Bar{}", groovyShell);
        assertNotNull(compile("import static foo.Bar.*", groovyShell));
    }

    // TODO: move somewhere else GROOVY-1874
    public void test_staticBlockWithNewLines() throws Exception {
        assertNotNull(compile("class MyClass \n{\n\tstatic\n{\nprintln 2\n}\n}"));
    }
    
    public void test_staticBlockWithNoStartNewLines() throws Exception {
        assertNotNull(compile("class MyClass \n{\n\tstatic {\nprintln 2\n}\n}"));
    }
    
    public void test_staticBlockWithNoNewLines() throws Exception {
        assertNotNull(compile("class MyClass \n{\n\tstatic { println 2 }}"));
    }
    
    // ------------------------
    // feature: type parameters
    // ------------------------
    public void test_TypeParameters_FAILS() throws Exception {
        if (notYetImplemented()) return;
        // Unexpected node type: TYPE_PARAMETERS found when expecting type: OBJBLOCK
        assertNotNull(compile("class Foo<T extends C & I> {T t}")); // fails after parser
    }

    private Script compile(String input) throws Exception {
        return compile(input, new GroovyShell());
    }

    private Script compile(String input, GroovyShell groovyShell) throws Exception {
        TraversalTestHelper traverser = new TraversalTestHelper();
        traverser.traverse(input, SourcePrinter.class, Boolean.FALSE);
        return groovyShell.parse(input);
    }

}
