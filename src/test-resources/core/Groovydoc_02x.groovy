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

// Task #24: verify runtime-groovydoc (/**@) attachment on script members
// alongside GROOVY-8877 leading-doc scenarios. Runtime-doc attachment is
// performed by the parser (GroovydocManager), independent of the groovydoc
// visitor's HTML-generation lift — this fixture confirms the two systems
// aren't accidentally coupled for the member-level case.
//
// Known runtime-doc gaps (pre-existing, independent of GROOVY-8877):
//   * Script-level /**@: a leading /**@ on a bare script is not attached to
//     the synthesised Script class — no AST node to claim it.
//   * @Field lift: /**@ before an @Field declaration is lost when the AST
//     transform lifts the local into a FieldNode; the annotation isn't
//     carried across.
// Both are candidates for the separate runtime-groovydoc enhancement ticket.

/**@
 * Top-level method m.
 */
void m() {}

// /**@ on a script's top-level method reaches the synthesised method.
def mMethod = this.class.getDeclaredMethods().find { it.name == 'm' }
assert mMethod != null
assert mMethod.groovydoc.isPresent()
assert mMethod.groovydoc.content.contains('Top-level method m')
