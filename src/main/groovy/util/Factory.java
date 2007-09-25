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
package groovy.util;

import java.util.Map;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.com>
 */ 
public interface Factory {
    /**
     *
     * @return true if no child closures should be processed
     */
    boolean isLeaf();

    /**
     *
     * @param builder the FactoryBuilder
     * @param name the name of the node being built
     * @param value the 'value' argument in the build node
     * @param properties the attributes of the build arg
     * @return the object created for the builder
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    Object newInstance( FactoryBuilderSupport builder, Object name, Object value, Map properties )
            throws InstantiationException, IllegalAccessException;

    /**
     *
     * @param builder the FactoryBuilder
     * @param node the node (returned from newINstance) to consider the attributes for
     * @param attributes the attributes, a mutable set
     * @return true if the factory builder should use standerd bean property matching for the remaining attributes
     */
    boolean onHandleNodeAttributes( FactoryBuilderSupport builder, Object node, Map attributes );

    /**
     *
     * @param builder the FactoryBuilder
     * @param parent the parent node (null if 'root')
     * @param node the node just completed
     */
    void onNodeCompleted( FactoryBuilderSupport builder, Object parent, Object node );

    void setParent( FactoryBuilderSupport builder, Object parent, Object child );
}
