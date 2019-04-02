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
import org.apache.ivy.util.DefaultMessageLogger
import org.apache.ivy.util.Message
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import picocli.CommandLine.ParentCommand
import picocli.CommandLine.RunLast
import picocli.CommandLine.Unmatched

@SuppressWarnings('Println')
@Command(name = 'grape', description = 'Allows for the inspection and management of the local grape cache.',
        subcommands = [
                Install,
                Uninstall,
                ListCommand,
                Resolve,
                picocli.CommandLine.HelpCommand])
class GrapeMain implements Runnable {
    @Option(names = ['-D', '--define'], description = 'define a system property', paramLabel = '<name=value>')
    private final Map<String, String> properties = new LinkedHashMap<String, String>()

    @SuppressWarnings('UnusedPrivateField') // used in run()
    @Option(names = ['-r', '--resolver'], description = 'define a grab resolver (for install)', paramLabel = '<url>')
    private final List<String> resolvers = new ArrayList<String>()

    @Option(names = ['-q', '--quiet'], description = 'Log level 0 - only errors')
    private boolean quiet

    @Option(names = ['-w', '--warn'], description = 'Log level 1 - errors and warnings')
    private boolean warn

    @Option(names = ['-i', '--info'], description = 'Log level 2 - info')
    private boolean info

    @Option(names = ['-V', '--verbose'], description = 'Log level 3 - verbose')
    private boolean verbose

    @Option(names = ['-d', '--debug'], description = 'Log level 4 - debug')
    private boolean debug

    @Unmatched List<String> unmatched = new ArrayList<String>()

    private CommandLine parser

    static void main(String[] args) {
        GrapeMain grape = new GrapeMain()
        def parser = new CommandLine(grape)
        parser.addMixin('helpOptions', new HelpOptionsMixin())
        parser.subcommands.findAll { k, v -> k != 'help' }.each { k, v -> v.addMixin('helpOptions', new HelpOptionsMixin()) }

        grape.parser = parser
        parser.parseWithHandler(new RunLast(), args)
    }

    void run() {
        if (unmatched) {
            System.err.println "grape: '${unmatched[0]}' is not a grape command. See 'grape --help'"
        } else {
            parser.usage(System.out) // if no subcommand was specified
        }
    }

    @SuppressWarnings('UnusedPrivateMethod') // used in run()
    private void init() {
        properties.each { k, v ->
            System.setProperty(k, v)
        }
    }

    @SuppressWarnings('UnusedPrivateMethod') // used in run()
    private void setupLogging(int defaultLevel = 2) { // = Message.MSG_INFO -> some parsing error :(
        if (quiet) {
            Message.defaultLogger = new DefaultMessageLogger(Message.MSG_ERR)
        } else if (warn) {
            Message.defaultLogger = new DefaultMessageLogger(Message.MSG_WARN)
        } else if (info) {
            Message.defaultLogger = new DefaultMessageLogger(Message.MSG_INFO)
        } else if (verbose) {
            Message.defaultLogger = new DefaultMessageLogger(Message.MSG_VERBOSE)
        } else if (debug) {
            Message.defaultLogger = new DefaultMessageLogger(Message.MSG_DEBUG)
        } else {
            Message.defaultLogger = new DefaultMessageLogger(defaultLevel)
        }
    }

    /**
     * Defines help options (--help and --version) and a version provider used by the top-level command and all subcommands.
     * Intended to be installed as a picocli mixin.
     */
    // IMPLEMENTATION NOTE:
    // The @Command(mixinStandardHelpOptions = true) attribute cannot be used because
    // the unix standard short option for version help is uppercase -V, while previous versions
    // of this class use lowercase -v. This custom mixin preserves option compatibility.
    @Command(versionProvider = VersionProvider, sortOptions = false,
            parameterListHeading = '%nParameters:%n',
            optionListHeading = '%nOptions:%n',
            descriptionHeading = '%n')
    private static class HelpOptionsMixin {
        @Option(names = ['-h', '--help'], usageHelp = true, description = 'usage information') boolean isHelpRequested
        @Option(names = ['-v', '--version'], versionHelp = true, description = 'display the Groovy and JVM versions') boolean isVersionRequested
    }

    private static class VersionProvider implements CommandLine.IVersionProvider {
        String[] getVersion() {
            String version = GroovySystem.version
            ["Groovy Version: $version JVM: ${System.getProperty('java.version')}"]
        }
    }

    @Command(name = 'install', header = 'Installs a particular grape',
            description = 'Installs the specified groovy module or maven artifact. If a version is specified that specific version will be installed, otherwise the most recent version will be used (as if `*` was passed in).')
    private static class Install implements Runnable {
        @Parameters(index = '0', arity = '1', description = 'Which module group the module comes from. Translates directly to a Maven groupId or an Ivy Organization. Any group matching /groovy[x][\\..*]^/ is reserved and may have special meaning to the groovy endorsed modules.')
        String group

        @Parameters(index = '1', arity = '1', description = 'The name of the module to load. Translated directly to a Maven artifactId or an Ivy artifact.')
        String module

        @Parameters(index = '2', arity = '0..1', description = 'The version of the module to use. Either a literal version `1.1-RC3` or an Ivy Range `[2.2.1,)` meaning 2.2.1 or any greater version).')
        String version = '*'

        @Parameters(index = '3', arity = '0..1', description = 'The optional classifier to use (for example, jdk15).')
        String classifier

        @ParentCommand GrapeMain parentCommand

        void run() {
            parentCommand.init()

            // set the instance so we can re-set the logger
            Grape.instance
            parentCommand.setupLogging()

            parentCommand.resolvers.each { String url ->
                Grape.addResolver(name:url, root:url)
            }

            try {
                Grape.grab(autoDownload: true, group: group, module: module, version: version, classifier: classifier, noExceptions: true)
            } catch (Exception ex) {
                System.err.println "An error occured : $ex"
            }
        }
    }

    @Command(name = 'list', header = 'Lists all installed grapes',
            description = 'Lists locally installed modules (with their full maven name in the case of groovy modules) and versions.')
    private static class ListCommand implements Runnable {

        @ParentCommand GrapeMain parentCommand

        void run() {
            parentCommand.init()

            println ''

            int moduleCount = 0
            int versionCount = 0

            // set the instance so we can re-set the logger
            Grape.instance
            parentCommand.setupLogging()

            Grape.enumerateGrapes().each {String groupName, Map group ->
                group.each {String moduleName, List<String> versions ->
                    println "$groupName $moduleName  $versions"
                    moduleCount++
                    versionCount += versions.size()
                }
            }
            println ''
            println "$moduleCount Grape modules cached"
            println "$versionCount Grape module versions cached"
        }
    }

    @Command(name = 'resolve', header = 'Enumerates the jars used by a grape',
            customSynopsis = 'grape resolve [-adhisv] (<groupId> <artifactId> <version>)+',
            description = [
                    'Prints the file locations of the jars representing the artifcats for the specified module(s) and the respective transitive dependencies.',
                    '',
                    'Parameters:',
                    '      <group>     Which module group the module comes from. Translates directly',
                    '                    to a Maven groupId or an Ivy Organization. Any group',
                    '                    matching /groovy[x][\\..*]^/ is reserved and may have',
                    '                    special meaning to the groovy endorsed modules.',
                    '      <module>    The name of the module to load. Translated directly to a',
                    '                    Maven artifactId or an Ivy artifact.',
                    '      <version>   The version of the module to use. Either a literal version',
                    '                    `1.1-RC3` or an Ivy Range `[2.2.1,)` meaning 2.2.1 or any',
                    '                    greater version).'
    ])
    private static class Resolve implements Runnable {

        @Option(names = ['-a', '--ant'], description = 'Express dependencies in a format applicable for an ant script')
        private boolean ant

        @Option(names = ['-d', '--dos'], description = 'Express dependencies in a format applicable for a windows batch file')
        private boolean dos

        @Option(names = ['-s', '--shell'], description = 'Express dependencies in a format applicable for a unix shell script')
        private boolean shell

        @Option(names = ['-i', '--ivy'], description = 'Express dependencies in an ivy-like format')
        private boolean ivyFormatRequested

        @Parameters(hidden = true) // parameter description is embedded in the command description
        List<String> args = new ArrayList<>() // the positional parameters

        @ParentCommand GrapeMain parentCommand

        void run() {
            parentCommand.init()

            // set the instance so we can re-set the logger
            Grape.instance
            parentCommand.setupLogging(Message.MSG_ERR)

            if ((args.size() % 3) != 0) {
                println 'There needs to be a multiple of three arguments: (group module version)+'
                return
            }
            if (args.size() < 3) {
                println 'At least one Grape reference is required'
                return
            }
            def before, between, after

            if (ant) {
                before = '<pathelement location="'
                between = '">\n<pathelement location="'
                after = '">'
            } else if (dos) {
                before = 'set CLASSPATH='
                between = ';'
                after = ''
            } else if (shell) {
                before = 'export CLASSPATH='
                between = ':'
                after = ''
            } else if (ivyFormatRequested) {
                before = '<dependency '
                between = '">\n<dependency '
                after = '">'
            } else {
                before = ''
                between = '\n'
                after = '\n'
            }

            def iter = args.iterator()
            def params = [[:]]
            def depsInfo = [] // this list will contain the module/group/version info of all resolved dependencies
            if (ivyFormatRequested) {
                params << depsInfo
            }
            while (iter.hasNext()) {
                params.add([group: iter.next(), module: iter.next(), version: iter.next()])
            }
            try {
                def results = []
                def uris = Grape.resolve(* params)
                if (!ivyFormatRequested) {
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
                System.err.println "Error in resolve:\n\t$e.message"
                if (e.message =~ /unresolved dependency/) println 'Perhaps the grape is not installed?'
            }
        }
    }

    @Command(name = 'uninstall',
            description = 'Uninstalls a particular grape (non-transitively removes the respective jar file from the grape cache).')
    private static class Uninstall implements Runnable {
        @Parameters(index = '0', arity = '1', description = 'Which module group the module comes from. Translates directly to a Maven groupId or an Ivy Organization. Any group matching /groovy[x][\\..*]^/ is reserved and may have special meaning to the groovy endorsed modules.')
        String group

        @Parameters(index = '1', arity = '1', description = 'The name of the module to load. Translated directly to a Maven artifactId or an Ivy artifact.')
        String module

        @Parameters(index = '2', arity = '1', description = 'The version of the module to use. Either a literal version `1.1-RC3` or an Ivy Range `[2.2.1,)` meaning 2.2.1 or any greater version).')
        String version

        // TODO make version optional? support classifier?
        //@Parameters(index = '3', arity = '0..1', description = 'The optional classifier to use (for example, jdk15).')
        //String classifier;

        @ParentCommand GrapeMain parentCommand

        void run() {
            parentCommand.init()

            // set the instance so we can re-set the logger
            Grape.instance
            parentCommand.setupLogging()

            if (!Grape.enumerateGrapes().find {String groupName, Map g ->
                g.any {String moduleName, List<String> versions ->
                    group == groupName && module == moduleName && version in versions
                }
            }) {
                println "uninstall did not find grape matching: $group $module $version"
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
            Grape.instance.uninstallArtifact(group, module, version)
        }
    }
}
