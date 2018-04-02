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

/*
 * This {@link Interceptor} traces method calls on the proxied object to a log. 
 * By default, the log is simply <pre>System.out</pre>; however, that can be 
 * changed with the <pre>setWriter(Writer)</pre> method.
 * <p>
 * A message will be written to output before a method is invoked and after a method
 * is invoked. If methods are nested, and invoke one another, then indentation 
 * of two spaces is written.
 * <p>
 * Here is an example usage on the ArrayList object: <br>
 * <pre class="groovyTestCase">
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

    public Object beforeInvoke(Object object, String methodName, Object[] arguments) {
        write(object, methodName, arguments, "before");
        indent++ ;
        return null;
    }

    public Object afterInvoke(Object object, String methodName, Object[] arguments, Object result) {
        indent--;
        write(object, methodName, arguments, "after ");
        return result;
    }

    public boolean doInvoke() {
        return true;
    }
    private String indent(){
        StringBuilder result = new StringBuilder();
        for (int i=0; i<indent;i++){
            result.append("  ");
        }
        return result.toString();
    }

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

    protected void writeInfo(final Class aClass, String methodName, Object[] arguments) throws IOException {
        writer.write(aClass.getName());
        writer.write(".");
        writer.write(methodName);
        writer.write("(");
        for (int i = 0; i < arguments.length; i++) {
            if (i > 0) writer.write(", ");
            Object argument = arguments[i];
            writer.write(argument.getClass().getName());
        }
        writer.write(")");
    }
}
