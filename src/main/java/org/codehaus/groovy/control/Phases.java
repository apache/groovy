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
 * Compilation phase identifiers.
 */
public class Phases {
    /** Opening of files and such */
    public static final int INITIALIZATION = 1;
    /** Lexing, parsing, and AST building */
    public static final int PARSING = 2;
    /** CST to AST conversion */
    public static final int CONVERSION = 3;
    /** AST semantic analysis and elucidation */
    public static final int SEMANTIC_ANALYSIS = 4;
    /** AST completion */
    public static final int CANONICALIZATION = 5;
    /** Class generation (pt.1) */
    public static final int INSTRUCTION_SELECTION = 6;
    /** Class generation (pt.2) */
    public static final int CLASS_GENERATION = 7;   //
    /** Output of class to disk */
    public static final int OUTPUT = 8;
    /** Cleanup */
    public static final int FINALIZATION = 9;

    /** Synonym for full compilation */
    public static final int ALL = FINALIZATION;

    // TODO: If no reference is made to array, convert to switch in getDescription(int).
    public static final String[] descriptions = {
        "startup",
        "initialization",
        "parsing",
        "conversion",
        "semantic analysis",
        "canonicalization",
        "instruction selection",
        "class generation",
        "output",
        "cleanup",
    };

    /**
     * Returns a description of the specified phase.
     */
    public static String getDescription(final int phase) {
        return descriptions[phase];
    }
}
