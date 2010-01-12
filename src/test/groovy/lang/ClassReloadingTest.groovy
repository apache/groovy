package groovy.lang

class ClassReloadingTest extends GroovyTestCase {

    public void testReloading() {
        def file = File.createTempFile("TestReload", ".groovy", new File("target"))
        file.deleteOnExit()
        def className = file.name - ".groovy"

        def cl = new GroovyClassLoader(this.class.classLoader);
        def currentDir = file.parentFile.absolutePath
        cl.addClasspath(currentDir)
        cl.shouldRecompile = true

        try {
            file.write """
              class $className {
                def greeting = "hello"
              }
            """
            def groovyClass = cl.loadClass(className, true, false)
            def message = groovyClass.newInstance().greeting
            assert "hello" == message

            sleep 1500

            // change class
            file.write """
              class $className {
                def greeting = "goodbye"
              }
            """
            def success = file.setLastModified(System.currentTimeMillis())
            assert success
            sleep 500

            // reload
            groovyClass = cl.loadClass(className, true, false)
            message = groovyClass.newInstance().greeting
            assert "goodbye" == message
        } finally {
            file.delete()
        }
    }

    public void testReloadingInStringStringVersion() {
        def fileName = "Dummy3981.groovy"

        def cl = new GroovyClassLoader(this.class.classLoader);

        def classStr = """
            class Groovy3981 {
                def greeting = "hello"
            }
        """

        def groovyClass = cl.parseClass(classStr, fileName)
        def message = groovyClass.newInstance().greeting
        assert "hello" == message

        // (string, string) version should not do the caching as it breaks Spring integration (bean refreh)
        classStr = """
            class Groovy3981 {
                def greeting = "goodbye"
            }
        """
        groovyClass = cl.parseClass(classStr, fileName)
        message = groovyClass.newInstance().greeting
        assert "goodbye" == message
    }
}