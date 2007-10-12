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

import org.codehaus.groovy.binding.ModelBinding

/**
 * @author <a href="mailto:shemnon@yahoo.com">Danno Ferrin</a>
 * @version $Revision$
 * @since Groovy 1.1
 */
public class ModelFactory extends AbstractFactory {

    // because isleaf is true we can keep state across calls assuming the same object
    protected boolean doBind

    public boolean isLeaf() {
        return true
    }

    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        if (value == null) {
            throw new RuntimeException("$name requires a value argument.");
        }
        ModelBinding mb = new ModelBinding(value);

        Object o = attributes.remove("bind");
        doBind = (o instanceof Boolean) && ((Boolean) o).booleanValue()
        return mb;
    }

    public void onNodeCompleted( FactoryBuilderSupport builder, Object parent, Object node ) {
        if (doBind) {
            node.bind()
        }
    }

}
