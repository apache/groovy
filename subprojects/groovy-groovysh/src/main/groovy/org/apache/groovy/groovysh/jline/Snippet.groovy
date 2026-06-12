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
package org.apache.groovy.groovysh.jline

/**
 * Stores a source fragment together with its snippet classification.
 */
class Snippet {
    /** Classification assigned to the snippet. */
    SnippetType type
    /** Source text stored for the snippet. */
    String snippet

    /**
     * Creates a typed snippet wrapper.
     *
     * @param type snippet classification
     * @param snippet source text to store
     */
    Snippet(SnippetType type, String snippet) {
        this.type = type
        this.snippet = snippet
    }
}
