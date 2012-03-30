// $Id: echo.java,v 1.1 2004-05-22 07:57:50 bfulgham Exp $
// http://www.bagley.org/~doug/shootout/
// author: Dirus@programmer.net

import java.io.*;
import java.net.*;

public class echo {
    public static void main(String[] args) throws Exception {
        int iIterations = 1;
        try {
            iIterations = Integer.parseInt(args[0]);
        } catch(Exception e) { }

        EchoServer esServer = new EchoServer(0);
        new EchoClient(InetAddress.getLocalHost(), esServer.getPort(), iIterations);
    }
}

class EchoClient extends Thread {
    private static final String GREETING = "Hello there sailor\n";
    private final InetAddress inetaServer;
    private final int         iPort;
    private final int         iIterations;

    public EchoClient(InetAddress inetaServer, int iPort, int iIterations) {
        this.inetaServer = inetaServer;
        this.iPort = iPort;
        this.iIterations = iIterations;
        start();
    }

    public void run() {
        Socket socketFromServer = null;
        try {
            socketFromServer = new Socket(inetaServer, iPort);
            BufferedReader in = new BufferedReader(new InputStreamReader(socketFromServer.getInputStream()));
            OutputStream out = socketFromServer.getOutputStream();

            byte[] bytesOut = GREETING.getBytes();
            String strIn = GREETING.trim();
            for(int i = 0; i < iIterations; ++i) {
            out.write(bytesOut);
            out.flush();
            String strRead = in.readLine();
            if(!strRead.equals(strIn))
                throw new RuntimeException("client: \"" + strIn + "\" ne \"" + strRead + "\"");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        try {
            socketFromServer.close();
        } catch(Exception e) { }
    }
}

class EchoServer extends Thread {
    private static final int   BUFFER_SIZE = 1024;
    private final ServerSocket ssAccepting;
    private final int          iPort;

    public EchoServer(int iPort) throws IOException {
        ssAccepting = new ServerSocket(iPort);
        this.iPort = ssAccepting.getLocalPort();
        start();
    }

    public final int getPort() {
        return iPort;
    }

    public void run() {
        byte bytesIn[] = new byte[BUFFER_SIZE];
        try {
            Socket socketClient = ssAccepting.accept();
            InputStream in = socketClient.getInputStream();
            OutputStream out = socketClient.getOutputStream();
            int iLength, iCount = 0;
            while ((iLength = in.read(bytesIn)) != -1) {
                out.write(bytesIn, 0, iLength);
                out.flush();
                iCount += iLength;
            }
            System.out.println("server processed " + iCount + " bytes");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
