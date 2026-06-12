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
 * GROOVY-8025: groovydoc used to NPE on annotations whose members are
 * closure expressions (e.g. Spock's {@code @IgnoreIf}). This fixture
 * exercises a closure-valued annotation on the class, a method, and a
 * field to verify the refactored {@code GroovydocVisitor} handles them
 * without error.
 */
@GroovyConditional({ System.getProperty('os.name') ==~ /.*Linux.*/ })
class ClassWithClosureInAnnotation {

    @GroovyConditional({ -> 1 + 1 == 2 })
    String annotatedField

    @GroovyConditional(value = { it > 0 })
    String annotatedMethod() {
        "hello"
    }
}

/** Helper annotation that accepts a closure value. */
@interface GroovyConditional {
    Class value()
}
