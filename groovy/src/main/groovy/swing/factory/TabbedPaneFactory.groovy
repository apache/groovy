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
import javax.swing.JTabbedPane

class TabbedPaneFactory extends BeanFactory {

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
        return newChild;
    }

    public static void inspectChild(FactoryBuilderSupport builder, Object node, Map attributes) {
        def name = attributes.remove('title')
        def icon = attributes.remove('tabIcon')
        def disabledIcon = attributes.remove('tabDisabledIcon')
        def toolTip = attributes.remove('tabToolTip')
        def background = attributes.remove('tabBackground')
        def foreground = attributes.remove('tabForeground')
        def enabled = attributes.remove('tabEnabled')
        def mnemonic = attributes.remove('tabMnemonic')
        def displayedMnemonicIndex = attributes.remove('tabDisplayedMnemonicIndex')
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