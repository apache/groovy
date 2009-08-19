/*
 * Copyright 2003-2009 the original author or authors.
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

import org.codehaus.groovy.runtime.SqlGroovyMethods;

/**
 * A facade over Java's normal JDBC APIs providing greatly simplified
 * resource management and result set handling. Under the covers the
 * facade hides away details associated with getting connections,
 * constructing and configuring statements, interacting with the
 * connection, closing resources and logging errors. Special
 * features of the facade include using closures to iterate
 * through result sets, a special GString syntax for representing
 * prepared statements and treating result sets like collections
 * of maps with the normal Groovy collection methods available.
 * <p/>
 * The class provides numerous extension points for overriding the
 * facade behavior associated with the various aspects of managing
 * the interaction with the underlying database.
 *
 * @author Chris Stevenson
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Paul King
 * @author Marc DeXeT
 * @author John Bito
 * @version $Revision$
 */
public class Sql {

    /**
     * Hook to allow derived classes to access the log
     */
    protected static Logger log = Logger.getLogger(Sql.class.getName());

    private static final List<Object> EMPTY_LIST = Collections.emptyList();

    private DataSource dataSource;

    private Connection useConnection;

    private int resultSetType = ResultSet.TYPE_FORWARD_ONLY;
    private int resultSetConcurrency = ResultSet.CONCUR_READ_ONLY;
    private int resultSetHoldability = -1;

    // store last row update count for executeUpdate, executeInsert and execute
    private int updateCount = 0;

    // allows a closure to be used to configure Statement objects before its use
    private Closure configureStatement;

    private boolean cacheConnection;

    private boolean cacheStatements;

    private final Map<String, Statement> statementCache = new HashMap<String, Statement>();

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
     *                        <code>jdbc:<em>subprotocol</em>:<em>subname</em></code>
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
     *                 <code>jdbc:<em>subprotocol</em>:<em>subname</em></code>
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
     *                        <code>jdbc:<em>subprotocol</em>:<em>subname</em></code>
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
     * @since 1.5.2
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
     *                      constants:
     *                      <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *                      <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *                      <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @since 1.5.2
     */
    public void setResultSetType(int resultSetType) {
        this.resultSetType = resultSetType;
    }

    /**
     * Gets the resultSetConcurrency for statements created using the connection.
     *
     * @return the current resultSetConcurrency value
     * @since 1.5.2
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
     *                             constants:
     *                             <code>ResultSet.CONCUR_READ_ONLY</code> or
     *                             <code>ResultSet.CONCUR_UPDATABLE</code>
     * @since 1.5.2
     */
    public void setResultSetConcurrency(int resultSetConcurrency) {
        this.resultSetConcurrency = resultSetConcurrency;
    }

    /**
     * Gets the resultSetHoldability for statements created using the connection.
     *
     * @return the current resultSetHoldability value or -1 if not set
     * @since 1.5.2
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
     *                             constants:
     *                             <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code> or
     *                             <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @since 1.5.2
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
     * When using GString SQL queries, allows a variable to be expanded
     * in the Sql string rather than representing an sql parameter.
     * <p/>
     * Example usage:
     * <pre>
     * def fieldName = 'firstname'
     * def fieldOp = Sql.expand('like')
     * def fieldVal = '%a%'
     * sql.query "select * from PERSON where ${Sql.expand(fieldName)} $fieldOp ${fieldVal}", { ResultSet rs ->
     *     while (rs.next()) println rs.getString('firstname')
     * }
     * // query will be 'select * from PERSON where firstname like ?'
     * // params will be [fieldVal]
     * </pre>
     *
     * @param object the object of interest
     * @return the expanded variable
     * @see #expand(Object)
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

    public DataSet dataSet(Class<?> type) {
        return new DataSet(this, type);
    }

    /**
     * Performs the given SQL query, which should return a single
     * <code>ResultSet</code> object. The given closure is called
     * with the <code>ResultSet</code> as its argument.
     * <p/>
     * Example usages:
     * <pre>
     * sql.query("select * from PERSON where firstname like 'S%'") { ResultSet rs ->
     *     while (rs.next()) println rs.getString('firstname') + ' ' + rs.getString(3)
     * }
     *
     * sql.query("call get_people_places()") { ResultSet rs ->
     *     while (rs.next()) println rs.toRowResult().firstname
     * }
     * </pre>
     *
     * All resources including the ResultSet are closed automatically
     * after the closure is called.
     *
     * @param sql     the sql statement
     * @param closure called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     */
    public void query(String sql, Closure closure) throws SQLException {
        Connection connection = createConnection();
        Statement statement = getStatement(connection, sql);
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

    /**
     * Performs the given SQL query, which should return a single
     * <code>ResultSet</code> object. The given closure is called
     * with the <code>ResultSet</code> as its argument.
     * The query may contain placeholder question marks which match the given list of parameters.
     * <p/>
     * Example usage:
     * <pre>
     * sql.query('select * from PERSON where lastname like ?', ['%a%']) { ResultSet rs ->
     *     while (rs.next()) println rs.getString('lastname')
     * }
     * </pre>
     *
     * All resources including the ResultSet are closed automatically
     * after the closure is called.
     *
     * @param sql     the sql statement
     * @param params  a list of parameters
     * @param closure called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     */
    public void query(String sql, List<Object> params, Closure closure) throws SQLException {
        Connection connection = createConnection();
        PreparedStatement statement = null;
        ResultSet results = null;
        try {
            log.fine(sql);
            statement = getPreparedStatement(connection, sql, params);
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
     * Performs the given SQL query, which should return a single
     * <code>ResultSet</code> object. The given closure is called
     * with the <code>ResultSet</code> as its argument.
     * The query may contain GString expressions.
     * <p/>
     * Example usage:
     * <pre>
     * def location = 25
     * sql.query "select * from PERSON where location_id < $location", { ResultSet rs ->
     *     while (rs.next()) println rs.getString('firstname')
     * }
     * </pre>
     *
     * All resources including the ResultSet are closed automatically
     * after the closure is called.
     *
     * @param gstring a GString containing the SQL query with embedded params
     * @param closure called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     * @see #expand(Object)
     */
    public void query(GString gstring, Closure closure) throws SQLException {
        List<Object> params = getParameters(gstring);
        String sql = asSql(gstring, params);
        query(sql, params, closure);
    }

    /**
     * Performs the given SQL query calling the given Closure with each row of the
     * result set. The row will be a <code>GroovyRowResult</code> which is a Map
     * that also supports accessing the fields using ordinal index values.
     * <p/>
     * Example usages:
     * <pre>
     * sql.eachRow("select * from PERSON where firstname like 'S%'") { row ->
     *    println "$row.firstname ${row[2]}}"
     * }
     *
     * sql.eachRow "call my_stored_proc_returning_resultset()", {
     *     println it.firstname
     * }
     * </pre>
     *
     * @param sql     the sql statement
     * @param closure called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     */
    public void eachRow(String sql, Closure closure) throws SQLException {
        eachRow(sql, (Closure) null, closure);
    }

    /**
     * Performs the given SQL query calling the given <code>rowClosure</code> with each row of the
     * result set. The row will be a <code>GroovyRowResult</code> which is a Map
     * that also supports accessing the fields using ordinal index values.
     * In addition, the <code>metaClosure</code> will be called once passing in the
     * <code>ResultSetMetaData</code> as argument.
     * <p/>
     * Example usage:
     * <pre>
     * def printColNames = { meta ->
     *     (1..meta.columnCount).each {
     *         print meta.getColumnLabel(it).padRight(20)
     *     }
     *     println()
     * }
     * def printRow = { row ->
     *     row.toRowResult().values().each{ print it.toString().padRight(20) }
     *     println()
     * }
     * sql.eachRow("select * from PERSON", printColNames, printRow)
     * </pre>
     *
     * @param sql         the sql statement
     * @param metaClosure called for meta data (only once after sql execution)
     * @param rowClosure  called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     */
    public void eachRow(String sql, Closure metaClosure, Closure rowClosure) throws SQLException {
        Connection connection = createConnection();
        Statement statement = getStatement(connection, sql);
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
     * Performs the given SQL query calling the given Closure with each row of the
     * result set. The row will be a <code>GroovyRowResult</code> which is a Map
     * that also supports accessing the fields using ordinal index values.
     * The query may contain placeholder question marks which match the given list of parameters.
     * <p/>
     * Example usage:
     * <pre>
     * sql.eachRow("select * from PERSON where lastname like ?", ['%a%']) { row ->
     *     println "${row[1]} $row.lastname"
     * }
     * </pre>
     *
     * @param sql     the sql statement
     * @param params  a list of parameters
     * @param closure called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     */
    public void eachRow(String sql, List<Object> params, Closure closure) throws SQLException {
        Connection connection = createConnection();
        PreparedStatement statement = null;
        ResultSet results = null;
        try {
            log.fine(sql);
            statement = getPreparedStatement(connection, sql, params);
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
     * Performs the given SQL query calling the given Closure with each row of the
     * result set. The row will be a <code>GroovyRowResult</code> which is a Map
     * that also supports accessing the fields using ordinal index values.
     * The query may contain GString expressions.
     * <p/>
     * Example usage:
     * <pre>
     * def location = 25
     * sql.eachRow("select * from PERSON where location_id < $location") { row ->
     *     println row.firstname
     * }
     * </pre>
     *
     * @param gstring a GString containing the SQL query with embedded params
     * @param closure called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     * @see #expand(Object)
     */
    public void eachRow(GString gstring, Closure closure) throws SQLException {
        List<Object> params = getParameters(gstring);
        String sql = asSql(gstring, params);
        eachRow(sql, params, closure);
    }

    /**
     * Performs the given SQL query and return the rows of the result set.
     * <p/>
     * Example usage:
     * <pre>
     * def ans = sql.rows("select * from PERSON where firstname like 'S%'")
     * println "Found ${ans.size()} rows"     * </pre>
     *
     * @param sql the SQL statement
     * @return a list of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     */
    public List<GroovyRowResult> rows(String sql) throws SQLException {
        return rows(sql, (Closure) null);
    }

    /**
     * Performs the given SQL query and return the rows of the result set.
     * The query may contain GString expressions.
     * <p/>
     * Example usage:
     * <pre>
     * def location = 25
     * def ans = sql.rows("select * from PERSON where location_id < $location")
     * println "Found ${ans.size()} rows"
     * </pre>
     *
     * @param gstring a GString containing the SQL query with embedded params
     * @return a list of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     * @see #expand(Object)
     */
    public List<GroovyRowResult> rows(GString gstring) throws SQLException {
        List<Object> params = getParameters(gstring);
        String sql = asSql(gstring, params);
        return rows(sql, params);
    }

    /**
     * Performs the given SQL query and return the rows of the result set.
     * <p/>
     * Example usage:
     * <pre>
     * def printNumCols = { meta -> println "Found $meta.columnCount columns" }
     * def ans = sql.rows("select * from PERSON", printNumCols)
     * println "Found ${ans.size()} rows"
     * </pre>
     *
     * @param sql         the SQL statement
     * @param metaClosure called with meta data of the ResultSet
     * @return a list of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     */
    public List<GroovyRowResult> rows(String sql, Closure metaClosure) throws SQLException {
		ResultSet rs = executeQuery(sql);
		if (metaClosure != null) metaClosure.call(rs.getMetaData());
		return asList(sql, rs);
    }

	/**
	 * Performs the given SQL query and return the rows of the result set.
     * The query may contain placeholder question marks which match the given list of parameters.
     * <p/>
     * Example usage:
     * <pre>
     * def ans = sql.rows("select * from PERSON where lastname like ?", ['%a%'])
     * println "Found ${ans.size()} rows"
     * </pre>
	 *
	 * @param sql
	 *            the SQL statement
	 * @param params
	 *            a list of parameters
	 * @return a list of GroovyRowResult objects
	 * @throws SQLException
	 *             if a database access error occurs
	 */
	public List<GroovyRowResult> rows(String sql, List<Object> params)
			throws SQLException {
		ResultSet rs = executePreparedQuery(sql, params);
		return asList(sql, rs);
	}

	/**
     * Performs the given SQL query and return the first row of the result set.
     * <p/>
     * Example usage:
     * <pre>
     * def ans = sql.firstRow("select * from PERSON where firstname like 'S%'")
     * println ans.firstname
     * </pre>
     *
     * @param sql the SQL statement
     * @return a GroovyRowResult object or <code>null</code> if no row is found
     * @throws SQLException if a database access error occurs
     */
    public Object firstRow(String sql) throws SQLException {
        List<GroovyRowResult> rows = rows(sql);
        if (rows.isEmpty()) return null;
        return (rows.get(0));
    }

    /**
     * Performs the given SQL query and return
     * the first row of the result set.
     * The query may contain GString expressions.
     * <p/>
     * Example usage:
     * <pre>
     * def location = 25
     * def ans = sql.firstRow("select * from PERSON where location_id < $location")
     * println ans.firstname
     * </pre>
     *
     * @param gstring a GString containing the SQL query with embedded params
     * @return a GroovyRowResult object or <code>null</code> if no row is found
     * @throws SQLException if a database access error occurs
     * @see #expand(Object)
     */
    public Object firstRow(GString gstring) throws SQLException {
        List<Object> params = getParameters(gstring);
        String sql = asSql(gstring, params);
        return firstRow(sql, params);
    }

    /**
     * Performs the given SQL query and return the first row of the result set.
     * The query may contain placeholder question marks which match the given list of parameters.
     * <p/>
     * Example usages:
     * <pre>
     * def ans = sql.firstRow("select * from PERSON where lastname like ?", ['%a%'])
     * println ans.firstname
     * </pre>
     * If your database returns scalar functions as ResultSets, you can also use firstRow
     * to gain access to stored procedure results, e.g. using hsqldb 1.9 RC4:
     * <pre>
     * sql.execute """
     *     create function FullName(p_firstname VARCHAR(40)) returns VARCHAR(80)
     *     BEGIN atomic
     *     DECLARE ans VARCHAR(80);
     *     SET ans = (SELECT firstname || ' ' || lastname FROM PERSON WHERE firstname = p_firstname);
     *     RETURN ans;
     *     END
     * """
     *
     * assert sql.firstRow("{call FullName(?)}", ['Sam'])[0] == 'Sam Pullara'
     * </pre>
     *
     * @param sql    the SQL statement
     * @param params a list of parameters
     * @return a GroovyRowResult object or <code>null</code> if no row is found
     * @throws SQLException if a database access error occurs
     */
    public Object firstRow(String sql, List<Object> params) throws SQLException {
        List<GroovyRowResult> rows = rows(sql, params);
        if (rows.isEmpty()) return null;
        return rows.get(0);
    }

    /**
     * Executes the given piece of SQL.
     * Also saves the updateCount, if any, for subsequent examination.
     * <p/>
     * Example usages:
     * <pre>
     * sql.execute "drop table if exists PERSON"
     *
     * sql.execute """
     *     create table PERSON (
     *         id integer not null,
     *         firstname varchar(100),
     *         lastname varchar(100),
     *         location_id integer
     *     )
     * """
     *
     * sql.execute """
     *     insert into PERSON (id, firstname, lastname, location_id) values (4, 'Paul', 'King', 40)
     * """
     * assert sql.updateCount == 1
     * </pre>
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
            // TODO handle multiple results
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
     * 
     * Executes the given piece of SQL with parameters.
     * Also saves the updateCount, if any, for subsequent examination.
     * <p/>
     * Example usage:
     * <pre>
     * sql.execute """
     *     insert into PERSON (id, firstname, lastname, location_id) values (?, ?, ?, ?)
     * """, [1, "Guillaume", "Laforge", 10]
     * assert sql.updateCount == 1
     * </pre>
     *
     * @param sql    the SQL statement
     * @param params a list of parameters
     * @return <code>true</code> if the first result is a <code>ResultSet</code>
     *         object; <code>false</code> if it is an update count or there are
     *         no results
     * @throws SQLException if a database access error occurs
     */
    public boolean execute(String sql, List<Object> params) throws SQLException {
        Connection connection = createConnection();
        PreparedStatement statement = null;
        try {
            log.fine(sql);
            statement = getPreparedStatement(connection, sql, params);
            // TODO handle multiple results
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
     * Also saves the updateCount, if any, for subsequent examination.
     * <p/>
     * Example usage:
     * <pre>
     * def scott = [firstname: "Scott", lastname: "Davis", id: 5, location_id: 50]
     * sql.execute """
     *     insert into PERSON (id, firstname, lastname, location_id) values ($scott.id, $scott.firstname, $scott.lastname, $scott.location_id)
     * """
     * assert sql.updateCount == 1
     * </pre>
     *
     * @param gstring a GString containing the SQL query with embedded params
     * @return <code>true</code> if the first result is a <code>ResultSet</code>
     *         object; <code>false</code> if it is an update count or there are
     *         no results
     * @throws SQLException if a database access error occurs
     * @see #expand(Object)
     */
    public boolean execute(GString gstring) throws SQLException {
        List<Object> params = getParameters(gstring);
        String sql = asSql(gstring, params);
        return execute(sql, params);
    }

    /**
     * Executes the given SQL statement (typically an INSERT statement).
     * Use this variant when you want to receive the values of any
     * auto-generated columns, such as an autoincrement ID field.
     * See {@link #executeInsert(GString)} for more details.
     *
     * @param sql The SQL statement to execute
     * @return A list of the auto-generated column values for each
     *         inserted row (typically auto-generated keys)
     * @throws SQLException if a database access error occurs
     */
    public List<List<Object>> executeInsert(String sql) throws SQLException {
        Connection connection = createConnection();
        Statement statement = null;
        try {
            log.fine(sql);
            statement = getStatement(connection, sql);
            this.updateCount = statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet keys = statement.getGeneratedKeys();
            return calculateKeys(keys);
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
     * Executes the given SQL statement (typically an INSERT statement).
     * Use this variant when you want to receive the values of any
     * auto-generated columns, such as an autoincrement ID field.
     * The query may contain placeholder question marks which match the given list of parameters.
     * See {@link #executeInsert(GString)} for more details.
     *
     * @param sql    The SQL statement to execute
     * @param params The parameter values that will be substituted
     *               into the SQL statement's parameter slots
     * @return A list of the auto-generated column values for each
     *         inserted row (typically auto-generated keys)
     * @throws SQLException if a database access error occurs
     */
    public List<List<Object>> executeInsert(String sql, List<Object> params) throws SQLException {
        Connection connection = createConnection();
        PreparedStatement statement = null;
        try {
            log.fine(sql);
            statement = getPreparedStatement(connection, sql, params, Statement.RETURN_GENERATED_KEYS);
            this.updateCount = statement.executeUpdate();
            ResultSet keys = statement.getGeneratedKeys();
            return calculateKeys(keys);
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
     * <p>Executes the given SQL statement (typically an INSERT statement).
     * Use this variant when you want to receive the values of any
     * auto-generated columns, such as an autoincrement ID field.
     * The query may contain GString expressions.</p>
     *
     * <p>Generated key values can be accessed using
     * array notation. For example, to return the second auto-generated
     * column value of the third row, use <code>keys[3][1]</code>. The
     * method is designed to be used with SQL INSERT statements, but is
     * not limited to them.</p>
     * 
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
     *     def keys = sql.executeInsert("insert into test_table (INT_DATA, STRING_DATA) "
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
     * @return A list of the auto-generated column values for each
     *         inserted row (typically auto-generated keys)
     * @throws SQLException if a database access error occurs
     * @see #expand(Object)
     */
    public List<List<Object>> executeInsert(GString gstring) throws SQLException {
        List<Object> params = getParameters(gstring);
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
    public int executeUpdate(String sql, List<Object> params) throws SQLException {
        Connection connection = createConnection();
        PreparedStatement statement = null;
        try {
            log.fine(sql);
            statement = getPreparedStatement(connection, sql, params);
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
     * @see #expand(Object)
     */
    public int executeUpdate(GString gstring) throws SQLException {
        List<Object> params = getParameters(gstring);
        String sql = asSql(gstring, params);
        return executeUpdate(sql, params);
    }

    /**
     * Performs a stored procedure call.
     * <p/>
     * Example usage (tested with MySQL) - suppose we have the following stored procedure:
     * <pre>
     * sql.execute """
     *     CREATE PROCEDURE HouseSwap(_first1 VARCHAR(50), _first2 VARCHAR(50))
     *     BEGIN
     *         DECLARE _loc1 INT;
     *         DECLARE _loc2 INT;
     *         SELECT location_id into _loc1 FROM PERSON where firstname = _first1;
     *         SELECT location_id into _loc2 FROM PERSON where firstname = _first2;
     *         UPDATE PERSON
     *         set location_id = case firstname
     *             when _first1 then _loc2
     *             when _first2 then _loc1
     *         end
     *         where (firstname = _first1 OR firstname = _first2);
     *     END
     * """
     * </pre>
     * then you can invoke the procedure as follows:
     * <pre>
     * def rowsChanged = sql.call("{call HouseSwap('Guillaume', 'Paul')}")
     * assert rowsChanged == 2
     * </pre>
     *
     * @param sql the SQL statement
     * @return the number of rows updated or 0 for SQL statements that return nothing
     * @throws SQLException if a database access error occurs
     */
    public int call(String sql) throws Exception {
        return call(sql, EMPTY_LIST);
    }

    /**
     * Performs a stored procedure call with the given embedded parameters.
     * <p/>
     * Example usage - see {@link #call(String)} for more details about
     * creating a <code>HouseSwap(IN name1, IN name2)</code> stored procedure.
     * Once created, it can be called like this:
     * <pre>
     * def p1 = 'Paul'
     * def p2 = 'Guillaume'
     * def rowsChanged = sql.call("{call HouseSwap($p1, $p2)}")
     * assert rowsChanged == 2
     * </pre>
     *
     * @param gstring a GString containing the SQL query with embedded params
     * @return the number of rows updated or 0 for SQL statements that return nothing
     * @throws SQLException if a database access error occurs
     * @see #expand(Object)
     * @see #call(String)
     */
    public int call(GString gstring) throws Exception {
        List<Object> params = getParameters(gstring);
        String sql = asSql(gstring, params);
        return call(sql, params);
    }

    /**
     * Performs a stored procedure call with the given parameters.
     * <p/>
     * Example usage - see {@link #call(String)} for more details about
     * creating a <code>HouseSwap(IN name1, IN name2)</code> stored procedure.
     * Once created, it can be called like this:
     * <pre>
     * def rowsChanged = sql.call("{call HouseSwap(?, ?)}", ['Guillaume', 'Paul'])
     * assert rowsChanged == 2
     * </pre>
     *
     * @param sql    the SQL statement
     * @param params a list of parameters
     * @return the number of rows updated or 0 for SQL statements that return nothing
     * @throws SQLException if a database access error occurs
     * @see #call(String)
     */
    public int call(String sql, List<Object> params) throws Exception {
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
     * <p/>
     * Example usage - suppose we create a stored procedure (ignore its simplistic implementation):
     * <pre>
     * // Tested with MySql 5.0.75
     * sql.execute """
     *     CREATE PROCEDURE Hemisphere(
     *         IN p_firstname VARCHAR(50),
     *         IN p_lastname VARCHAR(50),
     *         OUT ans VARCHAR(50))
     *     BEGIN
     *     DECLARE loc INT;
     *     SELECT location_id into loc FROM PERSON where firstname = p_firstname and lastname = p_lastname;
     *     CASE loc
     *         WHEN 40 THEN
     *             SET ans = 'Southern Hemisphere';
     *         ELSE
     *             SET ans = 'Northern Hemisphere';
     *     END CASE;
     *     END;
     * """
     * </pre>
     * we can now call the stored procedure as follows:
     * <pre>
     * sql.call '{call Hemisphere(?, ?, ?)}', ['Guillaume', 'Laforge', Sql.VARCHAR], { dwells ->
     *     println dwells
     * }
     * </pre>
     * which will output '<code>Northern Hemisphere</code>'.
     * <p/>
     * We can also access stored functions with scalar return values where the return value
     * will be treated as an OUT parameter. Here are examples for various databases for
     * creating such a procedure:
     * <pre>
     * // Tested with MySql 5.0.75
     * sql.execute """
     *     create function FullName(p_firstname VARCHAR(40)) returns VARCHAR(80)
     *     begin
     *         declare ans VARCHAR(80);
     *         SELECT CONCAT(firstname, ' ', lastname) INTO ans FROM PERSON WHERE firstname = p_firstname;
     *         return ans;
     *     end
     * """
     *
     * // Tested with MS SQLServer Express 2008
     * sql.execute """
     *     {@code create function FullName(@firstname VARCHAR(40)) returns VARCHAR(80)}
     *     begin
     *         declare {@code @ans} VARCHAR(80)
     *         {@code SET @ans = (SELECT firstname + ' ' + lastname FROM PERSON WHERE firstname = @firstname)}
     *         return {@code @ans}
     *     end
     * """
     *
     * // Tested with Oracle XE 10g
     * sql.execute """
     *     create function FullName(p_firstname VARCHAR) return VARCHAR is
     *     ans VARCHAR(80);
     *     begin
     *         SELECT CONCAT(CONCAT(firstname, ' '), lastname) INTO ans FROM PERSON WHERE firstname = p_firstname;
     *         return ans;
     *     end;
     * """
     * </pre>
     * and here is how you access the stored function for all databases:
     * <pre>
     * sql.call("{? = call FullName(?)}", [Sql.VARCHAR, 'Sam']) { name ->
     *     assert name == 'Sam Pullara'
     * }
     * </pre>
     *
     * @param sql     the sql statement
     * @param params  a list of parameters
     * @param closure called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     */
    public void call(String sql, List<Object> params, Closure closure) throws Exception {
        Connection connection = createConnection();
        CallableStatement statement = connection.prepareCall(sql);
        List<GroovyResultSet> resultSetResources = new ArrayList<GroovyResultSet>();
        try {
            log.fine(sql);
            setParameters(params, statement);
            // TODO handle multiple results and mechanism for retrieving ResultSet if any (GROOVY-3048)
            statement.execute();
            List<Object> results = new ArrayList<Object>();
            int indx = 0;
            int inouts = 0;
            for (Object value : params) {
                if (value instanceof OutParameter) {
                    if (value instanceof ResultSetOutParameter) {
                        GroovyResultSet resultSet = CallResultSet.getImpl(statement, indx);
                        resultSetResources.add(resultSet);
                        results.add(resultSet);
                    } else {
                        Object o = statement.getObject(indx + 1);
                        if (o instanceof ResultSet) {
                            GroovyResultSet resultSet = new GroovyResultSetProxy((ResultSet) o).getImpl();
                            results.add(resultSet);
                            resultSetResources.add(resultSet);
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
            for (GroovyResultSet rs : resultSetResources) {
                closeResources(null, null, rs);
            }
        }
    }

    /**
     * Performs a stored procedure call with the given parameters,
     * calling the closure once with all result objects.
     * <p/>
     * See {@link #call(String, List, Closure)} for more details about
     * creating a <code>Hemisphere(IN first, IN last, OUT dwells)</code> stored procedure.
     * Once created, it can be called like this:
     * <pre>
     * def first = 'Scott'
     * def last = 'Davis'
     * sql.call "{call Hemisphere($first, $last, ${Sql.VARCHAR})}", { dwells ->
     *     println dwells
     * }
     * </pre>
     * <p/>
     * As another example, see {@link #call(String, List, Closure)} for more details about
     * creating a <code>FullName(IN first)</code> stored function.
     * Once created, it can be called like this:
     * <pre>
     * def first = 'Sam'
     * sql.call("{$Sql.VARCHAR = call FullName($first)}") { name ->
     *     assert name == 'Sam Pullara'
     * }
     * </pre>
     *
     * @param gstring a GString containing the SQL query with embedded params
     * @param closure called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     * @see #call(String, List, Closure)
     * @see #expand(Object)
     */
    public void call(GString gstring, Closure closure) throws Exception {
        List<Object> params = getParameters(gstring);
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
            log.log(Level.FINEST, "Close operation not supported when using datasets - attempt to close ignored");
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
            log.log(Level.FINEST, "Commit operation not supported when using datasets unless using withTransaction or cacheConnection - attempt to commit ignored");
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
            log.log(Level.FINEST, "Rollback operation not supported when using datasets unless using withTransaction or cacheConnection - attempt to rollback ignored");
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
     * Allows a closure to be passed in to configure the JDBC statements before they are executed.
     * It can be used to do things like set the query size etc. When this method is invoked, the supplied
     * closure is saved. Statements subsequently created from other methods will then be
     * configured using this closure. The statement being configured is passed into the closure
     * as its single argument, e.g.:
     * <pre>
     * sql.withStatement{ stmt -> stmt.maxRows == 10 }
     * def firstTenRows = sql.rows("select * from table")
     * </pre>
     *
     * @param configureStatement the closure
     */
    public void withStatement(Closure configureStatement) {
        this.configureStatement = configureStatement;
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
     * If the closure takes a single argument, it will be called
     * with the connection, otherwise it will be called with no arguments.
     *
     * @param closure the given closure
     * @throws SQLException if a database error occurs
     */
    public synchronized void cacheConnection(Closure closure) throws SQLException {
        boolean savedCacheConnection = cacheConnection;
        cacheConnection = true;
        Connection connection = null;
        try {
            connection = createConnection();
            callClosurePossiblyWithConnection(closure, connection);
        }
        finally {
            cacheConnection = false;
            closeResources(connection, null);
            cacheConnection = savedCacheConnection;
            if (dataSource != null && !cacheConnection) {
                useConnection = null;
            }
        }
    }

    /**
     * Performs the closure within a transaction using a cached connection.
     * If the closure takes a single argument, it will be called
     * with the connection, otherwise it will be called with no arguments.
     *
     * @param closure the given closure
     * @throws SQLException if a database error occurs
     */
    public synchronized void withTransaction(Closure closure) throws SQLException {
        boolean savedCacheConnection = cacheConnection;
        cacheConnection = true;
        Connection connection = null;
        try {
            connection = createConnection();
            connection.setAutoCommit(false);
            callClosurePossiblyWithConnection(closure, connection);
            connection.commit();
        } catch (SQLException e) {
            handleError(connection, e);
            throw e;
        } catch (RuntimeException e) {
            handleError(connection, e);
            throw e;
        } catch (Error e) {
            handleError(connection, e);
            throw e;
        } finally {
            if (connection != null) connection.setAutoCommit(true);
            cacheConnection = false;
            closeResources(connection, null);
            cacheConnection = savedCacheConnection;
            if (dataSource != null && !cacheConnection) {
                useConnection = null;
            }
        }
    }

    /**
     * Performs the closure within a batch using a cached connection.
     * The closure will be called with a single argument; the statement
     * associated with this batch. Use it like this:
     * <pre>
     * def updateCounts = sql.withBatch { stmt ->
     *     stmt.addBatch("insert into TABLENAME ...")
     *     stmt.addBatch("insert into TABLENAME ...")
     *     stmt.addBatch("insert into TABLENAME ...")
     * }
     * </pre>
     *
     * @param closure the closure containing batch and optionally other statements
     * @return an array of update counts containing one element for each
     *         command in the batch.  The elements of the array are ordered according
     *         to the order in which commands were added to the batch.
     * @throws SQLException if a database access error occurs,
     *                      or this method is called on a closed <code>Statement</code>, or the
     *                      driver does not support batch statements. Throws {@link java.sql.BatchUpdateException}
     *                      (a subclass of <code>SQLException</code>) if one of the commands sent to the
     *                      database fails to execute properly or attempts to return a result set.
     */
    public synchronized int[] withBatch(Closure closure) throws SQLException {
        boolean savedCacheConnection = cacheConnection;
        cacheConnection = true;
        Connection connection = null;
        Statement statement = null;
        try {
            connection = createConnection();
            connection.setAutoCommit(false);
            statement = createStatement(connection);
            closure.call(statement);
            int[] result = statement.executeBatch();
            connection.commit();
            log.fine("Successfully executed batch with " + result.length + " command(s)");
            return result;
        } catch (SQLException e) {
            handleError(connection, e);
            throw e;
        } catch (RuntimeException e) {
            handleError(connection, e);
            throw e;
        } catch (Error e) {
            handleError(connection, e);
            throw e;
        } finally {
            if (connection != null) connection.setAutoCommit(true);
            cacheConnection = false;
            closeResources(connection, statement);
            cacheConnection = savedCacheConnection;
            if (dataSource != null && !cacheConnection) {
                useConnection = null;
            }
        }
    }

    /**
     * Caches every created preparedStatement in Closure <i>closure</i></br>
     * Every cached preparedStatement is closed after closure has been called.
     * If the closure takes a single argument, it will be called
     * with the connection, otherwise it will be called with no arguments.
     *
     * @param closure the given closure
     * @throws SQLException if a database error occurs
     * @see #setCacheStatements(boolean)
     */
    public synchronized void cacheStatements(Closure closure) throws SQLException {
        boolean savedCacheStatements = cacheStatements;
        cacheStatements = true;
        Connection connection = null;
        try {
            connection = createConnection();
            callClosurePossiblyWithConnection(closure, connection);
        }
        finally {
            cacheStatements = false;
            closeResources(connection, null);
            cacheStatements = savedCacheStatements;
        }
    }

    // protected implementation methods - extension points for subclasses
    //-------------------------------------------------------------------------

    /**
     * Hook to allow derived classes to access ResultSet returned from query.
     *
     * @param sql query to execute
     * @return the resulting ResultSet
     * @throws SQLException if a database error occurs
     */
    protected final ResultSet executeQuery(String sql) throws SQLException {
        return new QueryCommand(sql).execute();
    }

    /**
     * Hook to allow derived classes to access ResultSet returned from query.
     *
     * @param sql query to execute
     * @param params parameters matching question mark placeholders in the query
     * @return the resulting ResultSet
     * @throws SQLException if a database error occurs
     */
	protected final ResultSet executePreparedQuery(String sql, List<Object> params)
			throws SQLException {
		return new PreparedQueryCommand(sql, params).execute();
	}

    /**
     * Hook to allow derived classes to override list of result collection behavior.
     * The default behavior is to return a list of GroovyRowResult objects corresponding
     * to each row in the ResultSet.
     *
     * @param sql query to execute
     * @param rs the ResultSet to process
     * @return the resulting list of rows
     * @throws SQLException if a database error occurs
     */
    protected List<GroovyRowResult> asList(String sql, ResultSet rs) throws SQLException {
        List<GroovyRowResult> results = new ArrayList<GroovyRowResult>();

        try {
            while (rs.next()) {
                results.add(SqlGroovyMethods.toRowResult(rs));
            }
            return (results);
        } catch (SQLException e) {
            log.log(Level.INFO, "Failed to retrieve row from ResultSet for: " + sql, e);
            throw e;
        } finally {
            rs.close();
        }
    }

    /**
     * Hook to allow derived classes to override sql generation from GStrings.
     *
     * @param gstring a GString containing the SQL query with embedded params
     * @param values  the values to embed
     * @return the SQL version of the given query using ? instead of any parameter
     * @see #expand(Object)
     */
    protected String asSql(GString gstring, List<Object> values) {
        String[] strings = gstring.getStrings();
        if (strings.length <= 0) {
            throw new IllegalArgumentException("No SQL specified in GString: " + gstring);
        }
        boolean nulls = false;
        StringBuffer buffer = new StringBuffer();
        boolean warned = false;
        Iterator<Object> iter = values.iterator();
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
     * Hook to allow derived classes to override null handling.
     * Default behavior is to replace ?'"? references with NULLish
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
     * Hook to allow derived classes to override where clause sniffing.
     * Default behavior is to find the first 'where' keyword in the sql
     * doing simple avoidance of the word 'where' within quotes.
     *
     * @param sql the SQL statement
     * @return the index of the found keyword or -1 if not found
     */
    protected int findWhereKeyword(String sql) {
        char[] chars = sql.toLowerCase().toCharArray();
        char[] whereChars = "where".toCharArray();
        int i = 0;
        boolean inString = false; //TODO: Cater for comments?
        int inWhere = 0;
        while (i < chars.length) {
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
     * Hook to allow derived classes to override behavior associated with
     * extracting params from a GString.
     *
     * @param gstring a GString containing the SQL query with embedded params
     * @return extracts the parameters from the expression as a List
     * @see #expand(Object)
     */
    protected List<Object> getParameters(GString gstring) {
        return new ArrayList<Object>(Arrays.asList(gstring.getValues()));
    }

    /**
     * Hook to allow derived classes to override behavior associated with
     * setting params for a prepared statement. Default behavior is to
     * append the parameters to the given statement using <code>setObject</code>.
     *
     * @param params    the parameters to append
     * @param statement the statement
     * @throws SQLException if a database access error occurs
     */
    protected void setParameters(List<Object> params, PreparedStatement statement) throws SQLException {
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
     * @param i         the index of the object of interest
     * @param value     the new object value
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

    /**
     * An extension point allowing derived classes to change the behavior of
     * connection creation. The default behavior is to either use the
     * supplied connection or obtain it from the supplied datasource.
     *
     * @return the connection associated with this Sql
     * @throws java.sql.SQLException if a SQL error occurs
     */
    protected Connection createConnection() throws SQLException {
        if ((cacheStatements || cacheConnection) && useConnection != null) {
            return useConnection;
        }
        if (dataSource != null) {
            // Use a doPrivileged here as many different properties need to be
            // read, and the policy shouldn't have to list them all.
            Connection con;
            try {
                con = AccessController.doPrivileged(new PrivilegedExceptionAction<Connection>() {
                    public Connection run() throws SQLException {
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
        }
        return useConnection;
    }

    /**
     * An extension point allowing derived classes to change the behavior
     * of resource closing.
     *
     * @param connection the connection to close
     * @param statement  the statement to close
     * @param results    the results to close
     */
    protected void closeResources(Connection connection, Statement statement, ResultSet results) {
        if (results != null) {
            try {
                results.close();
            }
            catch (SQLException e) {
                log.log(Level.INFO, "Caught exception closing resultSet: " + e.getMessage() + " - continuing", e);
            }
        }
        closeResources(connection, statement);
    }

    /**
     * An extension point allowing the behavior of resource closing to be
     * overridden in derived classes.
     *
     * @param connection the connection to close
     * @param statement  the statement to close
     */
    protected void closeResources(Connection connection, Statement statement) {
        if (cacheStatements) return;
        if (statement != null) {
            try {
                statement.close();
            }
            catch (SQLException e) {
                log.log(Level.INFO, "Caught exception closing statement: " + e.getMessage() + " - continuing", e);
            }
        }
        if (cacheConnection) return;
        if (dataSource != null) {
            try {
                connection.close();
            }
            catch (SQLException e) {
                log.log(Level.INFO, "Caught exception closing connection: " + e.getMessage() + " - continuing", e);
            }
        }
    }

    /**
     * Provides a hook for derived classes to be able to configure JDBC statements.
     * Default behavior is to call a previously saved closure, if any, using the
     * statement as a parameter.
     *
     * @param statement the statement to configure
     */
    protected void configure(Statement statement) {
        // for thread safety, grab local copy
        Closure configureStatement = this.configureStatement;
        if (configureStatement != null) {
            configureStatement.call(statement);
        }
    }

    // private implementation methods
    //-------------------------------------------------------------------------

    private List<List<Object>> calculateKeys(ResultSet keys) throws SQLException {
        // Prepare a list to contain the auto-generated column
        // values, and then fetch them from the statement.
        List<List<Object>> autoKeys = new ArrayList<List<Object>>();
        int count = keys.getMetaData().getColumnCount();

        // Copy the column values into a list of a list.
        while (keys.next()) {
            List<Object> rowKeys = new ArrayList<Object>(count);
            for (int i = 1; i <= count; i++) {
                rowKeys.add(keys.getObject(i));
            }

            autoKeys.add(rowKeys);
        }
        return autoKeys;
    }

    private Statement createStatement(Connection connection) throws SQLException {
        if (resultSetHoldability == -1) {
            return connection.createStatement(resultSetType, resultSetConcurrency);
        }
        return connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    private void handleError(Connection connection, Throwable t) throws SQLException {
        if (connection != null) {
            log.log(Level.INFO, "Rolling back due to: " + t.getMessage(), t);
            connection.rollback();
        }
    }

    private void callClosurePossiblyWithConnection(Closure closure, Connection connection) {
        if (closure.getMaximumNumberOfParameters() == 1) {
            closure.call(connection);
        } else {
            closure.call();
        }
    }

    private void clearStatementCache() {
        Statement statements[];
        synchronized (statementCache) {
            if (statementCache.isEmpty())
                return;
            // Arrange to call close() outside synchronized block, since
            // the close may involve server requests.
            statements = new Statement[statementCache.size()];
            statementCache.values().toArray(statements);
            statementCache.clear();
        }
        for (Statement s : statements) {
            try {
                s.close();
            } catch (SQLException e) {
                log.log(Level.INFO, "Failed to close statement. Already closed?", e);
                // If there's a closed statement in the cache, the cache is corrupted.
            }
        }
    }

    private Statement getAbstractStatement(AbstractStatementCommand cmd, Connection connection, String sql) throws SQLException {
        Statement stmt;
        if (cacheStatements) {
            synchronized (statementCache) { // checking for existence without sync can cause leak if object needs close().
                stmt = statementCache.get(sql);
                if (stmt == null) {
                    stmt = cmd.execute(connection, sql);
                    statementCache.put(sql, stmt);
                }
            }
        } else {
            stmt = cmd.execute(connection, sql);
        }
        return stmt;
    }

    private Statement getStatement(Connection connection, String sql) throws SQLException {
        Statement stmt = getAbstractStatement(new CreateStatementCommand(), connection, sql);
        configure(stmt);
        return stmt;
    }

    private PreparedStatement getPreparedStatement(Connection connection, String sql, List<Object> params, int returnGeneratedKeys) throws SQLException {
        PreparedStatement statement = (PreparedStatement) getAbstractStatement(new CreatePreparedStatementCommand(returnGeneratedKeys), connection, sql);
        setParameters(params, statement);
        configure(statement);
        return statement;
    }

    private PreparedStatement getPreparedStatement(Connection connection, String sql, List<Object> params) throws SQLException {
        return getPreparedStatement(connection, sql, params, 0);
    }

    // command pattern implementation classes
    //-------------------------------------------------------------------------

    private abstract class AbstractStatementCommand {
        /**
         * Execute the command that's defined by the subclass following
         * the Command pattern.  Specialized parameters are held in the command instances.
         *
         * @param conn all commands accept a connection
         * @param sql  all commands accept an SQL statement
         * @return statement that can be cached, etc.
         * @throws SQLException if a database error occurs
         */
        abstract Statement execute(Connection conn, String sql) throws SQLException;
    }

    private class CreatePreparedStatementCommand extends AbstractStatementCommand {
        private final int returnGeneratedKeys;

        CreatePreparedStatementCommand(int returnGeneratedKeys) {
            this.returnGeneratedKeys = returnGeneratedKeys;
        }

        PreparedStatement execute(Connection connection, String sql) throws SQLException {
            if (returnGeneratedKeys != 0)
                return connection.prepareStatement(sql, returnGeneratedKeys);
            if (appearsLikeStoredProc(sql))
                return connection.prepareCall(sql);
            return connection.prepareStatement(sql);
        }

        boolean appearsLikeStoredProc(String sql) {
            return sql.matches("\\s*[{]?\\s*[?]?\\s*[=]?\\s*[cC][aA][lL][lL].*");
        }
    }

    private class CreateStatementCommand extends AbstractStatementCommand {

        @Override
        Statement execute(Connection conn, String sql) throws SQLException {
            return createStatement(conn);
		}

	}

    private abstract class AbstractQueryCommand {
    	protected final String sql;
    	protected Statement statement;

    	AbstractQueryCommand(String sql) {
    		// Don't create statement in subclass constructors to avoid throw in constructors
    		this.sql = sql;
    	}

        /**
         * Execute the command that's defined by the subclass following
         * the Command pattern.  Specialized parameters are held in the command instances.
         *
         * @return ResultSet from executing a query
         * @throws SQLException if a database error occurs
         */
         final ResultSet execute() throws SQLException {
     		Connection connection = createConnection();
    		Statement statement = null;
    		try {
    			log.fine(sql);
    			// The variation in the pattern is isolated
    			ResultSet result = runQuery(connection);
    			assert (null != statement);
    			return result;
    		} catch (SQLException e) {
    			log.log(Level.FINE, "Failed to execute: " + sql, e);
    			throw e;
    		} finally {
    			closeResources(connection, statement, null);
    		}
         }

         /**
          * Perform the query. Must set statement field so that the main ({@link #execute()}) method can clean up.
          * This is the method that encloses the variant part of the code.
          * @param connection the connection to use
          * @return ResultSet from an executeQuery method.
          * @throws SQLException if a database error occurs
          */
         protected abstract ResultSet runQuery(Connection connection) throws SQLException;
    }

    private class PreparedQueryCommand extends AbstractQueryCommand {
    	private List<Object> params;

		PreparedQueryCommand(String sql, List<Object> queryParams) {
			super(sql);
			params = queryParams;
		}

		@Override
		protected ResultSet runQuery(Connection connection) throws SQLException {
			PreparedStatement s = getPreparedStatement(connection, sql, params);
			statement = s;
			return s.executeQuery();
		}
    }

    private class QueryCommand extends AbstractQueryCommand {
    	
    	QueryCommand(String sql) {
    		super(sql);
    	}
    	
    	@Override
    	protected ResultSet runQuery(Connection connection) throws SQLException {
    		statement = getStatement(connection, sql);
    		return statement.executeQuery(sql);
    	}
    }
}
