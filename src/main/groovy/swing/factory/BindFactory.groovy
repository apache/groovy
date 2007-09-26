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

import groovy.swing.binding.AbstractButtonProperties
import groovy.swing.binding.JSliderProperties
import groovy.swing.binding.JTextComponentProperties
import org.codehaus.groovy.binding.*

/**
 * @author <a href="mailto:shemnon@yahoo.com">Danno Ferrin</a>
 * @version $Revision$
 * @since Groovy 1.1
 */
public class BindFactory extends AbstractFactory {

    Map/*<String, TriggerBinding*/ syntheticBindings;

    public BindFactory() {
        syntheticBindings = new HashMap();

        // covers JTextField.text
        // covers JTextPane.text
        // covers JTextArea.text
        // covers JEditorPane.text
        syntheticBindings.putAll(JTextComponentProperties.getSyntheticProperties());

        // covers JCheckBox.selected
        // covers JChecBoxMenuItem.selected
        // covers JRadioButton.selected
        // covers JRadioButtonMenuItem.selected
        // covers JToggleButton.selected
        syntheticBindings.putAll(AbstractButtonProperties.getSyntheticProperties());

        // covers JSlider.value
        syntheticBindings.putAll(JSliderProperties.getSyntheticProperties());

        // JComboBox.elements
        // JComboBox.selectedElement
        //syntheticBindings.putAll(JComboBoxProperties.getSyntheticProperties());

        // JList.elements
        // JList.selectedElement
        // JList.selectedElements
        //syntheticBindings.putAll(JListProperties.getSyntheticProperties());

        // JSpinner.value
        //syntheticBindings.putAll(JSpinnerProperties.getSyntheticProperties());

        // other properties handled in JSR-295
        // JTable.elements
        // JTable.selectedElement
        // JTable.selectedElements
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
     * @param properties
     * @return the newly created instance
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map properties) throws InstantiationException, IllegalAccessException {
        if (value != null) {
            throw new RuntimeException("$name elements do not accept a value argument.");
        }
        Object source = properties.remove("source");
        Object target = properties.remove("target");

        TargetBinding tb = null;
        if (target != null) {
            String targetProperty = (String) properties.remove("targetProperty");
            tb = new PropertyBinding(target, targetProperty);
        }
        FullBinding fb;

        if (properties.containsKey("sourceProperty")) {
            // first check for synthetic properties
            String property = (String) properties.remove("sourceProperty");
            PropertyBinding psb = new PropertyBinding(source, property);

            TriggerBinding trigger = null;
            Class currentClass = source.getClass();
            while ((trigger == null) && (currentClass != null)) {
                // should we check interfaces as well?  if so at what level?
                trigger = (TriggerBinding) syntheticBindings.get("$currentClass.name#$property" as String);
                currentClass = currentClass.getSuperclass();
            }
            if (trigger == null) {
                //TODO inspect the bean info and throw an error if the property is not obserbable and not bind:false?
                trigger = psb;
            }
            fb = trigger.createBinding(psb, tb);
        } else if (properties.containsKey("sourceEvent") && properties.containsKey("sourceValue")) {
            Closure queryValue = (Closure) properties.remove("sourceValue");
            ClosureSourceBinding psb = new ClosureSourceBinding(queryValue);
            String trigger = (String) properties.remove("sourceEvent");
            EventTriggerBinding etb = new EventTriggerBinding(source, trigger);
            fb = etb.createBinding(psb, tb);
        } else {
            throw new RuntimeException("$name does not have suffient properties to initialize");
        }

        Object o = properties.remove("bind");
        if (    (o == null)
            || ((o instanceof Boolean) && ((Boolean)o).booleanValue()))
        {
            fb.bind();
        }
        if (target != null) {
            fb.update();
        }

        builder.addDisposalClosure(fb.&unbind)
        return fb;
    }
}
