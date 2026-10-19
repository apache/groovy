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

import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.layout.Pane

/**
 * Creates scenes. A scene's size can only be given at construction, so the
 * {@code width} and {@code height} attributes are consumed here. The first
 * nested {@code Parent} becomes the scene's root; when the first child is
 * not a {@code Parent}, or further children follow, they are added to a
 * {@code Group} root.
 */
class SceneFactory extends NodeFactory {

    private static final String PLACEHOLDER_ROOT = 'groovyfx.placeholderRoot'

    SceneFactory() {
        super(Scene)
    }

    @Override
    Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        if (value instanceof Scene) return value
        if (value != null) {
            throw new IllegalArgumentException("The value of 'scene' must be a Scene. Found: ${value.getClass().name}")
        }
        Object root = attributes.remove('root')
        boolean placeholder = root == null
        if (placeholder) {
            root = new Group()
        } else if (!(root instanceof Parent)) {
            throw new IllegalArgumentException("The root of a scene must be a Parent. Found: ${root.getClass().name}")
        }
        Object width = attributes.remove('width')
        Object height = attributes.remove('height')
        Scene scene = width != null && height != null
            ? new Scene((Parent) root, (width as Number).doubleValue(), (height as Number).doubleValue())
            : new Scene((Parent) root)
        if (placeholder) {
            scene.properties[PLACEHOLDER_ROOT] = true
        }
        scene
    }

    @Override
    void setChild(FactoryBuilderSupport builder, Object parent, Object child) {
        Scene scene = (Scene) parent
        if (child instanceof Parent && scene.properties.remove(PLACEHOLDER_ROOT)) {
            scene.root = child
            return
        }
        if (child instanceof Node) {
            Parent root = scene.root
            if (root instanceof Group || root instanceof Pane) {
                root.children.add(child)
                return
            }
            throw new IllegalArgumentException("The scene already has a ${root.getClass().simpleName} root; nest further nodes inside it")
        }
        FXChildren.attach(parent, child)
    }

    @Override
    void onNodeCompleted(FactoryBuilderSupport builder, Object parent, Object node) {
        ((Scene) node).properties.remove(PLACEHOLDER_ROOT)
    }
}
