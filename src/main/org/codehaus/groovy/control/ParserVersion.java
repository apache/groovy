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
package org.codehaus.groovy.control;

/**
 * Represents the version of a parser
 *
 * @since 2.6.0
 */
public enum ParserVersion {
    /**
     * Before Groovy 2.6.0(including 2.6.0), the default version of parser is v2
     */
    V_2,

    /**
     * After Groovy 3.0.0(including 3.0.0), the default version of parser is v4(i.e. the new parser Parrot)
     */
    V_4("Parrot");

    private String name;

    ParserVersion() {
        this(null);
    }

    ParserVersion(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
