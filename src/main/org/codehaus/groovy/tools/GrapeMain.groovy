/*
 * Copyright 2003-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.tools

import groovy.grape.Grape
import org.apache.ivy.util.DefaultMessageLogger
import org.apache.ivy.util.Message
import org.codehaus.groovy.runtime.InvokerHelper
import org.apache.commons.cli.*

//commands

install = {arg, cmd ->
    if (arg.size() > 4 || arg.size() < 3) {
        println 'install requires two or three arguments, <group> <module> [<version>]'
        return
    }
    def ver = '*'
    if (arg.size() == 4) {
        ver = arg[3]
    }

    // set the instance so we can re-set the logger
    Grape.getInstance()
    try {
        // set the logger to louder
        Message.setDefaultLogger(new DefaultMessageLogger(Message.MSG_INFO))
    } catch (Throwable e) {
        e.printStackTrace(System.out);
        // doesn't matter, we just won't turn up ivy's logging
    }
    def ex = Grape.grab(autoDownload: true, group: arg[1], module: arg[2], version: ver, noExceptions: true)
    if (ex) {
        println "An error occured : $ex"
    }
}

list = {arg, cmd ->
    println ""

    int moduleCount = 0
    int versionCount = 0

    Grape.enumerateGrapes().each {String groupName, Map group ->
        group.each {String moduleName, List<String> versions ->
            println "$groupName $moduleName  $versions"
            moduleCount++
            versionCount += versions.size()
        }
    }
    println ""
    println "$moduleCount Grape modules cached"
    println "$versionCount Grape module versions cached"
}

resolve = {arg, cmd ->
    Options options = new Options();
    options.addOption(
        OptionBuilder.hasArg(false)
            .withLongOpt("ant")
            .create('a')
    );
    options.addOption(
        OptionBuilder.hasArg(false)
            .withLongOpt("dos")
            .create('d')
    );
    options.addOption(
        OptionBuilder.hasArg(false)
            .withLongOpt("shell")
            .create('s')
    );
    CommandLine cmd2 = new PosixParser().parse(options, arg[1..-1] as String[], true);
    arg = cmd2.args

    // set the instance so we can re-set the logger
    Grape.getInstance()

    if ((arg.size() % 3) != 0) {
        println 'There need to be a multiple of three arguments: (group module version)+'
        return
    }
    if (args.size() < 3) {
        println 'At least one Grape reference is required'
        return
    }
    def before, between, after

    if (cmd2.hasOption('a')) {
        before = '<pathelement location="'
        between = '">\n<pathelement location="'
        after = '">'
    } else if (cmd2.hasOption('d')) {
        before = 'set CLASSPATH='
        between = ';'
        after = ''
    } else if (cmd2.hasOption('s')) {
        before = 'export CLASSPATH='
        between = ':'
        after = ''
    } else {
        before = ''
        between = '\n'
        after = '\n'
    }

    iter = arg.iterator()
    def params = [[:]]
    while (iter.hasNext()) {
        params.add([group: iter.next(), module: iter.next(), version: iter.next()])
    }

    try {
        def results = []
        for (URI uri: Grape.resolve(* params)) {
            if (uri.scheme == 'file') {
                results += new File(uri).path
            } else {
                results += uri.toASCIIString()
            }
        }

        if (results) {
            println "${before}${results.join(between)}${after}"
        } else {
            println 'Nothing was resolved'
        }
    } catch (Exception e) {
        println "Error in resolve:\n\t$e.message"
        if (e.message =~ /unresolved dependency/) println "Perhaps the grape is not installed?"
    }
}

def commands = [
    'install': [closure: install,
        shortHelp: 'Installs a particular grape'],
    'list': [closure: list,
        shortHelp: 'Lists all installed grapes'],
    'resolve': [closure: resolve,
        shortHelp: 'Enumerates the jars used by a grape']
]

// command line parsing
Options options = new Options();

options.addOption(
    OptionBuilder.withLongOpt("define")
        .withDescription("define a system property")
        .hasArg(true)
        .withArgName("name=value")
        .create('D')
);
options.addOption(
    OptionBuilder.hasArg(false)
        .withDescription("usage information")
        .withLongOpt("help")
        .create('h')
);
// this will turn on grape logging, once grape logging is written
//options.addOption(
//    OptionBuilder.hasArg(false)
//    .withDescription("more verbose output")
//    .withLongOpt("debug")
//    .create('d'));
options.addOption(
    OptionBuilder.hasArg(false)
        .withDescription("display the Groovy and JVM versions")
        .withLongOpt("version")
        .create('v')
);


CommandLine cmd = new PosixParser().parse(options, args, true);

grapeHelp = {
    int spacesLen = commands.keySet().max {it.length()}.length() + 3
    String spaces = ' ' * spacesLen

    PrintWriter pw = new PrintWriter(binding.variables.out ?: System.out)
    new HelpFormatter().printHelp(
        pw,
        80,
        "groovy [options] <command> [args]\n",
        "options:",
        options,
        2,
        4,
        null, // footer
        false);
    pw.flush()

    println ""
    println "commands:"
    commands.each {String k, v ->
        println "  ${(k + spaces).substring(0, spacesLen)} $v.shortHelp"
    }
    println ""
}

if (cmd.hasOption('h')) {
    grapeHelp()
    return
}

if (cmd.hasOption('v')) {
    String version = InvokerHelper.getVersion();
    println "Groovy Version: $version JVM: ${System.getProperty('java.version')}"
    return
}

cmd.getOptionValues('D')?.each {String prop ->
    def (k, v) = prop.split ('=', 2) as List // array multiple assignement quirk
    System.setProperty(k, v ?: "")
}

String[] arg = cmd.args
if (arg?.length == 0) {
    grapeHelp()
} else if (commands.containsKey(arg[0])) {
    commands[arg[0]].closure(arg, cmd)
} else {
    println "grape: '${arg[0]}' is not a grape command. See 'grape --help'"
}
