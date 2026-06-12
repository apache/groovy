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
import groovy.lang.GroovyRuntimeException;
import groovy.transform.NamedParam;
import groovy.transform.NamedParams;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

/**
 * This class defines new groovy methods which appear on normal JDK
 * classes related to process management.
 * <p>
 * Static methods are used with the first parameter being the destination class,
 * i.e. <code>public static String reverse(String self)</code>
 * provides a <code>reverse()</code> method for <code>String</code>.
 * <p>
 * NOTE: While this class contains many 'public' static methods, it is
 * primarily regarded as an internal class (its internal package name
 * suggests this also). We value backwards compatibility of these
 * methods when used within Groovy but value less backwards compatibility
 * at the Java method call level. I.e. future versions of Groovy may
 * remove or move a method call in this file but would normally
 * aim to keep the method available from within Groovy.
 */
public class ProcessGroovyMethods extends DefaultGroovyMethodsSupport {

    /**
     * An alias method so that a process appears similar to System.out, System.in, System.err;
     * you can use process.in, process.out, process.err in a similar fashion.
     *
     * @param self a Process instance
     * @return the InputStream for the process
     * @since 1.0
     */
    public static InputStream getIn(Process self) {
        return self.getInputStream();
    }

    /**
     * Read the text of the output stream of the Process.
     * Closes all the streams associated with the process after retrieving the text.
     *
     * @param self a Process instance
     * @return the text of the output
     * @throws java.io.IOException if an IOException occurs.
     * @since 1.0
     */
    public static String getText(Process self) throws IOException {
        String text = IOGroovyMethods.getText(new BufferedReader(new InputStreamReader(self.getInputStream())));
        closeStreams(self);
        return text;
    }

    /**
     * An alias method so that a process appears similar to System.out, System.in, System.err;
     * you can use process.in, process.out, process.err in a similar fashion.
     *
     * @param self a Process instance
     * @return the error InputStream for the process
     * @since 1.0
     */
    public static InputStream getErr(Process self) {
        return self.getErrorStream();
    }

    /**
     * An alias method so that a process appears similar to System.out, System.in, System.err;
     * you can use process.in, process.out, process.err in a similar fashion.
     *
     * @param self a Process instance
     * @return the OutputStream for the process
     * @since 1.0
     */
    public static OutputStream getOut(Process self) {
        return self.getOutputStream();
    }

    /**
     * Overloads the left shift operator (&lt;&lt;) to provide an append mechanism
     * to pipe data to a Process.
     *
     * @param self  a Process instance
     * @param value a value to append
     * @return a Writer
     * @throws java.io.IOException if an IOException occurs.
     * @since 1.0
     */
    public static Writer leftShift(Process self, Object value) throws IOException {
        return IOGroovyMethods.leftShift(self.getOutputStream(), value);
    }

    /**
     * Overloads the left shift operator to provide an append mechanism
     * to pipe into a Process
     *
     * @param self  a Process instance
     * @param value data to append
     * @return an OutputStream
     * @throws java.io.IOException if an IOException occurs.
     * @since 1.0
     */
    public static OutputStream leftShift(Process self, byte[] value) throws IOException {
        return IOGroovyMethods.leftShift(self.getOutputStream(), value);
    }

    /**
     * Wait for the process to finish during a certain amount of time, otherwise stops the process.
     *
     * @param self           a Process
     * @param numberOfMillis the number of milliseconds to wait before stopping the process
     * @since 1.0
     */
    public static void waitForOrKill(Process self, long numberOfMillis) {
        ProcessRunner runnable = new ProcessRunner(self);
        Thread thread = new Thread(runnable);
        thread.start();
        runnable.waitForOrKill(numberOfMillis);
    }

    /**
     * Closes all the streams associated with the process (ignoring any IOExceptions).
     *
     * @param self a Process
     * @since 2.1
     */
    public static void closeStreams(Process self) {
        try { self.getErrorStream().close(); } catch (IOException ignore) {}
        try { self.getInputStream().close(); } catch (IOException ignore) {}
        try { self.getOutputStream().close(); } catch (IOException ignore) {}
    }

    /**
     * Gets the output and error streams from a process and reads them
     * to keep the process from blocking due to a full output buffer.
     * The stream data is thrown away but blocking due to a full output buffer is avoided.
     * Use this method if you don't care about the standard or error output and just
     * want the process to run silently - use carefully however, because since the stream
     * data is thrown away, it might be difficult to track down when something goes wrong.
     * For this, two Threads are started, so this method will return immediately.
     *
     * @param self a Process
     * @since 1.0
     */
    public static void consumeProcessOutput(Process self) {
        consumeProcessOutput(self, (OutputStream)null, (OutputStream)null);
    }

    /**
     * Gets the output and error streams from a process and reads them
     * to keep the process from blocking due to a full output buffer.
     * The processed stream data is appended to the supplied Appendable.
     * For this, two Threads are started, so this method will return immediately.
     * The threads will not be join()ed, even if waitFor() is called. To wait
     * for the output to be fully consumed call waitForProcessOutput().
     *
     * @param self a Process
     * @param output an Appendable to capture the process stdout
     * @param error an Appendable to capture the process stderr
     * @since 1.7.5
     */
    public static void consumeProcessOutput(Process self, Appendable output, Appendable error) {
        consumeProcessOutputStream(self, output);
        consumeProcessErrorStream(self, error);
    }

    /**
     * Gets the output and error streams from a process and reads them
     * to keep the process from blocking due to a full output buffer.
     * The processed stream data is appended to the supplied OutputStream.
     * For this, two Threads are started, so this method will return immediately.
     * The threads will not be join()ed, even if waitFor() is called. To wait
     * for the output to be fully consumed call waitForProcessOutput().
     *
     * @param self a Process
     * @param output an OutputStream to capture the process stdout
     * @param error an OutputStream to capture the process stderr
     * @since 1.5.2
     */
    public static void consumeProcessOutput(Process self, OutputStream output, OutputStream error) {
        consumeProcessOutputStream(self, output);
        consumeProcessErrorStream(self, error);
    }

    /**
     * Gets the output and error streams from a process and reads them
     * to keep the process from blocking due to a full output buffer.
     * The stream data is thrown away but blocking due to a full output buffer is avoided.
     * Use this method if you don't care about the standard or error output and just
     * want the process to run silently - use carefully however, because since the stream
     * data is thrown away, it might be difficult to track down when something goes wrong.
     * For this, two Threads are started, but join()ed, so we wait.
     * As implied by the waitFor... name, we also wait until we finish
     * as well. Finally, the output and error streams are closed.
     *
     * @param self a Process
     * @since 1.6.5
     */
    public static void waitForProcessOutput(Process self) {
        waitForProcessOutput(self, (OutputStream)null, (OutputStream)null);
    }

    /**
     * Gets the output and error streams from a process and reads them
     * to keep the process from blocking due to a full output buffer.
     * The processed stream data is appended to the supplied Appendable.
     * For this, two Threads are started, but join()ed, so we wait.
     * As implied by the waitFor... name, we also wait until we finish
     * as well. Finally, the input, output and error streams are closed.
     *
     * @param self a Process
     * @param output an Appendable to capture the process stdout
     * @param error an Appendable to capture the process stderr
     * @since 1.7.5
     */
    public static void waitForProcessOutput(Process self, Appendable output, Appendable error) {
        Thread tout = consumeProcessOutputStream(self, output);
        Thread terr = consumeProcessErrorStream(self, error);
        boolean interrupted = false;
        try {
            try { tout.join(); } catch (InterruptedException ignore) { interrupted = true; }
            try { terr.join(); } catch (InterruptedException ignore) { interrupted = true; }
            try { self.waitFor(); } catch (InterruptedException ignore) { interrupted = true; }
            closeStreams(self);
        } finally {
            if (interrupted) Thread.currentThread().interrupt();
        }
    }

    /**
     * Gets the output and error streams from a process and reads them
     * to keep the process from blocking due to a full output buffer.
     * The processed stream data is appended to the supplied OutputStream.
     * For this, two Threads are started, but join()ed, so we wait.
     * As implied by the waitFor... name, we also wait until we finish
     * as well. Finally, the input, output and error streams are closed.
     *
     * @param self a Process
     * @param output an OutputStream to capture the process stdout
     * @param error an OutputStream to capture the process stderr
     * @since 1.6.5
     */
    public static void waitForProcessOutput(Process self, OutputStream output, OutputStream error) {
        Thread tout = consumeProcessOutputStream(self, output);
        Thread terr = consumeProcessErrorStream(self, error);
        boolean interrupted = false;
        try {
            try { tout.join(); } catch (InterruptedException ignore) { interrupted = true; }
            try { terr.join(); } catch (InterruptedException ignore) { interrupted = true; }
            try { self.waitFor(); } catch (InterruptedException ignore) { interrupted = true; }
            closeStreams(self);
        } finally {
            if (interrupted) Thread.currentThread().interrupt();
        }
    }

    /**
     * Executes the process and waits for it to complete, capturing
     * the standard output, standard error, and exit code into a
     * {@link ProcessResult}.
     *
     * <pre>
     * def result = "ls -la".execute().waitForResult()
     * if (result.ok) println result.out
     * else System.err.println result.err
     * </pre>
     *
     * @param self a Process
     * @return a ProcessResult containing stdout, stderr, and exit code
     * @throws InterruptedException if the current thread is interrupted.
     * @since 6.0.0
     */
    public static ProcessResult waitForResult(Process self) throws InterruptedException {
        StringBuilder sout = new StringBuilder();
        StringBuilder serr = new StringBuilder();
        Thread tout = consumeProcessOutputStream(self, sout);
        Thread terr = consumeProcessErrorStream(self, serr);
        boolean interrupted = false;
        try {
            try { tout.join(); } catch (InterruptedException ignore) { interrupted = true; }
            try { terr.join(); } catch (InterruptedException ignore) { interrupted = true; }
            try { self.waitFor(); } catch (InterruptedException ignore) { interrupted = true; }
            closeStreams(self);
        } finally {
            if (interrupted) Thread.currentThread().interrupt();
        }
        return new ProcessResult(sout.toString(), serr.toString(), self.exitValue());
    }

    /**
     * Executes the process and waits for it to complete within the given timeout,
     * capturing the standard output, standard error, and exit code into a
     * {@link ProcessResult}. If the process does not complete within the timeout,
     * it is forcibly destroyed.
     *
     * <pre>
     * def result = "cmd".execute().waitForResult(30, TimeUnit.SECONDS)
     * </pre>
     *
     * @param self a Process
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return a ProcessResult containing stdout, stderr, and exit code
     * @throws InterruptedException if the current thread is interrupted.
     * @since 6.0.0
     */
    public static ProcessResult waitForResult(Process self, long timeout, TimeUnit unit) throws InterruptedException {
        StringBuilder sout = new StringBuilder();
        StringBuilder serr = new StringBuilder();
        Thread tout = consumeProcessOutputStream(self, sout);
        Thread terr = consumeProcessErrorStream(self, serr);
        boolean interrupted = false;
        try {
            try {
                boolean finished = self.waitFor(timeout, unit);
                if (!finished) {
                    self.destroyForcibly();
                    self.waitFor();
                }
            } catch (InterruptedException ignore) {
                interrupted = true;
                self.destroyForcibly();
                try { self.waitFor(); } catch (InterruptedException ie) { /* best effort */ }
            }
            try { tout.join(); } catch (InterruptedException ignore) { interrupted = true; }
            try { terr.join(); } catch (InterruptedException ignore) { interrupted = true; }
            closeStreams(self);
        } finally {
            if (interrupted) Thread.currentThread().interrupt();
        }
        return new ProcessResult(sout.toString(), serr.toString(), self.exitValue());
    }

    /**
     * Registers a closure to be called when the process terminates.
     * Uses {@link Process#onExit()} to asynchronously notify completion.
     *
     * <pre>
     * "cmd".execute().onExit { proc -&gt;
     *     println "Exited with code: ${proc.exitValue()}"
     * }
     * </pre>
     *
     * @param self a Process
     * @param action a closure to call with the completed Process
     * @return the Process to allow chaining
     * @since 6.0.0
     */
    public static Process onExit(final Process self, final Closure action) {
        self.onExit().thenAccept(action::call);
        return self;
    }

    /**
     * Gets the error stream from a process and reads it
     * to keep the process from blocking due to a full buffer.
     * The processed stream data is appended to the supplied OutputStream.
     * A new Thread is started, so this method will return immediately.
     *
     * @param self a Process
     * @param err an OutputStream to capture the process stderr
     * @return the Thread
     * @since 1.5.2
     */
    public static Thread consumeProcessErrorStream(Process self, OutputStream err) {
        Thread thread = new Thread(new ByteDumper(self.getErrorStream(), err));
        thread.start();
        return thread;
    }

    /**
     * Gets the error stream from a process and reads it
     * to keep the process from blocking due to a full buffer.
     * The processed stream data is appended to the supplied Appendable.
     * A new Thread is started, so this method will return immediately.
     *
     * @param self a Process
     * @param error an Appendable to capture the process stderr
     * @return the Thread
     * @since 1.7.5
     */
    public static Thread consumeProcessErrorStream(Process self, Appendable error) {
        Thread thread = new Thread(new TextDumper(self.getErrorStream(), error));
        thread.start();
        return thread;
    }

    /**
     * Gets the output stream from a process and reads it
     * to keep the process from blocking due to a full output buffer.
     * The processed stream data is appended to the supplied Appendable.
     * A new Thread is started, so this method will return immediately.
     *
     * @param self a Process
     * @param output an Appendable to capture the process stdout
     * @return the Thread
     * @since 1.7.5
     */
    public static Thread consumeProcessOutputStream(Process self, Appendable output) {
        Thread thread = new Thread(new TextDumper(self.getInputStream(), output));
        thread.start();
        return thread;
    }

    /**
     * Gets the output stream from a process and reads it
     * to keep the process from blocking due to a full output buffer.
     * The processed stream data is appended to the supplied OutputStream.
     * A new Thread is started, so this method will return immediately.
     *
     * @param self a Process
     * @param output an OutputStream to capture the process stdout
     * @return the Thread
     * @since 1.5.2
     */
    public static Thread consumeProcessOutputStream(Process self, OutputStream output) {
        Thread thread = new Thread(new ByteDumper(self.getInputStream(), output));
        thread.start();
        return thread;
    }

    /**
     * Creates a new BufferedWriter as stdin for this process,
     * passes it to the closure, and ensures the stream is flushed
     * and closed after the closure returns.
     * A new Thread is started, so this method will return immediately.
     *
     * @param self a Process
     * @param closure a closure
     * @since 1.5.2
     */
    public static void withWriter(final Process self, final Closure closure) {
        new Thread(() -> {
            try {
                IOGroovyMethods.withWriter(new BufferedOutputStream(getOut(self)), closure);
            } catch (IOException e) {
                throw new GroovyRuntimeException("exception while reading process stream", e);
            }
        }).start();
    }

    /**
     * Creates a new buffered OutputStream as stdin for this process,
     * passes it to the closure, and ensures the stream is flushed
     * and closed after the closure returns.
     * A new Thread is started, so this method will return immediately.
     *
     * @param self a Process
     * @param closure a closure
     * @since 1.5.2
     */
    public static void withOutputStream(final Process self, final Closure closure) {
        new Thread(() -> {
            try {
                IOGroovyMethods.withStream(new BufferedOutputStream(getOut(self)), closure);
            } catch (IOException e) {
                throw new GroovyRuntimeException("exception while reading process stream", e);
            }
        }).start();
    }

    /**
     * Allows one Process to asynchronously pipe data to another Process.
     *
     * @param left  a Process instance
     * @param right a Process to pipe output to
     * @return the second Process to allow chaining
     * @throws java.io.IOException if an IOException occurs.
     * @since 1.5.2
     */
    public static Process pipeTo(final Process left, final Process right) throws IOException {
        new Thread(() -> {
            InputStream in = new BufferedInputStream(getIn(left));
            OutputStream out = new BufferedOutputStream(getOut(right));
            byte[] buf = new byte[8192];
            int next;
            try {
                while ((next = in.read(buf)) != -1) {
                    out.write(buf, 0, next);
                }
            } catch (IOException e) {
                throw new GroovyRuntimeException("exception while reading process stream", e);
            } finally {
                closeWithWarning(out);
                closeWithWarning(in);
            }
        }).start();
        return right;
    }

    /**
     * Overrides the or operator to allow one Process to asynchronously
     * pipe data to another Process.
     *
     * @param left  a Process instance
     * @param right a Process to pipe output to
     * @return the second Process to allow chaining
     * @throws java.io.IOException if an IOException occurs.
     * @since 1.5.1
     */
    public static Process or(final Process left, final Process right) throws IOException {
        return pipeTo(left, right);
    }

    /**
     * Creates a native OS pipeline from a list of commands, using
     * {@link ProcessBuilder#startPipeline(List)}. Each element in the list
     * can be a String (tokenized on whitespace), a List (elements converted
     * to Strings via {@code toString()}), a String array, or a {@link ProcessBuilder}
     * for full control.
     *
     * <pre>
     * def procs = ["ps aux", "grep java", "wc -l"].pipeline()
     * println procs.last().text
     * </pre>
     *
     * @param commands a list of commands, each a String, List, String[], or ProcessBuilder
     * @return the list of started {@link Process} instances
     * @throws IOException if an IOException occurs.
     * @since 6.0.0
     */
    public static List<Process> pipeline(final List<?> commands) throws IOException {
        List<ProcessBuilder> builders = new ArrayList<>();
        for (Object cmd : commands) {
            if (cmd instanceof ProcessBuilder) {
                builders.add((ProcessBuilder) cmd);
            } else if (cmd instanceof String) {
                builders.add(toProcessBuilder((String) cmd));
            } else if (cmd instanceof String[]) {
                builders.add(toProcessBuilder((String[]) cmd));
            } else if (cmd instanceof List) {
                builders.add(toProcessBuilder((List) cmd));
            } else {
                throw new IllegalArgumentException(
                        "pipeline() elements must be String, List, String[], or ProcessBuilder; got: "
                                + (cmd == null ? "null" : cmd.getClass().getName()));
            }
        }
        return ProcessBuilder.startPipeline(builders);
    }

    /**
     * A Runnable which waits for a process to complete together with a notification scheme
     * allowing another thread to wait a maximum number of seconds for the process to complete
     * before killing it.
     *
     * @since 1.0
     */
    protected static class ProcessRunner implements Runnable {
        Process process;
        private boolean finished;

        public ProcessRunner(Process process) {
            this.process = process;
        }

        private void doProcessWait() {
            try {
                process.waitFor();
            } catch (InterruptedException ignore) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public void run() {
            doProcessWait();
            synchronized (this) {
                notifyAll();
                finished = true;
            }
        }

        public synchronized void waitForOrKill(long millis) {
            if (!finished) {
                try {
                    wait(millis);
                } catch (InterruptedException ignore) {
                    Thread.currentThread().interrupt();
                }
                if (!finished) {
                    process.destroy();
                    doProcessWait();
                }
            }
        }
    }

    private static class TextDumper implements Runnable {
        final InputStream in;
        final Appendable app;

        public TextDumper(InputStream in, Appendable app) {
            this.in = in;
            this.app = app;
        }

        @Override
        public void run() {
            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(isr);
            String next;
            try {
                while ((next = br.readLine()) != null) {
                    if (app != null) {
                        app.append(next);
                        app.append("\n");
                    }
                }
            } catch (IOException e) {
                throw new GroovyRuntimeException("exception while reading process stream", e);
            }
        }
    }

    private static class ByteDumper implements Runnable {
        final InputStream in;
        final OutputStream out;

        public ByteDumper(InputStream in, OutputStream out) {
            this.in = new BufferedInputStream(in);
            this.out = out;
        }

        @Override
        public void run() {
            byte[] buf = new byte[8192];
            int next;
            try {
                while ((next = in.read(buf)) != -1) {
                    if (out != null) out.write(buf, 0, next);
                }
            } catch (IOException e) {
                throw new GroovyRuntimeException("exception while dumping process stream", e);
            }
        }
    }

    /**
     * Creates a {@link ProcessBuilder} from a command line String,
     * tokenized on whitespace. This bridges Groovy's convenient string
     * syntax with ProcessBuilder's full feature set.
     *
     * <pre>
     * def pb = "find . -name '*.groovy'".toProcessBuilder()
     * pb.directory(new File("/project"))
     * pb.redirectErrorStream(true)
     * def proc = pb.start()
     * </pre>
     *
     * @param self a command line String
     * @return a ProcessBuilder ready to be configured further or started
     * @since 6.0.0
     */
    public static ProcessBuilder toProcessBuilder(final String self) {
        return new ProcessBuilder(tokenize(self));
    }

    /**
     * Creates a {@link ProcessBuilder} from a command array.
     *
     * @param commandArray an array of Strings containing the command name and parameters
     * @return a ProcessBuilder ready to be configured further or started
     * @since 6.0.0
     */
    public static ProcessBuilder toProcessBuilder(final String[] commandArray) {
        return new ProcessBuilder(commandArray);
    }

    /**
     * Creates a {@link ProcessBuilder} from a command list. The toString() method
     * is called for each item in the list to convert into a resulting String.
     *
     * @param commands a list containing the command name and parameters
     * @return a ProcessBuilder ready to be configured further or started
     * @since 6.0.0
     */
    public static ProcessBuilder toProcessBuilder(final List commands) {
        return new ProcessBuilder(stringify(commands));
    }

    /**
     * Executes the command specified by <code>self</code> as a command-line process.
     * <p>For more control over Process construction you can use
     * <code>java.lang.ProcessBuilder</code> or {@link #toProcessBuilder(String)}.
     *
     * @param self a command line String
     * @return the Process which has just started for this command line representation
     * @throws IOException if an IOException occurs.
     * @since 1.0
     */
    public static Process execute(final String self) throws IOException {
        return new ProcessBuilder(tokenize(self)).start();
    }

    /**
     * Executes the command specified by <code>self</code> with environment defined by <code>envp</code>
     * and under the working directory <code>dir</code>.
     * <p>For more control over Process construction you can use
     * <code>java.lang.ProcessBuilder</code>.
     *
     * @param self a command line String to be executed.
     * @param envp an array of Strings, each element of which
     *             has environment variable settings in the format
     *             <i>name</i>=<i>value</i>, or
     *             <tt>null</tt> if the subprocess should inherit
     *             the environment of the current process.
     * @param dir  the working directory of the subprocess, or
     *             <tt>null</tt> if the subprocess should inherit
     *             the working directory of the current process.
     * @return the Process which has just started for this command line representation.
     * @throws IOException if an IOException occurs.
     * @since 1.0
     */
    public static Process execute(final String self, final String[] envp, final File dir) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(tokenize(self));
        if (dir != null) pb.directory(dir);
        if (envp != null) {
            Map<String, String> env = pb.environment();
            env.clear();
            for (String e : envp) {
                int idx = e.indexOf('=');
                if (idx >= 0) env.put(e.substring(0, idx), e.substring(idx + 1));
            }
        }
        return pb.start();
    }

    /**
     * Executes the command specified by <code>self</code> with environment defined
     * by <code>envp</code> and under the working directory <code>dir</code>.
     * <p>For more control over Process construction you can use
     * <code>java.lang.ProcessBuilder</code>.
     *
     * @param self a command line String to be executed.
     * @param envp a List of Objects (converted to Strings using toString), each member of which
     *             has environment variable settings in the format
     *             <i>name</i>=<i>value</i>, or
     *             <tt>null</tt> if the subprocess should inherit
     *             the environment of the current process.
     * @param dir  the working directory of the subprocess, or
     *             <tt>null</tt> if the subprocess should inherit
     *             the working directory of the current process.
     * @return the Process which has just started for this command line representation.
     * @throws IOException if an IOException occurs.
     * @since 1.0
     */
    public static Process execute(final String self, final List envp, final File dir) throws IOException {
        return execute(self, stringify(envp), dir);
    }

    /**
     * Executes the command specified by the given <code>String</code> array.
     * The first item in the array is the command; the others are the parameters.
     * <p>For more control over Process construction you can use
     * <code>java.lang.ProcessBuilder</code>.
     *
     * @param commandArray an array of <code>String</code> containing the command name and
     *                     parameters as separate items in the array.
     * @return the Process which has just started for this command line representation.
     * @throws IOException if an IOException occurs.
     * @since 1.0
     */
    public static Process execute(final String[] commandArray) throws IOException {
        return Runtime.getRuntime().exec(commandArray);
    }

    /**
     * Executes the command specified by the <code>String</code> array given in the first parameter,
     * with the environment defined by <code>envp</code> and under the working directory <code>dir</code>.
     * The first item in the array is the command; the others are the parameters.
     * <p>For more control over Process construction you can use
     * <code>java.lang.ProcessBuilder</code>.
     *
     * @param commandArray an array of <code>String</code> containing the command name and
     *                     parameters as separate items in the array.
     * @param envp an array of Strings, each member of which
     *             has environment variable settings in the format
     *             <i>name</i>=<i>value</i>, or
     *             <tt>null</tt> if the subprocess should inherit
     *             the environment of the current process.
     * @param dir  the working directory of the subprocess, or
     *             <tt>null</tt> if the subprocess should inherit
     *             the working directory of the current process.
     * @return the Process which has just started for this command line representation.
     * @throws IOException if an IOException occurs.
     * @since 1.7.1
     */
    public static Process execute(final String[] commandArray, final String[] envp, final File dir) throws IOException {
        return Runtime.getRuntime().exec(commandArray, envp, dir);
    }

    /**
     * Executes the command specified by the <code>String</code> array given in the first parameter,
     * with the environment defined by <code>envp</code> and under the working directory <code>dir</code>.
     * The first item in the array is the command; the others are the parameters.
     * <p>For more control over Process construction you can use
     * <code>java.lang.ProcessBuilder</code>.
     *
     * @param commandArray an array of <code>String</code> containing the command name and
     *                     parameters as separate items in the array.
     * @param envp a List of Objects (converted to Strings using toString), each member of which
     *             has environment variable settings in the format
     *             <i>name</i>=<i>value</i>, or
     *             <tt>null</tt> if the subprocess should inherit
     *             the environment of the current process.
     * @param dir  the working directory of the subprocess, or
     *             <tt>null</tt> if the subprocess should inherit
     *             the working directory of the current process.
     * @return the Process which has just started for this command line representation.
     * @throws IOException if an IOException occurs.
     * @since 1.7.1
     */
    public static Process execute(final String[] commandArray, final List envp, final File dir) throws IOException {
        return Runtime.getRuntime().exec(commandArray, stringify(envp), dir);
    }

    /**
     * Executes the command specified by the given list. The toString() method is called
     * for each item in the list to convert into a resulting String.
     * The first item in the list is the command the others are the parameters.
     * <p>For more control over Process construction you can use
     * <code>java.lang.ProcessBuilder</code>.
     *
     * @param commands a list containing the command name and
     *                    parameters as separate items in the list.
     * @return the Process which has just started for this command line representation.
     * @throws IOException if an IOException occurs.
     * @since 1.0
     */
    public static Process execute(final List commands) throws IOException {
        return execute(stringify(commands));
    }

    /**
     * Executes the command specified by the given list,
     * with the environment defined by <code>envp</code> and under the working directory <code>dir</code>.
     * The first item in the list is the command; the others are the parameters. The toString()
     * method is called on items in the list to convert them to Strings.
     * <p>For more control over Process construction you can use
     * <code>java.lang.ProcessBuilder</code>.
     *
     * @param commands a List containing the command name and
     *                     parameters as separate items in the list.
     * @param envp an array of Strings, each member of which
     *             has environment variable settings in the format
     *             <i>name</i>=<i>value</i>, or
     *             <tt>null</tt> if the subprocess should inherit
     *             the environment of the current process.
     * @param dir  the working directory of the subprocess, or
     *             <tt>null</tt> if the subprocess should inherit
     *             the working directory of the current process.
     * @return the Process which has just started for this command line representation.
     * @throws IOException if an IOException occurs.
     * @since 1.7.1
     */
    public static Process execute(final List commands, final String[] envp, final File dir) throws IOException {
        return Runtime.getRuntime().exec(stringify(commands), envp, dir);
    }

    /**
     * Executes the command specified by the given list,
     * with the environment defined by <code>envp</code> and under the working directory <code>dir</code>.
     * The first item in the list is the command; the others are the parameters. The toString()
     * method is called on items in the list to convert them to Strings.
     * <p>For more control over Process construction you can use
     * <code>java.lang.ProcessBuilder</code>.
     *
     * @param commands a List containing the command name and
     *                     parameters as separate items in the list.
     * @param envp a List of Objects (converted to Strings using toString), each member of which
     *             has environment variable settings in the format
     *             <i>name</i>=<i>value</i>, or
     *             <tt>null</tt> if the subprocess should inherit
     *             the environment of the current process.
     * @param dir  the working directory of the subprocess, or
     *             <tt>null</tt> if the subprocess should inherit
     *             the working directory of the current process.
     * @return the Process which has just started for this command line representation.
     * @throws IOException if an IOException occurs.
     * @since 1.7.1
     */
    public static Process execute(final List commands, final List envp, final File dir) throws IOException {
        return Runtime.getRuntime().exec(stringify(commands), stringify(envp), dir);
    }

    /**
     * Executes the command specified by <code>self</code> with options provided
     * as named parameters. Supported options:
     * <ul>
     *   <li><b>dir</b>: (File, Path, or String) the working directory</li>
     *   <li><b>env</b>: (Map) environment variables to add (merged with inherited environment)</li>
     *   <li><b>redirectErrorStream</b>: (boolean) merge stderr into stdout</li>
     *   <li><b>inheritIO</b>: (boolean) inherit the parent process I/O streams</li>
     *   <li><b>outputFile</b>: (File, Path, or String) redirect stdout to a file</li>
     *   <li><b>errorFile</b>: (File, Path, or String) redirect stderr to a file</li>
     *   <li><b>inputFile</b>: (File, Path, or String) redirect stdin from a file</li>
     *   <li><b>appendOutput</b>: (File, Path, or String) append stdout to a file</li>
     *   <li><b>appendError</b>: (File, Path, or String) append stderr to a file</li>
     * </ul>
     *
     * <pre>
     * "make test".execute(dir: new File("/project"), env: [CI: "true"])
     * "cmd".execute(redirectErrorStream: true)
     * </pre>
     *
     * @param self a command line String
     * @param options a Map of options to configure the process
     * @return the Process which has just started
     * @throws IOException if an IOException occurs.
     * @since 6.0.0
     */
    public static Process execute(final String self, @NamedParams({
            @NamedParam(value = "dir"),
            @NamedParam(value = "env", type = Map.class),
            @NamedParam(value = "redirectErrorStream", type = Boolean.class),
            @NamedParam(value = "inheritIO", type = Boolean.class),
            @NamedParam(value = "outputFile"),
            @NamedParam(value = "errorFile"),
            @NamedParam(value = "inputFile"),
            @NamedParam(value = "appendOutput"),
            @NamedParam(value = "appendError")
    }) Map<String, Object> options) throws IOException {
        return configureProcessBuilder(toProcessBuilder(self), options).start();
    }

    /**
     * Executes the command specified by the given <code>String</code> array with options
     * provided as named parameters.
     *
     * @param commandArray an array of Strings containing the command name and parameters
     * @param options a Map of options to configure the process (see {@link #execute(String, Map)})
     * @return the Process which has just started
     * @throws IOException if an IOException occurs.
     * @since 6.0.0
     * @see #execute(String, Map)
     */
    public static Process execute(final String[] commandArray, @NamedParams({
            @NamedParam(value = "dir"),
            @NamedParam(value = "env", type = Map.class),
            @NamedParam(value = "redirectErrorStream", type = Boolean.class),
            @NamedParam(value = "inheritIO", type = Boolean.class),
            @NamedParam(value = "outputFile"),
            @NamedParam(value = "errorFile"),
            @NamedParam(value = "inputFile"),
            @NamedParam(value = "appendOutput"),
            @NamedParam(value = "appendError")
    }) Map<String, Object> options) throws IOException {
        return configureProcessBuilder(toProcessBuilder(commandArray), options).start();
    }

    /**
     * Executes the command specified by the given list with options
     * provided as named parameters.
     *
     * @param commands a list containing the command name and parameters
     * @param options a Map of options to configure the process (see {@link #execute(String, Map)})
     * @return the Process which has just started
     * @throws IOException if an IOException occurs.
     * @since 6.0.0
     * @see #execute(String, Map)
     */
    public static Process execute(final List commands, @NamedParams({
            @NamedParam(value = "dir"),
            @NamedParam(value = "env", type = Map.class),
            @NamedParam(value = "redirectErrorStream", type = Boolean.class),
            @NamedParam(value = "inheritIO", type = Boolean.class),
            @NamedParam(value = "outputFile"),
            @NamedParam(value = "errorFile"),
            @NamedParam(value = "inputFile"),
            @NamedParam(value = "appendOutput"),
            @NamedParam(value = "appendError")
    }) Map<String, Object> options) throws IOException {
        return configureProcessBuilder(toProcessBuilder(commands), options).start();
    }

    // just simple parsing otherwise use ProcessBuilder directly
    private static List<String> tokenize(final String command) {
        StringTokenizer st = new StringTokenizer(command);
        List<String> tokens = new ArrayList<>();
        while (st.hasMoreTokens()) {
            tokens.add(st.nextToken());
        }
        return tokens;
    }

    private static String[] stringify(final List orig) {
        if (orig == null) return null;
        String[] result = new String[orig.size()];
        for (int i = 0; i < orig.size(); i++) {
            result[i] = orig.get(i).toString();
        }
        return result;
    }

    private static File toFile(Object obj) {
        if (obj instanceof File) return (File) obj;
        if (obj instanceof Path) return ((Path) obj).toFile();
        return new File(obj.toString());
    }

    private static ProcessBuilder configureProcessBuilder(ProcessBuilder pb, Map<String, Object> options) {
        if (options == null || options.isEmpty()) return pb;

        Object dir = options.get("dir");
        if (dir != null) {
            pb.directory(toFile(dir));
        }

        Object env = options.get("env");
        if (env instanceof Map) {
            Map<String, String> pbEnv = pb.environment();
            ((Map<?, ?>) env).forEach((k, v) -> pbEnv.put(String.valueOf(k), String.valueOf(v)));
        }

        if (Boolean.TRUE.equals(options.get("redirectErrorStream"))) {
            pb.redirectErrorStream(true);
        }

        if (Boolean.TRUE.equals(options.get("inheritIO"))) {
            pb.inheritIO();
        }

        Object outputFile = options.get("outputFile");
        if (outputFile != null) {
            pb.redirectOutput(toFile(outputFile));
        }

        Object appendOutput = options.get("appendOutput");
        if (appendOutput != null) {
            pb.redirectOutput(ProcessBuilder.Redirect.appendTo(toFile(appendOutput)));
        }

        Object errorFile = options.get("errorFile");
        if (errorFile != null) {
            pb.redirectError(toFile(errorFile));
        }

        Object appendError = options.get("appendError");
        if (appendError != null) {
            pb.redirectError(ProcessBuilder.Redirect.appendTo(toFile(appendError)));
        }

        Object inputFile = options.get("inputFile");
        if (inputFile != null) {
            pb.redirectInput(toFile(inputFile));
        }

        return pb;
    }

}
