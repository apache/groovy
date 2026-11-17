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

/**
 * Runtime support for {@code StreamingMarkupBuilder} closures bound to namespace-aware tag handlers.
 */
public class BaseMarkupBuilder extends Builder {
    /**
     * Creates a builder backed by the supplied namespace method map.
     *
     * @param namespaceMethodMap namespace-specific tag handler metadata
     */
    public BaseMarkupBuilder(final Map namespaceMethodMap) {
        super(namespaceMethodMap);
    }

    /** {@inheritDoc} */
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
                /**
                 * Passes {@code value} to the {@code yield} markup command.
                 *
                 * @param value the content to yield
                 * @return this sink
                 */
                @Override
                public Object leftShift(final Object value) {
                    return leftShift("yield", value);
                }
            });
            specialProperties.put("unescaped", new OutputSink("unescaped") {
                /**
                 * Passes {@code value} to the {@code yieldUnescaped} markup command.
                 *
                 * @param value the content to yield without escaping
                 * @return this sink
                 */
                @Override
                public Object leftShift(final Object value) {
                    return leftShift("yieldUnescaped", value);
                }
            });
            specialProperties.put("namespaces", new OutputSink("namespaces") {
                /**
                 * Passes {@code value} to the {@code declareNamespace} markup command.
                 *
                 * @param value the namespace declaration
                 * @return this sink
                 */
                @Override
                public Object leftShift(final Object value) {
                    return leftShift("declareNamespace", value);
                }
            });
            specialProperties.put("pi", new OutputSink("pi") {
                /**
                 * Passes {@code value} to the {@code pi} markup command.
                 *
                 * @param value the processing-instruction content
                 * @return this sink
                 */
                @Override
                public Object leftShift(final Object value) {
                    return leftShift("pi", value);
                }
            });
            specialProperties.put("comment", new OutputSink("comment") {
                /**
                 * Passes {@code value} to the {@code comment} markup command.
                 *
                 * @param value the comment content
                 * @return this sink
                 */
                @Override
                public Object leftShift(final Object value) {
                    return leftShift("comment", value);
                }
            });
        }

        private abstract class OutputSink extends GroovyObjectSupport {
            private final String name;

            OutputSink(final String name) {
                this.name = name;
            }

            /**
             * Sets the document's namespace prefix to this sink's name, then
             * forwards the call to the document.
             *
             * @param name the method name to forward
             * @param args the arguments to forward
             * @return the document's result
             */
            @Override
            public Object invokeMethod(final String name, final Object args) {
                Document.this.prefix = this.name;
                return Document.this.invokeMethod(name, args);
            }

            /**
             * Shifts {@code item} into this sink. Each sink maps it onto one markup command.
             *
             * @param item the value being shifted
             * @return this sink
             */
            public abstract Object leftShift(Object item);

            /**
             * Resolves the {@code mkp} pseudo-namespace and invokes {@code command}
             * with {@code value}.
             *
             * @param command the markup command name
             * @param value the command argument
             * @return this sink
             */
            protected Object leftShift(final String command, final Object value) {
                Document.this.getProperty("mkp");
                Document.this.invokeMethod(command, new Object[]{value});
                return this;
            }
        }

        Document(final Closure root, final Map namespaceMethodMap) {
            super(root, namespaceMethodMap);
        }

        /**
         * Dispatches a tag call. A {@link Map} argument becomes the attributes,
         * a {@link Closure} is set as this document's delegate and used as a
         * writable body, and any other argument becomes the body. The current
         * prefix selects the namespace: a pending declaration, otherwise a
         * declared namespace, otherwise {@code ":"}. A named tag closure is
         * called when the namespace has one; otherwise the namespace default
         * is called. The prefix is cleared before that call.
         *
         * @param name the tag name
         * @param args the tag arguments
         * @return the tag closure's result
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
                } else if (arg instanceof Closure c) {

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

        /**
         * Returns the special sink registered for {@code property}
         * ({@code out}, {@code unescaped}, {@code namespaces}, {@code pi},
         * or {@code comment}). Any other name is stored as the current
         * namespace prefix and this document is returned.
         *
         * @param property the property name
         * @return the sink, or this document when the name is a prefix
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

        /**
         * When {@code property} is {@code trigger}, stores {@code newValue} as
         * the output destination and calls the root closure with this document.
         * Any other property is passed to the superclass.
         *
         * @param property the property name
         * @param newValue the value to assign
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
