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
package groovy.json;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.Writable;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A builder for creating JSON payloads.
 * <p>
 * This builder supports the usual builder syntax made of nested method calls and closures,
 * but also some specific aspects of JSON data structures, such as list of values, etc.
 * Please make sure to have a look at the various methods provided by this builder
 * to be able to learn about the various possibilities of usage.
 * <p>
 * Example:
 * <pre><code class="groovyTestCase">
 *       def builder = new groovy.json.JsonBuilder()
 *       def root = builder.people {
 *           person {
 *               firstName 'Guillame'
 *               lastName 'Laforge'
 *               // Named arguments are valid values for objects too
 *               address(
 *                       city: 'Paris',
 *                       country: 'France',
 *                       zip: 12345,
 *               )
 *               married true
 *               // a list of values
 *               conferences 'JavaOne', 'Gr8conf'
 *           }
 *       }
 *
 *       // creates a data structure made of maps (Json object) and lists (Json array)
 *       assert root instanceof Map
 *
 *       assert builder.toString() == '{"people":{"person":{"firstName":"Guillame","lastName":"Laforge","address":{"city":"Paris","country":"France","zip":12345},"married":true,"conferences":["JavaOne","Gr8conf"]}}}'
 * </code></pre>
 *
 * @since 1.8.0
 */
public class JsonBuilder extends GroovyObjectSupport implements Writable {

    private final JsonGenerator generator;
    private Object content;

    /**
     * Instantiates a JSON builder.
     */
    public JsonBuilder() {
        this.generator = JsonOutput.DEFAULT_GENERATOR;
    }

    /**
     * Instantiates a JSON builder with a configured generator.
     *
     * @param generator used to generate the output
     * @since 2.5.0
     */
    public JsonBuilder(JsonGenerator generator) {
        this.generator = generator;
    }

    /**
     * Instantiates a JSON builder with some existing data structure.
     *
     * @param content a pre-existing data structure
     */
    public JsonBuilder(Object content) {
        this.content = content;
        this.generator = JsonOutput.DEFAULT_GENERATOR;
    }

    /**
     * Instantiates a JSON builder with some existing data structure
     * and a configured generator.
     *
     * @param content a pre-existing data structure
     * @param generator used to generate the output
     * @since 2.5.0
     */
    public JsonBuilder(Object content, JsonGenerator generator) {
        this.content = content;
        this.generator = generator;
    }

    public Object getContent() {
        return content;
    }

    /**
     * Named arguments can be passed to the JSON builder instance to create a root JSON object
     * <p>
     * Example:
     * <pre><code class="groovyTestCase">
     * def json = new groovy.json.JsonBuilder()
     * json name: "Guillaume", age: 33
     *
     * assert json.toString() == '{"name":"Guillaume","age":33}'
     * </code></pre>
     *
     * @param m a map of key / value pairs
     * @return a map of key / value pairs
     */
    public Object call(Map m) {
        content = m;

        return content;
    }

    /**
     * A list of elements as arguments to the JSON builder creates a root JSON array
     * <p>
     * Example:
     * <pre><code class="groovyTestCase">
     * def json = new groovy.json.JsonBuilder()
     * def result = json([1, 2, 3])
     *
     * assert result instanceof List
     * assert json.toString() == "[1,2,3]"
     * </code></pre>
     *
     * @param l a list of values
     * @return a list of values
     */
    public Object call(List l) {
        content = l;

        return content;
    }

    /**
     * Varargs elements as arguments to the JSON builder create a root JSON array
     * <p>
     * Example:
     * <pre><code class="groovyTestCase">
     * def json = new groovy.json.JsonBuilder()
     * def result = json 1, 2, 3
     *
     * assert result instanceof List
     * assert json.toString() == "[1,2,3]"
     * </code></pre>
     *
     * @param args an array of values
     * @return a list of values
     */
    public Object call(Object... args) {
        List<Object> listContent = new ArrayList<>();
        Collections.addAll(listContent, args);
        content = listContent;

        return content;
    }

    /**
     * A collection and closure passed to a JSON builder will create a root JSON array applying
     * the closure to each object in the collection
     * <p>
     * Example:
     * <pre><code class="groovyTestCase">
     * class Author {
     *      String name
     * }
     * def authors = [new Author (name: "Guillaume"), new Author (name: "Jochen"), new Author (name: "Paul")]
     *
     * def json = new groovy.json.JsonBuilder()
     * json authors, { Author author ->
     *      name author.name
     * }
     *
     * assert json.toString() == '[{"name":"Guillaume"},{"name":"Jochen"},{"name":"Paul"}]'
     * </code></pre>
     * @param coll a collection
     * @param c a closure used to convert the objects of coll
     * @return a list of values
     */
    public Object call(Iterable coll, Closure c) {
        List<Object> listContent = new ArrayList<>();
        if (coll != null) {
            for (Object it : coll) {
                listContent.add(JsonDelegate.curryDelegateAndGetContent(c, it));
            }
        }
        content = listContent;

        return content;
    }

    /**
     * Delegates to {@link #call(Iterable, Closure)}
     */
    public Object call(Collection coll, Closure c) {
        return call((Iterable)coll, c);
    }

    /**
     * A closure passed to a JSON builder will create a root JSON object
     * <p>
     * Example:
     * <pre><code class="groovyTestCase">
     * def json = new groovy.json.JsonBuilder()
     * def result = json {
     *      name "Guillaume"
     *      age 33
     * }
     *
     * assert result instanceof Map
     * assert json.toString() == '{"name":"Guillaume","age":33}'
     * </code></pre>
     *
     * @param c a closure whose method call statements represent key / values of a JSON object
     * @return a map of key / value pairs
     */
    public Object call(Closure c) {
        content = JsonDelegate.cloneDelegateAndGetContent(c);

        return content;
    }

    /**
     * A method call on the JSON builder instance will create a root object with only one key
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
     * def json = new groovy.json.JsonBuilder()
     * def result = json.person {
     *      name "Guillaume"
     *      age 33
     * }
     *
     * assert result instanceof Map
     * assert json.toString() == '{"person":{"name":"Guillaume","age":33}}'
     * </code></pre>
     *
     * Or alternatively with a method call taking named arguments:
     * <pre><code class="groovyTestCase">
     * def json = new groovy.json.JsonBuilder()
     * json.person name: "Guillaume", age: 33
     *
     * assert json.toString() == '{"person":{"name":"Guillaume","age":33}}'
     * </code></pre>
     *
     * If you use named arguments and a closure as last argument,
     * the key/value pairs of the map (as named arguments)
     * and the key/value pairs represented in the closure
     * will be merged together &mdash;
     * the closure properties overriding the map key/values
     * in case the same key is used.
     * <pre><code class="groovyTestCase">
     * def json = new groovy.json.JsonBuilder()
     * json.person(name: "Guillaume", age: 33) { town "Paris" }
     *
     * assert json.toString() == '{"person":{"name":"Guillaume","age":33,"town":"Paris"}}'
     * </code></pre>
     *
     * The empty args call will create a key whose value will be an empty JSON object:
     * <pre><code class="groovyTestCase">
     * def json = new groovy.json.JsonBuilder()
     * json.person()
     *
     * assert json.toString() == '{"person":{}}'
     * </code></pre>
     *
     * @param name the single key
     * @param args the value associated with the key
     * @return a map with a single key
     */
    public Object invokeMethod(String name, Object args) {
        if (args != null && Object[].class.isAssignableFrom(args.getClass())) {
            Object[] arr = (Object[]) args;
            if (arr.length == 0) {
                return setAndGetContent(name, new HashMap<String, Object>());
            } else if (arr.length == 1) {
                if (arr[0] instanceof Closure) {
                    return setAndGetContent(name, JsonDelegate.cloneDelegateAndGetContent((Closure) arr[0]));
                } else if (arr[0] instanceof Map) {
                    return setAndGetContent(name, arr[0]);
                }
            } else if (arr.length == 2) {
                final Object first = arr[0];
                final Object second = arr[1];
                if (second instanceof Closure) {
                    final Closure closure = (Closure)second;
                    if (first instanceof Map) {
                        Map subMap = new LinkedHashMap();
                        subMap.putAll((Map) first);
                        subMap.putAll(JsonDelegate.cloneDelegateAndGetContent(closure));

                        return setAndGetContent(name, subMap);
                    } else if (first instanceof Iterable) {
                        List<Map<String, Object>> list = collectContentForEachEntry((Iterable) first, closure);

                        return setAndGetContent(name, list);
                    } else if (first != null && first.getClass().isArray()) {
                        final Iterable coll = Arrays.asList((Object[])first);
                        List<Map<String, Object>> list = collectContentForEachEntry(coll, closure);

                        return setAndGetContent(name, list);
                    }
                }
            }

            throw new JsonException("Expected no arguments, a single map, a single closure, or a map and closure as arguments.");
        } else {
            return setAndGetContent(name, new HashMap<String, Object>());
        }
    }

    private static List<Map<String, Object>> collectContentForEachEntry(Iterable coll, Closure closure) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object it : coll) {
            list.add(JsonDelegate.curryDelegateAndGetContent(closure, it));
        }
        return list;
    }

    private Object setAndGetContent(String name, Object value) {
        Map<String, Object> contentMap = new LinkedHashMap<>();
        contentMap.put(name, value);
        content = contentMap;

        return content;
    }

    /**
     * Serializes the internal data structure built with the builder to a conformant JSON payload string
     * <p>
     * Example:
     * <pre><code class="groovyTestCase">
     * def json = new groovy.json.JsonBuilder()
     * json { temperature 37 }
     *
     * assert json.toString() == '{"temperature":37}'
     * </code></pre>
     *
     * @return a JSON output
     */
    public String toString() {
        return generator.toJson(content);
    }

    /**
     * Pretty-prints and formats the JSON payload.
     * <p>
     * This method calls the JsonLexer to parser the output of the builder,
     * so this may not be an optimal method to call,
     * and should be used mainly for debugging purpose
     * for a human-readable output of the JSON content.
     *
     * @return a pretty printed JSON output
     */
    public String toPrettyString() {
        return JsonOutput.prettyPrint(toString());
    }

    /**
     * The JSON builder implements the <code>Writable</code> interface,
     * so that you can have the builder serialize itself the JSON payload to a writer.
     * <p>
     * Example:
     * <pre><code class="groovyTestCase">
     * def json = new groovy.json.JsonBuilder()
     * json { temperature 37 }
     *
     * def out = new StringWriter()
     * out << json
     *
     * assert out.toString() == '{"temperature":37}'
     * </code></pre>
     *
     * @param out a writer on which to serialize the JSON payload
     * @return the writer
     */
    public Writer writeTo(Writer out) throws IOException {
        return out.append(toString());
    }
}
