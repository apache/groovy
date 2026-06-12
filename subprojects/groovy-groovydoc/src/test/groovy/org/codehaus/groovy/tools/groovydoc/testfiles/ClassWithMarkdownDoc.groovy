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

/// GROOVY-11542 stage 1 fixture: a class-level Markdown doc comment using
/// the triple-slash syntax. The **body** is treated as `Markdown` by the
/// groovydoc tool.
///
/// - first bullet
/// - second bullet
class ClassWithMarkdownDoc {

    /// Field-level Markdown doc. *emphasis* and `code`.
    public int answer = 42

    /// Method Markdown body.
    ///
    /// @param x an input
    /// @return the doubled value
    int twice(int x) { x * 2 }
}
