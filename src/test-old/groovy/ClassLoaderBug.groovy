class ClassLoaderBug extends GroovyTestCase {
    
    static void main(args) {
        gst = new ClassLoaderBug();
        gst.testWithOneVariable();
    }

    void testWithOneVariable() {
        println("Called method")
    }
}
