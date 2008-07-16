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

import groovy.swing.SwingBuilder
import groovy.swing.binding.AbstractButtonProperties
import groovy.swing.binding.JScrollBarProperties
import groovy.swing.binding.JSliderProperties
import groovy.swing.binding.JTableProperties
import groovy.swing.binding.JTextComponentProperties
import java.util.Map.Entry
import org.codehaus.groovy.binding.*


/**
 * @author <a href="mailto:shemnon@yahoo.com">Danno Ferrin</a>
 * @version $Revision$
 * @since Groovy 1.1
 */
public class BindFactory extends AbstractFactory {

    final Map<String, TriggerBinding> syntheticBindings

    public BindFactory() {
        syntheticBindings = new HashMap()

        // covers JTextField.text
        // covers JTextPane.text
        // covers JTextArea.text
        // covers JEditorPane.text
        syntheticBindings.putAll(JTextComponentProperties.getSyntheticProperties())

        // covers JCheckBox.selected
        // covers JChecBoxMenuItem.selected
        // covers JRadioButton.selected
        // covers JRadioButtonMenuItem.selected
        // covers JToggleButton.selected
        syntheticBindings.putAll(AbstractButtonProperties.getSyntheticProperties())

        // covers JSlider.value
        syntheticBindings.putAll(JSliderProperties.getSyntheticProperties())

        // covers JScrollBar.value
        syntheticBindings.putAll(JScrollBarProperties.getSyntheticProperties())

        // JComboBox.elements
        // JComboBox.selectedElement
        //syntheticBindings.putAll(JComboBoxProperties.getSyntheticProperties())

        // JList.elements
        // JList.selectedElement
        // JList.selectedElements
        //syntheticBindings.putAll(JListProperties.getSyntheticProperties())

        // JSpinner.value
        //syntheticBindings.putAll(JSpinnerProperties.getSyntheticProperties())

        // other properties handled in JSR-295
        // JTable.elements
        // JTable.selectedElement
        // JTable.selectedElements
        syntheticBindings.putAll(JTableProperties.getSyntheticProperties());

        // JTree.root
        // JTree.selectedElement
        // JTree.selectedElements

    }

    /**
     * Accepted Properties...
     *
     * group?
     * source ((sourceProperty) | (sourceEvent sourceValue))
     * (target targetProperty)? (? use default javabeans property if targetProperty is not present?)
     *
     *
     * @param builder
     * @param name
     * @param value
     * @param attributes
     * @return the newly created instance
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        Object source = attributes.remove("source")
        Object target = attributes.remove("target")

        TargetBinding tb = null
        if (target != null) {
            String targetProperty = (String) attributes.remove("targetProperty") ?: value
            tb = new PropertyBinding(target, targetProperty)
            if (source == null) {
                def newAttributes = [:]
                newAttributes.putAll(attributes)
                builder.context.put(tb, newAttributes)
                attributes.clear()
                return tb
            }
        }
        FullBinding fb

        if (attributes.containsKey("sourceEvent") && attributes.containsKey("sourceValue")) {
            Closure queryValue = (Closure) attributes.remove("sourceValue")
            ClosureSourceBinding psb = new ClosureSourceBinding(queryValue)
            String trigger = (String) attributes.remove("sourceEvent")
            EventTriggerBinding etb = new EventTriggerBinding(source, trigger)
            fb = etb.createBinding(psb, tb)
        } else if (attributes.containsKey("sourceProperty") || value) {
            // first check for synthetic properties
            String property = (String) attributes.remove("sourceProperty") ?: value
            PropertyBinding psb = new PropertyBinding(source, property)

            TriggerBinding trigger = getTriggerBinding(psb)
            fb = trigger.createBinding(psb, tb)
        } else {
            def newAttributes = [:]
            newAttributes.putAll(attributes)
            builder.context.put(tb, newAttributes)
            attributes.clear()
            return new ClosureTriggerBinding(syntheticBindings)
        }

        if (attributes.containsKey("value")) {
            builder.context.put(fb, [value:attributes.remove("value")])
        }

        Object o = attributes.remove("bind")
        if (    (o == null)
            || ((o instanceof Boolean) && ((Boolean)o).booleanValue()))
        {
            fb.bind()
        }
        if (target != null) {
            fb.update()
        }

        builder.addDisposalClosure(fb.&unbind)
        return fb
    }

    public boolean isLeaf() {
        return false;
    }

    public boolean isHandlesNodeChildren() {
        return true;
    }

    public boolean onNodeChildren(FactoryBuilderSupport builder, Object node, Closure childContent) {
        if ((node instanceof FullBinding) && (node.converter == null)) {
            node.converter = childContent
            return false
        } else if (node instanceof ClosureTriggerBinding) {
            node.closure = childContent
            return false;
        } else if (node instanceof TriggerBinding) {
            def bindAttrs = builder.getContext().get(node) ?: [:]
            if (!bindAttrs.containsKey("converter")) {
                bindAttrs["converter"] = childContent
                return false;
            }
        }

        throw new RuntimeException("Binding nodes do not accept child content when a converter is already specified")
    }

    public TriggerBinding getTriggerBinding(PropertyBinding psb) {
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
        //TODO inspect the bean info and throw an error if the property is not obserbable and not bind:false?
        return psb
    }

    public bindingAttributeDelegate(FactoryBuilderSupport builder, def node, def attributes) {
        Iterator iter = attributes.entrySet().iterator()
        while (iter.hasNext()) {
            Entry entry = (Entry) iter.next()
            String property = entry.key.toString()
            Object value = entry.value

            def bindAttrs = builder.getContext().get(value) ?: [:]
            def idAttr = builder.getAt(SwingBuilder.DELEGATE_PROPERTY_OBJECT_ID) ?: SwingBuilder.DEFAULT_DELEGATE_PROPERTY_OBJECT_ID
            def id = bindAttrs.remove(idAttr)
            if (bindAttrs.containsKey("value")) {
                node."$property" = bindAttrs.remove("value")
            }

            FullBinding fb
            if (value instanceof FullBinding) {
                fb = (FullBinding) value
                fb.setTargetBinding(new PropertyBinding(node, property))
            } else  if (value instanceof TargetBinding) {
                PropertyBinding psb = new PropertyBinding(node, property)
                fb = getTriggerBinding(psb).createBinding(psb, value)

                Object o = bindAttrs.remove("bind")

                if (    (o == null)
                    || ((o instanceof Boolean) && ((Boolean)o).booleanValue()))
                {
                    fb.bind()
                }
                fb.update()
                
                bindAttrs.each{k, v -> fb."$k" = v}

                builder.addDisposalClosure(fb.&unbind)

                // replaces ourselves in the variables
                // id: is lost to us by now, so we just assume that any storage of us is a goner as well
                //builder.getVariables().each{ Map.Entry me -> if (value.is(me.value)) me.setValue fb}
                if (id) builder.setVariable(id, fb)
            } else if (value instanceof ClosureTriggerBinding) {
                PropertyBinding psb = new PropertyBinding(node, property)
                fb = value.createBinding(value, psb);

                Object o = bindAttrs.remove("bind")

                if (    (o == null)
                    || ((o instanceof Boolean) && ((Boolean)o).booleanValue()))
                {
                    fb.bind()
                }
                fb.update()

                bindAttrs.each{k, v -> fb."$k" = v}

                builder.addDisposalClosure(fb.&unbind)

                // replaces ourselves in the variables
                // id: is lost to us by now, so we just assume that any storage of us is a goner as well
                //builder.getVariables().each{ Map.Entry me -> if (value.is(me.value)) me.setValue fb}
                if (id) builder.setVariable(id, fb)
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

}
