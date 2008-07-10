package groovy.bugs

import groovy.xml.StreamingMarkupBuilder

class Groovy2491 extends GroovyTestCase {

    public Object invoke(Object target, String methodName, Object[] arguments) {
        assertEquals target, this

        StreamingMarkupBuilder b = new StreamingMarkupBuilder();
        Closure callable = (Closure)arguments [-1]
        Writable markup = (Writable)b.bind(callable);
        try {
            final StringWriter writer = new StringWriter()
            markup.writeTo(writer);
            return writer.toString()
        } catch (IOException e) {
            throw new Exception("I/O error executing render method for arguments ["+argMap+"]: " + e.getMessage(),e);
        }
    }

    void testRender () {

        Groovy2491.metaClass.render = {
            args, callable ->
              invoke (delegate, "render", [args, callable] as Object [])
        }

        metaClass = Groovy2491.metaClass
        assertEquals( "<hello>world</hello>", render(contentType:"text/xml") {
              hello("world")
        })
    }
}
