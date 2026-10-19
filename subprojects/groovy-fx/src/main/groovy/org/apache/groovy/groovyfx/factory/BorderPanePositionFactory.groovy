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
package org.apache.groovy.groovyfx.factory

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.BorderPane
import org.codehaus.groovy.runtime.InvokerHelper

/**
 * Places a nested node into one position of the enclosing {@code BorderPane}:
 * <pre>
 * borderPane {
 *     top { label('Header') }
 *     center(margin: 10) { textArea() }
 * }
 * </pre>
 * The {@code alignment} and {@code margin} attributes become the child's
 * {@code BorderPane} constraints.
 */
class BorderPanePositionFactory extends AbstractFactory {

    /**
     * Placeholder node produced for a position; it is replaced by the nested node.
     */
    static class Position {
        BorderPane pane
        String name
        Object alignment
        Object margin
    }

    final String position

    BorderPanePositionFactory(String position) {
        this.position = position
    }

    @Override
    Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        Object container = builder.current
        if (!(container instanceof BorderPane)) {
            throw new IllegalArgumentException("'${name}' must be nested directly inside a borderPane")
        }
        new Position(pane: (BorderPane) container, name: position)
    }

    @Override
    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, Object node, Map attributes) {
        Position holder = (Position) node
        holder.alignment = attributes.remove('alignment')
        holder.margin = attributes.remove('margin')
        if (attributes) {
            throw new IllegalArgumentException("'${holder.name}' accepts only alignment and margin attributes. Found: ${attributes.keySet()}")
        }
        false
    }

    @Override
    void setChild(FactoryBuilderSupport builder, Object parent, Object child) {
        if (!(child instanceof Node)) return
        Position holder = (Position) parent
        Node node = child
        InvokerHelper.setProperty(holder.pane, holder.name, node)
        if (holder.alignment != null) {
            Object alignment = holder.alignment
            BorderPane.setAlignment(node, alignment instanceof Pos ? alignment : Pos.valueOf(alignment.toString().toUpperCase(Locale.ROOT)))
        }
        if (holder.margin != null) {
            BorderPane.setMargin(node, FXAttributes.toInsets(holder.margin))
        }
    }
}
