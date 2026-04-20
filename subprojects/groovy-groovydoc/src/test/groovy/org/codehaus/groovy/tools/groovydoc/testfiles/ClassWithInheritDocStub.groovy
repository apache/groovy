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
 * Base class with a documented method.
 */
class InheritDocBase {
    /**
     * Parent-level description.
     * @param x the input
     * @return the transformed value
     */
    String transform(String x) { x.toUpperCase() }
}

/**
 * GROOVY-3782 stub: before {@code {@inheritDoc}} is implemented, the tag
 * should at minimum not leave a stray {@code '{'} or mangled block tag in
 * the rendered output.
 */
class InheritDocChild extends InheritDocBase {
    /**
     * {@inheritDoc}
     */
    @Override
    String transform(String x) { x.toLowerCase() }
}
