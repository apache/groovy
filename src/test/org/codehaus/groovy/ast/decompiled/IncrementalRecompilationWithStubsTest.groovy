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
package org.codehaus.groovy.ast.decompiled

import org.codehaus.groovy.tools.stubgenerator.StubTestCase

final class IncrementalRecompilationWithStubsTest extends StubTestCase {

    private static File createFile(File dir, String name, String text) {
        new File(dir, name).tap {
            createNewFile()
            setText(text)
        }
    }

    @Override
    void testRun() {
        configure()

        File groovy1 = createFile(stubDir, "AsmClass1.groovy", "class AsmClass1 {}")
        File groovy2 = createFile(stubDir, "AsmClass2.groovy", "class AsmClass2 extends AsmClass1 {}")
        File groovy3 = createFile(stubDir, "AsmClass3.groovy", "class AsmClass3 extends AsmClass2 {}")
        File javaOne = createFile(stubDir, "JavaClass.java"  , "class JavaClass {}")

        compile([groovy1, groovy2, groovy3, javaOne])

        File   class1 = new File(targetDir, "AsmClass1.class")
        assert class1.exists()
        assert class1.delete()

        compile([groovy1, groovy3, javaOne])

        assert class1.exists()
    }

    @Override
    void verifyStubs() {
    }
}
