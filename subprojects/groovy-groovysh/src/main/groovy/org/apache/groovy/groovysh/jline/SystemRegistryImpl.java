/*
 * Copyright (c) 2002-2025, the original author(s).
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 *
 * https://opensource.org/licenses/BSD-3-Clause
 */
package org.apache.groovy.groovysh.jline;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jline.builtins.Completers.FilesCompleter;
import org.jline.builtins.Completers.OptDesc;
import org.jline.builtins.Completers.OptionCompleter;
import org.jline.builtins.ConfigurationPath;
import org.jline.builtins.Options;
import org.jline.builtins.Options.HelpException;
import org.jline.builtins.Styles;
import org.jline.console.*;
import org.jline.console.ConsoleEngine.ExecutionResult;
import org.jline.console.impl.Builtins;
import org.jline.console.impl.ConsoleEngineImpl;
import org.jline.console.impl.JlineCommandRegistry;
import org.jline.reader.*;
import org.jline.reader.Parser.ParseContext;
import org.jline.reader.impl.completer.AggregateCompleter;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.reader.impl.completer.SystemCompleter;
import org.jline.terminal.Terminal;
import org.jline.utils.*;

/**
 * Aggregate command registries.
 */
public class SystemRegistryImpl implements SystemRegistry {
    // NOTE: This file can be deleted if the following PRs are merged:
    // https://github.com/jline/jline3/pull/1392
    // https://github.com/jline/jline3/pull/1394

    public enum Pipe {
        FLIP,
        NAMED,
        AND,
        OR,
        REDIRECT,
        APPEND,
        PIPE
    }

    private static final Class<?>[] BUILTIN_REGISTRIES = {Builtins.class, ConsoleEngineImpl.class};
    private CommandRegistry[] commandRegistries;
    private Integer consoleId;
    protected final Parser parser;
    protected final ConfigurationPath configPath;
    protected final Supplier<Path> workDir;
    private final Map<String, CommandRegistry> subcommands = new HashMap<>();
    private final Map<Pipe, String> pipeName = new HashMap<>();
    private final Map<String, CommandMethods> commandExecute = new HashMap<>();
    private final Map<String, List<String>> commandInfos = new HashMap<>();
    private Exception exception;
    private final CommandOutputStream outputStream;
    private ScriptStore scriptStore = new ScriptStore();
    private NamesAndValues names = new NamesAndValues();
    private final SystemCompleter customSystemCompleter = new SystemCompleter();
    private final AggregateCompleter customAggregateCompleter = new AggregateCompleter(new ArrayList<>());
    private boolean commandGroups = true;
    private Function<CmdLine, CmdDesc> scriptDescription;

    @SuppressWarnings("this-escape")
    public SystemRegistryImpl(Parser parser, Terminal terminal, Supplier<Path> workDir, ConfigurationPath configPath) {
        this.parser = parser;
        this.workDir = workDir;
        this.configPath = configPath;
        outputStream = new CommandOutputStream(terminal);
        pipeName.put(Pipe.FLIP, "|;");
        pipeName.put(Pipe.NAMED, "|");
        pipeName.put(Pipe.AND, "&&");
        pipeName.put(Pipe.OR, "||");
        pipeName.put(Pipe.PIPE, "|!");
        pipeName.put(Pipe.REDIRECT, ">");
        pipeName.put(Pipe.APPEND, ">>");
        commandExecute.put("exit", new CommandMethods(this::exit, this::exitCompleter));
        commandExecute.put("help", new CommandMethods(this::help, this::helpCompleter));
    }

    public void rename(Pipe pipe, String name) {
        if (name.matches("/w+") || pipeName.containsValue(name)) {
            throw new IllegalArgumentException();
        }
        pipeName.put(pipe, name);
    }

    public void renameLocal(String command, String newName) {
        CommandMethods old = commandExecute.remove(command);
        if (old != null) {
            commandExecute.put(newName, old);
        }
    }

    @Override
    public Collection<String> getPipeNames() {
        return pipeName.values();
    }

    @Override
    public void setCommandRegistries(CommandRegistry... commandRegistries) {
        this.commandRegistries = commandRegistries;
        for (int i = 0; i < commandRegistries.length; i++) {
            if (commandRegistries[i] instanceof ConsoleEngine) {
                if (consoleId != null) {
                    throw new IllegalArgumentException();
                } else {
                    this.consoleId = i;
                    ((ConsoleEngine) commandRegistries[i]).setSystemRegistry(this);
                    this.scriptStore = new ScriptStore((ConsoleEngine) commandRegistries[i]);
                    this.names = new NamesAndValues(configPath);
                }
            } else if (commandRegistries[i] instanceof SystemRegistry) {
                throw new IllegalArgumentException();
            }
        }
        SystemRegistry.add(this);
    }

    @Override
    public void initialize(File script) {
        if (consoleId != null) {
            try {
                consoleEngine().execute(script);
            } catch (Exception e) {
                trace(e);
            }
        }
    }

    @Override
    public Set<String> commandNames() {
        Set<String> out = new HashSet<>();
        for (CommandRegistry r : commandRegistries) {
            out.addAll(r.commandNames());
        }
        out.addAll(localCommandNames());
        return out;
    }

    private Set<String> localCommandNames() {
        return commandExecute.keySet();
    }

    @Override
    public Map<String, String> commandAliases() {
        Map<String, String> out = new HashMap<>();
        for (CommandRegistry r : commandRegistries) {
            out.putAll(r.commandAliases());
        }
        return out;
    }

    @Override
    public Object consoleOption(String name) {
        return consoleOption(name, null);
    }

    @Override
    public <T> T consoleOption(String name, T defVal) {
        T out = defVal;
        if (consoleId != null) {
            out = consoleEngine().consoleOption(name, defVal);
        }
        return out;
    }

    @Override
    public void setConsoleOption(String name, Object value) {
        if (consoleId != null) {
            consoleEngine().setConsoleOption(name, value);
        }
    }

    /**
     * Register subcommand registry
     * @param command main command
     * @param subcommandRegistry subcommand registry
     */
    @Override
    public void register(String command, CommandRegistry subcommandRegistry) {
        subcommands.put(command, subcommandRegistry);
        commandExecute.put(command, new CommandMethods(this::subcommand, this::emptyCompleter));
    }

    private List<String> localCommandInfo(String command) {
        try {
            CommandRegistry subCommand = subcommands.get(command);
            if (subCommand != null) {
                registryHelp(subCommand);
            } else {
                localExecute(command, new String[] {"--help"});
            }
        } catch (HelpException e) {
            exception = null;
            return JlineCommandRegistry.compileCommandInfo(e.getMessage());
        } catch (Exception e) {
            trace(e);
        }
        return new ArrayList<>();
    }

    @Override
    public List<String> commandInfo(String command) {
        int id = registryId(command);
        List<String> out = new ArrayList<>();
        if (id > -1) {
            if (!commandInfos.containsKey(command)) {
                commandInfos.put(command, commandRegistries[id].commandInfo(command));
            }
            out = commandInfos.get(command);
        } else if (scriptStore.hasScript(command) && consoleEngine() != null) {
            out = consoleEngine().commandInfo(command);
        } else if (isLocalCommand(command)) {
            out = localCommandInfo(command);
        }
        return out;
    }

    @Override
    public boolean hasCommand(String command) {
        return registryId(command) > -1 || isLocalCommand(command);
    }

    public void setGroupCommandsInHelp(boolean commandGroups) {
        this.commandGroups = commandGroups;
    }

    public SystemRegistryImpl groupCommandsInHelp(boolean commandGroups) {
        this.commandGroups = commandGroups;
        return this;
    }

    private boolean isLocalCommand(String command) {
        return commandExecute.containsKey(command);
    }

    @Override
    public boolean isCommandOrScript(ParsedLine line) {
        return isCommandOrScript(parser.getCommand(line.words().get(0)));
    }

    @Override
    public boolean isCommandOrScript(String command) {
        if (hasCommand(command)) {
            return true;
        }
        return scriptStore.hasScript(command);
    }

    public void addCompleter(Completer completer) {
        if (completer instanceof SystemCompleter sc) {
            if (sc.isCompiled()) {
                customAggregateCompleter.getCompleters().add(sc);
            } else {
                customSystemCompleter.add(sc);
            }
        } else {
            customAggregateCompleter.getCompleters().add(completer);
        }
    }

    @Override
    public SystemCompleter compileCompleters() {
        throw new IllegalStateException("Use method completer() to retrieve Completer!");
    }

    private SystemCompleter _compileCompleters() {
        SystemCompleter out = CommandRegistry.aggregateCompleters(commandRegistries);
        SystemCompleter local = new SystemCompleter();
        for (String command : commandExecute.keySet()) {
            CommandRegistry subCommand = subcommands.get(command);
            if (subCommand != null) {
                for (Entry<String, List<Completer>> entry :
                        subCommand.compileCompleters().getCompleters().entrySet()) {
                    for (Completer cc : entry.getValue()) {
                        if (!(cc instanceof ArgumentCompleter)) {
                            throw new IllegalArgumentException();
                        }
                        List<Completer> cmps = ((ArgumentCompleter) cc).getCompleters();
                        cmps.add(0, NullCompleter.INSTANCE);
                        cmps.set(1, new StringsCompleter(entry.getKey()));
                        Completer last = cmps.get(cmps.size() - 1);
                        if (last instanceof OptionCompleter) {
                            ((OptionCompleter) last).setStartPos(cmps.size() - 1);
                            cmps.set(cmps.size() - 1, last);
                        }
                        local.add(command, new ArgumentCompleter(cmps));
                    }
                }
            } else {
                local.add(
                        command, commandExecute.get(command).compileCompleter().apply(command));
            }
        }
        local.add(customSystemCompleter);
        out.add(local);
        out.compile(s -> CommandRegistry.createCandidate(commandRegistries, s));
        return out;
    }

    @Override
    public Completer completer() {
        List<Completer> completers = new ArrayList<>();
        completers.add(_compileCompleters());
        completers.add(customAggregateCompleter);
        if (consoleId != null) {
            completers.addAll(consoleEngine().scriptCompleters());
            completers.add(new PipelineCompleter(workDir, pipeName, names).doCompleter());
        }
        return new AggregateCompleter(completers);
    }

    private CmdDesc localCommandDescription(String command) {
        if (!isLocalCommand(command)) {
            throw new IllegalArgumentException();
        }
        try {
            localExecute(command, new String[] {"--help"});
        } catch (HelpException e) {
            exception = null;
            return JlineCommandRegistry.compileCommandDescription(e.getMessage());
        } catch (Exception e) {
            trace(e);
        }
        return null;
    }

    @Override
    public CmdDesc commandDescription(List<String> args) {
        CmdDesc out = new CmdDesc(false);
        String command = args.get(0);
        int id = registryId(command);
        if (id > -1) {
            out = commandRegistries[id].commandDescription(args);
        } else if (scriptStore.hasScript(command) && consoleEngine() != null) {
            out = consoleEngine().commandDescription(args);
        } else if (isLocalCommand(command)) {
            out = localCommandDescription(command);
        }
        return out;
    }

    private CmdDesc commandDescription(CommandRegistry subreg) {
        List<AttributedString> main = new ArrayList<>();
        Map<String, List<AttributedString>> options = new HashMap<>();
        StyleResolver helpStyle = Styles.helpStyle();
        for (String sc : new TreeSet<>(subreg.commandNames())) {
            for (String info : subreg.commandInfo(sc)) {
                main.add(HelpException.highlightSyntax(sc + " -  " + info, helpStyle, true));
                break;
            }
        }
        return new CmdDesc(main, ArgDesc.doArgNames(Collections.singletonList("")), options);
    }

    public void setScriptDescription(Function<CmdLine, CmdDesc> scriptDescription) {
        this.scriptDescription = scriptDescription;
    }

    @Override
    public CmdDesc commandDescription(CmdLine line) {
        CmdDesc out = null;
        String cmd = parser.getCommand(line.getArgs().get(0));
        switch (line.getDescriptionType()) {
            case COMMAND:
                if (isCommandOrScript(cmd) && !names.hasPipes(line.getArgs())) {
                    List<String> args = line.getArgs();
                    CommandRegistry subCommand = subcommands.get(cmd);
                    if (subCommand != null) {
                        String c = args.size() > 1 ? args.get(1) : null;
                        if (c == null || subCommand.hasCommand(c)) {
                            if (c != null && c.equals("help")) {
                                out = null;
                            } else if (c != null) {
                                out = subCommand.commandDescription(Collections.singletonList(c));
                            } else {
                                out = commandDescription(subCommand);
                            }
                        } else {
                            out = commandDescription(subCommand);
                        }
                        if (out != null) {
                            out.setSubcommand(true);
                        }
                    } else {
                        args.set(0, cmd);
                        out = commandDescription(args);
                    }
                }
                break;
            case METHOD:
            case SYNTAX:
                if (!isCommandOrScript(cmd) && scriptDescription != null) {
                    out = scriptDescription.apply(line);
                }
                break;
        }
        return out;
    }

    @Override
    public Object invoke(String command, Object... args) throws Exception {
        Object out = null;
        command = ConsoleEngine.plainCommand(command);
        args = args == null ? new Object[] {null} : args;
        int id = registryId(command);
        if (id > -1) {
            out = commandRegistries[id].invoke(commandSession(), command, args);
        } else if (isLocalCommand(command)) {
            out = localExecute(command, args);
        } else if (consoleId != null) {
            out = consoleEngine().invoke(commandSession(), command, args);
        }
        return out;
    }

    private Object localExecute(String command, Object[] args) throws Exception {
        if (!isLocalCommand(command)) {
            throw new IllegalArgumentException();
        }
        Object out = commandExecute.get(command).execute().apply(new CommandInput(command, args, commandSession()));
        if (exception != null) {
            throw exception;
        }
        return out;
    }

    public Terminal terminal() {
        return commandSession().terminal();
    }

    private CommandSession commandSession() {
        return outputStream.getCommandSession();
    }

    private static class CommandOutputStream {
        private final PrintStream origOut;
        private final PrintStream origErr;
        private final Terminal origTerminal;
        private OutputStream outputStream;
        private Terminal terminal;
        private String output;
        private CommandSession commandSession;
        private boolean redirecting = false;

        public CommandOutputStream(Terminal terminal) {
            this.origOut = System.out;
            this.origErr = System.err;
            this.origTerminal = terminal;
            this.terminal = terminal;
            PrintStream ps = new PrintStream(terminal.output());
            this.commandSession = new CommandSession(terminal, terminal.input(), ps, ps);
        }

        public void redirect() {
            outputStream = new ByteArrayOutputStream();
        }

        public void redirect(File file, boolean append) throws IOException {
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    (new File(file.getParent())).mkdirs();
                    file.createNewFile();
                }
            }
            outputStream = new FileOutputStream(file, append);
        }

        public void open(boolean redirectColor) throws IOException {
            if (redirecting || outputStream == null) {
                return;
            }
            output = null;
            PrintStream out = new PrintStream(outputStream);
            System.setOut(out);
            System.setErr(out);

            // Use simple streams instead of creating a PTY terminal to avoid hangs on macOS
            // Create a command session that uses the original terminal for input but redirected streams for output
            this.commandSession = new CommandSession(origTerminal, origTerminal.input(), out, out);
            redirecting = true;
        }

        public void close() {
            if (!redirecting) {
                return;
            }
            try {
                // Flush the original terminal since we're using it for input
                origTerminal.flush();
                if (outputStream instanceof ByteArrayOutputStream) {
                    output = outputStream.toString();
                }
                // No need to close a separate terminal since we're reusing the original one
            } catch (Exception e) {
                // ignore
            }
            reset();
        }

        public void resetOutput() {
            output = null;
        }

        private void reset() {
            outputStream = null;
            System.setOut(origOut);
            System.setErr(origErr);
            terminal = origTerminal;
            PrintStream ps = new PrintStream(terminal.output());
            this.commandSession = new CommandSession(terminal, terminal.input(), ps, ps);
            redirecting = false;
        }

        public CommandSession getCommandSession() {
            return commandSession;
        }

        public String getOutput() {
            return output;
        }

        public boolean isRedirecting() {
            return redirecting;
        }

        public boolean isByteOutputStream() {
            return outputStream instanceof ByteArrayOutputStream;
        }
    }

    @Override
    public boolean isCommandAlias(String command) {
        if (consoleEngine() == null) {
            return false;
        }
        ConsoleEngine consoleEngine = consoleEngine();
        if (!parser.validCommandName(command) || !consoleEngine.hasAlias(command)) {
            return false;
        }
        String value = consoleEngine.getAlias(command).split("\\s+")[0];
        return !names.isPipe(value);
    }

    private String replaceCommandAlias(String variable, String command, String rawLine) {
        ConsoleEngine consoleEngine = consoleEngine();
        assert consoleEngine != null;
        return variable == null
                ? rawLine.replaceFirst(command + "(\\b|$)", consoleEngine.getAlias(command))
                : rawLine.replaceFirst("=" + command + "(\\b|$)", "=" + consoleEngine.getAlias(command));
    }

    private String replacePipeAlias(
            ArgsParser ap, String pipeAlias, List<String> args, Map<String, List<String>> customPipes) {
        ConsoleEngine consoleEngine = consoleEngine();
        assert consoleEngine != null;
        String alias = pipeAlias;
        for (int j = 0; j < args.size(); j++) {
            alias = alias.replaceAll("\\s\\$" + j + "\\b", " " + args.get(j));
            alias = alias.replaceAll("\\$\\{" + j + "(|:-.*)}", args.get(j));
        }
        alias = alias.replaceAll("\\$\\{@}", consoleEngine.expandToList(args));
        alias = alias.replaceAll("\\$@", consoleEngine.expandToList(args));
        alias = alias.replaceAll("\\s+\\$\\d\\b", "");
        alias = alias.replaceAll("\\s+\\$\\{\\d+}", "");
        alias = alias.replaceAll("\\$\\{\\d+}", "");
        Matcher matcher = Pattern.compile("\\$\\{\\d+:-(.*?)}").matcher(alias);
        if (matcher.find()) {
            alias = matcher.replaceAll("$1");
        }
        ap.parse(alias);
        List<String> ws = ap.args();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ws.size(); i++) {
            if (ws.get(i).equals(pipeName.get(Pipe.NAMED))) {
                if (i + 1 < ws.size() && consoleEngine.hasAlias(ws.get(i + 1))) {
                    args.clear();
                    String innerPipe = consoleEngine.getAlias(ws.get(++i));
                    while (i < ws.size() - 1 && !names.isPipe(ws.get(i + 1), customPipes.keySet())) {
                        args.add(ws.get(++i));
                    }
                    sb.append(replacePipeAlias(ap, innerPipe, args, customPipes));
                } else {
                    sb.append(ws.get(i)).append(' ');
                }
            } else {
                sb.append(ws.get(i)).append(' ');
            }
        }
        return sb.toString();
    }

    private void replacePipeAliases(ConsoleEngine consoleEngine, Map<String, List<String>> customPipes, ArgsParser ap) {
        List<String> words = ap.args();
        if (consoleEngine != null && words.contains(pipeName.get(Pipe.NAMED))) {
            StringBuilder sb = new StringBuilder();
            boolean trace = false;
            for (int i = 0; i < words.size(); i++) {
                if (words.get(i).equals(pipeName.get(Pipe.NAMED))) {
                    if (i + 1 < words.size() && consoleEngine.hasAlias(words.get(i + 1))) {
                        trace = true;
                        List<String> args = new ArrayList<>();
                        String pipeAlias = consoleEngine.getAlias(words.get(++i));
                        while (i < words.size() - 1 && !names.isPipe(words.get(i + 1), customPipes.keySet())) {
                            args.add(words.get(++i));
                        }
                        sb.append(replacePipeAlias(ap, pipeAlias, args, customPipes));
                    } else {
                        sb.append(words.get(i)).append(' ');
                    }
                } else {
                    sb.append(words.get(i)).append(' ');
                }
            }
            ap.parse(sb.toString());
            if (trace) {
                consoleEngine.trace(ap.line());
            }
        }
    }

    private List<CommandData> compileCommandLine(String commandLine) {
        List<CommandData> out = new ArrayList<>();
        ArgsParser ap = new ArgsParser(parser);
        ap.parse(commandLine);
        ConsoleEngine consoleEngine = consoleEngine();
        Map<String, List<String>> customPipes = consoleEngine != null ? consoleEngine.getPipes() : new HashMap<>();
        replacePipeAliases(consoleEngine, customPipes, ap);
        List<String> words = ap.args();
        String nextRawLine = ap.line();
        int first = 0;
        int last;
        List<String> pipes = new ArrayList<>();
        String pipeSource = null;
        StringBuilder rawLine = null;
        String pipeResult = null;
        if (isCommandAlias(ap.command())) {
            ap.parse(replaceCommandAlias(ap.variable(), ap.command(), nextRawLine));
            replacePipeAliases(consoleEngine, customPipes, ap);
            nextRawLine = ap.line();
            words = ap.args();
        }
        if (!names.hasPipes(words)) {
            out.add(new CommandData(ap, false, nextRawLine, ap.variable(), null, false, ""));
        } else {
            //
            // compile pipe line
            //
            do {
                String rawCommand = parser.getCommand(words.get(first));
                String command = ConsoleEngine.plainCommand(rawCommand);
                String variable = parser.getVariable(words.get(first));
                if (isCommandAlias(command)) {
                    ap.parse(replaceCommandAlias(variable, command, nextRawLine));
                    replacePipeAliases(consoleEngine, customPipes, ap);
                    rawCommand = ap.rawCommand();
                    command = ap.command();
                    words = ap.args();
                    first = 0;
                }
                if (scriptStore.isConsoleScript(command) && !rawCommand.startsWith(":")) {
                    throw new IllegalArgumentException("Commands must be used in pipes with colon prefix!");
                }
                last = words.size();
                File file = null;
                boolean append = false;
                boolean pipeStart = false;
                boolean skipPipe = false;
                List<String> _words = new ArrayList<>();
                //
                // find next pipe
                //
                for (int i = first; i < last; i++) {
                    if (words.get(i).equals(pipeName.get(Pipe.REDIRECT))
                            || words.get(i).equals(pipeName.get(Pipe.APPEND))) {
                        pipes.add(words.get(i));
                        append = words.get(i).equals(pipeName.get(Pipe.APPEND));
                        if (i + 1 >= last) {
                            throw new IllegalArgumentException();
                        }
                        file = redirectFile(words.get(i + 1));
                        last = i + 1;
                        break;
                    } else if (consoleId == null) {
                        _words.add(words.get(i));
                    } else if (words.get(i).equals(pipeName.get(Pipe.FLIP))) {
                        if (variable != null || file != null || pipeResult != null || consoleId == null) {
                            throw new IllegalArgumentException();
                        }
                        pipes.add(words.get(i));
                        last = i;
                        variable = "_pipe" + (pipes.size() - 1);
                        break;
                    } else if (words.get(i).equals(pipeName.get(Pipe.NAMED))
                            || (words.get(i).matches("^.*[^a-zA-Z0-9 ].*$") && customPipes.containsKey(words.get(i)))) {
                        String pipe = words.get(i);
                        if (pipe.equals(pipeName.get(Pipe.NAMED))) {
                            if (i + 1 >= last) {
                                throw new IllegalArgumentException("Pipe is NULL!");
                            }
                            pipe = words.get(i + 1);
                            if (!pipe.matches("\\w+") || !customPipes.containsKey(pipe)) {
                                if (!consoleOption("ignoreUnknownPipes", false)) {
                                    throw new IllegalArgumentException("Unknown or illegal pipe name: " + pipe);
                                }
                            }
                        }
                        pipes.add(pipe);
                        last = i;
                        if (pipeSource == null) {
                            pipeSource = "_pipe" + (pipes.size() - 1);
                            pipeResult = variable;
                            variable = pipeSource;
                            pipeStart = true;
                        }
                        break;
                    } else if (words.get(i).equals(pipeName.get(Pipe.OR))
                            || words.get(i).equals(pipeName.get(Pipe.AND))|| words.get(i).equals(pipeName.get(Pipe.PIPE))) {
                        if (variable != null || pipeSource != null) {
                            pipes.add(words.get(i));
                        } else if (pipes.size() > 0
                                && (pipes.get(pipes.size() - 1).equals(pipeName.get(Pipe.REDIRECT))
                                        || pipes.get(pipes.size() - 1).equals(pipeName.get(Pipe.APPEND)))) {
                            pipes.remove(pipes.size() - 1);
                            out.get(out.size() - 1).setPipe(words.get(i));
                            skipPipe = true;
                        } else {
                            pipes.add(words.get(i));
                            pipeSource = "_pipe" + (pipes.size() - 1);
                            pipeResult = variable;
                            variable = pipeSource;
                            pipeStart = true;
                        }
                        last = i;
                        break;
                    } else {
                        _words.add(words.get(i));
                    }
                }
                if (last == words.size()) {
                    pipes.add("END_PIPE");
                } else if (skipPipe) {
                    first = last + 1;
                    continue;
                }
                //
                // compose pipe command
                //
                String subLine = last < words.size() || first > 0 ? String.join(" ", _words) : ap.line();
                if (last + 1 < words.size()) {
                    nextRawLine = String.join(" ", words.subList(last + 1, words.size()));
                }
                boolean done = true;
                boolean statement = false;
                List<String> arglist = new ArrayList<>();
                if (!_words.isEmpty()) {
                    arglist.addAll(_words.subList(1, _words.size()));
                }
                if (rawLine != null || (pipes.size() > 1 && customPipes.containsKey(pipes.get(pipes.size() - 2)))) {
                    done = false;
                    if (rawLine == null) {
                        rawLine = new StringBuilder(pipeSource);
                    }
                    if (customPipes.containsKey(pipes.get(pipes.size() - 2))) {
                        List<String> fixes = customPipes.get(pipes.get(pipes.size() - 2));
                        if (pipes.get(pipes.size() - 2).matches("\\w+")) {
                            int idx = subLine.indexOf(" ");
                            subLine = idx > 0 ? subLine.substring(idx + 1) : "";
                        }
                        rawLine.append(fixes.get(0)).append(consoleId != null ? consoleEngine().expandCommandLine(subLine) : subLine).append(fixes.get(1));
                        statement = true;
                    }
                    if (pipes.get(pipes.size() - 1).equals(pipeName.get(Pipe.FLIP))
                            || pipes.get(pipes.size() - 1).equals(pipeName.get(Pipe.AND))
                            || pipes.get(pipes.size() - 1).equals(pipeName.get(Pipe.OR))) {
                        done = true;
                        pipeSource = null;
                        if (variable != null) {
                            rawLine.insert(0, variable + " = ");
                        }
                    }
                    if (last + 1 >= words.size() || file != null) {
                        done = true;
                        pipeSource = null;
                        if (pipeResult != null) {
                            rawLine.insert(0, pipeResult + " = ");
                        }
                    }
                } else if (pipes.get(pipes.size() - 1).equals(pipeName.get(Pipe.FLIP)) || pipeStart) {
                    if (pipeStart && pipeResult != null) {
                        subLine = subLine.substring(subLine.indexOf("=") + 1);
                    }
                    rawLine = new StringBuilder(flipArgument(command, subLine, pipes, arglist));
                    rawLine.insert(0, variable + "=");
                } else {
                    rawLine = new StringBuilder(flipArgument(command, subLine, pipes, arglist));
                }
                if (done) {
                    //
                    // add composed command to return list
                    //
                    out.add(new CommandData(
                            ap, statement, rawLine.toString(), variable, file, append, pipes.get(pipes.size() - 1)));
                    if (pipes.get(pipes.size() - 1).equals(pipeName.get(Pipe.AND))
                            || pipes.get(pipes.size() - 1).equals(pipeName.get(Pipe.OR))) {
                        pipeSource = null;
                        pipeResult = null;
                    }
                    rawLine = null;
                }
                first = last + 1;
            } while (first < words.size());
        }
        return out;
    }

    private File redirectFile(String name) {
        File out;
        if (name.equals("null")) {
            out = OSUtils.IS_WINDOWS ? new File("NUL") : new File("/dev/null");
        } else {
            out = new File(name);
        }
        return out;
    }

    private static class ArgsParser {
        private int round = 0;
        private int curly = 0;
        private int square = 0;
        private boolean quoted;
        private boolean doubleQuoted;
        private String line;
        private String command = "";
        private String variable = "";
        private List<String> args;
        private final Parser parser;

        public ArgsParser(Parser parser) {
            this.parser = parser;
        }

        private void reset() {
            round = 0;
            curly = 0;
            square = 0;
            quoted = false;
            doubleQuoted = false;
        }

        private void next(String arg) {
            char prevChar = ' ';
            for (int i = 0; i < arg.length(); i++) {
                char c = arg.charAt(i);
                if (!parser.isEscapeChar(prevChar)) {
                    if (!quoted && !doubleQuoted) {
                        if (c == '(') {
                            round++;
                        } else if (c == ')') {
                            round--;
                        } else if (c == '{') {
                            curly++;
                        } else if (c == '}') {
                            curly--;
                        } else if (c == '[') {
                            square++;
                        } else if (c == ']') {
                            square--;
                        } else if (c == '"') {
                            doubleQuoted = true;
                        } else if (c == '\'') {
                            quoted = true;
                        }
                    } else if (quoted && c == '\'') {
                        quoted = false;
                    } else if (doubleQuoted && c == '"') {
                        doubleQuoted = false;
                    }
                }
                prevChar = c;
            }
        }

        private boolean isEnclosed() {
            return round == 0 && curly == 0 && square == 0 && !quoted && !doubleQuoted;
        }

        public boolean isEnclosed(String arg) {
            reset();
            next(arg);
            return isEnclosed();
        }

        private void enclosedArgs(List<String> words) {
            args = new ArrayList<>();
            reset();
            boolean first = true;
            StringBuilder sb = new StringBuilder();
            for (String a : words) {
                next(a);
                if (!first) {
                    sb.append(" ");
                }
                if (isEnclosed()) {
                    sb.append(a);
                    args.add(sb.toString());
                    sb = new StringBuilder();
                    first = true;
                } else {
                    sb.append(a);
                    first = false;
                }
            }
            if (!first) {
                args.add(sb.toString());
            }
        }

        public void parse(String line) {
            this.line = line;
            ParsedLine pl = parser.parse(line, 0, ParseContext.SPLIT_LINE);
            enclosedArgs(pl.words());
            if (!args.isEmpty()) {
                this.command = parser.getCommand(args.get(0));
                if (!parser.validCommandName(command)) {
                    this.command = "";
                }
                this.variable = parser.getVariable(args.get(0));
            } else {
                this.line = "";
            }
        }

        public String line() {
            return line;
        }

        public String command() {
            return ConsoleEngine.plainCommand(command);
        }

        public String rawCommand() {
            return command;
        }

        public String variable() {
            return variable;
        }

        public List<String> args() {
            return args;
        }

        private int closingQuote(String arg) {
            int out = -1;
            char prevChar = ' ';
            for (int i = 1; i < arg.length(); i++) {
                char c = arg.charAt(i);
                if (!parser.isEscapeChar(prevChar)) {
                    if (c == arg.charAt(0)) {
                        out = i;
                        break;
                    }
                }
                prevChar = c;
            }
            return out;
        }

        private String unquote(String arg) {
            if (arg.length() > 1 && (arg.startsWith("\"") && arg.endsWith("\""))
                    || (arg.startsWith("'") && arg.endsWith("'"))) {
                if (closingQuote(arg) == arg.length() - 1) {
                    return arg.substring(1, arg.length() - 1);
                }
            }
            return arg;
        }

        /**
         * Unescapes a string that contains standard Java escape sequences.
         * <ul>
         * <li><strong>&#92;b &#92;f &#92;n &#92;r &#92;t &#92;" &#92;'</strong> :
         * BS, FF, NL, CR, TAB, double and single quote.</li>
         * <li><strong>&#92;X &#92;XX &#92;XXX</strong> : Octal character
         * specification (0 - 377, 0x00 - 0xFF).</li>
         * <li><strong>&#92;uXXXX</strong> : Hexadecimal based Unicode character.</li>
         * </ul>
         *
         * @param arg
         *            A string optionally containing standard java escape sequences.
         * @return The translated string.
         *
         * Based on code by Udo Klimaschewski, https://gist.github.com/uklimaschewski/6741769
         */
        private String unescape(String arg) {
            if (arg == null || !parser.isEscapeChar('\\')) {
                return arg;
            }
            StringBuilder sb = new StringBuilder(arg.length());
            for (int i = 0; i < arg.length(); i++) {
                char ch = arg.charAt(i);
                if (ch == '\\') {
                    char nextChar = (i == arg.length() - 1) ? '\\' : arg.charAt(i + 1);
                    // Octal escape?
                    if (nextChar >= '0' && nextChar <= '7') {
                        String code = "" + nextChar;
                        i++;
                        if ((i < arg.length() - 1) && arg.charAt(i + 1) >= '0' && arg.charAt(i + 1) <= '7') {
                            code += arg.charAt(i + 1);
                            i++;
                            if ((i < arg.length() - 1) && arg.charAt(i + 1) >= '0' && arg.charAt(i + 1) <= '7') {
                                code += arg.charAt(i + 1);
                                i++;
                            }
                        }
                        sb.append((char) Integer.parseInt(code, 8));
                        continue;
                    }
                    switch (nextChar) {
                        case '\\':
                            ch = '\\';
                            break;
                        case 'b':
                            ch = '\b';
                            break;
                        case 'f':
                            ch = '\f';
                            break;
                        case 'n':
                            ch = '\n';
                            break;
                        case 'r':
                            ch = '\r';
                            break;
                        case 't':
                            ch = '\t';
                            break;
                        case '\"':
                            ch = '\"';
                            break;
                        case '\'':
                            ch = '\'';
                            break;
                        case ' ':
                            ch = ' ';
                            break;
                        // Hex Unicode: u????
                        case 'u':
                            if (i >= arg.length() - 5) {
                                ch = 'u';
                                break;
                            }
                            int code = Integer.parseInt(
                                    "" + arg.charAt(i + 2) + arg.charAt(i + 3) + arg.charAt(i + 4) + arg.charAt(i + 5),
                                    16);
                            sb.append(Character.toChars(code));
                            i += 5;
                            continue;
                    }
                    i++;
                }
                sb.append(ch);
            }
            return sb.toString();
        }
    }

    private String flipArgument(
            final String command, final String subLine, final List<String> pipes, List<String> arglist) {
        String out;
        if (pipes.size() > 1 && pipes.get(pipes.size() - 2).equals(pipeName.get(Pipe.FLIP))) {
            String s = isCommandOrScript(command) ? "$" : "";
            out = subLine + " " + s + "_pipe" + (pipes.size() - 2);
            if (!command.isEmpty()) {
                arglist.add(s + "_pipe" + (pipes.size() - 2));
            }
        } else {
            out = subLine;
        }
        return out;
    }

    protected static class CommandData {
        private final String rawLine;
        private String command;
        private String[] args;
        private final File file;
        private final boolean append;
        private final String variable;
        private String pipe;

        public CommandData(
                ArgsParser parser,
                boolean statement,
                String rawLine,
                String variable,
                File file,
                boolean append,
                String pipe) {
            this.rawLine = rawLine;
            this.variable = variable;
            this.file = file;
            this.append = append;
            this.pipe = pipe;
            this.args = new String[] {};
            this.command = "";
            if (!statement) {
                parser.parse(rawLine);
                this.command = parser.command();
                if (parser.args().size() > 1) {
                    this.args = new String[parser.args().size() - 1];
                    for (int i = 1; i < parser.args().size(); i++) {
                        args[i - 1] =
                                parser.unescape(parser.unquote(parser.args().get(i)));
                    }
                }
            }
        }

        public void setPipe(String pipe) {
            this.pipe = pipe;
        }

        public File file() {
            return file;
        }

        public boolean append() {
            return append;
        }

        public String variable() {
            return variable;
        }

        public String command() {
            return command;
        }

        public String[] args() {
            return args;
        }

        public String rawLine() {
            return rawLine;
        }

        public String pipe() {
            return pipe;
        }

        @Override
        public String toString() {
            return "[" + "rawLine:"
                    + rawLine + ", " + "command:"
                    + command + ", " + "args:"
                    + Arrays.asList(args) + ", " + "variable:"
                    + variable + ", " + "file:"
                    + file + ", " + "append:"
                    + append + ", " + "pipe:"
                    + pipe + "]";
        }
    }

    private static class ScriptStore {
        ConsoleEngine engine;
        Map<String, Boolean> scripts = new HashMap<>();

        public ScriptStore() {}

        public ScriptStore(ConsoleEngine engine) {
            this.engine = engine;
        }

        public void refresh() {
            if (engine != null) {
                scripts = engine.scripts();
            }
        }

        public boolean hasScript(String name) {
            return scripts.containsKey(name);
        }

        public boolean isConsoleScript(String name) {
            return scripts.getOrDefault(name, false);
        }

        public Set<String> getScripts() {
            return scripts.keySet();
        }
    }

    @SuppressWarnings("serial")
    public static class UnknownCommandException extends Exception {
        public UnknownCommandException(String message) {
            super(message);
        }
    }

    private Object execute(String command, String rawLine, String[] args) throws Exception {
        if (!parser.validCommandName(command)) {
            throw new UnknownCommandException("Invalid command: " + rawLine);
        }
        Object out;
        int id = registryId(command);
        if (id > -1) {
            Object[] _args = consoleId != null ? consoleEngine().expandParameters(args) : args;
            out = commandRegistries[id].invoke(outputStream.getCommandSession(), command, _args);
        } else if (scriptStore.hasScript(command) && consoleEngine() != null) {
            out = consoleEngine().execute(command, rawLine, args);
        } else if (isLocalCommand(command)) {
            out = localExecute(command, consoleId != null ? consoleEngine().expandParameters(args) : args);
        } else {
            throw new UnknownCommandException("Unknown command: " + command);
        }
        return out;
    }

    @Override
    public Object execute(String line) throws Exception {
        if (line.trim().isEmpty() || line.trim().startsWith("#")) {
            return null;
        }
        long start = new Date().getTime();
        Object out = null;
        boolean statement = false;
        boolean postProcessed = false;
        int errorCount = 0;
        scriptStore.refresh();
        List<CommandData> cmds = compileCommandLine(line);
        ConsoleEngine consoleEngine = consoleEngine();
        for (CommandData cmd : cmds) {
            if (cmd.file() != null && scriptStore.isConsoleScript(cmd.command())) {
                throw new IllegalArgumentException("Console script output cannot be redirected!");
            }
            try {
                outputStream.close();
                if (consoleEngine != null && !consoleEngine.isExecuting()) {
                    trace(cmd);
                }
                exception = null;
                statement = false;
                postProcessed = false;
                if (cmd.variable() != null || cmd.file() != null) {
                    if (cmd.file() != null) {
                        outputStream.redirect(cmd.file(), cmd.append());
                    } else if (consoleId != null) {
                        outputStream.redirect();
                    }
                    outputStream.open(consoleOption("redirectColor", false));
                }
                boolean consoleScript = false;
                try {
                    out = execute(cmd.command(), cmd.rawLine(), cmd.args());
                } catch (UnknownCommandException e) {
                    if (consoleEngine == null) {
                        throw e;
                    }
                    consoleScript = true;
                }
                if (consoleEngine != null) {
                    if (consoleScript) {
                        statement = cmd.command().isEmpty() || !scriptStore.hasScript(cmd.command());
                        if (statement && outputStream.isByteOutputStream()) {
                            outputStream.close();
                        }
                        out = consoleEngine.execute(cmd.command(), cmd.rawLine(), cmd.args());
                    }
                    if (cmd.pipe().equals(pipeName.get(Pipe.OR)) || cmd.pipe().equals(pipeName.get(Pipe.AND)) || cmd.pipe().equals(pipeName.get(Pipe.PIPE))) {
                        if (cmd.pipe().equals(pipeName.get(Pipe.PIPE)) && out == null) {
                            out = outputStream.output;
                        }
                        ExecutionResult er = postProcess(cmd, statement, consoleEngine, out);
                        postProcessed = true;
                        consoleEngine.println(er.result());
                        out = null;
                        boolean success = er.status() == 0;
                        if ((cmd.pipe().equals(pipeName.get(Pipe.OR)) && success)
                                || (cmd.pipe().equals(pipeName.get(Pipe.AND)) && !success)) {
                            break;
                        }
                    }
                }
            } catch (HelpException e) {
                trace(e);
            } catch (Exception e) {
                errorCount++;
                if (cmd.pipe().equals(pipeName.get(Pipe.OR))) {
                    trace(e);
                    postProcessed = true;
                } else {
                    throw e;
                }
            } finally {
                if (!postProcessed && consoleEngine != null) {
                    out = postProcess(cmd, statement, consoleEngine, out).result();
                }
            }
        }
        if (errorCount == 0) {
            names.extractNames(line);
        }
        Log.debug("execute: ", new Date().getTime() - start, " msec");
        return out;
    }

    private ExecutionResult postProcess(
            CommandData cmd, boolean statement, ConsoleEngine consoleEngine, Object result) {
        ExecutionResult out;
        if (cmd.file() != null) {
            int status = 1;
            if (cmd.file().exists()) {
                long delta = new Date().getTime() - cmd.file().lastModified();
                status = delta < 100 ? 0 : 1;
            }
            out = new ExecutionResult(status, result);
        } else if (!statement) {
            outputStream.close();
            out = consoleEngine.postProcess(cmd.rawLine(), result, outputStream.getOutput());
        } else if (cmd.variable() != null) {
            if (consoleEngine.hasVariable(cmd.variable())) {
                out = consoleEngine.postProcess(consoleEngine.getVariable(cmd.variable()));
            } else {
                out = consoleEngine.postProcess(result);
            }
            out = new ExecutionResult(out.status(), null);
        } else {
            out = consoleEngine.postProcess(result);
        }
        return out;
    }

    public void cleanUp() {
        outputStream.close();
        outputStream.resetOutput();
        if (consoleEngine() != null) {
            consoleEngine().purge();
        }
    }

    private void trace(CommandData commandData) {
        if (consoleEngine() != null) {
            consoleEngine().trace(commandData);
        } else {
            AttributedStringBuilder asb = new AttributedStringBuilder();
            asb.append(commandData.rawLine(), AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
                    .println(terminal());
        }
    }

    @Override
    public void trace(Throwable exception) {
        outputStream.close();
        ConsoleEngine consoleEngine = consoleEngine();
        if (consoleEngine != null) {
            if (!(exception instanceof HelpException)) {
                consoleEngine.putVariable("exception", exception);
            }
            consoleEngine.trace(exception);
        } else {
            trace(false, exception);
        }
    }

    @Override
    public void trace(boolean stack, Throwable exception) {
        if (exception instanceof HelpException) {
            HelpException.highlight((exception).getMessage(), Styles.helpStyle())
                    .print(terminal());
        } else if (exception instanceof UnknownCommandException) {
            AttributedStringBuilder asb = new AttributedStringBuilder();
            asb.append(exception.getMessage(), Styles.prntStyle().resolve(".em"));
            asb.toAttributedString().println(terminal());
        } else if (stack) {
            exception.printStackTrace();
        } else {
            String message = exception.getMessage();
            AttributedStringBuilder asb = new AttributedStringBuilder();
            asb.style(Styles.prntStyle().resolve(".em"));
            if (message != null) {
                asb.append(exception.getClass().getSimpleName()).append(": ").append(message);
            } else {
                asb.append("Caught exception: ");
                asb.append(exception.getClass().getCanonicalName());
            }
            asb.toAttributedString().println(terminal());
            Log.debug("Stack: ", exception);
        }
    }

    @Override
    public void close() {
        names.save();
    }

    public ConsoleEngine consoleEngine() {
        return consoleId != null ? (ConsoleEngine) commandRegistries[consoleId] : null;
    }

    private boolean isBuiltinRegistry(CommandRegistry registry) {
        for (Class<?> c : BUILTIN_REGISTRIES) {
            if (c == registry.getClass()) {
                return true;
            }
        }
        return false;
    }

    private void printHeader(String header) {
        AttributedStringBuilder asb = new AttributedStringBuilder().tabs(2);
        asb.append("\t");
        asb.append(header, HelpException.defaultStyle().resolve(".ti"));
        asb.append(":");
        asb.toAttributedString().println(terminal());
    }

    private void printCommandInfo(String command, String info, int max) {
        AttributedStringBuilder asb = new AttributedStringBuilder().tabs(Arrays.asList(4, max + 4));
        asb.append("\t");
        asb.append(command, HelpException.defaultStyle().resolve(".co"));
        asb.append("\t");
        asb.append(info, HelpException.defaultStyle().resolve(".de"));
        asb.setLength(terminal().getWidth());
        asb.toAttributedString().println(terminal());
    }

    private void printCommands(Collection<String> commands, int max) {
        AttributedStringBuilder asb = new AttributedStringBuilder().tabs(Arrays.asList(4, max + 4));
        int col = 0;
        asb.append("\t");
        col += 4;
        boolean done = false;
        for (String c : commands) {
            asb.append(c, HelpException.defaultStyle().resolve(".co"));
            asb.append("\t");
            col += max;
            if (col + max > terminal().getWidth()) {
                asb.toAttributedString().println(terminal());
                asb = new AttributedStringBuilder().tabs(Arrays.asList(4, max + 4));
                col = 0;
                asb.append("\t");
                col += 4;
                done = true;
            } else {
                done = false;
            }
        }
        if (!done) {
            asb.toAttributedString().println(terminal());
        }
        terminal().flush();
    }

    private String doCommandInfo(List<String> info) {
        return info != null && !info.isEmpty() ? info.get(0) : " ";
    }

    private boolean isInTopics(List<String> args, String name) {
        return args.isEmpty() || args.contains(name);
    }

    private Options parseOptions(String[] usage, Object[] args) throws HelpException {
        Options opt = Options.compile(usage).parse(args);
        if (opt.isSet("help")) {
            throw new HelpException(opt.usage());
        }
        return opt;
    }

    private Object help(CommandInput input) {
        String groupsOption = commandGroups ? "nogroups" : "groups";
        String groupsHelp = commandGroups
                ? "     --nogroups                   Commands are not grouped by registries"
                : "     --groups                     Commands are grouped by registries";
        final String[] usage = {
            "help -  command help",
            "Usage: help [TOPIC...]",
            "  -? --help                       Displays command help",
            groupsHelp,
            "  -i --info                       List commands with a short command info"
        };
        try {
            Options opt = parseOptions(usage, input.args());
            boolean doTopic = false;
            boolean cg = commandGroups;
            boolean info = false;
            if (!opt.args().isEmpty() && opt.args().size() == 1) {
                try {
                    String[] args = {"--help"};
                    String command = opt.args().get(0);
                    execute(command, command + " " + args[0], args);
                } catch (UnknownCommandException e) {
                    doTopic = true;
                } catch (Exception e) {
                    exception = e;
                }
            } else {
                doTopic = true;
                if (opt.isSet(groupsOption)) {
                    cg = !cg;
                }
                if (opt.isSet("info")) {
                    info = true;
                }
            }
            if (doTopic) {
                helpTopic(opt.args(), cg, info);
            }
        } catch (Exception e) {
            exception = e;
        }
        return null;
    }

    private void helpTopic(List<String> topics, boolean commandGroups, boolean info) {
        Set<String> commands = commandNames();
        commands.addAll(scriptStore.getScripts());
        boolean withInfo = commands.size() < terminal().getHeight() || !topics.isEmpty() || info;
        int max =
                Collections.max(commands, Comparator.comparing(String::length)).length() + 1;
        TreeMap<String, String> builtinCommands = new TreeMap<>();
        TreeMap<String, String> systemCommands = new TreeMap<>();
        if (!commandGroups && topics.isEmpty()) {
            TreeSet<String> ordered = new TreeSet<>(commands);
            if (withInfo) {
                for (String c : ordered) {
                    List<String> infos = commandInfo(c);
                    String cmdInfo = infos.isEmpty() ? "" : infos.get(0);
                    printCommandInfo(c, cmdInfo, max);
                }
            } else {
                printCommands(ordered, max);
            }
        } else {
            for (CommandRegistry r : commandRegistries) {
                if (isBuiltinRegistry(r)) {
                    for (String c : r.commandNames()) {
                        builtinCommands.put(c, doCommandInfo(commandInfo(c)));
                    }
                }
            }
            for (String c : localCommandNames()) {
                systemCommands.put(c, doCommandInfo(commandInfo(c)));
                exception = null;
            }
            if (isInTopics(topics, "System")) {
                printHeader("System");
                if (withInfo) {
                    for (Entry<String, String> entry : systemCommands.entrySet()) {
                        printCommandInfo(entry.getKey(), entry.getValue(), max);
                    }
                } else {
                    printCommands(systemCommands.keySet(), max);
                }
            }
            if (isInTopics(topics, "Builtins") && !builtinCommands.isEmpty()) {
                printHeader("Builtins");
                if (withInfo) {
                    for (Entry<String, String> entry : builtinCommands.entrySet()) {
                        printCommandInfo(entry.getKey(), entry.getValue(), max);
                    }
                } else {
                    printCommands(builtinCommands.keySet(), max);
                }
            }
            for (CommandRegistry r : commandRegistries) {
                if (isBuiltinRegistry(r)
                        || !isInTopics(topics, r.name())
                        || r.commandNames().isEmpty()) {
                    continue;
                }
                TreeSet<String> cmds = new TreeSet<>(r.commandNames());
                printHeader(r.name());
                if (withInfo) {
                    for (String c : cmds) {
                        printCommandInfo(c, doCommandInfo(commandInfo(c)), max);
                    }
                } else {
                    printCommands(cmds, max);
                }
            }
            if (consoleId != null
                    && isInTopics(topics, "Scripts")
                    && !scriptStore.getScripts().isEmpty()) {
                printHeader("Scripts");
                if (withInfo) {
                    for (String c : scriptStore.getScripts()) {
                        printCommandInfo(c, doCommandInfo(commandInfo(c)), max);
                    }
                } else {
                    printCommands(scriptStore.getScripts(), max);
                }
            }
        }
        terminal().flush();
    }

    private Object exit(CommandInput input) {
        final String[] usage = {
            "exit -  exit from app/script",
            "Usage: exit [OBJECT]",
            "  -? --help                       Displays command help"
        };
        try {
            Options opt = parseOptions(usage, input.xargs());
            ConsoleEngine consoleEngine = consoleEngine();
            if (!opt.argObjects().isEmpty() && consoleEngine != null) {
                try {
                    consoleEngine.putVariable(
                            "_return",
                            opt.argObjects().size() == 1 ? opt.argObjects().get(0) : opt.argObjects());
                } catch (Exception e) {
                    trace(e);
                }
            }
            exception = new EndOfFileException();
        } catch (Exception e) {
            exception = e;
        }
        return null;
    }

    private void registryHelp(CommandRegistry registry) throws Exception {
        List<Integer> tabs = new ArrayList<>();
        tabs.add(0);
        tabs.add(9);
        int max = registry.commandNames().stream()
                .map(String::length)
                .max(Integer::compareTo)
                .get();
        tabs.add(10 + max);
        AttributedStringBuilder sb = new AttributedStringBuilder().tabs(tabs);
        sb.append(" -  ");
        sb.append(registry.name());
        sb.append(" registry");
        sb.append("\n");
        boolean first = true;
        for (String c : new TreeSet<>(registry.commandNames())) {
            if (first) {
                sb.append("Summary:");
                first = false;
            }
            sb.append("\t");
            sb.append(c);
            sb.append("\t");
            sb.append(registry.commandInfo(c).get(0));
            sb.append("\n");
        }
        throw new HelpException(sb.toString());
    }

    private Object subcommand(CommandInput input) {
        Object out = null;
        try {
            if (input.args().length > 0 && subcommands.get(input.command()).hasCommand(input.args()[0])) {
                out = subcommands
                        .get(input.command())
                        .invoke(
                                input.session(),
                                input.args()[0],
                                input.xargs().length > 1
                                        ? Arrays.copyOfRange(input.xargs(), 1, input.xargs().length)
                                        : new Object[] {});
            } else {
                registryHelp(subcommands.get(input.command()));
            }
        } catch (Exception e) {
            exception = e;
        }
        return out;
    }

    private List<OptDesc> commandOptions(String command) {
        try {
            localExecute(command, new String[] {"--help"});
        } catch (HelpException e) {
            exception = null;
            return JlineCommandRegistry.compileCommandOptions(e.getMessage());
        } catch (Exception e) {
            trace(e);
        }
        return null;
    }

    private List<String> registryNames() {
        List<String> out = new ArrayList<>();
        out.add("System");
        out.add("Builtins");
        if (consoleId != null) {
            out.add("Scripts");
        }
        for (CommandRegistry r : commandRegistries) {
            if (!isBuiltinRegistry(r)) {
                out.add(r.name());
            }
        }
        out.addAll(commandNames());
        out.addAll(scriptStore.getScripts());
        return out;
    }

    private List<Completer> emptyCompleter(String command) {
        return new ArrayList<>();
    }

    private List<Completer> helpCompleter(String command) {
        List<Completer> completers = new ArrayList<>();
        List<Completer> params = new ArrayList<>();
        params.add(new StringsCompleter(this::registryNames));
        params.add(NullCompleter.INSTANCE);
        completers.add(
                new ArgumentCompleter(NullCompleter.INSTANCE, new OptionCompleter(params, this::commandOptions, 1)));
        return completers;
    }

    private List<Completer> exitCompleter(String command) {
        List<Completer> completers = new ArrayList<>();
        completers.add(new ArgumentCompleter(
                NullCompleter.INSTANCE, new OptionCompleter(NullCompleter.INSTANCE, this::commandOptions, 1)));
        return completers;
    }

    private int registryId(String command) {
        for (int i = 0; i < commandRegistries.length; i++) {
            if (commandRegistries[i].hasCommand(command)) {
                return i;
            }
        }
        return -1;
    }

    private static class PipelineCompleter implements Completer {
        private final NamesAndValues names;
        private final Supplier<Path> workDir;
        private final Map<Pipe, String> pipeName;

        public PipelineCompleter(Supplier<Path> workDir, Map<Pipe, String> pipeName, NamesAndValues names) {
            this.workDir = workDir;
            this.pipeName = pipeName;
            this.names = names;
        }

        public Completer doCompleter() {
            ArgumentCompleter out = new ArgumentCompleter(this);
            out.setStrict(false);
            return out;
        }

        @Override
        public void complete(LineReader reader, ParsedLine commandLine, List<Candidate> candidates) {
            assert commandLine != null;
            assert candidates != null;
            ArgsParser ap = new ArgsParser(reader.getParser());
            ap.parse(commandLine.line().substring(0, commandLine.cursor()));
            List<String> args = ap.args();
            if (args.size() < 2 || !names.hasPipes(args)) {
                return;
            }
            boolean enclosed = ap.isEnclosed(args.get(args.size() - 1));
            String pWord = commandLine.words().get(commandLine.wordIndex() - 1);
            if (enclosed && pWord.equals(pipeName.get(Pipe.NAMED))) {
                for (String name : names.namedPipes()) {
                    candidates.add(new Candidate(name, name, null, null, null, null, true));
                }
            } else if (enclosed && pWord.equals(pipeName.get(Pipe.REDIRECT))
                    || pWord.equals(pipeName.get(Pipe.APPEND))) {
                Completer c = new FilesCompleter(workDir);
                c.complete(reader, commandLine, candidates);
            } else {
                String buffer = commandLine.word().substring(0, commandLine.wordCursor());
                String param = buffer;
                String curBuf = "";
                int lastDelim = names.indexOfLastDelim(buffer);
                if (lastDelim > -1) {
                    param = buffer.substring(lastDelim + 1);
                    curBuf = buffer.substring(0, lastDelim + 1);
                }
                if (curBuf.startsWith("--") && !curBuf.contains("=")) {
                    doCandidates(candidates, names.options(), curBuf, "", param);
                } else if (param.isEmpty()) {
                    doCandidates(candidates, names.fieldsAndValues(), curBuf, "", "");
                } else if (param.contains(".")) {
                    int point = buffer.lastIndexOf(".");
                    param = buffer.substring(point + 1);
                    curBuf = buffer.substring(0, point + 1);
                    doCandidates(candidates, names.fields(), curBuf, "", param);
                } else if (names.encloseBy(param).length() == 1) {
                    lastDelim++;
                    String postFix = names.encloseBy(param);
                    param = buffer.substring(lastDelim + 1);
                    curBuf = buffer.substring(0, lastDelim + 1);
                    doCandidates(candidates, names.quoted(), curBuf, postFix, param);
                } else {
                    doCandidates(candidates, names.fieldsAndValues(), curBuf, "", param);
                }
            }
        }

        private void doCandidates(
                List<Candidate> candidates, Collection<String> fields, String curBuf, String postFix, String hint) {
            if (fields == null) {
                return;
            }
            for (String s : fields) {
                if (s != null && s.startsWith(hint)) {
                    candidates.add(new Candidate(
                            AttributedString.stripAnsi(curBuf + s + postFix), s, null, null, null, null, false));
                }
            }
        }
    }

    private class NamesAndValues {
        private final String[] delims = {
            "&", "\\|", "\\{", "\\}", "\\[", "\\]", "\\(", "\\)", "\\+", "-", "\\*", "=", ">", "<", "~", "!", ":", ",",
            ";"
        };

        private Path fileNames;
        private final Map<String, List<String>> names = new HashMap<>();
        private List<String> namedPipes;

        public NamesAndValues() {
            this(null);
        }

        @SuppressWarnings("unchecked")
        public NamesAndValues(ConfigurationPath configPath) {
            names.put("fields", new ArrayList<>());
            names.put("values", new ArrayList<>());
            names.put("quoted", new ArrayList<>());
            names.put("options", new ArrayList<>());
            ConsoleEngine consoleEngine = consoleEngine();
            if (configPath != null && consoleEngine != null) {
                try {
                    fileNames = configPath.getUserConfig("pipeline-names.json", true);
                    Map<String, List<String>> temp = (Map<String, List<String>>) consoleEngine.slurp(fileNames);
                    for (Entry<String, List<String>> entry : temp.entrySet()) {
                        names.get(entry.getKey()).addAll(entry.getValue());
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        public boolean isPipe(String arg) {
            Map<String, List<String>> customPipes =
                    consoleEngine() != null ? consoleEngine().getPipes() : new HashMap<>();
            return isPipe(arg, customPipes.keySet());
        }

        public boolean hasPipes(Collection<String> args) {
            Map<String, List<String>> customPipes =
                    consoleEngine() != null ? consoleEngine().getPipes() : new HashMap<>();
            for (String a : args) {
                if (isPipe(a, customPipes.keySet())) {
                    return true;
                }
            }
            return false;
        }

        private boolean isPipe(String arg, Set<String> pipes) {
            return pipeName.containsValue(arg) || pipes.contains(arg);
        }

        public void extractNames(String line) {
            if (parser.getCommand(line).equals("pipe")) {
                return;
            }
            ArgsParser ap = new ArgsParser(parser);
            ap.parse(line);
            List<String> args = ap.args();
            int pipeId = 0;
            for (String a : args) {
                if (isPipe(a)) {
                    break;
                }
                pipeId++;
            }
            if (pipeId < args.size()) {
                StringBuilder sb = new StringBuilder();
                int redirectPipe = -1;
                for (int i = pipeId + 1; i < args.size(); i++) {
                    String arg = args.get(i);
                    if (!isPipe(arg) && !namedPipes().contains(arg) && !arg.matches("\\d+") && redirectPipe != i - 1) {
                        if (arg.matches("\\w+(\\(\\))?")) {
                            addValues(arg);
                        } else if (arg.matches("--\\w+(=.*|)$") && arg.length() > 4) {
                            int idx = arg.indexOf('=');
                            if (idx > 0) {
                                if (idx > 4) {
                                    addOptions(arg.substring(2, idx));
                                }
                                sb.append(arg.substring(idx + 1));
                                sb.append(" ");
                            } else if (idx == -1) {
                                addOptions(arg.substring(2));
                            }
                        } else {
                            sb.append(arg);
                            sb.append(" ");
                        }
                    } else if (arg.equals(pipeName.get(Pipe.REDIRECT)) || arg.equals(pipeName.get(Pipe.APPEND))) {
                        redirectPipe = i;
                    } else {
                        redirectPipe = -1;
                    }
                }
                if (sb.length() > 0) {
                    String rest = sb.toString();
                    for (String d : delims) {
                        rest = rest.replaceAll(d, " ");
                    }
                    String[] words = rest.split("\\s+");
                    for (String w : words) {
                        if (w.length() < 3 || w.matches("\\d+")) {
                            continue;
                        }
                        if (isQuoted(w)) {
                            addQuoted(w.substring(1, w.length() - 1));
                        } else if (w.contains(".")) {
                            for (String f : w.split("\\.")) {
                                if (!f.matches("\\d+") && f.matches("\\w+")) {
                                    addFields(f);
                                }
                            }
                        } else if (w.matches("\\w+")) {
                            addValues(w);
                        }
                    }
                }
            }
            namedPipes = null;
        }

        public String encloseBy(String param) {
            boolean quoted =
                    !param.isEmpty() && (param.startsWith("\"") || param.startsWith("'") || param.startsWith("/"));
            if (quoted && param.length() > 1) {
                quoted = !param.endsWith(Character.toString(param.charAt(0)));
            }
            return quoted ? Character.toString(param.charAt(0)) : "";
        }

        private boolean isQuoted(String word) {
            return word.length() > 1
                    && ((word.startsWith("\"") && word.endsWith("\""))
                            || (word.startsWith("'") && word.endsWith("'"))
                            || (word.startsWith("/") && word.endsWith("/")));
        }

        public int indexOfLastDelim(String word) {
            int out = -1;
            for (String d : delims) {
                int x = word.lastIndexOf(d.replace("\\", ""));
                if (x > out) {
                    out = x;
                }
            }
            return out;
        }

        private void addFields(String field) {
            add("fields", field);
        }

        private void addValues(String arg) {
            add("values", arg);
        }

        private void addQuoted(String arg) {
            add("quoted", arg);
        }

        private void addOptions(String arg) {
            add("options", arg);
        }

        private void add(String where, String value) {
            if (value.length() < 3) {
                return;
            }
            names.get(where).remove(value);
            names.get(where).add(0, value);
        }

        public List<String> namedPipes() {
            if (namedPipes == null) {
                namedPipes = consoleId != null ? consoleEngine().getNamedPipes() : new ArrayList<>();
            }
            return namedPipes;
        }

        public List<String> values() {
            return names.get("values");
        }

        public List<String> fields() {
            return names.get("fields");
        }

        public List<String> quoted() {
            return names.get("quoted");
        }

        public List<String> options() {
            return names.get("options");
        }

        private Set<String> fieldsAndValues() {
            Set<String> out = new HashSet<>();
            out.addAll(fields());
            out.addAll(values());
            return out;
        }

        private void truncate(String where, int maxSize) {
            if (names.get(where).size() > maxSize) {
                names.put(where, names.get(where).subList(0, maxSize));
            }
        }

        public void save() {
            ConsoleEngine consoleEngine = consoleEngine();
            if (consoleEngine != null && fileNames != null) {
                int maxSize = consoleEngine.consoleOption("maxValueNames", 100);
                truncate("fields", maxSize);
                truncate("values", maxSize);
                truncate("quoted", maxSize);
                truncate("options", maxSize);
                consoleEngine.persist(fileNames, names);
            }
        }
    }
}
