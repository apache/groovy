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

public class GrapeUtil {
    public static Map<String, Object> getIvyParts(String allstr) {
        Map<String, Object> result = new LinkedHashMap<>();
        String ext = "";
        String[] parts;
        if (allstr.contains("@")) {
            parts = allstr.split("@");
            if (parts.length > 2) return result;
            allstr = parts[0];
            ext = parts[1];
        }
        parts = allstr.split(":");
        if (parts.length > 4) return result;
        if (parts.length > 3) result.put("classifier", parts[3]);
        if (parts.length > 2) result.put("version", parts[2]);
        else result.put("version", "*");
        if (ext.length() > 0) result.put("ext", ext);
        result.put("module", parts[1]);
        result.put("group", parts[0]);
        return result;
    }
}
