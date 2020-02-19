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
package org.codehaus.groovy.vmplugin.v9

import org.codehaus.groovy.control.ResolveVisitor
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.codehaus.groovy.vmplugin.VMPluginFactory
import org.junit.Test

import java.util.stream.Collectors

class ClassFinderTest {
    @Test
    void findGroovyClass() {
        Map<String, Set<String>> result = ClassFinder.find(GroovySystem.location.toURI(), "groovy/lang")
        assert ["groovy/lang"] == result.get("GString")?.toList()
        assert null == result.get("GroovydocHolder")
    }

    @Test
    void findGroovyClass2() {
        Map<String, Set<String>> result = ClassFinder.find(GroovySystem.location.toURI(), "groovy/util")
        assert ["groovy/util"] == result.get("NodeBuilder")?.toList()
    }

    @Test
    void findGroovyClass3() {
        Map<String, Set<String>> result = ClassFinder.find(org.codehaus.groovy.control.ResolveVisitor.location.toURI(), "org/codehaus/groovy/control")
        assert ["org/codehaus/groovy/control"] == result.get("ResolveVisitor")?.toList()
    }

    @Test
    void findGroovyClassRecursive() {
        Map<String, Set<String>> result = ClassFinder.find(GroovySystem.location.toURI(), "groovy/lang", true)
        assert ["groovy/lang"] == result.get("GString")?.toList()
        assert ["groovy/lang/groovydoc"] == result.get("GroovydocHolder")?.toList()
    }

    @Test
    void findJavaClass() {
        Map<String, Set<String>> result = ClassFinder.find(URI.create("jrt:/modules/java.base/"), "java/lang")
        assert ["java/lang"] == result.get("String")?.toList()
        assert null == result.get("MethodHandle")
    }

    @Test
    void findJavaClass2() {
        Map<String, Set<String>> result = ClassFinder.find(URI.create("jrt:/modules/java.base/"), "java/util")
        assert ["java/util"] == result.get("Map")?.toList()
    }

    @Test
    void findJavaClass3() {
        Map<String, Set<String>> result = ClassFinder.find(URI.create("jrt:/modules/java.base/"), "java/io")
        assert ["java/io"] == result.get("InputStream")?.toList()
    }

    @Test
    void findJavaClass4() {
        Map<String, Set<String>> result = ClassFinder.find(URI.create("jrt:/modules/java.base/"), "java/net")
        assert ["java/net"] == result.get("Inet4Address")?.toList()
    }

    @Test
    void findJavaClassRecursive() {
        Map<String, Set<String>> result = ClassFinder.find(URI.create("jrt:/modules/java.base/"), "java/lang", true)
        assert ["java/lang/invoke"] == result.get("MethodHandle")?.toList()
    }

    @Test
    void findJarClass() {
        Map<String, Set<String>> result = ClassFinder.find(org.antlr.v4.runtime.tree.ParseTree.location.toURI(), "org/antlr/v4/runtime/tree")
        assert ["org/antlr/v4/runtime/tree"] == result.get("ParseTree")?.toList()
        assert null == result.get("ParseTreePattern")
    }

    @Test
    void findJarClassRecursive() {
        Map<String, Set<String>> result = ClassFinder.find(org.antlr.v4.runtime.tree.ParseTree.location.toURI(), "org/antlr/v4/runtime/tree", true)
        assert ["org/antlr/v4/runtime/tree"] == result.get("ParseTree")?.toList()
        assert ["org/antlr/v4/runtime/tree/pattern"] == result.get("ParseTreePattern")?.toList()
    }

    @Test
    void defaultImportClasses() {
        Map<String, Set<String>> r1 = VMPluginFactory.getPlugin().getDefaultImportClasses(ResolveVisitor.DEFAULT_IMPORTS) as TreeMap<String, Set<String>>

        assert (ResolveVisitor.DEFAULT_IMPORTS as List).sort() == r1.values().stream().flatMap(e -> e.stream()).collect(Collectors.toSet()).sort()

        def r2 = [:] as TreeMap
        URI gsLocation = null;
        try {
            gsLocation = DefaultGroovyMethods.getLocation(GroovySystem.class).toURI();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        for (String prefix : ResolveVisitor.DEFAULT_IMPORTS) {
            String pn = prefix.substring(0, prefix.length() - 1).replace('.', '/');

            Map<String, Set<String>> map = Collections.emptyMap();
            if (pn.startsWith("java/")) {
                map = ClassFinder.find(URI.create("jrt:/modules/java.base/"), pn);
            } else if (pn.startsWith("groovy/")) {
                if (null != gsLocation) {
                    map = ClassFinder.find(gsLocation, pn);
                }
            }

            map = map.entrySet().stream()
                    .collect(
                            Collectors.toMap(
                                    (Map.Entry entry) -> entry.getKey(),
                                    (Map.Entry entry) -> entry.getValue().stream()
                                            .map(e -> e.replace('/', '.') + ".")
                                            .collect(Collectors.toSet())
                            )
                    );

            r2.putAll(map);
        }

        assert r1.size() == r2.size()
        assert r1.keySet() == r2.keySet()
        r1.keySet().each { c1 ->
            assert (r1.get(c1) as TreeSet) == (r2.get(c1) as TreeSet)
        }
    }
}
