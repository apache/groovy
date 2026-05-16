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
package org.codehaus.groovy.control.customizers.builder;

import groovy.lang.Closure;
import groovy.util.AbstractFactory;
import groovy.util.FactoryBuilderSupport;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.codehaus.groovy.runtime.ProxyGeneratorAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * This factory lets a user define a compilation customizer without having to define
 * an anonymous inner class.
 * <p>
 * Here is an example, which only logs the class name during compilation:
 * <pre>
 * inline(phase:'CONVERSION') { source, context, classNode {@code ->}
 *     println "visiting $classNode"
 * }
 * </pre>
 *
 * @since 2.1.0
 */
@SuppressWarnings("unchecked")
public class InlinedASTCustomizerFactory extends AbstractFactory implements PostCompletionFactory {

    /**
     * Indicates that inline customizer nodes accept nested closure content.
     *
     * @return {@code true}
     */
    @Override
    public boolean isHandlesNodeChildren() {
        return true;
    }

    /**
     * Creates the backing map used to build a proxy customizer.
     *
     * @param builder the active builder
     * @param name the node name
     * @param value the supplied node value
     * @param attributes the node attributes
     * @return the backing attribute map
     * @throws InstantiationException if instantiation fails
     * @throws IllegalAccessException if access fails
     */
    @Override
    public Object newInstance(final FactoryBuilderSupport builder, final Object name, final Object value, final Map attributes) throws InstantiationException, IllegalAccessException {
        if (attributes.isEmpty() || !attributes.containsKey("phase")) {
            throw new RuntimeException("You must specify a CompilePhase to run at, using the [phase] attribute");
        }
        Map result = new HashMap(1+attributes.size());
        result.putAll(attributes);
        return result;
    }

    /**
     * Stores the inline customization closure on the backing node.
     *
     * @param builder the active builder
     * @param node the current node
     * @param childContent the nested closure content
     * @return {@code false} to continue normal processing
     */
    @Override
    public boolean onNodeChildren(final FactoryBuilderSupport builder, final Object node, final Closure childContent) {
        if (node instanceof Map) {
            ((Map)node).put("call", childContent.clone());
        }
        return false;
    }

    /**
     * Builds the proxy-backed compilation customizer once the node is complete.
     *
     * @param factory the active builder
     * @param parent the parent node
     * @param node the completed node
     * @return the generated customizer or the original node
     */
    @Override
    public Object postCompleteNode(final FactoryBuilderSupport factory, final Object parent, final Object node) {
        if (node instanceof Map map) {
            ProxyGeneratorAdapter adapter = new ProxyGeneratorAdapter(
                    map,
                    map.containsKey("superClass")?(Class)map.get("superClass"):CompilationCustomizer.class,
                    map.containsKey("interfaces")?(Class[])map.get("interfaces"):null,
                    this.getClass().getClassLoader(),
                    false,
                    null
            );
            Object phase = map.get("phase");
            if (!(phase instanceof CompilePhase)) {
                phase = CompilePhase.valueOf(phase.toString());
            }
            return adapter.proxy(map, phase);
        }
        return node;
    }
}
