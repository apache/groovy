/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovy.swing.factory

import java.awt.Color
import javax.swing.BorderFactory

/**
 * accepts no value
 * accepts attributes:<br />
 * none <br />
 * highlight: java.awt.Color, shadow: java.awt.Color<br />
 * highlightOuter: java.awt.Color, highlightInner: java.awt.Color, shadowOuter: java.awt.Color, shadowInner: java.awt.Color<br />
 *
 */
class BevelBorderFactory extends SwingBorderFactory {

    final int type;

    public BevelBorderFactory(int newType) {
        type = newType;
    }

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