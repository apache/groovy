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

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

/**
 * Represents an extent of objects
 *
 * @author Chris Stevenson
 * @author <a href="mailto:james@coredevelopers.net">James Strachan </a>
 * @version $Revision$
 */
public class Sql {

    protected Logger log = Logger.getLogger(getClass().getName());

    private DataSource dataSource;

    private Connection useConnection;

    /** lets only warn of using deprecated methods once */
    private boolean warned;

    // store the last row count for executeUpdate
    int updateCount = 0;

    /** allows a closure to be used to configure the statement before its use */
    private Closure configureStatement;

    /**
     * A helper method which creates a new Sql instance from a JDBC connection
     * URL
     *
     * @param url
     * @return a new Sql instance with a connection
     */
    public static Sql newInstance(String url) throws SQLException {
        Connection connection = DriverManager.getConnection(url);
        return new Sql(connection);
    }

    /**
     * A helper method which creates a new Sql instance from a JDBC connection
     * URL
     *
     * @param url
     * @return a new Sql instance with a connection
     */
    public static Sql newInstance(String url, Properties properties) throws SQLException {
        Connection connection = DriverManager.getConnection(url, properties);
        return new Sql(connection);
    }

    /**
     * A helper method which creates a new Sql instance from a JDBC connection
     * URL and driver class name
     *
     * @param url
     * @return a new Sql instance with a connection
     */
    public static Sql newInstance(String url, Properties properties, String driverClassName) throws SQLException, ClassNotFoundException {
        loadDriver(driverClassName);
        return newInstance(url, properties);
    }

    /**
     * A helper method which creates a new Sql instance from a JDBC connection
     * URL, username and password
     *
     * @param url
     * @return a new Sql instance with a connection
     */
    public static Sql newInstance(String url, String user, String password) throws SQLException {
        Connection connection = DriverManager.getConnection(url, user, password);
        return new Sql(connection);
    }

    /**
     * A helper method which creates a new Sql instance from a JDBC connection
     * URL, username, password and driver class name
     *
     * @param url
     * @return a new Sql instance with a connection
     */
    public static Sql newInstance(String url, String user, String password, String driverClassName) throws SQLException,
            ClassNotFoundException {
        loadDriver(driverClassName);
        return newInstance(url, user, password);
    }

    /**
     * A helper method which creates a new Sql instance from a JDBC connection
     * URL and driver class name
     *
     * @param url
     * @param driverClassName
     *            the class name of the driver
     * @return a new Sql instance with a connection
     */
    public static Sql newInstance(String url, String driverClassName) throws SQLException, ClassNotFoundException {
        loadDriver(driverClassName);
        return newInstance(url);
    }

    /**
     * Attempts to load the JDBC driver on the thread, current or system class
     * loaders
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
     * @param type the JDBC data type
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
     * @param type the JDBC data type.
     * @return an OutParameter
     */
    public static OutParameter out(final int type){
        return new OutParameter(){
            public int getType() {
                return type;
            }
        };
    }
    
    /**
     * Create an inout parameter using this in parameter.
     * @param in
     */
    public static InOutParameter inout(final InParameter in){
        return new InOutParameter(){
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
     * @param type the JDBC data type.
     * @return a ResultSetOutParameter
     */
    public static ResultSetOutParameter resultSet(final int type){
        return new ResultSetOutParameter(){
            public int getType() {
                return type;
            }
        };
    }
        
    /**
     * Creates a variable to be expanded in the Sql string rather
     * than representing an sql parameter.
     * @param object
     */
    public static ExpandedVariable expand(final Object object){
        return new ExpandedVariable(){
            public Object getObject() {
                return object;
            }};
    }
    
    /**
     * Constructs an SQL instance using the given DataSource. Each operation
     * will use a Connection from the DataSource pool and close it when the
     * operation is completed putting it back into the pool.
     *
     * @param dataSource
     */
    public Sql(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Construts an SQL instance using the given Connection. It is the callers
     * responsibility to close the Connection after the Sql instance has been
     * used. You can do this on the connection object directly or by calling the
     * {@link java.sql.Connection#close()}  method.
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

    /**
     * Performs the given SQL query with parameters calling the closure with the
     * result set
     */
    public void query(String sql, List params, Closure closure) throws SQLException {
        Connection connection = createConnection();
        PreparedStatement statement = null;
        ResultSet results = null;
        try {
            log.fine(sql);
            statement = connection.prepareStatement(sql);
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
     * Performs the given SQL query calling the closure with the result set
     */
    public void query(GString gstring, Closure closure) throws SQLException {
        List params = getParameters(gstring);
        String sql = asSql(gstring, params);
        query(sql, params, closure);
    }

    /**
     * @deprecated please use eachRow instead
     */
    public void queryEach(String sql, Closure closure) throws SQLException {
        warnDeprecated();
        eachRow(sql, closure);
    }

    /**
     * Performs the given SQL query calling the closure with each row of the
     * result set
     */
    public void eachRow(String sql, Closure closure) throws SQLException {
        Connection connection = createConnection();
        Statement statement = connection.createStatement();
        configure(statement);
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
            log.log(Level.FINE, "Failed to execute: " + sql, e);
            throw e;
        }
        finally {
            closeResources(connection, statement, results);
        }
    }

    /**
     * @deprecated please use eachRow instead
     */
    public void queryEach(String sql, List params, Closure closure) throws SQLException {
        warnDeprecated();
        eachRow(sql, params, closure);
    }

    /**
     * Performs the given SQL query calling the closure with the result set
     */
    public void eachRow(String sql, List params, Closure closure) throws SQLException {
        Connection connection = createConnection();
        PreparedStatement statement = null;
        ResultSet results = null;
        try {
            log.fine(sql);
            statement = connection.prepareStatement(sql);
            setParameters(params, statement);
            configure(statement);
            results = statement.executeQuery();

            GroovyResultSet groovyRS = new GroovyResultSet(results);
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
     * Performs the given SQL query calling the closure with the result set
     */
    public void eachRow(GString gstring, Closure closure) throws SQLException {
        List params = getParameters(gstring);
        String sql = asSql(gstring, params);
        eachRow(sql, params, closure);
    }

    /**
     * @deprecated please use eachRow instead
     */
    public void queryEach(GString gstring, Closure closure) throws SQLException {
        warnDeprecated();
        eachRow(gstring, closure);
    }

    /**
     * Performs the given SQL query and return the rows of the result set
     */
     public List rows(String sql) throws SQLException {
        List results = new ArrayList();
        Connection connection = createConnection();
        Statement statement = connection.createStatement();
        configure(statement);
        ResultSet rs = null;
        try {
            log.fine(sql);
            rs = statement.executeQuery(sql);
            while (rs.next()) {
                ResultSetMetaData metadata = rs.getMetaData();
                LinkedHashMap lhm = new LinkedHashMap(metadata.getColumnCount(),1,true);
                for(int i=1 ; i<=metadata.getColumnCount() ; i++) {
                      lhm.put(metadata.getColumnName(i),rs.getObject(i));
                }
                GroovyRowResult row = new GroovyRowResult(lhm);
                results.add(row);
            }
            return(results);
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
     * Performs the given SQL query and return the first row of the result set
     */
    public Object firstRow(String sql) throws SQLException {
        List rows = rows(sql);
        if (rows.isEmpty()) return null;
        return(rows.get(0));
    }

    /**
     * Performs the given SQL query with the list of params and return
     * the rows of the result set
     */
    public List rows(String sql, List params) throws SQLException {
        List results = new ArrayList();
        Connection connection = createConnection();
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            log.fine(sql);
            statement = connection.prepareStatement(sql);
            setParameters(params, statement);
            configure(statement);
            rs = statement.executeQuery();
            while (rs.next()) {
                ResultSetMetaData metadata = rs.getMetaData();
                LinkedHashMap lhm = new LinkedHashMap(metadata.getColumnCount(),1,true);
                for(int i=1 ; i<=metadata.getColumnCount() ; i++) {
                    lhm.put(metadata.getColumnName(i),rs.getObject(i));
                }
                GroovyRowResult row = new GroovyRowResult(lhm);
                results.add(row);
            }
            return(results);
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
      * Performs the given SQL query with the list of params and return
      * the first row of the result set
      */
    public Object firstRow(String sql, List params) throws SQLException {
         List rows = rows(sql, params);
         if (rows.isEmpty()) return null;
         return rows.get(0);
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
     * Executes the given SQL statement. See {@link #executeInsert(GString)}
     * for more details. 
     * @param sql The SQL statement to execute.
     * @return A list of the auto-generated column values for each
     * inserted row.
     */
    public List executeInsert(String sql) throws SQLException {
        Connection connection = createConnection();
        Statement statement = null;
        try {
            log.fine(sql);
            statement = connection.createStatement();
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
     * Executes the given piece of SQL with parameters
     */
    public boolean execute(String sql, List params) throws SQLException {
        Connection connection = createConnection();
        PreparedStatement statement = null;
        try {
            log.fine(sql);
            statement = connection.prepareStatement(sql);
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
     * Executes the given SQL statement with a particular list of
     * parameter values. See {@link #executeInsert(GString)} for
     * more details. 
     * @param sql The SQL statement to execute.
     * @param params The parameter values that will be substituted
     * into the SQL statement's parameter slots.
     * @return A list of the auto-generated column values for each
     * inserted row.
     */
    public List executeInsert(String sql, List params) throws SQLException {
        // Now send the SQL to the database.
        Connection connection = createConnection();
        PreparedStatement statement = null;
        try {
            log.fine(sql);

            // Prepare a statement for the SQL and then execute it.
            statement = connection.prepareStatement(sql);
            setParameters(params, statement);
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
     * Executes the given SQL with embedded expressions inside
     */
    public boolean execute(GString gstring) throws SQLException {
        List params = getParameters(gstring);
        String sql = asSql(gstring, params);
        return execute(sql, params);
    }

    /**
     * Executes the given SQL update with embedded expressions inside
     * 
     * @return the number of rows updated
     */
    public int executeUpdate(GString gstring) throws SQLException {
        List params = getParameters(gstring);
        String sql = asSql(gstring, params);
        return executeUpdate(sql, params);
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
     *
     *     def keys = sql.insert("insert into test_table (INT_DATA, STRING_DATA) "
     *                           + "VALUES (1, 'Key Largo')")
     *
     *     def id = keys[0][0]
     *
     *     // 'id' now contains the value of the new row's ID column.
     *     // It can be used to update an object representation's
     *     // id attribute for example.
     *     ...
     * </pre>
     * @return A list of column values representing each row's
     * auto-generated keys.
     */
    public List executeInsert(GString gstring) throws SQLException {
        List params = getParameters(gstring);
        String sql = asSql(gstring, params);
        return executeInsert(sql, params);
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
            for (Iterator iter = params.iterator(); iter.hasNext();) {
                Object value = iter.next();
                if(value instanceof OutParameter){
                    if(value instanceof ResultSetOutParameter){
                        results.add(new CallResultSet(statement,indx));
                    }else{
                        Object o = statement.getObject(indx+1);
                        if(o instanceof ResultSet){
                            results.add(new GroovyResultSet((ResultSet)o));
                        }else{
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
     * Performs a stored procedure call with the given parameters
     */
    public int call(GString gstring) throws Exception {
        List params = getParameters(gstring);
        String sql = asSql(gstring, params);
        return call(sql, params);
    }


    /**
     * Performs a stored procedure call with the given parameters,
     * calling the closure once with all result objects.
     */
    public void call(GString gstring, Closure closure) throws Exception {
        List params = getParameters(gstring);
        String sql = asSql(gstring,params);
        call(sql, params,closure);
    }
    
    /**
     * If this SQL object was created with a Connection then this method closes
     * the connection. If this SQL object was created from a DataSource then
     * this method does nothing.
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


    public void commit() {
        try {
            this.useConnection.commit();
        }
        catch (SQLException e) {
            log.log(Level.SEVERE, "Caught exception commiting connection: " + e, e);
        }
    }

    public void rollback() {
        try {
            this.useConnection.rollback();
        }
        catch (SQLException e) {
            log.log(Level.SEVERE, "Caught exception rollbacking connection: " + e, e);
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
     * @param configureStatement
     */
    public void withStatement(Closure configureStatement) {
        this.configureStatement = configureStatement;
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    /**
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
                    if(value instanceof ExpandedVariable){
                        buffer.append(((ExpandedVariable)value).getObject());
                        iter.remove();
                    }else{
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
                }
                else {
                    nulls = true;
                    buffer.append("?'\"?"); // will replace these with nullish
                    // values
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
     * @param sql
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
            Pattern[] patterns = { Pattern.compile("(?is)^(.{" + firstWhere + "}.*?)!=\\s{0,1}(\\s*)\\?'\"\\?(.*)"),
                    Pattern.compile("(?is)^(.{" + firstWhere + "}.*?)<>\\s{0,1}(\\s*)\\?'\"\\?(.*)"),
                    Pattern.compile("(?is)^(.{" + firstWhere + "}.*?[^<>])=\\s{0,1}(\\s*)\\?'\"\\?(.*)"), };
            String[] replacements = { "$1 is not $2null$3", "$1 is not $2null$3", "$1 is $2null$3", };
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
     * @param sql
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
                    if (inString) {
                        inString = false;
                    }
                    else {
                        inString = true;
                    }
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
     * @return extracts the parameters from the expression as a List
     */
    protected List getParameters(GString gstring) {
        Object[] values = gstring.getValues();
        List answer = new ArrayList(values.length);
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) {
                answer.add(values[i]);
            }
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
    protected void setObject(PreparedStatement statement, int i, Object value)
        throws SQLException {
        if (value instanceof InParameter  || value instanceof OutParameter) {
            if(value instanceof InParameter){
                InParameter in = (InParameter) value;
                Object val = in.getValue();
                if (null == val) {
                    statement.setNull(i, in.getType());
                } else {
                    statement.setObject(i, val, in.getType());
                }
            }
            if(value instanceof OutParameter){
                try{
                    OutParameter out = (OutParameter)value;
                    ((CallableStatement)statement).registerOutParameter(i,out.getType());
                }catch(ClassCastException e){
                    throw new SQLException("Cannot register out parameter.");
                }
            }
        } else {
            statement.setObject(i, value);
        }
    }

    protected Connection createConnection() throws SQLException {
        if (dataSource != null) {
            //Use a doPrivileged here as many different properties need to be
            // read, and the policy
            //shouldn't have to list them all.
            Connection con = null;
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
                }
                else {
                    throw (RuntimeException) e;
                }
            }
            return con;
        }
        else {
            //System.out.println("createConnection returning: " +
            // useConnection);
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

    private void warnDeprecated() {
        if (!warned) {
            warned = true;
            log.warning("queryEach() is deprecated, please use eachRow() instead");
        }
    }

    /**
     * Provides a hook to be able to configure JDBC statements, such as to configure
     *
     * @param statement
     */
    protected void configure(Statement statement) {
        if (configureStatement != null) {
            configureStatement.call(statement);
        }
    }
}
