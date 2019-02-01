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
package org.codehaus.groovy.runtime.powerassert;

import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.control.SourceUnit;

/**
 * Indicates that the source text for an assertion statement is not available.
 */
public class SourceTextNotAvailableException extends RuntimeException {
    private static final long serialVersionUID = -3815868502019514479L;

    // only accepts AssertStatementS so that better error messages can be produced
    public SourceTextNotAvailableException(AssertStatement stat, SourceUnit unit, String msg) {
        super(String.format("%s for %s at (%d,%d)-(%d,%d) in %s",
                msg, stat.getBooleanExpression().getText(), stat.getLineNumber(), stat.getColumnNumber(),
                stat.getLastLineNumber(), stat.getLastColumnNumber(), unit.getName()));
    }
}
