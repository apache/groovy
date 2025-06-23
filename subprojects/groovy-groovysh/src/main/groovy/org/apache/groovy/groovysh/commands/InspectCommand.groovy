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
package org.apache.groovy.groovysh.commands

import groovy.console.ui.ObjectBrowser
import org.apache.groovy.groovysh.jline.GroovyEngine
import org.apache.groovy.groovysh.jline.ObjectInspector
import org.jline.builtins.Completers.OptDesc
import org.jline.builtins.Completers.OptionCompleter
import org.jline.console.CmdDesc
import org.jline.console.CommandInput
import org.jline.console.CommandMethods
import org.jline.console.Printer
import org.jline.reader.Completer
import org.jline.reader.impl.completer.ArgumentCompleter
import org.jline.reader.impl.completer.NullCompleter
import org.jline.reader.impl.completer.StringsCompleter
import org.jline.utils.AttributedString

class InspectCommand {
    private static final String DEFAULT_NANORC_VALUE = "classpath:/nanorc/gron.nanorc"
    private final GroovyEngine engine
    private final Printer printer
    private boolean consoleUi

    InspectCommand(GroovyEngine engine, Printer printer) {
        this.engine = engine
        this.printer = printer
        try {
            Class.forName("groovy.console.ui.ObjectBrowser")
            consoleUi = true
        } catch (Exception e) {
            // ignore
        }

        commandExecute.put(Command.INSPECT, new CommandMethods(this::inspect, this::inspectCompleter));
        commandExecute.put(Command.CONSOLE, new CommandMethods(this::console, this::defaultCompleter));
        commandExecute.put(Command.GRAB, new CommandMethods(this::grab, this::defaultCompleter));
        commandExecute.put(Command.CLASSLOADER, new CommandMethods(this::classLoader, this::classloaderCompleter));
        registerCommands(commandName, commandExecute);
        commandDescs.put(Command.INSPECT, inspectCmdDesc());
        commandDescs.put(Command.CONSOLE, consoleCmdDesc());
        commandDescs.put(Command.GRAB, grabCmdDesc());
        commandDescs.put(Command.CLASSLOADER, classLoaderCmdDesc());
    }

    Object inspect(CommandInput input) {
        if (input.xargs().length == 0) {
            return null;
        }
        if (input.args().length > 2) {
            throw new IllegalArgumentException("Wrong number of command parameters: " + input.args().length);
        }
        int idx = optionIdx(input.args())
        String option = idx < 0 ? "--info" : input.args()[idx]
        if (option.equals("-?") || option.equals("--help")) {
            printer.println(helpDesc(Command.INSPECT))
            return null
        }
        int id = 0;
        if (idx >= 0) {
            id = idx == 0 ? 1 : 0
        }
        if (input.args().length < id + 1) {
            throw new IllegalArgumentException("Wrong number of command parameters: " + input.args().length)
        }
        try {
            Object obj = input.xargs()[id]
            ObjectInspector inspector = new ObjectInspector(obj)
            Object out = null
            Map<String, Object> options = new HashMap<>()
            if (option.equals("-m") || option.equals("--methods")) {
                out = inspector.methods()
                options.put(Printer.COLUMNS, ObjectInspector.METHOD_COLUMNS)
            } else if (option.equals("-n") || option.equals("--metaMethods")) {
                out = inspector.metaMethods()
                options.put(Printer.COLUMNS, ObjectInspector.METHOD_COLUMNS)
            } else if (option.equals("-i") || option.equals("--info")) {
                out = inspector.properties()
                options.put(Printer.VALUE_STYLE, engine.groovyOption(GroovyEngine.NANORC_VALUE, DEFAULT_NANORC_VALUE))
            } else if (consoleUi && (option.equals("-g") || option.equals("--gui"))) {
                ObjectBrowser.inspect(obj)
            } else {
                throw new IllegalArgumentException("Unknown option: " + option)
            }
            options.put(Printer.SKIP_DEFAULT_OPTIONS, true)
            options.put(Printer.MAX_DEPTH, 1)
            options.put(Printer.INDENTION, 4)
            printer.println(options, out)
        } catch (Exception e) {
            saveException(e)
        }
        return null
    }

    private CmdDesc inspectCmdDesc() {
        def optDescs = [:]
        optDescs.put("-? --help", doDescription("Displays command help"))
        if (consoleUi) {
            optDescs.put("-g --gui", doDescription("Launch object browser"))
        }
        optDescs.put("-i --info", doDescription("Object class info"))
        optDescs.put("-m --methods", doDescription("List object methods"))
        optDescs.put("-n --metaMethods", doDescription("List object metaMethods"))
        CmdDesc out = new CmdDesc(new ArrayList<>(), optDescs)
        List<AttributedString> mainDesc = new ArrayList<>()
        List<String> info = new ArrayList<>()
        info.add("Display object info on terminal/in browser")
        commandInfos.put(Command.INSPECT, info)
        mainDesc.add(new AttributedString("inspect [OPTION] OBJECT"))
        out.setMainDesc(mainDesc)
        out.setHighlighted(false)
        return out
    }

    private List<AttributedString> doDescription(String description) {
        [new AttributedString(description)]
    }

    private int optionIdx(String[] args) {
        int out = 0
        for (String a : args) {
            if (a.startsWith("-")) {
                return out
            }
            out++
        }
        return -1
    }

    private List<String> variables() {
        engine.find(null).keySet().collect{'$' + it }
    }

    private List<OptDesc> compileOptDescs(String command) {
        List<OptDesc> out = new ArrayList<>()
//        Command cmd = Command.valueOf(command.toUpperCase());
        for (Map.Entry<String, List<AttributedString>> entry :
                commandDescs.get(cmd).getOptsDesc().entrySet()) {
            String[] option = entry.getKey().split("\\s+");
            String desc = entry.getValue().get(0).toString();
            if (option.length == 2) {
                out.add(new OptDesc(option[0], option[1], desc));
            } else if (option[0].charAt(1) == '-') {
                out.add(new OptDesc(null, option[0], desc));
            } else {
                out.add(new OptDesc(option[0], null, desc));
            }
        }
        return out;
    }

    private List<Completer> inspectCompleter(String command) {
        [new ArgumentCompleter(
            NullCompleter.INSTANCE,
            new OptionCompleter(
                Arrays.asList(new StringsCompleter(this::variables), NullCompleter.INSTANCE),
                this::compileOptDescs,
                1))]
    }

    private List<Completer> defaultCompleter(String command) {
        [new ArgumentCompleter(
                NullCompleter.INSTANCE, new OptionCompleter(NullCompleter.INSTANCE, this::compileOptDescs, 1))]
    }
}
