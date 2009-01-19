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
import groovy.swing.binding.*
import java.util.Map.Entry
import org.codehaus.groovy.binding.*

/**
 * @author <a href="mailto:shemnon@yahoo.com">Danno Ferrin</a>
 * @version $Revision$
 * @since Groovy 1.1
 */
public class BindFactory extends AbstractFactory {

    public static final String CONTEXT_DATA_KEY = "BindFactoryData";

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

        // JComboBox.elements / items
        // JComboBox.selectedElement / selectedItem
        syntheticBindings.putAll(JComboBoxProperties.getSyntheticProperties())

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
        Map bindContext = builder.context.get(CONTEXT_DATA_KEY) ?: [:]
        if (bindContext.isEmpty()) {
            builder.context.put(CONTEXT_DATA_KEY, bindContext)
        }

        TargetBinding tb = null
        if (target != null) {
            String targetProperty = (String) attributes.remove("targetProperty") ?: value
            tb = new PropertyBinding(target, targetProperty)
            if (source == null) {
                // if we have a target but no source assume the build context is the source and return
                def newAttributes = [:]
                newAttributes.putAll(attributes)
                bindContext.put(tb, newAttributes)
                attributes.clear()
                return tb
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
            String property = (String) attributes.remove("sourceProperty") ?: value
            PropertyBinding pb = new PropertyBinding(source, property)

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

            SourceBinding sb;
            if (sva) {
                // source value comes from a value closure
                Closure queryValue = (Closure) attributes.remove("sourceValue")
                sb = new ClosureSourceBinding(queryValue)
            } else {
                // soruce value is the property value
                sb = pb
            }

            if (!sea && !sva) {
                // check for a mutual binding (bi-directional)
                if (attributes.remove("mutual")) {
                    fb = new MutualPropertyBinding(sb, tb)
                } else {
                    fb = trigger.createBinding(sb, tb)
                }
            } else {
                fb = trigger.createBinding(sb, tb)
            }
        } else if (!(sea || sva || spa)) {
            // if no sourcing is defined then assume we are a closure binding and return
            def newAttributes = [:]
            newAttributes.putAll(attributes)
            bindContext.put(tb, newAttributes)
            attributes.clear()
            return new ClosureTriggerBinding(syntheticBindings)
        } else {
            throw new RuntimeException("Both sourceEvent: and sourceValue: cannot be specified along with sourceProperty: or a value argument")
        }

        if (attributes.containsKey("value")) {
            bindContext.put(fb, [value:attributes.remove("value")])
        }

        Object o = attributes.remove("bind")
        if (    ((o == null) && !attributes.containsKey('group'))
            || ((o instanceof Boolean) && ((Boolean)o).booleanValue()))
        {
            fb.bind()
        }
        if (target != null) {
            fb.update()
        }

        if ((attributes.group instanceof AggregateBinding) && (fb instanceof BindingUpdatable)) {
            attributes.remove('group').addBinding(fb)
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
            def bindAttrs = builder.context.get(CONTEXT_DATA_KEY)[node] ?: [:]
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
