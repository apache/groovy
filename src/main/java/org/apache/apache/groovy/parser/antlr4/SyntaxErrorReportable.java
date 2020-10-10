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

/**
 * A SyntaxErrorReportable is a recognizer that can report syntax error
 */
public interface SyntaxErrorReportable {
    Tuple2<Integer, Integer> NO_OFFSET = Tuple.tuple(0, 0);

    default void require(boolean condition, String msg, int offset, boolean toAttachPositionInfo) {
        require(condition, msg, Tuple.tuple(0, offset), toAttachPositionInfo);
    }
    default void require(boolean condition, String msg, Tuple2<Integer, Integer> offset, boolean toAttachPositionInfo) {
        if (condition) {
            return;
        }

        this.throwSyntaxError(msg, offset, toAttachPositionInfo);
    }

    default void require(boolean condition, String msg, boolean toAttachPositionInfo) {
        require(condition, msg, NO_OFFSET, toAttachPositionInfo);
    }

    default void require(boolean condition, String msg, int offset) {
        require(condition, msg, Tuple.tuple(0, offset));
    }
    default void require(boolean condition, String msg, Tuple2<Integer, Integer> offset) {
        require(condition, msg, offset,false);
    }

    default void require(boolean condition, String msg) {
        require(condition, msg, false);
    }

    default void throwSyntaxError(String msg, int offset, boolean toAttachPositionInfo) {
        throwSyntaxError(msg, Tuple.tuple(0, offset), toAttachPositionInfo);
    }
    default void throwSyntaxError(String msg, Tuple2<Integer, Integer> offset, boolean toAttachPositionInfo) {
        PositionInfo positionInfo = this.genPositionInfo(offset);
        throw new GroovySyntaxError(msg + (toAttachPositionInfo ? positionInfo.toString() : ""),
                this.getSyntaxErrorSource(),
                positionInfo.getLine(),
                positionInfo.getColumn()
        );
    }

    int getSyntaxErrorSource();

    default PositionInfo genPositionInfo(int offset) {
        return genPositionInfo(Tuple.tuple(0, offset));
    }
    default PositionInfo genPositionInfo(Tuple2<Integer, Integer> offset) {
        return new PositionInfo(getErrorLine() + offset.getV1(), getErrorColumn() + offset.getV2());
    }

    int getErrorLine();
    int getErrorColumn();
}
