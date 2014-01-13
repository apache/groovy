/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.tools.shell

import groovy.mock.interceptor.MockFor
import jline.console.completer.Completer
import org.codehaus.groovy.tools.shell.commands.ImportCommand
import org.codehaus.groovy.tools.shell.commands.ImportCompleter
import org.codehaus.groovy.tools.shell.util.PackageHelper
import org.codehaus.groovy.tools.shell.util.Preferences

class ImportCompleterUnitTest
extends GroovyTestCase {

    MockFor helperMocker
    MockFor preferencesMocker

    void setUp() {
        super.setUp()
        helperMocker = new MockFor(PackageHelper)
        helperMocker.demand.initializePackages(0..1) {}
        helperMocker.demand.getClass(0..1) {PackageHelper}
        preferencesMocker = new MockFor(Preferences)
        preferencesMocker.demand.get(1) { "true" }
        preferencesMocker.demand.addChangeListener(1) {}
    }

    void testPattern() {
        assertTrue("" ==~ ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN)
        assertTrue("j" ==~ ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN)
        assertTrue("java." ==~ ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN)
        assertTrue("java.util" ==~ ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN)
        assertTrue("java.util." ==~ ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN)
        assertTrue("java.util.T" ==~ ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN)
        assertTrue("org.w3c.T" ==~ ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN)
        assertTrue("java.util.Test" ==~ ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN)
        assertTrue("java.util.Test123" ==~ ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN)
        // inverse
        assertFalse("." ==~ ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN)
        assertFalse("Upper" ==~ ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN )
        assertFalse("java.util.Test123." ==~ ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN)
        assertFalse("java.util.Test123.foo" ==~ ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN)

        assertTrue("" ==~ ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN)
        assertTrue("j" ==~ ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN)
        assertTrue("java." ==~ ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN)
        assertTrue("java.util" ==~ ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN)
        assertTrue("java.util." ==~ ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN)
        assertTrue("java.util.T" ==~ ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN)
        assertTrue("org.w3c.T" ==~ ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN)
        assertTrue("java.util.Test" ==~ ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN)
        assertTrue("java.util.Test123" ==~ ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN)
        assertTrue("java.util.Test123." ==~ ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN)
        assertTrue("java.util.Test123.foo" ==~ ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN)
        //inverse
        assertFalse("." ==~ ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN)
        assertFalse("Upper" ==~ ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN)
        assertFalse("java.util.Test123.foo." ==~ ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN)
        assertFalse("java.util.Test123.Test.foo" ==~ ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN)
    }

    void testCompleteEmpty() {

        helperMocker.demand.getContents(1) { str -> assert(str == '' ); ["java", "groovy"] }
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            ImportCompleter compl = new ImportCompleter(packageHelper2, null, false);
            def candidates = []
            String buffer = ""
            assertEquals(0, compl.complete(buffer, buffer.length(), candidates))
            assertEquals(["groovy.", "java."], candidates.sort())
        }}
    }

    void testCompleteStaticEmpty() {
        helperMocker.demand.getContents(1) { str -> assert(str == '' ); ["java", "groovy"] }
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            ImportCompleter compl = new ImportCompleter(packageHelper2, null, true);
            def candidates = []
            String buffer = ""
            assertEquals(0, compl.complete(buffer, buffer.length(), candidates))
            assertEquals(["groovy.", "java."], candidates.sort())
        }}
    }

    void testCompleteJ() {
        helperMocker.demand.getContents(1) { str -> assert(str == '' ); ["java", "javax"] }
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            ImportCompleter compl = new ImportCompleter(packageHelper2, null, false);
            def candidates = []
            String buffer = "j"
            assertEquals(0, compl.complete(buffer, buffer.length(), candidates))
            assertEquals(["java.", "javax."], candidates.sort())
        }
    }}

    void testCompleteStaticJ() {
        helperMocker.demand.getContents(1) { str -> assert(str == '' ); ["java", "javax"] }
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            ImportCompleter compl = new ImportCompleter(packageHelper2, null, true);
            def candidates = []
            String buffer = "j"
            assertEquals(0, compl.complete(buffer, buffer.length(), candidates))
            assertEquals(["java.", "javax."], candidates.sort())
        }
    }}

    void testCompleteCo() {
        helperMocker.demand.getContents(1) { str -> assert(str == '' ); ["com", "org"] }
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            ImportCompleter compl = new ImportCompleter(packageHelper2, null, false);
            def candidates = []
            String buffer = "co"
            assertEquals(0, compl.complete(buffer, buffer.length(), candidates))
            assertEquals(["com."], candidates.sort())
        }
    }}

    void testCompleteJavaDot() {
        helperMocker.demand.getContents(1) { ["util", "math"] }
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            ImportCompleter compl = new ImportCompleter(packageHelper2, null, false);
            def candidates = []
            String buffer = "java."
            assertEquals(5, compl.complete(buffer, buffer.length(), candidates))
            assertEquals(["* ", "math.", "util."], candidates.sort())
        }
    }}

    void testCompleteStaticJavaDot() {
        helperMocker.demand.getContents(1) { ["util", "math"] }
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            ImportCompleter compl = new ImportCompleter(packageHelper2, null, true);
            def candidates = []
            String buffer = "java."
            assertEquals(5, compl.complete(buffer, buffer.length(), candidates))
            assertEquals(["math.", "util."], candidates.sort())
        }
    }}

    void testCompleteJavaDotU() {
        helperMocker.demand.getContents(1) { ["util", "math"] }
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            ImportCompleter compl = new ImportCompleter(packageHelper2, null, false);
            def candidates = []
            String buffer = "java.u"
            assertEquals(5, compl.complete(buffer, buffer.length(), candidates))
            assertEquals(["util."], candidates.sort())
        }
    }}

    void testCompleteJavaDotUtil() {
        helperMocker.demand.getContents(1) { ["util"] }
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            ImportCompleter compl = new ImportCompleter(packageHelper2, null, false);
            def candidates = []
            String buffer = "java.util"
            assertEquals(5, compl.complete(buffer, buffer.length(), candidates))
            assertEquals(["util."], candidates.sort())
        }
    }}

    void testCompleteJavaDotUtilDot() {
        helperMocker.demand.getContents(1) { ["zip", "jar"] }
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            ImportCompleter compl = new ImportCompleter(packageHelper2, null, false);
            def candidates = []
            String buffer = "java.util."
            assertEquals(10, compl.complete(buffer, buffer.length(), candidates))
            assertEquals(["* ", "jar.", "zip."], candidates.sort())
        }
    }}

    void testCompleteJavaDotUtilDotZip() {
        helperMocker.demand.getContents(1) { ["zip"] }
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            ImportCompleter compl = new ImportCompleter(packageHelper2, null, false);
            def candidates = []
            String buffer = "java.util.zip"
            assertEquals(10, compl.complete(buffer, buffer.length(), candidates))
            assertEquals(["zip."], candidates.sort())
        }
    }}

    void testCompleteJavaDotUtilDotZipDot() {
        helperMocker.demand.getContents(1) { ["Test1", "Test2"] }
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            ImportCompleter compl = new ImportCompleter(packageHelper2, null, false);
            def candidates = []
            String buffer = "java.util.zip."
            assertEquals(14, compl.complete(buffer, buffer.length(), candidates))
            assertEquals(["* ", "Test1 ", "Test2 "], candidates.sort())
        }
    }}

    void testCompleteStaticJavaDotUtilDotZipDot() {
        helperMocker.demand.getContents(1) { ["Test1", "Test2"] }
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            ImportCompleter compl = new ImportCompleter(packageHelper2, null, true);
            def candidates = []
            String buffer = "java.util.zip."
            assertEquals(14, compl.complete(buffer, buffer.length(), candidates))
            assertEquals(["Test1.", "Test2."], candidates.sort())
        }
    }}

    void testCompleteJavaDotUtilDotZipDotT() {
        helperMocker.demand.getContents(1) { ["Test", "NotThis"] }
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            ImportCompleter compl = new ImportCompleter(packageHelper2, null, false);
            def candidates = []
            String buffer = "java.util.zip.T"
            assertEquals(14, compl.complete(buffer, buffer.length(), candidates))
            assertEquals(["Test "], candidates.sort())
        }
    }}

    void testCompleteStaticJavaDotUtilDotZipDotT() {
        helperMocker.demand.getContents(1) { ["Test", "NotThis"] }
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            ImportCompleter compl = new ImportCompleter(packageHelper2, null, true);
            def candidates = []
            String buffer = "java.util.zip.T"
            assertEquals(14, compl.complete(buffer, buffer.length(), candidates))
            assertEquals(["Test "], candidates.sort())
        }
    }}

    void testCompleteStaticJavaDotUtilDotZipDotTestDot() {
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            def mockInterp = [evaluate: {expr -> assert(expr == ['java.util.zip.Test']); Math}]
            ImportCompleter compl = new ImportCompleter(packageHelper2, mockInterp, true);
            def candidates = ['previousitem']
            String buffer = "java.util.zip.Test."
            assertEquals(19, compl.complete(buffer, buffer.length(), candidates))
            // using Math as mock class
            assertTrue('* ' in candidates)
            assertTrue(candidates.toString(), 'abs ' in candidates)
            assertTrue('previousitem' in candidates)
        }
    }}

    void testCompleteStaticJavaDotUtilDotZipDotTestDotMa() {
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            def mockInterp = [evaluate: {expr -> assert(expr == ['java.util.zip.Test']); Math}]
            ImportCompleter compl = new ImportCompleter(packageHelper2, mockInterp, true);
            def candidates = []
            String buffer = "java.util.zip.Test.ma"
            assertEquals(19, compl.complete(buffer, buffer.length(), candidates))
            assertEquals(["max "], candidates.sort())
        }
    }}
}

class ImportCompleterTest
extends CompletorTestSupport {

    void testEmpty() {
        mockPackageHelper = packageHelperMocker.proxyInstance()
        groovyshMocker.demand.getPackageHelper(1) { mockPackageHelper }
        groovyshMocker.demand.getInterp(1) {}
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportCommand iCom = new ImportCommand(groovyshMock)
            Completer completer = iCom.getCompleter()
            def candidates = []
            assertEquals(0, completer.complete("", 0, candidates))
            // order changed by sort
            assertEquals([":i", ":i", "import", "import"], candidates.sort())
        }
    }

    void testUnknownVar() {
        mockPackageHelper = packageHelperMocker.proxyInstance()
        groovyshMocker.demand.getPackageHelper(1) { mockPackageHelper }
        groovyshMocker.demand.getInterp(1) {}
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportCommand iCom = new ImportCommand(groovyshMock)
            Completer completer = iCom.getCompleter()
            def candidates = []
            assertEquals(7, completer.complete("import ", "import ".length(), candidates))
            // order changed by sort, needed to make tests run on different JDks
            assertEquals(["java.", "static ", "test."], candidates.sort())
        }
    }

    void testJ() {
        mockPackageHelper = packageHelperMocker.proxyInstance()
        groovyshMocker.demand.getPackageHelper(1) { mockPackageHelper }
        groovyshMocker.demand.getInterp(1) {}
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportCommand iCom = new ImportCommand(groovyshMock)
            Completer completer = iCom.getCompleter()
            def candidates = []
            // argument completer completes after "import "
            assertEquals(7, completer.complete("import j", "import j".length(), candidates))
            assertEquals(["java."], candidates)
        }
    }

    void testJavaDot() {
        mockPackageHelper = packageHelperMocker.proxyInstance()
        groovyshMocker.demand.getPackageHelper(1) { mockPackageHelper }
        groovyshMocker.demand.getInterp(1) {}
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportCommand iCom = new ImportCommand(groovyshMock)
            Completer completer = iCom.getCompleter()
            def candidates = []
            // argument completer completes after "import "
            String buffer = "import java."
            assertEquals(12, completer.complete(buffer, buffer.length(), candidates))
            // order changed by sort, needed to run tests on different JDKs
            assertEquals(["* ", "java.", "test."], candidates.sort())
        }
    }

    void testJavaLangDot() {
        mockPackageHelper = packageHelperMocker.proxyInstance()
        groovyshMocker.demand.getPackageHelper(1) { mockPackageHelper }
        groovyshMocker.demand.getInterp(1) {}
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportCommand iCom = new ImportCommand(groovyshMock)
            Completer completer = iCom.getCompleter()
            def candidates = []
            // argument completer completes after "import "
            String buffer = "import java.lang."
            assertEquals(17, completer.complete(buffer, buffer.length(), candidates))
            // order changed by sort, needed to make tests run on different JDks
            assertEquals(["* ", "java.", "test."], candidates.sort())
        }
    }

    void testAs() {
        mockPackageHelper = packageHelperMocker.proxyInstance()
        groovyshMocker.demand.getPackageHelper(1) { mockPackageHelper }
        groovyshMocker.demand.getInterp(1) {}
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportCommand iCom = new ImportCommand(groovyshMock)
            Completer completer = iCom.getCompleter()
            def candidates = []
            // mock package
            String buffer = "import java.test "
            assertEquals(17, completer.complete(buffer, buffer.length(), candidates))
            assertEquals(["as "], candidates)
        }
    }
}