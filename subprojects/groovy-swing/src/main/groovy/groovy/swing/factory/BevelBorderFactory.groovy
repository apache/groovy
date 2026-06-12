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
package groovy.swing.factory

import javax.swing.*
import java.awt.*

/**
 * accepts no value<br>
 * accepts attributes:<br>
 * none <br>
 * highlight: java.awt.Color, shadow: java.awt.Color<br>
 * highlightOuter: java.awt.Color, highlightInner: java.awt.Color, shadowOuter: java.awt.Color, shadowInner: java.awt.Color
 */
class BevelBorderFactory extends SwingBorderFactory {

    /**
     * Bevel type used when creating borders.
     */
    final int type;

    /**
     * Creates a new factory or helper type for beve\1 \2orde\1 \2actory
     *
     * @param newType the border type constant
     */
    public BevelBorderFactory(int newType) {
        type = newType;
    }

    /**
     * Creates the node handled by this factory.
     *
     * @param builder the factory builder
     * @param name the node name
     * @param value the node value
     * @param attributes the node attributes
     * @return the created or reused node
     */
    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        builder.context.applyBorderToParent = attributes.remove('parent')

        // no if-else-if chain so that we can have one attribute failure block
        if (attributes.containsKey("highlight")) {
            Color highlight = attributes.remove("highlight")
            Color shadow = attributes.remove("shadow")
            if (highlight && shadow && !attributes) {
                return BorderFactory.createBevelBorder(type, highlight, shadow);
            }
        }
        if (attributes.containsKey("highlightOuter")) {
            Color highlightOuter = attributes.remove("highlightOuter")
            Color highlightInner = attributes.remove("highlightInner")
            Color shadowOuter = attributes.remove("shadowOuter")
            Color shadowInner = attributes.remove("shadowInner")
            if (highlightOuter && highlightInner && shadowOuter && shadowInner && !attributes) {
                return BorderFactory.createBevelBorder(type, highlightOuter, highlightInner, shadowOuter, shadowInner);
            }
        }
        if (attributes) {
            throw new RuntimeException("$name only accepts no attributes, or highlight: and shadow: attributes, or highlightOuter: and highlightInner: and shadowOuter: and shadowInner: attributes")
        }
        return BorderFactory.createBevelBorder(type);
    }
}
