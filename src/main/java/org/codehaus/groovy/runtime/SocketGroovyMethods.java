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
package org.codehaus.groovy.runtime;

import groovy.lang.Closure;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.SimpleType;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * This class defines new groovy methods for Sockets which enhance
 * JDK classes inside the Groovy environment.
 * <p>
 * NOTE: While this class contains many 'public' static methods, it is
 * primarily regarded as an internal class (its internal package name
 * suggests this also). We value backwards compatibility of these
 * methods when used within Groovy but value less backwards compatibility
 * at the Java method call level. I.e. future versions of Groovy may
 * remove or move a method call in this file but would normally
 * aim to keep the method available from within Groovy.
 */
public class SocketGroovyMethods extends DefaultGroovyMethodsSupport {
    private static final Logger LOG = Logger.getLogger(SocketGroovyMethods.class.getName());

    /**
     * Passes the Socket's InputStream and OutputStream to the closure.  The
     * streams will be closed after the closure returns, even if an exception
     * is thrown.
     *
     * @param socket  a Socket
     * @param closure a Closure
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @since 1.5.2
     */
    public static <T> T withStreams(Socket socket, @ClosureParams(value=SimpleType.class, options={"java.io.InputStream","java.io.OutputStream"}) Closure<T> closure) throws IOException {
        InputStream input = socket.getInputStream();
        OutputStream output = socket.getOutputStream();
        try {
            T result = closure.call(input, output);

            InputStream temp1 = input;
            input = null;
            temp1.close();
            OutputStream temp2 = output;
            output = null;
            temp2.close();

            return result;
        } finally {
            closeWithWarning(input);
            closeWithWarning(output);
        }
    }

    /**
     * Creates an InputObjectStream and an OutputObjectStream from a Socket, and
     * passes them to the closure.  The streams will be closed after the closure
     * returns, even if an exception is thrown.
     *
     * @param socket  this Socket
     * @param closure a Closure
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @since 1.5.0
     */
    public static <T> T withObjectStreams(Socket socket, @ClosureParams(value=SimpleType.class, options={"java.io.ObjectInputStream","java.io.ObjectOutputStream"}) Closure<T> closure) throws IOException {
        InputStream input = socket.getInputStream();
        OutputStream output = socket.getOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(output);
        ObjectInputStream ois = new ObjectInputStream(input);
        try {
            T result = closure.call(ois, oos);

            InputStream temp1 = ois;
            ois = null;
            temp1.close();
            temp1 = input;
            input = null;
            temp1.close();
            OutputStream temp2 = oos;
            oos = null;
            temp2.close();
            temp2 = output;
            output = null;
            temp2.close();

            return result;
        } finally {
            closeWithWarning(ois);
            closeWithWarning(input);
            closeWithWarning(oos);
            closeWithWarning(output);
        }
    }

    /**
     * Overloads the left shift operator to provide an append mechanism to
     * add things to the output stream of a socket
     *
     * @param self  a Socket
     * @param value a value to append
     * @return a Writer
     * @throws IOException if an IOException occurs.
     * @since 1.0
     */
    public static Writer leftShift(Socket self, Object value) throws IOException {
        return IOGroovyMethods.leftShift(self.getOutputStream(), value);
    }

    /**
     * Overloads the left shift operator to provide an append mechanism
     * to add bytes to the output stream of a socket
     *
     * @param self  a Socket
     * @param value a value to append
     * @return an OutputStream
     * @throws IOException if an IOException occurs.
     * @since 1.0
     */
    public static OutputStream leftShift(Socket self, byte[] value) throws IOException {
        return IOGroovyMethods.leftShift(self.getOutputStream(), value);
    }

    /**
     * Accepts a connection and passes the resulting Socket to the closure
     * which runs in a new Thread.
     *
     * @param serverSocket a ServerSocket
     * @param closure      a Closure
     * @return a Socket
     * @throws IOException if an IOException occurs.
     * @see java.net.ServerSocket#accept()
     * @since 1.0
     */
    public static Socket accept(ServerSocket serverSocket, @ClosureParams(value=SimpleType.class, options="java.net.Socket") final Closure closure) throws IOException {
        return accept(serverSocket, true, closure);
    }

    /**
     * Accepts a connection and passes the resulting Socket to the closure
     * which runs in a new Thread or the calling thread, as needed.
     *
     * @param serverSocket    a ServerSocket
     * @param runInANewThread This flag should be true, if the closure should be invoked in a new thread, else false.
     * @param closure         a Closure
     * @return a Socket
     * @throws IOException if an IOException occurs.
     * @see java.net.ServerSocket#accept()
     * @since 1.7.6
     */
    public static Socket accept(ServerSocket serverSocket, final boolean runInANewThread,
                                @ClosureParams(value=SimpleType.class, options="java.net.Socket") final Closure closure) throws IOException {
        final Socket socket = serverSocket.accept();
        if (runInANewThread) {
            new Thread(() -> invokeClosureWithSocket(socket, closure)).start();
        } else {
            invokeClosureWithSocket(socket, closure);
        }
        return socket;
    }

    private static void invokeClosureWithSocket(Socket socket, Closure closure) {
        try {
            closure.call(socket);
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    LOG.warning("Caught exception closing socket: " + e);
                }
            }
        }
    }

}
