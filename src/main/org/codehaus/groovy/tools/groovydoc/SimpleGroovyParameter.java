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

import org.codehaus.groovy.groovydoc.*;

public class SimpleGroovyParameter implements GroovyParameter {
    private String name;
    private String typeName;
    private String defaultValue;
    private GroovyType type;

    public SimpleGroovyParameter(String name) {
        this.name = name;
    }

    public String defaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String name() {
        return name;
    }

    public String typeName() {
        if (type == null) {
            return typeName;
        }
        return type.simpleTypeName();
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    //	public GroovyAnnotationDesc[] annotations() {/*todo*/return null;}
    public GroovyType type() {
        return type;
    }

    /* for later class resolution */
    public void setType(GroovyType type) {
        this.type = type;
    }

    public boolean isTypeAvailable() {
        return !(type == null);
    }
}
