/*
 * Copyright 2008 the original author or authors.
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
package groovy.grape

import org.codehaus.groovy.control.CompilationFailedException

/**
 * @author Danno Ferrin
 */
class GrapeIvyTest extends GroovyTestCase {

    public GrapeIvyTest() {
        Grape.initGrape()
    }

    public void testSingleArtifact() {
        GroovyClassLoader loader = new GroovyClassLoader()
        GroovyShell shell = new GroovyShell(loader)
        shouldFail(CompilationFailedException) {
            shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class")
        }

        Grape.grab(groupId:'com.jidesoft', artifactId:'jide-oss', version:'[2.2.1,)', classLoader:loader)

        assert shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class").name == 'com.jidesoft.swing.JideSplitButton';
    }

    public void testModuleWithDependencies() {
        GroovyClassLoader loader = new GroovyClassLoader()
        GroovyShell shell = new GroovyShell(loader)
        shouldFail(CompilationFailedException) {
            shell.evaluate("import org.apache.poi.hssf.model.Sheet; Sheet.class")
        }

        Grape.grab(groupId:'org.apache.poi', artifactId:'poi', version:'3.0.1-FINAL', classLoader:loader)

        assert shell.evaluate("import org.apache.poi.hssf.model.Sheet; Sheet.class").name == 'org.apache.poi.hssf.model.Sheet'
    }

    public void testMultipleDependencies() {
        GroovyClassLoader loader = new GroovyClassLoader()
        GroovyShell shell = new GroovyShell(loader)
        shouldFail(CompilationFailedException) {
            shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class")
            shell.evaluate("import org.apache.poi.hssf.model.Sheet; Sheet.class")
        }

        Grape.grab(classLoader:loader,
            [groupId:'org.apache.poi', artifactId:'poi', version:'3.0.1-FINAL'],
            [groupId:'com.jidesoft', artifactId:'jide-oss', version:'[2.2.1,)'])

        assert shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class").name == 'com.jidesoft.swing.JideSplitButton';
        assert shell.evaluate("import org.apache.poi.hssf.model.Sheet; Sheet.class").name == 'org.apache.poi.hssf.model.Sheet'
    }

    public void testGrabRefless() {
        GroovyClassLoader loader = new GroovyClassLoader()
        GroovyShell shell = new GroovyShell(loader)
        shouldFail(CompilationFailedException) {
            shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class")
        }
        shell.evaluate("groovy.grape.GrapeIvy.grab(groupId:'com.jidesoft', artifactId:'jide-oss', version:'[2.2.1,)')")
        assert shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class").name == 'com.jidesoft.swing.JideSplitButton';
    }

    public void testGrabScriptClass() {
        GroovyClassLoader loader = new GroovyClassLoader()
        GroovyShell shell = new GroovyShell(loader)
        shouldFail(CompilationFailedException) {
            shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class")
        }
        shell.evaluate("groovy.grape.GrapeIvy.grab(groupId:'com.jidesoft', artifactId:'jide-oss', version:'[2.2.1,)', refObject:this)")
        assert shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class").name == 'com.jidesoft.swing.JideSplitButton';
    }

    public void testGrabScriptLoader() {
        GroovyClassLoader loader = new GroovyClassLoader()
        GroovyShell shell = new GroovyShell(loader)
        shell.setVariable("loader", loader)
        shouldFail(CompilationFailedException) {
            shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class")
        }
        shell.evaluate("groovy.grape.GrapeIvy.grab(groupId:'com.jidesoft', artifactId:'jide-oss', version:'[2.2.1,)', classLoader:loader)")
        assert shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class").name == 'com.jidesoft.swing.JideSplitButton';
    }

    public void testGrabReflessMultiple() {
        GroovyClassLoader loader = new GroovyClassLoader()
        GroovyShell shell = new GroovyShell(loader)
        shouldFail(CompilationFailedException) {
            shell.evaluate("import org.apache.poi.hssf.model.Sheet; Sheet.class")
            shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class")
        }
        shell.evaluate("""groovy.grape.GrapeIvy.grab([:],
            [groupId:'org.apache.poi', artifactId:'poi', version:'3.0.1-FINAL'],
            [groupId:'com.jidesoft', artifactId:'jide-oss', version:'[2.2.1,)'])""")

        assert shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class").name == 'com.jidesoft.swing.JideSplitButton';
        assert shell.evaluate("import org.apache.poi.hssf.model.Sheet; Sheet.class").name == 'org.apache.poi.hssf.model.Sheet'
    }

    public void testGrabScriptClassMultiple() {
        GroovyClassLoader loader = new GroovyClassLoader()
        GroovyShell shell = new GroovyShell(loader)
        shouldFail(CompilationFailedException) {
            shell.evaluate("import org.apache.poi.hssf.model.Sheet; Sheet.class")
            shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class")
        }
        shell.evaluate("""groovy.grape.GrapeIvy.grab(refObject: this,
            [groupId:'org.apache.poi', artifactId:'poi', version:'3.0.1-FINAL'],
            [groupId:'com.jidesoft', artifactId:'jide-oss', version:'[2.2.1,)'])""")

        assert shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class").name == 'com.jidesoft.swing.JideSplitButton';
        assert shell.evaluate("import org.apache.poi.hssf.model.Sheet; Sheet.class").name == 'org.apache.poi.hssf.model.Sheet'
    }

    public void testGrabScriptLoaderMultiple() {
        GroovyClassLoader loader = new GroovyClassLoader()
        GroovyShell shell = new GroovyShell(loader)
        shell.setVariable("loader", loader)
        shouldFail(CompilationFailedException) {
            shell.evaluate("import org.apache.poi.hssf.model.Sheet; Sheet.class")
            shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class")
        }
        shell.evaluate("""groovy.grape.GrapeIvy.grab(classLoader:loader, 
            [groupId:'org.apache.poi', artifactId:'poi', version:'3.0.1-FINAL'],
            [groupId:'com.jidesoft', artifactId:'jide-oss', version:'[2.2.1,)'])""")

        assert shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class").name == 'com.jidesoft.swing.JideSplitButton';
        assert shell.evaluate("import org.apache.poi.hssf.model.Sheet; Sheet.class").name == 'org.apache.poi.hssf.model.Sheet'
    }

    public void testSerialGrabs() {
        GroovyClassLoader loader = new GroovyClassLoader()

        Grape.grab(groupId:'log4j', artifactId:'log4j', version:'1.1.3', classLoader:loader)

        Grape.grab(groupId:'org.apache.poi', artifactId:'poi', version:'3.0.1-FINAL', classLoader:loader)
        def jars = loader.getURLs().collect {URL it -> it.getPath().split('/')[-1]}
        // because poi asks for log4j 1.2.13, but we already have 1.1.3 so it won't be loaded
        assert jars.contains ("log4j-1.1.3.jar")
        assert !jars.contains ("log4j-1.2.13.jar")

        Grape.grab(groupId:'log4j', artifactId:'log4j', version:'1.2.13', classLoader:loader)
        jars = loader.getURLs().collect {URL it -> it.getPath().split('/')[-1]}
        // because log4j 1.1.3 was loaded first, 1.2.13 should not get loaded
        // even though it was explicitly asked for 
        assert jars.contains ("log4j-1.1.3.jar")
        assert !jars.contains ("log4j-1.2.13.jar")
    }

    public void testConf() {
        GroovyClassLoader loader = new GroovyClassLoader()

        def coreJars = ["ivy-2.0.0-beta1.jar"] as Set
        def optionalJars = [
                "ant-1.6.jar",
                "commons-httpclient-3.0.jar",
                "junit-3.8.1.jar",
                "commons-codec-1.2.jar",
                "commons-cli-1.0.jar",
                "commons-lang-1.0.jar",
                "oro-2.0.8.jar",
                "commons-vfs-1.0.jar",
                "commons-logging-1.0.4.jar",
                "jsch-0.1.25.jar",
            ]  as Set
        def testJars = [
                "junit-3.8.2.jar",
                "commons-lang-2.3.jar",
            ]  as Set

        Grape.grab(groupId:'org.apache.ivy', artifactId:'ivy', version:'2.0.0-beta1', classLoader:loader, preserveFiles:true)
        def jars = loader.getURLs().collect {URL it -> it.getPath().split('/')[-1]} as Set
        assert coreJars - jars == Collections.EMPTY_SET, "assert that all core jars are present"
        assert testJars - jars == testJars, "assert that no test jars are present"
        assert optionalJars - jars == optionalJars, "assert that no optional jars are present"
        assert jars == coreJars, "assert that no extraneous jars are present"

        loader = new GroovyClassLoader()
        Grape.grab(groupId:'org.apache.ivy', artifactId:'ivy', version:'2.0.0-beta1', conf:'optional', classLoader:loader)
        jars = loader.getURLs().collect {URL it -> it.getPath().split('/')[-1]} as Set
        assert coreJars - jars == coreJars, "assert that no core jars are present"
        assert testJars - jars == testJars, "assert that no test jars are present"
        assert optionalJars - jars == Collections.EMPTY_SET, "assert that all optional jars are present"
        assert jars == optionalJars, "assert that no extraneous jars are present"

        loader = new GroovyClassLoader()
        Grape.grab(groupId:'org.apache.ivy', artifactId:'ivy', version:'2.0.0-beta1', conf:['default', 'optional'], classLoader:loader)
        jars = loader.getURLs().collect {URL it -> it.getPath().split('/')[-1]} as Set
        assert coreJars - jars == Collections.EMPTY_SET, "assert that all core jars are present"
        assert testJars - jars == testJars, "assert that no test jars are present"
        assert optionalJars - jars == Collections.EMPTY_SET, "assert that all optional jars are present"
        assert jars == coreJars + optionalJars, "assert that no extraneous jars are present"
    }

}
