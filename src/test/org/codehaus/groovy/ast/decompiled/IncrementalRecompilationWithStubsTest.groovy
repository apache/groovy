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

class IncrementalRecompilationWithStubsTest extends StubTestCase {
    @Override
    void testRun() {
        configure()
        File srcDir = createTempDirectory()
        srcDir.deleteOnExit()

        File src1 = createFile(srcDir, "AsmClass1.groovy", "class AsmClass1 {}")
        File src2 = createFile(srcDir, "AsmClass2.groovy", "class AsmClass2 extends AsmClass1 {}")
        File src3 = createFile(srcDir, "AsmClass3.groovy", "class AsmClass3 extends AsmClass2 {}")
        File javaSrc = createFile(srcDir, "JavaClass.java", "class JavaClass {}")

        compile([src1, src2, src3, javaSrc])

        def cls1 = new File(targetDir, "AsmClass1.class")
        assert cls1.exists()
        assert cls1.delete()

        compile([src1, src3, javaSrc])
        assert cls1.exists()
    }

    private static File createFile(File srcDir, String name, String text) {
        File srcFile = new File(srcDir, name)
        assert srcFile.createNewFile()
        srcFile.text = text
        return srcFile
    }

    @Override
    void verifyStubs() {}
}
