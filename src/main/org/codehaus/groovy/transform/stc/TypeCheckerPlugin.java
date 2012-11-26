/*
 * Copyright 2003-2012 the original author or authors.
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
package org.codehaus.groovy.transform.stc;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;

import java.util.Collections;
import java.util.List;

/**
 * Type checking plugins can be used to extend the type checker capabilities.
 * For example, when a method is not found or a dynamic variable is recognized,
 * a plugin may return the type of the expression.
 * <p/>
 * This is an expert and experimental feature. APIs may change without notice.
 *
 * @author Cedric Champeau
 * @since 2.0.0
 */
public abstract class TypeCheckerPlugin {

    /**
     * This method is called by the type checker whenever it fails to find a method on a receiver.
     * In that case, it delegates the search to the plugin, which <i>may</i> return a list of
     * matching method nodes.
     * <p/>
     * Strictly speaking, it is <b>not</b> required that the returned method nodes correspond to
     * real method nodes. The type checker will normally use the return type of the method node
     * to infer the return type of the call. Therefore, if you create a virtual method node and
     * that you want the type checker to behave correctly, you must ensure, at least, to return
     * a method node with a valid return type. This is important when the return type makes use
     * of generics, for example.
     * <p/>
     * Note that this method can be called several times with the same method name and arguments,
     * but with different receivers. This is the case, for example, when you are in with{} blocks
     * and that a method could potentially be called on different receivers. Be sure to return an
     * empty list if you are not able to determine a method node for the current receiver.
     *
     * @param receiver a class node for with an undefined method is called
     * @param name     the name of the method
     * @param args     the inferred arguments types of the method.
     * @return a list of matching method nodes.
     */
    public List<MethodNode> findMethod(ClassNode receiver, String name, ClassNode... args) {
        return Collections.emptyList();
    }

    /**
     * This method is called when the type checker finds a dynamic variable which is not supposed
     * to be allowed. In that case, a plugin <i>may</i> return a type for the dynamic variable.
     * <p/>
     * This is useful when type checking scripts for which, for example, a binding has been set,
     * and that you know the types of the variables included in the binding.
     * <p/>
     * If the plugin is not able to determine the type of a variable, it is required to return
     * null, which will in turn trigger a compilation error. Any non null type will be considered
     * as the inferred type.
     *
     * @param variable the dynamic variable for which we want the plugin to resolve the type
     * @return the type of the dynamic variable, or null if the plugin is not able to determine its type.
     */
    public ClassNode resolveDynamicVariableType(DynamicVariable variable) {
        return null;
    }

    /**
     * This method is called when the type checker cannot find a property on a receiver.
     * <p/>
     * This is useful when classes override the getProperty method.
     * <p/>
     * If the plugin is not able to determine the type of a variable, it is required to return
     * null, which will in turn trigger a compilation error. Any non null type will be considered
     * as the inferred type.
     * <p/>
     * Note that this method can be called multiple times with the same property name before you
     * reach a receiver you expect. It is required that you check the receiver type.
     * <p/>
     * It is not required that the returned property node actually exists.
     *
     * @param receiver     a class node for which a property wasn't found
     * @param propertyName the name of the property
     * @return the type of the property or null if the plugin is not able to determine its type.
     */
    public PropertyNode resolveProperty(ClassNode receiver, String propertyName) {
        return null;
    }

    protected static Parameter[] toParameterArray(ClassNode[] types) {
        Parameter[] results = new Parameter[types.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = new Parameter(types[i], "p" + i);
        }
        return results;
    }

}
