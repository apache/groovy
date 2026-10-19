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
 * {@code width} and {@code height} attributes are consumed here. A
 * {@code root} attribute supplies the scene's root and nested nodes are added
 * to it. Otherwise nested nodes are collected in a {@code Group}, which stays
 * the scene's root unless the only nested node is a {@code Parent}, in which
 * case that node is promoted to be the root once the scene is complete. That
 * choice cannot be made earlier, so while the body is still being built the
 * scene's root is the placeholder group.
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
        if (child instanceof Node) {
            Parent root = ((Scene) parent).root
            if (root instanceof Group || root instanceof Pane) {
                root.children.add(child)
                return
            }
            throw new IllegalArgumentException("The scene has a ${root.getClass().simpleName} root; nest further nodes inside it")
        }
        FXChildren.attach(parent, child)
    }

    @Override
    void onNodeCompleted(FactoryBuilderSupport builder, Object parent, Object node) {
        Scene scene = (Scene) node
        if (scene.properties.remove(PLACEHOLDER_ROOT)) {
            Group placeholder = (Group) scene.root
            if (placeholder.children.size() == 1 && placeholder.children[0] instanceof Parent) {
                // a node cannot have two parents, so detach it from the group before it becomes the root
                Parent root = (Parent) placeholder.children.remove(0)
                scene.root = root
            }
        }
    }
}
