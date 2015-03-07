/*
 * Copyright 2003-2014 the original author or authors.
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
package groovy.json;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * A builder for creating JSON payloads.
 * <p>
 * This builder supports the usual builder syntax made of nested method calls and closures,
 * but also some specific aspects of JSON data structures, such as list of values, etc.
 * Please make sure to have a look at the various methods provided by this builder
 * to be able to learn about the various possibilities of usage.
 * <p>
 * Unlike the JsonBuilder class which creates a data structure in memory,
 * which is handy in those situations where you want to alter the structure programatically before output,
 * the StreamingJsonBuilder streams to a writer directly without any memory data structure.
 * So if you don't need to modify the structure, and want a more memory-efficient approach,
 * please use the StreamingJsonBuilder.
 * <p>
 * Example:
 * <pre class="groovyTestCase">
 *     new StringWriter().with { w ->
 *         def builder = new groovy.json.StreamingJsonBuilder(w)
 *         builder.people {
 *             person {
 *                 firstName 'Tim'
 *                 lastName 'Yates'
 *                 // Named arguments are valid values for objects too
 *                 address(
 *                     city: 'Manchester',
 *                     country: 'UK',
 *                     zip: 'M1 2AB',
 *                 )
 *                 living true
 *                 eyes 'left', 'right'
 *             }
 *         }
 *
 *         assert w.toString() == '{"people":{"person":{"firstName":"Tim","lastName":"Yates","address":{"city":"Manchester","country":"UK","zip":"M1 2AB"},"living":true,"eyes":["left","right"]}}}'
 *    }
 * </pre>
 *
 * @author Tim Yates
 * @author Andrey Bloschetsov
 * @since 1.8.1
 */
public class StreamingJsonBuilder extends GroovyObjectSupport {

    private Writer writer;

    /**
     * Instantiates a JSON builder.
     *
     * @param writer A writer to which Json will be written
     */
    public StreamingJsonBuilder(Writer writer) {
        this.writer = writer;
    }

    /**
     * Instantiates a JSON builder, possibly with some existing data structure.
     *
     * @param writer  A writer to which Json will be written
     * @param content a pre-existing data structure, default to null
     */
    public StreamingJsonBuilder(Writer writer, Object content) throws IOException {
        this(writer);
        if (content != null) {
            writer.write(JsonOutput.toJson(content));
        }
    }

    /**
     * Named arguments can be passed to the JSON builder instance to create a root JSON object
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * new StringWriter().with { w ->
     *   def json = new groovy.json.StreamingJsonBuilder(w)
     *   json name: "Tim", age: 31
     *
     *   assert w.toString() == '{"name":"Tim","age":31}'
     * }
     * </pre>
     *
     * @param m a map of key / value pairs
     * @return a map of key / value pairs
     */
    public Object call(Map m) throws IOException {
        writer.write(JsonOutput.toJson(m));

        return m;
    }

    /**
     * A list of elements as arguments to the JSON builder creates a root JSON array
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * new StringWriter().with { w ->
     *   def json = new groovy.json.StreamingJsonBuilder(w)
     *   def result = json([1, 2, 3])
     *
     *   assert result == [ 1, 2, 3 ]
     *   assert w.toString() == "[1,2,3]"
     * }
     * </pre>
     *
     * @param l a list of values
     * @return a list of values
     */
    public Object call(List l) throws IOException {
        writer.write(JsonOutput.toJson(l));

        return l;
    }

    /**
     * Varargs elements as arguments to the JSON builder create a root JSON array
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * new StringWriter().with { w ->
     *   def json = new groovy.json.StreamingJsonBuilder(w)
     *   def result = json 1, 2, 3
     *
     *   assert result instanceof List
     *   assert w.toString() == "[1,2,3]"
     * }
     * </pre>

     * @param args an array of values
     * @return a list of values
     */
    public Object call(Object... args) throws IOException {
        return call(Arrays.asList(args));
    }

    /**
     * A collection and closure passed to a JSON builder will create a root JSON array applying
     * the closure to each object in the collection
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * class Author {
     *      String name
     * }
     * def authors = [new Author (name: "Guillaume"), new Author (name: "Jochen"), new Author (name: "Paul")]
     *
     * new StringWriter().with { w ->
     *     def json = new groovy.json.StreamingJsonBuilder(w)
     *     json authors, { Author author ->
     *         name author.name
     *     }
     *
     *     assert w.toString() == '[{"name":"Guillaume"},{"name":"Jochen"},{"name":"Paul"}]'
     * }
     * </pre>
     * @param coll a collection
     * @param c a closure used to convert the objects of coll
     */
    public Object call(Collection coll, Closure c) throws IOException {
        StreamingJsonDelegate.writeCollectionWithClosure(writer, coll, c);

        return null;
    }

    /**
     * A closure passed to a JSON builder will create a root JSON object
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * new StringWriter().with { w ->
     *   def json = new groovy.json.StreamingJsonBuilder(w)
     *   json {
     *      name "Tim"
     *      age 39
     *   }
     *
     *   assert w.toString() == '{"name":"Tim","age":39}'
     * }
     * </pre>
     *
     * @param c a closure whose method call statements represent key / values of a JSON object
     */
    public Object call(Closure c) throws IOException {
        writer.write("{");
        StreamingJsonDelegate.cloneDelegateAndGetContent(writer, c);
        writer.write("}");

        return null;
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
     * <pre class="groovyTestCase">
     * new StringWriter().with { w ->
     *     def json = new groovy.json.StreamingJsonBuilder(w)
     *     json.person {
     *         name "Tim"
     *          age 28
     *     }
     *
     *     assert w.toString() == '{"person":{"name":"Tim","age":28}}'
     * }
     * </pre>
     *
     * Or alternatively with a method call taking named arguments:
     * <pre class="groovyTestCase">
     * new StringWriter().with { w ->
     *     def json = new groovy.json.StreamingJsonBuilder(w)
     *     json.person name: "Tim", age: 32
     *
     *     assert w.toString() == '{"person":{"name":"Tim","age":32}}'
     * }
     * </pre>
     *
     * If you use named arguments and a closure as last argument,
     * the key/value pairs of the map (as named arguments)
     * and the key/value pairs represented in the closure
     * will be merged together &mdash;
     * the closure properties overriding the map key/values
     * in case the same key is used.
     * <pre class="groovyTestCase">
     * new StringWriter().with { w ->
     *     def json = new groovy.json.StreamingJsonBuilder(w)
     *     json.person(name: "Tim", age: 35) { town "Manchester" }
     *
     *     assert w.toString() == '{"person":{"name":"Tim","age":35,"town":"Manchester"}}'
     * }
     * </pre>
     *
     * The empty args call will create a key whose value will be an empty JSON object:
     * <pre class="groovyTestCase">
     * new StringWriter().with { w ->
     *     def json = new groovy.json.StreamingJsonBuilder(w)
     *     json.person()
     *
     *     assert w.toString() == '{"person":{}}'
     * }
     * </pre>
     *
     * @param name the single key
     * @param args the value associated with the key
     */
    public Object invokeMethod(String name, Object args) {
        boolean notExpectedArgs = false;
        if (args != null && Object[].class.isAssignableFrom(args.getClass())) {
            Object[] arr = (Object[]) args;
            try {
                if (arr.length == 0) {
                    writer.write(JsonOutput.toJson(Collections.singletonMap(name, Collections.emptyMap())));
                } else if (arr.length == 1) {
                    if (arr[0] instanceof Closure) {
                        writer.write("{");
                        writer.write(JsonOutput.toJson(name));
                        writer.write(":");
                        call((Closure) arr[0]);
                        writer.write("}");
                    } else if (arr[0] instanceof Map) {
                        writer.write(JsonOutput.toJson(Collections.singletonMap(name, (Map) arr[0])));
                    } else {
                        notExpectedArgs = true;
                    }
                } else if (arr.length == 2 && arr[0] instanceof Map && arr[1] instanceof Closure) {
                    writer.write("{");
                    writer.write(JsonOutput.toJson(name));
                    writer.write(":{");
                    boolean first = true;
                    Map map = (Map) arr[0];
                    for (Object it : map.entrySet()) {
                        if (!first) {
                            writer.write(",");
                        } else {
                            first = false;
                        }

                        Map.Entry entry = (Map.Entry) it;
                        writer.write(JsonOutput.toJson(entry.getKey()));
                        writer.write(":");
                        writer.write(JsonOutput.toJson(entry.getValue()));
                    }
                    StreamingJsonDelegate.cloneDelegateAndGetContent(writer, (Closure) arr[1], map.size() == 0);
                    writer.write("}}");
                } else if (StreamingJsonDelegate.isCollectionWithClosure(arr)) {
                    writer.write("{");
                    writer.write(JsonOutput.toJson(name));
                    writer.write(":");
                    call((Collection) arr[0], (Closure) arr[1]);
                    writer.write("}");
                } else {
                    notExpectedArgs = true;
                }
            } catch (IOException ioe) {
                throw new JsonException(ioe);
            }
        } else {
            notExpectedArgs = true;
        }

        if (!notExpectedArgs) {
            return this;
        } else {
            throw new JsonException("Expected no arguments, a single map, a single closure, or a map and closure as arguments.");
        }
    }
}

class StreamingJsonDelegate extends GroovyObjectSupport {

    private Writer writer;
    private boolean first;

    public StreamingJsonDelegate(Writer w, boolean first) {
        this.writer = w;
        this.first = first;
    }

    public Object invokeMethod(String name, Object args) {
        if (args != null && Object[].class.isAssignableFrom(args.getClass())) {
            try {
                if (!first) {
                    writer.write(",");
                } else {
                    first = false;
                }
                writer.write(JsonOutput.toJson(name));
                writer.write(":");
                Object[] arr = (Object[]) args;

                if (arr.length == 1) {
                    writer.write(JsonOutput.toJson(arr[0]));
                } else if (isCollectionWithClosure(arr)) {
                    writeCollectionWithClosure(writer, (Collection) arr[0], (Closure) arr[1]);
                } else {
                    writer.write(JsonOutput.toJson(Arrays.asList(arr)));
                }
            } catch (IOException ioe) {
                throw new JsonException(ioe);
            }
        }

        return this;
    }

    public static boolean isCollectionWithClosure(Object[] args) {
        return args.length == 2 && args[0] instanceof Collection && args[1] instanceof Closure;
    }

    public static Object writeCollectionWithClosure(Writer writer, Collection coll, Closure closure) throws IOException {
        writer.write("[");
        boolean first = true;
        for (Object it : coll) {
            if (!first) {
                writer.write(",");
            } else {
                first = false;
            }

            writer.write("{");
            curryDelegateAndGetContent(writer, closure, it);
            writer.write("}");
        }
        writer.write("]");

        return writer;
    }

    public static void cloneDelegateAndGetContent(Writer w, Closure c) {
        cloneDelegateAndGetContent(w, c, true);
    }

    public static void cloneDelegateAndGetContent(Writer w, Closure c, boolean first) {
        StreamingJsonDelegate delegate = new StreamingJsonDelegate(w, first);
        Closure cloned = (Closure) c.clone();
        cloned.setDelegate(delegate);
        cloned.setResolveStrategy(Closure.DELEGATE_FIRST);
        cloned.call();
    }

    public static void curryDelegateAndGetContent(Writer w, Closure c, Object o) {
        curryDelegateAndGetContent(w, c, o, true);
    }

    public static void curryDelegateAndGetContent(Writer w, Closure c, Object o, boolean first) {
        StreamingJsonDelegate delegate = new StreamingJsonDelegate(w, first);
        Closure curried = c.curry(o);
        curried.setDelegate(delegate);
        curried.setResolveStrategy(Closure.DELEGATE_FIRST);
        curried.call();
    }
}
