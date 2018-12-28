/*
 * The initial contribution was derived from the reference implementation
 * developed by Sun in consultation with the Groovy community. The reference
 * implementation had the following license header:
 *
 *  Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 *  Use is subject to license terms.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met: Redistributions of source code
 *  must retain the above copyright notice, this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice, this list of
 *  conditions and the following disclaimer in the documentation and/or other materials
 *  provided with the distribution. Neither the name of the Sun Microsystems nor the names of
 *  is contributors may be used to endorse or promote products derived from this software
 *  without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 *  OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 *  AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 *  OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 *  OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 *
 * Subsequent modifications by the Groovy community have been done under the Apache License v2:
 *
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

import groovy.lang.GroovySystem;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A factory class conforming to JSR-223 which is used to instantiate
 * Groovy <code>ScriptEngines</code> and also exposes metadata describing
 * Groovy's engine class.
 *
 * Adapted from original by Mike Grogan and A. Sundararajan
 */
public class GroovyScriptEngineFactory implements ScriptEngineFactory {

    private static final String VERSION = "2.0";

    private static final String SHORT_NAME = "groovy";

    private static final String LANGUAGE_NAME = "Groovy";

    public String getEngineName() {
        return "Groovy Scripting Engine";
    }

    /**
     * Note that the scripting.dev.java.net engine had this backwards.
     * The engine version refers to this engine implementation.
     * Whereas language version refers to the groovy implementation
     * (which is obtained from the runtime).
     */
    public String getEngineVersion() {
        return VERSION;
    }

    /**
     * This is also different than scripting.dev.java.net which used an
     * initial lowercase.  But these are proper names and should be capitalized.
     */
    public String getLanguageName() {
        return LANGUAGE_NAME;
    }

    public String getLanguageVersion() {
        return GroovySystem.getVersion();
    }

    public List<String> getExtensions() {
        return EXTENSIONS;
    }

    public List<String> getMimeTypes() {
        return MIME_TYPES;
    }

    public List<String> getNames() {
        return NAMES;
    }

    public Object getParameter(String key) {

        if (ScriptEngine.NAME.equals(key)) {
            return SHORT_NAME;
        } else if (ScriptEngine.ENGINE.equals(key)) {
            return getEngineName();
        } else if (ScriptEngine.ENGINE_VERSION.equals(key)) {
            return VERSION;
        } else if (ScriptEngine.LANGUAGE.equals(key)) {
            return LANGUAGE_NAME;
        } else if (ScriptEngine.LANGUAGE_VERSION.equals(key)) {
            return GroovySystem.getVersion();
        } else if ("THREADING".equals(key)) {
            return "MULTITHREADED";
        } else {
            throw new IllegalArgumentException("Invalid key");
        }

    }

    public ScriptEngine getScriptEngine() {
        return new GroovyScriptEngineImpl(this);
    }

    public String getMethodCallSyntax(String obj, String method,
                                      String... args) {

        StringBuilder ret = new StringBuilder(obj + "." + method + "(");
        int len = args.length;
        if (len == 0) {
            ret.append(")");
            return ret.toString();
        }

        for (int i = 0; i < len; i++) {
            ret.append(args[i]);
            if (i != len - 1) {
                ret.append(",");
            } else {
                ret.append(")");
            }
        }
        return ret.toString();
    }

    public String getOutputStatement(String toDisplay) {
        StringBuilder buf = new StringBuilder();
        buf.append("println(\"");
        int len = toDisplay.length();
        for (int i = 0; i < len; i++) {
            char ch = toDisplay.charAt(i);
            switch (ch) {
                case '"':
                    buf.append("\\\"");
                    break;
                case '\\':
                    buf.append("\\\\");
                    break;
                default:
                    buf.append(ch);
                    break;
            }
        }
        buf.append("\")");
        return buf.toString();
    }

    public String getProgram(String... statements) {
        StringBuilder ret = new StringBuilder();
        for (String statement : statements) {
            ret.append(statement).append('\n');
        }
        return ret.toString();
    }

    private static final List<String> NAMES;
    private static final List<String> EXTENSIONS;
    private static final List<String> MIME_TYPES;

    static {
        List<String> n = new ArrayList<String>(2);
        n.add(SHORT_NAME);
        n.add(LANGUAGE_NAME);
        NAMES = Collections.unmodifiableList(n);

        n = new ArrayList<String>(1);
        n.add("groovy");
        EXTENSIONS = Collections.unmodifiableList(n);

        n = new ArrayList<String>(1);
        n.add("application/x-groovy");
        MIME_TYPES = Collections.unmodifiableList(n);
    }
}
