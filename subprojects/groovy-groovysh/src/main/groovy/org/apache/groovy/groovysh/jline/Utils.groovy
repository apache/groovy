/*
 * Copyright (c) 2002-2021, the original author or authors.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 *
 * https://opensource.org/licenses/BSD-3-Clause
 */
package org.apache.groovy.groovysh.jline

import groovy.json.JsonOutput
import groovy.json.JsonParserType
import groovy.json.JsonSlurper
import org.codehaus.groovy.runtime.HandleMetaClass
import org.codehaus.groovy.runtime.typehandling.GroovyCastException

import java.nio.file.Files
import java.nio.file.Path

/**
 * Shared conversion helpers for rendering, parsing, and persisting shell values.
 */
class Utils {

    private Utils() {}

    /**
     * Renders a shell value as a human-readable string.
     *
     * @param object value to render
     * @return string representation suitable for shell output
     */
    static String toString(Object object) {
        if (object == null) {
            return 'null'
        } else if (object instanceof Collection) {
            return object.toListString()
        } else if (object instanceof Map) {
            return object.toMapString()
        }
        object.toString()
    }

    /**
     * Parses JSON text into the corresponding Groovy object graph.
     *
     * @param json JSON content to parse
     * @return parsed object graph
     */
    static Object toObject(String json) {
        def slurper = new JsonSlurper(type: JsonParserType.LAX)
        slurper.parseText(json)
    }

    /**
     * Converts an object into a map representation suited for shell persistence.
     *
     * @param object value to convert
     * @return map view of the object
     */
    static Map<String,Object> toMap(Object object) {
        Map<String,Object> out = [:]
        try {
            if (object instanceof Closure) {
                out['closure'] = object.getClass().getName()
            } else if (object instanceof HandleMetaClass) {
                out['HandleMetaClass'] = object.toString()
            } else {
                out = object != null ? object.properties : null
            }
            return out
        } catch (GroovyCastException e) {
            out[object.getClass().getSimpleName()] = object.toString()
        }
        out
    }

    /**
     * Serializes an object to JSON, pretty-printing structured values when useful.
     *
     * @param object value to serialize
     * @return JSON or plain string representation
     */
    static String toJson(Object object) {
        String json = object instanceof String ? object : JsonOutput.toJson(object)
        (((json.startsWith("{") && json.endsWith("}"))
            || (json.startsWith("[") && json.endsWith("]"))) && json.length() > 5) ? JsonOutput.prettyPrint(json) : json
    }

    /**
     * Writes an object to disk using the requested persistence format.
     *
     * @param file destination file
     * @param object value to persist
     * @param format output format to use
     */
    static void persist(Path file, Object object, GroovyEngine.Format format) {
        if (format == GroovyEngine.Format.JSON) {
            Files.writeString(file, JsonOutput.toJson(object))
        } else if (format == GroovyEngine.Format.NONE) {
            Files.writeString(file, toString(object))
        } else {
            throw new IllegalArgumentException()
        }
    }

}
