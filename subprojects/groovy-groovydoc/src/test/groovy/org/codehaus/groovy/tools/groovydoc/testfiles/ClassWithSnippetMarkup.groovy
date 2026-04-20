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
 * GROOVY-11938 stage 3 fixture: {@snippet} markup comments.
 *
 * {@snippet lang="groovy" :
 * def greet(name) { "Hello, $name!" } // @highlight substring="greet" type="bold"
 * def aside = "TODO: fill this in"     // @replace substring="TODO: fill this in" replacement="see SiblingHelper"
 * // @link substring="SiblingHelper" target="SiblingHelper"
 * def helper = SiblingHelper.otherMethod()
 * }
 *
 * Region-scoped variant — a {@code region} attribute activates the directive
 * until a matching {@code // @end} marker:
 *
 * {@snippet lang="groovy" :
 * def regular = 1
 * // @highlight region="api" substring="call" type="highlighted"
 * def call(x) { x }
 * def call2(x) { x + 1 }
 * // @end region="api"
 * def afterRegion = "call"
 * }
 */
class ClassWithSnippetMarkup {
}
