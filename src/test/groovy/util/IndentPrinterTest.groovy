package groovy.util

/**
 * Unit test for IndentPrinter.
 * @author Hamlet D'Arcy
 */
class IndentPrinterTest extends GroovyTestCase {

    Writer out

    void setUp(){
        out = new StringWriter()
    }

    void testSimpleIndentation() {
        def printer = new IndentPrinter(new PrintWriter(out))

        printer.printIndent()
        printer.println 'parent'
        printer.incrementIndent()
        printer.printIndent()
        printer.println 'child'
        printer.decrementIndent()
        printer.printIndent()
        printer.println 'parent2'
        printer.flush()

        assert 'parent\n  child\nparent2\n' == out.toString()
    }

    void testAutoIndentation() {
        def printer = new IndentPrinter(new PrintWriter(out))
        printer.autoIndent = true

        printer.println 'parent'
        printer.incrementIndent()
        printer.println 'child'
        printer.decrementIndent()
        printer.println 'parent2'
        printer.flush()

        assert 'parent\n  child\nparent2\n' == out.toString()
    }

    void testInWithBlock() {
        new IndentPrinter(new PrintWriter(out)).with { p ->
            p.printIndent()
            p.println('parent1')
            p.incrementIndent()
            p.printIndent()
            p.println('child 1')
            p.printIndent()
            p.println('child 2')
            p.decrementIndent()
            p.printIndent()
            p.println('parent2')
            p.flush()
        }
        assert 'parent1\n  child 1\n  child 2\nparent2\n' == out.toString()
    }
}