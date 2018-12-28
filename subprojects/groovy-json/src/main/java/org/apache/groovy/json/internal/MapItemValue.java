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
package org.apache.groovy.json.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This holds a mapping from value key to value value to maximize laziness.
 */
public class MapItemValue implements Map.Entry<String, Value> {

    final Value name;
    final Value value;

    private String key = null;

    private static final boolean internKeys = Boolean.parseBoolean(System.getProperty("groovy.json.implementation.internKeys", "false"));

    protected static ConcurrentHashMap<String, String> internedKeysCache;

    static {
        if (internKeys) {
            internedKeysCache = new ConcurrentHashMap<String, String>();
        }
    }

    public MapItemValue(Value name, Value value) {
        this.name = name;
        this.value = value;
    }

    public String getKey() {
        if (key == null) {
            if (internKeys) {
                key = name.toString();

                String keyPrime = internedKeysCache.get(key);
                if (keyPrime == null) {
                    key = key.intern();
                    internedKeysCache.put(key, key);
                } else {
                    key = keyPrime;
                }
            } else {
                key = name.toString();
            }
        }
        return key;
    }

    public Value getValue() {
        return value;
    }

    public Value setValue(Value value) {
        Exceptions.die("not that kind of Entry");
        return null;
    }
}
