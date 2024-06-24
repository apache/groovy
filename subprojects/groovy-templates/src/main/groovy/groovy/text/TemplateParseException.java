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
package groovy.text;

/**
 * A custom exception class to flag template parsing errors
 */
public class TemplateParseException extends RuntimeException {
    private static final long serialVersionUID = 1607958968337123274L;
    private final int lineNumber;
    private final int column;

    public TemplateParseException(int lineNumber, int column) {
        super();
        this.lineNumber = lineNumber;
        this.column = column;
    }

    public TemplateParseException(String message, int lineNumber, int column) {
        super(message);
        this.lineNumber = lineNumber;
        this.column = column;
    }

    public TemplateParseException(String message, Throwable cause, int lineNumber, int column) {
        super(message, cause);
        this.lineNumber = lineNumber;
        this.column = column;
    }

    public TemplateParseException(Throwable t, int lineNumber, int column) {
        super(t);
        this.lineNumber = lineNumber;
        this.column = column;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getColumn() {
        return column;
    }
}
