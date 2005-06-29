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
    public AST process(AST t) {
        setUp();

        // gather and sort all unvisited AST nodes
        unvisitedNodes = new ArrayList();
        traverse((GroovySourceAST)t);
        Collections.sort(unvisitedNodes);

        // process each node in turn
        accept((GroovySourceAST)t);

        tearDown();
        return null;
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

    protected void accept(GroovySourceAST currentNode, boolean followSiblings) {
        if (currentNode != null && unvisitedNodes != null && unvisitedNodes.size() > 0) {
            GroovySourceAST t = currentNode;

            if (!(unvisitedNodes.contains(currentNode))) {
                return;
            }

            switch (t.getType()) {
                case GroovyTokenTypes.EXPR:
                case GroovyTokenTypes.IMPORT:
                case GroovyTokenTypes.PACKAGE_DEF:
                case GroovyTokenTypes.VARIABLE_DEF:
                    accept_v_AllChildren_v_Siblings(t, followSiblings);
                    break;

                case GroovyTokenTypes.ELIST:
                case GroovyTokenTypes.STRING_CONSTRUCTOR:
                    accept_v_FirstChild_v_SecondChild_v___LastChild_v(t);
                    break;

                case GroovyTokenTypes.METHOD_DEF:
                case GroovyTokenTypes.PARAMETER_DEF:
                case GroovyTokenTypes.SLIST:
                    accept_v_AllChildren_v(t);
                    break;

                case GroovyTokenTypes.EQUAL:
                case GroovyTokenTypes.ASSIGN:
                    if (t.childAt(1) != null) {
                        accept_FirstChild_v_RestOfTheChildren(t);
                    } else {
                        accept_v_FirstChild_v_RestOfTheChildren(t);
                    }
                    break;

                case GroovyTokenTypes.CLASS_DEF:
                case GroovyTokenTypes.DOT:
                case GroovyTokenTypes.METHOD_CALL:
                case GroovyTokenTypes.STAR:
                    accept_FirstChild_v_RestOfTheChildren(t);
                    break;

                case GroovyTokenTypes.LITERAL_case:
                case GroovyTokenTypes.LITERAL_while:
                    accept_v_FirstChildsFirstChild_v_RestOfTheChildren(t);
                    break;

                case GroovyTokenTypes.LITERAL_if:
                    accept_v_FirstChildsFirstChild_v_Child2_Child3_v_Child4_v___v_LastChild(t);

                    break;

                case GroovyTokenTypes.LITERAL_catch:
                case GroovyTokenTypes.LITERAL_new:
                case GroovyTokenTypes.LITERAL_try:
                case GroovyTokenTypes.TYPECAST:
                    accept_v_FirstChild_v_RestOfTheChildren(t);
                    break;

                default:
                    accept_v_FirstChild_v(t);
                    break;
            }
        }
    }
}
