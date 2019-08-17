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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
 */
public class GroovySocketServer implements Runnable {
    private URL url;
    private final GroovyShell groovy;
    private final GroovyCodeSource source;
    private final boolean autoOutput;
    private static int counter;

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
     *       the port to listen on
     *
     */
    public GroovySocketServer(GroovyShell groovy, boolean isScriptFile, String scriptFilenameOrText, boolean autoOutput, int port) {
        this(groovy, getCodeSource(isScriptFile, scriptFilenameOrText), autoOutput, port);
    }

    private static GroovyCodeSource getCodeSource(boolean scriptFile, String scriptFilenameOrText) {
        if (scriptFile) {
            try {
                if (URI_PATTERN.matcher(scriptFilenameOrText).matches()) {
                    return new GroovyCodeSource(new URI(scriptFilenameOrText));
                } else {
                    return new GroovyCodeSource(GroovyMain.searchForGroovyScriptFile(scriptFilenameOrText));
                }
            } catch (IOException e) {
                throw new GroovyRuntimeException("Unable to get script from: " + scriptFilenameOrText, e);
            } catch (URISyntaxException e) {
                throw new GroovyRuntimeException("Unable to get script from URI: " + scriptFilenameOrText, e);
            }
        } else {
            // We could jump through some hoops to have GroovyShell make our script name, but that seems unwarranted.
            // If we *did* jump through that hoop then we should probably change the run loop to not recompile
            // the script on every iteration since the script text can't change (the reason for the recompilation).
            return new GroovyCodeSource(scriptFilenameOrText, generateScriptName(), GroovyShell.DEFAULT_CODE_BASE);
        }
    }

    private static synchronized String generateScriptName() {
        return "ServerSocketScript" + (++counter) + ".groovy";
    }


    // RFC2396
    // scheme        = alpha *( alpha | digit | "+" | "-" | "." )
    private static final Pattern URI_PATTERN = Pattern.compile("\\p{Alpha}[-+.\\p{Alnum}]*:.*");

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
    *       the port to listen on
    * @since 2.3.0
    */ 
    public GroovySocketServer(GroovyShell groovy, GroovyCodeSource source, boolean autoOutput, int port) {
        this.groovy = groovy;
        this.source = source;
        this.autoOutput = autoOutput;
        try {
            url = new URL("http", InetAddress.getLocalHost().getHostAddress(), port, "/");
            System.out.println("groovy is listening on port " + port);
        } catch (IOException e) { 
            e.printStackTrace();
        }
        new Thread(this).start();
    }

    /**
    * Runs this server. There is typically no need to call this method, as the object's constructor
    * creates a new thread and runs this object automatically. 
    */
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(url.getPort())) {
            while (true) {
                // Create one script per socket connection.
                // This is purposefully not caching the Script
                // so that the script source file can be changed on the fly,
                // as each connection is made to the server.
                //FIXME: Groovy has other mechanisms specifically for watching to see if source code changes.
                // We should probably be using that here.
                // See also the comment about the fact we recompile a script that can't change.
                Script script = groovy.parse(source);
                new GroovyClientConnection(script, autoOutput, serverSocket.accept());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    static class GroovyClientConnection implements Runnable {
        private Script script;
        private Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;
        private boolean autoOutputFlag;
    
        GroovyClientConnection(Script script, boolean autoOutput,Socket socket) throws IOException {
            this.script = script;
            this.autoOutputFlag = autoOutput;
            this.socket = socket;
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream());
            new Thread(this, "Groovy client connection - " + socket.getInetAddress().getHostAddress()).start();
        }
        public void run() {
            try {
                String line = null;
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
