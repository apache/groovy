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
package org.codehaus.groovy.tools.groovydoc;

import org.codehaus.groovy.groovydoc.GroovyClassDoc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * GROOVY-11942: builds the inheritance/extension trees rendered by the
 * {@code overview-tree.html} and {@code package-tree.html} templates.
 *
 * <p>Produces two separate trees:
 * <ul>
 *   <li>The <em>class tree</em> is rooted at {@code java.lang.Object} and
 *       built by walking each class's {@code getParentClasses()} chain,
 *       so external ancestors (like {@code java.lang.Throwable}) appear
 *       as non-link nodes joining the documented classes to Object.</li>
 *   <li>The <em>interface tree</em> lists top-level interfaces and traits
 *       whose parent interfaces are not themselves documented in this
 *       tree, with children determined by direct {@code extends}
 *       relationships between documented interfaces. Interfaces with
 *       multiple parents may appear under more than one root.</li>
 * </ul>
 *
 * <p>Traits are treated as interfaces for the purposes of grouping,
 * matching their JVM representation, but retain their {@code isTrait()}
 * flag so templates can render them with a distinct label.
 */
public final class ClassTree {

    private ClassTree() {}

    /**
     * A node in an inheritance/extension tree.
     *
     * <p>If {@link #classDoc} is non-null the node represents a documented
     * type (rendered as a link); otherwise it represents an external type
     * joining documented children to a common ancestor (rendered as plain
     * text using {@link #qualifiedName}).
     */
    public static final class Node {
        private final String qualifiedName;
        private final GroovyClassDoc classDoc;
        private final Map<String, Node> children = new LinkedHashMap<>();

        Node(String qualifiedName, GroovyClassDoc classDoc) {
            this.qualifiedName = qualifiedName;
            this.classDoc = classDoc;
        }

        public String getQualifiedName() { return qualifiedName; }
        public GroovyClassDoc getClassDoc() { return classDoc; }

        /** Children in insertion order; callers typically sort before rendering. */
        public List<Node> getChildren() {
            List<Node> sorted = new ArrayList<>(children.values());
            sorted.sort(Comparator.comparing(Node::getQualifiedName));
            return sorted;
        }
    }

    /**
     * Build the class tree for the given documented classes. The returned
     * root is a synthetic node for {@code java.lang.Object}; traits,
     * interfaces, and annotation types are excluded (they belong in the
     * interface tree).
     */
    public static Node buildClassTree(GroovyClassDoc[] classes) {
        Node root = new Node("java.lang.Object", null);
        for (GroovyClassDoc c : classes) {
            if (isInterfaceLike(c)) continue;
            if (!(c instanceof SimpleGroovyClassDoc)) continue;
            List<GroovyClassDoc> chain = ((SimpleGroovyClassDoc) c).getParentClasses();
            Node cursor = root;
            for (GroovyClassDoc ancestor : chain) {
                String qn = ancestor.qualifiedTypeName();
                if ("java.lang.Object".equals(qn)) continue; // root itself
                Node child = cursor.children.get(qn);
                if (child == null) {
                    GroovyClassDoc docForLink = ancestor == c ? c :
                            (ancestor instanceof SimpleGroovyClassDoc ? ancestor : null);
                    child = new Node(qn, docForLink);
                    cursor.children.put(qn, child);
                } else if (child.classDoc == null && ancestor instanceof SimpleGroovyClassDoc) {
                    // upgrade a previously-external node to a linkable one
                    Node upgraded = new Node(qn, ancestor);
                    upgraded.children.putAll(child.children);
                    cursor.children.put(qn, upgraded);
                    child = upgraded;
                }
                cursor = child;
            }
        }
        return root;
    }

    /**
     * Build the interface tree for the given documented classes.
     * Returns a synthetic top-level node whose children are the top-level
     * interfaces/traits — i.e. those whose super-interfaces aren't
     * themselves present in the input set.
     */
    public static Node buildInterfaceTree(GroovyClassDoc[] classes) {
        Map<String, GroovyClassDoc> docByName = new LinkedHashMap<>();
        for (GroovyClassDoc c : classes) {
            if (isInterfaceLike(c)) docByName.put(c.qualifiedTypeName(), c);
        }
        Node root = new Node("", null);
        // Children of a given interface = documented interfaces that directly extend it.
        Map<String, Map<String, Node>> childrenOf = new TreeMap<>();
        Map<String, Node> nodesByName = new LinkedHashMap<>();
        for (Map.Entry<String, GroovyClassDoc> e : docByName.entrySet()) {
            nodesByName.put(e.getKey(), new Node(e.getKey(), e.getValue()));
        }
        for (Map.Entry<String, GroovyClassDoc> e : docByName.entrySet()) {
            GroovyClassDoc c = e.getValue();
            GroovyClassDoc[] supers = c.interfaces();
            boolean attached = false;
            if (supers != null) {
                for (GroovyClassDoc s : supers) {
                    String sn = s == null ? null : s.qualifiedTypeName();
                    if (sn != null && docByName.containsKey(sn)) {
                        childrenOf.computeIfAbsent(sn, k -> new LinkedHashMap<>())
                                .put(e.getKey(), nodesByName.get(e.getKey()));
                        attached = true;
                    }
                }
            }
            if (!attached) {
                root.children.put(e.getKey(), nodesByName.get(e.getKey()));
            }
        }
        // Wire documented parent/child relationships.
        for (Map.Entry<String, Map<String, Node>> e : childrenOf.entrySet()) {
            Node parent = nodesByName.get(e.getKey());
            if (parent != null) parent.children.putAll(e.getValue());
        }
        return root;
    }

    private static boolean isInterfaceLike(GroovyClassDoc c) {
        if (c == null) return false;
        if (c.isInterface() || c.isAnnotationType()) return true;
        return c instanceof SimpleGroovyDoc && ((SimpleGroovyDoc) c).isTrait();
    }

    /**
     * Helper used by templates: list of children of a node, sorted by
     * qualified name. Returns an empty list when {@code node} is {@code null}.
     */
    public static List<Node> sortedChildren(Node node) {
        return node == null ? Collections.emptyList() : node.getChildren();
    }
}
