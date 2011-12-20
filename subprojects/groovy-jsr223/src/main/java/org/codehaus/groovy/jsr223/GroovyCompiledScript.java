/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.jsr223;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * Used to represent compiled Groovy scripts.  Such scripts may be executed repeatedly
 * by Groovy's <code>ScriptEngine</code> using the <code>eval</code> method without reparsing overheads.
 *
 * @author Mike Grogan
 * @author A. Sundararajan
 */
public class GroovyCompiledScript extends CompiledScript {
    
    private final GroovyScriptEngineImpl engine;
    private final Class clasz;
    
    public GroovyCompiledScript(GroovyScriptEngineImpl engine, Class clazz) {
        this.engine = engine;
        this.clasz = clazz;
    }
    
    public Object eval(ScriptContext context) throws ScriptException {
        return engine.eval(clasz, context);
    }    
    
    public ScriptEngine getEngine() {
        return engine;
    }
    
}
