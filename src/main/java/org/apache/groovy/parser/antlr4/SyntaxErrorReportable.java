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

import groovy.lang.Tuple;
import groovy.lang.Tuple2;
import org.codehaus.groovy.syntax.SyntaxException;

/**
 * A SyntaxErrorReportable is a recognizer that can report syntax error
 */
public interface SyntaxErrorReportable {
    /**
     * A line offset of zero and a column offset of zero.
     */
    Tuple2<Integer, Integer> NO_OFFSET = Tuple.tuple(0, 0);

    /**
     * Throws a syntax error when {@code condition} is false. {@code offset}
     * is added to the current error column; the line is not adjusted.
     *
     * @param condition requirement that must hold
     * @param msg the error text
     * @param offset columns added to the current error column
     * @param toAttachPositionInfo whether the message includes the caret position
     */
    default void require(boolean condition, String msg, int offset, boolean toAttachPositionInfo) {
        require(condition, msg, Tuple.tuple(0, offset), toAttachPositionInfo);
    }

    /**
     * Throws a syntax error when {@code condition} is false. {@code offset}'s
     * first value is added to the error line and its second value to the error column.
     *
     * @param condition requirement that must hold
     * @param msg the error text
     * @param offset line adjustment, then column adjustment
     * @param toAttachPositionInfo whether the message includes the caret position
     */
    default void require(boolean condition, String msg, Tuple2<Integer, Integer> offset, boolean toAttachPositionInfo) {
        if (condition) {
            return;
        }

        this.throwSyntaxError(msg, offset, toAttachPositionInfo);
    }

    /**
     * Throws a syntax error when {@code condition} is false, without a position
     * adjustment. {@code toAttachPositionInfo} selects whether the message
     * includes the caret position.
     *
     * @param condition requirement that must hold
     * @param msg the error text
     * @param toAttachPositionInfo whether the message includes the caret position
     */
    default void require(boolean condition, String msg, boolean toAttachPositionInfo) {
        require(condition, msg, NO_OFFSET, toAttachPositionInfo);
    }

    /**
     * Throws a syntax error when {@code condition} is false, adding {@code offset}
     * to the current error column. The message does not repeat the caret position.
     *
     * @param condition requirement that must hold
     * @param msg the error text
     * @param offset columns added to the current error column
     */
    default void require(boolean condition, String msg, int offset) {
        require(condition, msg, Tuple.tuple(0, offset));
    }
    /**
     * Throws a syntax error when {@code condition} is false. {@code offset}'s
     * first value is added to the error line and its second value to the error column.
     * The message does not repeat the caret position.
     *
     * @param condition requirement that must hold
     * @param msg the error text
     * @param offset line adjustment, then column adjustment
     */
    default void require(boolean condition, String msg, Tuple2<Integer, Integer> offset) {
        require(condition, msg, offset,false);
    }

    /**
     * Throws a syntax error when {@code condition} is false, at the current
     * error position, without repeating that position in the message.
     *
     * @param condition requirement that must hold
     * @param msg the error text
     */
    default void require(boolean condition, String msg) {
        require(condition, msg, false);
    }

    /**
     * Throws a syntax error whose column is the current error column plus {@code offset}.
     *
     * @param msg the error text
     * @param offset columns added to the current error column
     * @param toAttachPositionInfo whether the message includes the caret position
     */
    default void throwSyntaxError(String msg, int offset, boolean toAttachPositionInfo) {
        throwSyntaxError(msg, Tuple.tuple(0, offset), toAttachPositionInfo);
    }
    /**
     * @param toAttachPositionInfo if {@code true}, append {@code " @ line N, column M"}
     *                             to the exception message. Leave {@code false} when the
     *                             error will be wrapped in {@link SyntaxException},
     *                             which already appends the same location (otherwise the
     *                             displayed message repeats the position).
     */
    default void throwSyntaxError(String msg, Tuple2<Integer, Integer> offset, boolean toAttachPositionInfo) {
        PositionInfo positionInfo = this.genPositionInfo(offset);
        throw new GroovySyntaxError(msg + (toAttachPositionInfo ? positionInfo.toString() : ""),
                this.getSyntaxErrorSource(),
                positionInfo.getLine(),
                positionInfo.getColumn()
        );
    }

    /**
     * Returns whether this reporter is the lexer or the parser.
     *
     * @return {@link GroovySyntaxError#LEXER} or {@link GroovySyntaxError#PARSER}
     */
    int getSyntaxErrorSource();

    /**
     * Builds a position from the current error line and the current error column
     * plus {@code offset}.
     *
     * @param offset columns added to the current error column
     * @return that position
     */
    default PositionInfo genPositionInfo(int offset) {
        return genPositionInfo(Tuple.tuple(0, offset));
    }
    /**
     * Builds a position by adding {@code offset} to the current error line and column.
     *
     * @param offset line adjustment, then column adjustment
     * @return that position
     */
    default PositionInfo genPositionInfo(Tuple2<Integer, Integer> offset) {
        return new PositionInfo(getErrorLine() + offset.getV1(), getErrorColumn() + offset.getV2());
    }

    /**
     * Returns the 1-based line of the current syntax error.
     *
     * @return the error line
     */
    int getErrorLine();

    /**
     * Returns the 1-based column of the current syntax error.
     *
     * @return the error column
     */
    int getErrorColumn();
}
