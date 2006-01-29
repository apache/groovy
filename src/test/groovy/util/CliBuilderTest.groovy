def cli = new CliBuilder(usage:'groovy [option]* filename')
cli.h(longOpt: 'help', 'usage information')
cli.c(argName: 'charset', args: 1, longOpt: 'encoding', 'character encoding')
cli.i(argName: 'extension', optionalArg: true,
     "modify files in place, create backup if extension is given (e.g. \'.bak\')")

assert cli.options.toString() == "[ Options: [ short {i=[ option: i  :: modify files in place, create backup if extension is given (e.g. '.bak') ], c=[ option: c encoding  :: character encoding ], h=[ option: h help  :: usage information ]} ] [ long {encoding=[ option: c encoding  :: character encoding ], help=[ option: h help  :: usage information ]} ]"

def cmd = cli.cmd(['-h','-c','ASCII'])
if (cmd.hasOption('h')) cli.help()
if (cmd.hasOption('c')) println "-c is ${cmd.getOptionValue('v')}"