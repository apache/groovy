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
package org.apache.groovy.mcp;

import org.apache.groovy.mcp.internal.JsonValues;
import org.apache.groovy.mcp.internal.StdioFrames;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PushbackInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Process entry for the Groovy MCP server. Stdio is newline-delimited
 * JSON-RPC. {@code --help} and {@code --version} write to the supplied
 * output and do not start the protocol. {@link #run} does not call
 * {@link System#exit}. {@link #main} does, after moving incidental
 * {@code System.out} prints onto stderr so they cannot break the stream.
 */
@SuppressWarnings("java:S106") // stdout is the JSON-RPC stream; --help and --version are CLI
public final class GroovyMcpLauncher {

    private static final String HELP = """
            groovymcp - Apache Groovy MCP server
            Usage: groovymcp [--stdio] [--root <dir>] [--classpath <path>] [--version] [--help]
            Speaks MCP over stdio. Does not run scripts.
            --root confines path arguments to that directory and does not scan it.
            --classpath is the compile classpath. A model cannot change it.
            """;

    private GroovyMcpLauncher() {
    }

    /**
     * Parsed command line. {@code config} is set only for server mode.
     *
     * @param help whether to print usage
     * @param version whether to print the version
     * @param error whether the command line was rejected
     * @param message error text, or empty
     * @param config server config, or {@code null}
     */
    public record Parsed(boolean help, boolean version, boolean error, String message, McpServerConfig config) {

        static Parsed usage() {
            return new Parsed(true, false, false, "", null);
        }

        static Parsed release() {
            return new Parsed(false, true, false, "", null);
        }

        static Parsed error(final String message) {
            return new Parsed(false, false, true, message, null);
        }

        static Parsed server(final McpServerConfig config) {
            return new Parsed(false, false, false, "", config);
        }
    }

    /**
     * Process entry. Returns a status to the operating system.
     *
     * @param args command line
     */
    @SuppressWarnings("java:S1147") // the process entry must return a status; in-process callers use launch
    public static void main(final String[] args) {
        System.exit(launch(args));
    }

    /**
     * Body of {@link #main} without {@link System#exit}. When the process
     * serves the protocol, incidental {@code System.out} writes are moved
     * to stderr for the call and restored afterwards.
     *
     * @param args command line
     * @return process status
     */
    static int launch(final String[] args) {
        InputStream in = System.in;
        OutputStream out = System.out;
        OutputStream err = System.err;
        PrintStream previous = System.out;
        boolean redirected = serves(args);
        if (redirected) {
            System.setOut(new PrintStream(err, true, StandardCharsets.UTF_8));
        }
        try {
            return run(in, out, err, args);
        } finally {
            if (redirected) {
                System.setOut(previous);
            }
        }
    }

    /**
     * Runs one invocation.
     *
     * @param in protocol input, ignored for {@code --help} and {@code --version}
     * @param out protocol or usage output
     * @param err errors
     * @param args command line
     * @return process status
     */
    public static int run(final InputStream in, final OutputStream out, final OutputStream err, final String[] args) {
        Parsed parsed = parse(args);
        OutputStream sink = out == null ? OutputStream.nullOutputStream() : out;
        PrintStream errors = new PrintStream(err == null ? OutputStream.nullOutputStream() : err, true,
                StandardCharsets.UTF_8);
        try {
            if (parsed.help) {
                sink.write(HELP.getBytes(StandardCharsets.UTF_8));
                return 0;
            }
            if (parsed.version) {
                String line = "groovy-mcp " + GroovyMcpServer.implementationVersion() + "\n";
                sink.write(line.getBytes(StandardCharsets.UTF_8));
                return 0;
            }
            if (parsed.error) {
                errors.println(parsed.message);
                return 2;
            }
            if (in == null) {
                errors.println("groovy-mcp: missing input");
                return 1;
            }
            return serve(in, sink, errors, parsed.config);
        } catch (IOException ex) {
            errors.println("groovy-mcp: " + JsonValues.brief(ex.getMessage()));
            return 1;
        }
    }

    /**
     * @param args command line, or {@code null}
     * @return the parsed command
     */
    public static Parsed parse(final String[] args) {
        String[] values = args == null ? new String[0] : args;
        for (String arg : values) {
            if ("--help".equals(arg)) {
                return Parsed.usage();
            }
        }
        boolean version = false;
        Path root = null;
        boolean sawRoot = false;
        List<String> classpath = List.of();
        boolean sawClasspath = false;
        for (int i = 0; i < values.length; i++) {
            String arg = values[i];
            if (arg == null) {
                return Parsed.error("missing argument");
            }
            switch (arg) {
                case "--version" -> version = true;
                case "--stdio" -> {
                    // stdio is the only transport
                }
                case "--root" -> {
                    if (sawRoot) {
                        return Parsed.error("duplicate --root");
                    }
                    if (i + 1 >= values.length || values[i + 1] == null) {
                        return Parsed.error("--root requires a directory");
                    }
                    sawRoot = true;
                    try {
                        root = Path.of(values[++i]);
                    } catch (InvalidPathException ex) {
                        return Parsed.error("--root is not a directory");
                    }
                }
                case "--classpath" -> {
                    if (sawClasspath) {
                        return Parsed.error("duplicate --classpath");
                    }
                    if (i + 1 >= values.length || values[i + 1] == null) {
                        return Parsed.error("--classpath requires a path");
                    }
                    sawClasspath = true;
                    classpath = splitClasspath(values[++i]);
                }
                default -> {
                    return Parsed.error("unknown argument: " + JsonValues.brief(arg));
                }
            }
        }
        if (version) {
            return Parsed.release();
        }
        if (sawRoot && !Files.isDirectory(root)) {
            return Parsed.error("--root is not a directory");
        }
        return Parsed.server(new McpServerConfig(root, classpath));
    }

    /**
     * @param args command line
     * @return {@code false} when the process only prints help or the version
     */
    static boolean serves(final String[] args) {
        if (args == null) {
            return true;
        }
        for (String arg : args) {
            if ("--help".equals(arg) || "--version".equals(arg)) {
                return false;
            }
        }
        return true;
    }

    private static int serve(final InputStream in, final OutputStream out, final PrintStream err,
                             final McpServerConfig config) {
        GroovyMcpServer server = new GroovyMcpServer(config, err);
        PushbackInputStream push = new PushbackInputStream(in, 1);
        OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        try {
            while (true) {
                StdioFrames.Frame frame = StdioFrames.read(push, config.maxMessageBytes());
                String response = switch (frame.kind()) {
                    case EOF -> null;
                    case TOO_LARGE -> server.tooLarge();
                    case MALFORMED -> server.malformed();
                    case LINE -> server.exchange(frame.text());
                };
                if (frame.kind() == StdioFrames.Frame.Kind.EOF) {
                    return 0;
                }
                if (response != null) {
                    writer.write(response);
                    writer.write('\n');
                    writer.flush();
                }
            }
        } catch (IOException | RuntimeException ex) {
            err.println("groovy-mcp: " + JsonValues.brief(ex.getMessage()));
            return 1;
        } finally {
            server.close();
        }
    }

    static List<String> splitClasspath(final String raw) {
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }
        List<String> entries = new ArrayList<>();
        for (String part : raw.split(Pattern.quote(File.pathSeparator), -1)) {
            if (!part.isEmpty()) {
                entries.add(part);
            }
        }
        return List.copyOf(entries);
    }
}
