package org.codehaus.groovy.tools

class LoaderConfigurationTest extends GroovyTestCase {

    void testComment() {
        def txt = "# I am a comment"

        def config = new LoaderConfiguration()
        config.requireMain = false
        config.configure(new StringBufferInputStream(txt))

        assert config.classPathUrls.length == 0
    }

    void testNormalPath() {
        // generate a load instruction with a valid path
        def file = new File(".")
        def txt = "load $file"

        def config = new LoaderConfiguration()
        config.requireMain = false
        config.configure(new StringBufferInputStream(txt))

        assert config.classPathUrls.length == 1
        assert config.classPathUrls[0].sameFile(file.toURI().toURL())
    }

    void testNonexistingPath() {
        // generate a load instruction with a non-existing path
        def file = getNonexistantFile(new File("."))

        def txt = "load $file"

        def config = new LoaderConfiguration()
        config.requireMain = false
        config.configure(new StringBufferInputStream(txt))

        assert config.classPathUrls.length == 0
    }

    private File getNonexistantFile(File base) {
        def number = "0"
        while (base.exists()) {
            base = new File(base, number)
            number++
        }
        return base
    }

    void testExistingProperty() {
        def txt = 'load ${java.home}'

        def config = new LoaderConfiguration()
        config.requireMain = false
        config.configure(new StringBufferInputStream(txt))

        assert config.classPathUrls.length == 1
        def url1 = config.classPathUrls[0]
        def url2 = new File(System.getProperty("java.home")).toURI().toURL()
        assert url1.sameFile(url2)
    }

    void testNonexistingProperty() {
        String name = getNonexistingPropertyName("foo")

        def txt = 'load !{' + name + '}'

        def config = new LoaderConfiguration()
        config.requireMain = false
        shouldFail {
            config.configure(new StringBufferInputStream(txt))
        }

        txt = 'load ${' + name + '}'

        config = new LoaderConfiguration()
        config.requireMain = false
        config.configure(new StringBufferInputStream(txt))

        assert config.classPathUrls.length == 0
    }


    private getNonexistingPropertyName(String base) {
        while (System.getProperty(base) != null) {
            base += "x"
        }
        return base
    }
}