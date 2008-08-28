/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.sql;

import groovy.lang.Closure;
import groovy.lang.GString;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;

/**
 * Represents an extent of objects
 *
 * @author Chris Stevenson
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Paul King
 * @author Marc DeXeT
 * @version $Revision$
 */
public class Sql {

    protected Logger log = Logger.getLogger(getClass().getName());

    private DataSource dataSource;

    private Connection useConnection;

    private int resultSetType = ResultSet.TYPE_FORWARD_ONLY;
    private int resultSetConcurrency = ResultSet.CONCUR_READ_ONLY;
    private int resultSetHoldability = -1;

    // store the last row count for executeUpdate
    int updateCount = 0;

    /**
     * allows a closure to be used to configure the statement before its use
     */
    private Closure configureStatement;

    /**
     * property for allowing statements caching feature
     */
    private boolean cacheStatements;

    /**
     * Statement cache
     */
    private final Map<String, Statement> statementCache = new HashMap<String, Statement>();

    /**
     * property for allowing connection caching feature
     */
    private boolean cacheConnection;

    /**
     * Creates a new Sql instance given a JDBC connection URL.
     *
     * @param url a database url of the form
     *            <code> jdbc:<em>subprotocol</em>:<em>subname</em></code>
     * @return a new Sql instance with a connection
     * @throws SQLException if a database access error occurs
     */
    public static Sql newInstance(String url) throws SQLException {
        Connection connection = DriverManager.getConnection(url);
        return new Sql(connection);
    }

    /**
     * Creates a new Sql instance given a JDBC connection URL
     * and some properties.
     *
     * @param url        a database url of the form
     *                   <code> jdbc:<em>subprotocol</em>:<em>subname</em></code>
     * @param properties a list of arbitrary string tag/value pairs
     *                   as connection arguments; normally at least a "user" and
     *                   "password" property should be included
     * @return a new Sql instance with a connection
     * @throws SQLException if a database access error occurs
     */
    public static Sql newInstance(String url, Properties properties) throws SQLException {
        Connection connection = DriverManager.getConnection(url, properties);
        return new Sql(connection);
    }

    /**
     * Creates a new Sql instance given a JDBC connection URL,
     * some properties and a driver class name.
     *
     * @param url             a database url of the form
     *                        <code> jdbc:<em>subprotocol</em>:<em>subname</em></code>
     * @param properties      a list of arbitrary string tag/value pairs
     *                        as connection arguments; normally at least a "user" and
     *                        "password" property should be included
     * @param driverClassName the fully qualified class name of the driver class
     * @return a new Sql instance with a connection
     * @throws SQLException           if a database access error occurs
     * @throws ClassNotFoundException if the class cannot be found or loaded
     */
    public static Sql newInstance(String url, Properties properties, String driverClassName)
            throws SQLException, ClassNotFoundException {
        loadDriver(driverClassName);
        return newInstance(url, properties);
    }

    /**
     * Creates a new Sql instance given a JDBC connection URL,
     * a username and a password.
     *
     * @param url      a database url of the form
     *                 <code> jdbc:<em>subprotocol</em>:<em>subname</em></code>
     * @param user     the database user on whose behalf the connection
     *                 is being made
     * @param password the user's password
     * @return a new Sql instance with a connection
     * @throws SQLException if a database access error occurs
     */
    public static Sql newInstance(String url, String user, String password) throws SQLException {
        Connection connection = DriverManager.getConnection(url, user, password);
        return new Sql(connection);
    }

    /**
     * Creates a new Sql instance given a JDBC connection URL,
     * a username, a password and a driver class name.
     *
     * @param url             a database url of the form
     *                        <code> jdbc:<em>subprotocol</em>:<em>subname</em></code>
     * @param user            the database user on whose behalf the connection
     *                        is being made
     * @param password        the user's password
     * @param driverClassName the fully qualified class name of the driver class
     * @return a new Sql instance with a connection
     * @throws SQLException           if a database access error occurs
     * @throws ClassNotFoundException if the class cannot be found or loaded
     */
    public static Sql newInstance(String url, String user, String password, String driverClassName) throws SQLException,
            ClassNotFoundException {
        loadDriver(driverClassName);
        return newInstance(url, user, password);
    }

    /**
     * Creates a new Sql instance given a JDBC connection URL
     * and a driver class name.
     *
     * @param url             a database url of the form
     *                        <code> jdbc:<em>subprotocol</em>:<em>subname</em></code>
     * @param driverClassName the fully qualified class name of the driver class
     * @return a new Sql instance with a connection
     * @throws SQLException           if a database access error occurs
     * @throws ClassNotFoundException if the class cannot be found or loaded
     */
    public static Sql newInstance(String url, String driverClassName) throws SQLException, ClassNotFoundException {
        loadDriver(driverClassName);
        return newInstance(url);
    }

    /**
     * Gets the resultSetType for statements created using the connection.
     *
     * @return the current resultSetType value
     */
    public int getResultSetType() {
        return resultSetType;
    }

    /**
     * Sets the resultSetType for statements created using the connection.
     * May cause SQLFeatureNotSupportedException exceptions to occur if the
     * underlying database doesn't support the requested type value.
     *
     * @param resultSetType one of the following <code>ResultSet</code>
     *        constants:
     *         <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *         <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *         <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     */
    public void setResultSetType(int resultSetType) {
        this.resultSetType = resultSetType;
    }

    /**
     * Gets the resultSetConcurrency for statements created using the connection.
     *
     * @return the current resultSetConcurrency value
     */
    public int getResultSetConcurrency() {
        return resultSetConcurrency;
    }

    /**
     * Sets the resultSetConcurrency for statements created using the connection.
     * May cause SQLFeatureNotSupportedException exceptions to occur if the
     * underlying database doesn't support the requested concurrency value.
     *
     * @param resultSetConcurrency one of the following <code>ResultSet</code>
     *        constants:
     *         <code>ResultSet.CONCUR_READ_ONLY</code> or
     *         <code>ResultSet.CONCUR_UPDATABLE</code>
     */
    public void setResultSetConcurrency(int resultSetConcurrency) {
        this.resultSetConcurrency = resultSetConcurrency;
    }

    /**
     * Gets the resultSetHoldability for statements created using the connection.
     *
     * @return the current resultSetHoldability value or -1 if not set
     */
    public int getResultSetHoldability() {
        return resultSetHoldability;
    }

    /**
     * Sets the resultSetHoldability for statements created using the connection.
     * May cause SQLFeatureNotSupportedException exceptions to occur if the
     * underlying database doesn't support the requested holdability value.
     *
     * @param resultSetHoldability one of the following <code>ResultSet</code>
     *        constants:
     *         <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code> or
     *         <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     */
    public void setResultSetHoldability(int resultSetHoldability) {
        this.resultSetHoldability = resultSetHoldability;
    }

    /**
     * Attempts to load the JDBC driver on the thread, current or system class
     * loaders
     *
     * @param driverClassName the fully qualified class name of the driver class
     * @throws ClassNotFoundException if the class cannot be found or loaded
     */
    public static void loadDriver(String driverClassName) throws ClassNotFoundException {
        // let's try the thread context class loader first
        // let's try to use the system class loader
        try {
            Class.forName(driverClassName);
        }
        catch (ClassNotFoundException e) {
            try {
                Thread.currentThread().getContextClassLoader().loadClass(driverClassName);
            }
            catch (ClassNotFoundException e2) {
                // now let's try the classloader which loaded us
                try {
                    Sql.class.getClassLoader().loadClass(driverClassName);
                }
                catch (ClassNotFoundException e3) {
                    throw e;
                }
            }
        }
    }

    public static final OutParameter ARRAY         = new OutParameter(){ public int getType() { return Types.ARRAY; }};
    public static final OutParameter BIGINT        = new OutParameter(){ public int getType() { return Types.BIGINT; }};
    public static final OutParameter BINARY        = new OutParameter(){ public int getType() { return Types.BINARY; }};
    public static final OutParameter BIT           = new OutParameter(){ public int getType() { return Types.BIT; }};
    public static final OutParameter BLOB          = new OutParameter(){ public int getType() { return Types.BLOB; }};
    public static final OutParameter BOOLEAN       = new OutParameter(){ public int getType() { return Types.BOOLEAN; }};
    public static final OutParameter CHAR          = new OutParameter(){ public int getType() { return Types.CHAR; }};
    public static final OutParameter CLOB          = new OutParameter(){ public int getType() { return Types.CLOB; }};
    public static final OutParameter DATALINK      = new OutParameter(){ public int getType() { return Types.DATALINK; }};
    public static final OutParameter DATE          = new OutParameter(){ public int getType() { return Types.DATE; }};
    public static final OutParameter DECIMAL       = new OutParameter(){ public int getType() { return Types.DECIMAL; }};
    public static final OutParameter DISTINCT      = new OutParameter(){ public int getType() { return Types.DISTINCT; }};
    public static final OutParameter DOUBLE        = new OutParameter(){ public int getType() { return Types.DOUBLE; }};
    public static final OutParameter FLOAT         = new OutParameter(){ public int getType() { return Types.FLOAT; }};
    public static final OutParameter INTEGER       = new OutParameter(){ public int getType() { return Types.INTEGER; }};
    public static final OutParameter JAVA_OBJECT   = new OutParameter(){ public int getType() { return Types.JAVA_OBJECT; }};
    public static final OutParameter LONGVARBINARY = new OutParameter(){ public int getType() { return Types.LONGVARBINARY; }};
    public static final OutParameter LONGVARCHAR   = new OutParameter(){ public int getType() { return Types.LONGVARCHAR; }};
    public static final OutParameter NULL          = new OutParameter(){ public int getType() { return Types.NULL; }};
    public static final OutParameter NUMERIC       = new OutParameter(){ public int getType() { return Types.NUMERIC; }};
    public static final OutParameter OTHER         = new OutParameter(){ public int getType() { return Types.OTHER; }};
    public static final OutParameter REAL          = new OutParameter(){ public int getType() { return Types.REAL; }};
    public static final OutParameter REF           = new OutParameter(){ public int getType() { return Types.REF; }};
    public static final OutParameter SMALLINT      = new OutParameter(){ public int getType() { return Types.SMALLINT; }};
    public static final OutParameter STRUCT        = new OutParameter(){ public int getType() { return Types.STRUCT; }};
    public static final OutParameter TIME          = new OutParameter(){ public int getType() { return Types.TIME; }};
    public static final OutParameter TIMESTAMP     = new OutParameter(){ public int getType() { return Types.TIMESTAMP; }};
    public static final OutParameter TINYINT       = new OutParameter(){ public int getType() { return Types.TINYINT; }};
    public static final OutParameter VARBINARY     = new OutParameter(){ public int getType() { return Types.VARBINARY; }};
    public static final OutParameter VARCHAR       = new OutParameter(){ public int getType() { return Types.VARCHAR; }};

    public static InParameter ARRAY(Object value) { return in(Types.ARRAY, value); }
    public static InParameter BIGINT(Object value) { return in(Types.BIGINT, value); }
    public static InParameter BINARY(Object value) { return in(Types.BINARY, value); }
    public static InParameter BIT(Object value) { return in(Types.BIT, value); }
    public static InParameter BLOB(Object value) { return in(Types.BLOB, value); }
    public static InParameter BOOLEAN(Object value) { return in(Types.BOOLEAN, value); }
    public static InParameter CHAR(Object value) { return in(Types.CHAR, value); }
    public static InParameter CLOB(Object value) { return in(Types.CLOB, value); }
    public static InParameter DATALINK(Object value) { return in(Types.DATALINK, value); }
    public static InParameter DATE(Object value) { return in(Types.DATE, value); }
    public static InParameter DECIMAL(Object value) { return in(Types.DECIMAL, value); }
    public static InParameter DISTINCT(Object value) { return in(Types.DISTINCT, value); }
    public static InParameter DOUBLE(Object value) { return in(Types.DOUBLE, value); }
    public static InParameter FLOAT(Object value) { return in(Types.FLOAT, value); }
    public static InParameter INTEGER(Object value) { return in(Types.INTEGER, value); }
    public static InParameter JAVA_OBJECT(Object value) { return in(Types.JAVA_OBJECT, value); }
    public static InParameter LONGVARBINARY(Object value) { return in(Types.LONGVARBINARY, value); }
    public static InParameter LONGVARCHAR(Object value) { return in(Types.LONGVARCHAR, value); }
    public static InParameter NULL(Object value) { return in(Types.NULL, value); }
    public static InParameter NUMERIC(Object value) { return in(Types.NUMERIC, value); }
    public static InParameter OTHER(Object value) { return in(Types.OTHER, value); }
    public static InParameter REAL(Object value) { return in(Types.REAL, value); }
    public static InParameter REF(Object value) { return in(Types.REF, value); }
    public static InParameter SMALLINT(Object value) { return in(Types.SMALLINT, value); }
    public static InParameter STRUCT(Object value) { return in(Types.STRUCT, value); }
    public static InParameter TIME(Object value) { return in(Types.TIME, value); }
    public static InParameter TIMESTAMP(Object value) { return in(Types.TIMESTAMP, value); }
    public static InParameter TINYINT(Object value) { return in(Types.TINYINT, value); }
    public static InParameter VARBINARY(Object value) { return in(Types.VARBINARY, value); }
    public static InParameter VARCHAR(Object value) { return in(Types.VARCHAR, value); }

    /**
     * Create a new InParameter
     *
     * @param type  the JDBC data type
     * @param value the object value
     * @return an InParameter
     */
    public static InParameter in(final int type, final Object value) {
        return new InParameter() {
            public int getType() {
                return type;
            }

            public Object getValue() {
                return value;
            }
        };
    }

    /**
     * Create a new OutParameter
     *
     * @param type the JDBC data type.
     * @return an OutParameter
     */
    public static OutParameter out(final int type) {
        return new OutParameter() {
            public int getType() {
                return type;
            }
        };
    }

    /**
     * Create an inout parameter using this in parameter.
     *
     * @param in the InParameter of interest
     * @return the resulting InOutParameter
     */
    public static InOutParameter inout(final InParameter in) {
        return new InOutParameter() {
            public int getType() {
                return in.getType();
            }

            public Object getValue() {
                return in.getValue();
            }
        };
    }

    /**
     * Create a new ResultSetOutParameter
     *
     * @param type the JDBC data type.
     * @return a ResultSetOutParameter
     */
    public static ResultSetOutParameter resultSet(final int type) {
        return new ResultSetOutParameter() {
            public int getType() {
                return type;
            }
        };
    }

    /**
     * Creates a variable to be expanded in the Sql string rather
     * than representing an sql parameter.
     *
     * @param object the object of interest
     * @return the expanded variable
     */
    public static ExpandedVariable expand(final Object object) {
        return new ExpandedVariable() {
            public Object getObject() {
                return object;
            }
        };
    }

    /**
     * Constructs an SQL instance using the given DataSource. Each operation
     * will use a Connection from the DataSource pool and close it when the
     * operation is completed putting it back into the pool.
     *
     * @param dataSource the DataSource to use
     */
    public Sql(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Constructs an SQL instance using the given Connection. It is the caller's
     * responsibility to close the Connection after the Sql instance has been
     * used. You can do this on the connection object directly or by calling the
     * {@link Connection#close()} method.
     *
     * @param connection the Connection to use
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
     * Performs the given SQL query calling the closure with the result set.
     *
     * @param sql     the sql statement
     * @param closure called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     */
    public void query(String sql, Closure closure) throws SQLException {
        Connection connection = createConnection();
        Statement statement = getStatement(connection, sql);
        configure(statement);
        ResultSet results = null;
        try {
            log.fine(sql);
            results = statement.executeQuery(sql);
            closure.call(results);
        }
        catch (SQLException e) {
            log.log(Level.FINE, "Failed to execute: " + sql, e);
            throw e;
        }
        finally {
            closeResources(connection, statement, results);
        }
    }

    private Statement createStatement(Connection connection) throws SQLException {
        if (resultSetHoldability == -1) {
            return connection.createStatement(resultSetType, resultSetConcurrency);
        }
        return connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    /**
     * Performs the given SQL query with parameters calling the closure with the
     * result set.
     *
     * @param sql     the sql statement
     * @param params  a list of parameters
     * @param closure called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     */
    public void query(String sql, List params, Closure closure) throws SQLException {
        Connection connection = createConnection();
        PreparedStatement statement = null;
        ResultSet results = null;
        try {
            log.fine(sql);
            statement = getPreparedStatement(connection, sql);
            setParameters(params, statement);
            configure(statement);
            results = statement.executeQuery();
            closure.call(results);
        }
        catch (SQLException e) {
            log.log(Level.FINE, "Failed to execute: " + sql, e);
            throw e;
        }
        finally {
            closeResources(connection, statement, results);
        }
    }

    /**
     * Performs the given SQL query calling the closure with the result set.
     *
     * @param gstring a GString containing the SQL query with embedded params
     * @param closure called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     */
    public void query(GString gstring, Closure closure) throws SQLException {
        List params = getParameters(gstring);
        String sql = asSql(gstring, params);
        query(sql, params, closure);
    }

    /**
     * Performs the given SQL query calling the closure with each row of the
     * result set.
     *
     * @param sql     the sql statement
     * @param closure called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     */
    public void eachRow(String sql, Closure closure) throws SQLException {
        eachRow(sql, (Closure) null, closure);
    }

    /**
     * Performs the given SQL query calling closures for metadata and each row
     *
     * @param sql         the sql statement
     * @param metaClosure called for meta data (only once after sql execution)
     * @param rowClosure  called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     */
    public void eachRow(String sql, Closure metaClosure, Closure rowClosure) throws SQLException {
        Connection connection = createConnection();
        Statement statement = getStatement(connection, sql);
        configure(statement);
        ResultSet results = null;
        try {
            log.fine(sql);
            results = statement.executeQuery(sql);

            if (metaClosure != null) metaClosure.call(results.getMetaData());

            GroovyResultSet groovyRS = new GroovyResultSetProxy(results).getImpl();
            while (groovyRS.next()) {
                rowClosure.call(groovyRS);
            }
        } catch (SQLException e) {
            log.log(Level.FINE, "Failed to execute: " + sql, e);
            throw e;
        } finally {
            closeResources(connection, statement, results);
        }
    }

    /**
     * Performs the given SQL query calling the closure with the result set.
     *
     * @param sql     the sql statement
     * @param params  a list of parameters
     * @param closure called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     */
    public void eachRow(String sql, List params, Closure closure) throws SQLException {
        Connection connection = createConnection();
        PreparedStatement statement = null;
        ResultSet results = null;
        try {
            log.fine(sql);
            statement = getPreparedStatement(connection, sql);
            setParameters(params, statement);
            configure(statement);
            results = statement.executeQuery();

            GroovyResultSet groovyRS = new GroovyResultSetProxy(results).getImpl();
            while (groovyRS.next()) {
                closure.call(groovyRS);
            }
        }
        catch (SQLException e) {
            log.log(Level.FINE, "Failed to execute: " + sql, e);
            throw e;
        }
        finally {
            closeResources(connection, statement, results);
        }
    }

    /**
     * Performs the given SQL query calling the closure with the result set.
     *
     * @param gstring a GString containing the SQL query with embedded params
     * @param closure called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     */
    public void eachRow(GString gstring, Closure closure) throws SQLException {
        List params = getParameters(gstring);
        String sql = asSql(gstring, params);
        eachRow(sql, params, closure);
    }

    /**
     * Performs the given SQL query and return the rows of the result set.
     *
     * @param sql the SQL statement
     * @return a list of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     */
    public List rows(String sql) throws SQLException {
        return rows(sql, (Closure) null);
    }

    /**
     * Performs the given SQL query and return the rows of the result set.
     *
     * @param gstring a GString containing the SQL query with embedded params
     * @return a list of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     */
    public List rows(GString gstring) throws SQLException {
        List params = getParameters(gstring);
        String sql = asSql(gstring, params);
        return rows(sql, params);
    }

    /**
     * Performs the given SQL query and return the rows of the result set.
     *
     * @param sql         the SQL statement
     * @param metaClosure called with meta data of the ResultSet
     * @return a list of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     */
    public List rows(String sql, Closure metaClosure) throws SQLException {
        List results = new ArrayList();
        Connection connection = createConnection();
        Statement statement = getStatement(connection, sql);
        configure(statement);
        ResultSet rs = null;
        try {
            log.fine(sql);
            rs = statement.executeQuery(sql);
            if (metaClosure != null) metaClosure.call(rs.getMetaData());

            while (rs.next()) {
                results.add(DefaultGroovyMethods.toRowResult(rs));
            }
            return (results);
        } catch (SQLException e) {
            log.log(Level.FINE, "Failed to execute: " + sql, e);
            throw e;
        } finally {
            closeResources(connection, statement, rs);
        }
    }

    /**
     * Performs the given SQL query with the list of params and return
     * the rows of the result set.
     *
     * @param sql    the SQL statement
     * @param params a list of parameters
     * @return a list of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     */
    public List rows(String sql, List params) throws SQLException {
        List results = new ArrayList();
        Connection connection = createConnection();
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            log.fine(sql);
            statement = getPreparedStatement(connection, sql);
            setParameters(params, statement);
            configure(statement);
            rs = statement.executeQuery();
            while (rs.next()) {
                results.add(DefaultGroovyMethods.toRowResult(rs));
            }
            return (results);
        }
        catch (SQLException e) {
            log.log(Level.FINE, "Failed to execute: " + sql, e);
            throw e;
        }
        finally {
            closeResources(connection, statement, rs);
        }
    }

    /**
     * Performs the given SQL query and return the first row of the result set.
     *
     * @param sql the SQL statement
     * @return a GroovyRowResult object
     * @throws SQLException if a database access error occurs
     */
    public Object firstRow(String sql) throws SQLException {
        List rows = rows(sql);
        if (rows.isEmpty()) return null;
        return (rows.get(0));
    }

    /**
     * Performs the given SQL query and return
     * the first row of the result set.
     *
     * @param gstring a GString containing the SQL query with embedded params
     * @return a GroovyRowResult object
     * @throws SQLException if a database access error occurs
     */
    public Object firstRow(GString gstring) throws SQLException {
        List params = getParameters(gstring);
        String sql = asSql(gstring, params);
        return firstRow(sql, params);
    }

    /**
     * Performs the given SQL query with the list of params and return
     * the first row of the result set.
     *
     * @param sql    the SQL statement
     * @param params a list of parameters
     * @return a GroovyRowResult object
     * @throws SQLException if a database access error occurs
     */
    public Object firstRow(String sql, List params) throws SQLException {
        List rows = rows(sql, params);
        if (rows.isEmpty()) return null;
        return rows.get(0);
    }

    /**
     * Executes the given piece of SQL.
     *
     * @param sql the SQL to execute
     * @return <code>true</code> if the first result is a <code>ResultSet</code>
     *         object; <code>false</code> if it is an update count or there are
     *         no results
     * @throws SQLException if a database access error occurs
     */
    public boolean execute(String sql) throws SQLException {
        Connection connection = createConnection();
        Statement statement = null;
        try {
            log.fine(sql);
            statement = getStatement(connection, sql);
            configure(statement);
            boolean isResultSet = statement.execute(sql);
            this.updateCount = statement.getUpdateCount();
            return isResultSet;
        }
        catch (SQLException e) {
            log.log(Level.FINE, "Failed to execute: " + sql, e);
            throw e;
        }
        finally {
            closeResources(connection, statement);
        }
    }

    /**
     * Executes the given piece of SQL with parameters.
     *
     * @param sql    the SQL statement
     * @param params a list of parameters
     * @return <code>true</code> if the first result is a <code>ResultSet</code>
     *         object; <code>false</code> if it is an update count or there are
     *         no results
     * @throws SQLException if a database access error occurs
     */
    public boolean execute(String sql, List params) throws SQLException {
        Connection connection = createConnection();
        PreparedStatement statement = null;
        try {
            log.fine(sql);
            statement = getPreparedStatement(connection, sql);
            setParameters(params, statement);
            configure(statement);
            boolean isResultSet = statement.execute();
            this.updateCount = statement.getUpdateCount();
            return isResultSet;
        }
        catch (SQLException e) {
            log.log(Level.FINE, "Failed to execute: " + sql, e);
            throw e;
        }
        finally {
            closeResources(connection, statement);
        }
    }

    /**
     * Executes the given SQL with embedded expressions inside.
     *
     * @param gstring a GString containing the SQL query with embedded params
     * @return <code>true</code> if the first result is a <code>ResultSet</code>
     *         object; <code>false</code> if it is an update count or there are
     *         no results
     * @throws SQLException if a database access error occurs
     */
    public boolean execute(GString gstring) throws SQLException {
        List params = getParameters(gstring);
        String sql = asSql(gstring, params);
        return execute(sql, params);
    }

    /**
     * Executes the given SQL statement. See {@link #executeInsert(GString)}
     * for more details.
     *
     * @param sql The SQL statement to execute
     * @return A list of the auto-generated column values for each
     *         inserted row
     * @throws SQLException if a database access error occurs
     */
    public List executeInsert(String sql) throws SQLException {
        Connection connection = createConnection();
        Statement statement = null;
        try {
            log.fine(sql);
            statement = getStatement(connection, sql);
            configure(statement);
            boolean hasResultSet = statement.execute(sql, Statement.RETURN_GENERATED_KEYS);

            // Prepare a list to contain the auto-generated column
            // values, and then fetch them from the statement.
            List autoKeys = new ArrayList();
            ResultSet keys = statement.getGeneratedKeys();
            int count = keys.getMetaData().getColumnCount();

            // Copy the column values into a list of a list.
            while (keys.next()) {
                List rowKeys = new ArrayList(count);
                for (int i = 1; i <= count; i++) {
                    rowKeys.add(keys.getObject(i));
                }

                autoKeys.add(rowKeys);
            }

            // Store the update count so that it can be retrieved by
            // clients, and then return the list of auto-generated
            // values.
            this.updateCount = statement.getUpdateCount();
            return autoKeys;
        }
        catch (SQLException e) {
            log.log(Level.FINE, "Failed to execute: " + sql, e);
            throw e;
        }
        finally {
            closeResources(connection, statement);
        }
    }

    /**
     * Executes the given SQL statement with a particular list of
     * parameter values. See {@link #executeInsert(GString)} for
     * more details.
     *
     * @param sql    The SQL statement to execute
     * @param params The parameter values that will be substituted
     *               into the SQL statement's parameter slots
     * @return A list of the auto-generated column values for each
     *         inserted row
     * @throws SQLException if a database access error occurs
     */
    public List executeInsert(String sql, List params) throws SQLException {
        // Now send the SQL to the database.
        Connection connection = createConnection();
        PreparedStatement statement = null;
        try {
            log.fine(sql);

            // Prepare a statement for the SQL and then execute it.
            statement = getPreparedStatement(connection, sql, Statement.RETURN_GENERATED_KEYS);
            setParameters(params, statement);
            configure(statement);
            this.updateCount = statement.executeUpdate();

            // Prepare a list to contain the auto-generated column
            // values, and then fetch them from the statement.
            List autoKeys = new ArrayList();
            ResultSet keys = statement.getGeneratedKeys();
            int count = keys.getMetaData().getColumnCount();

            // Copy the column values into a list of a list.
            while (keys.next()) {
                List rowKeys = new ArrayList(count);
                for (int i = 1; i <= count; i++) {
                    rowKeys.add(keys.getObject(i));
                }

                autoKeys.add(rowKeys);
            }

            return autoKeys;
        }
        catch (SQLException e) {
            log.log(Level.FINE, "Failed to execute: " + sql, e);
            throw e;
        }
        finally {
            closeResources(connection, statement);
        }
    }

    /**
     * <p>Executes the given SQL with embedded expressions inside, and
     * returns the values of any auto-generated colums, such as an
     * autoincrement ID field. These values can be accessed using
     * array notation. For example, to return the second auto-generated
     * column value of the third row, use <code>keys[3][1]</code>. The
     * method is designed to be used with SQL INSERT statements, but is
     * not limited to them.</p>
     * <p>The standard use for this method is when a table has an
     * autoincrement ID column and you want to know what the ID is for
     * a newly inserted row. In this example, we insert a single row
     * into a table in which the first column contains the autoincrement
     * ID:</p>
     * <pre>
     *     def sql = Sql.newInstance("jdbc:mysql://localhost:3306/groovy",
     *                               "user",
     *                               "password",
     *                               "com.mysql.jdbc.Driver")
     * <p/>
     *     def keys = sql.insert("insert into test_table (INT_DATA, STRING_DATA) "
     *                           + "VALUES (1, 'Key Largo')")
     * <p/>
     *     def id = keys[0][0]
     * <p/>
     *     // 'id' now contains the value of the new row's ID column.
     *     // It can be used to update an object representation's
     *     // id attribute for example.
     *     ...
     * </pre>
     *
     * @param gstring a GString containing the SQL query with embedded params
     * @return A list of column values representing each row's
     *         auto-generated keys
     * @throws SQLException if a database access error occurs
     */
    public List executeInsert(GString gstring) throws SQLException {
        List params = getParameters(gstring);
        String sql = asSql(gstring, params);
        return executeInsert(sql, params);
    }

    /**
     * Executes the given SQL update.
     *
     * @param sql the SQL to execute
     * @return the number of rows updated or 0 for SQL statements that return nothing
     * @throws SQLException if a database access error occurs
     */
    public int executeUpdate(String sql) throws SQLException {
        Connection connection = createConnection();
        Statement statement = null;
        try {
            log.fine(sql);
            statement = getStatement(connection, sql);
            configure(statement);
            this.updateCount = statement.executeUpdate(sql);
            return this.updateCount;
        }
        catch (SQLException e) {
            log.log(Level.FINE, "Failed to execute: " + sql, e);
            throw e;
        }
        finally {
            closeResources(connection, statement);
        }
    }

    /**
     * Executes the given SQL update with parameters.
     *
     * @param sql    the SQL statement
     * @param params a list of parameters
     * @return the number of rows updated or 0 for SQL statements that return nothing
     * @throws SQLException if a database access error occurs
     */
    public int executeUpdate(String sql, List params) throws SQLException {
        Connection connection = createConnection();
        PreparedStatement statement = null;
        try {
            log.fine(sql);
            statement = getPreparedStatement(connection, sql);
            setParameters(params, statement);
            configure(statement);
            this.updateCount = statement.executeUpdate();
            return this.updateCount;
        }
        catch (SQLException e) {
            log.log(Level.FINE, "Failed to execute: " + sql, e);
            throw e;
        }
        finally {
            closeResources(connection, statement);
        }
    }

    /**
     * Executes the given SQL update with embedded expressions inside.
     *
     * @param gstring a GString containing the SQL query with embedded params
     * @return the number of rows updated or 0 for SQL statements that return nothing
     * @throws SQLException if a database access error occurs
     */
    public int executeUpdate(GString gstring) throws SQLException {
        List params = getParameters(gstring);
        String sql = asSql(gstring, params);
        return executeUpdate(sql, params);
    }

    /**
     * Performs a stored procedure call.
     *
     * @param sql the SQL statement
     * @return the number of rows updated or 0 for SQL statements that return nothing
     * @throws SQLException if a database access error occurs
     */
    public int call(String sql) throws Exception {
        return call(sql, Collections.EMPTY_LIST);
    }

    /**
     * Performs a stored procedure call with the given parameters.
     *
     * @param sql    the SQL statement
     * @param params a list of parameters
     * @return the number of rows updated or 0 for SQL statements that return nothing
     * @throws SQLException if a database access error occurs
     */
    public int call(String sql, List params) throws Exception {
        Connection connection = createConnection();
        CallableStatement statement = connection.prepareCall(sql);
        try {
            log.fine(sql);
            setParameters(params, statement);
            configure(statement);
            return statement.executeUpdate();
        }
        catch (SQLException e) {
            log.log(Level.FINE, "Failed to execute: " + sql, e);
            throw e;
        }
        finally {
            closeResources(connection, statement);
        }
    }

    /**
     * Performs a stored procedure call with the given parameters.  The closure
     * is called once with all the out parameters.
     *
     * @param sql     the sql statement
     * @param params  a list of parameters
     * @param closure called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     */
    public void call(String sql, List params, Closure closure) throws Exception {
        Connection connection = createConnection();
        CallableStatement statement = connection.prepareCall(sql);
        try {
            log.fine(sql);
            setParameters(params, statement);
            statement.execute();
            List results = new ArrayList();
            int indx = 0;
            int inouts = 0;
            for (Object value : params) {
                if (value instanceof OutParameter) {
                    if (value instanceof ResultSetOutParameter) {
                        results.add(CallResultSet.getImpl(statement, indx));
                    } else {
                        Object o = statement.getObject(indx + 1);
                        if (o instanceof ResultSet) {
                            results.add(new GroovyResultSetProxy((ResultSet) o).getImpl());
                        } else {
                            results.add(o);
                        }
                    }
                    inouts++;
                }
                indx++;
            }
            closure.call(results.toArray(new Object[inouts]));
        } catch (SQLException e) {
            log.log(Level.WARNING, "Failed to execute: " + sql, e);
            throw e;
        } finally {
            closeResources(connection, statement);
        }
    }

    /**
     * Performs a stored procedure call with the given parameters.
     *
     * @param gstring a GString containing the SQL query with embedded params
     * @return the number of rows updated or 0 for SQL statements that return nothing
     * @throws SQLException if a database access error occurs
     */
    public int call(GString gstring) throws Exception {
        List params = getParameters(gstring);
        String sql = asSql(gstring, params);
        return call(sql, params);
    }

    /**
     * Performs a stored procedure call with the given parameters,
     * calling the closure once with all result objects.
     *
     * @param gstring a GString containing the SQL query with embedded params
     * @param closure called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     */
    public void call(GString gstring, Closure closure) throws Exception {
        List params = getParameters(gstring);
        String sql = asSql(gstring, params);
        call(sql, params, closure);
    }

    /**
     * If this SQL object was created with a Connection then this method closes
     * the connection. If this SQL object was created from a DataSource then
     * this method does nothing.
     *
     * @throws SQLException if a database access error occurs
     */
    public void close() throws SQLException {
        if (useConnection == null) {
            log.log(Level.FINEST, "Close operation not supported when using datasets");
            return;
        }
        try {
            useConnection.close();
        }
        catch (SQLException e) {
            log.log(Level.SEVERE, "Caught exception closing connection: " + e, e);
            throw e;
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }


    /**
     * If this SQL object was created with a Connection then this method commits
     * the connection. If this SQL object was created from a DataSource then
     * this method does nothing.
     *
     * @throws SQLException if a database access error occurs
     */
    public void commit() throws SQLException {
        if (useConnection == null) {
            log.log(Level.FINEST, "Commit operation not supported when using datasets");
            return;
        }
        try {
            useConnection.commit();
        }
        catch (SQLException e) {
            log.log(Level.SEVERE, "Caught exception committing connection: " + e, e);
            throw e;
        }
    }

    /**
     * If this SQL object was created with a Connection then this method rolls back
     * the connection. If this SQL object was created from a DataSource then
     * this method does nothing.
     *
     * @throws SQLException if a database access error occurs
     */
    public void rollback() throws SQLException {
        if (useConnection == null) {
            log.log(Level.FINEST, "Rollback operation not supported when using datasets");
            return;
        }
        try {
            useConnection.rollback();
        }
        catch (SQLException e) {
            log.log(Level.SEVERE, "Caught exception rolling back connection: " + e, e);
            throw e;
        }
    }

    /**
     * @return Returns the updateCount.
     */
    public int getUpdateCount() {
        return updateCount;
    }

    /**
     * If this instance was created with a single Connection then the connection
     * is returned. Otherwise if this instance was created with a DataSource
     * then this method returns null
     *
     * @return the connection wired into this object, or null if this object
     *         uses a DataSource
     */
    public Connection getConnection() {
        return useConnection;
    }


    /**
     * Allows a closure to be passed in to configure the JDBC statements before they are executed
     * to do things like set the query size etc.
     *
     * @param configureStatement the closure
     */
    public void withStatement(Closure configureStatement) {
        this.configureStatement = configureStatement;
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * @param gstring a GString containing the SQL query with embedded params
     * @param values  the values to embed
     * @return the SQL version of the given query using ? instead of any
     *         parameter
     */
    protected String asSql(GString gstring, List values) {
        String[] strings = gstring.getStrings();
        if (strings.length <= 0) {
            throw new IllegalArgumentException("No SQL specified in GString: " + gstring);
        }
        boolean nulls = false;
        StringBuffer buffer = new StringBuffer();
        boolean warned = false;
        Iterator iter = values.iterator();
        for (int i = 0; i < strings.length; i++) {
            String text = strings[i];
            if (text != null) {
                buffer.append(text);
            }
            if (iter.hasNext()) {
                Object value = iter.next();
                if (value != null) {
                    if (value instanceof ExpandedVariable) {
                        buffer.append(((ExpandedVariable) value).getObject());
                        iter.remove();
                    } else {
                        boolean validBinding = true;
                        if (i < strings.length - 1) {
                            String nextText = strings[i + 1];
                            if ((text.endsWith("\"") || text.endsWith("'")) && (nextText.startsWith("'") || nextText.startsWith("\""))) {
                                if (!warned) {
                                    log.warning("In Groovy SQL please do not use quotes around dynamic expressions " +
                                            "(which start with $) as this means we cannot use a JDBC PreparedStatement " +
                                            "and so is a security hole. Groovy has worked around your mistake but the security hole is still there. " +
                                            "The expression so far is: " + buffer.toString() + "?" + nextText);
                                    warned = true;
                                }
                                buffer.append(value);
                                iter.remove();
                                validBinding = false;
                            }
                        }
                        if (validBinding) {
                            buffer.append("?");
                        }
                    }
                } else {
                    nulls = true;
                    iter.remove();
                    buffer.append("?'\"?"); // will replace these with nullish values
                }
            }
        }
        String sql = buffer.toString();
        if (nulls) {
            sql = nullify(sql);
        }
        return sql;
    }

    /**
     * replace ?'"? references with NULLish
     *
     * @param sql the SQL statement
     * @return the modified SQL String
     */
    protected String nullify(String sql) {
        /*
         * Some drivers (Oracle classes12.zip) have difficulty resolving data
         * type if setObject(null). We will modify the query to pass 'null', 'is
         * null', and 'is not null'
         */
        //could be more efficient by compiling expressions in advance.
        int firstWhere = findWhereKeyword(sql);
        if (firstWhere >= 0) {
            Pattern[] patterns = {Pattern.compile("(?is)^(.{" + firstWhere + "}.*?)!=\\s{0,1}(\\s*)\\?'\"\\?(.*)"),
                    Pattern.compile("(?is)^(.{" + firstWhere + "}.*?)<>\\s{0,1}(\\s*)\\?'\"\\?(.*)"),
                    Pattern.compile("(?is)^(.{" + firstWhere + "}.*?[^<>])=\\s{0,1}(\\s*)\\?'\"\\?(.*)"),};
            String[] replacements = {"$1 is not $2null$3", "$1 is not $2null$3", "$1 is $2null$3",};
            for (int i = 0; i < patterns.length; i++) {
                Matcher matcher = patterns[i].matcher(sql);
                while (matcher.matches()) {
                    sql = matcher.replaceAll(replacements[i]);
                    matcher = patterns[i].matcher(sql);
                }
            }
        }
        return sql.replaceAll("\\?'\"\\?", "null");
    }

    /**
     * Find the first 'where' keyword in the sql.
     *
     * @param sql the SQL statement
     * @return the index of the found keyword or -1 if not found
     */
    protected int findWhereKeyword(String sql) {
        char[] chars = sql.toLowerCase().toCharArray();
        char[] whereChars = "where".toCharArray();
        int i = 0;
        boolean inString = false; //TODO: Cater for comments?
        boolean noWhere = true;
        int inWhere = 0;
        while (i < chars.length && noWhere) {
            switch (chars[i]) {
                case '\'':
                    inString = !inString;
                    break;
                default:
                    if (!inString && chars[i] == whereChars[inWhere]) {
                        inWhere++;
                        if (inWhere == whereChars.length) {
                            return i;
                        }
                    }
            }
            i++;
        }
        return -1;
    }

    /**
     * @param gstring a GString containing the SQL query with embedded params
     * @return extracts the parameters from the expression as a List
     */
    protected List getParameters(GString gstring) {
        return new ArrayList<Object>(Arrays.asList(gstring.getValues()));
    }

    /**
     * Appends the parameters to the given statement.
     *
     * @param params the parameters to append
     * @param statement the statement
     * @throws SQLException if a database access error occurs
     */
    protected void setParameters(List params, PreparedStatement statement) throws SQLException {
        int i = 1;
        for (Object value : params) {
            setObject(statement, i++, value);
        }
    }

    /**
     * Strategy method allowing derived classes to handle types differently
     * such as for CLOBs etc.
     *
     * @param statement the statement of interest
     * @param i the index of the object of interest
     * @param value the new object value
     * @throws SQLException if a database access error occurs
     */
    protected void setObject(PreparedStatement statement, int i, Object value)
            throws SQLException {
        if (value instanceof InParameter || value instanceof OutParameter) {
            if (value instanceof InParameter) {
                InParameter in = (InParameter) value;
                Object val = in.getValue();
                if (null == val) {
                    statement.setNull(i, in.getType());
                } else {
                    statement.setObject(i, val, in.getType());
                }
            }
            if (value instanceof OutParameter) {
                try {
                    OutParameter out = (OutParameter) value;
                    ((CallableStatement) statement).registerOutParameter(i, out.getType());
                } catch (ClassCastException e) {
                    throw new SQLException("Cannot register out parameter.");
                }
            }
        } else {
            statement.setObject(i, value);
        }
    }

    protected Connection createConnection() throws SQLException {
        if ((cacheStatements || cacheConnection) && useConnection != null) {
            return useConnection;
        }
        if (dataSource != null) {
            // Use a doPrivileged here as many different properties need to be
            // read, and the policy shouldn't have to list them all.
            Connection con;
            try {
                con = (Connection) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    public Object run() throws SQLException {
                        return dataSource.getConnection();
                    }
                });
            }
            catch (PrivilegedActionException pae) {
                Exception e = pae.getException();
                if (e instanceof SQLException) {
                    throw (SQLException) e;
                } else {
                    throw (RuntimeException) e;
                }
            }
            if (cacheStatements || cacheConnection) {
                useConnection = con;
            }
            return con;
        } else {
            return useConnection;
        }
    }

    protected void closeResources(Connection connection, Statement statement, ResultSet results) {
        if (results != null) {
            try {
                results.close();
            }
            catch (SQLException e) {
                log.log(Level.INFO, "Caught exception closing resultSet: " + e, e);
            }
        }
        closeResources(connection, statement);
    }

    protected void closeResources(Connection connection, Statement statement) {
        if (isCacheStatements()) return;
        if (statement != null) {
            try {
                statement.close();
            }
            catch (SQLException e) {
                log.log(Level.INFO, "Caught exception closing statement: " + e, e);
            }
        }
        if (dataSource != null) {
            try {
                connection.close();
            }
            catch (SQLException e) {
                log.log(Level.INFO, "Caught exception closing connection: " + e, e);
            }
        }
    }

    /**
     * Provides a hook to be able to configure JDBC statements, such as to configure
     *
     * @param statement the statement to configure
     */
    protected void configure(Statement statement) {
        if (configureStatement != null) {
            configureStatement.call(statement);
        }
    }

    /**
     * Enables statement caching.</br>
     * if <i>b</i> is true, cache is created and all created prepared statements will be cached.</br>
     * if <i>b</i> is false, all cached statements will be properly closed.
     *
     * @param cacheStatements the new value
     */
    public synchronized void setCacheStatements(boolean cacheStatements) {
        this.cacheStatements = cacheStatements;
        if (!cacheStatements) {
            clearStatementCache();
        }
    }

    /**
     * @return boolean    true if cache is enabled
     */
    public boolean isCacheStatements() {
        return cacheStatements;
    }

    /**
     * Caches the connection used while the closure is active.
     *
     * @param closure the given closure
     * @throws SQLException if a database error occurs
     */
    public synchronized void cacheConnection(Closure closure) throws SQLException {
        cacheConnection = true;
        Connection connection = null;
        try {
            connection = createConnection();
            closure.call();
        }
        finally {
            cacheConnection = false;
            closeResources(connection, null);
        }
    }

    /**
     * Caches every created preparedStatement in closure <i>closure</i></br>
     * Every cached preparedStatement is closed after closure has been called.
     *
     * @param closure the given closure
     * @throws SQLException if a database error occurs
     * @see #setCacheStatements(boolean)
     */
    public synchronized void cacheStatements(Closure closure) throws SQLException {
        setCacheStatements(true);
        Connection connection = null;
        try {
            connection = createConnection();
            closure.call();
        }
        finally {
            setCacheStatements(false);
            closeResources(connection, null);
        }
    }

    private synchronized void clearStatementCache() {
        if (!statementCache.isEmpty()) {
            for (Object o : statementCache.values())
                try {
                    ((Statement) o).close();
                } catch (SQLException e) {
                    log.log(Level.FINEST, "Failed to close statement. Already closed?", e);
                }
            statementCache.clear();
        }
    }

    private Statement getStatement(Connection connection, String sql) throws SQLException {
        Statement stmt;
        if (cacheStatements) {
            stmt = statementCache.get(sql);
            if (stmt == null) {
                synchronized (statementCache) {
                    stmt = createStatement(connection);
                    statementCache.put(sql, stmt);
                }
            }
        } else {
            stmt = createStatement(connection);
        }
        return stmt;
    }

    private PreparedStatement getPreparedStatement(Connection connection, String sql, int returnGeneratedKeys) throws SQLException {
        PreparedStatement pStmt;
        if (cacheStatements) {
            pStmt = (PreparedStatement) statementCache.get(sql);
            if (pStmt == null) {
                synchronized (statementCache) {
                    pStmt = createPreparedStatement(connection, sql, returnGeneratedKeys);
                    statementCache.put(sql, pStmt);
                }
            }
        } else {
            pStmt = createPreparedStatement(connection, sql, returnGeneratedKeys);
        }
        return pStmt;
    }

    private PreparedStatement createPreparedStatement(Connection connection, String sql, int returnGeneratedKeys) throws SQLException {
        if (returnGeneratedKeys != 0) {
            return connection.prepareStatement(sql, returnGeneratedKeys);
        } else {
            return connection.prepareStatement(sql);
        }
    }

    private PreparedStatement getPreparedStatement(Connection connection, String sql) throws SQLException {
        return getPreparedStatement(connection, sql, 0);
    }
}
