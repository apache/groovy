/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.tools

import groovy.grape.Grape
import groovy.transform.Field
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.OptionGroup
import org.apache.commons.cli.Options
import org.apache.ivy.util.DefaultMessageLogger
import org.apache.ivy.util.Message

//commands

@Field install = {arg, cmd ->
    if (arg.size() > 5 || arg.size() < 3) {
        println 'install requires two to four arguments: <group> <module> [<version> [<classifier>]]'
        return
    }
    def ver = '*'
    if (arg.size() >= 4) {
        ver = arg[3]
    }
    def classifier = null
    if (arg.size() >= 5) {
        classifier = arg[4]
    }

    // set the instance so we can re-set the logger
    Grape.getInstance()
    setupLogging()

    cmd.getOptionValues('r')?.each { String url ->
        Grape.addResolver(name:url, root:url)
    }

    try {
        Grape.grab(autoDownload: true, group: arg[1], module: arg[2], version: ver, classifier: classifier, noExceptions: true)
    } catch (Exception e) {
        println "An error occured : $ex"
    }
}

@Field uninstall = {arg, cmd ->
    if (arg.size() != 4) {
        println 'uninstall requires three arguments: <group> <module> <version>'
        // TODO make version optional? support classifier?
//        println 'uninstall requires two to four arguments, <group> <module> [<version>] [<classifier>]'
        return
    }
    String group = arg[1]
    String module = arg[2]
    String ver = arg[3]
//    def classifier = null

    // set the instance so we can re-set the logger
    Grape.getInstance()
    setupLogging()

    if (!Grape.enumerateGrapes().find {String groupName, Map g ->
        g.any {String moduleName, List<String> versions ->
            group == groupName && module == moduleName && ver in versions
        }
    }) {
        println "uninstall did not find grape matching: $group $module $ver"
        def fuzzyMatches = Grape.enumerateGrapes().findAll { String groupName, Map g ->
            g.any {String moduleName, List<String> versions ->
                groupName.contains(group) || moduleName.contains(module) ||
                group.contains(groupName) || module.contains(moduleName)
            }
        }
        if (fuzzyMatches) {
            println 'possible matches:'
            fuzzyMatches.each { String groupName, Map g -> println "    $groupName: $g" }
        }
        return
    }
    Grape.instance.uninstallArtifact(group, module, ver)
}

@Field list = {arg, cmd ->
    println ""

    int moduleCount = 0
    int versionCount = 0

    // set the instance so we can re-set the logger
    Grape.getInstance()
    setupLogging()

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

@Field resolve = {arg, cmd ->
    Options options = new Options();
    options.addOption(Option.builder("a").hasArg(false).longOpt("ant").build());
    options.addOption(Option.builder("d").hasArg(false).longOpt("dos").build());
    options.addOption(Option.builder("s").hasArg(false).longOpt("shell").build());
    options.addOption(Option.builder("i").hasArg(false).longOpt("ivy").build());
    CommandLine cmd2 = new DefaultParser().parse(options, arg[1..-1] as String[], true);
    arg = cmd2.args

    // set the instance so we can re-set the logger
    Grape.getInstance()
    setupLogging(Message.MSG_ERR)

    if ((arg.size() % 3) != 0) {
        println 'There needs to be a multiple of three arguments: (group module version)+'
        return
    }
    if (args.size() < 3) {
        println 'At least one Grape reference is required'
        return
    }
    def before, between, after
    def ivyFormatRequested = false

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
    } else if (cmd2.hasOption('i')) {
        ivyFormatRequested = true
        before = '<dependency '
        between = '">\n<dependency '
        after = '">'
    } else {
        before = ''
        between = '\n'
        after = '\n'
    }

    iter = arg.iterator()
    def params = [[:]]
    def depsInfo = [] // this list will contain the module/group/version info of all resolved dependencies
    if(ivyFormatRequested) {
        params << depsInfo
    }
    while (iter.hasNext()) {
        params.add([group: iter.next(), module: iter.next(), version: iter.next()])
    }
    try {
        def results = []
        def uris = Grape.resolve(* params)
        if(!ivyFormatRequested) {
            for (URI uri: uris) {
                if (uri.scheme == 'file') {
                    results += new File(uri).path
                } else {
                    results += uri.toASCIIString()
                }
            }
        } else {
            depsInfo.each { dep ->
                results += ('org="' + dep.group + '" name="' + dep.module + '" revision="' + dep.revision)
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

@Field help = { arg, cmd -> grapeHelp() }

@Field commands = [
    'install': [closure: install,
        shortHelp: 'Installs a particular grape'],
    'uninstall': [closure: uninstall,
        shortHelp: 'Uninstalls a particular grape (non-transitively removes the respective jar file from the grape cache)'],
    'list': [closure: list,
        shortHelp: 'Lists all installed grapes'],
    'resolve': [closure: resolve,
        shortHelp: 'Enumerates the jars used by a grape'],
    'help': [closure: help,
        shortHelp: 'Usage information']
]

@Field grapeHelp = {
    int spacesLen = commands.keySet().max {it.length()}.length() + 3
    String spaces = ' ' * spacesLen

    PrintWriter pw = new PrintWriter(binding.variables.out ?: System.out)
    new HelpFormatter().printHelp(
            pw,
            80,
            "grape [options] <command> [args]\n",
            "options:",
            options,
            2,
            4,
            null, // footer
            true);
    pw.flush()

    println ""
    println "commands:"
    commands.each {String k, v ->
        println "  ${(k + spaces).substring(0, spacesLen)} $v.shortHelp"
    }
    println ""
}

@Field setupLogging = {int defaultLevel = 2 -> // = Message.MSG_INFO -> some parsing error :(
    if (cmd.hasOption('q')) {
        Message.setDefaultLogger(new DefaultMessageLogger(Message.MSG_ERR))
    } else if (cmd.hasOption('w')) {
        Message.setDefaultLogger(new DefaultMessageLogger(Message.MSG_WARN))
    } else if (cmd.hasOption('i')) {
        Message.setDefaultLogger(new DefaultMessageLogger(Message.MSG_INFO))
    } else if (cmd.hasOption('V')) {
        Message.setDefaultLogger(new DefaultMessageLogger(Message.MSG_VERBOSE))
    } else if (cmd.hasOption('d')) {
        Message.setDefaultLogger(new DefaultMessageLogger(Message.MSG_DEBUG))
    } else {
        Message.setDefaultLogger(new DefaultMessageLogger(defaultLevel))
    }
}

// command line parsing
@Field Options options = new Options();

options.addOption(Option.builder("D").longOpt("define").desc("define a system property").numberOfArgs(2).valueSeparator().argName("name=value").build());
options.addOption(Option.builder("r").longOpt("resolver").desc("define a grab resolver (for install)").hasArg(true).argName("url").build());
options.addOption(Option.builder("h").hasArg(false).desc("usage information").longOpt("help").build());

// Logging Level Options
options.addOptionGroup(
        new OptionGroup()
                .addOption(Option.builder("q").hasArg(false).desc("Log level 0 - only errors").longOpt("quiet").build())
                .addOption(Option.builder("w").hasArg(false).desc("Log level 1 - errors and warnings").longOpt("warn").build())
                .addOption(Option.builder("i").hasArg(false).desc("Log level 2 - info").longOpt("info").build())
                .addOption(Option.builder("V").hasArg(false).desc("Log level 3 - verbose").longOpt("verbose").build())
                .addOption(Option.builder("d").hasArg(false).desc("Log level 4 - debug").longOpt("debug").build())
)
options.addOption(Option.builder("v").hasArg(false).desc("display the Groovy and JVM versions").longOpt("version").build());

@Field CommandLine cmd

cmd = new DefaultParser().parse(options, args, true);

if (cmd.hasOption('h')) {
    grapeHelp()
    return
}

if (cmd.hasOption('v')) {
    String version = GroovySystem.getVersion();
    println "Groovy Version: $version JVM: ${System.getProperty('java.version')}"
    return
}

if (options.hasOption('D')) {
    options.getOptionProperties('D')?.each { k, v ->
        System.setProperty(k, v)
    }
}

String[] arg = cmd.args
if (arg?.length == 0) {
    grapeHelp()
} else if (commands.containsKey(arg[0])) {
    commands[arg[0]].closure(arg, cmd)
} else {
    println "grape: '${arg[0]}' is not a grape command. See 'grape --help'"
}
