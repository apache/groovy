/** 
 * Simple server that executes supplied script against a socket
 * @author: Jeremy Rayner
 */

package groovy.ui;
import groovy.lang.*;
import java.io.*;
import java.net.*;

public class GroovySocketServer implements Runnable {
    private URL url;
    private GroovyShell groovy;
    private boolean isScriptFile;
    private String scriptLocation;
    private boolean autoOutput;
    
    public GroovySocketServer(GroovyShell groovy, boolean isScriptFile, String scriptLocation, boolean autoOutput, int port) {
        this.groovy = groovy;
        this.isScriptFile = isScriptFile;
        this.scriptLocation = scriptLocation;
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
                // create one script per socket connection
                Script script;
                if (isScriptFile) {
                    script = groovy.parse(new File(scriptLocation));
                } else {
                    script = groovy.parse(scriptLocation, "main");
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
