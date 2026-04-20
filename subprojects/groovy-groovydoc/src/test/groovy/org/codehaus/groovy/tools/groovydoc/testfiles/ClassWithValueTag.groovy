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
package org.codehaus.groovy.tools.groovydoc.testfiles

/**
 * GROOVY-6016: demonstrates the {@code {@value}} inline tag referencing
 * constants. Substitutions should happen for bare literals, folded
 * constant expressions, string concatenations, and references to other
 * static-final fields (via {@code ExpressionUtils.transformInlineConstants}).
 *
 * Max allowed: {@value #MAX}
 * Greeting: {@value #GREETING}
 * Sum: {@value #SUM}
 * Combined: {@value #COMBINED}
 */
class ClassWithValueTag {
    /** Max allowed (bare: {@value}). */
    public static final int MAX = 42
    /** Greeting (bare: {@value}). */
    public static final String GREETING = "hello"
    public static final int SUM = 40 + 2
    public static final String COMBINED = "hel" + "lo"
    public static final int SMALL_TIMEOUT = 1000
    public static final int MEDIUM_TIMEOUT = 3000
    /** Big timeout in ms. Default: {@value}. */
    public static final int BIG_TIMEOUT = SMALL_TIMEOUT + MEDIUM_TIMEOUT
    /** Four is: {@value}. */
    public static final int FOUR = 'four'.size()
}
