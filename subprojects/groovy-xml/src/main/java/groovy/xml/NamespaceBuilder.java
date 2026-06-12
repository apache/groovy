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
package groovy.xml;

import groovy.util.BuilderSupport;

import java.util.Map;

/**
 * A helper class for creating namespaces for GroovyMarkup
 */
public class NamespaceBuilder {

    private BuilderSupport builder;

    /**
     * Creates a namespace-aware builder wrapper for a single default namespace URI.
     *
     * @param builder the builder to wrap
     * @param uri the namespace URI to associate with the default prefix
     * @return a namespace-aware builder wrapper
     */
    public static NamespaceBuilderSupport newInstance(BuilderSupport builder, String uri) {
        return new NamespaceBuilder(builder).namespace(uri);
    }

    /**
     * Creates a namespace-aware builder wrapper without any predefined namespace mappings.
     *
     * @param builder the builder to wrap
     * @return a namespace-aware builder wrapper
     */
    public static NamespaceBuilderSupport newInstance(BuilderSupport builder) {
        return new NamespaceBuilderSupport(builder);
    }

    /**
     * Creates a namespace-aware builder wrapper for a single namespace mapping.
     *
     * @param builder the builder to wrap
     * @param uri the namespace URI to associate
     * @param prefix the prefix to associate with {@code uri}
     * @return a namespace-aware builder wrapper
     */
    public static NamespaceBuilderSupport newInstance(BuilderSupport builder, String uri, String prefix) {
        return new NamespaceBuilder(builder).namespace(uri, prefix);
    }

    /**
     * Creates a namespace-aware builder wrapper using the supplied prefix-to-URI mappings.
     *
     * @param nsMap the namespace mappings to declare
     * @param builder the builder to wrap
     * @return a namespace-aware builder wrapper
     */
    public static NamespaceBuilderSupport newInstance(Map nsMap, BuilderSupport builder) {
        return new NamespaceBuilder(builder).declareNamespace(nsMap);
    }

    /**
     * Creates a helper that produces namespace-aware wrappers for the supplied builder.
     *
     * @param builder the builder to wrap
     */
    public NamespaceBuilder(BuilderSupport builder) {
        this.builder = builder;
    }

    /**
     * Associates a namespace URI with the default prefix.
     *
     * @param uri the namespace URI to associate
     * @return a namespace-aware builder wrapper
     */
    public NamespaceBuilderSupport namespace(String uri) {
        return namespace(uri, "");
    }

    /**
     * Associates a namespace URI with the supplied prefix.
     *
     * @param uri the namespace URI to associate
     * @param prefix the prefix to associate with {@code uri}
     * @return a namespace-aware builder wrapper
     */
    public NamespaceBuilderSupport namespace(String uri, String prefix) {
        return new NamespaceBuilderSupport(builder, uri, prefix);
    }

    /**
     * Declares multiple namespace mappings at once.
     *
     * @param ns the prefix-to-URI mappings to declare
     * @return a namespace-aware builder wrapper
     */
    public NamespaceBuilderSupport declareNamespace(Map ns) {
        return new NamespaceBuilderSupport(builder, ns);
    }
}
