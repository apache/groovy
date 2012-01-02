/*
 * Copyright 2007-2008 the original author or authors.
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

import javax.swing.RootPaneContainer

import java.awt.Insets
import java.awt.GridBagConstraints
import java.awt.Container
import java.awt.GridBagLayout
import java.awt.Component

public class GridBagFactory extends LayoutFactory {

    public GridBagFactory() {
        super(GridBagLayout, true)
    }

    public void addLayoutProperties(context) {
        addLayoutProperties(context, GridBagConstraints)
    }



    public static void processGridBagConstraintsAttributes(FactoryBuilderSupport builder, Object node, Map attributes) {
        // if we are not a component, don't do anything
        if (!(node instanceof Component)) {
            return
        }
        // an explicit constraints means don't do anything
        def constraintsAttr = builder?.context?.getAt(LayoutFactory.DELEGATE_PROPERTY_CONSTRAINT) ?: LayoutFactory.DEFAULT_DELEGATE_PROPERTY_CONSTRAINT
        if (attributes.containsKey(constraintsAttr)) {
            return
        }
        // if there are cached constraints, don't do it again, we need to be idempotent...
        if (builder.getContext().containsKey("constraints")) {
            return
        }


        // next, check to be sure we are in an appropriate container
        Object parent = builder.getCurrent()
        if (parent instanceof RootPaneContainer) {
            if (!(((RootPaneContainer)parent).getContentPane().getLayout() instanceof GridBagLayout)) {
                return
            }
        } else if (parent instanceof Container) {
            if (!(((Container)parent).getLayout() instanceof GridBagLayout)) {
                return
            }
        } else {
            return
        }

        // next, look for matching attrs.
        boolean anyAttrs = false
        GridBagConstraints gbc = new GridBagConstraints()
        Object o

        o = extractAttribute(attributes, "gridx", Number)
        if (o != null) {
            gbc.gridx = o
            anyAttrs = true
        }
        o = extractAttribute(attributes, "gridy", Number)
        if (o != null) {
            gbc.gridy = o
            anyAttrs = true
        }
        o = extractAttribute(attributes, "gridwidth", Number)
        if (o != null) {
            gbc.gridwidth = o
            anyAttrs = true
        }
        o = extractAttribute(attributes, "gridheight", Number)
        if (o != null) {
            gbc.gridheight = o
            anyAttrs = true
        }

        o = extractAttribute(attributes, "weightx", Number)
        if (o != null) {
            gbc.weightx = o
            anyAttrs = true
        }
        o = extractAttribute(attributes, "weighty", Number)
        if (o != null) {
            gbc.weighty = o
            anyAttrs = true
        }

        o = extractAttribute(attributes, "anchor", Number)
        if (o != null) {
            gbc.anchor = o
            anyAttrs = true
        }
        o = extractAttribute(attributes, "fill", Number)
        if (o != null) {
            gbc.fill = o
            anyAttrs = true
        }

        o = extractAttribute(attributes, "insets", Object)
        if (o != null) {
            gbc.insets = o as Insets
            anyAttrs = true
        }

        o = extractAttribute(attributes, "ipadx", Number)
        if (o != null) {
            gbc.ipadx = o
            anyAttrs = true
        }
        o = extractAttribute(attributes, "ipady", Number)
        if (o != null) {
            gbc.ipady = o
            anyAttrs = true
        }

        // if we find any attrs, stash the constraints
        if (anyAttrs) {
            builder.getContext().put("constraints", gbc)
        }
    }

    /**
     * @return null if not found.
     * null as a 'not found' works because all attrs except insets
     * are primitive types, and insets will crash if set to null
     */
    static Object extractAttribute(Map attrs, String name, Class type) {
        if (attrs.containsKey(name)) {
            Object o = attrs.get(name)
            if ((o != null) && type.isAssignableFrom(type)) {
                attrs.remove(name)
                return o
            } else {
                return null
            }
        } else {
            return null
        }
    }
}
