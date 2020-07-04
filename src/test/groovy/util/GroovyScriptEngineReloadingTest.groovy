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
package groovy.util

import groovy.test.GroovyTestCase

import java.util.concurrent.ConcurrentHashMap
import org.codehaus.groovy.control.CompilerConfiguration

class GroovyScriptEngineReloadingTest extends GroovyTestCase {
    GroovyScriptEngine gse

    void setUp() {
        MapFileSystem.instance.registerMapFileSystem()
        makeGSE(null)
    }

    private void makeGSE(ClassLoader parent) {
        if (parent == null) {
            gse = new GroovyScriptEngine([MapUrlConnection.URL_SCHEME] as String[]) {
                long time = 1000

                protected long getCurrentTime() {
                    return time
                }
            }
        } else {
            gse = new GroovyScriptEngine([MapUrlConnection.URL_SCHEME] as String[], parent) {
                long time = 1000

                protected long getCurrentTime() {
                    return time
                }
            }
        }
    }

    private void sleep(int i) {
        gse.@time += i
    }

    private void execute(intervall, sleepTime, expected) {
        gse.config.minimumRecompilationInterval = intervall
        sleep intervall

        Binding binding = new Binding()
        int val = 0
        binding.setVariable("val", val)
        MapFileSystem.instance.modFile("s_1", "val = 1", gse.@time)
        gse.run("s_1", binding)

        assert binding.getVariable("val") == 1

        sleep sleepTime

        MapFileSystem.instance.modFile("s_1", "val = 2", gse.@time)
        gse.run("s_1", binding)

        assert binding.getVariable("val") == expected

    }

    // test to ensure new source is no picked up
    void testIsSourceNewer() {
        execute(1000, 2000, 2)
        execute(1000, 5000, 2)
        execute(1000, 10000, 2)
    }
    // test to ensure new source is ignored till minimumRecompilationIntervall is passed
    void testRecompilationIntervall() {
        execute(100000, 10000, 1)
        execute(100000, 10000, 1)
        execute(100000, 200000, 2)
    }

    void testRecompilingWithGenerics() {
        MapFileSystem.instance.modFile('BaseClass.groovy', 'abstract class BaseClass<T> extends Script {}', gse.@time)

        def subClassText = '''
            class SubClass extends BaseClass<String> {
                @Override
                Object run() {
                    null
                }
            }
        '''
        MapFileSystem.instance.modFile('SubClass.groovy', subClassText, gse.@time)

        gse.loadScriptByName('SubClass.groovy')
        sleep 1000

        // make a change to the sub-class so that it gets recompiled
        MapFileSystem.instance.modFile('SubClass.groovy', subClassText + "\n", gse.@time)
        gse.loadScriptByName('SubClass.groovy')

    }

    void testDeleteDependent() {
        sleep 10000
        MapFileSystem.instance.modFile('ClassA.groovy', 'DependentClass ic = new DependentClass()', gse.@time as long)
        MapFileSystem.instance.modFile('DependentClass.groovy', 'class DependentClass {}', gse.@time as long)
        def clazz = gse.loadScriptByName('ClassA.groovy')
        assert clazz != null //classA is valid with dep
        sleep 11000
        MapFileSystem.instance.modFile('ClassA.groovy', "println 'this is a valid script'", gse.@time as long)
        MapFileSystem.instance.deleteFile('DependentClass.groovy')
        clazz = gse.loadScriptByName('ClassA.groovy')
        assert clazz != null //classA is valid with dep removed
    }

    void testReloadWith2ScriptsDependentOnSameBeanAndReloadForSecond() {
        gse.config.minimumRecompilationInterval = 1000
        writeBean(1)
        writeScript(1)

        def val1 = gse.run("script1.groovy", "")
        assert val1 == '1', "script1 should have returned 1"

        sleep 1
        writeBean(2)
        writeScript(2)
        val1 = gse.run("script1.groovy", "")
        assert val1 == '1', "script1 should have returned 1"

        sleep 10000

        def val2 = gse.run("script2.groovy", "")
        assert val2 == '2', "script2 should have returned 2"
    }

    void testReloadWith2ScriptsDependentOnSameBean() {
        gse.config.minimumRecompilationInterval = 1
        writeBean(1)
        writeScript(1)
        writeScript(2)

        def val1 = gse.run("script2.groovy", "")
        assert val1 == '1', "script2 should have returned 1"

        def val2 = gse.run("script1.groovy", "")
        assert val2 == '1', "script1 should have returned 1"

        sleep 10000
        writeBean(2)

        def val3 = gse.run("script1.groovy", "")
        assert val3 == '2', "script1 should have returned 2 after bean was modified but returned $val3"

        def val4 = gse.run("script2.groovy", "")
        assert val4 == '2', "script2 should have returned 2 after bean was modified but returned $val4"
    }

    private void writeClassBean() {
        def s = """
            class Bean {
                def getVal(){this.class.hashCode()}
            }
        """
        MapFileSystem.instance.modFile("Bean.groovy", s, gse.@time)
    }

    void testDependencyReloadNotTooOften() {
        gse.config.minimumRecompilationInterval = 1
        writeClassBean()
        writeScript(1)
        writeScript(2)

        def beanClass1 = gse.run("script2.groovy", "")
        def beanClass2 = gse.run("script1.groovy", "")
        assert beanClass1 == beanClass2, "bean class should have been compiled only once"
        def oldBeanClass = beanClass1

        sleep 10000
        writeClassBean()
        writeScript(1)
        writeScript(2)

        beanClass1 = gse.run("script2.groovy", "")
        beanClass2 = gse.run("script1.groovy", "")
        assert beanClass1 == beanClass2, "bean class should have been compiled only once"
        assert beanClass1 != oldBeanClass, "bean class was not recompiled"
    }

    void testReloadWhenModifyingAllScripts() {
        gse.config.minimumRecompilationInterval = 1
        writeBean(1)
        writeScript(1)
        writeScript(2)

        def val1 = gse.run("script2.groovy", "")
        assert val1 == '1', "script2 should have returned 1"

        def val2 = gse.run("script1.groovy", "")
        assert val2 == '1', "script1 should have returned 1"

        // write Scripts stay the same, timestamps updated
        writeScript(1)
        writeScript(2)
        sleep 10000

        val1 = gse.run("script2.groovy", "")
        assert val1 == '1', "script2 should have returned 1"
        val2 = gse.run("script1.groovy", "")
        assert val2 == '1', "script1 should have returned 1"

        // Modify Bean to return new value
        sleep 10000
        writeBean(2)

        def val3 = gse.run("script1.groovy", "")
        assert val3 == '2', "script1 should have returned 2 after bean was modified but returned $val3"
        def val4 = gse.run("script2.groovy", "")
        assert val4 == '2', "script2 should have returned 2 after bean was modified but returned $val4"
    }

    void writeScript(int name) throws IOException {
        def s = """
            def b = new Bean()
            return b.getVal()
        """
        MapFileSystem.instance.modFile("script${name}.groovy", s, gse.@time)
    }

    void writeBean(int d) throws IOException {
        def s = """
            class Bean {
                String prop0
                String prop${d}
                def getVal(){"$d"}
            }
        """
        MapFileSystem.instance.modFile("Bean.groovy", s, gse.@time)
    }

    void testDynamicInstantiation() throws Exception {
        MapFileSystem.instance.modFile("script.groovy",
                """
               def obj = dynaInstantiate.instantiate(className, getClass().getClassLoader())
               obj.modifyWidth(dim, addThis)
               returnedMessage = obj.message
           """, 0)

        MapFileSystem.instance.modFile("com/company/MakeMeSuper.groovy",
                """
              package com.company
              import com.company.util.*
              class MakeMeSuper{
                 private HelperIntf helper = new Helper()
                 def getMessage(){
                         helper.getMessage()
                 }
              }
          """, 0)

        MapFileSystem.instance.modFile("com/company/MakeMe.groovy",
                """
              package com.company
              class MakeMe extends MakeMeSuper{
                 def modifyWidth(dim, addThis){
                    dim.width += addThis
                 }
              }
          """, 0)

        MapFileSystem.instance.modFile("com/company/util/HelperIntf.groovy",
                """
              package com.company.util
              interface HelperIntf{
                 public String getMessage();
              }
          """, 0)

        MapFileSystem.instance.modFile("com/company/util/Helper.groovy",
                """
              package com.company.util
              class Helper implements HelperIntf{
                 public String getMessage(){
                       'worked'
                 }
              }
           """, 0)

        //Code run in the script will modify this dimension object.
        MyDimension dim = new MyDimension();

        Binding binding = new Binding();
        binding.setVariable("dim", dim);
        binding.setVariable("dynaInstantiate", this);

        binding.setVariable("className", "com.company.MakeMe");

        int addThis = 3;
        binding.setVariable("addThis", addThis);

        gse.run("script.groovy", binding);

        //The script instantiated com.company.MakeMe via our own
        //instantiate method.  The instantiated object modified the
        //width of our Dimension object, adding the value of our
        //'addThis' variable to it.
        assertEquals(new MyDimension(addThis, 0), dim);

        assertEquals('worked', binding.getVariable("returnedMessage"))
    }

    /**
     * The script passes the className of the class it's supposed to
     * instantiate to this method, expecting a newly instantiated object
     * in return.  The reason this is not done in the script is that
     * we want to ensure that no unforeseen problems occur if
     * the instantiation is not actually done inside the script,
     * since real-world usages will likely require delegating that
     * job.
     */
    Object instantiate(String className, ClassLoader classLoader) {
        Class clazz = null;
        try {
            clazz = Class.forName(className, true, classLoader);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Class.forName failed for  " + className, ex);
        }
        try {
            return clazz.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException("Could not instantiate object of class " + className, ex);
        }
    }

    /**
     * Test for GROOVY-3281, to ensure details passed through CompilerConfiguration are inherited by GSE.
     */
    void testCompilerConfigurationInheritance() {
        CompilerConfiguration cc = new CompilerConfiguration();
        cc.scriptBaseClass = CustomBaseClass.name

        GroovyClassLoader cl = new GroovyClassLoader(this.class.getClassLoader(), cc)
        makeGSE(cl);

        MapFileSystem.instance.modFile(
                "groovyScriptEngineSampleScript.groovy",
                'println "Hello Guillaume, is it a Groovy day?"', 0)
        def aScript = gse.createScript("groovyScriptEngineSampleScript.groovy", new Binding())

        assert aScript instanceof CustomBaseClass
    }

    /** GROOVY-3893 */
    void testGSEWithNoScriptRoots() {
        shouldFail ResourceException, {
            String[] emptyScriptRoots = []
            GroovyScriptEngine gse = new GroovyScriptEngine(emptyScriptRoots)
            gse.run("unknownScriptName", "")
        }
    }

    /** GROOVY-6203 */
    void testGSEBaseClass() {
        CompilerConfiguration cc = new CompilerConfiguration()
        cc.setScriptBaseClass(CustomBaseClass.name)

        makeGSE(null)
        gse.setConfig(cc)

        MapFileSystem.instance.modFile(
                "Groovy6203Helper.groovy",
                'println "Hello Guillaume, is it a Groovy day?"', 0)

        def script = gse.createScript("Groovy6203Helper.groovy", new Binding())
        assert script instanceof CustomBaseClass
    }

    /** GROOVY-4013 */
    void testGSENoCachingOfInnerClasses() {
        MapFileSystem.instance.modFile("Groovy4013Helper.groovy",
                """
               import java.awt.event.*
               import java.awt.*
               class Groovy4013Helper
               {
                  def initPanel()
                  {
                       def b = new Button('click me')
                       b.addActionListener( new ActionListener(){
                           public void actionPerformed(ActionEvent e) {}
                       })
                  }
               }
           """, 0)

        def klazz = gse.loadScriptByName('Groovy4013Helper.groovy')
        assert klazz.name == 'Groovy4013Helper'

        klazz = gse.loadScriptByName('Groovy4013Helper.groovy')
        assert klazz.name == 'Groovy4013Helper' // we should still get the outer class, not inner one
    }

    /** GROOVY-4234 */
    void testGSERunningAScriptThatHasMultipleClasses() {
        MapFileSystem.instance.modFile("Groovy4234Helper.groovy",
                """
              class Foo4234 {
                  static main(args){
                      //println "Running Foo4234 -> main()"
                  }
              }
              class Bar4234 { }
          """, 0)

        //println "testGSELoadingAScriptThatHasMultipleClasses - Run 1"
        gse.run("Groovy4234Helper.groovy", new Binding())

        //println "testGSELoadingAScriptThatHasMultipleClasses - Run 2"
        gse.run("Groovy4234Helper.groovy", new Binding())
    }

    /** GROOVY-2811 and GROOVY-4286  */
    void testReloadingInterval() {
        gse.config.minimumRecompilationInterval = 1500
        def binding = new Binding([:])
        def scriptName = "gse.groovy"

        MapFileSystem.instance.modFile(scriptName, "1", 0)
        sleep 1000
        // first time, the script is compiled and cached
        assert gse.run(scriptName, binding) == 1

        MapFileSystem.instance.modFile(scriptName, "12", gse.@time)
        sleep 3000
        // the file was updated, and we waited for more than the minRecompilationInterval
        assert gse.run(scriptName, binding) == 12

        MapFileSystem.instance.modFile(scriptName, "123", gse.@time)
        sleep 1000
        // still the old result, as we didn't wait more than the minRecompilationInterval
        assert gse.run(scriptName, binding) == 12

        sleep 2000
        // we've waited enough, so we get the new output
        assert gse.run(scriptName, binding) == 123
    }


    class MapFileEntry {
        String content
        long lutime

        MapFileEntry(String content, long lutime) {
            this.content = content
            this.lutime = lutime
        }
    }

    @Singleton
    class MapFileSystem {

        public final ConcurrentHashMap<String, MapFileEntry> fileCache = new ConcurrentHashMap<String, MapFileEntry>()
        private boolean registered = false

        void registerMapFileSystem() {
            if (!registered) {
                URL.setURLStreamHandlerFactory(new MapUrlFactory())
                registered = true
            }
        }

        void modFile(String name, String content, long lutime) {
            if (fileCache.containsKey(name)) {
                MapFileEntry sce = fileCache.get(name)
                sce.content = content
                sce.lutime = lutime
            } else {
                fileCache.put(name, new MapFileEntry(content, lutime))
            }
        }

        def deleteFile(String name) {
            return fileCache.remove(name)
        }

        String getFilesrc(String name) {
            return fileCache.get(name).content
        }

        boolean fileExists(String name) {
            return fileCache.containsKey(name)
        }
    }

    class MapUrlHandler extends URLStreamHandler {

        MapUrlHandler() {
            super()
        }

        protected URLConnection openConnection(URL u) throws IOException {
            return new MapUrlConnection(u)
        }

        protected void parseURL(URL u, String spec, int start, int limit) {
            super.parseURL(u, spec, start, limit)
        }
    }

    class MapUrlConnection extends URLConnection {
        String getContentEncoding() {
            return CHARSET
        }

        Object getContent() throws IOException {
            return super.content
        }

        public static final String CHARSET = "UTF-8"
        public static final String URL_HOST = "local"
        public static final String PROTOCOL = "map"
        public static final String URL_SCHEME = PROTOCOL + "://" + URL_HOST + "/"

        private String name

        InputStream getInputStream() throws IOException {
            // System.out.println(name+"\t"+MapFileSystem.fileCache.get(name).content);
            if (MapFileSystem.instance.fileCache.containsKey(name)) {
                String content = MapFileSystem.instance.fileCache.get(name).content
                return new ByteArrayInputStream(content.getBytes(CHARSET))
            } else {
                throw new IOException("file not found" + name)
            }
        }

        long getLastModified() {
            long lastmodified = 0
            if (MapFileSystem.instance.fileCache.containsKey(name)) {
                lastmodified = MapFileSystem.instance.fileCache.get(name).lutime
            }
            // System.out.println(name+"\t"+lastmodified);
            return lastmodified
        }

        URL getURL() {
            return super.getURL()
        }

        MapUrlConnection(URL url) {
            super(url)
            name = url.getFile()
            if (name.startsWith("/")) {
                name = name.substring(1)
            }
        }

        void connect() throws IOException {}
    }

    class MapUrlFactory implements URLStreamHandlerFactory {

        MapUrlFactory() {
            super()
        }

        URLStreamHandler createURLStreamHandler(String protocol) {
            if (MapUrlConnection.PROTOCOL.equals(protocol)) {
                return new MapUrlHandler()
            } else {
                return null
            }
        }
    }

    static class MyDimension {
        int width
        int height

        MyDimension(int x, int y) {
            width = x
            height = y
        }

        MyDimension() {
            width = 0
            height = 0
        }

        boolean equals(o) { o.width == width && o.height == height }

        int hashCode() { width + 13 * height }
    }

    static abstract class CustomBaseClass extends Script {}
}
