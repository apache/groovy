package groovy.swt.guibuilder;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class RunGuiBuilderDemo {

    public static void main(String[] args) throws Exception {
        String basePath = "src/examples/groovy/swt/guibuilder";
        GroovyScriptEngine scriptEngine = new GroovyScriptEngine(basePath);

        Binding binding = new Binding();
        ApplicationGuiBuilder guiBuilder = new ApplicationGuiBuilder(basePath);
        binding.setVariable("guiBuilder", guiBuilder);

        scriptEngine.run("GuiBuilderDemo.groovy", binding);
    }

}