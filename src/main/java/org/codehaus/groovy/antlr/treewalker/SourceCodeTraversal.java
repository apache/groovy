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

import org.codehaus.groovy.antlr.GroovySourceAST;
import org.codehaus.groovy.antlr.parser.GroovyTokenTypes;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A treewalker for the antlr generated AST that attempts to visit the
 * AST nodes in the order needed to generate valid groovy source code.
 */
public class SourceCodeTraversal extends TraversalHelper {
    /**
     * Constructs a treewalker for the antlr generated AST that attempts to visit the
     * AST nodes in the order needed to generate valid groovy source code.
     * @param visitor the visitor implementation to call for each AST node.
     */
    public SourceCodeTraversal(Visitor visitor) {
        super(visitor);
    }

    /**
     * gather, sort and process all unvisited nodes
     * @param t the AST to process
     */
    public void setUp(GroovySourceAST t) {
        super.setUp(t);
        
        // gather and sort all unvisited AST nodes
        unvisitedNodes = new ArrayList<GroovySourceAST>();
        traverse(t);
        Collections.sort(unvisitedNodes);
    }

    /**
     * traverse an AST node
     * @param t the AST node to traverse
     */
    private void traverse(GroovySourceAST t) {
        if (t == null) { return; }
        if (unvisitedNodes != null) {
           unvisitedNodes.add(t);
        }
        GroovySourceAST child = (GroovySourceAST)t.getFirstChild();
        if (child != null) {
            traverse(child);
        }
        GroovySourceAST sibling = (GroovySourceAST)t.getNextSibling();
        if (sibling != null) {
            traverse(sibling);
        }
    }

    protected void accept(GroovySourceAST currentNode) {
        if (currentNode != null && unvisitedNodes != null && !unvisitedNodes.isEmpty()) {
            GroovySourceAST t = currentNode;

            if (!(unvisitedNodes.contains(currentNode))) {
                return;
            }
            push(t);
            switch (t.getType()) {
                case GroovyTokenTypes.QUESTION: // expr?foo:bar
                    accept_FirstChild_v_SecondChild_v_ThirdChild_v(t);
                    break;

                case GroovyTokenTypes.CASE_GROUP: //
                case GroovyTokenTypes.LITERAL_instanceof: // foo instanceof MyType
                    accept_FirstChild_v_SecondChildsChildren_v(t);
                    break;

                case GroovyTokenTypes.ANNOTATION:
                    accept_v_FirstChild_2ndv_SecondChild_v___LastChild_v(t);
                    break;

                case GroovyTokenTypes.CLOSURE_LIST: // (a=1; a<10; a++)
                case GroovyTokenTypes.ELIST: // a,b,c
                case GroovyTokenTypes.PARAMETERS: // a,b,c
                case GroovyTokenTypes.TYPE_ARGUMENTS: // <String, Object>
                case GroovyTokenTypes.STRING_CONSTRUCTOR: // "foo${bar}wibble"
                case GroovyTokenTypes.TYPE_PARAMETER: // class Foo<T extends F>
                case GroovyTokenTypes.TYPE_PARAMETERS: // class Foo<T>
                case GroovyTokenTypes.TYPE_UPPER_BOUNDS: // class Foo<T extends F>
                    accept_v_FirstChild_v_SecondChild_v___LastChild_v(t);
                    // todo : confirm that TYPE_LOWER_BOUNDS does not have multiple children
                    break;

                case GroovyTokenTypes.VARIABLE_PARAMETER_DEF: // void f(String ... others) {}
                    accept_v_FirstChild_SecondChild_v_ThirdChild_v(t);
                    break;

                case GroovyTokenTypes.INDEX_OP:
                    //accept_FirstChild_v_SecondChild_v(t);
                    accept_SecondChild_v_ThirdChild_v(t);
                    break;

                case GroovyTokenTypes.ENUM_CONSTANT_DEF: // enum Foo(THESE,ARE,THEY)
                case GroovyTokenTypes.EXPR:
                case GroovyTokenTypes.IMPORT:
                case GroovyTokenTypes.STATIC_IMPORT:
                case GroovyTokenTypes.VARIABLE_DEF:
                case GroovyTokenTypes.METHOD_DEF:
                case GroovyTokenTypes.OBJBLOCK: //class Foo {def bar()}  <-- this block
                case GroovyTokenTypes.PARAMETER_DEF: // void f(String me) {}
                case GroovyTokenTypes.SLIST: // list of expressions, variable defs etc
                    accept_v_AllChildren_v(t);
                    break;

                case GroovyTokenTypes.ANNOTATION_MEMBER_VALUE_PAIR: // @Blue(foo=123)
                case GroovyTokenTypes.ASSIGN: // a = b
                case GroovyTokenTypes.BAND_ASSIGN: // a &= b
                case GroovyTokenTypes.BOR_ASSIGN: // a |= b
                case GroovyTokenTypes.BSR_ASSIGN: // a >>>= b
                case GroovyTokenTypes.BXOR_ASSIGN: // a ^= b
                case GroovyTokenTypes.COMPARE_TO: // a <=> b
                case GroovyTokenTypes.DIV_ASSIGN: // a /= b
                case GroovyTokenTypes.EQUAL: // a == b
                case GroovyTokenTypes.MINUS_ASSIGN: // a -= b
                case GroovyTokenTypes.MOD_ASSIGN: // a %= b
                case GroovyTokenTypes.NOT_EQUAL: // a != b
                case GroovyTokenTypes.PLUS_ASSIGN: // a += b
                case GroovyTokenTypes.REGEX_FIND: // a =~ b
                case GroovyTokenTypes.REGEX_MATCH: // a ==~ b
                case GroovyTokenTypes.SL_ASSIGN: // a <<= b
                case GroovyTokenTypes.SR_ASSIGN: // a >>= b
                case GroovyTokenTypes.STAR_ASSIGN: // a *= b
                case GroovyTokenTypes.STAR_STAR_ASSIGN: // x **= 3
                    if (t.childAt(1) != null) {
                        accept_FirstChild_v_RestOfTheChildren(t);
                    } else {
                        accept_v_FirstChild_v_RestOfTheChildren(t);
                    }
                    break;

                case GroovyTokenTypes.ANNOTATION_FIELD_DEF: // @interface Foo{ int bar()...
                    accept_FirstSecondAndThirdChild_v_v_ForthChild(t);
                    break;
                    
                case GroovyTokenTypes.ANNOTATION_DEF: // @interface Foo...
                case GroovyTokenTypes.BAND: // 1 & 2
                case GroovyTokenTypes.BOR: // 1 | 2
                case GroovyTokenTypes.BSR: // 1 >>> 2
                case GroovyTokenTypes.BXOR: // 1 ^ 2
                case GroovyTokenTypes.CLASS_DEF: // class Foo...
                case GroovyTokenTypes.CTOR_IDENT: // private Foo() {...
                case GroovyTokenTypes.DIV: //  3/4
                case GroovyTokenTypes.DOT: // foo.bar
                case GroovyTokenTypes.ENUM_DEF: // enum Foo...
                case GroovyTokenTypes.GE: // a >= b
                case GroovyTokenTypes.GT: // a > b
                case GroovyTokenTypes.INTERFACE_DEF: // interface Foo...
                case GroovyTokenTypes.LABELED_ARG: // myMethod(name:"Jez")
                case GroovyTokenTypes.LABELED_STAT: // foo:x=1                        
                case GroovyTokenTypes.LAND: // true && false
                case GroovyTokenTypes.LE: // a <= b
                case GroovyTokenTypes.LITERAL_as: // foo as Bar
                case GroovyTokenTypes.LITERAL_in: // if (i in myList) ...
                case GroovyTokenTypes.LOR: // true && false
                case GroovyTokenTypes.LT: // a < b
                case GroovyTokenTypes.MEMBER_POINTER: // this.&foo()
                case GroovyTokenTypes.MOD: //  4 % 3
                case GroovyTokenTypes.MINUS: // 1 - 1
                case GroovyTokenTypes.OPTIONAL_DOT: // foo?.bar
                case GroovyTokenTypes.PACKAGE_DEF:
                case GroovyTokenTypes.PLUS: // 1 + 1
                case GroovyTokenTypes.RANGE_EXCLUSIVE: // [1..<10]
                case GroovyTokenTypes.RANGE_INCLUSIVE: // [1..10]
                case GroovyTokenTypes.SL: // a << b
                case GroovyTokenTypes.SPREAD_DOT: // foo*.bar
                case GroovyTokenTypes.SR: // a >> b
                case GroovyTokenTypes.STAR: // a * b   or    import foo.*
                case GroovyTokenTypes.STAR_STAR: // x ** 3
                case GroovyTokenTypes.TRAIT_DEF: // trait Foo...
                    accept_FirstChild_v_RestOfTheChildren(t);
                    break;

                case GroovyTokenTypes.CTOR_CALL:
                case GroovyTokenTypes.METHOD_CALL:
                    if (t.getNumberOfChildren() == 2 && t.childAt(1) != null && t.childAt(1).getType() == GroovyTokenTypes.CLOSABLE_BLOCK) {
                        // myMethod {...
                        accept_FirstChild_v_SecondChild(t);
                    } else {
                        GroovySourceAST lastChild = t.childAt(t.getNumberOfChildren() -1);
                        if (lastChild != null && lastChild.getType() == GroovyTokenTypes.CLOSABLE_BLOCK) {
                            // myMethod(a,b) {...
                            accept_FirstChild_v_RestOfTheChildren_v_LastChild(t);
                        } else {
                            // myMethod(a,b)
                            accept_FirstChild_v_RestOfTheChildren_v(t);
                        }
                    }
                    break;

                case GroovyTokenTypes.LITERAL_while:
//deprecated                case GroovyTokenTypes.LITERAL_with:
                case GroovyTokenTypes.TYPECAST: // (String)itr.next()
                    accept_v_FirstChildsFirstChild_v_RestOfTheChildren(t);
                    break;

                case GroovyTokenTypes.LITERAL_if: // if (grandchild) {child1} else {child2} ...
                    accept_v_FirstChildsFirstChild_v_Child2_Child3_v_Child4_v___v_LastChild(t);
                    break;

                case GroovyTokenTypes.CLOSABLE_BLOCK: // [1,2,3].each {foo(it)}  <-- Closure
                    if (t.childAt(0) != null && t.childAt(0).getType() == GroovyTokenTypes.IMPLICIT_PARAMETERS) {
                        accept_v_AllChildren_v(t);
                    } else {
                        accept_v_FirstChild_v_RestOfTheChildren_v(t);
                    }
                    break;

                case GroovyTokenTypes.FOR_IN_ITERABLE:
                case GroovyTokenTypes.LITERAL_for:
                case GroovyTokenTypes.LITERAL_new:
                case GroovyTokenTypes.LITERAL_switch:
                    accept_v_FirstChild_v_RestOfTheChildren_v(t);
                    break;
 
                case GroovyTokenTypes.ANNOTATIONS: // just like modifiers but for package/enum declarations
                case GroovyTokenTypes.LITERAL_assert:
                case GroovyTokenTypes.LITERAL_catch:
                case GroovyTokenTypes.LITERAL_synchronized:
                case GroovyTokenTypes.LITERAL_try:
                case GroovyTokenTypes.MODIFIERS:
                    accept_v_FirstChild_v_RestOfTheChildren(t);
                    break;
                    
                case GroovyTokenTypes.WILDCARD_TYPE:
                    accept_v_Siblings_v(t);
                    break;

                default:
                    accept_v_FirstChild_v(t);
                    break;
            }
            pop();
        }
    }
}
