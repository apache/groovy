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
        assert "" ==~ ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN
        assert "j" ==~ ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN
        assert "java." ==~ ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN
        assert "java.util" ==~ ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN
        assert "java.util." ==~ ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN
        assert "java.util.T" ==~ ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN
        assert "org.w3c.T" ==~ ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN
        assert "java.util.Test" ==~ ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN
        assert "java.util.Test123" ==~ ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN
        // inverse
        assertFalse("." ==~ ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN)
        assertFalse("Upper" ==~ ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN)
        assertFalse("java.util.Test123." ==~ ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN)
        assertFalse("java.util.Test123.foo" ==~ ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN)

        assert "" ==~ ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN
        assert "j" ==~ ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN
        assert "java." ==~ ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN
        assert "java.util" ==~ ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN
        assert "java.util." ==~ ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN
        assert "java.util.T" ==~ ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN
        assert "org.w3c.T" ==~ ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN
        assert "java.util.Test" ==~ ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN
        assert "java.util.Test123" ==~ ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN
        assert "java.util.Test123." ==~ ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN
        assert "java.util.Test123.foo" ==~ ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN
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
            assert 0 == compl.complete(buffer, buffer.length(), candidates)
            assert ["groovy.", "java."] == candidates.sort()
        }}
    }

    void testCompleteStaticEmpty() {
        helperMocker.demand.getContents(1) { str -> assert(str == '' ); ["java", "groovy"] }
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            ImportCompleter compl = new ImportCompleter(packageHelper2, null, true);
            def candidates = []
            String buffer = ""
            assert 0 == compl.complete(buffer, buffer.length(), candidates)
            assert ["groovy.", "java."] == candidates.sort()
        }}
    }

    void testCompleteJ() {
        helperMocker.demand.getContents(1) { str -> assert(str == '' ); ["java", "javax"] }
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            ImportCompleter compl = new ImportCompleter(packageHelper2, null, false);
            def candidates = []
            String buffer = "j"
            assert 0 == compl.complete(buffer, buffer.length(), candidates)
            assert ["java.", "javax."] == candidates.sort()
        }
    }}

    void testCompleteStaticJ() {
        helperMocker.demand.getContents(1) { str -> assert(str == '' ); ["java", "javax"] }
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            ImportCompleter compl = new ImportCompleter(packageHelper2, null, true);
            def candidates = []
            String buffer = "j"
            assert 0 == compl.complete(buffer, buffer.length(), candidates)
            assert ["java.", "javax."] == candidates.sort()
        }
    }}

    void testCompleteCo() {
        helperMocker.demand.getContents(1) { str -> assert(str == '' ); ["com", "org"] }
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            ImportCompleter compl = new ImportCompleter(packageHelper2, null, false);
            def candidates = []
            String buffer = "co"
            assert 0 == compl.complete(buffer, buffer.length(), candidates)
            assert ["com."] == candidates.sort()
        }
    }}

    void testCompleteJavaDot() {
        helperMocker.demand.getContents(1) { ["util", "math"] }
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            ImportCompleter compl = new ImportCompleter(packageHelper2, null, false);
            def candidates = []
            String buffer = "java."
            assert 5 == compl.complete(buffer, buffer.length(), candidates)
            assert ["* ", "math.", "util."] == candidates.sort()
        }
    }}

    void testCompleteStaticJavaDot() {
        helperMocker.demand.getContents(1) { ["util", "math"] }
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            ImportCompleter compl = new ImportCompleter(packageHelper2, null, true);
            def candidates = []
            String buffer = "java."
            assert 5 == compl.complete(buffer, buffer.length(), candidates)
            assert ["math.", "util."] == candidates.sort()
        }
    }}

    void testCompleteJavaDotU() {
        helperMocker.demand.getContents(1) { ["util", "math"] }
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            ImportCompleter compl = new ImportCompleter(packageHelper2, null, false);
            def candidates = []
            String buffer = "java.u"
            assert 5 == compl.complete(buffer, buffer.length(), candidates)
            assert ["util."] == candidates.sort()
        }
    }}

    void testCompleteJavaDotUtil() {
        helperMocker.demand.getContents(1) { ["util"] }
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            ImportCompleter compl = new ImportCompleter(packageHelper2, null, false);
            def candidates = []
            String buffer = "java.util"
            assert 5 == compl.complete(buffer, buffer.length(), candidates)
            assert ["util."] == candidates.sort()
        }
    }}

    void testCompleteJavaDotUtilDot() {
        helperMocker.demand.getContents(1) { ["zip", "jar"] }
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            ImportCompleter compl = new ImportCompleter(packageHelper2, null, false);
            def candidates = []
            String buffer = "java.util."
            assert 10 == compl.complete(buffer, buffer.length(), candidates)
            assert ["* ", "jar.", "zip."] == candidates.sort()
        }
    }}

    void testCompleteJavaDotUtilDotZip() {
        helperMocker.demand.getContents(1) { ["zip"] }
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            ImportCompleter compl = new ImportCompleter(packageHelper2, null, false);
            def candidates = []
            String buffer = "java.util.zip"
            assert 10 == compl.complete(buffer, buffer.length(), candidates)
            assert ["zip."] == candidates.sort()
        }
    }}

    void testCompleteJavaDotUtilDotZipDot() {
        helperMocker.demand.getContents(1) { ["Test1", "Test2"] }
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            ImportCompleter compl = new ImportCompleter(packageHelper2, null, false);
            def candidates = []
            String buffer = "java.util.zip."
            assert 14 == compl.complete(buffer, buffer.length(), candidates)
            assert ["* ", "Test1 ", "Test2 "] == candidates.sort()
        }
    }}

    void testCompleteStaticJavaDotUtilDotZipDot() {
        helperMocker.demand.getContents(1) { ["Test1", "Test2"] }
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            ImportCompleter compl = new ImportCompleter(packageHelper2, null, true);
            def candidates = []
            String buffer = "java.util.zip."
            assert 14 == compl.complete(buffer, buffer.length(), candidates)
            assert ["Test1.", "Test2."] == candidates.sort()
        }
    }}

    void testCompleteJavaDotUtilDotZipDotT() {
        helperMocker.demand.getContents(1) { ["Test", "NotThis"] }
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            ImportCompleter compl = new ImportCompleter(packageHelper2, null, false);
            def candidates = []
            String buffer = "java.util.zip.T"
            assert 14 == compl.complete(buffer, buffer.length(), candidates)
            assert ["Test "] == candidates.sort()
        }
    }}

    void testCompleteStaticJavaDotUtilDotZipDotT() {
        helperMocker.demand.getContents(1) { ["Test", "NotThis"] }
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            ImportCompleter compl = new ImportCompleter(packageHelper2, null, true);
            def candidates = []
            String buffer = "java.util.zip.T"
            assert 14 == compl.complete(buffer, buffer.length(), candidates)
            assert ["Test "] == candidates.sort()
        }
    }}

    void testCompleteStaticJavaDotUtilDotZipDotTestDot() {
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            def evaluator = new Evaluator() {
                @Override
                def evaluate(Collection<String> buffer) {
                    assert(buffer == ['java.util.zip.Test']);
                    return Math
                }
            }
            ImportCompleter compl = new ImportCompleter(packageHelper2, evaluator, true);
            def candidates = ['previousitem']
            String buffer = "java.util.zip.Test."
            assert 19 == compl.complete(buffer, buffer.length(), candidates)
            // using Math as mock class
            assert '* ' in candidates
            assert candidates.toString(), 'abs ' in candidates
            assert 'previousitem' in candidates
        }
    }}

    void testCompleteStaticJavaDotUtilDotZipDotTestDotMa() {
        helperMocker.use { preferencesMocker.use {
            def packageHelper2 = new PackageHelper()
            def evaluator = new Evaluator() {
                @Override
                def evaluate(Collection<String> buffer) {
                    assert(buffer == ['java.util.zip.Test']);
                    return Math
                }
            }
            ImportCompleter compl = new ImportCompleter(packageHelper2, evaluator, true);
            def candidates = []
            String buffer = "java.util.zip.Test.ma"
            assert 19 == compl.complete(buffer, buffer.length(), candidates)
            assert ["max "] == candidates.sort()
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
            assert 0 == completer.complete("", 0, candidates)
            // order changed by sort
            assert [":i", ":i", "import", "import"] == candidates.sort()
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
            assert 7 == completer.complete("import ", "import ".length(), candidates)
            // order changed by sort, needed to make tests run on different JDks
            assert ["java.", "static ", "test."] == candidates.sort()
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
            assert 7 == completer.complete("import j", "import j".length(), candidates)
            assert ["java."] == candidates
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
            assert 12 == completer.complete(buffer, buffer.length(), candidates)
            // order changed by sort, needed to run tests on different JDKs
            assert ["* ", "java.", "test."] == candidates.sort()
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
            assert 17 == completer.complete(buffer, buffer.length(), candidates)
            // order changed by sort, needed to make tests run on different JDks
            assert ["* ", "java.", "test."] == candidates.sort()
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
            assert 17 == completer.complete(buffer, buffer.length(), candidates)
            assert ["as "] == candidates
        }
    }
}
