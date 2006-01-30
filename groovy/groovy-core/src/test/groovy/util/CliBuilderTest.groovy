class CliBuilderTest extends GroovyTestCase {

    void testSample() {
        def writer = new StringWriter()
        def cli = new CliBuilder(usage:'groovy [option]* filename', writer: new PrintWriter(writer))
        cli.h(longOpt: 'help', 'usage information')
        cli.c(argName: 'charset', args:1, longOpt: 'encoding', 'character encoding')
        cli.i(argName: 'extension', optionalArg: true,
             "modify files in place, create backup if extension is given (e.g. \'.bak\')")

        assert cli.options.toString() == "[ Options: [ short {i=[ option: i  :: modify files in place, create backup if extension is given (e.g. '.bak') ], c=[ option: c encoding  :: character encoding ], h=[ option: h help  :: usage information ]} ] [ long {encoding=[ option: c encoding  :: character encoding ], help=[ option: h help  :: usage information ]} ]"

        def options = cli.parse(['-h','-c','ASCII'])

        assert options.hasOption('h')
        assert options.h
        if (options.h) cli.usage()
        assert writer.toString().tokenize("\r\n").join("\n") ==
'''usage: groovy [option]* filename
 -c,--encoding <charset>   character encoding
 -h,--help                 usage information
 -i                        modify files in place, create backup if
                           extension is given (e.g. '.bak')'''

        assert options.hasOption('c')
        assert options.c
        assertEquals 'ASCII', options.getOptionValue('c')
        assertEquals 'ASCII', options.c
        assertEquals 'ASCII', options.encoding

        assertEquals false, options.noSuchOptionGiven
        assertEquals false, options.x
    }

    void testMultipleArgs() {
        def cli = new CliBuilder()
        cli.a(longOpt:'arg', args:2, valueSeparator:',' as char, 'arguments')
        def options = cli.parse(['-a','1,2'])
        assertEquals '1', options.a
        assertEquals(['1','2'], options.as)
        assertEquals '1', options.arg
        assertEquals(['1','2'], options.args)
    }

    void testArgs() {
        def cli = new CliBuilder()
        cli.a([:],'')
        def options = cli.parse(['-a','1','2'])
        assertEquals(['1','2'], options.getArgs().toList())
    }
}