package groovy

class ClassLoaderBug extends GroovyTestCase {
    
    static void main(args) {
        def gst = new ClassLoaderBug();
        gst.testWithOneVariable();
    }

    void testWithOneVariable() {
        println("Called method")
    }
}
