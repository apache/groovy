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

import java.math.BigDecimal;

/**
 * Extension properties for increment and decrement tests. State lives on the
 * receiver, and each accessor appends to {@code log}: a lower-case letter for
 * an extension method, an upper-case one for a real method.
 */
public class Groovy12630Extension {

    /**
     * Holds the state of the extension properties.
     */
    public static class Counter {
        public int n;
        public BigDecimal amount = BigDecimal.ONE;
        public String text = "a";
        public String log = "";

        /** Real setter of {@code total}, whose getter is an extension method. */
        public void setTotal(final int value) {
            log += "S";
            n = value;
        }

        /** Real getter of {@code score}, whose setter is an extension method. */
        public int getScore() {
            log += "G";
            return n;
        }
    }

    public static int getTally(final Counter self) {
        self.log += "g";
        return self.n;
    }

    public static void setTally(final Counter self, final int value) {
        self.log += "s";
        self.n = value;
    }

    public static BigDecimal getMoney(final Counter self) {
        self.log += "g";
        return self.amount;
    }

    public static void setMoney(final Counter self, final BigDecimal value) {
        self.log += "s";
        self.amount = value;
    }

    public static String getLetter(final Counter self) {
        self.log += "g";
        return self.text;
    }

    public static void setLetter(final Counter self, final String value) {
        self.log += "s";
        self.text = value;
    }

    public static int getTotal(final Counter self) {
        self.log += "g";
        return self.n;
    }

    public static void setScore(final Counter self, final int value) {
        self.log += "s";
        self.n = value;
    }
}
