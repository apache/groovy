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
package org.codehaus.groovy.tools.ast

import groovy.test.GroovyTestCase
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Unit test that exercises the TransformTestHelper.
 */
class TransformTestHelperTest extends GroovyTestCase {

    void testParseFile() {

        def file = File.createTempFile('TransformTestHelperTest', '.groovy')
        file.deleteOnExit()
        file.text = ' 4+2 '

        def observer = new ObservingTransformation()
        def helper = new TransformTestHelper(observer, CompilePhase.CANONICALIZATION)
        def clazz = helper.parse(file)
        assert observer.wasCalled.get(), "The Transformation was not called"
        assert 6 == clazz.newInstance().run()
    }

    void testParseString() {

        def observer = new ObservingTransformation()
        def helper = new TransformTestHelper(observer, CompilePhase.CANONICALIZATION)
        def clazz = helper.parse(' 4+2 ')
        assert observer.wasCalled.get(), "The Transformation was not called"
        assert 6 == clazz.newInstance().run()
    }

}

class ObservingTransformation implements ASTTransformation {

    private final AtomicBoolean wasCalled = new AtomicBoolean(false)

    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        if (wasCalled.getAndSet(true)) {
            throw new IllegalStateException("${this.class.simpleName()} cannot be called twice.")
        }
    }
}
