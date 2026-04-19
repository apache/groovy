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
package org.codehaus.groovy.tools.groovydoc.testfiles.anno

import groovy.transform.EqualsAndHashCode
import groovy.transform.NamedParam
import groovy.transform.NamedVariant

// GROOVY-4634: groovydoc now filters by @Documented (like Javadoc). @Internal
// (not @Documented) should be filtered out; user-facing transform annotations
// like @EqualsAndHashCode, @NamedVariant, @NamedParam, @Generated keep showing.
@EqualsAndHashCode(cache = true)
class Groovy implements Serializable {
    @groovy.transform.Internal
    public String annotatedField

    @Deprecated List annotatedProperty

    @NamedVariant
    Groovy(@NamedParam List ctorParam) {}

    @NamedVariant
    void annotatedMethod(@NamedParam(required = true) String methodParam) {}
}
