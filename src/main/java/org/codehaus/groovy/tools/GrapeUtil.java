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
package org.codehaus.groovy.tools;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility methods for parsing compact Grape dependency coordinates.
 */
public class GrapeUtil {
    /**
     * Parses a dependency coordinate in Maven or Ivy shorthand form into its
     * component parts.
     * <p>
     * Recognized forms:
     * <ul>
     *   <li>Maven: {@code group:module:version[:classifier][@ext]}</li>
     *   <li>Ivy:   {@code group#module;version} (translated internally to the Maven form before parsing)</li>
     * </ul>
     *
     * @param allstr the dependency coordinate to parse
     * @return a map containing any parsed {@code group}, {@code module},
     * {@code version}, {@code classifier}, and {@code ext} entries
     */
    public static Map<String, Object> getIvyParts(String allstr) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        if (allstr == null) return result;
        // Accept the Ivy shorthand "group#module;version" by translating its separators to
        // the Maven form before parsing. Neither '#' nor ';' is legal in a groupId, artifactId,
        // or version (Ivy ranges use '[]()' not '#;'), so a straight replace cannot collide
        // with the Maven form.
        allstr = allstr.replace('#', ':').replace(';', ':');
        String ext = "";
        String[] parts;
        if (allstr.contains("@")) {
            parts = allstr.split("@");
            if (parts.length > 2) return result;
            if (parts.length > 1) ext = parts[1];
            if (parts.length > 0) allstr = parts[0];
        }
        parts = allstr.split(":");
        if (parts.length > 4) return result;
        if (parts.length > 3) result.put("classifier", parts[3]);
        if (parts.length > 2) result.put("version", parts[2]);
        else result.put("version", "*");
        if (!ext.isEmpty()) result.put("ext", ext);
        if (parts.length > 1) result.put("module", parts[1]);
        if (parts.length > 0) result.put("group", parts[0]);
        return result;
    }
}
