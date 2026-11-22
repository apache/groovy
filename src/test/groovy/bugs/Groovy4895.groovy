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
package bugs

import groovy.lang.Binding
import groovy.lang.GroovyShell
import org.junit.jupiter.api.Test

import javax.tools.ToolProvider

/**
 * GROOVY-4895: {@link GroovyShell} constructed with a custom
 * {@link ClassLoader} used to resolve JDBC drivers against a Groovy
 * {@code CallSiteClassLoader} instead of that loader. {@code Class.forName}
 * of a driver registered in the custom loader then failed with
 * {@code SQLException: No suitable driver found} on
 * {@code DriverManager.getConnection}.
 */
final class Groovy4895 {

    @Test
    void testDriverManagerGetConnectionUsesSpecifiedClassLoader() {
        def dir = File.createTempDir()
        try {
            new File(dir, 'DummyDriver.java').write dummyDriverSource
            def compiler = ToolProvider.getSystemJavaCompiler()
            assert compiler != null, 'JDK java compiler required'
            def src = new File(dir, 'DummyDriver.java')
            int rc = compiler.run(null, null, null, '-d', dir.absolutePath, src.absolutePath)
            assert rc == 0

            def loader = new URLClassLoader([dir.toURI().toURL()] as URL[], GroovyShell.class.classLoader)
            def shell = new GroovyShell(loader, new Binding())
            def result = shell.evaluate('''
                import java.sql.DriverManager
                Class.forName('DummyDriver')
                def conn = DriverManager.getConnection('jdbc:dummy:test')
                conn.getClass().name
            ''')
            assert result == 'DummyDriver$DummyConnection'
        } finally {
            dir.deleteDir()
        }
    }

    private static final String dummyDriverSource = '''
        import java.sql.*;
        import java.util.Properties;
        import java.util.logging.Logger;
        public class DummyDriver implements Driver {
          static {
            try { DriverManager.registerDriver(new DummyDriver()); }
            catch (SQLException e) { throw new ExceptionInInitializerError(e); }
          }
          public Connection connect(String url, Properties info) {
            if (!acceptsURL(url)) return null;
            return new DummyConnection();
          }
          public boolean acceptsURL(String url) {
            return url != null && url.startsWith("jdbc:dummy:");
          }
          public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
            return new DriverPropertyInfo[0];
          }
          public int getMajorVersion() { return 1; }
          public int getMinorVersion() { return 0; }
          public boolean jdbcCompliant() { return false; }
          public Logger getParentLogger() { return Logger.getLogger("DummyDriver"); }
          static class DummyConnection implements Connection {
            public Statement createStatement() { throw new UnsupportedOperationException(); }
            public PreparedStatement prepareStatement(String sql) { throw new UnsupportedOperationException(); }
            public CallableStatement prepareCall(String sql) { throw new UnsupportedOperationException(); }
            public String nativeSQL(String sql) { return sql; }
            public void setAutoCommit(boolean autoCommit) {}
            public boolean getAutoCommit() { return true; }
            public void commit() {}
            public void rollback() {}
            public void close() {}
            public boolean isClosed() { return false; }
            public DatabaseMetaData getMetaData() { throw new UnsupportedOperationException(); }
            public void setReadOnly(boolean readOnly) {}
            public boolean isReadOnly() { return false; }
            public void setCatalog(String catalog) {}
            public String getCatalog() { return null; }
            public void setTransactionIsolation(int level) {}
            public int getTransactionIsolation() { return Connection.TRANSACTION_NONE; }
            public SQLWarning getWarnings() { return null; }
            public void clearWarnings() {}
            public Statement createStatement(int a, int b) { throw new UnsupportedOperationException(); }
            public PreparedStatement prepareStatement(String s, int a, int b) { throw new UnsupportedOperationException(); }
            public CallableStatement prepareCall(String s, int a, int b) { throw new UnsupportedOperationException(); }
            public java.util.Map<String,Class<?>> getTypeMap() { return java.util.Collections.emptyMap(); }
            public void setTypeMap(java.util.Map<String,Class<?>> map) {}
            public void setHoldability(int holdability) {}
            public int getHoldability() { return 0; }
            public Savepoint setSavepoint() { return null; }
            public Savepoint setSavepoint(String name) { return null; }
            public void rollback(Savepoint savepoint) {}
            public void releaseSavepoint(Savepoint savepoint) {}
            public Statement createStatement(int a, int b, int c) { throw new UnsupportedOperationException(); }
            public PreparedStatement prepareStatement(String s, int a, int b, int c) { throw new UnsupportedOperationException(); }
            public CallableStatement prepareCall(String s, int a, int b, int c) { throw new UnsupportedOperationException(); }
            public PreparedStatement prepareStatement(String s, int a) { throw new UnsupportedOperationException(); }
            public PreparedStatement prepareStatement(String s, int[] a) { throw new UnsupportedOperationException(); }
            public PreparedStatement prepareStatement(String s, String[] a) { throw new UnsupportedOperationException(); }
            public Clob createClob() { return null; }
            public Blob createBlob() { return null; }
            public NClob createNClob() { return null; }
            public SQLXML createSQLXML() { return null; }
            public boolean isValid(int timeout) { return true; }
            public void setClientInfo(String name, String value) {}
            public void setClientInfo(Properties properties) {}
            public String getClientInfo(String name) { return null; }
            public Properties getClientInfo() { return new Properties(); }
            public Array createArrayOf(String typeName, Object[] elements) { return null; }
            public Struct createStruct(String typeName, Object[] attributes) { return null; }
            public void setSchema(String schema) {}
            public String getSchema() { return null; }
            public void abort(java.util.concurrent.Executor executor) {}
            public void setNetworkTimeout(java.util.concurrent.Executor executor, int milliseconds) {}
            public int getNetworkTimeout() { return 0; }
            public <T> T unwrap(Class<T> iface) { throw new UnsupportedOperationException(); }
            public boolean isWrapperFor(Class<?> iface) { return false; }
          }
        }
    '''
}
