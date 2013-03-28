/*
 * Copyright 2003-2013 the original author or authors.
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
package groovy.ui;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

/**
 * Simple server that executes supplied script against a socket.
 * <p>
 * Typically this is used from the groovy command line agent but it can be 
 * invoked programatically. To run this program from the command line please
 * refer to the command line documentation at <a href="http://groovy.codehaus.org/Groovy+CLI">
 * Groovy CLI</a>.
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
 * 
 * @author Jeremy Rayner
 */
public class GroovySocketServer implements Runnable {
    private URL url;
    private GroovyShell groovy;
    private boolean isScriptFile;
    private String scriptFilenameOrText;
    private boolean autoOutput;
    
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
        this.groovy = groovy;
        this.isScriptFile = isScriptFile;
        this.scriptFilenameOrText = scriptFilenameOrText;
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
        try {
            ServerSocket serverSocket = new ServerSocket(url.getPort());
            while (true) {
                // Create one script per socket connection.
                // This is purposefully not caching the Script
                // so that the script source file can be changed on the fly,
                // as each connection is made to the server.
                Script script;
                if (isScriptFile) {
                    GroovyMain gm = new GroovyMain();
                    script = groovy.parse(gm.getText(scriptFilenameOrText));
                } else {
                    script = groovy.parse(scriptFilenameOrText);
                }
                new GroovyClientConnection(script, autoOutput, serverSocket.accept());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    class GroovyClientConnection implements Runnable {
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
