package org.codehaus.groovy.control.io


class FileReaderTest extends GroovyTestCase {

    void testFileBOM() {
        def file = File.createTempFile("encoding", ".groovy")
        file.deleteOnExit()
        
        def fos = new FileOutputStream(file)
        // first write the byteorder mark for UTF-8
        fos.write(0xEF)
        fos.write(0xBB)
        fos.write(0xFF)
        fos.write("return 1".getBytes("US-ASCII"))
        fos.flush()
        fos.close()
        
        
        def gcl = new GroovyClassLoader()
        gcl.config.sourceEncoding = "UTF-8"
        def clazz = gcl.parseClass(file)
        def result = clazz.newInstance().run()
        assert result == 1
    }
}