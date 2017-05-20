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
package org.apache.groovy.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Map utilities.
 */
public abstract class Maps {
    public static Map of(Object... args) {
        int length = args.length;

        if (0 != length % 2) {
            throw new IllegalArgumentException("the length of arguments should be a power of 2");
        }

        Map map = new LinkedHashMap();

        for (int i = 0, n = length / 2; i < n; i++) {
            int index = i * 2;

            map.put(args[index], args[index + 1]);
        }

        return Collections.unmodifiableMap(map);
    }
}
