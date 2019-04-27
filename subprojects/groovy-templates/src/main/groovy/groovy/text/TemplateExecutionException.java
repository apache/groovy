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
 * A custom exception class to flag template execution errors
 */
public class TemplateExecutionException extends Exception  {
    private static final long serialVersionUID = 3940987189684210921L;
    private int lineNumber;

    public TemplateExecutionException(int lineNumber) {
        super();
        this.lineNumber = lineNumber;
    }

    public TemplateExecutionException(int lineNumber, String message) {
        super(message);
        this.lineNumber = lineNumber;
    }

    public TemplateExecutionException(int lineNumber, String message, Throwable cause) {
        super(message, cause);
        this.lineNumber = lineNumber;
    }

    public TemplateExecutionException(int lineNumber, Throwable cause) {
        super(cause);
        this.lineNumber = lineNumber;
    }

    /**
     * Returns the line number in the template source where the error occurred
     *
     * @return the one-based line number of the template parsing error.
     */
    public int getLineNumber() {
        return lineNumber;
    }
}
