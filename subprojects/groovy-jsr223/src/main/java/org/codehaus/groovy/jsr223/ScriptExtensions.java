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
package org.codehaus.groovy.jsr223;

import groovy.lang.Binding;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.Reader;
import java.util.Map;
import java.util.Set;

/**
 * This class defines new Java 6 specific groovy methods which extend the normal
 * JDK classes inside the Groovy environment. Static methods are used with the
 * first parameter the destination class.
 */
public class ScriptExtensions {
    /**
     * Executes the specified script.  The default <code>ScriptContext</code> for the <code>ScriptEngine</code>
     * is used. Variables from a Groovy <code>Binding</code> are made available in the default scope of the
     * <code>Bindings</code> of the <code>ScriptEngine</code>. Resulting variables in the <code>Bindings</code>
     * are returned back to the Groovy <code>Binding</code>.
     *
     * @param self A ScriptEngine
     * @param script The script language source to be executed
     * @param binding A Groovy binding
     * @return The value returned from the execution of the script (if supported by the Script engine)
     * @throws javax.script.ScriptException if error occurs in script
     * @throws NullPointerException if the argument is null
     * @see #eval(javax.script.ScriptEngine, java.io.Reader, groovy.lang.Binding)
     * @since 1.7.3
     */
    public static Object eval(ScriptEngine self, String script, Binding binding) throws ScriptException {
        storeBindingVars(self, binding);
        Object result = self.eval(script);
        retrieveBindingVars(self, binding);
        return result;
    }

    /**
     * Same as <code>eval(ScriptEngine, Reader, Binding)</code> except that the
     * source of the script is provided as a <code>Reader</code>
     *
     * @param self A ScriptEngine
     * @param reader The source of the script
     * @param binding A Groovy binding
     * @return The value returned by the script
     * @throws javax.script.ScriptException if an error occurs in script
     * @throws NullPointerException if the argument is null
     * @see #eval(javax.script.ScriptEngine, java.lang.String, groovy.lang.Binding)
     * @since 1.7.3
     */
    public static Object eval(ScriptEngine self, Reader reader, Binding binding) throws ScriptException {
        storeBindingVars(self, binding);
        Object result = self.eval(reader);
        retrieveBindingVars(self, binding);
        return result;
    }

    private static void retrieveBindingVars(ScriptEngine self, Binding binding) {
        Set<Map.Entry<String, Object>> returnVars = self.getBindings(ScriptContext.ENGINE_SCOPE).entrySet();
        for (Map.Entry<String, Object> me : returnVars) {
            binding.setVariable(me.getKey(), me.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private static void storeBindingVars(ScriptEngine self, Binding binding) {
        Set<Map.Entry<?, ?>> vars = binding.getVariables().entrySet();
        for (Map.Entry<?, ?> me : vars) {
            self.put(me.getKey().toString(), me.getValue());
        }
    }

}
