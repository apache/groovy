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
package org.codehaus.groovy.control.customizers.builder;

import groovy.lang.Closure;
import groovy.lang.GString;
import groovy.util.AbstractFactory;
import groovy.util.FactoryBuilderSupport;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

import java.util.Collection;
import java.util.Map;

/**
 * This factory allows the generation of an {@link ImportCustomizer import customizer}. You may embed several
 * elements:
 * <ul>
 *     <li><i>normal</i> for "regular" imports</li>
 *     <li><i>star</i> for "star" imports</li>
 *     <li><i>staticStar</i> for "static star" imports</li>
 *     <li><i>alias</i> for imports with alias</li>
 *     <li><i>staticMember</i> for static imports of individual members</li>
 * </ul>
 *
 * For example:
 * <pre><code>builder.imports {
 * alias 'AI', 'java.util.concurrent.atomic.AtomicInteger'
 * alias 'AL', 'java.util.concurrent.atomic.AtomicLong'
 *}</code></pre>
 *
 * @since 2.1.0
 */
public class ImportCustomizerFactory extends AbstractFactory {
    @Override
    public boolean isHandlesNodeChildren() {
        return true;
    }

    @Override
    public Object newInstance(final FactoryBuilderSupport builder, final Object name, final Object value, final Map attributes) throws InstantiationException, IllegalAccessException {
        ImportCustomizer customizer = new ImportCustomizer();
        addImport(customizer, value);
        return customizer;
    }

    private void addImport(final ImportCustomizer customizer, final Object value) {
        if (value==null) return;
        if (value instanceof Collection) {
            for (Object e : (Collection)value) {
                addImport(customizer, e);
            }
        } else if (value instanceof String) {
            customizer.addImports((String)value);
        } else if (value instanceof Class) {
            customizer.addImports(((Class) value).getName());
        } else if (value instanceof GString) {
            customizer.addImports(value.toString());
        } else {
            throw new RuntimeException("Unsupported import value type ["+value+"]");
        }
    }

    @Override
    public boolean onNodeChildren(final FactoryBuilderSupport builder, final Object node, final Closure childContent) {
        if (node instanceof ImportCustomizer) {
            Closure clone = (Closure) childContent.clone();
            clone.setDelegate(new ImportHelper((ImportCustomizer) node));
            clone.call();
        }
        return false;
    }

    private static final class ImportHelper {
        private final ImportCustomizer customizer;

        private ImportHelper(final ImportCustomizer customizer) {
            this.customizer = customizer;
        }

        protected void normal(String... names) {
            customizer.addImports(names);
        }
        protected void normal(Class... classes) {
            for (Class aClass : classes) {
                customizer.addImports(aClass.getName());
            }
        }

        protected void alias(String alias, String name) {
            customizer.addImport(alias, name);
        }
        protected void alias(String alias, Class clazz) {
            customizer.addImport(alias, clazz.getName());
        }

        protected void star(String... packages) {
            customizer.addStarImports(packages);
        }

        protected void staticStar(String... classNames) {
            customizer.addStaticStars(classNames);
        }
        protected void staticStar(Class... classes) {
            for (Class aClass : classes) {
                customizer.addStaticStars(aClass.getName());
            }
        }

        protected void staticMember(String name, String field) {
            customizer.addStaticImport(name, field);
        }
        protected void staticMember(String alias, String name, String field) {
            customizer.addStaticImport(alias, name, field);
        }

    }

}
