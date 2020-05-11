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
package org.codehaus.groovy.tools.groovydoc;

import org.codehaus.groovy.groovydoc.GroovyAnnotationRef;
import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.runtime.StringGroovyMethods;

public class SimpleGroovyAnnotationRef implements GroovyAnnotationRef {
    private GroovyClassDoc type;
    private final String desc;
    private String name;

    public SimpleGroovyAnnotationRef(String name, String desc) {
        this.name = name;
        final String params = StringGroovyMethods.minus(desc, "@" + name);
        this.desc = "()".equals(params) ? "" : params;
    }

    public void setType(GroovyClassDoc type) {
        this.type = type;
    }

    public GroovyClassDoc type() {
        return type;
    }

    public boolean isTypeAvailable() {
        return !(type == null);
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String description() {
        return desc;
    }
}