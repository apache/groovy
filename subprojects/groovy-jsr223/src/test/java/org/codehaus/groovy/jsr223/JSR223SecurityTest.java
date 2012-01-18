package org.codehaus.groovy.jsr223;

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilationUnit.PrimaryClassNodeOperation;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.lang.reflect.Field;
import java.security.CodeSource;
import java.util.HashSet;
import java.util.Set;

/**
 * Test contributed by Tiago Fernandez, for GROOVY-3946
 */
public class JSR223SecurityTest {

    @Test(expected = ScriptException.class)
    public void should_forbid_an_instruction_when_overriding_GroovyClassLoader_using_reflection() throws Exception {
        secureEval("System.exit 2", "java.lang.System", true);
    }

    @Test(expected = ScriptException.class)
    public void should_forbid_an_instruction_when_overriding_GroovyClassLoader_using_injection() throws Exception {
        secureEval("System.exit 2", "java.lang.System", false);
    }

    private void secureEval(final String script, final String forbiddenInstruction, final boolean useReflection) throws Exception {
        final ScriptEngine groovyEngine = new ScriptEngineManager().getEngineByName("groovy");

        final GroovySecurityManager groovySecurityManager = GroovySecurityManager.instance();
        groovySecurityManager.overrideGroovyClassLoader(groovyEngine, useReflection);
        groovySecurityManager.forbid(forbiddenInstruction);

        groovyEngine.eval(script);
    }
}

class GroovySecurityManager {

    private final static GroovySecurityManager instance = new GroovySecurityManager();

    private final Set<String> blacklist = new HashSet<String>();

    private GroovySecurityManager() { }

    public synchronized static GroovySecurityManager instance() {
        return instance;
    }

    public void overrideGroovyClassLoader(final ScriptEngine engine, final boolean useReflection) {
        try {
            if (useReflection)
                overrideDefaultGroovyClassLoaderUsingReflection(engine);
            else
                overrideDefaultGroovyClassLoaderUsingInjection(engine);
        } catch (Throwable ex) {
            throw new RuntimeException("Could not initialize the security manager", ex);
        }
    }

    public void forbid(final String instruction) {
        blacklist.add(instruction);
    }

    public boolean isForbidden(final String instruction) {
        for (String forbidden : blacklist)
            if (instruction.startsWith(forbidden))
                return true;

        return false;
    }

    private void overrideDefaultGroovyClassLoaderUsingReflection(final ScriptEngine engine) throws Exception {
        final Field classLoader = engine.getClass().getDeclaredField("loader");
        classLoader.setAccessible(true);
        classLoader.set(engine, new CustomGroovyClassLoader());
    }

    private void overrideDefaultGroovyClassLoaderUsingInjection(final ScriptEngine engine) throws Exception {
        GroovyScriptEngineImpl concreteEngine = (GroovyScriptEngineImpl) engine;
        concreteEngine.setClassLoader(new CustomGroovyClassLoader());
    }
}

class GroovySecurityException extends RuntimeException {

    public GroovySecurityException(final String message) {
        super(message);
    }
}

class CustomGroovyClassLoader extends GroovyClassLoader {

    @Override
    protected CompilationUnit createCompilationUnit(final CompilerConfiguration config, final CodeSource source) {
        final CompilationUnit unit = super.createCompilationUnit(config, source);
        unit.addPhaseOperation(new CustomPrimaryClassNodeOperation(), Phases.SEMANTIC_ANALYSIS);
        return unit;
    }
}

class CustomPrimaryClassNodeOperation extends PrimaryClassNodeOperation {

    @Override
    public void call(final SourceUnit source, final GeneratorContext context, final ClassNode classNode) {
        for (Object statement : source.getAST().getStatementBlock().getStatements())
            ((ExpressionStatement) statement).visit(new CustomCodeVisitorSupport());
    }
}

class CustomCodeVisitorSupport extends CodeVisitorSupport {

    private final GroovySecurityManager groovySecurityManager = GroovySecurityManager.instance();

    @Override
    public void visitMethodCallExpression(final MethodCallExpression call) {
        if (groovySecurityManager.isForbidden(call.getText()))
            throw new GroovySecurityException("The following code is forbidden in the script: " + call.getText());
    }
}
