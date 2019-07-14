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

/**
 * A SyntaxErrorReportable is a recognizer that can report syntax error
 */
public interface SyntaxErrorReportable {
    default void require(boolean condition, String msg, int offset, boolean toAttachPositionInfo) {
        if (condition) {
            return;
        }

        this.throwSyntaxError(msg, offset, toAttachPositionInfo);
    }
    default void require(boolean condition, String msg, boolean toAttachPositionInfo) {
        require(condition, msg, 0, toAttachPositionInfo);
    }
    default void require(boolean condition, String msg, int offset) {
        require(condition, msg, offset,false);
    }
    default void require(boolean condition, String msg) {
        require(condition, msg, false);
    }

    default void throwSyntaxError(String msg, int offset, boolean toAttachPositionInfo) {
        PositionInfo positionInfo = this.genPositionInfo(offset);

        String message = msg + (toAttachPositionInfo ? positionInfo.toString() : "");
        if (isErrorRecovery()) {
            throw new GroovySyntaxException(message,
                    this.getSyntaxErrorSource(),
                    positionInfo.getLine(),
                    positionInfo.getColumn()
            );
        }

        throw new GroovySyntaxError(message,
                this.getSyntaxErrorSource(),
                positionInfo.getLine(),
                positionInfo.getColumn()
        );
    }

    int getSyntaxErrorSource();
    default PositionInfo genPositionInfo(int offset) {
        return new PositionInfo(getErrorLine(), getErrorColumn() + offset);
    }

    int getErrorLine();
    int getErrorColumn();
    boolean isErrorRecovery();
}
