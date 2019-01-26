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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TreeContext {
    private enum TreeContextKey {
        expression_replacement
    }
    final TreeContext parent;
    final ASTNode node;
    final List<TreeContext> siblings = new LinkedList<>();
    final List<TreeContextAction> onPopHandlers = new LinkedList<>();
    final Map<Object, List<?>> userdata = MapWithDefault.newInstance(
            new HashMap<>(),
            new Closure(this) {
                private static final long serialVersionUID = -4694773031569936343L;

                public Object doCall(Object key) {
                    return new LinkedList<>();
                }
            }
    );

    TreeContext(final TreeContext parent, final ASTNode node) {
        this.parent = parent;
        this.node = node;
        if (parent!=null) {
            parent.siblings.add(this);
        }
    }

    public Map<?, List<?>> getUserdata() {
        return userdata;
    }

    public void putUserdata(Object key, Object value) {
        ((List)userdata.get(key)).add(value);
    }

    public List<?> getUserdata(Object key) {
        return getUserdata(key,true);
    }

    public List<?> getUserdata(Object key, boolean searchParent) {
        if (userdata.containsKey(key)) {
            return userdata.get(key);
        } else if (parent!=null && searchParent) {
            return parent.getUserdata(key, true);
        }
        return null;
    }

    public TreeContext getParent() { return parent; }
    public ASTNode getNode() { return node; }

    public TreeContext fork(ASTNode node) {
        return new TreeContext(this, node);
    }

    public boolean matches(ASTNodePredicate predicate) {
        return predicate.matches(node);
    }

    public boolean matches(@DelegatesTo(value=ASTNode.class, strategy=Closure.DELEGATE_FIRST) Closure<Boolean> predicate) {
        return MatcherUtils.cloneWithDelegate(predicate, node).call();
    }

    public List<TreeContext> getSiblings() {
        return Collections.unmodifiableList(siblings);
    }

    public List<TreeContextAction> getOnPopHandlers() {
        return Collections.unmodifiableList(onPopHandlers);
    }

    public void afterVisit(TreeContextAction action) {
        onPopHandlers.add(action);
    }

    public void afterVisit(@DelegatesTo(value=TreeContext.class, strategy=Closure.DELEGATE_FIRST) Closure<?> action) {
        Closure<?> clone = MatcherUtils.cloneWithDelegate(action, this);
        afterVisit(DefaultGroovyMethods.asType(clone, TreeContextAction.class));
    }

    public void setReplacement(Expression replacement) {
        userdata.put(TreeContextKey.expression_replacement, Collections.singletonList(replacement));
    }

    public Expression getReplacement() {
        List<?> list = userdata.get(TreeContextKey.expression_replacement);
        if (list.size()==1) {
            return (Expression) list.get(0);
        }
        return null;
    }

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
        //sb.append(", siblings=").append(siblings);
        sb.append('}');
        return sb.toString();
    }

    private String dumpNode() {
        return node!=null?node.getClass().getSimpleName():"undefined";
    }
}
