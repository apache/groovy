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

import groovy.swing.SwingBuilder
import groovy.swing.binding.AbstractButtonProperties
import groovy.swing.binding.JComboBoxProperties
import groovy.swing.binding.JComponentProperties
import groovy.swing.binding.JListProperties
import groovy.swing.binding.JScrollBarProperties
import groovy.swing.binding.JSliderProperties
import groovy.swing.binding.JSpinnerProperties
import groovy.swing.binding.JTableProperties
import groovy.swing.binding.JTextComponentProperties
import org.apache.groovy.swing.binding.AggregateBinding
import org.apache.groovy.swing.binding.BindingUpdatable
import org.apache.groovy.swing.binding.ClosureSourceBinding
import org.apache.groovy.swing.binding.ClosureTriggerBinding
import org.apache.groovy.swing.binding.EventTriggerBinding
import org.apache.groovy.swing.binding.FullBinding
import org.apache.groovy.swing.binding.MutualPropertyBinding
import org.apache.groovy.swing.binding.PropertyBinding
import org.apache.groovy.swing.binding.SourceBinding
import org.apache.groovy.swing.binding.TargetBinding
import org.apache.groovy.swing.binding.TriggerBinding

import java.util.Map.Entry

/**
 * @since Groovy 1.1
 */
class BindFactory extends AbstractFactory {

    public static final String CONTEXT_DATA_KEY = "BindFactoryData";

    final Map<String, TriggerBinding> syntheticBindings

    BindFactory() {
        syntheticBindings = new HashMap()

        // covers JTextField.text
        // covers JTextPane.text
        // covers JTextArea.text
        // covers JEditorPane.text
        syntheticBindings.putAll(JTextComponentProperties.syntheticProperties)

        // covers JCheckBox.selected
        // covers JCheckBoxMenuItem.selected
        // covers JRadioButton.selected
        // covers JRadioButtonMenuItem.selected
        // covers JToggleButton.selected
        syntheticBindings.putAll(AbstractButtonProperties.syntheticProperties)

        // covers JSlider.value
        syntheticBindings.putAll(JSliderProperties.syntheticProperties)

        // covers JScrollBar.value
        syntheticBindings.putAll(JScrollBarProperties.syntheticProperties)

        // JComboBox.elements / items
        // JComboBox.selectedElement / selectedItem
        syntheticBindings.putAll(JComboBoxProperties.syntheticProperties)

        // JList.selectedElement / selectedItem / selectedElements / selectedItems / selectedIndex
        syntheticBindings.putAll(JListProperties.syntheticProperties)

        // JSpinner.value
        syntheticBindings.putAll(JSpinnerProperties.syntheticProperties)

        // other properties handled in JSR-295
        // JTable.elements
        // JTable.selectedElement
        // JTable.selectedElements
        syntheticBindings.putAll(JTableProperties.syntheticProperties)

        // JTree.root
        // JTree.selectedElement
        // JTree.selectedElements

        // covers JComponent.size
        // covers JComponent.width
        // covers JComponent.height
        // covers JComponent.bounds
        // covers JComponent.x
        // covers JComponent.y
        // covers JComponent.visible
        syntheticBindings.putAll(JComponentProperties.syntheticProperties)

    }

    /**
     * Accepted Properties...
     *
     * group?
     * source ((sourceProperty) | (sourceEvent sourceValue))
     * (target targetProperty)? (? use default javabeans property if targetProperty is not present?)
     *
     * @param builder
     * @param name
     * @param value
     * @param attributes
     * @return the newly created instance
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        Object source = attributes.remove("source")
        Object target = attributes.remove("target")
        Object update = attributes.get("update")
        Map bindContext = builder.context.get(CONTEXT_DATA_KEY) ?: [:]
        if (bindContext.isEmpty()) {
            builder.context.put(CONTEXT_DATA_KEY, bindContext)
        }

        TargetBinding tb = null
        if (target != null) {
            Object targetProperty = attributes.remove("targetProperty") ?: value
            if (!(targetProperty instanceof CharSequence)) {
                throw new IllegalArgumentException("Invalid value for targetProperty: (or node value)." +
                        " Value for this attribute must be a String but it is " + (targetProperty != null ? targetProperty.getClass().getName() : null))
            }
            tb = new PropertyBinding(target, targetProperty.toString(), update)
            if (source == null) {
                // if we have a target but no source assume the build context is the source and return
                def result
                if (attributes.remove("mutual")) {
                    result = new MutualPropertyBinding(null, null, tb, this.&getTriggerBinding)
                } else {
                    result = tb
                }
                def newAttributes = [:]
                newAttributes.putAll(attributes)
                bindContext.put(result, newAttributes)
                attributes.clear()
                return result
            }
        }

        FullBinding fb
        boolean sea = attributes.containsKey("sourceEvent")
        boolean sva = attributes.containsKey("sourceValue")
        boolean spa = attributes.containsKey("sourceProperty") || value

        if (sea && sva && !spa) {
            // entirely event triggered binding
            Closure queryValue = (Closure) attributes.remove("sourceValue")
            ClosureSourceBinding csb = new ClosureSourceBinding(queryValue)
            String trigger = (String) attributes.remove("sourceEvent")
            EventTriggerBinding etb = new EventTriggerBinding(source, trigger)
            fb = etb.createBinding(csb, tb)
        } else if (spa && !(sea && sva)) {
            // partially property driven binding
            Object property = attributes.remove("sourceProperty") ?: value
            if (!(property instanceof CharSequence)) {
                throw new IllegalArgumentException("Invalid value for sourceProperty: (or node value). " +
                        "Value for this attribute must be a String but it is " + (property != null ? property.getClass().getName() : null))
            }

            if (source == null) {
                // if we have a sourceProperty but no source then we're in trouble
                throw new IllegalArgumentException("Missing value for source: even though sourceProperty: (or node value) " +
                        "was specified. Please check you didn't write bind(model.someProperty) instead of bind{ model.someProperty }")
            }

            PropertyBinding pb = new PropertyBinding(source, property.toString(), update)

            TriggerBinding trigger
            if (sea) {
                // source trigger comes from an event
                String triggerName = (String) attributes.remove("sourceEvent")
                trigger = new EventTriggerBinding(source, triggerName)
            } else {
                // source trigger comes from a property change
                // this method will also check for synthetic properties
                trigger = getTriggerBinding(pb)
            }

            SourceBinding sb
            if (sva) {
                // source value comes from a value closure
                Closure queryValue = (Closure) attributes.remove("sourceValue")
                sb = new ClosureSourceBinding(queryValue)
            } else {
                // source value is the property value
                sb = pb
            }

            // check for a mutual binding (bi-directional)
            if (attributes.remove("mutual")) {
                fb = new MutualPropertyBinding(trigger, sb, tb, this.&getTriggerBinding)
            } else {
                fb = trigger.createBinding(sb, tb)
            }
        } else if (!(sea || sva || spa)) {
            // if no sourcing is defined then assume we are a closure binding and return
            def newAttributes = [:]
            newAttributes.putAll(attributes)
            def ctb = new ClosureTriggerBinding(syntheticBindings)
            bindContext.put(ctb, newAttributes)
            attributes.clear()
            return ctb
        } else {
            throw new RuntimeException("Both sourceEvent: and sourceValue: cannot be specified along with sourceProperty: or a value argument")
        }

        if (attributes.containsKey("value")) {
            bindContext.put(fb, [value: attributes.remove("value")])
        }

        bindContext.get(fb, [:]).put('update', update)

        Object o = attributes.remove("bind")
        if (((o == null) && !attributes.containsKey('group'))
                || ((o instanceof Boolean) && ((Boolean) o).booleanValue())) {
            fb.bind()
        }

        if ((attributes.group instanceof AggregateBinding) && (fb instanceof BindingUpdatable)) {
            attributes.remove('group').addBinding(fb)
        }

        builder.addDisposalClosure(fb.&unbind)
        return fb
    }

    void onNodeCompleted(FactoryBuilderSupport builder, Object parent, Object node) {
        super.onNodeCompleted(builder, parent, node);

        if (node instanceof FullBinding && node.sourceBinding && node.targetBinding) {
            try {
                node.update()
            } catch (Exception ignored) {
                // don't throw out to top
            }
            try {
                node.rebind()
            } catch (Exception ignored) {
                // don't throw out to top
            }
        }
    }

    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, Object node, Map attributes) {
        attributes.remove('update')
        true
    }

    boolean isLeaf() {
        return false
    }

    boolean isHandlesNodeChildren() {
        return true
    }

    boolean onNodeChildren(FactoryBuilderSupport builder, Object node, Closure childContent) {
        if ((node instanceof FullBinding) && (node.converter == null)) {
            node.converter = childContent
            return false
        } else if (node instanceof ClosureTriggerBinding) {
            node.closure = childContent
            return false
        } else if (node instanceof TriggerBinding) {
            def bindAttrs = builder.context.get(CONTEXT_DATA_KEY)[node] ?: [:]
            if (!bindAttrs.containsKey("converter")) {
                bindAttrs["converter"] = childContent
                return false
            }
        }

        throw new RuntimeException("Binding nodes do not accept child content when a converter is already specified")
    }

    TriggerBinding getTriggerBinding(PropertyBinding psb) {
        String property = psb.propertyName
        Class currentClass = psb.bean.getClass()
        while (currentClass != null) {
            // should we check interfaces as well?  if so at what level?
            def trigger = (TriggerBinding) syntheticBindings.get("$currentClass.name#$property" as String)
            if (trigger != null) {
                return trigger
            }
            currentClass = currentClass.getSuperclass()
        }
        //TODO inspect the bean info and throw an error if the property is not observable and not bind:false?
        return psb
    }

    def bindingAttributeDelegate(FactoryBuilderSupport builder, def node, def attributes) {
        Iterator iter = attributes.entrySet().iterator()
        Map bindContext = builder.context.get(CONTEXT_DATA_KEY) ?: [:]

        while (iter.hasNext()) {
            Entry entry = (Entry) iter.next()
            String property = entry.key.toString()
            Object value = entry.value

            def bindAttrs = bindContext.get(value) ?: [:]
            def idAttr = builder.getAt(SwingBuilder.DELEGATE_PROPERTY_OBJECT_ID) ?: SwingBuilder.DEFAULT_DELEGATE_PROPERTY_OBJECT_ID
            def id = bindAttrs.remove(idAttr)
            if (bindAttrs.containsKey("value")) {
                node."$property" = bindAttrs.remove("value")
            }
            def update = bindAttrs.get('update')

            FullBinding fb
            if (value instanceof MutualPropertyBinding) {
                fb = (FullBinding) value
                PropertyBinding psb = new PropertyBinding(node, property, update)
                if (fb.sourceBinding == null) {
                    fb.sourceBinding = psb
                    finishContextualBinding(fb, builder, bindAttrs, id)
                } else if (fb.targetBinding == null) {
                    fb.targetBinding = psb
                }
            } else if (value instanceof FullBinding) {
                fb = (FullBinding) value
                fb.targetBinding = new PropertyBinding(node, property, update)
            } else if (value instanceof TargetBinding) {
                PropertyBinding psb = new PropertyBinding(node, property, update)
                fb = getTriggerBinding(psb).createBinding(psb, value)
                finishContextualBinding(fb, builder, bindAttrs, id)
            } else if (value instanceof ClosureTriggerBinding) {
                PropertyBinding psb = new PropertyBinding(node, property, update)
                fb = value.createBinding(value, psb)
                finishContextualBinding(fb, builder, bindAttrs, id)
            } else {
                continue
            }
            try {
                fb.update()
            } catch (Exception e) {
                // just eat it?
            }
            try {
                fb.rebind()
            } catch (Exception e) {
                // just eat it?
            }
            // this is why we cannot use entrySet().each { }
            iter.remove()
        }
    }

    private finishContextualBinding(FullBinding fb, FactoryBuilderSupport builder, bindAttrs, id) {
        bindAttrs.remove('update')
        Object bindValue = bindAttrs.remove("bind")
        List propertiesToBeSkipped = ['group']
        bindAttrs.each { k, v -> if (!(k in propertiesToBeSkipped)) fb."$k" = v }

        if ((bindAttrs.group instanceof AggregateBinding) && (fb instanceof BindingUpdatable)) {
            bindAttrs.group.addBinding(fb)
        }

        if ((bindValue == null)
                || ((bindValue instanceof Boolean) && ((Boolean) bindValue).booleanValue())) {
            fb.bind()
        }

        builder.addDisposalClosure(fb.&unbind)

        // replaces ourselves in the variables
        // id: is lost to us by now, so we just assume that any storage of us is a goner as well
        //builder.getVariables().each{ Map.Entry me -> if (value.is(me.value)) me.setValue fb}
        if (id) builder.setVariable(id, fb)
    }

}
