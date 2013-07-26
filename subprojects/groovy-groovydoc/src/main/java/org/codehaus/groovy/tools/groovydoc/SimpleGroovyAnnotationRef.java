/*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.tools.groovydoc;

import org.codehaus.groovy.groovydoc.GroovyAnnotationRef;
import org.codehaus.groovy.groovydoc.GroovyClassDoc;

public class SimpleGroovyAnnotationRef implements GroovyAnnotationRef {
    private GroovyClassDoc type;
    private final String desc;
    private String name;

    public SimpleGroovyAnnotationRef(String name, String desc) {
        this.desc = desc;
        this.name = name;
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