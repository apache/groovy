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

/**
 * Extension properties whose state lives on the receiver, so tests do not share mutable statics.
 */
public class Groovy12524Extension {

    /**
     * A receiver with a real {@code getLabel}/{@code getTag} so compound assignment
     * resolves the write to an extension setter and the read to the instance getter.
     * State stays on the instance.
     */
    public static class Holder {
        public String value = "";

        public String getLabel() {
            return value;
        }

        public String getTag() {
            return value;
        }
    }

    public static int getGroovy12524(int[] self) {
        return self[0];
    }

    public static void setGroovy12524(int[] self, int value) {
        self[0] = value;
    }

    public static boolean isGroovy12524On(boolean[] self) {
        return self[0];
    }

    public static void setGroovy12524On(boolean[] self, boolean value) {
        self[0] = value;
    }

    /** Zero-arg instance getter used when the read is not already an extension call. */
    public static String getLabel(Holder self) {
        return self.value;
    }

    /** Same name, extra argument: not a property getter. */
    public static String getLabel(Holder self, String ignored) {
        return self.value;
    }

    /** Same bean name as {@link #getLabel(Holder)} but not a boolean {@code is} getter. */
    public static String isLabel(Holder self) {
        return self.value;
    }

    public static void setLabel(Holder self, String label) {
        self.value = label;
    }

    public static String getTag(Holder self) {
        return self.value;
    }

    public static boolean isTag(Holder self) {
        return self.value != null && !self.value.isEmpty();
    }

    public static void setTag(Holder self, String tag) {
        self.value = tag;
    }

    /** Primitive receiver, setter only: the property read has no extension getter. */
    public static void setGroovy12524Raw(int self, int value) {
    }
}
