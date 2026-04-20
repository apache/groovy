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
 * constants. The class doc below uses {@value #MAX} and {@value #GREETING}
 * which should be substituted with the constants' source-form values.
 *
 * Max allowed: {@value #MAX}
 * Greeting: {@value #GREETING}
 */
class ClassWithValueTag {
    public static final int MAX = 42
    public static final String GREETING = "hello"
}
