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
package org.codehaus.groovy.antlr.java;

import org.codehaus.groovy.antlr.GroovySourceAST;
import org.codehaus.groovy.antlr.treewalker.VisitorAdapter;

import java.util.Stack;

/** This class mutates the Java AST, whilst it is still a Java AST, in readiness for conversion to Groovy, yippee-ky-a ! */
@Deprecated
public class PreJava2GroovyConverter extends VisitorAdapter{
    private final Stack stack;

    public PreJava2GroovyConverter(String[] tokenNames) {
        this.stack = new Stack();
    }

    public void visitDefault(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            if (t.getType() == JavaTokenTypes.LITERAL_do) {
                visitJavaLiteralDo(t);
            } else if (t.getType() == JavaTokenTypes.ARRAY_INIT) {
                visitJavaArrayInit(t);
            }
        }
    }

    private void visitJavaLiteralDo(GroovySourceAST t) {
        // todo - incomplete, as body of do...while... should be executed at least once, which this doesn't provide.
        swapTwoChildren(t);
    }

    /**
     * Handle Arrays. Examples:
     *
     * <pre>
     * String[] myArray = new String[] {"a","b","c"};
     *
     * becomes
     *
     * String[] myArray = ["a", "b", "c"]
     *
     * ---
     *
     * To convert node (t) and surrounding nodes into the right structure for List Constructor
     *
     * (a) java/EXPR
     *  |
     *  +- (b) java/new
     *      |
     *      + (t) java/ARRAY_INIT
     *
     *  becomes
     *
     * (a) groovy/LIST_CONSTRUCTOR (via ARRAY_INIT as temporary marker type)
     *  |
     *  +- (t) groovy/ELIST
     *
     *  * note: node (b) is thrown away...
     * </pre>
     */
    private void visitJavaArrayInit(GroovySourceAST t) {
        // given that we might have a grandParent...
        if (stack.size() > 2) {
            GroovySourceAST grandParent = getGrandParentNode();
            if (grandParent.getType() == JavaTokenTypes.EXPR) {
                grandParent.setType(JavaTokenTypes.ARRAY_INIT); //set type as indicator for Java2GroovyConvertor to turn into LIST_CONSTRUCTOR
                grandParent.setFirstChild(t);
                t.setType(JavaTokenTypes.ELIST);
            }
        }
    }

    /** To swap two children of node t...
     *
     *<pre>
     *   (t)
     *    |
     *    |
     *   (a) -- (b)
     *
     * t.down = firstNode
     * a.right = b
     * b.right = null
     *</pre>
     * becomes
     *<pre>
     *   (t)
     *    |
     *    |
     *   (b) -- (a)
     *
     * t.down = b
     * a.right = null
     * b.right = a
     *</pre>
     *
     * todo - build API of basic tree mutations like this method.
     */
    public void swapTwoChildren(GroovySourceAST t) {
        // this swaps the two child nodes, see javadoc above for explanation of implementation
        GroovySourceAST a = (GroovySourceAST) t.getFirstChild();
        GroovySourceAST b = (GroovySourceAST) a.getNextSibling();

        t.setFirstChild(b);
        a.setNextSibling(null);
        b.setNextSibling(a);
    }




    public void push(GroovySourceAST t) {
        stack.push(t);
    }
    public GroovySourceAST pop() {
        if (!stack.empty()) {
            return (GroovySourceAST) stack.pop();
        }
        return null;
    }

    private GroovySourceAST getGrandParentNode() {
        Object currentNode = stack.pop();
        Object parentNode = stack.pop();
        Object grandParentNode = stack.peek();
        stack.push(parentNode);
        stack.push(currentNode);
        return (GroovySourceAST) grandParentNode;
    }
}
