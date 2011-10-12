package groovy.bugs

class Groovy5025Bug extends GroovyTestCase {
    void testDisableAstBuilder() {
        def config = new org.codehaus.groovy.control.CompilerConfiguration()
        config.disabledGlobalASTTransformations = ['org.codehaus.groovy.ast.builder.AstBuilderTransformation']
        def script = '''
            new org.codehaus.groovy.ast.builder.AstBuilder().buildFromCode { "Hello" }
        '''

        def shell = new GroovyShell()
        assert shell.evaluate(script).class == ArrayList

        shell = new GroovyShell(config)
        shouldFail {
            shell.evaluate(script)
        }
    }
}
