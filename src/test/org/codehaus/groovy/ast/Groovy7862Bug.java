package org.codehaus.groovy.ast;

import groovy.lang.GroovyShell;
import groovy.util.GroovyTestCase;
import org.codehaus.groovy.control.CompilerConfiguration;

public class Groovy7862Bug extends GroovyTestCase {
  public void testComplexTypeArguments() throws Exception {
    String script = "def f(org.codehaus.groovy.ast.Groovy7862Bug.C1 c1) { }";

    CompilerConfiguration config = new CompilerConfiguration();
    config.getOptimizationOptions().put("asmResolving", false);

    GroovyShell shell = new GroovyShell(config);
    shell.evaluate(script, "bug7862.groovy");
  }

  public static class C1<T2 extends C2<T2, T1>, T1 extends C1<T2, T1>> {
  }

  public static class C2<T2 extends C2<T2, T1>, T1 extends C1<T2, T1>> {
  }
}
