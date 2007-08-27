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
package groovy.swing.factory;

import groovy.lang.Closure;
import groovy.swing.SwingBuilder;
import org.codehaus.groovy.binding.ClosureSourceBinding;
import org.codehaus.groovy.binding.EventTriggerBinding;
import org.codehaus.groovy.binding.FullBinding;
import org.codehaus.groovy.binding.PropertyChangeTriggerBinding;
import org.codehaus.groovy.binding.PropertySourceBinding;
import org.codehaus.groovy.binding.TargetBinding;
import org.codehaus.groovy.binding.PropertyTargetBinding;

import java.util.Map;

/**
 * @author <a href="mailto:shemnon@yahoo.com">Danno Ferrin</a>
 * @version $Revision: 7046 $
 * @since Groovy 1.1
 */
public class BindFactory implements Factory {

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
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public Object newInstance(SwingBuilder builder, Object name, Object value, Map properties) throws InstantiationException, IllegalAccessException {
        if (value != null) {
            throw new RuntimeException(name + " elements do not accept a value argument.");
        }
        Object source = properties.remove("source");
        Object target = properties.remove("target");

        TargetBinding tb = null;
        if (target != null) {
            String targetProperty = (String) properties.remove("targetProperty");
            tb = new PropertyTargetBinding(target, targetProperty);
        }
        FullBinding fb;

        if (properties.containsKey("sourceProperty")) {
            String property = (String) properties.remove("sourceProperty");
            PropertySourceBinding psb = new PropertySourceBinding(this, source, property);
            PropertyChangeTriggerBinding pctb = new PropertyChangeTriggerBinding(source, property);
            fb = pctb.createBinding(psb, tb);
        } else if (properties.containsKey("sourceEvent") && properties.containsKey("sourceValue")) {
            Closure queryValue = (Closure) properties.remove("sourceValue");
            ClosureSourceBinding psb = new ClosureSourceBinding(queryValue);
            String trigger = (String) properties.remove("sourceEvent");
            EventTriggerBinding etb = new EventTriggerBinding(source, trigger);
            fb = etb.createBinding(psb, tb);
        } else {
            throw new RuntimeException(name + " does not have suffient properties to initialize");
        }

        if (target != null) {
            fb.bind();
            fb.forceUpdate();
        }
        return fb;
    }
}
