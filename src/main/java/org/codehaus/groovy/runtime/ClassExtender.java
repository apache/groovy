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
package org.codehaus.groovy.runtime;

import groovy.lang.Closure;

import java.util.HashMap;
import java.util.Map;

/**
 * A helper class used by the runtime to allow Groovy classes to be extended at runtime
 */
@Deprecated
public class ClassExtender {
    private Map variables;
    private Map methods;

    public synchronized Object get(String name) {
        if (variables != null) {
            return variables.get(name);
        }
        return null;
    }

    public synchronized void set(String name, Object value) {
        if (variables == null) {
            variables = createMap();
        }
        variables.put(name, value);
    }

    public synchronized void remove(String name) {
        if (variables != null) {
            variables.remove(name);
        }
    }

    public void call(String name, Object params) {
        Closure closure = null;
        synchronized (this) {
            if (methods != null) {
                closure = (Closure) methods.get(name);
            }
        }
        if (closure != null) {
            closure.call(params);
        }
        /*
        else {
            throw DoesNotUnderstandException();
        }
        */
    }

    public synchronized void addMethod(String name, Closure closure) {
        if (methods == null) {
            methods = createMap();
        }
        methods.put(name, methods);
    }

    public synchronized void removeMethod(String name) {
        if (methods != null) {
            methods.remove(name);
        }
    }

    protected Map createMap() {
        return new HashMap();
    }
}
