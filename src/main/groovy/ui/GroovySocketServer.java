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
    private Script script;
    
    public GroovySocketServer(Script script, int port) {
        this.script = script;
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
                new GroovyClientConnection(script, serverSocket.accept());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    class GroovyClientConnection implements Runnable {
        private Script script;
        private Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;
    
        GroovyClientConnection(Script script, Socket socket) throws IOException {
            this.script = script;
            this.socket = socket;
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream());
            new Thread(this, "Groovy client connection - " + socket.getInetAddress().getHostAddress()).start();
        }
        public void run() {
            try {
                String line = null;
                script.setProperty("out", writer);
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    script.setProperty("line", line);
                    Object o = script.run();
                    writer.println(o);
                    writer.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
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
