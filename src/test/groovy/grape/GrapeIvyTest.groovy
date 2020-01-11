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
package groovy.grape

import org.codehaus.groovy.control.CompilationFailedException
import org.junit.BeforeClass
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail
import static org.junit.Assume.assumeTrue

final class GrapeIvyTest {

    private static Set<String> jarNames(GroovyClassLoader loader) {
        loader.URLs.collect { url -> url.path.split('/')[-1] } as Set
    }

    @BeforeClass
    static void setUpClass() {
        // make sure files are installed locally
        [
            [groupId:'log4j', artifactId:'log4j', version:'1.1.3'],
            [groupId:'org.apache.poi', artifactId:'poi', version:'3.7'],
            [groupId:'com.jidesoft', artifactId:'jide-oss', version:'2.2.12'],
            [groupId:'commons-lang', artifactId:'commons-lang', version:'2.6'],
            [groupId:'org.neo4j', artifactId:'neo4j-kernel', version:'2.0.0-RC1'],
            [groupId:'commons-digester', artifactId:'commons-digester', version:'2.1'],
            [groupId:'net.sf.json-lib', artifactId:'json-lib', version:'2.2.3', classifier:'jdk15'],
            [groupId:'org.apache.ivy', artifactId:'ivy', version:'2.0.0', conf:['default', 'optional']]
        ].each { spec ->
            Grape.resolve([autoDownload:true, classLoader:new GroovyClassLoader()], spec)
        }
    }

    @Test
    void testSingleArtifact() {
        def shell = new GroovyShell(new GroovyClassLoader())
        shouldFail(CompilationFailedException) {
            shell.evaluate('import com.jidesoft.swing.JideSplitButton; JideSplitButton.class')
        }
        Grape.grab(groupId:'com.jidesoft', artifactId:'jide-oss', version:'[2.2.1,2.3)', classLoader:shell.classLoader)
        assert shell.evaluate('import com.jidesoft.swing.JideSplitButton; JideSplitButton.class').name == 'com.jidesoft.swing.JideSplitButton';
    }

    @Test
    void testModuleWithDependencies() {
        def shell = new GroovyShell(new GroovyClassLoader())
        shouldFail(CompilationFailedException) {
            shell.evaluate('import org.apache.poi.POIDocument; POIDocument.class')
        }
        Grape.grab(groupId:'org.apache.poi', artifactId:'poi', version:'3.7', classLoader:shell.classLoader)
        assert shell.evaluate('import org.apache.poi.POIDocument; POIDocument.class').name == 'org.apache.poi.POIDocument'
    }

    @Test
    void testMultipleDependencies() {
        def shell = new GroovyShell(new GroovyClassLoader())
        shouldFail(CompilationFailedException) {
            shell.evaluate('import org.apache.poi.POIDocument; POIDocument.class')
        }
        shouldFail(CompilationFailedException) {
            shell.evaluate('import com.jidesoft.swing.JideSplitButton; JideSplitButton.class')
        }

        Grape.grab(classLoader:shell.classLoader,
            [groupId:'org.apache.poi', artifactId:'poi', version:'3.7'],
            [groupId:'com.jidesoft', artifactId:'jide-oss', version:'[2.2.1,2.3)'])

        assert shell.evaluate('import org.apache.poi.POIDocument; POIDocument.class').name == 'org.apache.poi.POIDocument'
        assert shell.evaluate('import com.jidesoft.swing.JideSplitButton; JideSplitButton.class').name == 'com.jidesoft.swing.JideSplitButton';
    }

    @Test
    void testListDependencies() {
        def shell = new GroovyShell(new GroovyClassLoader())
        shouldFail(CompilationFailedException) {
            shell.evaluate('import com.jidesoft.swing.JideSplitButton; JideSplitButton.class')
        }
        shouldFail(CompilationFailedException) {
            shell.evaluate('import org.apache.poi.POIDocument; POIDocument.class')
        }

        Grape.grab(classLoader:shell.classLoader,
            [groupId:'org.apache.poi', artifactId:'poi', version:'3.7'],
            [groupId:'com.jidesoft', artifactId:'jide-oss', version:'[2.2.1,2.3)'])

        def loadedDependencies = Grape.listDependencies(shell.classLoader)
        assert loadedDependencies == [
            [group:'org.apache.poi', module:'poi', version:'3.7'],
            [group:'com.jidesoft', module:'jide-oss', version:'[2.2.1,2.3)']
        ]
    }

    @Test
    void testGrabRefless() {
        def shell = new GroovyShell(new GroovyClassLoader())
        shouldFail(CompilationFailedException) {
            shell.evaluate('import com.jidesoft.swing.JideSplitButton; JideSplitButton.class')
        }
        shell.evaluate("new groovy.grape.Grape().grab(groupId:'com.jidesoft', artifactId:'jide-oss', version:'[2.2.1,2.3)')")
        assert shell.evaluate('import com.jidesoft.swing.JideSplitButton; JideSplitButton.class').name == 'com.jidesoft.swing.JideSplitButton';
    }

    @Test
    void testGrabScriptClass() {
        def shell = new GroovyShell(new GroovyClassLoader())
        shouldFail(CompilationFailedException) {
            shell.evaluate('import com.jidesoft.swing.JideSplitButton; JideSplitButton.class')
        }
        shell.evaluate("new groovy.grape.Grape().grab(groupId:'com.jidesoft', artifactId:'jide-oss', version:'[2.2.1,2.3)', refObject:this)")
        assert shell.evaluate('import com.jidesoft.swing.JideSplitButton; JideSplitButton.class').name == 'com.jidesoft.swing.JideSplitButton';
    }

    @Test
    void testGrabScriptLoader() {
        def shell = new GroovyShell(new GroovyClassLoader())
        shell.setVariable('loader', shell.classLoader)
        shouldFail(CompilationFailedException) {
            shell.evaluate('import com.jidesoft.swing.JideSplitButton; JideSplitButton.class')
        }
        shell.evaluate("new groovy.grape.Grape().grab(groupId:'com.jidesoft', artifactId:'jide-oss', version:'[2.2.1,2.3)', classLoader:loader)")
        assert shell.evaluate('import com.jidesoft.swing.JideSplitButton; JideSplitButton.class').name == 'com.jidesoft.swing.JideSplitButton';
    }

    @Test
    void testGrabReflessMultiple() {
        def shell = new GroovyShell(new GroovyClassLoader())
        shouldFail(CompilationFailedException) {
            shell.evaluate('import org.apache.poi.POIDocument; POIDocument.class')
        }
        shouldFail(CompilationFailedException) {
            shell.evaluate('import com.jidesoft.swing.JideSplitButton; JideSplitButton.class')
        }
        shell.evaluate('''new groovy.grape.Grape().grab([:],
            [groupId:'org.apache.poi', artifactId:'poi', version:'3.7'],
            [groupId:'com.jidesoft', artifactId:'jide-oss', version:'[2.2.1,2.3)'])''')

        assert shell.evaluate('import org.apache.poi.POIDocument; POIDocument.class').name == 'org.apache.poi.POIDocument'
        assert shell.evaluate('import com.jidesoft.swing.JideSplitButton; JideSplitButton.class').name == 'com.jidesoft.swing.JideSplitButton';
    }

    @Test
    void testGrabScriptClassMultiple() {
        def shell = new GroovyShell(new GroovyClassLoader())
        shouldFail(CompilationFailedException) {
            shell.evaluate('import org.apache.poi.POIDocument; POIDocument.class')
        }
        shouldFail(CompilationFailedException) {
            shell.evaluate('import com.jidesoft.swing.JideSplitButton; JideSplitButton.class')
        }
        shell.evaluate('''new groovy.grape.Grape().grab(refObject: this,
            [groupId:'org.apache.poi', artifactId:'poi', version:'3.7'],
            [groupId:'com.jidesoft', artifactId:'jide-oss', version:'[2.2.1,2.3)'])''')

        assert shell.evaluate("import org.apache.poi.POIDocument; POIDocument.class").name == 'org.apache.poi.POIDocument'
        assert shell.evaluate("import com.jidesoft.swing.JideSplitButton; JideSplitButton.class").name == 'com.jidesoft.swing.JideSplitButton';
    }

    @Test
    void testGrabScriptLoaderMultiple() {
        def shell = new GroovyShell(new GroovyClassLoader())
        shell.setVariable('loader', shell.classLoader)
        shouldFail(CompilationFailedException) {
            shell.evaluate('import org.apache.poi.POIDocument; POIDocument.class')
        }
        shouldFail(CompilationFailedException) {
            shell.evaluate('import com.jidesoft.swing.JideSplitButton; JideSplitButton.class')
        }
        shell.evaluate('''new groovy.grape.Grape().grab(classLoader:loader,
            [groupId:'org.apache.poi', artifactId:'poi', version:'3.7'],
            [groupId:'com.jidesoft', artifactId:'jide-oss', version:'[2.2.1,2.3)'])''')

        assert shell.evaluate('import org.apache.poi.POIDocument; POIDocument.class').name == 'org.apache.poi.POIDocument'
        assert shell.evaluate('import com.jidesoft.swing.JideSplitButton; JideSplitButton.class').name == 'com.jidesoft.swing.JideSplitButton';
    }

    @Test
    void testSerialGrabs() {
        GroovyClassLoader loader = new GroovyClassLoader()
        Grape.grab(groupId:'log4j', artifactId:'log4j', version:'1.1.3', classLoader:loader)
        Grape.grab(groupId:'org.apache.poi', artifactId:'poi', version:'3.7', classLoader:loader)
        def jars = jarNames(loader)
        // because poi asks for log4j 1.2.13, but we already have 1.1.3 so it won't be loaded
        assert jars.contains('log4j-1.1.3.jar')
        assert !jars.contains('log4j-1.2.13.jar')

        Grape.grab(groupId:'log4j', artifactId:'log4j', version:'1.2.13', classLoader:loader)
        jars = jarNames(loader)
        // because log4j 1.1.3 was loaded first, 1.2.13 should not get loaded
        // even though it was explicitly asked for
        assert jars.contains('log4j-1.1.3.jar')
        assert !jars.contains('log4j-1.2.13.jar')
    }

    @Test
    void testConf1() {
        Set noJars = [
        ]
        Set coreJars = [
            'ivy-2.0.0.jar'
        ]
        Set testJars = [
            'commons-lang-2.3.jar',
            'junit-3.8.2.jar'
        ]
        Set optionalJars = [
            'ant-1.6.2.jar',
            'ant-nodeps-1.6.2.jar',
            'ant-trax-1.6.2.jar',
            'commons-codec-1.2.jar',
            'commons-httpclient-3.0.jar',
            'commons-logging-1.0.4.jar',
            'commons-vfs-1.0.jar',
            'jsch-0.1.31.jar',
            'junit-3.8.1.jar',
            'oro-2.0.8.jar'
        ]

        def loader = new GroovyClassLoader()
        Grape.grab(groupId:'org.apache.ivy', artifactId:'ivy', version:'2.0.0', classLoader:loader)
        def jars = jarNames(loader)
        assert jars == coreJars, 'assert that no extraneous jars are present'
        assert coreJars - jars == noJars, 'assert that all core jars are present'
        assert testJars - jars == testJars, 'assert that no test jars are present'
        assert optionalJars - jars == optionalJars, 'assert that no optional jars are present'

        loader = new GroovyClassLoader()
        Grape.grab(groupId:'org.apache.ivy', artifactId:'ivy', version:'2.0.0', conf:'optional', classLoader:loader)
        jars = jarNames(loader)
        assert jars == optionalJars, 'assert that no extraneous jars are present'
        assert coreJars - jars == coreJars, 'assert that no core jars are present'
        assert testJars - jars == testJars, 'assert that no test jars are present'
        assert optionalJars - jars == noJars, 'assert that all optional jars are present'

        loader = new GroovyClassLoader()
        Grape.grab(groupId:'org.apache.ivy', artifactId:'ivy', version:'2.0.0', conf:['default', 'optional'], classLoader:loader)
        jars = jarNames(loader)
        assert coreJars - jars == noJars, 'assert that all core jars are present'
        assert testJars - jars == testJars, 'assert that no test jars are present'
        assert optionalJars - jars == noJars, 'assert that all optional jars are present'
        assert jars == coreJars + optionalJars, 'assert that no extraneous jars are present'
    }

    @Test // GROOVY-8372
    void testConf2() {
        def tempDir = File.createTempDir()
        def jarsDir = new File(tempDir, 'foo/bar/jars'); jarsDir.mkdirs()

        new File(jarsDir, 'bar-1.2.3.jar').createNewFile()
        new File(jarsDir, 'baz-1.2.3.jar').createNewFile()

        new File(tempDir, 'ivysettings.xml').write '''\
            <?xml version="1.0" encoding="UTF-8"?>
            <ivysettings>
                <caches useOrigin="true" />
                <resolvers>
                    <filesystem name="downloadGrapes">
                        <ivy pattern="${ivy.settings.dir}/[organization]/[module]/ivy-[revision].xml" />
                        <artifact pattern="${ivy.settings.dir}/[organization]/[module]/[type]s/[artifact]-[revision].[ext]" />
                    </filesystem>
                </resolvers>
            </ivysettings>
            '''.stripIndent()

        new File(tempDir, 'foo/bar/ivy-1.2.3.xml').write '''\
            <?xml version="1.0" encoding="UTF-8"?>
            <ivy-module version="2.0" xmlns:m="http://ant.apache.org/ivy/maven">
                <info organisation="foo" module="bar" revision="1.2.3" status="release" />
                <configurations>
                    <conf name="default" visibility="public" extends="master" />
                    <conf name="master" visibility="public" />
                    <conf name="other" visibility="public" />
                </configurations>
                <publications>
                    <artifact name="bar" type="jar" ext="jar" conf="master" />
                    <artifact name="baz" type="jar" ext="jar" conf="other" />
                </publications>
            </ivy-module>
            '''.stripIndent()

        System.setProperty('grape.config', tempDir.absolutePath + File.separator + 'ivysettings.xml')
        try {
            Grape.@instance = null
            def loader = new GroovyClassLoader()
            // request conf="other" which should resolve to artifact "baz-1.2.3.jar"
            def uris = Grape.resolve(classLoader:loader, validate:false, [group:'foo', module:'bar', version:'1.2.3', conf:'other'])

            def jars = uris.collect { uri -> uri.path.split('/')[-1] } as Set
            assert 'baz-1.2.3.jar' in jars
            assert 'bar-1.2.3.jar' !in jars
        } finally {
            System.clearProperty('grape.config')
            Grape.@instance = null
            tempDir.deleteDir()
        }
    }

    @Test
    void testClassifier() {
        def shell = new GroovyShell(new GroovyClassLoader())
        shouldFail(CompilationFailedException) {
            shell.evaluate('import net.sf.json.JSON; JSON')
        }
        Grape.grab(groupId:'net.sf.json-lib', artifactId:'json-lib', version:'2.2.3', classifier:'jdk15', classLoader:shell.classLoader)
        assert shell.evaluate('import net.sf.json.JSON; JSON').name == 'net.sf.json.JSON'
    }

    @Test
    void testClassifierWithConf() {
        Set coreJars = [
            'json-lib-2.2.3-jdk15.jar',
            'commons-beanutils-1.7.0.jar',
            'commons-collections-3.2.jar',
            'commons-lang-2.4.jar',
            'commons-logging-1.1.1.jar',
            'ezmorph-1.0.6.jar'
        ]
        Set optionalJars = [
            'ant-1.7.0.jar',
            'ant-launcher-1.7.0.jar',
            'dom4j-1.6.1.jar',
            'groovy-all-1.5.7.jar',
            'jaxen-1.1-beta-8.jar',
            'jdom-1.0.jar',
            'jline-0.9.94.jar',
            'jruby-1.1.jar',
            'junit-3.8.2.jar',
            'log4j-1.2.14.jar',
            'oro-2.0.8.jar',
            'xalan-2.7.0.jar',
            'xercesImpl-2.6.2.jar',
            'xmlParserAPIs-2.6.2.jar',
            'xom-1.1.jar'
        ]

        def loader = new GroovyClassLoader()
        Grape.grab(groupId:'net.sf.json-lib', artifactId:'json-lib', version:'2.2.3', classifier:'jdk15', classLoader:loader)
        assert jarNames(loader) == coreJars

        loader = new GroovyClassLoader()
        Grape.grab(groupId:'net.sf.json-lib', artifactId:'json-lib', version:'2.2.3', classifier:'jdk15', conf:'optional', classLoader:loader)
        assert jarNames(loader) == optionalJars

        loader = new GroovyClassLoader()
        Grape.grab(groupId:'net.sf.json-lib', artifactId:'json-lib', version:'2.2.3', classifier:'jdk15', conf:['default', 'optional'], classLoader:loader)
        assert jarNames(loader) == coreJars + optionalJars
    }

    @Test // BeanUtils is a transitive dependency for Digester
    void testTransitiveShorthandControl() {
        assertScript '''
            @Grab('commons-digester:commons-digester:2.1')
            import org.apache.commons.digester.Digester

            assert Digester.name.size() == 36
            assert org.apache.commons.beanutils.BeanUtils.name.size() == 38
        '''
    }

    @Test
    void testTransitiveShorthandExpectFailure() {
        shouldFail MissingPropertyException, '''
            @Grab('commons-digester:commons-digester:2.1;transitive=false')
            import org.apache.commons.digester.Digester

            assert Digester.name.size() == 36
            assert org.apache.commons.beanutils.BeanUtils.name.size() == 38
        '''
    }

    @Test
    void testAutoDownloadGrapeConfig() {
        assertScript '''
            @Grab('commons-digester:commons-digester:2.1;transitive=false')
            import org.apache.commons.digester.Digester

            assert Digester.name.size() == 36
        '''
        assert Grape.instance.ivyInstance.settings.defaultResolver.name == 'downloadGrapes'

        assertScript '''
            @Grab('commons-digester:commons-digester:2.1;transitive=false')
            @GrabConfig(autoDownload=false)
            import org.apache.commons.digester.Digester

            assert Digester.name.size() == 36
        '''
        assert Grape.instance.ivyInstance.settings.defaultResolver.name == 'cachedGrapes'

        assertScript '''
            @Grab('commons-digester:commons-digester:2.1;transitive=false')
            @GrabConfig(autoDownload=true)
            import org.apache.commons.digester.Digester

            assert Digester.name.size() == 36
        '''
        assert Grape.instance.ivyInstance.settings.defaultResolver.name == 'downloadGrapes'
    }

    @Test // GROOVY-470: multiple jars should be loaded for an artifacts with and without a classifier
    void testClassifierAndNonClassifierOnSameArtifact() {
        GroovyClassLoader loader = new GroovyClassLoader()
        Grape.grab(groupId:'org.neo4j', artifactId:'neo4j-kernel', version:'2.0.0-RC1', classLoader:loader)
        Grape.grab(groupId:'org.neo4j', artifactId:'neo4j-kernel', version:'2.0.0-RC1', classifier:'tests', classLoader:loader)

        def jars = jarNames(loader)
        assert jars.contains('neo4j-kernel-2.0.0-RC1.jar')
        assert jars.contains('neo4j-kernel-2.0.0-RC1-tests.jar')

        // also check reverse ordering of deps
        loader = new GroovyClassLoader()
        Grape.grab(groupId:'org.neo4j', artifactId:'neo4j-kernel', version:'2.0.0-RC1', classifier:'tests', classLoader:loader)
        Grape.grab(groupId:'org.neo4j', artifactId:'neo4j-kernel', version:'2.0.0-RC1', classLoader:loader)

        jars = jarNames(loader)
        assert jars.contains('neo4j-kernel-2.0.0-RC1.jar')
        assert jars.contains('neo4j-kernel-2.0.0-RC1-tests.jar')
    }

    @Test // GROOVY-7548
    void testSystemProperties() {
        System.setProperty('groovy7548prop', 'x')
        assert System.getProperty('groovy7548prop') == 'x'
        try {
            assertScript '''
                @GrabConfig(systemProperties='groovy7548prop=y')
                @Grab('commons-lang:commons-lang:2.6')
                import org.apache.commons.lang.StringUtils

                assert StringUtils.name == 'org.apache.commons.lang.StringUtils'
            '''
            assert System.getProperty('groovy7548prop') == 'y'
        } finally {
            System.clearProperty('groovy7548prop')
        }
    }

    @Test // GROOVY-7649
    void testResolveSucceedsAfterFailure() {
        GroovyClassLoader loader = new GroovyClassLoader()

        shouldFail {
            Grape.resolve([classLoader:loader], [], [groupId:'bogus', artifactId:'bogus', version:'0.1'])
        }

        Grape.resolve([classLoader:loader], [], [groupId:'org.apache.poi', artifactId:'poi', version:'3.7'])
    }

    @Test // GROOVY-9312
    void testResolveSucceedsFromLocalMavenRepository() {
        assumeTrue System.getProperty('maven.home') != null
        def tempDir = File.createTempDir()

        new File(tempDir, 'pom.xml').write '''\
            <?xml version="1.0" encoding="UTF-8"?>
            <project
              xmlns="http://maven.apache.org/POM/4.0.0"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
                <modelVersion>4.0.0</modelVersion>
                <groupId>org.codehaus.groovy.tests</groupId>
                <artifactId>maven-bootstrap</artifactId>
                <version>1.0-SNAPSHOT</version>
                <dependencies>
                    <dependency>
                        <groupId>net.jqwik</groupId>
                        <artifactId>jqwik</artifactId>
                        <version>1.2.1</version>
                    </dependency>
                    <dependency>
                        <groupId>net.jqwik</groupId>
                        <artifactId>jqwik-engine</artifactId>
                        <version>1.2.1</version>
                    </dependency>
                </dependencies>
            </project>
        '''.stripIndent()

        try {
            // prime "${user.home}/.m2/repository" with jqwik
            assertScript """
                @Grab('org.apache.maven.shared:maven-invoker:3.0.1')
                import org.apache.maven.shared.invoker.*

                def request = new DefaultInvocationRequest(goals: ['compile'],
                    baseDirectory: new File('${tempDir.absolutePath.replace('\\', '\\\\')}'))
                def result = new DefaultInvoker().execute(request)
                assert result.exitCode == 0
            """

            assertScript '''
                @Grab('net.jqwik:jqwik:1.2.1')
                import net.jqwik.api.ForAll
                import net.jqwik.api.Property

                @groovy.transform.CompileStatic
                class Jsr308 {
                    @Property
                    boolean 'size zero or positive'(@ForAll List<Integer> items) {
                        items.size() >= 0
                    }
                }

                println 'success'
            '''
        } finally {
            tempDir.deleteDir()
        }
    }
}
