package groovy.lang;

import org.codehaus.groovy.classgen.TestSupport;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.runtime.MethodClosure;

import java.io.IOException;

public class ScriptTest extends TestSupport
{
    public void testInvokeMethodFallsThroughToMethodClosureInBinding() throws IOException, CompilationFailedException, IllegalAccessException, InstantiationException
    {
        String text = "if (method() == 3) { println 'succeeded' }";

        GroovyCodeSource codeSource = new GroovyCodeSource(text, "groovy.script", "groovy.script");
        GroovyClassLoader loader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader());
        Class clazz = loader.parseClass(codeSource);
        Script script = ((Script) clazz.newInstance());

        Binding binding = new Binding();
        binding.setVariable("method", new MethodClosure(new Dummy(), "method"));
        script.setBinding(binding);

        script.run();
    }

    public static class Dummy {
        public Integer method() {
            return new Integer(3);
        }
    }
}
