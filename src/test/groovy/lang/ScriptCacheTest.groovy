package groovy.lang

class ScriptCacheTest extends GroovyTestCase {

    def packageName = "scriptcachetest"
    def className
    def cl = new GroovyClassLoader(this.class.classLoader);
    def file
    def packageDir

    public void setUp() {
        packageDir = new File(new File("target"), packageName)
        packageDir.mkdir()
        packageDir.deleteOnExit()
        file = File.createTempFile("TestScriptCache", ".groovy", packageDir)
        file.deleteOnExit()
        className = "${packageName}.${file.name - '.groovy'}"

        def currentDir = packageDir.parentFile.absolutePath
        cl.addClasspath(currentDir)
        file.write """
            package ${packageName}

            def greeting = "hello"
        """
    }

    public void tearDown() {
        file.delete()
        packageDir.delete()
    }

    public void testScriptCaching() {
        def groovyClass1 = cl.loadClass(className, true, false)
        def groovyClass2 = cl.loadClass(className, true, false)
        assert groovyClass1 == groovyClass2
    }

    void testScriptNaming() {
        def groovyClass1 = cl.loadClass(className, true, false)
        assert groovyClass1.getName() == className
    }
}
