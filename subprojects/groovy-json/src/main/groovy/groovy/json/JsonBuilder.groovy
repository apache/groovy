/*
 * Copyright 2003-2012 the original author or authors.
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
package groovy.json

/**
 * A builder for creating JSON payloads.
 * <p>
 * This builder supports the usual builder syntax made of nested method calls and closures,
 * but also some specific aspects of JSON data structures, such as list of values, etc.
 * Please make sure to have a look at the various methods provided by this builder
 * to be able to learn about the various possibilities of usage.
 * <p>
 * Example:
 * <pre><code>
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
 * @author Guillaume Laforge
 * @since 1.8.0
 */
class JsonBuilder implements Writable {

    /**
     * Resulting data structure made of lists and maps, representing a JSON payload
     */
    def content

    /**
     * Instanciates a JSON builder, possibly with some existing data structure.
     *
     * @param content a pre-existing data structure, default to null
     */
    JsonBuilder(content = null) {
        this.content = content
    }

    /**
     * Named arguments can be passed to the JSON builder instance to create a root JSON object
     * <p>
     * Example:
     * <pre><code>
     * def json = new JsonBuilder()
     * json name: "Guillaume", age: 33
     *
     * assert json.toString() == '{"name":"Guillaume","age":33}'
     * </code></pre>
     *
     * @param m a map of key / value pairs
     * @return a map of key / value pairs
     */
    def call(Map m) {
        this.content = m
        return content
    }

    /**
     * A list of elements as arguments to the JSON builder creates a root JSON array
     * <p>
     * Example:
     * <pre><code>
     * def json = new JsonBuilder()
     * def result = json([1, 2, 3])
     *
     * assert result instanceof List
     * assert json.toString() == "[1,2,3]"
     * </code></pre>
     *
     * @param l a list of values
     * @return a list of values
     */
    def call(List l) {
        this.content = l
        return content
    }

    /**
     * Varargs elements as arguments to the JSON builder create a root JSON array
     * <p>
     * Example:
     * <pre><code>
     * def json = new JsonBuilder()
     * def result = json 1, 2, 3
     *
     * assert result instanceof List
     * assert json.toString() == "[1,2,3]"
     * </code></pre>

     * @param args an array of values
     * @return a list of values
     */
    def call(Object... args) {
        this.content = args.toList()
        return this.content
    }

    /**
     * A closure passed to a JSON builder will create a root JSON object
     * <p>
     * Example:
     * <pre><code>
     * def json = new JsonBuilder()
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
    def call(Closure c) {
        this.content = JsonDelegate.cloneDelegateAndGetContent(c)
        return content
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
     * Example with a classicala builder-style:
     * <pre><code>
     * def json = new JsonBuilder()
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
     * <pre><code>
     * def json = new JsonBuilder()
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
     * <pre><code>
     * def json = new JsonBuilder()
     * json.person(name: "Guillaume", age: 33) { town "Paris" }
     *
     * assert json.toString() == '{"person":{"name":"Guillaume","age":33,"town":"Paris"}}'
     * </code></pre>
     *
     * The empty args call will create a key whose value will be an empty JSON object:
     * <pre><code>
     * def json = new JsonBuilder()
     * json.person()
     *
     * assert json.toString() == '{"person":{}}'
     * </code></pre>
     *
     * @param name the single key
     * @param args the value associated with the key
     * @return a map with a single key
     */
    def invokeMethod(String name, Object args) {
        if (args?.size() == 0) {
            this.content = [(name): [:]]
            return content
        } else if (args?.size() == 1) {
            if (args[0] instanceof Closure) {
                this.content = [(name): JsonDelegate.cloneDelegateAndGetContent(args[0])]
                return content
            } else if (args[0] instanceof Map) {
                this.content = [(name): args[0]]
                return content
            }
        } else if (args?.size() == 2 && args[0] instanceof Map && args[1] instanceof Closure) {
            this.content = [(name): [*:args[0], *:JsonDelegate.cloneDelegateAndGetContent(args[1])]]
            return content
        }

        throw new JsonException("Expected no arguments, a single map, a single closure, or a map and closure as arguments.")
    }

    /**
     * Serializes the internal data structure built with the builder to a conformant JSON payload string
     * <p>
     * Example:
     * <pre><code>
     * def json = new JsonBuilder()
     * json { temperature 37 }
     * 
     * assert json.toString() == '{"temperature":37}'
     * </code></pre>
     *
     * @return a JSON output
     */
    String toString() {
        JsonOutput.toJson(content)
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
    String toPrettyString() {
        JsonOutput.prettyPrint(toString())
    }

    /**
     * The JSON builder implements the <code>Writable</code> interface,
     * so that you can have the builder serialize itself the JSON payload to a writer.
     * <p>
     * Example:
     * <pre><code>
     * def json = new JsonBuilder()
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
    Writer writeTo(Writer out) {
        out << toString()
    }
}