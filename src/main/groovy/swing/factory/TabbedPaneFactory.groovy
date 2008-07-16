/*
 * Copyright 2003-2007 the original author or authors.
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

import java.awt.Component
import java.awt.Window

class TabbedPaneFactory extends BeanFactory {

    public static final String DELEGATE_PROPERTY_TITLE = "_delegateProperty:title";
    public static final String DEFAULT_DELEGATE_PROPERTY_TITLE = "title";
    public static final String DELEGATE_PROPERTY_TAB_ICON = "_delegateProperty:tabIcon";
    public static final String DEFAULT_DELEGATE_PROPERTY_TAB_ICON = "tabIcon";
    public static final String DELEGATE_PROPERTY_TAB_DISABLED_ICON = "_delegateProperty:tabDisabledIcon";
    public static final String DEFAULT_DELEGATE_PROPERTY_TAB_DISABLED_ICON = "tabDisabledIcon";
    public static final String DELEGATE_PROPERTY_TAB_TOOL_TIP = "_delegateProperty:tabToolTip";
    public static final String DEFAULT_DELEGATE_PROPERTY_TAB_TOOL_TIP = "tabToolTip";
    public static final String DELEGATE_PROPERTY_TAB_FOREGROUND = "_delegateProperty:tabForeground";
    public static final String DEFAULT_DELEGATE_PROPERTY_TAB_FOREGROUND = "tabForeground";
    public static final String DELEGATE_PROPERTY_TAB_BACKGROUND = "_delegateProperty:tabBackground";
    public static final String DEFAULT_DELEGATE_PROPERTY_TAB_BACKGROUND = "tabBackground";
    public static final String DELEGATE_PROPERTY_TAB_ENABLED = "_delegateProperty:tabEnabled";
    public static final String DEFAULT_DELEGATE_PROPERTY_TAB_ENABLED = "tabEnabled";
    public static final String DELEGATE_PROPERTY_TAB_MNEMONIC = "_delegateProperty:tabMnemonic";
    public static final String DEFAULT_DELEGATE_PROPERTY_TAB_MNEMONIC = "tabMnemonic";
    public static final String DELEGATE_PROPERTY_TAB_DISPLAYED_MNEMONIC_INDEX = "_delegateProperty:tabDisplayedMnemonicIndex";
    public static final String DEFAULT_DELEGATE_PROPERTY_TAB_DISPLAYED_MNEMONIC_INDEX = "tabDisplayedMnemonicIndex";

    public TabbedPaneFactory(Class beanClass) {
        super(beanClass, false)
    }

    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        def newChild = super.newInstance(builder, name, value, attributes)
        builder.context.tabbedPaneFactoryClosure =
            { FactoryBuilderSupport cBuilder, Object cNode, Map cAttributes ->
                if (builder.current == newChild) inspectChild(cBuilder, cNode, cAttributes)
            }
        builder.addAttributeDelegate(builder.context.tabbedPaneFactoryClosure)
        builder.context.selectedIndex = attributes.remove('selectedIndex')
        builder.context.selectedComponent = attributes.remove('selectedComponent')

        builder.context[DELEGATE_PROPERTY_TITLE] = attributes.remove("titleProperty") ?: DEFAULT_DELEGATE_PROPERTY_TITLE
        builder.context[DELEGATE_PROPERTY_TAB_ICON] = attributes.remove("tabIconProperty") ?: DEFAULT_DELEGATE_PROPERTY_TAB_ICON
        builder.context[DELEGATE_PROPERTY_TAB_DISABLED_ICON] = attributes.remove("tabDisabledIconProperty") ?: DEFAULT_DELEGATE_PROPERTY_TAB_DISABLED_ICON
        builder.context[DELEGATE_PROPERTY_TAB_TOOL_TIP] = attributes.remove("tabToolTipProperty") ?: DEFAULT_DELEGATE_PROPERTY_TAB_TOOL_TIP
        builder.context[DELEGATE_PROPERTY_TAB_BACKGROUND] = attributes.remove("tabBackgroundProperty") ?: DEFAULT_DELEGATE_PROPERTY_TAB_BACKGROUND
        builder.context[DELEGATE_PROPERTY_TAB_FOREGROUND] = attributes.remove("tabForegroundProperty") ?: DEFAULT_DELEGATE_PROPERTY_TAB_FOREGROUND
        builder.context[DELEGATE_PROPERTY_TAB_ENABLED] = attributes.remove("tabEnabledProperty") ?: DEFAULT_DELEGATE_PROPERTY_TAB_ENABLED
        builder.context[DELEGATE_PROPERTY_TAB_MNEMONIC] = attributes.remove("tabMnemonicProperty") ?: DEFAULT_DELEGATE_PROPERTY_TAB_MNEMONIC
        builder.context[DELEGATE_PROPERTY_TAB_DISPLAYED_MNEMONIC_INDEX] = attributes.remove("tabDisplayedMnemonicIndexProperty") ?: DEFAULT_DELEGATE_PROPERTY_TAB_DISPLAYED_MNEMONIC_INDEX

        return newChild;
    }

    public static void inspectChild(FactoryBuilderSupport builder, Object node, Map attributes) {
        def name = attributes.remove(builder?.parentContext?.getAt(DELEGATE_PROPERTY_TITLE) ?: DEFAULT_DELEGATE_PROPERTY_TITLE)
        def icon = attributes.remove(builder?.parentContext?.getAt(DELEGATE_PROPERTY_TAB_ICON) ?: DEFAULT_DELEGATE_PROPERTY_TAB_ICON)
        def disabledIcon = attributes.remove(builder?.parentContext?.getAt(DELEGATE_PROPERTY_TAB_DISABLED_ICON) ?: DEFAULT_DELEGATE_PROPERTY_TAB_DISABLED_ICON)
        def toolTip = attributes.remove(builder?.parentContext?.getAt(DELEGATE_PROPERTY_TAB_TOOL_TIP) ?: DEFAULT_DELEGATE_PROPERTY_TAB_TOOL_TIP)
        def background = attributes.remove(builder?.parentContext?.getAt(DELEGATE_PROPERTY_TAB_BACKGROUND) ?: DEFAULT_DELEGATE_PROPERTY_TAB_BACKGROUND)
        def foreground = attributes.remove(builder?.parentContext?.getAt(DELEGATE_PROPERTY_TAB_FOREGROUND) ?: DEFAULT_DELEGATE_PROPERTY_TAB_FOREGROUND)
        def enabled = attributes.remove(builder?.parentContext?.getAt(DELEGATE_PROPERTY_TAB_ENABLED) ?: DEFAULT_DELEGATE_PROPERTY_TAB_ENABLED)
        def mnemonic = attributes.remove(builder?.parentContext?.getAt(DELEGATE_PROPERTY_TAB_MNEMONIC) ?: DEFAULT_DELEGATE_PROPERTY_TAB_MNEMONIC)
        def displayedMnemonicIndex = attributes.remove(builder?.parentContext?.getAt(DELEGATE_PROPERTY_TAB_DISPLAYED_MNEMONIC_INDEX) ?: DEFAULT_DELEGATE_PROPERTY_TAB_DISPLAYED_MNEMONIC_INDEX)
        builder.context.put(node, [name, icon, disabledIcon, toolTip, background, foreground, enabled, mnemonic, displayedMnemonicIndex])
    }

    public void setChild(FactoryBuilderSupport builder, Object parent, Object child) {
        if (!(child instanceof Component) || (child instanceof Window)) {
            return;
        }
        try {
            def title = builder.context[child] ?: [null, null, null, null, null, null, null, null, null]
            if (title[0] == null) {
                title[0] = child.name
            }
            parent.addTab(title[0], title[1], child, title[3])
            int index = parent.indexOfComponent(child)
            if (title[2]) {
                parent.setDisabledIconAt(index, title[2])
            }
            if (title[4]) {
                parent.setBackgroundAt(index, title[4])
            }
            if (title[5]) {
                parent.setForegroundAt(index, title[5])
            }
            if (title[6] != null) {
                parent.setEnabledAt(index, title[6])
            }
            if (title[7]) {
                def mnemonic = title[7]
                if (mnemonic instanceof String) {
                    parent.setMnemonicAt(index, mnemonic.charAt(0) as int)
                } else {
                    parent.setMnemonicAt(index, mnemonic as int)
                } 
            }
            if (title[8]) {
                parent.setDisplayedMnemonicIndexAt(index, title[8])
            }
        } catch (MissingPropertyException mpe) {
            parent.add(child)
        }
    }

    public void onNodeCompleted( FactoryBuilderSupport builder, Object parent, Object node ) {
        super.onNodeCompleted (builder, parent, node)
        builder.removeAttributeDelegate(builder.context.tabbedPaneFactoryClosure)
        if (builder.context.selectedComponent != null) {
            node.selectedComponent = builder.context.selectedComponent
        }
        if (builder.context.selectedIndex != null) {
            node.selectedIndex = builder.context.selectedIndex
        }
    }


}