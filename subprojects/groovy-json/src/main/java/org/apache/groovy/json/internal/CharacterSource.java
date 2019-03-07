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

public interface CharacterSource {

    /**
     * Skip white space.
     */
    void skipWhiteSpace();

    /**
     * returns the next character moving the file pointer or index to the next location.
     */
    int nextChar();

    /**
     * returns the current character without changing the IO pointer or index.
     */
    int currentChar();

    /**
     * Checks to see if there is a next character.
     */
    boolean hasChar();

    /**
     * Useful for finding constants in a string like true, false, etc.
     */
    boolean consumeIfMatch(char[] match);

    /**
     * This is mostly for debugging and testing.
     */
    int location();

    /**
     * Combines the operations of nextChar and hasChar.
     * Characters is -1 if not found which signifies end of file.
     * This might be preferable to avoid two method calls.
     */
    int safeNextChar();

    /**
     * Used to find strings and their ilk
     * Finds the next non-escaped char
     *
     * @param ch  character to find
     * @param esc escape character to avoid next char if escaped
     * @return list of chars until this is found.
     */
    char[] findNextChar(int ch, int esc);

    boolean hadEscape();

    /**
     * Reads a number from the character source.
     */
    char[] readNumber();

    String errorDetails(String message);
}
