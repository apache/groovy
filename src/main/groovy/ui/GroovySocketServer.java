/*
 * Copyright 2003-2007 the original author or authors.
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

/**
 * Simple server that executes supplied script against a socket
 *
 * @version $Id$
 * @author Jeremy Rayner
 */
public class GroovySocketServer implements Runnable {
    private URL url;
    private GroovyShell groovy;
    private boolean isScriptFile;
    private String scriptFilenameOrText;
    private boolean autoOutput;
    
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
                    script = groovy.parse(new FileInputStream(gm.huntForTheScriptFile(scriptFilenameOrText)));
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
