/*
 * $Id$
 * 
 * Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.
 * 
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met: 1. Redistributions of source code must retain
 * copyright statements and notices. Redistributions must also contain a copy
 * of this document. 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the distribution. 3.
 * The name "groovy" must not be used to endorse or promote products derived
 * from this Software without prior written permission of The Codehaus. For
 * written permission, please contact info@codehaus.org. 4. Products derived
 * from this Software may not be called "groovy" nor may "groovy" appear in
 * their names without prior written permission of The Codehaus. "groovy" is a
 * registered trademark of The Codehaus. 5. Due credit should be given to The
 * Codehaus - http://groovy.codehaus.org/
 * 
 * THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *  
 */
package groovy.sql;

import groovy.lang.Closure;
import groovy.lang.GString;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * Represents an extent of objects
 * 
 * @author Chris Stevenson
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class Sql {

    protected Logger log = Logger.getLogger(getClass().getName());

    private DataSource dataSource;
    private Connection useConnection;

    /**
     * A helper method which creates a new Sql instance from a JDBC connection URL
     * 
     * @param url
     * @return a new Sql instance with a connection
     */
    public static Sql newInstance(String url) throws SQLException {
        Connection connection = DriverManager.getConnection(url);
        return new Sql(connection);
    }

    /**
     * A helper method which creates a new Sql instance from a JDBC connection URL
     * 
     * @param url
     * @return a new Sql instance with a connection
     */
    public static Sql newInstance(String url, Properties properties) throws SQLException {
        Connection connection = DriverManager.getConnection(url, properties);
        return new Sql(connection);
    }

    /**
     * A helper method which creates a new Sql instance from a JDBC connection URL
     * and driver class name
     * 
     * @param url
     * @return a new Sql instance with a connection
     */
    public static Sql newInstance(String url, Properties properties, String driverClassName)
        throws SQLException, ClassNotFoundException {
        loadDriver(driverClassName);
        return newInstance(url, properties);
    }

    /**
     * A helper method which creates a new Sql instance from a JDBC connection URL, username and password
     * 
     * @param url
     * @return a new Sql instance with a connection
     */
    public static Sql newInstance(String url, String user, String password) throws SQLException {
        Connection connection = DriverManager.getConnection(url, user, password);
        return new Sql(connection);
    }

    /**
     * A helper method which creates a new Sql instance from a JDBC connection URL, username, password
     * and driver class name
     * 
     * @param url
     * @return a new Sql instance with a connection
     */
    public static Sql newInstance(String url, String user, String password, String driverClassName)
        throws SQLException, ClassNotFoundException {
        loadDriver(driverClassName);
        return newInstance(url, user, password);
    }

    /**
     * A helper method which creates a new Sql instance from a JDBC connection URL
     * and driver class name
     * 
     * @param url
     * @param driverClassName the class name of the driver
     * @return a new Sql instance with a connection
     */
    public static Sql newInstance(String url, String driverClassName) throws SQLException, ClassNotFoundException {
        loadDriver(driverClassName);
        return newInstance(url);
    }

    /**
     * Attempts to load the JDBC driver on the thread, current or system class loaders
     * 
     * @param driverClassName
     * @throws ClassNotFoundException
     */
    public static void loadDriver(String driverClassName) throws ClassNotFoundException {
        // lets try the thread context class loader first
        // lets try to use the system class loader
        try {
            Class.forName(driverClassName);
        }
        catch (ClassNotFoundException e) {
            try {
                Thread.currentThread().getContextClassLoader().loadClass(driverClassName);
            }
            catch (ClassNotFoundException e2) {
                // now lets try the classloader which loaded us
                try {
                    Sql.class.getClassLoader().loadClass(driverClassName);
                }
                catch (ClassNotFoundException e3) {
                    throw e;
                }
            }
        }
    }

    /**
     * Constructs an SQL instance using the given DataSource. 
     * Each operation will use a Connection
     * from the DataSource pool and close it when the operation is completed
     * putting it back into the pool.
     * 
     * @param dataSource
     */
    public Sql(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Construts an SQL instance using the given Connection.
     * It is the callers responsibility to close the Connection after 
     * the Sql instance has been used. You can do this on the connection
     * object directly or by calling the {@link close()} method.
     * 
     * @param connection
     */
    public Sql(Connection connection) {
        if (connection == null) {
            throw new NullPointerException("Must specify a non-null Connection");
        }
        this.useConnection = connection;
    }

    public Sql(Sql parent) {
        this.dataSource = parent.dataSource;
        this.useConnection = parent.useConnection;
    }

    public DataSet dataSet(String table) {
        return new DataSet(this, table);
    }

    public DataSet dataSet(Class type) {
        return new DataSet(this, type);
    }

    /**
     * Performs the given SQL query calling the closure with the result set
     */
    public void query(String sql, Closure closure) throws SQLException {
        Connection connection = createConnection();
        Statement statement = connection.createStatement();
        ResultSet results = null;
        try {
            log.fine(sql);
            results = statement.executeQuery(sql);
            closure.call(results);
        }
        catch (SQLException e) {
            log.log(Level.WARNING, "Failed to execute: " + sql, e);
            throw e;
        }
        finally {
            closeResources(connection, statement, results);
        }
    }

    /**
     * Performs the given SQL query with parameters calling the closure with
     * the result set
     */
    public void query(String sql, List params, Closure closure) throws SQLException {
        Connection connection = createConnection();
        PreparedStatement statement = null;
        ResultSet results = null;
        try {
            log.fine(sql);
            statement = connection.prepareStatement(sql);
            setParameters(params, statement);
            results = statement.executeQuery();
            closure.call(results);
        }
        catch (SQLException e) {
            log.log(Level.WARNING, "Failed to execute: " + sql, e);
            throw e;
        }
        finally {
            closeResources(connection, statement, results);
        }
    }

    /**
     * Performs the given SQL query calling the closure with the result set
     */
    public void query(GString gstring, Closure closure) throws SQLException {
        String sql = asSql(gstring);
        List params = getParameters(gstring);
        query(sql, params, closure);
    }

    /**
     * Performs the given SQL query calling the closure with each row of the
     * result set
     */
    public void queryEach(String sql, Closure closure) throws SQLException {
        Connection connection = createConnection();
        Statement statement = connection.createStatement();
        ResultSet results = null;
        try {
            log.fine(sql);
            results = statement.executeQuery(sql);

            GroovyResultSet groovyRS = new GroovyResultSet(results);
            while (groovyRS.next()) {
                closure.call(groovyRS);
            }
        }
        catch (SQLException e) {
            log.log(Level.WARNING, "Failed to execute: " + sql, e);
            throw e;
        }
        finally {
            closeResources(connection, statement, results);
        }
    }

    /**
     * Performs the given SQL query calling the closure with the result set
     */
    public void queryEach(String sql, List params, Closure closure) throws SQLException {
        Connection connection = createConnection();
        PreparedStatement statement = null;
        ResultSet results = null;
        try {
            log.fine(sql);
            statement = connection.prepareStatement(sql);
            setParameters(params, statement);
            results = statement.executeQuery();

            GroovyResultSet groovyRS = new GroovyResultSet(results);
            while (groovyRS.next()) {
                closure.call(groovyRS);
            }
        }
        catch (SQLException e) {
            log.log(Level.WARNING, "Failed to execute: " + sql, e);
            throw e;
        }
        finally {
            closeResources(connection, statement, results);
        }
    }

    /**
     * Performs the given SQL query calling the closure with the result set
     */
    public void queryEach(GString gstring, Closure closure) throws SQLException {
        String sql = asSql(gstring);
        List params = getParameters(gstring);
        queryEach(sql, params, closure);

    }

    /**
     * Executes the given piece of SQL
     */
    public boolean execute(String sql) throws SQLException {
        Connection connection = createConnection();
        Statement statement = null;
        try {
            log.fine(sql);
            statement = connection.createStatement();
            return statement.execute(sql);
        }
        catch (SQLException e) {
            log.log(Level.WARNING, "Failed to execute: " + sql, e);
            throw e;
        }
        finally {
            closeResources(connection, statement);
        }
    }

    /**
     * Executes the given SQL update
     * 
     * @return the number of rows updated
     */
    public int executeUpdate(String sql) throws SQLException {
        Connection connection = createConnection();
        Statement statement = null;
        try {
            log.fine(sql);
            statement = connection.createStatement();
            return statement.executeUpdate(sql);
        }
        catch (SQLException e) {
            log.log(Level.WARNING, "Failed to execute: " + sql, e);
            throw e;
        }
        finally {
            closeResources(connection, statement);
        }
    }

    /**
     * Executes the given piece of SQL with parameters
     */
    public boolean execute(String sql, List params) throws SQLException {
        Connection connection = createConnection();
        PreparedStatement statement = null;
        try {
            log.fine(sql);
            statement = connection.prepareStatement(sql);
            setParameters(params, statement);
            return statement.execute();
        }
        catch (SQLException e) {
            log.log(Level.WARNING, "Failed to execute: " + sql, e);
            throw e;
        }
        finally {
            closeResources(connection, statement);
        }
    }

    /**
     * Executes the given SQL update with parameters
     * 
     * @return the number of rows updated
     */
    public int executeUpdate(String sql, List params) throws SQLException {
        Connection connection = createConnection();
        PreparedStatement statement = null;
        try {
            log.fine(sql);
            statement = connection.prepareStatement(sql);
            setParameters(params, statement);
            return statement.executeUpdate();
        }
        catch (SQLException e) {
            log.log(Level.WARNING, "Failed to execute: " + sql, e);
            throw e;
        }
        finally {
            closeResources(connection, statement);
        }
    }

    /**
     * Executes the given SQL with embedded expressions inside
     */
    public boolean execute(GString gstring) throws SQLException {
        String sql = asSql(gstring);
        List params = getParameters(gstring);
        return execute(sql, params);
    }

    /**
     * Executes the given SQL update with embedded expressions inside
     * 
     * @return the number of rows updated
     */
    public int executeUpdate(GString gstring) throws SQLException {
        String sql = asSql(gstring);
        List params = getParameters(gstring);
        return executeUpdate(sql, params);
    }

    /**
     * Performs a stored procedure call
     */
    public int call(String sql) throws Exception {
        return call(sql, Collections.EMPTY_LIST);
    }

    /**
     * Performs a stored procedure call with the given parameters
     */
    public int call(String sql, List params) throws Exception {
        Connection connection = createConnection();
        CallableStatement statement = connection.prepareCall(sql);
        try {
            log.fine(sql);
            setParameters(params, statement);
            return statement.executeUpdate();
        }
        catch (SQLException e) {
            log.log(Level.WARNING, "Failed to execute: " + sql, e);
            throw e;
        }
        finally {
            closeResources(connection, statement);
        }
    }

    /**
     * Performs a stored procedure call with the given parameters
     */
    public int call(GString gstring) throws Exception {
        String sql = asSql(gstring);
        List params = getParameters(gstring);
        return call(sql, params);
    }

    /**
     * If this SQL object was created with a Connection then this method
     * closes the connection. If this SQL object was created from a DataSource
     * then this method does nothing.
     * 
     * @throws SQLException
     */
    public void close() throws SQLException {
        if (useConnection != null) {
            useConnection.close();
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * @return the SQL version of the given query using ? instead of any
     *         parameter
     */
    protected String asSql(GString gstring) {
        String[] strings = gstring.getStrings();
        if (strings.length <= 0) {
            throw new IllegalArgumentException("No SQL specified in GString: " + gstring);
        }
        Object[] values = gstring.getValues();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < strings.length; i++) {
            buffer.append(strings[i]);
            if (i < values.length) {
                buffer.append("?");
            }
        }
        return buffer.toString();
    }

    /**
     * @return extracts the parameters from the expression as a List
     */
    protected List getParameters(GString gstring) {
        Object[] values = gstring.getValues();
        List answer = new ArrayList(values.length);
        for (int i = 0; i < values.length; i++) {
            answer.add(values[i]);
        }
        return answer;
    }

    /**
     * Appends the parameters to the given statement
     */
    protected void setParameters(List params, PreparedStatement statement) throws SQLException {
        int i = 1;
        for (Iterator iter = params.iterator(); iter.hasNext();) {
            Object value = iter.next();
            setObject(statement, i++, value);
        }
    }

    /**
     * Strategy method allowing derived classes to handle types differently
     * such as for CLOBs etc.
     */
    protected void setObject(PreparedStatement statement, int i, Object value) throws SQLException {
        statement.setObject(i, value);
    }

    protected Connection createConnection() throws SQLException {
        if (dataSource != null) {
            return dataSource.getConnection();
        }
        else {
            //System.out.println("createConnection returning: " + useConnection);
            return useConnection;
        }
    }

    protected void closeResources(Connection connection, Statement statement, ResultSet results) {
        if (results != null) {
            try {
                results.close();
            }
            catch (SQLException e) {
                log.log(Level.SEVERE, "Caught exception closing resultSet: " + e, e);
            }
        }
        closeResources(connection, statement);
    }

    protected void closeResources(Connection connection, Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            }
            catch (SQLException e) {
                log.log(Level.SEVERE, "Caught exception closing statement: " + e, e);
            }
        }
        if (dataSource != null) {
            try {
                connection.close();
            }
            catch (SQLException e) {
                log.log(Level.SEVERE, "Caught exception closing connection: " + e, e);
            }
        }
    }
}
