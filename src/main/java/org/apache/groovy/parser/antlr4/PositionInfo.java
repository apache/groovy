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
package org.apache.groovy.parser.antlr4;

import java.util.Objects;

/**
 * Line and column attached to a parser syntax error. {@link #toString}
 * renders {@code  @ line N, column M}, or an empty string when either
 * coordinate is {@code -1}.
 */
public class PositionInfo {
    private int line;
    private int column;

    /**
     * Stores a source location. Coordinates are the caret position used by
     * the syntax error, including any offset the caller already applied.
     *
     * @param line 1-based line, or {@code -1} when the location is omitted
     * @param column 1-based column, or {@code -1} when the location is omitted
     */
    public PositionInfo(int line, int column) {
        this.line = line;
        this.column = column;
    }

    /**
     * Returns the stored line.
     *
     * @return the 1-based line, or {@code -1} when omitted
     */
    public int getLine() {
        return line;
    }

    /**
     * Replaces the stored line.
     *
     * @param line 1-based line, or {@code -1} to omit the location
     */
    public void setLine(int line) {
        this.line = line;
    }

    /**
     * Returns the stored column.
     *
     * @return the 1-based column, or {@code -1} when omitted
     */
    public int getColumn() {
        return column;
    }

    /**
     * Replaces the stored column.
     *
     * @param column 1-based column, or {@code -1} to omit the location
     */
    public void setColumn(int column) {
        this.column = column;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PositionInfo that = (PositionInfo) o;
        return line == that.line &&
                column == that.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(line, column);
    }

    @Override
    public String toString() {
        if (-1 == line || -1 == column) {
            return "";
        }

        return " @ line " + line + ", column " + column;
    }

}
