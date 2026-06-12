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
package org.codehaus.groovy.macro.matcher;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.lang.MapWithDefault;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.io.Serial;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Holds the current AST node together with its traversal context.
 *
 * @since 2.5.0
 */
public class TreeContext {
    private enum TreeContextKey {
        expression_replacement
    }
    /** Parent traversal context. */
    final TreeContext parent;
    /** AST node represented by this context. */
    final ASTNode node;
    /** Child contexts created from this context. */
    final List<TreeContext> siblings = new LinkedList<>();
    /** Actions invoked when this context is popped. */
    final List<TreeContextAction> onPopHandlers = new LinkedList<>();
    /** User data stored against this context. */
    final Map<Object, List<Object>> userdata = MapWithDefault.newInstance(
            new HashMap<Object, List<Object>>(),
            new Closure<List<Object>>(this) {
                @Serial
                private static final long serialVersionUID = -4694773031569936343L;

                /**
                 * Creates the default list for a user-data key.
                 *
                 * @param key the user-data key
                 * @return a new mutable value list
                 */
                public Object doCall(Object key) {
                    return new LinkedList<Object>();
                }
            }
    );

    /**
     * Creates a traversal context for the supplied node.
     */
    TreeContext(final TreeContext parent, final ASTNode node) {
        this.parent = parent;
        this.node = node;
        if (parent!=null) {
            parent.siblings.add(this);
        }
    }

    /**
     * Returns the user data attached to this context.
     *
     * @return the user data map
     */
    public Map<Object, List<Object>> getUserdata() {
        return userdata;
    }

    /**
     * Appends a user data value for the supplied key.
     *
     * @param key the user data key
     * @param value the value to add
     */
    @SuppressWarnings("unchecked")
    public void putUserdata(Object key, Object value) {
        ((List)userdata.get(key)).add(value);
    }

    /**
     * Returns user data for the supplied key, searching parent contexts by default.
     *
     * @param key the user data key
     * @return the associated values, or {@code null}
     */
    public List<Object> getUserdata(Object key) {
        return getUserdata(key,true);
    }

    /**
     * Returns user data for the supplied key.
     *
     * @param key the user data key
     * @param searchParent whether to search parent contexts
     * @return the associated values, or {@code null}
     */
    public List<Object> getUserdata(Object key, boolean searchParent) {
        if (userdata.containsKey(key)) {
            return userdata.get(key);
        } else if (parent!=null && searchParent) {
            return parent.getUserdata(key, true);
        }
        return null;
    }

    /**
     * Returns the parent traversal context.
     *
     * @return the parent context, or {@code null}
     */
    public TreeContext getParent() { return parent; }
    /**
     * Returns the AST node represented by this context.
     *
     * @return the current AST node
     */
    public ASTNode getNode() { return node; }

    /**
     * Creates a child context for the supplied node.
     *
     * @param node the child node
     * @return the child context
     */
    public TreeContext fork(ASTNode node) {
        return new TreeContext(this, node);
    }

    /**
     * Tests the current node with the supplied predicate.
     *
     * @param predicate the predicate to apply
     * @return {@code true} if the predicate matches
     */
    public boolean matches(ASTNodePredicate predicate) {
        return predicate.matches(node);
    }

    /**
     * Tests the current node with the supplied closure predicate.
     *
     * @param predicate the predicate to apply
     * @return {@code true} if the predicate matches
     */
    public boolean matches(@DelegatesTo(value=ASTNode.class, strategy=Closure.DELEGATE_FIRST) Closure<Boolean> predicate) {
        return MatcherUtils.cloneWithDelegate(predicate, node).call();
    }

    /**
     * Returns the child contexts created from this node.
     *
     * @return the sibling list
     */
    public List<TreeContext> getSiblings() {
        return Collections.unmodifiableList(siblings);
    }

    /**
     * Returns handlers invoked when this context is popped.
     *
     * @return the registered handlers
     */
    public List<TreeContextAction> getOnPopHandlers() {
        return Collections.unmodifiableList(onPopHandlers);
    }

    /**
     * Registers a handler to invoke after the node is visited.
     *
     * @param action the handler to register
     */
    public void afterVisit(TreeContextAction action) {
        onPopHandlers.add(action);
    }

    /**
     * Registers a closure to invoke after the node is visited.
     *
     * @param action the handler to register
     */
    public void afterVisit(@DelegatesTo(value=TreeContext.class, strategy=Closure.DELEGATE_FIRST) Closure<?> action) {
        Closure<?> clone = MatcherUtils.cloneWithDelegate(action, this);
        afterVisit(DefaultGroovyMethods.asType(clone, TreeContextAction.class));
    }

    /**
     * Replaces the current expression node when this context is popped.
     *
     * @param replacement the replacement expression
     */
    public void setReplacement(Expression replacement) {
        userdata.put(TreeContextKey.expression_replacement, Collections.singletonList(replacement));
    }

    /**
     * Returns the replacement expression for this context.
     *
     * @return the replacement expression, or {@code null}
     */
    public Expression getReplacement() {
        List<?> list = userdata.get(TreeContextKey.expression_replacement);
        if (list.size()==1) {
            return (Expression) list.get(0);
        }
        return null;
    }

    /**
     * Returns a string form containing the current node and its parent path.
     *
     * @return the context description
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TreeContext{");
        sb.append("node=").append(dumpNode());
        TreeContext p = parent;
        if (p!=null) {
            sb.append(", path=");
        }
        while (p!=null) {
            sb.append(p.dumpNode());
            sb.append("<-");
            p = p.parent;
        }
        sb.append('}');
        return sb.toString();
    }

    private String dumpNode() {
        return node!=null?node.getClass().getSimpleName():"undefined";
    }
}
