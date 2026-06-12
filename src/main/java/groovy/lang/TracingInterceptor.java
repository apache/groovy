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
package groovy.lang;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * This {@link Interceptor} traces method calls on the proxied object to a log.
 * By default, the log is simply <pre>System.out</pre>; however, that can be
 * changed with the <pre>setWriter(Writer)</pre> method.
 * <p>
 * A message will be written to output before a method is invoked and after a method
 * is invoked. If methods are nested, and invoke one another, then indentation
 * of two spaces is written.
 * <p>
 * Here is an example usage on the ArrayList object: <br>
 * <pre class="language-groovy groovyTestCase">
 * def proxy = ProxyMetaClass.getInstance(ArrayList.class)
 * proxy.interceptor = new TracingInterceptor()
 * proxy.use {
 *     def list = [1, 2, 3]
 *     assert 3 == list.size()
 *     assert list.contains(1)
 * }
 * </pre>
 * Running this code produces this output:
 * <pre>
 * before java.util.ArrayList.size()
 * after  java.util.ArrayList.size()
 * before java.util.ArrayList.contains(java.lang.Integer)
 * after  java.util.ArrayList.contains(java.lang.Integer)
 * </pre>
 */
public class TracingInterceptor implements Interceptor {

    /**
     * Writer used to emit trace output.
     */
    protected Writer writer = new PrintWriter(System.out);
    private int indent = 0;

    /**
    * Returns the writer associated with this interceptor.
    */
    public Writer getWriter() {
        return writer;
    }

    /**
    * Changes the writer associated with this interceptor.
    */
    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object beforeInvoke(Object object, String methodName, Object[] arguments) {
        write(object, methodName, arguments, "before");
        indent++ ;
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object afterInvoke(Object object, String methodName, Object[] arguments, Object result) {
        indent--;
        write(object, methodName, arguments, "after ");
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean doInvoke() {
        return true;
    }
    private String indent(){
        return "  ".repeat(Math.max(0, indent));
    }

    /**
     * Writes a formatted trace line for the supplied invocation stage.
     *
     * @param object the receiver object
     * @param methodName the invoked method name
     * @param arguments the invocation arguments
     * @param origin the trace prefix to write
     */
    protected void write(Object object, String methodName, Object[] arguments, final String origin) {
        try {
            writer.write(indent());
            writer.write(origin);
            writer.write(" ");
            Class theClass = object instanceof Class ? (Class) object: object.getClass();
            writeInfo(theClass, methodName, arguments);
            writer.write("\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes a trace line describing the intercepted method invocation.
     *
     * @param aClass the declaring or receiver class to report
     * @param methodName the intercepted method name
     * @param arguments the intercepted arguments
     * @throws IOException if the trace output cannot be written
     */
    protected void writeInfo(final Class aClass, final String methodName, final Object[] arguments) throws IOException {
        String argumentTypes = java.util.stream.Stream.of(arguments)
                .map(arg -> arg != null ? arg.getClass().getName() : "java.lang.Object") // GROOVY-10009
                .collect(java.util.stream.Collectors.joining(", "));
        String result = aClass.getName() + '.' + methodName + '(' + argumentTypes + ')';
        writer.write(result);
    }
}
