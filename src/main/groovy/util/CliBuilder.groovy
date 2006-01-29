import org.apache.commons.cli.*
import org.codehaus.groovy.runtime.InvokerHelper

/**
    Supported Option Properties:
    argName:        String
    longOpt:        String
    args:           int
    optionalArg:    boolean
    required:       boolean
    type:           Object
    valueSeparator: char

    @see org.apache.commons.cli.Option for meaning of the parameters.
    @see CliBuilderTest for example usages.
    @author Dierk Koenig
*/

class CliBuilder {

    // default settings: use setters to override
    @Property String usage             = ''
    @Property CommandLineParser parser = new PosixParser()
    @Property HelpFormatter printer    = new HelpFormatter()

    @Property Options options          = new Options()

    /**
        Recognize all one-character method calls as option specifications.
    */
    def invokeMethod(String name, Object args){
        if (1 == name.size()) {
            options.addOption(option(name, args[0], args[1]))
            return null
        }
        return InvokerHelper.getMetaClass(this).invokeMethod(this, name, args)
    }

    /**
        Make a CommandLine object from commant line args with parser (default: Posix).
        Exits on bad command lines.
    */
    CommandLine cmd (args) {
        try {
            return parser.parse(options, args as String[], true)
        } catch (ParseException pe) {
            println("error: " + pe.getMessage())
            help()
            System.exit(1)
        }
    }

    /**
        Print the help and usage message with printer (default: HelpFormatter)
    */
    void help(){
        printer.printHelp(usage, options)
    }

    // implementation details -------------------------------------

    /**
        How to create an option from the specification.
    */
    Option option (shortname, Map details, info){
        Option option = new Option( shortname, info)
        details.each { key, value ->
            if (key == 'optionalArg')       // surprising that this extra handling is needed
                option.setOptionalArg(value)
            else
                option[key] = value
        }
        return option
    }
}