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
package groovy.yaml;

import groovy.json.JsonBuilder;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.Writable;
import org.apache.groovy.yaml.util.YamlConverter;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *  A builder for creating YAML payloads.
 *
 *  @since 3.0.0
 */
public class YamlBuilder extends GroovyObjectSupport implements Writable {
    private JsonBuilder jsonBuilder;

    public YamlBuilder() {
        this.jsonBuilder = new JsonBuilder();
    }

    public Object getContent() {
        return jsonBuilder.getContent();
    }

    /**
     * Named arguments can be passed to the YAML builder instance to create a root YAML object
     * <p>
     * Example:
     * <pre><code class="groovyTestCase">
     * def yaml = new groovy.yaml.YamlBuilder()
     * yaml name: "Guillaume", age: 33
     *
     * assert yaml.toString() == '''---
     * name: "Guillaume"
     * age: 33
     * '''
     * </code></pre>
     *
     * @param m a map of key / value pairs
     * @return a map of key / value pairs
     */
    public Object call(Map m) {
        return jsonBuilder.call(m);
    }

    /**
     * A list of elements as arguments to the YAML builder creates a root YAML array
     * <p>
     * Example:
     * <pre><code class="groovyTestCase">
     * def yaml = new groovy.yaml.YamlBuilder()
     * def result = yaml([1, 2, 3])
     *
     * assert result instanceof List
     * assert yaml.toString() == '''---
     * - 1
     * - 2
     * - 3
     * '''
     * </code></pre>
     *
     * @param l a list of values
     * @return a list of values
     */
    public Object call(List l) {
        return jsonBuilder.call(l);
    }

    /**
     * Varargs elements as arguments to the YAML builder create a root YAML array
     * <p>
     * Example:
     * <pre><code class="groovyTestCase">
     * def yaml = new groovy.yaml.YamlBuilder()
     * def result = yaml 1, 2, 3
     *
     * assert result instanceof List
     * assert yaml.toString() == '''---
     * - 1
     * - 2
     * - 3
     * '''
     * </code></pre>
     *
     * @param args an array of values
     * @return a list of values
     */
    public Object call(Object... args) {
        return jsonBuilder.call(args);
    }

    /**
     * A collection and closure passed to a YAML builder will create a root YAML array applying
     * the closure to each object in the collection
     * <p>
     * Example:
     * <pre><code class="groovyTestCase">
     * class Author {
     *      String name
     * }
     * def authors = [new Author (name: "Guillaume"), new Author (name: "Jochen"), new Author (name: "Paul")]
     *
     * def yaml = new groovy.yaml.YamlBuilder()
     * yaml authors, { Author author {@code ->}
     *      name author.name
     * }
     *
     * assert yaml.toString() == '''---
     * - name: "Guillaume"
     * - name: "Jochen"
     * - name: "Paul"
     * '''
     * </code></pre>
     * @param coll a collection
     * @param c a closure used to convert the objects of coll
     * @return a list of values
     */
    public Object call(Iterable coll, Closure c) {
        return jsonBuilder.call(coll, c);
    }

    /**
     * Delegates to {@link #call(Iterable, Closure)}
     * @param coll
     * @param c
     */
    public Object call(Collection coll, Closure c) {
        return jsonBuilder.call(coll, c);
    }

    /**
     * A closure passed to a YAML builder will create a root YAML object
     * <p>
     * Example:
     * <pre><code class="groovyTestCase">
     * def yaml = new groovy.yaml.YamlBuilder()
     * def result = yaml {
     *      name "Guillaume"
     *      age 33
     * }
     *
     * assert result instanceof Map
     * assert yaml.toString() == '''---
     * name: "Guillaume"
     * age: 33
     * '''
     * </code></pre>
     *
     * @param c a closure whose method call statements represent key / values of a YAML object
     * @return a map of key / value pairs
     */
    public Object call(Closure c) {
        return jsonBuilder.call(c);
    }

    /**
     * A method call on the YAML builder instance will create a root object with only one key
     * whose name is the name of the method being called.
     * This method takes as arguments:
     * <ul>
     *     <li>a closure</li>
     *     <li>a map (ie. named arguments)</li>
     *     <li>a map and a closure</li>
     *     <li>or no argument at all</li>
     * </ul>
     * <p>
     * Example with a classical builder-style:
     * <pre><code class="groovyTestCase">
     * def yaml = new groovy.yaml.YamlBuilder()
     * def result = yaml.person {
     *      name "Guillaume"
     *      age 33
     * }
     *
     * assert result instanceof Map
     * assert yaml.toString() == '''---
     * person:
     *   name: "Guillaume"
     *   age: 33
     * '''
     * </code></pre>
     *
     * Or alternatively with a method call taking named arguments:
     * <pre><code class="groovyTestCase">
     * def yaml = new groovy.yaml.YamlBuilder()
     * yaml.person name: "Guillaume", age: 33
     *
     * assert yaml.toString() == '''---
     * person:
     *   name: "Guillaume"
     *   age: 33
     * '''
     * </code></pre>
     *
     * If you use named arguments and a closure as last argument,
     * the key/value pairs of the map (as named arguments)
     * and the key/value pairs represented in the closure
     * will be merged together &mdash;
     * the closure properties overriding the map key/values
     * in case the same key is used.
     * <pre><code class="groovyTestCase">
     * def yaml = new groovy.yaml.YamlBuilder()
     * yaml.person(name: "Guillaume", age: 33) { town "Paris" }
     *
     * assert yaml.toString() == '''---
     * person:
     *   name: "Guillaume"
     *   age: 33
     *   town: "Paris"
     * '''
     * </code></pre>
     *
     * The empty args call will create a key whose value will be an empty YAML object:
     * <pre><code class="groovyTestCase">
     * def yaml = new groovy.yaml.YamlBuilder()
     * yaml.person()
     *
     * assert yaml.toString() == '''---
     * person: {}
     * '''
     * </code></pre>
     *
     * @param name the single key
     * @param args the value associated with the key
     * @return a map with a single key
     */
    @Override
    public Object invokeMethod(String name, Object args) {
        return jsonBuilder.invokeMethod(name, args);
    }

    /**
     * Serializes the internal data structure built with the builder to a conformant YAML payload string
     * <p>
     * Example:
     * <pre><code class="groovyTestCase">
     * def yaml = new groovy.yaml.YamlBuilder()
     * yaml { temperature 37 }
     *
     * assert yaml.toString() == '''---
     * temperature: 37
     * '''
     * </code></pre>
     *
     * @return a YAML output
     */
    @Override
    public String toString() {
        return YamlConverter.convertJsonToYaml(new StringReader(jsonBuilder.toString()));
    }

    /**
     * The YAML builder implements the <code>Writable</code> interface,
     * so that you can have the builder serialize itself the YAML payload to a writer.
     * <p>
     * Example:
     * <pre><code class="groovyTestCase">
     * def yaml = new groovy.yaml.YamlBuilder()
     * yaml { temperature 37 }
     *
     * def out = new StringWriter()
     * out {@code <<} yaml
     *
     * assert out.toString() == '''---
     * temperature: 37
     * '''
     * </code></pre>
     *
     * @param out a writer on which to serialize the YAML payload
     * @return the writer
     */
    @Override
    public Writer writeTo(Writer out) throws IOException {
        return out.append(toString());
    }
}
