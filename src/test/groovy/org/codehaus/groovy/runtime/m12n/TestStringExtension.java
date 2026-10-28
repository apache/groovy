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
package org.codehaus.groovy.runtime.m12n;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestStringExtension {

    public static String reverseToUpperCase(String self) {
        return new StringBuilder(self.toUpperCase()).reverse().toString();
    }

    public static String getString(List<String> self) {
        return self.get(0);
    }

    public static <CS extends CharSequence> CharSequence getSequence(List<CS> self) {
        return self.get(0);
    }

    public static String getJoined(List<String> self) {
        return String.join(",", self);
    }

    public static void setJoined(List<String> self, String value) {
        self.clear();
        if (value != null && !value.isEmpty()) {
            self.addAll(Arrays.asList(value.split(",")));
        }
    }

    public static String getArrayJoined(ArrayList<String> self) {
        return String.join(":", self);
    }

    public static boolean isPlural(List<?> self) {
        return self.size() > 1;
    }
}
