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

class Utils {

    private Utils() {}

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

    static Object toObject(String json) {
        def slurper = new JsonSlurper(type: JsonParserType.LAX)
        slurper.parseText(json)
    }

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

    static String toJson(Object object) {
        String json = object instanceof String ? object : JsonOutput.toJson(object)
        (((json.startsWith("{") && json.endsWith("}"))
            || (json.startsWith("[") && json.endsWith("]"))) && json.length() > 5) ? JsonOutput.prettyPrint(json) : json
    }

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
