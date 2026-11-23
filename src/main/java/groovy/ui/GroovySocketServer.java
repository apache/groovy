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
package groovy.ui;

import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.util.URLStreams;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Simple server that executes supplied script against a socket.
 * <p>
 * Typically this is used from the groovy command line agent but it can be
 * invoked programmatically. To run this program from the command line please
 * refer to the command line documentation at
 * <a href="http://docs.groovy-lang.org/docs/latest/html/documentation/#_running_groovy_from_the_commandline">
 * Running Groovy from the commandline</a>.
 * <p>
 * Here is an example of how to use this class to open a listening socket on the server,
 * listen for incoming data, and then echo the data back to the client in reverse order:
 * <pre>
 * new GroovySocketServer(
 *         new GroovyShell(),      // evaluator
 *         false,                  // is not a file
 *         "println line.reverse()",         // script to evaluate
 *         true,                   // return result to client
 *         1960)                   //port
 * </pre>
 * There are several variables in the script binding:
 * <ul>
 * <li>line - The data from the socket</li>
 * <li>out - The output PrintWriter, should you need it for some reason.</li>
 * <li>socket - The socket, should you need it for some reason.</li>
 * </ul>
 * <p>
 * A new script instance is created for each connection. The source is read
 * after the connection is accepted, and the class is compiled once and again
 * only when a file or URL source's bytes change, so a connection does not
 * define another class. An edit made while the listener is waiting is what
 * the connection that ends that wait runs. A failed read or compile is
 * reported and that client is closed; the listener keeps accepting. Instances
 * share the {@link GroovyShell} binding. Class-level state (static fields,
 * static initializers, and class-level meta-class changes) survives from one
 * connection to the next, while instance fields still reset because each
 * connection gets a new instance.
 * <p>
 * The constructors that take a port bind the listening socket to the loopback
 * address, so only clients on the same host can connect. To accept connections
 * from other hosts, pass an explicit {@link InetSocketAddress}; note that doing
 * so exposes the script to any peer that can reach the port, and this class
 * performs no authentication of its own.
 */
public class GroovySocketServer implements Runnable {
    private final InetSocketAddress bindAddress;
    private final GroovyShell groovy;
    private final GroovyCodeSource source;
    private final boolean autoOutput;
    private static int counter;
    /**
     * Listening socket once {@link #run()} has bound it. Closed by
     * {@link #close()}, or by {@link #run()} when {@code close()} already
     * ran and this field had not been published yet.
     */
    private volatile ServerSocket serverSocket;
    /**
     * Set by {@link #close()} before it reads or closes {@link #serverSocket}.
     * {@link #run()} reads this after publishing the socket. The
     * {@code SocketException} catch reads it as well: try-with-resources
     * closes that socket before the catch runs, so
     * {@link ServerSocket#isClosed()} cannot tell {@link #close()} from any
     * other {@link SocketException}.
     */
    private volatile boolean closing;
    /**
     * Thread started by the public constructors. Absent when the server is not started.
     */
    private volatile Thread serverThread;
    /**
     * Class compiled for {@link #sourceBytes}. Reused for every connection until the bytes change.
     */
    private Class<?> scriptClass;
    /**
     * Bytes {@link #scriptClass} was compiled from.
     */
    private byte[] sourceBytes;

    /**
     * This creates and starts the socket server on a new Thread. There is no need to call run or spawn
     * a new thread yourself.
     * @param groovy
     *       The GroovyShell object that evaluates the incoming text. If you need additional classes in the
     *       classloader then configure that through this object.
     * @param isScriptFile
     *       Whether the incoming socket data String will be a script or a file path.
     * @param scriptFilenameOrText
     *       This will be a groovy script or a file location depending on the argument isScriptFile.
     * @param autoOutput
     *       whether output should be automatically echoed back to the client
     * @param port
     *       the port to listen on, bound to the loopback address
     *
     */
    public GroovySocketServer(GroovyShell groovy, boolean isScriptFile, String scriptFilenameOrText, boolean autoOutput, int port) {
        this(groovy, getCodeSource(isScriptFile, scriptFilenameOrText), autoOutput, port);
    }

    private static GroovyCodeSource getCodeSource(boolean scriptFile, String scriptFilenameOrText) {
        if (scriptFile) {
            try {
                if (isScriptUri(scriptFilenameOrText)) {
                    return new GroovyCodeSource(new URI(scriptFilenameOrText));
                }
                return new GroovyCodeSource(GroovyMain.searchForGroovyScriptFile(scriptFilenameOrText));
            } catch (IOException e) {
                throw new GroovyRuntimeException("Unable to get script from: " + scriptFilenameOrText, e);
            } catch (URISyntaxException e) {
                throw new GroovyRuntimeException("Unable to get script from URI: " + scriptFilenameOrText, e);
            }
        } else {
            // A stable name lets every connection reuse one compiled class. Inline text cannot change.
            GroovyCodeSource source = new GroovyCodeSource(scriptFilenameOrText, generateScriptName(), GroovyShell.DEFAULT_CODE_BASE);
            source.setNameGenerated(true);
            return source;
        }
    }

    private static synchronized String generateScriptName() {
        return "ServerSocketScript" + (++counter) + ".groovy";
    }


    // RFC2396
    // scheme        = alpha *( alpha | digit | "+" | "-" | "." )
    private static final Pattern URI_PATTERN = Pattern.compile("\\p{Alpha}[-+.\\p{Alnum}]*:.*");

    /**
     * True for a hierarchical URI such as {@code file:} or {@code http:}.
     * A Windows drive path ({@code C:\...} or {@code C:/...}) matches a naive
     * scheme pattern but is a filesystem path, so it is opened as a file.
     */
    private static boolean isScriptUri(final String value) {
        if (value.length() >= 2 && Character.isLetter(value.charAt(0)) && value.charAt(1) == ':') {
            return false;
        }
        return URI_PATTERN.matcher(value).matches();
    }

    /**
    * This creates and starts the socket server on a new Thread. There is no need to call run or spawn
    * a new thread yourself.
    * @param groovy
    *       The GroovyShell object that evaluates the incoming text. If you need additional classes in the
    *       classloader then configure that through this object.
    * @param source
    *       GroovyCodeSource for the Groovy script
    * @param autoOutput
    *       whether output should be automatically echoed back to the client
    * @param port
    *       the port to listen on, bound to the loopback address
    * @since 2.3.0
    */
    public GroovySocketServer(GroovyShell groovy, GroovyCodeSource source, boolean autoOutput, int port) {
        this(groovy, source, autoOutput, new InetSocketAddress(InetAddress.getLoopbackAddress(), port));
    }

    /**
    * This creates and starts the socket server on a new Thread. There is no need to call run or spawn
    * a new thread yourself.
    * <p>
    * Binding to anything other than a loopback address accepts connections from other hosts. The
    * script is run once per line received, and this class performs no authentication, so the port
    * should be reachable only by peers the script is prepared to serve.
    * @param groovy
    *       The GroovyShell object that evaluates the incoming text. If you need additional classes in the
    *       classloader then configure that through this object.
    * @param source
    *       GroovyCodeSource for the Groovy script
    * @param autoOutput
    *       whether output should be automatically echoed back to the client
    * @param bindAddress
    *       the address and port to listen on
    * @since 6.0.0
    */
    public GroovySocketServer(GroovyShell groovy, GroovyCodeSource source, boolean autoOutput, InetSocketAddress bindAddress) {
        this(groovy, source, autoOutput, bindAddress, true);
    }

    private GroovySocketServer(GroovyShell groovy, GroovyCodeSource source, boolean autoOutput, InetSocketAddress bindAddress, boolean start) {
        this.groovy = groovy;
        this.source = source;
        this.autoOutput = autoOutput;
        if (bindAddress.isUnresolved()) {
            throw new IllegalArgumentException("Cannot listen on unresolved address: " + bindAddress);
        }
        this.bindAddress = bindAddress;
        if (start) {
            Thread thread = new Thread(this);
            this.serverThread = thread;
            thread.start();
        }
    }

    /**
     * Builds a server that does not open a socket or start a thread, so tests can
     * load scripts without accepting connections.
     *
     * @param groovy the shell that compiles the script and supplies its binding
     * @param source the script source
     * @return a server that has not been started
     */
    static GroovySocketServer withoutStarting(GroovyShell groovy, GroovyCodeSource source) {
        return new GroovySocketServer(groovy, source, false,
                new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), false);
    }

    /**
     * Reports where the server is listening, once it is. Reporting the requested address before
     * binding would announce a server that failed to start, and would name port 0 rather than the
     * one the system chose for it.
     *
     * @param serverSocket the bound socket
     */
    private static void announce(ServerSocket serverSocket) {
        InetAddress host = serverSocket.getInetAddress();
        String where = host.isAnyLocalAddress() ? "all interfaces" : host.getHostAddress();
        System.out.println("groovy is listening on " + where + " port " + serverSocket.getLocalPort());
        if (!host.isLoopbackAddress()) {
            System.out.println("warning: this port accepts connections from other hosts and is not authenticated");
        }
    }

    /**
    * Runs this server. There is typically no need to call this method, as the object's constructor
    * creates a new thread and runs this object automatically.
    * <p>
    * Each connection is accepted before its script is read and compiled, so the
    * connection runs the bytes just read. A failed read or compile is reported
    * and does not stop the listener.
    */
    @Override
    public void run() {
        try (ServerSocket listening = new ServerSocket(bindAddress.getPort(), 0, bindAddress.getAddress())) {
            this.serverSocket = listening;
            // close() may already have observed a null socket and set closing.
            // This read sees that write, and try-with-resources closes the
            // socket on the way out: no SocketException, so no trace. If this
            // check has passed, close() observes the socket published above
            // and unblocks accept().
            if (closing) {
                return;
            }
            announce(listening);
            while (true) {
                // Accept first. GroovyClientConnection starts a handler and
                // returns, so a script read before this accept would still be
                // the previous bytes, and the connection that ends the wait
                // would run that stale class. The bytes read after accept()
                // are the ones this connection runs.
                Socket socket = listening.accept();
                // close() may have set the flag after this client was accepted.
                // Close the client and return; try-with-resources closes the
                // listening socket and leaves no trace. A SocketException from
                // accept() itself is not caught here.
                if (closing) {
                    socket.close();
                    return;
                }
                Script script;
                try {
                    // One instance per connection. The class is reused until a
                    // file or URL source's bytes change. A failed read or
                    // compile is reported and the client is closed; the loop
                    // accepts again. Reading again before the next accept()
                    // would spin while the source stayed unreadable.
                    script = newConnectionScript();
                } catch (CompilationFailedException | IOException e) {
                    e.printStackTrace();
                    try {
                        socket.close();
                    } catch (IOException closeError) {
                        closeError.printStackTrace();
                    }
                    continue;
                }
                new GroovyClientConnection(script, autoOutput, socket);
            }
        } catch (SocketException e) {
            // try-with-resources closes the listening socket before this catch
            // runs, so isClosed() cannot distinguish close() from other
            // SocketExceptions. close() sets closing before ServerSocket.close();
            // print when that flag is still clear, including a failure to bind.
            if (!closing) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Port the server is bound to, or {@code -1} before it is listening.
     *
     * @return the bound port, or {@code -1}
     */
    int getLocalPort() {
        ServerSocket listening = serverSocket;
        return listening == null ? -1 : listening.getLocalPort();
    }

    /**
     * Stops listening. A blocked {@code accept} returns and the server thread
     * ends. If {@code accept} has already returned a client and the source
     * has not been read yet, that client is closed and {@code run()} returns.
     * If this method runs before {@link #run()} publishes the listening
     * socket, {@code run()} still exits after the bind, when it observes
     * {@code closing}. {@link Thread#join(long) join} waits at most 5 seconds,
     * so a thread blocked in compilation or a source read after {@code accept}
     * can still be alive when this method returns. That thread still exits
     * when it next reaches {@code accept}.
     * <p>
     * Does nothing when the server was not started. The {@link SocketException}
     * from unblocking {@code accept} is not printed. An exit at the
     * {@code closing} check closes the socket with try-with-resources and does
     * not throw, so it leaves no trace either.
     *
     * @throws IOException if closing the listening socket fails
     */
    void close() throws IOException {
        // Set before the read of serverSocket and before ServerSocket.close().
        // run() publishes the socket and then reads closing, so either this
        // method closes the published socket or run() sees the flag and
        // returns. try-with-resources closes the listening socket before the
        // catch runs, so isClosed() cannot distinguish close() from other
        // SocketExceptions.
        closing = true;
        ServerSocket listening = serverSocket;
        if (listening != null) {
            listening.close();
        }
        Thread thread = serverThread;
        if (thread != null && thread != Thread.currentThread()) {
            try {
                thread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Returns a new script instance for one connection.
     * <p>
     * The compiled class is kept until a file or URL source's bytes change, and
     * each instance uses the shell binding. Inline text has no bytes to change,
     * so it is compiled once.
     *
     * @return a script instance for this connection
     * @throws IOException if a file or URL source cannot be read
     */
    Script newConnectionScript() throws IOException {
        byte[] latest = readSourceBytes();
        if (scriptClass == null || !Arrays.equals(latest, sourceBytes)) {
            scriptClass = groovy.getClassLoader().parseClass(source, false);
            sourceBytes = latest;
        }
        return InvokerHelper.createScript(scriptClass, groovy.getContext());
    }

    /**
     * Current bytes of the source. File and URL sources are read again so an edit
     * is visible; inline text is the text the code source already holds.
     *
     * @return the bytes to compare with the last compilation
     * @throws IOException if a file or URL source cannot be read
     */
    private byte[] readSourceBytes() throws IOException {
        File file = source.getFile();
        if (file != null) {
            return Files.readAllBytes(file.toPath());
        }
        URL url = source.getURL();
        if (url != null) {
            try (InputStream in = URLStreams.openUncachedStream(url)) {
                return in.readAllBytes();
            }
        }
        return source.getScriptText().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Handles a single client connection.
     */
    static class GroovyClientConnection implements Runnable {
        private Script script;
        private Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;
        private boolean autoOutputFlag;

        /**
         * Creates and starts a client connection handler.
         *
         * @param script the script to execute per line
         * @param autoOutput whether non-null results should be written automatically
         * @param socket the client socket
         * @throws IOException if the socket streams cannot be opened
         */
        GroovyClientConnection(Script script, boolean autoOutput,Socket socket) throws IOException {
            this.script = script;
            this.autoOutputFlag = autoOutput;
            this.socket = socket;
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream());
            new Thread(this, "Groovy client connection - " + socket.getInetAddress().getHostAddress()).start();
        }
        /**
         * Processes incoming client lines until the connection closes.
         */
        @Override
        public void run() {
            try {
                String line;
                script.setProperty("out", writer);
                script.setProperty("socket", socket);
                script.setProperty("init", Boolean.TRUE);
                while ((line = reader.readLine()) != null) {
                    // System.out.println(line);
                    script.setProperty("line", line);
                    Object o = script.run();
                    script.setProperty("init", Boolean.FALSE);
                    if (o != null) {
                        if ("success".equals(o)) {
                            break; // to close sockets gracefully etc...
                        } else {
                            if (autoOutputFlag) {
                                writer.println(o);
                            }
                        }
                    }
                    writer.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    writer.flush();
                    writer.close();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
            }
        }
    }
}
