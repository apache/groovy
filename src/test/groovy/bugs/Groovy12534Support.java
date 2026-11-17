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
package bugs;

import java.util.ArrayList;

/**
 * List subclass with a public field and a one-argument setter.
 * Static compilation must treat both as members of the list, not of its elements.
 */
public class Groovy12534Support extends ArrayList<String> {

    public String tag = "t";

    private String mark = "m";

    /**
     * Not a bean setter. Declared before the one-argument setter so a property
     * write scans this overload and then the real setter.
     */
    public void setMark(String value, String ignored) {
        this.mark = value;
    }

    public void setMark(String value) {
        this.mark = value;
    }

    public String getMark() {
        return this.mark;
    }
}
