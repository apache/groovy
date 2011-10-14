/*
 * Copyright 2008-2011 the original author or authors.
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
import gls.CompilableTestSupport

/**
 * @author Danno Ferrin
 * @author Paul King
 */
class GrapeIvyTest extends CompilableTestSupport {

    public GrapeIvyTest() {
        // insure files are installed locally
        [[groupId:'log4j', artifactId:'log4j', version:'1.1.3'],
            [groupId:'org.apache.poi', artifactId:'poi', version:'3.7'],
            [groupId:'com.jidesoft', artifactId:'jide-oss', version:'[2.2.1,2.3)'],
            [groupId:'org.apache.ivy', artifactId:'ivy', version:'2.0.0', conf:['default', 'optional']],
            [groupId:'net.sf.json-lib', artifactId:'json-lib', version:'2.2.3', classifier:'jdk15']
        ].each {
            Grape.resolve([autoDownload:true, classLoader:new GroovyClassLoader()], it)
        }
    }

    public void testSingleArtifact() {
        GroovyClassLoader loader = new GroovyClassLoader()
        GroovyShell shell = new GroovyShell(loader)
        shouldFail(CompilationFailedException) {
            shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class")
        }

        Grape.grab(groupId:'com.jidesoft', artifactId:'jide-oss', version:'[2.2.1,2.3)', classLoader:loader)

        assert shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class").name == 'com.jidesoft.swing.JideSplitButton';
    }

    public void testModuleWithDependencies() {
        GroovyClassLoader loader = new GroovyClassLoader()
        GroovyShell shell = new GroovyShell(loader)
        shouldFail(CompilationFailedException) {
            shell.evaluate("import org.apache.poi.POIDocument; POIDocument.class")
        }

        Grape.grab(groupId:'org.apache.poi', artifactId:'poi', version:'3.7', classLoader:loader)

        assert shell.evaluate("import org.apache.poi.POIDocument; POIDocument.class").name == 'org.apache.poi.POIDocument'
    }

    public void testMultipleDependencies() {
        GroovyClassLoader loader = new GroovyClassLoader()
        GroovyShell shell = new GroovyShell(loader)
        shouldFail(CompilationFailedException) {
            shell.evaluate("import org.apache.poi.POIDocument; POIDocument.class")
        }
        shouldFail(CompilationFailedException) {
            shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class")
        }

        Grape.grab(classLoader:loader,
            [groupId:'org.apache.poi', artifactId:'poi', version:'3.7'],
            [groupId:'com.jidesoft', artifactId:'jide-oss', version:'[2.2.1,2.3)'])

        assert shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class").name == 'com.jidesoft.swing.JideSplitButton';
        assert shell.evaluate("import org.apache.poi.POIDocument; POIDocument.class").name == 'org.apache.poi.POIDocument'
    }

    public void testListDependencies() {
        GroovyClassLoader loader = new GroovyClassLoader()
        GroovyShell shell = new GroovyShell(loader)
        shouldFail(CompilationFailedException) {
            shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class")
        }
        shouldFail(CompilationFailedException) {
            shell.evaluate("import org.apache.poi.POIDocument; POIDocument.class")
        }

        Grape.grab(classLoader:loader,
            [groupId:'org.apache.poi', artifactId:'poi', version:'3.7'],
            [groupId:'com.jidesoft', artifactId:'jide-oss', version:'[2.2.1,2.3)'])

        def loadedDependencies = Grape.listDependencies(loader)
        assert loadedDependencies == [
            [group:'org.apache.poi', module:'poi', version:'3.7'],
            [group:'com.jidesoft', module:'jide-oss', version:'[2.2.1,2.3)']
        ]
    }

    public void testGrabRefless() {
        GroovyClassLoader loader = new GroovyClassLoader()
        GroovyShell shell = new GroovyShell(loader)
        shouldFail(CompilationFailedException) {
            shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class")
        }
        shell.evaluate("new groovy.grape.Grape().grab(groupId:'com.jidesoft', artifactId:'jide-oss', version:'[2.2.1,2.3)')")
        assert shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class").name == 'com.jidesoft.swing.JideSplitButton';
    }

    public void testGrabScriptClass() {
        GroovyClassLoader loader = new GroovyClassLoader()
        GroovyShell shell = new GroovyShell(loader)
        shouldFail(CompilationFailedException) {
            shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class")
        }
        shell.evaluate("new groovy.grape.Grape().grab(groupId:'com.jidesoft', artifactId:'jide-oss', version:'[2.2.1,2.3)', refObject:this)")
        assert shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class").name == 'com.jidesoft.swing.JideSplitButton';
    }

    public void testGrabScriptLoader() {
        GroovyClassLoader loader = new GroovyClassLoader()
        GroovyShell shell = new GroovyShell(loader)
        shell.setVariable("loader", loader)
        shouldFail(CompilationFailedException) {
            shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class")
        }
        shell.evaluate("new groovy.grape.Grape().grab(groupId:'com.jidesoft', artifactId:'jide-oss', version:'[2.2.1,2.3)', classLoader:loader)")
        assert shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class").name == 'com.jidesoft.swing.JideSplitButton';
    }

    public void testGrabReflessMultiple() {
        GroovyClassLoader loader = new GroovyClassLoader()
        GroovyShell shell = new GroovyShell(loader)
        shouldFail(CompilationFailedException) {
            shell.evaluate("import org.apache.poi.POIDocument; POIDocument.class")
        }
        shouldFail(CompilationFailedException) {
            shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class")
        }
        shell.evaluate("""new groovy.grape.Grape().grab([:],
            [groupId:'org.apache.poi', artifactId:'poi', version:'3.7'],
            [groupId:'com.jidesoft', artifactId:'jide-oss', version:'[2.2.1,2.3)'])""")

        assert shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class").name == 'com.jidesoft.swing.JideSplitButton';
        assert shell.evaluate("import org.apache.poi.POIDocument; POIDocument.class").name == 'org.apache.poi.POIDocument'
    }

    public void testGrabScriptClassMultiple() {
        GroovyClassLoader loader = new GroovyClassLoader()
        GroovyShell shell = new GroovyShell(loader)
        shouldFail(CompilationFailedException) {
            shell.evaluate("import org.apache.poi.POIDocument; POIDocument.class")
        }
        shouldFail(CompilationFailedException) {
            shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class")
        }
        shell.evaluate("""new groovy.grape.Grape().grab(refObject: this,
            [groupId:'org.apache.poi', artifactId:'poi', version:'3.7'],
            [groupId:'com.jidesoft', artifactId:'jide-oss', version:'[2.2.1,2.3)'])""")

        assert shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class").name == 'com.jidesoft.swing.JideSplitButton';
        assert shell.evaluate("import org.apache.poi.POIDocument; POIDocument.class").name == 'org.apache.poi.POIDocument'
    }

    public void testGrabScriptLoaderMultiple() {
        GroovyClassLoader loader = new GroovyClassLoader()
        GroovyShell shell = new GroovyShell(loader)
        shell.setVariable("loader", loader)
        shouldFail(CompilationFailedException) {
            shell.evaluate("import org.apache.poi.POIDocument; POIDocument.class")
        }
        shouldFail(CompilationFailedException) {
            shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class")
        }
        shell.evaluate("""new groovy.grape.Grape().grab(classLoader:loader,
            [groupId:'org.apache.poi', artifactId:'poi', version:'3.7'],
            [groupId:'com.jidesoft', artifactId:'jide-oss', version:'[2.2.1,2.3)'])""")

        assert shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class").name == 'com.jidesoft.swing.JideSplitButton';
        assert shell.evaluate("import org.apache.poi.POIDocument; POIDocument.class").name == 'org.apache.poi.POIDocument'
    }

    public void testSerialGrabs() {
        GroovyClassLoader loader = new GroovyClassLoader()

        Grape.grab(groupId:'log4j', artifactId:'log4j', version:'1.1.3', classLoader:loader)

        Grape.grab(groupId:'org.apache.poi', artifactId:'poi', version:'3.7', classLoader:loader)
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

        def coreJars = ["ivy-2.0.0.jar"] as Set
        def optionalJars = [
                "ant-1.6.2.jar",
                "ant-trax-1.6.2.jar",
                "ant-nodeps-1.6.2.jar",
                "commons-httpclient-3.0.jar",
                "junit-3.8.1.jar",
                "commons-codec-1.2.jar",
                "oro-2.0.8.jar",
                "commons-vfs-1.0.jar",
                "commons-logging-1.0.4.jar",
                "jsch-0.1.31.jar",
            ]  as Set
        def testJars = [
                "junit-3.8.2.jar",
                "commons-lang-2.3.jar",
            ]  as Set

        Grape.grab(groupId:'org.apache.ivy', artifactId:'ivy', version:'2.0.0', classLoader:loader, preserveFiles:true)
        def jars = loader.getURLs().collect {URL it -> it.getPath().split('/')[-1]} as Set
        assert coreJars - jars == Collections.EMPTY_SET, "assert that all core jars are present"
        assert testJars - jars == testJars, "assert that no test jars are present"
        assert optionalJars - jars == optionalJars, "assert that no optional jars are present"
        assert jars == coreJars, "assert that no extraneous jars are present"

        loader = new GroovyClassLoader()
        Grape.grab(groupId:'org.apache.ivy', artifactId:'ivy', version:'2.0.0', conf:'optional', classLoader:loader)
        jars = loader.getURLs().collect {URL it -> it.getPath().split('/')[-1]} as Set
        assert coreJars - jars == coreJars, "assert that no core jars are present"
        assert testJars - jars == testJars, "assert that no test jars are present"
        assert optionalJars - jars == Collections.EMPTY_SET, "assert that all optional jars are present"
        assert jars == optionalJars, "assert that no extraneous jars are present"

        loader = new GroovyClassLoader()
        Grape.grab(groupId:'org.apache.ivy', artifactId:'ivy', version:'2.0.0', conf:['default', 'optional'], classLoader:loader)
        jars = loader.getURLs().collect {URL it -> it.getPath().split('/')[-1]} as Set
        assert coreJars - jars == Collections.EMPTY_SET, "assert that all core jars are present"
        assert testJars - jars == testJars, "assert that no test jars are present"
        assert optionalJars - jars == Collections.EMPTY_SET, "assert that all optional jars are present"
        assert jars == coreJars + optionalJars, "assert that no extraneous jars are present"
    }

    public void testClassifier() {
        GroovyClassLoader loader = new GroovyClassLoader()
        GroovyShell shell = new GroovyShell(loader)
        shouldFail(CompilationFailedException) {
            shell.evaluate("import net.sf.json.JSON; JSON")
        }

        Grape.grab(groupId:'net.sf.json-lib', artifactId:'json-lib', version:'2.2.3', classifier:'jdk15', classLoader:loader)

        assert shell.evaluate("import net.sf.json.JSON; JSON").name == 'net.sf.json.JSON'
    }

    public void testClassifierWithConf() {
        def coreJars = [
                "json-lib-2.2.3-jdk15.jar",
                "commons-lang-2.4.jar",
                "commons-collections-3.2.jar",
                "ezmorph-1.0.6.jar",
                "commons-logging-1.1.1.jar",
                "commons-beanutils-1.7.0.jar"
            ] as Set
        def optionalJars = [
                "xercesImpl-2.6.2.jar",
                "xmlParserAPIs-2.6.2.jar",
                "groovy-all-1.5.7.jar",
                "oro-2.0.8.jar",
                "jruby-1.1.jar",
                "junit-3.8.2.jar",
                "ant-launcher-1.7.0.jar",
                "xalan-2.7.0.jar",
                "json-lib-2.2.3-jdk15.jar",
                "ant-1.7.0.jar",
                "xom-1.1.jar",
                "jaxen-1.1-beta-8.jar",
                "jdom-1.0.jar",
                "jline-0.9.94.jar",
                "log4j-1.2.14.jar",
                "dom4j-1.6.1.jar"
            ] as Set

        GroovyClassLoader loader = new GroovyClassLoader()
        Grape.grab(groupId:'net.sf.json-lib', artifactId:'json-lib', version:'2.2.3', classifier:'jdk15', classLoader:loader, preserveFiles:true)
        def jars = loader.getURLs().collect {URL it -> it.getPath().split('/')[-1]} as Set
        assert jars == coreJars

        loader = new GroovyClassLoader()
        Grape.grab(groupId:'net.sf.json-lib', artifactId:'json-lib', version:'2.2.3', classifier:'jdk15', conf:'optional', classLoader:loader)
        jars = loader.getURLs().collect {URL it -> it.getPath().split('/')[-1]} as Set
        assert jars == optionalJars

        loader = new GroovyClassLoader()
        Grape.grab(groupId:'net.sf.json-lib', artifactId:'json-lib', version:'2.2.3', classifier:'jdk15', conf:['default', 'optional'], classLoader:loader)
        jars = loader.getURLs().collect {URL it -> it.getPath().split('/')[-1]} as Set
        assert jars == coreJars + optionalJars
    }

    void testTransitiveShorthandControl() {
        // BeanUtils is a transitive dependency for Digester
        assertScript '''
            @Grab('commons-digester:commons-digester:2.1')
            import org.apache.commons.digester.Digester

            assert Digester.name.size() == 36
            assert org.apache.commons.beanutils.BeanUtils.name.size() == 38
        '''
    }

    void testTransitiveShorthandExpectFailure() {
        assertScript '''
            @Grab('commons-digester:commons-digester:2.1;transitive=false')
            import org.apache.commons.digester.Digester

            assert Digester.name.size() == 36
            try {
                assert org.apache.commons.beanutils.BeanUtils.name.size() == 38
                assert false
            } catch(MissingPropertyException mpe) { }
        '''
    }
}
