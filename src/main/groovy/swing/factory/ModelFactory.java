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

import groovy.swing.SwingBuilder;
import org.codehaus.groovy.binding.ModelBinding;

import java.util.Map;

/**
 * @author <a href="mailto:shemnon@yahoo.com">Danno Ferrin</a>
 * @version $Revision$
 * @since Groovy 1.1
 */
public class ModelFactory implements Factory {

    public Object newInstance(SwingBuilder builder, Object name, Object value, Map properties) throws InstantiationException, IllegalAccessException {
        if (value == null) {
            throw new RuntimeException(name + " requires a value argument.");
        }
        ModelBinding mb = new ModelBinding(value);

        Object o = properties.remove("bind");
        if ((o instanceof Boolean) && ((Boolean) o).booleanValue())
        {
            mb.bind();
        }
        return mb;
    }
}