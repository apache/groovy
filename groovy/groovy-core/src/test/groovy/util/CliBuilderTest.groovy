class CliBuilderTest extends GroovyTestCase {

    void testSample() {
        def cli = new CliBuilder(usage:'groovy [option]* filename')
        cli.h(longOpt: 'help', 'usage information')
        cli.c(argName: 'charset', args:1, longOpt: 'encoding', 'character encoding')
        cli.i(argName: 'extension', optionalArg: true,
             "modify files in place, create backup if extension is given (e.g. \'.bak\')")

        assert cli.options.toString() == "[ Options: [ short {i=[ option: i  :: modify files in place, create backup if extension is given (e.g. '.bak') ], c=[ option: c encoding  :: character encoding ], h=[ option: h help  :: usage information ]} ] [ long {encoding=[ option: c encoding  :: character encoding ], help=[ option: h help  :: usage information ]} ]"

        def cmd = cli.cmd(['-h','-c','ASCII'])

        assert cmd.hasOption('h')
        assert cmd.h
        if (cmd.h) cli.help()

        assert cmd.hasOption('c')
        assert cmd.c
        assertEquals 'ASCII', cmd.getOptionValue('c')
        assertEquals 'ASCII', cmd.c
        assertEquals 'ASCII', cmd.encoding

        assertEquals false, cmd.noSuchOptionGiven
        assertEquals false, cmd.x
    }

    void testMultipleArgs() {
        def cli = new CliBuilder()
        cli.a(longOpt:'arg', args:2, valueSeparator:',' as char, 'arguments')
        def cmd = cli.cmd(['-a','1,2'])
        assertEquals '1', cmd.a
        assertEquals(['1','2'], cmd.as.toList())
        assertEquals '1', cmd.arg
        assertEquals(['1','2'], cmd.args.toList())
    }

    void testArgs() {
        def cli = new CliBuilder()
        cli.a([:],'')
        def cmd = cli.cmd(['-a','1','2'])
        assertEquals(['1','2'], cmd.getArgs().toList())
    }
}