class NullWriterTest extends GroovyTestCase {

    void testProperties() {
        assert NullWriter.DEFAULT instanceof NullWriter
    }

    void testWriterMethodsForCoverage() {
        def writer = NullWriter.DEFAULT
        writer.close()
        writer.flush()
        writer.write(null, 0, 0)
    }
}