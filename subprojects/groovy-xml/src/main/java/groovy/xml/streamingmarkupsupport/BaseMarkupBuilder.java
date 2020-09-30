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
package groovy.xml.streamingmarkupsupport;

import groovy.lang.Closure;
import groovy.lang.GroovyInterceptable;
import groovy.lang.GroovyObjectSupport;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BaseMarkupBuilder extends Builder {
    public BaseMarkupBuilder(final Map namespaceMethodMap) {
        super(namespaceMethodMap);
    }

    @Override
    public Object bind(final Closure root) {
        return new Document(root, this.namespaceMethodMap);
    }

    private static class Document extends Built implements GroovyInterceptable {
        private Object out;
        private final Map pendingNamespaces = new HashMap();
        private final Map namespaces = new HashMap();
        private final Map specialProperties = new HashMap();
        private String prefix = "";

        {

            namespaces.put("xml", "http://www.w3.org/XML/1998/namespace");             // built in namespace
            namespaces.put("mkp", "http://www.codehaus.org/Groovy/markup/keywords");   // pseudo namespace for markup keywords

            specialProperties.put("out", new OutputSink("out") {
                @Override
                public Object leftShift(final Object value) {
                    return leftShift("yield", value);
                }
            });
            specialProperties.put("unescaped", new OutputSink("unescaped") {
                @Override
                public Object leftShift(final Object value) {
                    return leftShift("yieldUnescaped", value);
                }
            });
            specialProperties.put("namespaces", new OutputSink("namespaces") {
                @Override
                public Object leftShift(final Object value) {
                    return leftShift("declareNamespace", value);
                }
            });
            specialProperties.put("pi", new OutputSink("pi") {
                @Override
                public Object leftShift(final Object value) {
                    return leftShift("pi", value);
                }
            });
            specialProperties.put("comment", new OutputSink("comment") {
                @Override
                public Object leftShift(final Object value) {
                    return leftShift("comment", value);
                }
            });
        }

        private abstract class OutputSink extends GroovyObjectSupport {
            private final String name;

            public OutputSink(final String name) {
                this.name = name;
            }

            @Override
            public Object invokeMethod(final String name, final Object args) {
                Document.this.prefix = this.name;
                return Document.this.invokeMethod(name, args);
            }

            public abstract Object leftShift(Object item);

            protected Object leftShift(final String command, final Object value) {
                Document.this.getProperty("mkp");
                Document.this.invokeMethod(command, new Object[]{value});
                return this;
            }
        }

        public Document(final Closure root, final Map namespaceMethodMap) {
            super(root, namespaceMethodMap);
        }

        /* (non-Javadoc)
           * @see groovy.lang.GroovyObject#invokeMethod(java.lang.String, java.lang.Object)
           */
        @Override
        public Object invokeMethod(final String name, final Object args) {
            final Object[] arguments = (Object[]) args;
            Map attrs = Collections.EMPTY_MAP;
            Object body = null;

            //
            // Sort the parameters out
            //
            for (int i = 0; i != arguments.length; i++) {
                final Object arg = arguments[i];

                if (arg instanceof Map) {
                    attrs = (Map) arg;
                } else if (arg instanceof Closure) {
                    final Closure c = ((Closure) arg);

                    c.setDelegate(this);
                    body = c.asWritable();
                } else {
                    body = arg;
                }
            }

            //
            // call the closure corresponding to the tag
            //
            final Object uri;

            if (this.pendingNamespaces.containsKey(this.prefix)) {
                uri = this.pendingNamespaces.get(this.prefix);
            } else if (this.namespaces.containsKey(this.prefix)) {
                uri = this.namespaces.get(this.prefix);
            } else {
                uri = ":";
            }

            final Object[] info = (Object[]) this.namespaceSpecificTags.get(uri);
            final Map tagMap = (Map) info[2];
            final Closure defaultTagClosure = (Closure) info[0];

            final String prefix = this.prefix;
            this.prefix = "";

            if (tagMap.containsKey(name)) {
                return ((Closure) tagMap.get(name)).call(this, this.pendingNamespaces, this.namespaces, this.namespaceSpecificTags, prefix, attrs, body, this.out);
            } else {
                return defaultTagClosure.call(name, this, this.pendingNamespaces, this.namespaces, this.namespaceSpecificTags, prefix, attrs, body, this.out);
            }
        }

        /* (non-Javadoc)
         * @see groovy.lang.GroovyObject#getProperty(java.lang.String)
         */
        @Override
        public Object getProperty(final String property) {
            final Object special = this.specialProperties.get(property);

            if (special == null) {
                this.prefix = property;
                return this;
            } else {
                return special;
            }
        }

        /* (non-Javadoc)
         * @see groovy.lang.GroovyObject#setProperty(java.lang.String, java.lang.Object)
         */
        @Override
        public void setProperty(String property, Object newValue) {
            if ("trigger".equals(property)) {
                this.out = newValue;
                this.root.call(this);
            } else {
                super.setProperty(property, newValue);
            }
        }
    }
}
