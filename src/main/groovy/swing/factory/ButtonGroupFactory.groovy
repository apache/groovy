/*
 * Copyright 2008 the original author or authors.
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
/**
 * Created by IntelliJ IDEA.
 * User: Danno.Ferrin
 * Date: Jul 11, 2008
 * Time: 9:08:24 PM
 * To change this template use File | Settings | File Templates.
 */

import javax.swing.AbstractButton
import javax.swing.ButtonGroup

class ButtonGroupFactory extends BeanFactory {

    public static final String DELEGATE_PROPERTY_BUTTON_GROUP = "_delegateProperty:buttonGroup";
    public static final String DEFAULT_DELEGATE_PROPERTY_BUTTON_GROUP = "buttonGroup";

    public ButtonGroupFactory() {
        super(ButtonGroup, true)
    }

    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        builder.context[DELEGATE_PROPERTY_BUTTON_GROUP] = attributes.remove("buttonGroupProperty") ?: DEFAULT_DELEGATE_PROPERTY_BUTTON_GROUP
        return super.newInstance(builder, name, value, attributes);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public static buttonGroupAttributeDelegate(def builder, def node, def attributes) {
        def buttonGroupAttr = builder?.context?.getAt(DELEGATE_PROPERTY_BUTTON_GROUP) ?: DEFAULT_DELEGATE_PROPERTY_BUTTON_GROUP
        if (attributes.containsKey(buttonGroupAttr)) {
            def o = attributes.get(buttonGroupAttr)
            if ((o instanceof ButtonGroup) && (node instanceof AbstractButton)) {
                node.model.group = o
                attributes.remove(buttonGroupAttr)
            }
         }
    }

}