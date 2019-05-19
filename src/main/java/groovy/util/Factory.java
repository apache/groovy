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
package groovy.util;

import groovy.lang.Closure;

import java.util.Map;

public interface Factory {
    /**
     *
     * @return true if no child closures should be processed
     */
    boolean isLeaf();

    /**
     * Does this factory "Own" it's child closure.
     *
     * @return true  if the factory should have onContentClosure() called,
     *         false if the builder should handle it
     */
    boolean isHandlesNodeChildren();

    /**
     * Called when a factory is registered to a builder
     * @param builder the build the factory has been registered to
     * @param registeredName the name the factory has been registered under
     */
    void onFactoryRegistration(FactoryBuilderSupport builder, String registeredName, String registeredGroupName);

    /**
     * @param builder the FactoryBuilder
     * @param name the name of the node being built
     * @param value the 'value' argument in the build node
     * @param attributes the attributes of the build arg
     * @return the object created for the builder
     * @throws InstantiationException if attempting to instantiate an interface or abstract class
     * @throws IllegalAccessException if the instance can't be created due to a security violation
     */
    Object newInstance( FactoryBuilderSupport builder, Object name, Object value, Map attributes )
            throws InstantiationException, IllegalAccessException;

    /**
     * @param builder the FactoryBuilder
     * @param node the node (returned from newINstance) to consider the attributes for
     * @param attributes the attributes, a mutable set
     * @return true if the factory builder should use standard bean property matching for the remaining attributes
     */
    boolean onHandleNodeAttributes( FactoryBuilderSupport builder, Object node, Map attributes );

    /**
     * Only called if it isLeaf is false and isHandlesNodeChildren is true
     * @param builder the FactoryBuilder
     * @param node the node (returned from newINstance) to consider the attributes for
     * @param childContent the child content closure of the builder
     * @return true if the factory builder should apply default node processing to the content child
     */
    boolean onNodeChildren( FactoryBuilderSupport builder, Object node, Closure childContent);

    /**
     * @param builder the FactoryBuilder
     * @param parent the parent node (null if 'root')
     * @param node the node just completed
     */
    void onNodeCompleted( FactoryBuilderSupport builder, Object parent, Object node );

    void setParent( FactoryBuilderSupport builder, Object parent, Object child );

    void setChild( FactoryBuilderSupport builder, Object parent, Object child );
}
