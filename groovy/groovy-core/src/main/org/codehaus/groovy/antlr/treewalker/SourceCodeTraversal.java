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
import java.util.*;
import org.codehaus.groovy.antlr.GroovySourceAST;
import org.codehaus.groovy.antlr.parser.GroovyTokenTypes;

/**
 * A treewalker for the antlr generated AST that attempts to visit the
 * AST nodes in the order needed to generate valid groovy source code.
 *
 * @author <a href="mailto:groovy@ross-rayner.com">Jeremy Rayner</a>
 * @version $Revision$
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
        unvisitedNodes = new ArrayList();
        traverse((GroovySourceAST)t);
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
        if (currentNode != null && unvisitedNodes != null && unvisitedNodes.size() > 0) {
            GroovySourceAST t = currentNode;

            if (!(unvisitedNodes.contains(currentNode))) {
                return;
            }

            switch (t.getType()) {
                case GroovyTokenTypes.QUESTION: // expr?foo:bar
                    accept_FirstChild_v_SecondChild_v_ThirdChild_v(t);
                    break;

                case GroovyTokenTypes.CASE_GROUP: //
                case GroovyTokenTypes.LITERAL_instanceof: // foo instanceof MyType
                    accept_FirstChild_v_SecondChildsChildren_v(t);
                    break;

                case GroovyTokenTypes.ELIST: // a,b,c
                case GroovyTokenTypes.PARAMETERS: // a,b,c
                case GroovyTokenTypes.STRING_CONSTRUCTOR: // "foo${bar}wibble"
                    accept_v_FirstChild_v_SecondChild_v___LastChild_v(t);
                    break;

                case GroovyTokenTypes.INDEX_OP:
                    accept_FirstChild_v_SecondChild_v(t);
                    break;

                case GroovyTokenTypes.EXPR:
                case GroovyTokenTypes.IMPORT:
                case GroovyTokenTypes.PACKAGE_DEF:
                case GroovyTokenTypes.VARIABLE_DEF:
                case GroovyTokenTypes.METHOD_DEF:
                case GroovyTokenTypes.OBJBLOCK: //class Foo {def bar()}  <-- this block
                case GroovyTokenTypes.PARAMETER_DEF:
                case GroovyTokenTypes.SLIST: // list of expressions, variable defs etc
                    accept_v_AllChildren_v(t);
                    break;

                case GroovyTokenTypes.ASSIGN: // a=b
                case GroovyTokenTypes.EQUAL: // a==b
                case GroovyTokenTypes.NOT_EQUAL:
                    if (t.childAt(1) != null) {
                        accept_FirstChild_v_RestOfTheChildren(t);
                    } else {
                        accept_v_FirstChild_v_RestOfTheChildren(t);
                    }
                    break;

                case GroovyTokenTypes.CLASS_DEF: // class Foo...
                case GroovyTokenTypes.CTOR_IDENT: // private Foo() {...
                case GroovyTokenTypes.DOT: // foo.bar
                case GroovyTokenTypes.GT: // a > b
                case GroovyTokenTypes.LABELED_ARG: // myMethod(name:"Jez")
                case GroovyTokenTypes.LAND: // true && false
                case GroovyTokenTypes.LT: // a < b
                case GroovyTokenTypes.MEMBER_POINTER: // this.&foo()
                case GroovyTokenTypes.MINUS:
                case GroovyTokenTypes.PLUS:
                case GroovyTokenTypes.RANGE_EXCLUSIVE:
                case GroovyTokenTypes.RANGE_INCLUSIVE:
                case GroovyTokenTypes.STAR: // a * b   or    import foo.*
                    accept_FirstChild_v_RestOfTheChildren(t);
                    break;

                case GroovyTokenTypes.METHOD_CALL:
                    if (t.getNumberOfChildren() == 2 && t.childAt(1) != null && t.childAt(1).getType() == GroovyTokenTypes.CLOSED_BLOCK ) {
                        // myMethod {...
                        accept_FirstChild_v_SecondChild(t);
                    } else {
                        GroovySourceAST lastChild = t.childAt(t.getNumberOfChildren() -1);
                        if (lastChild != null && lastChild.getType() == GroovyTokenTypes.CLOSED_BLOCK) {
                            // myMethod(a,b) {...
                            accept_FirstChild_v_RestOfTheChildren_v_LastChild(t);
                        } else {
                            // myMethod(a,b)
                            accept_FirstChild_v_RestOfTheChildren_v(t);
                        }
                    }
                    break;

                case GroovyTokenTypes.LITERAL_while:
                case GroovyTokenTypes.TYPECAST: // (String)itr.next()
                    accept_v_FirstChildsFirstChild_v_RestOfTheChildren(t);
                    break;

                case GroovyTokenTypes.LITERAL_if: // if (grandchild) {child1} else {child2} ...
                    accept_v_FirstChildsFirstChild_v_Child2_Child3_v_Child4_v___v_LastChild(t);
                    break;

                case GroovyTokenTypes.CLOSED_BLOCK: // [1,2,3].each {foo(it)}  <-- Closure
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

                case GroovyTokenTypes.LITERAL_catch:
                case GroovyTokenTypes.LITERAL_try:
                    accept_v_FirstChild_v_RestOfTheChildren(t);
                    break;

                default:
                    accept_v_FirstChild_v(t);
                    break;
            }
        }
    }
}
