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
 * Unlike the JsonBuilder class which creates a data structure in memory,
 * which is handy in those situations where you want to alter the structure programmatically before output,
 * the StreamingJsonBuilder streams to a writter directly without any memory data structure.
 * So if you don't need to modify the structure, and want a more memory-efficient approach,
 * please use the StreamingJsonBuilder.
 * <p>
 * Example:
 * <pre class="groovyTestCase">
 *     new StringWriter().with { w ->
 *         def builder = new groovy.json.StreamingJsonBuilder( w )
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
 * @since 1.8.1
 */
class StreamingJsonBuilder {

    Writer writer
    
    /**
     * Instantiates a JSON builder, possibly with some existing data structure.
     *
     * @param writer A writer to which Json will be written
     * @param content a pre-existing data structure, default to null
     */
    StreamingJsonBuilder( Writer writer, content = null) {
        this.writer = writer
        if( content ) writer.write( JsonOutput.toJson( content ) )
    }

    /**
     * Named arguments can be passed to the JSON builder instance to create a root JSON object
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * new StringWriter().with { w ->
     *   def json = new groovy.json.StreamingJsonBuilder( w )
     *   json name: "Tim", age: 31
     *
     *   assert w.toString() == '{"name":"Tim","age":31}'
     * }
     * </pre>
     *
     * @param m a map of key / value pairs
     * @return a map of key / value pairs
     */
    def call(Map m) {
        writer.write JsonOutput.toJson( m )
        return m
    }

    /**
     * A list of elements as arguments to the JSON builder creates a root JSON array
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * new StringWriter().with { w ->
     *   def json = new groovy.json.StreamingJsonBuilder( w )
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
    def call(List l) {
        writer.write( JsonOutput.toJson( l ) )
        return l
    }

    /**
     * Varargs elements as arguments to the JSON builder create a root JSON array
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * new StringWriter().with { w ->
     *   def json = new groovy.json.StreamingJsonBuilder( w )
     *   def result = json 1, 2, 3
     *
     *   assert result instanceof List
     *   assert w.toString() == "[1,2,3]"
     * }
     * </pre>

     * @param args an array of values
     * @return a list of values
     */
    def call(Object... args) {
        def l = args.toList()
        writer.write JsonOutput.toJson( l )
        return l
    }

    /**
     * A closure passed to a JSON builder will create a root JSON object
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * new StringWriter().with { w ->
     *   def json = new groovy.json.StreamingJsonBuilder( w )
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
    def call(Closure c) {
        writer.write( '{' )
        StreamingJsonDelegate.cloneDelegateAndGetContent( writer, c )
        writer.write( '}' )
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
     *     def json = new groovy.json.StreamingJsonBuilder( w )
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
     *     def json = new groovy.json.StreamingJsonBuilder( w )
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
     *     def json = new groovy.json.StreamingJsonBuilder( w )
     *     json.person(name: "Tim", age: 35) { town "Manchester" }
     *
     *     assert w.toString() == '{"person":{"name":"Tim","age":35,"town":"Manchester"}}'
     * }
     * </pre>
     *
     * The empty args call will create a key whose value will be an empty JSON object:
     * <pre class="groovyTestCase">
     * new StringWriter().with { w ->
     *     def json = new groovy.json.StreamingJsonBuilder( w )
     *     json.person()
     *
     *     assert w.toString() == '{"person":{}}'
     * }
     * </pre>
     *
     * @param name the single key
     * @param args the value associated with the key
     */
    def invokeMethod(String name, Object args) {
        if (args?.size() == 0) {
            writer.write JsonOutput.toJson( [(name): [:]] )
        } else if (args?.size() == 1) {
            if (args[0] instanceof Closure) {
                writer.write "{${JsonOutput.toJson( name )}:"
                this.call( args[0] )
                writer.write '}'
            } else if (args[0] instanceof Map) {
                writer.write JsonOutput.toJson( [(name): args[0]] )
            }
            else {
                throw new JsonException("Expected no arguments, a single map, a single closure, or a map and closure as arguments.")
            }
        } else if (args?.size() == 2 && args[0] instanceof Map && args[1] instanceof Closure) {
            writer.write "{${JsonOutput.toJson( name )}:{"
            args[0].eachWithIndex { it, idx ->
                if( idx > 0 ) writer.write ','
                writer.write JsonOutput.toJson( it.key )
                writer.write ':'
                writer.write JsonOutput.toJson( it.value )
            }
            StreamingJsonDelegate.cloneDelegateAndGetContent( writer, args[1], !args[0].size() )
            writer.write '}}'
        }
        else {
            throw new JsonException("Expected no arguments, a single map, a single closure, or a map and closure as arguments.")
        }
    }
}

class StreamingJsonDelegate {
    Writer writer
    boolean first
    
    StreamingJsonDelegate( Writer w, boolean first ) {
        this.writer = w
        this.first = first
    }
    
    def invokeMethod(String name, Object args) {
        if (args) {
            if( !first ) {
                writer.write ','
            }
            writer.write JsonOutput.toJson( name )
            writer.write ':'
            writer.write JsonOutput.toJson( args.size() == 1 ? args[0] : args.toList() )
            first = false
        }
    }

    static cloneDelegateAndGetContent(Writer w, Closure c, boolean first=true ) {
        def delegate = new StreamingJsonDelegate( w, first )
        Closure cloned = c.clone()
        cloned.delegate = delegate
        cloned.resolveStrategy = Closure.DELEGATE_FIRST
        cloned()
    }
}
