/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */

package groovy.ui;
import groovy.lang.*;
import java.io.*;
import java.net.*;

/**
 * Simple server that executes supplied script against a socket
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
        private boolean autoOutput;
    
        GroovyClientConnection(Script script, boolean autoOutput,Socket socket) throws IOException {
            this.script = script;
            this.autoOutput = autoOutput;
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
                            if (autoOutput) {
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
