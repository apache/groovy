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
package org.codehaus.groovy.ast.stmt;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Collections;

/**
 * Base class for any statement.
 */
public class Statement extends ASTNode {

    private List<String> statementLabels;

    public /*@Nullable*/ List<String> getStatementLabels() {
        return statementLabels;
    }

    @Deprecated
    public /*@Nullable*/ String getStatementLabel() {
        // last label by default which is added first by APP
        return statementLabels == null ? null : statementLabels.get(0);
    }

    // TODO: @Deprecated
    public void setStatementLabel(final String label) {
        if (label != null) addStatementLabel(label);
    }

    public void addStatementLabel(final String label) {
        if (statementLabels == null) statementLabels = new LinkedList<>();
        statementLabels.add(Objects.requireNonNull(label));
    }

    public void copyStatementLabels(final Statement that) {
        Optional.ofNullable(that.getStatementLabels()).ifPresent(labels -> {
            labels.forEach(this::addStatementLabel);
        });
    }

    //--------------------------------------------------------------------------
    // Statement-level annotation support (Groovy-only; stored in node metadata)

    private static final Object STATEMENT_ANNOTATIONS_KEY = "_statementAnnotations_";

    /**
     * Returns the list of statement-level annotations attached to this statement.
     * These are Groovy-only source-retention annotations that do not appear at the
     * JVM level; they are processed by registered {@link org.codehaus.groovy.transform.ASTTransformation}s.
     *
     * @return an unmodifiable view of the annotations list, never {@code null}
     * @since 6.0.0
     */
    @SuppressWarnings("unchecked")
    public List<AnnotationNode> getStatementAnnotations() {
        List<AnnotationNode> annotations = getNodeMetaData(STATEMENT_ANNOTATIONS_KEY);
        return annotations != null ? Collections.unmodifiableList(annotations) : Collections.emptyList();
    }

    /**
     * Attaches a statement-level annotation to this statement.
     *
     * @param annotation the annotation to attach
     * @since 6.0.0
     */
    @SuppressWarnings("unchecked")
    public void addStatementAnnotation(final AnnotationNode annotation) {
        List<AnnotationNode> annotations = getNodeMetaData(STATEMENT_ANNOTATIONS_KEY);
        if (annotations == null) {
            annotations = new ArrayList<>();
            setNodeMetaData(STATEMENT_ANNOTATIONS_KEY, annotations);
        }
        annotations.add(Objects.requireNonNull(annotation));
    }

    //--------------------------------------------------------------------------

    public boolean isEmpty() {
        return false;
    }
}
