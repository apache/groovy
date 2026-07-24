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
package org.apache.groovy.parser.antlr4.internal;

import org.antlr.v4.runtime.CharStream;

/**
 * Recovering parser error strategy with the same friendly diagnostics as
 * {@link DescriptiveErrorStrategy}.
 * <p>
 * Uses ANTLR's default resync / single-token repair so multiple recognition
 * errors can be reported in one pass (IDE editing). Prefer construction via
 * {@link DescriptiveErrorStrategy#create(CharStream, boolean)}.
 * </p>
 * <p>
 * Callers must parse with LL prediction and error listeners installed. Hosts
 * should read diagnostics from
 * {@link org.codehaus.groovy.control.ErrorCollector}: a partial tree may still
 * fail during AST building after recovery.
 * </p>
 *
 * @see DescriptiveErrorStrategy
 * @see org.codehaus.groovy.control.CompilerConfiguration#ERROR_RECOVERY
 */
public final class RecoveringDescriptiveErrorStrategy extends AbstractFriendlyErrorStrategy {

    /**
     * @param charStream source character stream used for snippet extraction
     */
    public RecoveringDescriptiveErrorStrategy(final CharStream charStream) {
        super(charStream);
    }
}
