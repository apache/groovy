/*
 * Copyright 2003-2007 the original author or authors.
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
package groovy.xml;

import java.util.Map;

import groovy.util.BuilderSupport;


/**
 * A helper class for creating namespaced GroovyMarkup
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class NamespaceBuilderSupport extends BuilderSupport {

    private final Object builder;
    private final String uri;
    private final String prefix;

    public NamespaceBuilderSupport(BuilderSupport builder, String uri) {
        this(builder, uri, "");
    }

    public NamespaceBuilderSupport(BuilderSupport builder, String uri, String prefix) {
        super(builder);
        this.builder = builder;
        this.uri = uri;
        this.prefix = prefix;
    }

    protected void setParent(Object parent, Object child) {
    }

    protected Object getName(String methodName) {
        return new QName(uri, methodName, prefix);
    }

    protected Object createNode(Object name) {
        return name;
    }

    protected Object createNode(Object name, Object value) {
        return name;
    }

    protected Object createNode(Object name, Map attributes) {
        return name;
    }

    protected Object createNode(Object name, Map attributes, Object value) {
        return name;
    }
}
