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
package groovy.sql;

import groovy.lang.Closure;
import groovy.lang.GString;
import groovy.lang.MissingPropertyException;
import groovy.lang.Tuple;
import groovy.transform.NamedParam;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.SimpleType;
import org.codehaus.groovy.runtime.InvokerHelper;

import javax.sql.DataSource;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.groovy.sql.extensions.SqlExtensions.toRowResult;

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
 *
 * <h4>Typical usage</h4>
 *
 * First you need to set up your sql instance. There are several constructors
 * and a few <code>newInstance</code> factory methods available to do this.
 * In simple cases, you can just provide
 * the necessary details to set up a connection (e.g. for hsqldb):
 * <pre>
 * def db = [url:'jdbc:hsqldb:mem:testDB', user:'sa', password:'', driver:'org.hsqldb.jdbc.JDBCDriver']
 * def sql = Sql.newInstance(db.url, db.user, db.password, db.driver)
 * </pre>
 * or if you have an existing connection (perhaps from a connection pool) or a
 * datasource use one of the constructors:
 * <pre>
 * def sql = new Sql(datasource)
 * </pre>
 * Now you can invoke sql, e.g. to create a table:
 * <pre>
 * sql.execute '''
 *     create table PROJECT (
 *         id integer not null,
 *         name varchar(50),
 *         url varchar(100),
 *     )
 * '''
 * </pre>
 * Or insert a row using JDBC PreparedStatement inspired syntax:
 * <pre>
 * def params = [10, 'Groovy', 'http://groovy.codehaus.org']
 * sql.execute 'insert into PROJECT (id, name, url) values (?, ?, ?)', params
 * </pre>
 * Or insert a row using GString syntax:
 * <pre>
 * def map = [id:20, name:'Grails', url:'http://grails.codehaus.org']
 * sql.execute "insert into PROJECT (id, name, url) values ($map.id, $map.name, $map.url)"
 * </pre>
 * Or a row update:
 * <pre>
 * def newUrl = 'http://grails.org'
 * def project = 'Grails'
 * sql.executeUpdate "update PROJECT set url=$newUrl where name=$project"
 * </pre>
 * Now try a query using <code>eachRow</code>:
 * <pre>
 * println 'Some GR8 projects:'
 * sql.eachRow('select * from PROJECT') { row ->
 *     println "${row.name.padRight(10)} ($row.url)"
 * }
 * </pre>
 * Which will produce something like this:
 * <pre>
 * Some GR8 projects:
 * Groovy     (http://groovy.codehaus.org)
 * Grails     (http://grails.org)
 * Griffon    (http://griffon.codehaus.org)
 * Gradle     (http://gradle.org)
 * </pre>
 * Now try a query using <code>rows</code>:
 * <pre>
 * def rows = sql.rows("select * from PROJECT where name like 'Gra%'")
 * assert rows.size() == 2
 * println rows.join('\n')
 * </pre>
 * with output like this:
 * <pre>
 * [ID:20, NAME:Grails, URL:http://grails.org]
 * [ID:40, NAME:Gradle, URL:http://gradle.org]
 * </pre>
 * Also, <code>eachRow</code> and <code>rows</code> support paging.  Here's an example: 
 * <pre>
 * sql.eachRow('select * from PROJECT', 2, 2) { row ->
 *     println "${row.name.padRight(10)} ($row.url)"
 * }
 * </pre>
 * Which will start at the second row and return a maximum of 2 rows.  Here's an example result:
 * <pre>
 * Grails     (http://grails.org)
 * Griffon    (http://griffon.codehaus.org)
 * </pre>
 * 
 * Finally, we should clean up:
 * <pre>
 * sql.close()
 * </pre>
 * If we are using a DataSource and we haven't enabled statement caching, then
 * strictly speaking the final <code>close()</code> method isn't required - as all connection
 * handling is performed transparently on our behalf; however, it doesn't hurt to
 * have it there as it will return silently in that case.
 * <p>
 * If instead of <code>newInstance</code> you use <code>withInstance</code>, then
 * <code>close()</code> will be called automatically for you.
 *
 * <h4>Avoiding SQL injection</h4>
 *
 * If you find yourself creating queries based on any kind of input from the user or
 * a 3rd party application you might wish to avoid the pure string method variants in this class.
 * While this is safe:
 * <code>sql.firstRow('select * from PersonTable')</code>
 * This example is potentially at risk of SQL injection:
 * <code>sql.firstRow('select * from PersonTable where SurnameColumn = ' + userInput)</code>
 * This in turn will be fine if '<code>userInput</code>' is something like 'Smith' but maybe
 * not so fine if '<code>userInput</code>' is something like 'Smith; DROP table PersonTable'.
 * Instead, use one of the variants with parameters and placeholders:
 * <code>sql.firstRow("select * from PersonTable where SurnameColumn = ?", [userInput])</code>
 * or the GString variants which will be converted to the placeholder variants under the covers:
 * <code>sql.firstRow("select * from PersonTable where SurnameColumn = $userInput")</code>
 * or the named parameter variants discussed next.
 *
 * <h4>Named and named ordinal parameters</h4>
 *
 * Several of the methods in this class (ones which have a String-based sql query and params in
 * a List&lt;Object&gt; or Object[] or Map) support <em>named</em> or <em>named ordinal</em> parameters.
 * These methods are useful for queries with large numbers of parameters - though the GString
 * variations are often preferred in such cases too. Reminder: when you see a variant with Object[] as
 * the type of the last parameter, Groovy allows vararg style parameters so you don't explicitly need to
 * create an Object[] and if the first parameter is of type Map, Groovy supports named arguments - examples
 * of both are contained in the examples below.
 * <p>
 * Named parameter queries use placeholder values in the query String. Two forms are supported
 * ':propname1' and '?.propname2'. For these variations, a single <em>model</em> object is
 * supplied in the parameter list/array/map. The propname refers to a property of that model object.
 * The model object could be a map, Expando or domain class instance. Here are some examples:
 * <pre>
 * // using rows() with a named parameter with the parameter supplied in a map
 * println sql.rows('select * from PROJECT where name=:foo', [foo:'Gradle'])
 * // as above for eachRow()
 * sql.eachRow('select * from PROJECT where name=:foo', [foo:'Gradle']) {
 *     // process row
 * }
 *
 * // an example using both the ':' and '?.' variants of the notation
 * println sql.rows('select * from PROJECT where name=:foo and id=?.bar', [foo:'Gradle', bar:40])
 * // as above but using Groovy's named arguments instead of an explicit map
 * println sql.rows('select * from PROJECT where name=:foo and id=?.bar', foo:'Gradle', bar:40)
 *
 * // an example showing rows() with a domain object instead of a map
 * class MyDomainClass { def baz = 'Griffon' }
 * println sql.rows('select * from PROJECT where name=?.baz', new MyDomainClass())
 * // as above for eachRow() with the domain object supplied in a list
 * sql.eachRow('select * from PROJECT where name=?.baz', [new MyDomainClass()]) {
 *     // process row
 * }
 * </pre>
 * Named ordinal parameter queries have multiple model objects with the index number (starting
 * at 1) also supplied in the placeholder. Only the question mark variation of placeholder is supported.
 * Here are some examples:
 * <pre>
 * // an example showing the model objects as vararg style parameters (since rows() has an Object[] variant)
 * println sql.rows("select * from PROJECT where name=?1.baz and id=?2.num", new MyDomainClass(), [num:30])
 *
 * // an example showing the model objects (one domain class and one map) provided in a list
 * sql.eachRow("select * from PROJECT where name=?1.baz and id=?2.num", [new MyDomainClass(), [num:30]]) {
 *     // do something with row
 * }
 * </pre>
 *
 * <h4>More details</h4>
 *
 * See the method and constructor JavaDoc for more details.
 * <p>
 * For advanced usage, the class provides numerous extension points for overriding the
 * facade behavior associated with the various aspects of managing
 * the interaction with the underlying database.
 * <p>
 * This class is <b>not</b> thread-safe.
 */
public class Sql implements AutoCloseable {

    /**
     * Hook to allow derived classes to access the log
     */
    protected static final Logger LOG = Logger.getLogger(Sql.class.getName());

    private static final List<Object> EMPTY_LIST = Collections.emptyList();
    private static final int USE_COLUMN_NAMES = -1;
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

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

    private boolean cacheNamedQueries = true;

    private boolean enableNamedQueries = true;

    private boolean withinBatch;

    private final Map<String, Statement> statementCache = new HashMap<String, Statement>();
    private final Map<String, String> namedParamSqlCache = new HashMap<String, String>();
    private final Map<String, List<Tuple>> namedParamIndexPropCache = new HashMap<String, List<Tuple>>();
    private List<String> keyColumnNames;

    /**
     * Creates a new Sql instance given a JDBC connection URL.
     *
     * @param url a database url of the form
     *            <code>jdbc:<em>subprotocol</em>:<em>subname</em></code>
     * @return a new Sql instance with a connection
     * @throws SQLException if a database access error occurs
     */
    public static Sql newInstance(String url) throws SQLException {
        Connection connection = DriverManager.getConnection(url);
        return new Sql(connection);
    }

    /**
     * Invokes a closure passing it a new Sql instance created from the given JDBC connection URL.
     * The created connection will be closed if required.
     *
     * @param url a database url of the form
     *            <code>jdbc:<em>subprotocol</em>:<em>subname</em></code>
     * @param c the Closure to call
     * @see #newInstance(String)
     * @throws SQLException if a database access error occurs
     */
    public static void withInstance(String url, Closure c) throws SQLException {
        try (Sql sql = newInstance(url)) {
            c.call(sql);
        }
    }

    /**
     * Creates a new Sql instance given a JDBC connection URL
     * and some properties.
     *
     * @param url        a database url of the form
     *                   <code>jdbc:<em>subprotocol</em>:<em>subname</em></code>
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
     * Invokes a closure passing it a new Sql instance created from the given JDBC connection URL and properties.
     * The created connection will be closed if required.
     *
     * @param url a database url of the form
     *            <code>jdbc:<em>subprotocol</em>:<em>subname</em></code>
     * @param properties a list of arbitrary string tag/value pairs
     *                   as connection arguments; normally at least a "user" and
     *                   "password" property should be included
     * @param c the Closure to call
     * @see #newInstance(String, java.util.Properties)
     * @throws SQLException if a database access error occurs
     */
    public static void withInstance(String url, Properties properties, Closure c) throws SQLException {
        try (Sql sql = newInstance(url, properties)) {
            c.call(sql);
        }
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
     * @throws ClassNotFoundException if the driver class cannot be found or loaded
     */
    public static Sql newInstance(String url, Properties properties, String driverClassName)
            throws SQLException, ClassNotFoundException {
        loadDriver(driverClassName);
        return newInstance(url, properties);
    }

    /**
     * Invokes a closure passing it a new Sql instance created from the given JDBC connection URL,
     * properties and driver classname. The created connection will be closed if required.
     *
     * @param url a database url of the form
     *            <code>jdbc:<em>subprotocol</em>:<em>subname</em></code>
     * @param properties a list of arbitrary string tag/value pairs
     *                   as connection arguments; normally at least a "user" and
     *                   "password" property should be included
     * @param driverClassName the fully qualified class name of the driver class
     * @param c the Closure to call
     * @see #newInstance(String, java.util.Properties, String)
     * @throws SQLException if a database access error occurs
     * @throws ClassNotFoundException if the driver class cannot be found or loaded
     */
    public static void withInstance(String url, Properties properties, String driverClassName, Closure c)
            throws SQLException, ClassNotFoundException {
        try (Sql sql = newInstance(url, properties, driverClassName)) {
            c.call(sql);
        }
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
     * Invokes a closure passing it a new Sql instance created from the given JDBC connection URL, user and password.
     * The created connection will be closed if required.
     *
     * @param url a database url of the form
     *            <code>jdbc:<em>subprotocol</em>:<em>subname</em></code>
     * @param user     the database user on whose behalf the connection
     *                 is being made
     * @param password the user's password
     * @param c the Closure to call
     * @see #newInstance(String, String, String)
     * @throws SQLException if a database access error occurs
     */
    public static void withInstance(String url, String user, String password, Closure c) throws SQLException {
        try (Sql sql = newInstance(url, user, password)) {
            c.call(sql);
        }
    }

    /**
     * Creates a new Sql instance given a JDBC connection URL,
     * a username, a password and a driver class name.
     *
     * @param url             a database url of the form
     *                        <code>jdbc:<em>subprotocol</em>:<em>subname</em></code>
     * @param user            the database user on whose behalf the connection
     *                        is being made
     * @param password        the user's password
     * @param driverClassName the fully qualified class name of the driver class
     * @return a new Sql instance with a connection
     * @throws SQLException           if a database access error occurs
     * @throws ClassNotFoundException if the driver class cannot be found or loaded
     */
    public static Sql newInstance(String url, String user, String password, String driverClassName)
            throws SQLException, ClassNotFoundException {
        loadDriver(driverClassName);
        return newInstance(url, user, password);
    }

    /**
     * Invokes a closure passing it a new Sql instance created from the given JDBC connection URL.
     * The created connection will be closed if required.
     *
     * @param url a database url of the form
     *            <code>jdbc:<em>subprotocol</em>:<em>subname</em></code>
     * @param user            the database user on whose behalf the connection
     *                        is being made
     * @param password        the user's password
     * @param driverClassName the fully qualified class name of the driver class
     * @param c the Closure to call
     * @see #newInstance(String, String, String, String)
     * @throws SQLException if a database access error occurs
     * @throws ClassNotFoundException if the driver class cannot be found or loaded
     */
    public static void withInstance(String url, String user, String password, String driverClassName, Closure c)
            throws SQLException, ClassNotFoundException {
        try (Sql sql = newInstance(url, user, password, driverClassName)) {
            c.call(sql);
        }
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
     * @throws ClassNotFoundException if the driver class cannot be found or loaded
     */
    public static Sql newInstance(String url, String driverClassName) throws SQLException, ClassNotFoundException {
        loadDriver(driverClassName);
        return newInstance(url);
    }

    /**
     * Invokes a closure passing it a new Sql instance created from the given JDBC connection URL.
     * The created connection will be closed if required.
     *
     * @param url a database url of the form
     *            <code>jdbc:<em>subprotocol</em>:<em>subname</em></code>
     * @param driverClassName the fully qualified class name of the driver class
     * @param c the Closure to call
     * @see #newInstance(String, String)
     * @throws SQLException if a database access error occurs
     * @throws ClassNotFoundException if the driver class cannot be found or loaded
     */
    public static void withInstance(String url, String driverClassName, Closure c)
            throws SQLException, ClassNotFoundException {
        try (Sql sql = newInstance(url, driverClassName)) {
            c.call(sql);
        }
    }

    /**
     * Creates a new Sql instance given parameters in a Map.
     * Recognized keys for the Map include:
     * <pre>
     * driverClassName the fully qualified class name of the driver class
     * driver          a synonym for driverClassName
     * url             a database url of the form: <code>jdbc:<em>subprotocol</em>:<em>subname</em></code>
     * user            the database user on whose behalf the connection is being made
     * password        the user's password
     * properties      a list of arbitrary string tag/value pairs as connection arguments;
     *                 normally at least a "user" and "password" property should be included
     * <em>other</em>           any of the public setter methods of this class may be used with property notation
     *                 e.g. <em>cacheStatements: true, resultSetConcurrency: ResultSet.CONCUR_READ_ONLY</em>
     * </pre>
     * Of these, '<code>url</code>' is required. Others may be needed depending on your database.<br>
     * If '<code>properties</code>' is supplied, neither '<code>user</code>' nor '<code>password</code>' should be supplied.<br>
     * If one of '<code>user</code>' or '<code>password</code>' is supplied, both should be supplied.
     *<p>
     * Example usage:
     * <pre>
     * import groovy.sql.Sql
     * import static java.sql.ResultSet.*
     *
     * def sql = Sql.newInstance(
     *     url:'jdbc:hsqldb:mem:testDB',
     *     user:'sa',
     *     password:'',
     *     driver:'org.hsqldb.jdbc.JDBCDriver',
     *     cacheStatements: true,
     *     resultSetConcurrency: CONCUR_READ_ONLY
     * )
     * </pre>
     * 
     * @param args a Map contain further arguments
     * @return a new Sql instance with a connection
     * @throws SQLException           if a database access error occurs
     * @throws ClassNotFoundException if the driver class cannot be found or loaded
     */
    public static Sql newInstance(
            @NamedParam(value = "url", type = String.class, required = true)
            @NamedParam(value = "properties", type = Properties.class)
            @NamedParam(value = "driverClassName", type = String.class)
            @NamedParam(value = "driver", type = String.class)
            @NamedParam(value = "user", type = String.class)
            @NamedParam(value = "password", type = String.class)
            @NamedParam(value = "cacheNamedQueries", type = Boolean.class)
            @NamedParam(value = "cacheStatements", type = Boolean.class)
            @NamedParam(value = "enableNamedQueries", type = Boolean.class)
            @NamedParam(value = "resultSetConcurrency", type = Integer.class)
            @NamedParam(value = "resultSetHoldability", type = Integer.class)
            @NamedParam(value = "resultSetType", type = Integer.class)
            // TODO below will be deleted once we fix type checker to understand
            // readonly Map otherwise seen as Map<String, Serializable>
            @NamedParam(value = "unused", type = Object.class)
            Map<String, Object> args) throws SQLException, ClassNotFoundException {
        if (!args.containsKey("url"))
            throw new IllegalArgumentException("Argument 'url' is required");

        if (args.get("url") == null)
            throw new IllegalArgumentException("Argument 'url' must not be null");

        if (args.containsKey("driverClassName") && args.containsKey("driver"))
            throw new IllegalArgumentException("Only one of 'driverClassName' and 'driver' should be provided");

        // Make a copy so destructive operations will not affect the caller
        Map<String, Object> sqlArgs = new HashMap<String, Object>(args);

        sqlArgs.remove("unused"); // TODO remove
        Object driverClassName = sqlArgs.remove("driverClassName");
        if (driverClassName == null) driverClassName = sqlArgs.remove("driver");
        if (driverClassName != null) loadDriver(driverClassName.toString());

        Properties props = (Properties) sqlArgs.remove("properties");
        if (props != null && sqlArgs.containsKey("user"))
            throw new IllegalArgumentException("Only one of 'properties' and 'user' should be supplied");
        if (props != null && sqlArgs.containsKey("password"))
            throw new IllegalArgumentException("Only one of 'properties' and 'password' should be supplied");
        if (sqlArgs.containsKey("user") ^ sqlArgs.containsKey("password"))
            throw new IllegalArgumentException("Found one but not both of 'user' and 'password'");

        Object url = sqlArgs.remove("url");
        Connection connection;
        LOG.fine("url = " + url);
        if (props != null) {
            connection = DriverManager.getConnection(url.toString(), props);
            if (LOG.isLoggable(Level.FINE)) {
                if (!props.containsKey("password")) {
                    LOG.fine("props = " + props);
                } else {
                    // don't log the password
                    Properties propsCopy = new Properties();
                    propsCopy.putAll(props);
                    propsCopy.setProperty("password", "***");
                    LOG.fine("props = " + propsCopy);
                }
            }
        } else if (sqlArgs.containsKey("user")) {
            Object user = sqlArgs.remove("user");
            LOG.fine("user = " + user);
            Object password = sqlArgs.remove("password");
            LOG.fine("password = " + (password == null ? "null" : "***"));
            connection = DriverManager.getConnection(url.toString(),
                    (user == null ? null : user.toString()),
                    (password == null ? null : password.toString()));
        } else {
            LOG.fine("No user/password specified");
            connection = DriverManager.getConnection(url.toString());
        }

        Sql result = (Sql) InvokerHelper.invokeConstructorOf(Sql.class, sqlArgs);
        result.setConnection(connection);
        return result;
    }

    /**
     * Invokes a closure passing it a new Sql instance created from the given map of arguments.
     * The created connection will be closed if required.
     *
     * @param args a Map contain further arguments
     * @param c the Closure to call
     * @see #newInstance(java.util.Map)
     * @throws SQLException if a database access error occurs
     * @throws ClassNotFoundException if the driver class cannot be found or loaded
     */
    public static void withInstance(
            @NamedParam(value = "url", type = String.class, required = true)
            @NamedParam(value = "properties", type = Properties.class)
            @NamedParam(value = "driverClassName", type = String.class)
            @NamedParam(value = "driver", type = String.class)
            @NamedParam(value = "user", type = String.class)
            @NamedParam(value = "password", type = String.class)
            @NamedParam(value = "cacheNamedQueries", type = Boolean.class)
            @NamedParam(value = "cacheStatements", type = Boolean.class)
            @NamedParam(value = "enableNamedQueries", type = Boolean.class)
            @NamedParam(value = "resultSetConcurrency", type = Integer.class)
            @NamedParam(value = "resultSetHoldability", type = Integer.class)
            @NamedParam(value = "resultSetType", type = Integer.class)
            // TODO below will be deleted once we fix type checker to understand
            // readonly Map otherwise seen as Map<String, Serializable>
            @NamedParam(value = "unused", type = Object.class)
            Map<String, Object> args,
            Closure c) throws SQLException, ClassNotFoundException {
        try (Sql sql = newInstance(args)) {
            c.call(sql);
        }
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

    public static final int NO_RESULT_SETS = 0;
    public static final int FIRST_RESULT_SET = 1;
    public static final int ALL_RESULT_SETS = 2;

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
     * <p>
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
     * used. Depending on which features you are using, you may be able to do
     * this on the connection object directly but the preferred approach is to
     * call the {@link #close()} method which will close the connection but also
     * free any cached resources.
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

    private Sql() {
        // supports Map style newInstance method
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
     * <p>
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
     * <p>
     * All resources including the ResultSet are closed automatically
     * after the closure is called.
     *
     * @param sql     the sql statement
     * @param closure called for each row with a <code>ResultSet</code>
     * @throws SQLException if a database access error occurs
     */
    public void query(String sql, @ClosureParams(value=SimpleType.class, options="java.sql.ResultSet") Closure closure) throws SQLException {
        Connection connection = createConnection();
        Statement statement = null;
        ResultSet results = null;
        try {
            statement = getStatement(connection, sql);
            results = statement.executeQuery(sql);
            closure.call(results);
        } catch (SQLException e) {
            LOG.warning("Failed to execute: " + sql + " because: " + e.getMessage());
            throw e;
        } finally {
            closeResources(connection, statement, results);
        }
    }

    /**
     * Performs the given SQL query, which should return a single
     * <code>ResultSet</code> object. The given closure is called
     * with the <code>ResultSet</code> as its argument.
     * The query may contain placeholder question marks which match the given list of parameters.
     * <p>
     * Example usage:
     * <pre>
     * sql.query('select * from PERSON where lastname like ?', ['%a%']) { ResultSet rs ->
     *     while (rs.next()) println rs.getString('lastname')
     * }
     * </pre>
     * <p>
     * This method supports named and named ordinal parameters.
     * See the class Javadoc for more details.
     * <p>
     * All resources including the ResultSet are closed automatically
     * after the closure is called.
     *
     * @param sql     the sql statement
     * @param params  a list of parameters
     * @param closure called for each row with a <code>ResultSet</code>
     * @throws SQLException if a database access error occurs
     */
    public void query(String sql, List<Object> params, @ClosureParams(value=SimpleType.class, options="java.sql.ResultSet") Closure closure) throws SQLException {
        Connection connection = createConnection();
        PreparedStatement statement = null;
        ResultSet results = null;
        try {
            statement = getPreparedStatement(connection, sql, params);
            results = statement.executeQuery();
            closure.call(results);
        } catch (SQLException e) {
            LOG.warning("Failed to execute: " + sql + " because: " + e.getMessage());
            throw e;
        } finally {
            closeResources(connection, statement, results);
        }
    }

    /**
     * A variant of {@link #query(String, java.util.List, groovy.lang.Closure)}
     * useful when providing the named parameters as a map.
     *
     * @param sql     the sql statement
     * @param map     a map containing the named parameters
     * @param closure called for each row with a <code>ResultSet</code>
     * @throws SQLException if a database access error occurs
     * @since 1.8.7
     */
    public void query(String sql, Map map, @ClosureParams(value=SimpleType.class, options="java.sql.ResultSet") Closure closure) throws SQLException {
        query(sql, singletonList(map), closure);
    }

    /**
     * A variant of {@link #query(String, java.util.List, groovy.lang.Closure)}
     * useful when providing the named parameters as named arguments.
     *
     * @param map     a map containing the named parameters
     * @param sql     the sql statement
     * @param closure called for each row with a <code>ResultSet</code>
     * @throws SQLException if a database access error occurs
     * @since 1.8.7
     */
    public void query(Map map, String sql, @ClosureParams(value=SimpleType.class, options="java.sql.ResultSet") Closure closure) throws SQLException {
        query(sql, singletonList(map), closure);
    }

    private static ArrayList<Object> singletonList(Object item) {
        ArrayList<Object> params = new ArrayList<Object>();
        params.add(item);
        return params;
    }

    /**
     * Performs the given SQL query, which should return a single
     * <code>ResultSet</code> object. The given closure is called
     * with the <code>ResultSet</code> as its argument.
     * The query may contain GString expressions.
     * <p>
     * Example usage:
     * <pre>
     * def location = 25
     * sql.query "select * from PERSON where location_id < $location", { ResultSet rs ->
     *     while (rs.next()) println rs.getString('firstname')
     * }
     * </pre>
     * <p>
     * All resources including the ResultSet are closed automatically
     * after the closure is called.
     *
     * @param gstring a GString containing the SQL query with embedded params
     * @param closure called for each row with a <code>ResultSet</code>
     * @throws SQLException if a database access error occurs
     * @see #expand(Object)
     */
    public void query(GString gstring, @ClosureParams(value=SimpleType.class, options="java.sql.ResultSet") Closure closure) throws SQLException {
        List<Object> params = getParameters(gstring);
        String sql = asSql(gstring, params);
        query(sql, params, closure);
    }

    /**
     * Performs the given SQL query calling the given Closure with each row of the result set.
     * The row will be a <code>GroovyResultSet</code> which is a <code>ResultSet</code>
     * that supports accessing the fields using property style notation and ordinal index values.
     * <p>
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
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param sql     the sql statement
     * @param closure called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     */
    public void eachRow(String sql, @ClosureParams(value=SimpleType.class, options="groovy.sql.GroovyResultSet") Closure closure) throws SQLException {
        eachRow(sql, (Closure) null, closure);
    }

    /**
     * Performs the given SQL query calling the given <code>closure</code> with each row of the result set starting at
     * the provided <code>offset</code>, and including up to <code>maxRows</code> number of rows.
     * The row will be a <code>GroovyResultSet</code> which is a <code>ResultSet</code>
     * that supports accessing the fields using property style notation and ordinal index values.
     * <p>
     * Note that the underlying implementation is based on either invoking <code>ResultSet.absolute()</code>,
     * or if the ResultSet type is <code>ResultSet.TYPE_FORWARD_ONLY</code>, the <code>ResultSet.next()</code> method
     * is invoked equivalently.  The first row of a ResultSet is 1, so passing in an offset of 1 or less has no effect
     * on the initial positioning within the result set.
     * <p>
     * Note that different database and JDBC driver implementations may work differently with respect to this method.
     * Specifically, one should expect that <code>ResultSet.TYPE_FORWARD_ONLY</code> may be less efficient than a
     * "scrollable" type.
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param sql     the sql statement
     * @param offset  the 1-based offset for the first row to be processed
     * @param maxRows the maximum number of rows to be processed
     * @param closure called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     */
    public void eachRow(String sql, int offset, int maxRows,
                        @ClosureParams(value=SimpleType.class, options="groovy.sql.GroovyResultSet") Closure closure) throws SQLException {
        eachRow(sql, (Closure) null, offset, maxRows, closure);
    }

    /**
     * Performs the given SQL query calling the given <code>rowClosure</code> with each row of the
     * result set.
     * The row will be a <code>GroovyResultSet</code> which is a <code>ResultSet</code>
     * that supports accessing the fields using property style notation and ordinal index values.
     * In addition, the <code>metaClosure</code> will be called once passing in the
     * <code>ResultSetMetaData</code> as argument.
     * <p>
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
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param sql         the sql statement
     * @param metaClosure called for meta data (only once after sql execution)
     * @param rowClosure  called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     */
    public void eachRow(String sql, @ClosureParams(value=SimpleType.class, options="java.sql.ResultSetMetaData") Closure metaClosure,
                        @ClosureParams(value=SimpleType.class, options="groovy.sql.GroovyResultSet") Closure rowClosure) throws SQLException {
        eachRow(sql, metaClosure, 0, 0, rowClosure);
    }

    /**
     * Performs the given SQL query calling the given <code>rowClosure</code> with each row of the result set starting at
     * the provided <code>offset</code>, and including up to <code>maxRows</code> number of rows.
     * The row will be a <code>GroovyResultSet</code> which is a <code>ResultSet</code>
     * that supports accessing the fields using property style notation and ordinal index values.
     * <p>
     * In addition, the <code>metaClosure</code> will be called once passing in the
     * <code>ResultSetMetaData</code> as argument.
     * <p>
     * Note that the underlying implementation is based on either invoking <code>ResultSet.absolute()</code>,
     * or if the ResultSet type is <code>ResultSet.TYPE_FORWARD_ONLY</code>, the <code>ResultSet.next()</code> method
     * is invoked equivalently.  The first row of a ResultSet is 1, so passing in an offset of 1 or less has no effect
     * on the initial positioning within the result set.
     * <p>
     * Note that different database and JDBC driver implementations may work differently with respect to this method.
     * Specifically, one should expect that <code>ResultSet.TYPE_FORWARD_ONLY</code> may be less efficient than a
     * "scrollable" type.
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param sql         the sql statement
     * @param offset      the 1-based offset for the first row to be processed
     * @param maxRows     the maximum number of rows to be processed
     * @param metaClosure called for meta data (only once after sql execution)
     * @param rowClosure  called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     */
    public void eachRow(String sql,
                        @ClosureParams(value=SimpleType.class, options="java.sql.ResultSetMetaData") Closure metaClosure, int offset, int maxRows,
                        @ClosureParams(value=SimpleType.class, options="groovy.sql.GroovyResultSet") Closure rowClosure) throws SQLException {
        Connection connection = createConnection();
        Statement statement = null;
        ResultSet results = null;
        try {
            statement = getStatement(connection, sql);
            results = statement.executeQuery(sql);
            if (metaClosure != null) metaClosure.call(results.getMetaData());
            boolean cursorAtRow = moveCursor(results, offset);
            if (!cursorAtRow) return;

            GroovyResultSet groovyRS = new GroovyResultSetProxy(results).getImpl();
            int i = 0;
            while ((maxRows <= 0 || i++ < maxRows) && groovyRS.next()) {
                rowClosure.call(groovyRS);
            }
        } catch (SQLException e) {
            LOG.warning("Failed to execute: " + sql + " because: " + e.getMessage());
            throw e;
        } finally {
            closeResources(connection, statement, results);
        }
    }

    private static boolean moveCursor(ResultSet results, int offset) throws SQLException {
        boolean cursorAtRow = true;
        if (results.getType() == ResultSet.TYPE_FORWARD_ONLY) {
            int i = 1;
            while (i++ < offset && cursorAtRow) {
                cursorAtRow = results.next();
            }
        } else if (offset > 1) {
            cursorAtRow = results.absolute(offset - 1);
        }
        return cursorAtRow;
    }

    /**
     * Performs the given SQL query calling the given <code>rowClosure</code> with each row of the result set starting at
     * the provided <code>offset</code>, and including up to <code>maxRows</code> number of rows.
     * The row will be a <code>GroovyResultSet</code> which is a <code>ResultSet</code>
     * that supports accessing the fields using property style notation and ordinal index values.
     * <p>
     * In addition, the <code>metaClosure</code> will be called once passing in the
     * <code>ResultSetMetaData</code> as argument.
     * The query may contain placeholder question marks which match the given list of parameters.
     * <p>
     * Note that the underlying implementation is based on either invoking <code>ResultSet.absolute()</code>,
     * or if the ResultSet type is <code>ResultSet.TYPE_FORWARD_ONLY</code>, the <code>ResultSet.next()</code> method
     * is invoked equivalently.  The first row of a ResultSet is 1, so passing in an offset of 1 or less has no effect
     * on the initial positioning within the result set.
     * <p>
     * Note that different database and JDBC driver implementations may work differently with respect to this method.
     * Specifically, one should expect that <code>ResultSet.TYPE_FORWARD_ONLY</code> may be less efficient than a
     * "scrollable" type.
     *
     * @param sql         the sql statement
     * @param params      a list of parameters
     * @param offset      the 1-based offset for the first row to be processed
     * @param maxRows     the maximum number of rows to be processed
     * @param metaClosure called for meta data (only once after sql execution)
     * @param rowClosure  called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     */
    public void eachRow(String sql, List<Object> params,
                        @ClosureParams(value=SimpleType.class, options="java.sql.ResultSetMetaData") Closure metaClosure, int offset, int maxRows,
                        @ClosureParams(value=SimpleType.class, options="groovy.sql.GroovyResultSet") Closure rowClosure) throws SQLException {
        Connection connection = createConnection();
        PreparedStatement statement = null;
        ResultSet results = null;
        try {
            statement = getPreparedStatement(connection, sql, params);
            results = statement.executeQuery();
            if (metaClosure != null) metaClosure.call(results.getMetaData());
            boolean cursorAtRow = moveCursor(results, offset);
            if (!cursorAtRow) return;

            GroovyResultSet groovyRS = new GroovyResultSetProxy(results).getImpl();
            int i = 0;
            while ((maxRows <= 0 || i++ < maxRows) && groovyRS.next()) {
                rowClosure.call(groovyRS);
            }
        } catch (SQLException e) {
            LOG.warning("Failed to execute: " + sql + " because: " + e.getMessage());
            throw e;
        } finally {
            closeResources(connection, statement, results);
        }
    }

    /**
     * A variant of {@link #eachRow(String, java.util.List, groovy.lang.Closure, int, int, groovy.lang.Closure)}
     * allowing the named parameters to be supplied in a map.
     *
     * @param sql         the sql statement
     * @param map         a map containing the named parameters
     * @param offset      the 1-based offset for the first row to be processed
     * @param maxRows     the maximum number of rows to be processed
     * @param metaClosure called for meta data (only once after sql execution)
     * @param rowClosure  called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     * @since 1.8.7
     */
    public void eachRow(String sql, Map map,
                        @ClosureParams(value=SimpleType.class, options="java.sql.ResultSetMetaData") Closure metaClosure, int offset, int maxRows,
                        @ClosureParams(value=SimpleType.class, options="groovy.sql.GroovyResultSet") Closure rowClosure) throws SQLException {
        eachRow(sql, singletonList(map), metaClosure, offset, maxRows, rowClosure);
    }

    /**
     * A variant of {@link #eachRow(String, java.util.List, groovy.lang.Closure, int, int, groovy.lang.Closure)}
     * allowing the named parameters to be supplied as named arguments.
     *
     * @param map         a map containing the named parameters
     * @param sql         the sql statement
     * @param offset      the 1-based offset for the first row to be processed
     * @param maxRows     the maximum number of rows to be processed
     * @param metaClosure called for meta data (only once after sql execution)
     * @param rowClosure  called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     * @since 1.8.7
     */
    public void eachRow(Map map, String sql,
                        @ClosureParams(value=SimpleType.class, options="java.sql.ResultSetMetaData") Closure metaClosure, int offset, int maxRows,
                        @ClosureParams(value=SimpleType.class, options="groovy.sql.GroovyResultSet") Closure rowClosure) throws SQLException {
        eachRow(sql, singletonList(map), metaClosure, offset, maxRows, rowClosure);
    }

    /**
     * Performs the given SQL query calling the given Closure with each row of the result set.
     * The row will be a <code>GroovyResultSet</code> which is a <code>ResultSet</code>
     * that supports accessing the fields using property style notation and ordinal index values.
     * In addition, the <code>metaClosure</code> will be called once passing in the
     * <code>ResultSetMetaData</code> as argument.
     * The query may contain placeholder question marks which match the given list of parameters.
     * <p>
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
     * sql.eachRow("select * from PERSON where lastname like ?", ['%a%'], printColNames, printRow)
     * </pre>
     * <p>
     * This method supports named and named ordinal parameters.
     * See the class Javadoc for more details.
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param sql         the sql statement
     * @param params      a list of parameters
     * @param metaClosure called for meta data (only once after sql execution)
     * @param rowClosure  called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     */
    public void eachRow(String sql, List<Object> params,
                        @ClosureParams(value=SimpleType.class, options="java.sql.ResultSetMetaData") Closure metaClosure,
                        @ClosureParams(value=SimpleType.class, options="groovy.sql.GroovyResultSet") Closure rowClosure) throws SQLException {
        eachRow(sql, params, metaClosure, 0, 0, rowClosure);
    }

    /**
     * A variant of {@link #eachRow(String, java.util.List, groovy.lang.Closure, groovy.lang.Closure)}
     * useful when providing the named parameters as a map.
     *
     * @param sql         the sql statement
     * @param params      a map of named parameters
     * @param metaClosure called for meta data (only once after sql execution)
     * @param rowClosure  called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     * @since 1.8.7
     */
    public void eachRow(String sql, Map params,
                        @ClosureParams(value=SimpleType.class, options="java.sql.ResultSetMetaData") Closure metaClosure,
                        @ClosureParams(value=SimpleType.class, options="groovy.sql.GroovyResultSet") Closure rowClosure) throws SQLException {
        eachRow(sql, singletonList(params), metaClosure, rowClosure);
    }

    /**
     * A variant of {@link #eachRow(String, java.util.List, groovy.lang.Closure, groovy.lang.Closure)}
     * useful when providing the named parameters as named arguments.
     *
     * @param params      a map of named parameters
     * @param sql         the sql statement
     * @param metaClosure called for meta data (only once after sql execution)
     * @param rowClosure  called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     * @since 1.8.7
     */
    public void eachRow(Map params, String sql,
                        @ClosureParams(value=SimpleType.class, options="java.sql.ResultSetMetaData") Closure metaClosure,
                        @ClosureParams(value=SimpleType.class, options="groovy.sql.GroovyResultSet") Closure rowClosure) throws SQLException {
        eachRow(sql, singletonList(params), metaClosure, rowClosure);
    }

    /**
     * Performs the given SQL query calling the given Closure with each row of the result set.
     * The row will be a <code>GroovyResultSet</code> which is a <code>ResultSet</code>
     * that supports accessing the fields using property style notation and ordinal index values.
     * The query may contain placeholder question marks which match the given list of parameters.
     * <p>
     * Example usage:
     * <pre>
     * sql.eachRow("select * from PERSON where lastname like ?", ['%a%']) { row ->
     *     println "${row[1]} $row.lastname"
     * }
     * </pre>
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param sql     the sql statement
     * @param params  a list of parameters
     * @param closure called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     */
    public void eachRow(String sql, List<Object> params,
                        @ClosureParams(value=SimpleType.class, options="groovy.sql.GroovyResultSet") Closure closure) throws SQLException {
        eachRow(sql, params, null, closure);
    }

    /**
     * A variant of {@link #eachRow(String, java.util.List, groovy.lang.Closure)}
     * useful when providing the named parameters as a map.
     *
     * @param sql     the sql statement
     * @param params  a map of named parameters
     * @param closure called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     * @since 1.8.7
     */
    public void eachRow(String sql, Map params,
                        @ClosureParams(value=SimpleType.class, options="groovy.sql.GroovyResultSet") Closure closure) throws SQLException {
        eachRow(sql, singletonList(params), closure);
    }

    /**
     * A variant of {@link #eachRow(String, java.util.List, groovy.lang.Closure)}
     * useful when providing the named parameters as named arguments.
     *
     * @param params  a map of named parameters
     * @param sql     the sql statement
     * @param closure called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     * @since 1.8.7
     */
    public void eachRow(Map params, String sql,
                        @ClosureParams(value=SimpleType.class, options="groovy.sql.GroovyResultSet") Closure closure) throws SQLException {
        eachRow(sql, singletonList(params), closure);
    }

    /**
     * Performs the given SQL query calling the given <code>closure</code> with each row of the result set starting at
     * the provided <code>offset</code>, and including up to <code>maxRows</code> number of rows.
     * The row will be a <code>GroovyResultSet</code> which is a <code>ResultSet</code>
     * that supports accessing the fields using property style notation and ordinal index values.
     * The query may contain placeholder question marks which match the given list of parameters.
     * <p>
     * Note that the underlying implementation is based on either invoking <code>ResultSet.absolute()</code>,
     * or if the ResultSet type is <code>ResultSet.TYPE_FORWARD_ONLY</code>, the <code>ResultSet.next()</code> method
     * is invoked equivalently.  The first row of a ResultSet is 1, so passing in an offset of 1 or less has no effect
     * on the initial positioning within the result set.
     * <p>
     * Note that different database and JDBC driver implementations may work differently with respect to this method.
     * Specifically, one should expect that <code>ResultSet.TYPE_FORWARD_ONLY</code> may be less efficient than a
     * "scrollable" type.
     *
     * @param sql     the sql statement
     * @param params  a list of parameters
     * @param offset  the 1-based offset for the first row to be processed
     * @param maxRows the maximum number of rows to be processed
     * @param closure called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     */
    public void eachRow(String sql, List<Object> params, int offset, int maxRows,
                        @ClosureParams(value=SimpleType.class, options="groovy.sql.GroovyResultSet") Closure closure) throws SQLException {
        eachRow(sql, params, null, offset, maxRows, closure);
    }

    /**
     * A variant of {@link #eachRow(String, java.util.List, int, int, groovy.lang.Closure)}
     * useful when providing the named parameters as a map.
     *
     * @param sql     the sql statement
     * @param params  a map of named parameters
     * @param offset  the 1-based offset for the first row to be processed
     * @param maxRows the maximum number of rows to be processed
     * @param closure called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     * @since 1.8.7
     */
    public void eachRow(String sql, Map params, int offset, int maxRows,
                        @ClosureParams(value=SimpleType.class, options="groovy.sql.GroovyResultSet") Closure closure) throws SQLException {
        eachRow(sql, singletonList(params), offset, maxRows, closure);
    }

    /**
     * A variant of {@link #eachRow(String, java.util.List, int, int, groovy.lang.Closure)}
     * useful when providing the named parameters as named arguments.
     *
     * @param params  a map of named parameters
     * @param sql     the sql statement
     * @param offset  the 1-based offset for the first row to be processed
     * @param maxRows the maximum number of rows to be processed
     * @param closure called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     * @since 1.8.7
     */
    public void eachRow(Map params, String sql, int offset, int maxRows,
                        @ClosureParams(value=SimpleType.class, options="groovy.sql.GroovyResultSet") Closure closure) throws SQLException {
        eachRow(sql, singletonList(params), offset, maxRows, closure);
    }

    /**
     * Performs the given SQL query calling the given Closure with each row of the result set.
     * The row will be a <code>GroovyResultSet</code> which is a <code>ResultSet</code>
     * that supports accessing the fields using property style notation and ordinal index values.
     * <p>
     * In addition, the <code>metaClosure</code> will be called once passing in the
     * <code>ResultSetMetaData</code> as argument.
     * The query may contain GString expressions.
     * <p>
     * Example usage:
     * <pre>
     * def location = 25
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
     * sql.eachRow("select * from PERSON where location_id < $location", printColNames, printRow)
     * </pre>
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param gstring     a GString containing the SQL query with embedded params
     * @param metaClosure called for meta data (only once after sql execution)
     * @param rowClosure  called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     * @see #expand(Object)
     */
    public void eachRow(GString gstring, @ClosureParams(value=SimpleType.class, options="java.sql.ResultSetMetaData") Closure metaClosure,
                        @ClosureParams(value=SimpleType.class, options="groovy.sql.GroovyResultSet") Closure rowClosure) throws SQLException {
        List<Object> params = getParameters(gstring);
        String sql = asSql(gstring, params);
        eachRow(sql, params, metaClosure, rowClosure);
    }

    /**
     * Performs the given SQL query calling the given <code>closure</code> with each row of the result set starting at
     * the provided <code>offset</code>, and including up to <code>maxRows</code> number of rows.
     * The row will be a <code>GroovyResultSet</code> which is a <code>ResultSet</code>
     * that supports accessing the fields using property style notation and ordinal index values.
     * In addition, the <code>metaClosure</code> will be called once passing in the
     * <code>ResultSetMetaData</code> as argument.
     * The query may contain GString expressions.
     * <p>
     * Note that the underlying implementation is based on either invoking <code>ResultSet.absolute()</code>,
     * or if the ResultSet type is <code>ResultSet.TYPE_FORWARD_ONLY</code>, the <code>ResultSet.next()</code> method
     * is invoked equivalently.  The first row of a ResultSet is 1, so passing in an offset of 1 or less has no effect
     * on the initial positioning within the result set.
     * <p>
     * Note that different database and JDBC driver implementations may work differently with respect to this method.
     * Specifically, one should expect that <code>ResultSet.TYPE_FORWARD_ONLY</code> may be less efficient than a
     * "scrollable" type.
     *
     * @param gstring     a GString containing the SQL query with embedded params
     * @param metaClosure called for meta data (only once after sql execution)
     * @param offset      the 1-based offset for the first row to be processed
     * @param maxRows     the maximum number of rows to be processed
     * @param rowClosure  called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     */
    public void eachRow(GString gstring,
                        @ClosureParams(value=SimpleType.class, options="java.sql.ResultSetMetaData") Closure metaClosure, int offset, int maxRows,
                        @ClosureParams(value=SimpleType.class, options="groovy.sql.GroovyResultSet") Closure rowClosure) throws SQLException {
        List<Object> params = getParameters(gstring);
        String sql = asSql(gstring, params);
        eachRow(sql, params, metaClosure, offset, maxRows, rowClosure);
    }

    /**
     * Performs the given SQL query calling the given <code>closure</code> with each row of the result set starting at
     * the provided <code>offset</code>, and including up to <code>maxRows</code> number of rows.
     * The row will be a <code>GroovyResultSet</code> which is a <code>ResultSet</code>
     * that supports accessing the fields using property style notation and ordinal index values.
     * The query may contain GString expressions.
     * <p>
     * Note that the underlying implementation is based on either invoking <code>ResultSet.absolute()</code>,
     * or if the ResultSet type is <code>ResultSet.TYPE_FORWARD_ONLY</code>, the <code>ResultSet.next()</code> method
     * is invoked equivalently.  The first row of a ResultSet is 1, so passing in an offset of 1 or less has no effect
     * on the initial positioning within the result set.
     * <p>
     * Note that different database and JDBC driver implementations may work differently with respect to this method.
     * Specifically, one should expect that <code>ResultSet.TYPE_FORWARD_ONLY</code> may be less efficient than a
     * "scrollable" type.
     *
     * @param gstring a GString containing the SQL query with embedded params
     * @param offset  the 1-based offset for the first row to be processed
     * @param maxRows the maximum number of rows to be processed
     * @param closure called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     */
    public void eachRow(GString gstring, int offset, int maxRows,
                        @ClosureParams(value=SimpleType.class, options="groovy.sql.GroovyResultSet") Closure closure) throws SQLException {
        List<Object> params = getParameters(gstring);
        String sql = asSql(gstring, params);
        eachRow(sql, params, offset, maxRows, closure);
    }

    /**
     * Performs the given SQL query calling the given Closure with each row of the result set.
     * The row will be a <code>GroovyResultSet</code> which is a <code>ResultSet</code>
     * that supports accessing the fields using property style notation and ordinal index values.
     * The query may contain GString expressions.
     * <p>
     * Example usage:
     * <pre>
     * def location = 25
     * sql.eachRow("select * from PERSON where location_id < $location") { row ->
     *     println row.firstname
     * }
     * </pre>
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param gstring a GString containing the SQL query with embedded params
     * @param closure called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     * @see #expand(Object)
     */
    public void eachRow(GString gstring, @ClosureParams(value=SimpleType.class, options="groovy.sql.GroovyResultSet") Closure closure) throws SQLException {
        eachRow(gstring, null, closure);
    }

    /**
     * Performs the given SQL query and return the rows of the result set.
     * <p>
     * Example usage:
     * <pre>
     * def ans = sql.rows("select * from PERSON where firstname like 'S%'")
     * println "Found ${ans.size()} rows"
     * </pre>
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param sql the SQL statement
     * @return a list of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     */
    public List<GroovyRowResult> rows(String sql) throws SQLException {
        return rows(sql, 0, 0, null);
    }

    /**
     * Performs the given SQL query and return a "page" of rows from the result set.  A page is defined as starting at
     * a 1-based offset, and containing a maximum number of rows.
     * <p>
     * Note that the underlying implementation is based on either invoking <code>ResultSet.absolute()</code>,
     * or if the ResultSet type is <code>ResultSet.TYPE_FORWARD_ONLY</code>, the <code>ResultSet.next()</code> method
     * is invoked equivalently.  The first row of a ResultSet is 1, so passing in an offset of 1 or less has no effect
     * on the initial positioning within the result set.
     * <p>
     * Note that different database and JDBC driver implementations may work differently with respect to this method.
     * Specifically, one should expect that <code>ResultSet.TYPE_FORWARD_ONLY</code> may be less efficient than a
     * "scrollable" type.
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param sql     the SQL statement
     * @param offset  the 1-based offset for the first row to be processed
     * @param maxRows the maximum number of rows to be processed
     * @return a list of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     */
    public List<GroovyRowResult> rows(String sql, int offset, int maxRows) throws SQLException {
        return rows(sql, offset, maxRows, null);
    }


    /**
     * Performs the given SQL query and return the rows of the result set.
     * In addition, the <code>metaClosure</code> will be called once passing in the
     * <code>ResultSetMetaData</code> as argument.
     * <p>
     * Example usage:
     * <pre>
     * def printNumCols = { meta -> println "Found $meta.columnCount columns" }
     * def ans = sql.rows("select * from PERSON", printNumCols)
     * println "Found ${ans.size()} rows"
     * </pre>
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param sql         the SQL statement
     * @param metaClosure called with meta data of the ResultSet
     * @return a list of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     */
    public List<GroovyRowResult> rows(String sql, @ClosureParams(value=SimpleType.class, options="java.sql.ResultSetMetaData") Closure metaClosure)
            throws SQLException {
        return rows(sql, 0, 0, metaClosure);
    }

    /**
     * Performs the given SQL query and return a "page" of rows from the result set.  A page is defined as starting at
     * a 1-based offset, and containing a maximum number of rows.
     * In addition, the <code>metaClosure</code> will be called once passing in the
     * <code>ResultSetMetaData</code> as argument.
     * <p>
     * Note that the underlying implementation is based on either invoking <code>ResultSet.absolute()</code>,
     * or if the ResultSet type is <code>ResultSet.TYPE_FORWARD_ONLY</code>, the <code>ResultSet.next()</code> method
     * is invoked equivalently.  The first row of a ResultSet is 1, so passing in an offset of 1 or less has no effect
     * on the initial positioning within the result set.
     * <p>
     * Note that different database and JDBC driver implementations may work differently with respect to this method.
     * Specifically, one should expect that <code>ResultSet.TYPE_FORWARD_ONLY</code> may be less efficient than a
     * "scrollable" type.
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param sql         the SQL statement
     * @param offset      the 1-based offset for the first row to be processed
     * @param maxRows     the maximum number of rows to be processed
     * @param metaClosure called for meta data (only once after sql execution)
     * @return a list of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     */
    public List<GroovyRowResult> rows(String sql, int offset, int maxRows,
                                      @ClosureParams(value=SimpleType.class, options="java.sql.ResultSetMetaData") Closure metaClosure) throws SQLException {
        AbstractQueryCommand command = createQueryCommand(sql);
        // for efficiency set maxRows (adjusted for the first offset rows we are going to skip the cursor over)
        command.setMaxRows(offset + maxRows);
        ResultSet rs = null;
        try {
            rs = command.execute();
            List<GroovyRowResult> result = asList(sql, rs, offset, maxRows, metaClosure);
            rs = null;
            return result;
        } finally {
            command.closeResources(rs);
        }
    }

    /**
     * Performs the given SQL query and return the rows of the result set.
     * The query may contain placeholder question marks which match the given list of parameters.
     * <p>
     * Example usage:
     * <pre>
     * def ans = sql.rows("select * from PERSON where lastname like ?", ['%a%'])
     * println "Found ${ans.size()} rows"
     * </pre>
     * <p>
     * This method supports named and named ordinal parameters by supplying such
     * parameters in the <code>params</code> list. See the class Javadoc for more details.
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param sql    the SQL statement
     * @param params a list of parameters
     * @return a list of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     */
    public List<GroovyRowResult> rows(String sql, List<Object> params) throws SQLException {
        return rows(sql, params, null);
    }

    /**
     * A variant of {@link #rows(String, java.util.List)}
     * useful when providing the named parameters as named arguments.
     *
     * @param params a map containing the named parameters
     * @param sql    the SQL statement
     * @return a list of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     * @since 1.8.7
     */
    public List<GroovyRowResult> rows(Map params, String sql) throws SQLException {
        return rows(sql, singletonList(params));
    }

    /**
     * Performs the given SQL query and return a "page" of rows from the result set.  A page is defined as starting at
     * a 1-based offset, and containing a maximum number of rows.
     * The query may contain placeholder question marks which match the given list of parameters.
     * <p>
     * Note that the underlying implementation is based on either invoking <code>ResultSet.absolute()</code>,
     * or if the ResultSet type is <code>ResultSet.TYPE_FORWARD_ONLY</code>, the <code>ResultSet.next()</code> method
     * is invoked equivalently.  The first row of a ResultSet is 1, so passing in an offset of 1 or less has no effect
     * on the initial positioning within the result set.
     * <p>
     * Note that different database and JDBC driver implementations may work differently with respect to this method.
     * Specifically, one should expect that <code>ResultSet.TYPE_FORWARD_ONLY</code> may be less efficient than a
     * "scrollable" type.
     * <p>
     * This method supports named and named ordinal parameters by supplying such
     * parameters in the <code>params</code> list. See the class Javadoc for more details.
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param sql     the SQL statement
     * @param params  a list of parameters
     * @param offset  the 1-based offset for the first row to be processed
     * @param maxRows the maximum number of rows to be processed
     * @return a list of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     */
    public List<GroovyRowResult> rows(String sql, List<Object> params, int offset, int maxRows) throws SQLException {
        return rows(sql, params, offset, maxRows, null);
    }

    /**
     * A variant of {@link #rows(String, java.util.List, int, int)}
     * useful when providing the named parameters as a map.
     *
     * @param sql     the SQL statement
     * @param params  a map of named parameters
     * @param offset  the 1-based offset for the first row to be processed
     * @param maxRows the maximum number of rows to be processed
     * @return a list of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     * @since 1.8.7
     */
    public List<GroovyRowResult> rows(String sql, Map params, int offset, int maxRows) throws SQLException {
        return rows(sql, singletonList(params), offset, maxRows);
    }

    /**
     * A variant of {@link #rows(String, java.util.List, int, int)}
     * useful when providing the named parameters as named arguments.
     *
     * @param params  a map of named parameters
     * @param sql     the SQL statement
     * @param offset  the 1-based offset for the first row to be processed
     * @param maxRows the maximum number of rows to be processed
     * @return a list of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     * @since 1.8.7
     */
    public List<GroovyRowResult> rows(Map params, String sql, int offset, int maxRows) throws SQLException {
        return rows(sql, singletonList(params), offset, maxRows);
    }

    /**
     * Performs the given SQL query and return the rows of the result set.
     * <p>
     * This method supports named and named ordinal parameters by supplying such
     * parameters in the <code>params</code> array. See the class Javadoc for more details.
     * <p>
     * An Object array variant of {@link #rows(String, List)}.
     *
     * @param sql    the SQL statement
     * @param params an array of parameters
     * @return a list of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     */
    public List<GroovyRowResult> rows(String sql, Object[] params)
            throws SQLException {
        return rows(sql, params, 0, 0);
    }

    /**
     * Performs the given SQL query and return the rows of the result set.
     * <p>
     * This method supports named and named ordinal parameters by supplying such
     * parameters in the <code>params</code> array. See the class Javadoc for more details.
     * <p>
     * An Object array variant of {@link #rows(String, List, int, int)}.
     *
     * @param sql     the SQL statement
     * @param params  an array of parameters
     * @param offset  the 1-based offset for the first row to be processed
     * @param maxRows the maximum number of rows to be processed
     * @return a list of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     */
    public List<GroovyRowResult> rows(String sql, Object[] params, int offset, int maxRows) throws SQLException {
        return rows(sql, Arrays.asList(params), offset, maxRows, null);
    }

    /**
     * Performs the given SQL query and return the rows of the result set.
     * In addition, the <code>metaClosure</code> will be called once passing in the
     * <code>ResultSetMetaData</code> as argument.
     * The query may contain placeholder question marks which match the given list of parameters.
     * <p>
     * Example usage:
     * <pre>
     * def printNumCols = { meta -> println "Found $meta.columnCount columns" }
     * def ans = sql.rows("select * from PERSON where lastname like ?", ['%a%'], printNumCols)
     * println "Found ${ans.size()} rows"
     * </pre>
     * <p>
     * This method supports named and named ordinal parameters by supplying such
     * parameters in the <code>params</code> list. Here is an example:
     * <pre>
     * def printNumCols = { meta -> println "Found $meta.columnCount columns" }
     *
     * def mapParam = [foo: 'Smith']
     * def domainParam = new MyDomainClass(bar: 'John')
     * def qry = 'select * from PERSON where lastname=?1.foo and firstname=?2.bar'
     * def ans = sql.rows(qry, [mapParam, domainParam], printNumCols)
     * println "Found ${ans.size()} rows"
     *
     * def qry2 = 'select * from PERSON where firstname=:first and lastname=:last'
     * def ans2 = sql.rows(qry2, [[last:'Smith', first:'John']], printNumCols)
     * println "Found ${ans2.size()} rows"
     * </pre>
     * See the class Javadoc for more details.
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param sql         the SQL statement
     * @param params      a list of parameters
     * @param metaClosure called for meta data (only once after sql execution)
     * @return a list of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     */
    public List<GroovyRowResult> rows(String sql, List<Object> params,
                                      @ClosureParams(value=SimpleType.class, options="java.sql.ResultSetMetaData") Closure metaClosure) throws SQLException {
        return rows(sql, params, 0, 0, metaClosure);
    }

    /**
     * A variant of {@link #rows(String, java.util.List, groovy.lang.Closure)}
     * useful when providing the named parameters as a map.
     *
     * @param sql         the SQL statement
     * @param params      a map of named parameters
     * @param metaClosure called for meta data (only once after sql execution)
     * @return a list of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     * @since 1.8.7
     */
    public List<GroovyRowResult> rows(String sql, Map params,
                                      @ClosureParams(value=SimpleType.class, options="java.sql.ResultSetMetaData") Closure metaClosure) throws SQLException {
        return rows(sql, singletonList(params), metaClosure);
    }

    /**
     * A variant of {@link #rows(String, java.util.List, groovy.lang.Closure)}
     * useful when providing the named parameters as named arguments.
     *
     * @param params      a map of named parameters
     * @param sql         the SQL statement
     * @param metaClosure called for meta data (only once after sql execution)
     * @return a list of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     * @since 1.8.7
     */
    public List<GroovyRowResult> rows(Map params, String sql,
                                      @ClosureParams(value=SimpleType.class, options="java.sql.ResultSetMetaData") Closure metaClosure) throws SQLException {
        return rows(sql, singletonList(params), metaClosure);
    }

    /**
     * Performs the given SQL query and return a "page" of rows from the result set.  A page is defined as starting at
     * a 1-based offset, and containing a maximum number of rows.
     * In addition, the <code>metaClosure</code> will be called once passing in the
     * <code>ResultSetMetaData</code> as argument.
     * The query may contain placeholder question marks which match the given list of parameters.
     * <p>
     * Note that the underlying implementation is based on either invoking <code>ResultSet.absolute()</code>,
     * or if the ResultSet type is <code>ResultSet.TYPE_FORWARD_ONLY</code>, the <code>ResultSet.next()</code> method
     * is invoked equivalently.  The first row of a ResultSet is 1, so passing in an offset of 1 or less has no effect
     * on the initial positioning within the result set.
     * <p>
     * Note that different database and JDBC driver implementations may work differently with respect to this method.
     * Specifically, one should expect that <code>ResultSet.TYPE_FORWARD_ONLY</code> may be less efficient than a
     * "scrollable" type.
     * <p>
     * This method supports named and named ordinal parameters by supplying such
     * parameters in the <code>params</code> list. See the class Javadoc for more details.
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param sql         the SQL statement
     * @param params      a list of parameters
     * @param offset      the 1-based offset for the first row to be processed
     * @param maxRows     the maximum number of rows to be processed
     * @param metaClosure called for meta data (only once after sql execution)
     * @return a list of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     */
    public List<GroovyRowResult> rows(String sql, List<Object> params, int offset, int maxRows,
                                      @ClosureParams(value=SimpleType.class, options="java.sql.ResultSetMetaData") Closure metaClosure) throws SQLException {
        AbstractQueryCommand command = createPreparedQueryCommand(sql, params);
        // for efficiency set maxRows (adjusted for the first offset rows we are going to skip the cursor over)
        command.setMaxRows(offset + maxRows);
        try {
            return asList(sql, command.execute(), offset, maxRows, metaClosure);
        } finally {
            command.closeResources();
        }
    }

    /**
     * A variant of {@link #rows(String, java.util.List, int, int, groovy.lang.Closure)}
     * useful when providing the named parameters as a map.
     *
     * @param sql         the SQL statement
     * @param params      a map of named parameters
     * @param offset      the 1-based offset for the first row to be processed
     * @param maxRows     the maximum number of rows to be processed
     * @param metaClosure called for meta data (only once after sql execution)
     * @return a list of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     * @since 1.8.7
     */
    public List<GroovyRowResult> rows(String sql, Map params, int offset, int maxRows,
                                      @ClosureParams(value=SimpleType.class, options="java.sql.ResultSetMetaData") Closure metaClosure) throws SQLException {
        return rows(sql, singletonList(params), offset, maxRows, metaClosure);
    }

    /**
     * A variant of {@link #rows(String, java.util.List, int, int, groovy.lang.Closure)}
     * useful when providing the named parameters as named arguments.
     *
     * @param params      a map of named parameters
     * @param sql         the SQL statement
     * @param offset      the 1-based offset for the first row to be processed
     * @param maxRows     the maximum number of rows to be processed
     * @param metaClosure called for meta data (only once after sql execution)
     * @return a list of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     * @since 1.8.7
     */
    public List<GroovyRowResult> rows(Map params, String sql, int offset, int maxRows,
                                      @ClosureParams(value=SimpleType.class, options="java.sql.ResultSetMetaData") Closure metaClosure) throws SQLException {
        return rows(sql, singletonList(params), offset, maxRows, metaClosure);
    }

    /**
     * Performs the given SQL query and return a "page" of rows from the result set.  A page is defined as starting at
     * a 1-based offset, and containing a maximum number of rows.
     * The query may contain GString expressions.
     * <p>
     * Note that the underlying implementation is based on either invoking <code>ResultSet.absolute()</code>,
     * or if the ResultSet type is <code>ResultSet.TYPE_FORWARD_ONLY</code>, the <code>ResultSet.next()</code> method
     * is invoked equivalently.  The first row of a ResultSet is 1, so passing in an offset of 1 or less has no effect
     * on the initial positioning within the result set.
     * <p>
     * Note that different database and JDBC driver implementations may work differently with respect to this method.
     * Specifically, one should expect that <code>ResultSet.TYPE_FORWARD_ONLY</code> may be less efficient than a
     * "scrollable" type.
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param sql     the SQL statement
     * @param offset  the 1-based offset for the first row to be processed
     * @param maxRows the maximum number of rows to be processed
     * @return a list of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     */
    public List<GroovyRowResult> rows(GString sql, int offset, int maxRows) throws SQLException {
        return rows(sql, offset, maxRows, null);
    }

    /**
     * Performs the given SQL query and return the rows of the result set.
     * The query may contain GString expressions.
     * <p>
     * Example usage:
     * <pre>
     * def location = 25
     * def ans = sql.rows("select * from PERSON where location_id < $location")
     * println "Found ${ans.size()} rows"
     * </pre>
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param gstring a GString containing the SQL query with embedded params
     * @return a list of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     * @see #expand(Object)
     */
    public List<GroovyRowResult> rows(GString gstring) throws SQLException {
        return rows(gstring, null);
    }

    /**
     * Performs the given SQL query and return the rows of the result set.
     * In addition, the <code>metaClosure</code> will be called once passing in the
     * <code>ResultSetMetaData</code> as argument.
     * The query may contain GString expressions.
     * <p>
     * Example usage:
     * <pre>
     * def location = 25
     * def printNumCols = { meta -> println "Found $meta.columnCount columns" }
     * def ans = sql.rows("select * from PERSON where location_id < $location", printNumCols)
     * println "Found ${ans.size()} rows"
     * </pre>
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param gstring     a GString containing the SQL query with embedded params
     * @param metaClosure called with meta data of the ResultSet
     * @return a list of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     * @see #expand(Object)
     */
    public List<GroovyRowResult> rows(GString gstring, @ClosureParams(value=SimpleType.class, options="java.sql.ResultSetMetaData") Closure metaClosure)
            throws SQLException {
        List<Object> params = getParameters(gstring);
        String sql = asSql(gstring, params);
        return rows(sql, params, metaClosure);
    }

    /**
     * Performs the given SQL query and return a "page" of rows from the result set.  A page is defined as starting at
     * a 1-based offset, and containing a maximum number of rows.
     * In addition, the <code>metaClosure</code> will be called once passing in the
     * <code>ResultSetMetaData</code> as argument.
     * The query may contain GString expressions.
     * <p>
     * Note that the underlying implementation is based on either invoking <code>ResultSet.absolute()</code>,
     * or if the ResultSet type is <code>ResultSet.TYPE_FORWARD_ONLY</code>, the <code>ResultSet.next()</code> method
     * is invoked equivalently.  The first row of a ResultSet is 1, so passing in an offset of 1 or less has no effect
     * on the initial positioning within the result set.
     * <p>
     * Note that different database and JDBC driver implementations may work differently with respect to this method.
     * Specifically, one should expect that <code>ResultSet.TYPE_FORWARD_ONLY</code> may be less efficient than a
     * "scrollable" type.
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param gstring     the SQL statement
     * @param offset      the 1-based offset for the first row to be processed
     * @param maxRows     the maximum number of rows to be processed
     * @param metaClosure called for meta data (only once after sql execution)
     * @return a list of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     */
    public List<GroovyRowResult> rows(GString gstring, int offset, int maxRows,
                                      @ClosureParams(value=SimpleType.class, options="java.sql.ResultSetMetaData") Closure metaClosure) throws SQLException {
        List<Object> params = getParameters(gstring);
        String sql = asSql(gstring, params);
        return rows(sql, params, offset, maxRows, metaClosure);
    }

    /**
     * Performs the given SQL query and return the first row of the result set.
     * <p>
     * Example usage:
     * <pre>
     * def ans = sql.firstRow("select * from PERSON where firstname like 'S%'")
     * println ans.firstname
     * </pre>
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param sql the SQL statement
     * @return a GroovyRowResult object or <code>null</code> if no row is found
     * @throws SQLException if a database access error occurs
     */
    public GroovyRowResult firstRow(String sql) throws SQLException {
        List<GroovyRowResult> rows = null;
        try {
            rows = rows(sql, 1, 1, null);
        }
        //should be SQLFeatureNotSupportedException instead once we move to Java 1.6
        catch (SQLException featureNotSupportedException) {
            rows = rows(sql);
        }
        if (rows.isEmpty()) return null;
        return rows.get(0);
    }

    /**
     * Performs the given SQL query and return
     * the first row of the result set.
     * The query may contain GString expressions.
     * <p>
     * Example usage:
     * <pre>
     * def location = 25
     * def ans = sql.firstRow("select * from PERSON where location_id < $location")
     * println ans.firstname
     * </pre>
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param gstring a GString containing the SQL query with embedded params
     * @return a GroovyRowResult object or <code>null</code> if no row is found
     * @throws SQLException if a database access error occurs
     * @see #expand(Object)
     */
    public GroovyRowResult firstRow(GString gstring) throws SQLException {
        List<Object> params = getParameters(gstring);
        String sql = asSql(gstring, params);
        return firstRow(sql, params);
    }

    /**
     * Performs the given SQL query and return the first row of the result set.
     * The query may contain placeholder question marks which match the given list of parameters.
     * <p>
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
     * <p>
     * This method supports named and named ordinal parameters by supplying such
     * parameters in the <code>params</code> list. See the class Javadoc for more details.
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param sql    the SQL statement
     * @param params a list of parameters
     * @return a GroovyRowResult object or <code>null</code> if no row is found
     * @throws SQLException if a database access error occurs
     */
    public GroovyRowResult firstRow(String sql, List<Object> params) throws SQLException {
        List<GroovyRowResult> rows = null;
        try {
            rows = rows(sql, params, 1, 1, null);
        }
        //should be SQLFeatureNotSupportedException instead once we move to Java 1.6
        catch (SQLException featureNotSupportedException) {
            rows = rows(sql, params);
        }
        if (rows.isEmpty()) return null;
        return rows.get(0);
    }

    /**
     * A variant of {@link #firstRow(String, java.util.List)}
     * useful when providing the named parameters as named arguments.
     *
     * @param params a map containing the named parameters
     * @param sql    the SQL statement
     * @return a GroovyRowResult object or <code>null</code> if no row is found
     * @throws SQLException if a database access error occurs
     * @since 1.8.7
     */
    public GroovyRowResult firstRow(Map params, String sql) throws SQLException {
        return firstRow(sql, singletonList(params));
    }

    /**
     * Performs the given SQL query and return the first row of the result set.
     * <p>
     * An Object array variant of {@link #firstRow(String, List)}.
     * <p>
     * This method supports named and named ordinal parameters by supplying such
     * parameters in the <code>params</code> array. See the class Javadoc for more details.
     *
     * @param sql    the SQL statement
     * @param params an array of parameters
     * @return a GroovyRowResult object or <code>null</code> if no row is found
     * @throws SQLException if a database access error occurs
     */
    public GroovyRowResult firstRow(String sql, Object[] params) throws SQLException {
        return firstRow(sql, Arrays.asList(params));
    }

    /**
     * Executes the given piece of SQL.
     * Also saves the updateCount, if any, for subsequent examination.
     * <p>
     * Example usages:
     * <pre>
     * sql.execute "DROP TABLE IF EXISTS person"
     *
     * sql.execute """
     *     CREATE TABLE person (
     *         id INTEGER NOT NULL,
     *         firstname VARCHAR(100),
     *         lastname VARCHAR(100),
     *         location_id INTEGER
     *     )
     * """
     *
     * sql.execute """
     *     INSERT INTO person (id, firstname, lastname, location_id) VALUES (4, 'Paul', 'King', 40)
     * """
     * assert sql.updateCount == 1
     * </pre>
     * <p>
     * Resource handling is performed automatically where appropriate.
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
            statement = getStatement(connection, sql);
            boolean isResultSet = statement.execute(sql);
            this.updateCount = statement.getUpdateCount();
            return isResultSet;
        } catch (SQLException e) {
            LOG.warning("Failed to execute: " + sql + " because: " + e.getMessage());
            throw e;
        } finally {
            closeResources(connection, statement);
        }
    }

    /**
     * Executes the given piece of SQL.
     * Also calls the provided processResults Closure to process any ResultSet or UpdateCount results that executing the SQL might produce.
     * <p>
     * Example usages:
     * <pre>
     * boolean first = true
     * sql.execute "{call FindAllByFirst('J')}", { isResultSet, result ->
     *   if (first) {
     *     first = false
     *     assert !isResultSet && result == 0
     *   } else {
     *     assert isResultSet && result == [[ID:1, FIRSTNAME:'James', LASTNAME:'Strachan'], [ID:4, FIRSTNAME:'Jean', LASTNAME:'Gabin']]
     *   }
     * }
     * </pre>
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param sql the SQL to execute
     * @param processResults a Closure which will be passed two parameters: either {@code true} plus a list of GroovyRowResult values
     *                       derived from {@code statement.getResultSet()} or {@code false} plus the update count from {@code statement.getUpdateCount()}.
     *                       The closure will be called for each result produced from executing the SQL.
     * @throws SQLException if a database access error occurs
     * @since 2.3.2
     */
    public void execute(String sql, Closure processResults) throws SQLException {
        Connection connection = createConnection();
        Statement statement = null;
        try {
            statement = getStatement(connection, sql);
            boolean isResultSet = statement.execute(sql);
            int updateCount = statement.getUpdateCount();
            while(isResultSet || updateCount != -1) {
                if (processResults.getMaximumNumberOfParameters() != 2) {
                    throw new SQLException("Incorrect number of parameters for processResults Closure");
                }
                if (isResultSet) {
                    ResultSet resultSet = statement.getResultSet();
                    List<GroovyRowResult> rowResult = resultSet == null ? null : asList(sql, resultSet);
                    processResults.call(isResultSet, rowResult);
                } else {
                    processResults.call(isResultSet, updateCount);
                }
                isResultSet = statement.getMoreResults();
                updateCount = statement.getUpdateCount();
            }
        } catch (SQLException e) {
            LOG.warning("Failed to execute: " + sql + " because: " + e.getMessage());
            throw e;
        } finally {
            closeResources(connection, statement);
        }
    }

    /**
     * Executes the given piece of SQL with parameters.
     * Also saves the updateCount, if any, for subsequent examination.
     * <p>
     * Example usage:
     * <pre>
     * sql.execute """
     *     insert into PERSON (id, firstname, lastname, location_id) values (?, ?, ?, ?)
     * """, [1, "Guillaume", "Laforge", 10]
     * assert sql.updateCount == 1
     * </pre>
     * <p>
     * This method supports named and named ordinal parameters.
     * See the class Javadoc for more details.
     * <p>
     * Resource handling is performed automatically where appropriate.
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
            statement = getPreparedStatement(connection, sql, params);
            boolean isResultSet = statement.execute();
            this.updateCount = statement.getUpdateCount();
            return isResultSet;
        } catch (SQLException e) {
            LOG.warning("Failed to execute: " + sql + " because: " + e.getMessage());
            throw e;
        } finally {
            closeResources(connection, statement);
        }
    }

    /**
     * Executes the given piece of SQL with parameters.
     * Also calls the provided processResults Closure to process any ResultSet or UpdateCount results that executing the SQL might produce.
     * <p>
     * This method supports named and named ordinal parameters.
     * See the class Javadoc for more details.
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param sql    the SQL statement
     * @param params a list of parameters
     * @param processResults a Closure which will be passed two parameters: either {@code true} plus a list of GroovyRowResult values
     *                       derived from {@code statement.getResultSet()} or {@code false} plus the update count from {@code statement.getUpdateCount()}.
     *                       The closure will be called for each result produced from executing the SQL.
     * @throws SQLException if a database access error occurs
     * @see #execute(String, Closure)
     * @since 2.3.2
     */
    public void execute(String sql, List<Object> params, Closure processResults) throws SQLException {
        Connection connection = createConnection();
        PreparedStatement statement = null;
        try {
            statement = getPreparedStatement(connection, sql, params);
            boolean isResultSet = statement.execute();
            int updateCount = statement.getUpdateCount();
            while(isResultSet || updateCount != -1) {
                if (processResults.getMaximumNumberOfParameters() != 2) {
                    throw new SQLException("Incorrect number of parameters for processResults Closure");
                }
                if (isResultSet) {
                    ResultSet resultSet = statement.getResultSet();
                    List<GroovyRowResult> rowResult = resultSet == null ? null : asList(sql, resultSet);
                    processResults.call(isResultSet, rowResult);
                } else {
                    processResults.call(isResultSet, updateCount);
                }
                isResultSet = statement.getMoreResults();
                updateCount = statement.getUpdateCount();
            }
        } catch (SQLException e) {
            LOG.warning("Failed to execute: " + sql + " because: " + e.getMessage());
            throw e;
        } finally {
            closeResources(connection, statement);
        }
    }

    /**
     * A variant of {@link #execute(String, java.util.List)}
     * useful when providing the named parameters as named arguments.
     *
     * @param params a map containing the named parameters
     * @param sql    the SQL statement
     * @return <code>true</code> if the first result is a <code>ResultSet</code>
     *         object; <code>false</code> if it is an update count or there are
     *         no results
     * @throws SQLException if a database access error occurs
     * @since 1.8.7
     */
    public boolean execute(Map params, String sql) throws SQLException {
        return execute(sql, singletonList(params));
    }

    /**
     * A variant of {@link #execute(String, java.util.List, Closure)}
     * useful when providing the named parameters as named arguments.
     *
     * @param params a map containing the named parameters
     * @param sql    the SQL statement
     * @param processResults a Closure which will be passed two parameters: either {@code true} plus a list of GroovyRowResult values
     *                       derived from {@code statement.getResultSet()} or {@code false} plus the update count from {@code statement.getUpdateCount()}.
     *                       The closure will be called for each result produced from executing the SQL.
     * @throws SQLException if a database access error occurs
     * @since 2.3.2
     */
    public void execute(Map params, String sql, Closure processResults) throws SQLException {
        execute(sql, singletonList(params), processResults);
    }

    /**
     * Executes the given piece of SQL with parameters.
     * <p>
     * An Object array variant of {@link #execute(String, List)}.
     * <p>
     * This method supports named and named ordinal parameters by supplying such
     * parameters in the <code>params</code> array. See the class Javadoc for more details.
     *
     * @param sql    the SQL statement
     * @param params an array of parameters
     * @return <code>true</code> if the first result is a <code>ResultSet</code>
     *         object; <code>false</code> if it is an update count or there are
     *         no results
     * @throws SQLException if a database access error occurs
     */
    public boolean execute(String sql, Object[] params) throws SQLException {
        return execute(sql, Arrays.asList(params));
    }

    /**
     * Executes the given piece of SQL with parameters.
     * <p>
     * An Object array variant of {@link #execute(String, List, Closure)}.
     * <p>
     * This method supports named and named ordinal parameters by supplying such
     * parameters in the <code>params</code> array. See the class Javadoc for more details.
     *
     * @param sql    the SQL statement
     * @param params an array of parameters
     * @param processResults a Closure which will be passed two parameters: either {@code true} plus a list of GroovyRowResult values
     *                       derived from {@code statement.getResultSet()} or {@code false} plus the update count from {@code statement.getUpdateCount()}.
     *                       The closure will be called for each result produced from executing the SQL.
     * @throws SQLException if a database access error occurs
     * @see #execute(String, List, Closure)
     * @since 2.3.2
     */
    public void execute(String sql, Object[] params, Closure processResults) throws SQLException {
        execute(sql, Arrays.asList(params), processResults);
    }

    /**
     * Executes the given SQL with embedded expressions inside.
     * Also saves the updateCount, if any, for subsequent examination.
     * <p>
     * Example usage:
     * <pre>
     * def scott = [firstname: "Scott", lastname: "Davis", id: 5, location_id: 50]
     * sql.execute """
     *     insert into PERSON (id, firstname, lastname, location_id) values ($scott.id, $scott.firstname, $scott.lastname, $scott.location_id)
     * """
     * assert sql.updateCount == 1
     * </pre>
     * <p>
     * Resource handling is performed automatically where appropriate.
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
     * Executes the given SQL with embedded expressions inside.
     * Also calls the provided processResults Closure to process any ResultSet or UpdateCount results that executing the SQL might produce.
     * Resource handling is performed automatically where appropriate.
     *
     * @param gstring a GString containing the SQL query with embedded params
     * @param processResults a Closure which will be passed two parameters: either {@code true} plus a list of GroovyRowResult values
     *                       derived from {@code statement.getResultSet()} or {@code false} plus the update count from {@code statement.getUpdateCount()}.
     *                       The closure will be called for each result produced from executing the SQL.
     * @throws SQLException if a database access error occurs
     * @see #expand(Object)
     * @see #execute(String, List, Closure)
     * @since 2.3.2
     */
    public void execute(GString gstring, Closure processResults) throws SQLException {
        List<Object> params = getParameters(gstring);
        String sql = asSql(gstring, params);
        execute(sql, params, processResults);
    }

    /**
     * Executes the given SQL statement (typically an INSERT statement).
     * Use this variant when you want to receive the values of any
     * auto-generated columns, such as an autoincrement ID field.
     * See {@link #executeInsert(GString)} for more details.
     * <p>
     * Resource handling is performed automatically where appropriate.
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
            statement = getStatement(connection, sql);
            this.updateCount = statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet keys = statement.getGeneratedKeys();
            return calculateKeys(keys);
        } catch (SQLException e) {
            LOG.warning("Failed to execute: " + sql + " because: " + e.getMessage());
            throw e;
        } finally {
            closeResources(connection, statement);
        }
    }

    /**
     * Executes the given SQL statement (typically an INSERT statement).
     * Use this variant when you want to receive the values of any
     * auto-generated columns, such as an autoincrement ID field.
     * The query may contain placeholder question marks which match the given list of parameters.
     * See {@link #executeInsert(GString)} for more details.
     * <p>
     * This method supports named and named ordinal parameters.
     * See the class Javadoc for more details.
     * <p>
     * Resource handling is performed automatically where appropriate.
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
            statement = getPreparedStatement(connection, sql, params, Statement.RETURN_GENERATED_KEYS);
            this.updateCount = statement.executeUpdate();
            ResultSet keys = statement.getGeneratedKeys();
            return calculateKeys(keys);
        } catch (SQLException e) {
            LOG.warning("Failed to execute: " + sql + " because: " + e.getMessage());
            throw e;
        } finally {
            closeResources(connection, statement);
        }
    }

    /**
     * Executes the given SQL statement (typically an INSERT statement).
     * Use this variant when you want to receive the values of any auto-generated columns,
     * such as an autoincrement ID field (or fields) and you know the column name(s) of the ID field(s).
     * The query may contain placeholder question marks which match the given list of parameters.
     * See {@link #executeInsert(GString)} for more details.
     * <p>
     * This method supports named and named ordinal parameters.
     * See the class Javadoc for more details.
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param sql            The SQL statement to execute
     * @param params         The parameter values that will be substituted
     *                       into the SQL statement's parameter slots
     * @param keyColumnNames a list of column names indicating the columns that should be returned from the
     *                       inserted row or rows (some drivers may be case sensitive, e.g. may require uppercase names)
     * @return A list of the auto-generated row results for each inserted row (typically auto-generated keys)
     * @throws SQLException if a database access error occurs
     * @see Connection#prepareStatement(String, String[])
     * @since 2.3.2
     */
    public List<GroovyRowResult> executeInsert(String sql, List<Object> params, List<String> keyColumnNames) throws SQLException {
        Connection connection = createConnection();
        PreparedStatement statement = null;
        try {
            this.keyColumnNames = keyColumnNames;
            statement = getPreparedStatement(connection, sql, params, USE_COLUMN_NAMES);
            this.keyColumnNames = null;
            this.updateCount = statement.executeUpdate();
            ResultSet keys = statement.getGeneratedKeys();
            return asList(sql, keys);
        } catch (SQLException e) {
            LOG.warning("Failed to execute: " + sql + " because: " + e.getMessage());
            throw e;
        } finally {
            closeResources(connection, statement);
        }
    }

    /**
     * A variant of {@link #executeInsert(String, java.util.List)}
     * useful when providing the named parameters as named arguments.
     *
     * @param params a map containing the named parameters
     * @param sql    The SQL statement to execute
     * @return A list of the auto-generated column values for each
     *         inserted row (typically auto-generated keys)
     * @throws SQLException if a database access error occurs
     * @since 1.8.7
     */
    public List<List<Object>> executeInsert(Map params, String sql) throws SQLException {
        return executeInsert(sql, singletonList(params));
    }

    /**
     * A variant of {@link #executeInsert(String, List, List)}
     * useful when providing the named parameters as named arguments.
     * This variant allows you to receive the values of any auto-generated columns,
     * such as an autoincrement ID field (or fields) when you know the column name(s) of the ID field(s).
     *
     * @param params         a map containing the named parameters
     * @param sql            The SQL statement to execute
     * @param keyColumnNames a list of column names indicating the columns that should be returned from the
     *                       inserted row or rows (some drivers may be case sensitive, e.g. may require uppercase names)
     * @return A list of the auto-generated row results for each inserted row (typically auto-generated keys)
     * @throws SQLException if a database access error occurs
     * @see Connection#prepareStatement(String, String[])
     * @since 2.3.2
     */
    public List<GroovyRowResult> executeInsert(Map params, String sql, List<String> keyColumnNames) throws SQLException {
        return executeInsert(sql, singletonList(params), keyColumnNames);
    }

    /**
     * Executes the given SQL statement (typically an INSERT statement).
     * <p>
     * An Object array variant of {@link #executeInsert(String, List)}.
     * <p>
     * This method supports named and named ordinal parameters by supplying such
     * parameters in the <code>params</code> array. See the class Javadoc for more details.
     *
     * @param sql    The SQL statement to execute
     * @param params The parameter values that will be substituted
     *               into the SQL statement's parameter slots
     * @return A list of the auto-generated column values for each
     *         inserted row (typically auto-generated keys)
     * @throws SQLException if a database access error occurs
     */
    public List<List<Object>> executeInsert(String sql, Object[] params) throws SQLException {
        return executeInsert(sql, Arrays.asList(params));
    }

    /**
     * Executes the given SQL statement (typically an INSERT statement).
     * This variant allows you to receive the values of any auto-generated columns,
     * such as an autoincrement ID field (or fields) when you know the column name(s) of the ID field(s).
     * <p>
     * This method supports named and named ordinal parameters by supplying such
     * parameters in the <code>params</code> array. See the class Javadoc for more details.
     *
     * @param sql            The SQL statement to execute
     * @param keyColumnNames an array of column names indicating the columns that should be returned from the
     *                       inserted row or rows (some drivers may be case sensitive, e.g. may require uppercase names)
     * @return A list of the auto-generated row results for each inserted row (typically auto-generated keys)
     * @throws SQLException if a database access error occurs
     * @since 2.3.2
     */
    public List<GroovyRowResult> executeInsert(String sql, String[] keyColumnNames) throws SQLException {
        Connection connection = createConnection();
        Statement statement = null;
        try {
            statement = getStatement(connection, sql);
            this.updateCount = statement.executeUpdate(sql, keyColumnNames);
            ResultSet keys = statement.getGeneratedKeys();
            return asList(sql, keys);
        } catch (SQLException e) {
            LOG.warning("Failed to execute: " + sql + " because: " + e.getMessage());
            throw e;
        } finally {
            closeResources(connection, statement);
        }
    }

    /**
     * Executes the given SQL statement (typically an INSERT statement).
     * This variant allows you to receive the values of any auto-generated columns,
     * such as an autoincrement ID field (or fields) when you know the column name(s) of the ID field(s).
     * <p>
     * An array variant of {@link #executeInsert(String, List, List)}.
     * <p>
     * This method supports named and named ordinal parameters by supplying such
     * parameters in the <code>params</code> array. See the class Javadoc for more details.
     *
     * @param sql            The SQL statement to execute
     * @param keyColumnNames an array of column names indicating the columns that should be returned from the
     *                       inserted row or rows (some drivers may be case sensitive, e.g. may require uppercase names)
     * @param params         The parameter values that will be substituted
     *                       into the SQL statement's parameter slots
     * @return A list of the auto-generated row results for each inserted row (typically auto-generated keys)
     * @throws SQLException if a database access error occurs
     * @since 2.3.2
     */
    public List<GroovyRowResult> executeInsert(String sql, String[] keyColumnNames, Object[] params) throws SQLException {
        return executeInsert(sql, Arrays.asList(params), Arrays.asList(keyColumnNames));
    }

    /**
     * Executes the given SQL statement (typically an INSERT statement).
     * Use this variant when you want to receive the values of any
     * auto-generated columns, such as an autoincrement ID field.
     * The query may contain GString expressions.
     * <p>
     * Generated key values can be accessed using
     * array notation. For example, to return the second auto-generated
     * column value of the third row, use <code>keys[3][1]</code>. The
     * method is designed to be used with SQL INSERT statements, but is
     * not limited to them.
     * <p>
     * The standard use for this method is when a table has an
     * autoincrement ID column and you want to know what the ID is for
     * a newly inserted row. In this example, we insert a single row
     * into a table in which the first column contains the autoincrement ID:
     * <pre>
     * def sql = Sql.newInstance("jdbc:mysql://localhost:3306/groovy",
     *                           "user",
     *                           "password",
     *                           "com.mysql.jdbc.Driver")
     *
     * def keys = sql.executeInsert("insert into test_table (INT_DATA, STRING_DATA) "
     *                       + "VALUES (1, 'Key Largo')")
     *
     * def id = keys[0][0]
     *
     * // 'id' now contains the value of the new row's ID column.
     * // It can be used to update an object representation's
     * // id attribute for example.
     * ...
     * </pre>
     * <p>
     * Resource handling is performed automatically where appropriate.
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
     * Executes the given SQL statement (typically an INSERT statement).
     * Use this variant when you want to receive the values of any auto-generated columns,
     * such as an autoincrement ID field (or fields) and you know the column name(s) of the ID field(s).
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param gstring        a GString containing the SQL query with embedded params
     * @param keyColumnNames a list of column names indicating the columns that should be returned from the
     *                       inserted row or rows (some drivers may be case sensitive, e.g. may require uppercase names)
     * @return A list of the auto-generated row results for each inserted row (typically auto-generated keys)
     * @throws SQLException if a database access error occurs
     * @see Connection#prepareStatement(String, String[])
     * @see #expand(Object)
     * @since 2.3.2
     */
    public List<GroovyRowResult> executeInsert(GString gstring, List<String> keyColumnNames) throws SQLException {
        List<Object> params = getParameters(gstring);
        String sql = asSql(gstring, params);
        return executeInsert(sql, params, keyColumnNames);
    }

    /**
     * Executes the given SQL update.
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param sql the SQL to execute
     * @return the number of rows updated or 0 for SQL statements that return nothing
     * @throws SQLException if a database access error occurs
     */
    public int executeUpdate(String sql) throws SQLException {
        Connection connection = createConnection();
        Statement statement = null;
        try {
            statement = getStatement(connection, sql);
            this.updateCount = statement.executeUpdate(sql);
            return this.updateCount;
        } catch (SQLException e) {
            LOG.warning("Failed to execute: " + sql + " because: " + e.getMessage());
            throw e;
        } finally {
            closeResources(connection, statement);
        }
    }

    /**
     * Executes the given SQL update with parameters.
     * <p>
     * This method supports named and named ordinal parameters.
     * See the class Javadoc for more details.
     * <p>
     * Resource handling is performed automatically where appropriate.
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
            statement = getPreparedStatement(connection, sql, params);
            this.updateCount = statement.executeUpdate();
            return this.updateCount;
        } catch (SQLException e) {
            LOG.warning("Failed to execute: " + sql + " because: " + e.getMessage());
            throw e;
        } finally {
            closeResources(connection, statement);
        }
    }

    /**
     * A variant of {@link #executeUpdate(String, java.util.List)}
     * useful when providing the named parameters as named arguments.
     *
     * @param params a map containing the named parameters
     * @param sql    the SQL statement
     * @return the number of rows updated or 0 for SQL statements that return nothing
     * @throws SQLException if a database access error occurs
     * @since 1.8.7
     */
    public int executeUpdate(Map params, String sql) throws SQLException {
        return executeUpdate(sql, singletonList(params));
    }

    /**
     * Executes the given SQL update with parameters.
     * <p>
     * An Object array variant of {@link #executeUpdate(String, List)}.
     *
     * @param sql    the SQL statement
     * @param params an array of parameters
     * @return the number of rows updated or 0 for SQL statements that return nothing
     * @throws SQLException if a database access error occurs
     */
    public int executeUpdate(String sql, Object[] params) throws SQLException {
        return executeUpdate(sql, Arrays.asList(params));
    }

    /**
     * Executes the given SQL update with embedded expressions inside.
     * <p>
     * Resource handling is performed automatically where appropriate.
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
     * <p>
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
     * <p>
     * Example usage - see {@link #call(String)} for more details about
     * creating a <code>HouseSwap(IN name1, IN name2)</code> stored procedure.
     * Once created, it can be called like this:
     * <pre>
     * def p1 = 'Paul'
     * def p2 = 'Guillaume'
     * def rowsChanged = sql.call("{call HouseSwap($p1, $p2)}")
     * assert rowsChanged == 2
     * </pre>
     * <p>
     * Resource handling is performed automatically where appropriate.
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
     * <p>
     * Example usage - see {@link #call(String)} for more details about
     * creating a <code>HouseSwap(IN name1, IN name2)</code> stored procedure.
     * Once created, it can be called like this:
     * <pre>
     * def rowsChanged = sql.call("{call HouseSwap(?, ?)}", ['Guillaume', 'Paul'])
     * assert rowsChanged == 2
     * </pre>
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param sql    the SQL statement
     * @param params a list of parameters
     * @return the number of rows updated or 0 for SQL statements that return nothing
     * @throws SQLException if a database access error occurs
     * @see #call(String)
     */
    public int call(String sql, List<Object> params) throws Exception {
        Connection connection = createConnection();
        CallableStatement statement = null;
        try {
            statement = getCallableStatement(connection, sql, params);
            return statement.executeUpdate();
        } catch (SQLException e) {
            LOG.warning("Failed to execute: " + sql + " because: " + e.getMessage());
            throw e;
        } finally {
            closeResources(connection, statement);
        }
    }

    /**
     * Performs a stored procedure call with the given parameters.
     * <p>
     * An Object array variant of {@link #call(String, List)}.
     *
     * @param sql    the SQL statement
     * @param params an array of parameters
     * @return the number of rows updated or 0 for SQL statements that return nothing
     * @throws SQLException if a database access error occurs
     * @see #call(String)
     */
    public int call(String sql, Object[] params) throws Exception {
        return call(sql, Arrays.asList(params));
    }

    /**
     * Performs a stored procedure call with the given parameters.  The closure
     * is called once with all the out parameters.
     * <p>
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
     * <p>
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
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param sql     the sql statement
     * @param params  a list of parameters
     * @param closure called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     */
    public void call(String sql, List<Object> params, Closure closure) throws Exception {
        callWithRows(sql, params, NO_RESULT_SETS, closure);
    }

    /**
     * Performs a stored procedure call with the given parameters,
     * calling the closure once with all result objects.
     * <p>
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
     * <p>
     * As another example, see {@link #call(String, List, Closure)} for more details about
     * creating a <code>FullName(IN first)</code> stored function.
     * Once created, it can be called like this:
     * <pre>
     * def first = 'Sam'
     * sql.call("{$Sql.VARCHAR = call FullName($first)}") { name ->
     *     assert name == 'Sam Pullara'
     * }
     * </pre>
     * <p>
     * Resource handling is performed automatically where appropriate.
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
     * Performs a stored procedure call with the given parameters,
     * calling the closure once with all result objects,
     * and also returning the rows of the ResultSet.
     * <p>
     * Use this when calling a stored procedure that utilizes both
     * output parameters and returns a single ResultSet.
     * <p>
     * Once created, the stored procedure can be called like this:
     * <pre>
     * def first = 'Jeff'
     * def last = 'Sheets'
     * def rows = sql.callWithRows "{call Hemisphere2($first, $last, ${Sql.VARCHAR})}", { dwells ->
     *     println dwells
     * }
     * </pre>
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param gstring a GString containing the SQL query with embedded params
     * @param closure called once with all out parameter results
     * @return a list of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     * @see #callWithRows(String, List, Closure)
     */
    public List<GroovyRowResult> callWithRows(GString gstring, Closure closure) throws SQLException {
        List<Object> params = getParameters(gstring);
        String sql = asSql(gstring, params);
        return callWithRows(sql, params, closure);
    }

    /**
     * Performs a stored procedure call with the given parameters,
     * calling the closure once with all result objects,
     * and also returning the rows of the ResultSet.
     * <p>
     * Use this when calling a stored procedure that utilizes both
     * output parameters and returns a single ResultSet.
     * <p>
     * Once created, the stored procedure can be called like this:
     * <pre>
     * def rows = sql.callWithRows '{call Hemisphere2(?, ?, ?)}', ['Guillaume', 'Laforge', Sql.VARCHAR], { dwells ->
     *     println dwells
     * }
     * </pre>
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param sql     the sql statement
     * @param params  a list of parameters
     * @param closure called once with all out parameter results
     * @return a list of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     * @see #callWithRows(GString, Closure)
     */
    public List<GroovyRowResult> callWithRows(String sql, List<Object> params, Closure closure) throws SQLException {
        return callWithRows(sql, params, FIRST_RESULT_SET, closure).get(0);
    }

    /**
     * Performs a stored procedure call with the given parameters,
     * calling the closure once with all result objects,
     * and also returning a list of lists with the rows of the ResultSet(s).
     * <p>
     * Use this when calling a stored procedure that utilizes both
     * output parameters and returns multiple ResultSets.
     * <p>
     * Once created, the stored procedure can be called like this:
     * <pre>
     * def first = 'Jeff'
     * def last = 'Sheets'
     * def rowsList = sql.callWithAllRows "{call Hemisphere2($first, $last, ${Sql.VARCHAR})}", { dwells ->
     *     println dwells
     * }
     * </pre>
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param gstring a GString containing the SQL query with embedded params
     * @param closure called once with all out parameter results
     * @return a list containing lists of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     * @see #callWithAllRows(String, List, Closure)
     */
    public List<List<GroovyRowResult>> callWithAllRows(GString gstring, Closure closure) throws SQLException {
        List<Object> params = getParameters(gstring);
        String sql = asSql(gstring, params);
        return callWithAllRows(sql, params, closure);
    }

    /**
     * Performs a stored procedure call with the given parameters,
     * calling the closure once with all result objects,
     * and also returning a list of lists with the rows of the ResultSet(s).
     * <p>
     * Use this when calling a stored procedure that utilizes both
     * output parameters and returns multiple ResultSets.
     * <p>
     * Once created, the stored procedure can be called like this:
     * <pre>
     * def rowsList = sql.callWithAllRows '{call Hemisphere2(?, ?, ?)}', ['Guillaume', 'Laforge', Sql.VARCHAR], { dwells ->
     *     println dwells
     * }
     * </pre>
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param sql     the sql statement
     * @param params  a list of parameters
     * @param closure called once with all out parameter results
     * @return a list containing lists of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     * @see #callWithRows(GString, Closure)
     */
    public List<List<GroovyRowResult>> callWithAllRows(String sql, List<Object> params, Closure closure) throws SQLException {
        return callWithRows(sql, params, ALL_RESULT_SETS, closure);
    }

    /**
     * Base internal method for call(), callWithRows(), and callWithAllRows() style of methods.
     * <p>
     * Performs a stored procedure call with the given parameters,
     * calling the closure once with all result objects,
     * and also returning the rows of the ResultSet(s) (if processResultSets is set to
     * Sql.FIRST_RESULT_SET, Sql.ALL_RESULT_SETS)
     * <p>
     * Main purpose of processResultSets param is to retain original call() method
     * performance when this is set to Sql.NO_RESULT_SETS
     * <p>
     * Resource handling is performed automatically where appropriate.
     *
     * @param sql     the sql statement
     * @param params  a list of parameters
     * @param processResultsSets the result sets to process, either Sql.NO_RESULT_SETS, Sql.FIRST_RESULT_SET, or Sql.ALL_RESULT_SETS
     * @param closure called once with all out parameter results
     * @return a list of GroovyRowResult objects
     * @throws SQLException if a database access error occurs
     * @see #callWithRows(String, List, Closure)
     */
    protected List<List<GroovyRowResult>> callWithRows(String sql, List<Object> params, int processResultsSets, Closure closure) throws SQLException {
        Connection connection = createConnection();
        CallableStatement statement = null;
        List<GroovyResultSet> resultSetResources = new ArrayList<GroovyResultSet>();
        try {
            statement = getCallableStatement(connection, sql, params);
            boolean hasResultSet = statement.execute();
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
            List<List<GroovyRowResult>> resultSets = new ArrayList<List<GroovyRowResult>>();
            if (processResultsSets == NO_RESULT_SETS) {
                resultSets.add(new ArrayList<GroovyRowResult>());
                return resultSets;
            }
            //Check both hasResultSet and getMoreResults() because of differences in vendor behavior
            if (!hasResultSet) {
                hasResultSet = statement.getMoreResults();
            }
            while (hasResultSet && (processResultsSets != NO_RESULT_SETS)) {
                resultSets.add(asList(sql, statement.getResultSet()));
                if (processResultsSets == FIRST_RESULT_SET) {
                    break;
                } else {
                    hasResultSet = statement.getMoreResults();
                }
            }
            return resultSets;
        } catch (SQLException e) {
            LOG.warning("Failed to execute: " + sql + " because: " + e.getMessage());
            throw e;
        } finally {
            for (GroovyResultSet rs : resultSetResources) {
                closeResources(null, null, rs);
            }
            closeResources(connection, statement);
        }
    }

    /**
     * If this SQL object was created with a Connection then this method closes
     * the connection. If this SQL object was created from a DataSource then
     * this method only frees any cached objects (statements in particular).
     */
    @Override
    public void close() {
        namedParamSqlCache.clear();
        namedParamIndexPropCache.clear();
        clearStatementCache();
        if (useConnection != null) {
            try {
                useConnection.close();
            } catch (SQLException e) {
                LOG.finest("Caught exception closing connection: " + e.getMessage());
            }
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
            LOG.info("Commit operation not supported when using datasets unless using withTransaction or cacheConnection - attempt to commit ignored");
            return;
        }
        try {
            useConnection.commit();
        } catch (SQLException e) {
            LOG.warning("Caught exception committing connection: " + e.getMessage());
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
            LOG.info("Rollback operation not supported when using datasets unless using withTransaction or cacheConnection - attempt to rollback ignored");
            return;
        }
        try {
            useConnection.rollback();
        } catch (SQLException e) {
            LOG.warning("Caught exception rolling back connection: " + e.getMessage());
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

    private void setConnection(Connection connection) {
        useConnection = connection;
    }

    /**
     * Allows a closure to be passed in to configure the JDBC statements before they are executed.
     * It can be used to do things like set the query size etc. When this method is invoked, the supplied
     * closure is saved. Statements subsequently created from other methods will then be
     * configured using this closure. The statement being configured is passed into the closure
     * as its single argument, e.g.:
     * <pre>
     * sql.withStatement{ stmt -> stmt.maxRows = 10 }
     * def firstTenRows = sql.rows("select * from table")
     * </pre>
     *
     * @param configureStatement the closure
     */
    public void withStatement(@ClosureParams(value=SimpleType.class, options="java.sql.Statement") Closure configureStatement) {
        this.configureStatement = configureStatement;
    }

    /**
     * Enables statement caching.<br>
     * if <i>cacheStatements</i> is true, cache is created and all created prepared statements will be cached.
     * if <i>cacheStatements</i> is false, all cached statements will be properly closed.
     *
     * @param cacheStatements the new value
     */
    public void setCacheStatements(boolean cacheStatements) {
        this.cacheStatements = cacheStatements;
        if (!cacheStatements) {
            clearStatementCache();
        }
    }

    /**
     * @return boolean true if cache is enabled (default is false)
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
    public void cacheConnection(Closure closure) throws SQLException {
        boolean savedCacheConnection = cacheConnection;
        cacheConnection = true;
        Connection connection = null;
        try {
            connection = createConnection();
            callClosurePossiblyWithConnection(closure, connection);
        } finally {
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
    public void withTransaction(Closure closure) throws SQLException {
        boolean savedCacheConnection = cacheConnection;
        cacheConnection = true;
        Connection connection = null;
        boolean savedAutoCommit = true;
        try {
            connection = createConnection();
            savedAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            callClosurePossiblyWithConnection(closure, connection);
            connection.commit();
        } catch (SQLException | Error | RuntimeException e) {
            handleError(connection, e);
            throw e;
        } catch (Exception e) {
            handleError(connection, e);
            throw new SQLException("Unexpected exception during transaction", e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(savedAutoCommit);
                }
                catch (SQLException e) {
                    LOG.finest("Caught exception resetting auto commit: " + e.getMessage() + " - continuing");
                }
            }
            cacheConnection = false;
            closeResources(connection, null);
            cacheConnection = savedCacheConnection;
            if (dataSource != null && !cacheConnection) {
                useConnection = null;
            }
        }
    }

    /**
     * Returns true if the current Sql object is currently executing a withBatch
     * method call.
     *
     * @return true if a withBatch call is currently being executed.
     */
    public boolean isWithinBatch() {
        return withinBatch;
    }

    /**
     * Performs the closure (containing batch operations) within a batch.
     * Uses a batch size of zero, i.e. no automatic partitioning of batches.
     * <p>
     * This means that <code>executeBatch()</code> will be called automatically after the <code>withBatch</code>
     * closure has finished but may be called explicitly if desired as well for more fine-grained
     * partitioning of the batch.
     * <p>
     * The closure will be called with a single argument; the database
     * statement (actually a <code>BatchingStatementWrapper</code> helper object)
     * associated with this batch.
     * <p>
     * Use it like this:
     * <pre>
     * def updateCounts = sql.withBatch { stmt ->
     *     stmt.addBatch("insert into TABLENAME ...")
     *     stmt.addBatch("insert into TABLENAME ...")
     *     stmt.addBatch("insert into TABLENAME ...")
     *     ...
     * }
     * </pre>
     * For integrity and performance reasons, you may wish to consider executing your batch command(s) within a transaction:
     * <pre>
     * sql.withTransaction {
     *     def result1 = sql.withBatch { ... }
     *     ...
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
     * @see #withBatch(int, Closure)
     */
    public int[] withBatch(Closure closure) throws SQLException {
        return withBatch(0, closure);
    }

    /**
     * Performs the closure (containing batch operations) within a batch using a given batch size.
     * <p>
     * After every <code>batchSize</code> <code>addBatch(sqlBatchOperation)</code>
     * operations, automatically calls an <code>executeBatch()</code> operation to "chunk" up the database operations
     * into partitions. Though not normally needed, you can also explicitly call <code>executeBatch()</code> which
     * after executing the current batch, resets the batch count back to zero.
     * <p>
     * The closure will be called with a single argument; the database statement
     * (actually a <code>BatchingStatementWrapper</code> helper object)
     * associated with this batch.
     * <p>
     * Use it like this for batchSize of 20:
     * <pre>
     * def updateCounts = sql.withBatch(20) { stmt ->
     *     stmt.addBatch("insert into TABLENAME ...")
     *     stmt.addBatch("insert into TABLENAME ...")
     *     stmt.addBatch("insert into TABLENAME ...")
     *     ...
     * }
     * </pre>
     * For integrity and performance reasons, you may wish to consider executing your batch command(s) within a transaction:
     * <pre>
     * sql.withTransaction {
     *     def result1 = sql.withBatch { ... }
     *     ...
     * }
     * </pre>
     *
     * @param batchSize partition the batch into batchSize pieces, i.e. after batchSize
     *                  <code>addBatch()</code> invocations, call <code>executeBatch()</code> automatically;
     *                  0 means manual calls to executeBatch are required
     * @param closure   the closure containing batch and optionally other statements
     * @return an array of update counts containing one element for each
     *         command in the batch.  The elements of the array are ordered according
     *         to the order in which commands were added to the batch.
     * @throws SQLException if a database access error occurs,
     *                      or this method is called on a closed <code>Statement</code>, or the
     *                      driver does not support batch statements. Throws {@link java.sql.BatchUpdateException}
     *                      (a subclass of <code>SQLException</code>) if one of the commands sent to the
     *                      database fails to execute properly or attempts to return a result set.
     * @see #withBatch(Closure)
     * @see BatchingStatementWrapper
     * @see Statement
     */
    public int[] withBatch(int batchSize, Closure closure) throws SQLException {
        Connection connection = createConnection();
        BatchingStatementWrapper statement = null;
        boolean savedWithinBatch = withinBatch;
        try {
            withinBatch = true;
            statement = new BatchingStatementWrapper(createStatement(connection), batchSize, LOG);
            closure.call(statement);
            return statement.executeBatch();
        } catch (SQLException e) {
            LOG.warning("Error during batch execution: " + e.getMessage());
            throw e;
        } finally {
            closeResources(statement);
            closeResources(connection);
            withinBatch = savedWithinBatch;
        }
    }

    /**
     * Performs the closure (containing batch operations specific to an associated prepared statement)
     * within a batch. Uses a batch size of zero, i.e. no automatic partitioning of batches.
     * <p>
     * This means that <code>executeBatch()</code> will be called automatically after the <code>withBatch</code>
     * closure has finished but may be called explicitly if desired as well for more fine-grained
     * partitioning of the batch.
     * <p>
     * The closure will be called with a single argument; the prepared
     * statement (actually a <code>BatchingPreparedStatementWrapper</code> helper object)
     * associated with this batch.
     * <p>
     * An example:
     * <pre>
     * def updateCounts = sql.withBatch('insert into TABLENAME(a, b, c) values (?, ?, ?)') { ps ->
     *     ps.addBatch([10, 12, 5])
     *     ps.addBatch([7, 3, 98])
     *     ps.addBatch(22, 67, 11)
     *     def partialUpdateCounts = ps.executeBatch() // optional interim batching
     *     ps.addBatch(30, 40, 50)
     *     ...
     * }
     * </pre>
     * For integrity and performance reasons, you may wish to consider executing your batch command(s) within a transaction:
     * <pre>
     * sql.withTransaction {
     *     def result1 = sql.withBatch { ... }
     *     ...
     * }
     * </pre>
     *
     * @param sql     batch update statement
     * @param closure the closure containing batch statements (to bind parameters) and optionally other statements
     * @return an array of update counts containing one element for each
     *         binding in the batch.  The elements of the array are ordered according
     *         to the order in which commands were executed.
     * @throws SQLException if a database access error occurs,
     *                      or this method is called on a closed <code>Statement</code>, or the
     *                      driver does not support batch statements. Throws {@link java.sql.BatchUpdateException}
     *                      (a subclass of <code>SQLException</code>) if one of the commands sent to the
     *                      database fails to execute properly or attempts to return a result set.
     * @see #withBatch(int, String, Closure)
     * @see BatchingPreparedStatementWrapper
     * @see PreparedStatement
     */
    public int[] withBatch(String sql, Closure closure) throws SQLException {
        return withBatch(0, sql, closure);
    }

    /**
     * Performs the closure (containing batch operations specific to an associated prepared statement)
     * within a batch using a given batch size.
     * <p>
     * After every <code>batchSize</code> <code>addBatch(params)</code>
     * operations, automatically calls an <code>executeBatch()</code> operation to "chunk" up the database operations
     * into partitions. Though not normally needed, you can also explicitly call <code>executeBatch()</code> which
     * after executing the current batch, resets the batch count back to zero.
     * <p>
     * The closure will be called with a single argument; the prepared
     * statement (actually a <code>BatchingPreparedStatementWrapper</code> helper object)
     * associated with this batch.
     * <p>
     * Below is an example using a batchSize of 20:
     * <pre>
     * def updateCounts = sql.withBatch(20, 'insert into TABLENAME(a, b, c) values (?, ?, ?)') { ps ->
     *     ps.addBatch(10, 12, 5)      // varargs style
     *     ps.addBatch([7, 3, 98])     // list
     *     ps.addBatch([22, 67, 11])
     *     ...
     * }
     * </pre>
     * Named parameters (into maps or domain objects) are also supported:
     * <pre>
     * def updateCounts = sql.withBatch(20, 'insert into TABLENAME(a, b, c) values (:foo, :bar, :baz)') { ps ->
     *     ps.addBatch([foo:10, bar:12, baz:5])  // map
     *     ps.addBatch(foo:7, bar:3, baz:98)     // Groovy named args allow outer brackets to be dropped
     *     ...
     * }
     * </pre>
     * Named ordinal parameters (into maps or domain objects) are also supported:
     * <pre>
     * def updateCounts = sql.withBatch(20, 'insert into TABLENAME(a, b, c) values (?1.foo, ?2.bar, ?2.baz)') { ps ->
     *     ps.addBatch([[foo:22], [bar:67, baz:11]])  // list of maps or domain objects
     *     ps.addBatch([foo:10], [bar:12, baz:5])     // varargs allows outer brackets to be dropped
     *     ps.addBatch([foo:7], [bar:3, baz:98])
     *     ...
     * }
     * // swap to batch size of 5 and illustrate simple and domain object cases ...
     * class Person { String first, last }
     * def updateCounts2 = sql.withBatch(5, 'insert into PERSON(id, first, last) values (?1, ?2.first, ?2.last)') { ps ->
     *     ps.addBatch(1, new Person(first:'Peter', last:'Pan'))
     *     ps.addBatch(2, new Person(first:'Snow', last:'White'))
     *     ...
     * }
     * </pre>
     * For integrity and performance reasons, you may wish to consider executing your batch command(s) within a transaction:
     * <pre>
     * sql.withTransaction {
     *     def result1 = sql.withBatch { ... }
     *     ...
     * }
     * </pre>
     *
     * @param batchSize partition the batch into batchSize pieces, i.e. after batchSize
     *                  <code>addBatch()</code> invocations, call <code>executeBatch()</code> automatically;
     *                  0 means manual calls to executeBatch are required if additional partitioning of the batch is required
     * @param sql       batch update statement
     * @param closure   the closure containing batch statements (to bind parameters) and optionally other statements
     * @return an array of update counts containing one element for each
     *         binding in the batch.  The elements of the array are ordered according
     *         to the order in which commands were executed.
     * @throws SQLException if a database access error occurs,
     *                      or this method is called on a closed <code>Statement</code>, or the
     *                      driver does not support batch statements. Throws {@link java.sql.BatchUpdateException}
     *                      (a subclass of <code>SQLException</code>) if one of the commands sent to the
     *                      database fails to execute properly or attempts to return a result set.
     * @see BatchingPreparedStatementWrapper
     * @see PreparedStatement
     */
    public int[] withBatch(int batchSize, String sql, Closure closure) throws SQLException {
        Connection connection = createConnection();
        List<Tuple> indexPropList = null;
        SqlWithParams preCheck = buildSqlWithIndexedProps(sql);
        boolean savedWithinBatch = withinBatch;
        BatchingPreparedStatementWrapper psWrapper = null;
        if (preCheck != null) {
            indexPropList = new ArrayList<Tuple>();
            for (Object next : preCheck.getParams()) {
                indexPropList.add((Tuple) next);
            }
            sql = preCheck.getSql();
        }

        try {
            withinBatch = true;
            PreparedStatement statement = (PreparedStatement) getAbstractStatement(new CreatePreparedStatementCommand(0), connection, sql);
            configure(statement);
            psWrapper = new BatchingPreparedStatementWrapper(statement, indexPropList, batchSize, LOG, this);
            closure.call(psWrapper);
            return psWrapper.executeBatch();
        } catch (SQLException e) {
            LOG.warning("Error during batch execution of '" + sql + "' with message: " + e.getMessage());
            throw e;
        } finally {
            closeResources(psWrapper);
            closeResources(connection);
            withinBatch = savedWithinBatch;
        }
    }

    /**
     * Caches every created preparedStatement in Closure <i>closure</i>
     * Every cached preparedStatement is closed after closure has been called.
     * If the closure takes a single argument, it will be called
     * with the connection, otherwise it will be called with no arguments.
     *
     * @param closure the given closure
     * @throws SQLException if a database error occurs
     * @see #setCacheStatements(boolean)
     */
    public void cacheStatements(Closure closure) throws SQLException {
        boolean savedCacheStatements = cacheStatements;
        cacheStatements = true;
        Connection connection = null;
        try {
            connection = createConnection();
            callClosurePossiblyWithConnection(closure, connection);
        } finally {
            cacheStatements = false;
            closeResources(connection, null);
            cacheStatements = savedCacheStatements;
        }
    }

    // protected implementation methods - extension points for subclasses
    //-------------------------------------------------------------------------

    /**
     * Useful helper method which handles resource management when executing a
     * query which returns a result set.
     * Derived classes of Sql can override "createQueryCommand" and then call
     * this method to access the ResultSet returned from the provided query
     * or alternatively can use the higher-level method of Sql which return
     * result sets which are funnelled through this method, e.g. eachRow, query.
     *
     * @param sql query to execute
     * @return the resulting ResultSet
     * @throws SQLException if a database error occurs
     */
    protected final ResultSet executeQuery(String sql) throws SQLException {
        AbstractQueryCommand command = createQueryCommand(sql);
        ResultSet rs = null;
        try {
            rs = command.execute();
        } finally {
            command.closeResources();
        }
        return rs;
    }

    /**
     * Useful helper method which handles resource management when executing a
     * prepared query which returns a result set.
     * Derived classes of Sql can override "createPreparedQueryCommand" and then
     * call this method to access the ResultSet returned from the provided query.
     *
     * @param sql    query to execute
     * @param params parameters matching question mark placeholders in the query
     * @return the resulting ResultSet
     * @throws SQLException if a database error occurs
     */
    protected final ResultSet executePreparedQuery(String sql, List<Object> params)
            throws SQLException {
        AbstractQueryCommand command = createPreparedQueryCommand(sql, params);
        ResultSet rs = null;
        try {
            rs = command.execute();
        } finally {
            command.closeResources();
        }
        return rs;
    }

    /**
     * Hook to allow derived classes to override list of result collection behavior.
     * The default behavior is to return a list of GroovyRowResult objects corresponding
     * to each row in the ResultSet.
     *
     * @param sql query to execute
     * @param rs  the ResultSet to process
     * @return the resulting list of rows
     * @throws SQLException if a database error occurs
     */
    protected List<GroovyRowResult> asList(String sql, ResultSet rs) throws SQLException {
        return asList(sql, rs, null);
    }

    /**
     * Hook to allow derived classes to override list of result collection behavior.
     * The default behavior is to return a list of GroovyRowResult objects corresponding
     * to each row in the ResultSet.
     *
     * @param sql         query to execute
     * @param rs          the ResultSet to process
     * @param metaClosure called for meta data (only once after sql execution)
     * @return the resulting list of rows
     * @throws SQLException if a database error occurs
     */
    protected List<GroovyRowResult> asList(String sql, ResultSet rs,
                                           @ClosureParams(value=SimpleType.class, options="java.sql.ResultSetMetaData") Closure metaClosure) throws SQLException {
        return asList(sql, rs, 0, 0, metaClosure);
    }

    protected List<GroovyRowResult> asList(String sql, ResultSet rs, int offset, int maxRows,
                                           @ClosureParams(value=SimpleType.class, options="java.sql.ResultSetMetaData") Closure metaClosure) throws SQLException {
        List<GroovyRowResult> results = new ArrayList<GroovyRowResult>();

        try {
            if (metaClosure != null) {
                metaClosure.call(rs.getMetaData());
            }

            boolean cursorAtRow = moveCursor(rs, offset);
            if (!cursorAtRow) return null;

            int i = 0;
            while ((maxRows <= 0 || i++ < maxRows) && rs.next()) {
                results.add(toRowResult(rs));
            }
            return (results);
        } catch (SQLException e) {
            LOG.warning("Failed to retrieve row from ResultSet for: " + sql + " because: " + e.getMessage());
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
        StringBuilder buffer = new StringBuilder();
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
                                    LOG.warning("In Groovy SQL please do not use quotes around dynamic expressions " +
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
                    } else {
                        inWhere = 0;
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
        ParameterMetaData metaData = getParameterMetaDataSafe(statement);
        if (metaData != null) {
            if (metaData.getParameterCount() == 0 && params.size() == 1 && params.get(0) instanceof Map) {
                Map paramsMap = (Map) params.get(0);
                if (paramsMap.isEmpty()) return;
            }
            // GROOVY-8174: we'd like stricter checking here but many drivers currently in use just aren't consistent enough, so we log
            if (metaData.getParameterCount() != params.size()) {
                LOG.warning("Found " + metaData.getParameterCount() + " parameter placeholders but supplied with " + params.size() + " parameters");
            }
        }
        for (Object value : params) {
            setObject(statement, i++, value);
        }
    }

    private ParameterMetaData getParameterMetaDataSafe(PreparedStatement statement) throws SQLException {
        try {
            return statement.getParameterMetaData();
        } catch(SQLException se) {
            LOG.fine("Unable to retrieve parameter metadata - reduced checking will occur: " + se.getMessage());
            return null;
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
            try {
                statement.setObject(i, value);
            } catch (SQLException e) {
                if (value == null) {
                    SQLException se = new SQLException("Your JDBC driver may not support null arguments for setObject. Consider using Groovy's InParameter feature." +
                            (e.getMessage() == null ? "" : " (CAUSE: " + e.getMessage() + ")"));
                    se.setNextException(e);
                    throw se;
                } else {
                    throw e;
                }
            }
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
            } catch (PrivilegedActionException pae) {
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
            } catch (SQLException e) {
                LOG.finest("Caught exception closing resultSet: " + e.getMessage() + " - continuing");
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
            } catch (SQLException e) {
                LOG.finest("Caught exception closing statement: " + e.getMessage() + " - continuing");
            }
        }
        closeResources(connection);
    }

    private void closeResources(BatchingPreparedStatementWrapper statement) {
        if (cacheStatements) return;
        closeResources((BatchingStatementWrapper) statement);
    }

    private static void closeResources(BatchingStatementWrapper statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                LOG.finest("Caught exception closing statement: " + e.getMessage() + " - continuing");
            }
        }
    }

    /**
     * An extension point allowing the behavior of resource closing to be
     * overridden in derived classes.
     *
     * @param connection the connection to close
     */
    protected void closeResources(Connection connection) {
        if (cacheConnection) return;
        if (connection != null && dataSource != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOG.finest("Caught exception closing connection: " + e.getMessage() + " - continuing");
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

    private static List<List<Object>> calculateKeys(ResultSet keys) throws SQLException {
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

    private static void handleError(Connection connection, Throwable t) throws SQLException {
        if (connection != null) {
            LOG.warning("Rolling back due to: " + t.getMessage());
            connection.rollback();
        }
    }

    private static void callClosurePossiblyWithConnection(Closure closure, Connection connection) {
        if (closure.getMaximumNumberOfParameters() == 1) {
            closure.call(connection);
        } else {
            closure.call();
        }
    }

    private void clearStatementCache() {
        Statement statements[];
        if (statementCache.isEmpty())
            return;
        statements = new Statement[statementCache.size()];
        statementCache.values().toArray(statements);
        statementCache.clear();
        for (Statement s : statements) {
            try {
                s.close();
            } catch (Exception e) {
                // It's normally safe to ignore exceptions during cleanup but here if there is
                // a closed statement in the cache, the cache is possibly corrupted, hence log
                // at slightly elevated level than similar cases.
                LOG.info("Failed to close statement. Already closed? Exception message: " + e.getMessage());
            }
        }
    }

    private Statement getAbstractStatement(AbstractStatementCommand cmd, Connection connection, String sql) throws SQLException {
        Statement stmt;
        if (cacheStatements) {
            stmt = statementCache.get(sql);
            if (stmt == null) {
                stmt = cmd.execute(connection, sql);
                statementCache.put(sql, stmt);
            }
        } else {
            stmt = cmd.execute(connection, sql);
        }
        return stmt;
    }

    private Statement getStatement(Connection connection, String sql) throws SQLException {
        LOG.fine(sql);
        Statement stmt = getAbstractStatement(new CreateStatementCommand(), connection, sql);
        configure(stmt);
        return stmt;
    }

    private PreparedStatement getPreparedStatement(Connection connection, String sql, List<Object> params, int returnGeneratedKeys) throws SQLException {
        SqlWithParams updated = checkForNamedParams(sql, params);
        LOG.fine(updated.getSql() + " | " + updated.getParams());
        PreparedStatement statement = (PreparedStatement) getAbstractStatement(new CreatePreparedStatementCommand(returnGeneratedKeys), connection, updated.getSql());
        setParameters(updated.getParams(), statement);
        configure(statement);
        return statement;
    }

    private CallableStatement getCallableStatement(Connection connection, String sql, List<Object> params) throws SQLException {
        LOG.fine(sql + " | " + params);
        CallableStatement statement = (CallableStatement) getAbstractStatement(new CreateCallableStatementCommand(), connection, sql);
        setParameters(params, statement);
        configure(statement);
        return statement;
    }

    public SqlWithParams checkForNamedParams(String sql, List<Object> params) {
        SqlWithParams preCheck = buildSqlWithIndexedProps(sql);
        if (preCheck == null) {
            return new SqlWithParams(sql, params);
        }

        List<Tuple> indexPropList = new ArrayList<Tuple>();
        for (Object next : preCheck.getParams()) {
            indexPropList.add((Tuple) next);
        }
        return new SqlWithParams(preCheck.getSql(), getUpdatedParams(params, indexPropList));
    }

    /**
     * @deprecated Use {@link #buildSqlWithIndexedProps(String)} instead
     */
    @Deprecated
    public SqlWithParams preCheckForNamedParams(String sql) {
        return buildSqlWithIndexedProps(sql);
    }

    /**
     * Hook to allow derived classes to override behavior associated with the
     * parsing and indexing of parameters from a given sql statement.
     *
     * @param sql the sql statement to process
     * @return a {@link SqlWithParams} instance containing the parsed sql
     *         and parameters containing the indexed location and property
     *         name of parameters or {@code null} if no parsing of
     *         the sql was performed.
     */
    protected SqlWithParams buildSqlWithIndexedProps(String sql) {
        // look for quick exit
        if (!enableNamedQueries || !ExtractIndexAndSql.hasNamedParameters(sql)) {
            return null;
        }

        String newSql;
        List<Tuple> propList;
        if (cacheNamedQueries && namedParamSqlCache.containsKey(sql)) {
            newSql = namedParamSqlCache.get(sql);
            propList = namedParamIndexPropCache.get(sql);
        } else {
            ExtractIndexAndSql extractIndexAndSql = ExtractIndexAndSql.from(sql);
            newSql = extractIndexAndSql.getNewSql();
            propList = extractIndexAndSql.getIndexPropList();
            namedParamSqlCache.put(sql, newSql);
            namedParamIndexPropCache.put(sql, propList);
        }

        if (sql.equals(newSql)) {
            return null;
        }

        List<Object> indexPropList = new ArrayList<Object>(propList);
        return new SqlWithParams(newSql, indexPropList);
    }

    public List<Object> getUpdatedParams(List<Object> params, List<Tuple> indexPropList) {
        List<Object> updatedParams = new ArrayList<Object>();
        for (Tuple tuple : indexPropList) {
            int index = (Integer) tuple.get(0);
            String prop = (String) tuple.get(1);
            if (index < 0 || index >= params.size())
                throw new IllegalArgumentException("Invalid index " + index + " should be in range 1.." + params.size());
            try {
                updatedParams.add(prop.equals("<this>") ? params.get(index) : InvokerHelper.getProperty(params.get(index), prop));
            } catch(MissingPropertyException mpe) {
                throw new IllegalArgumentException("Property '" + prop + "' not found for parameter " + index);
            }
        }
        return updatedParams;
    }

    private PreparedStatement getPreparedStatement(Connection connection, String sql, List<Object> params) throws SQLException {
        return getPreparedStatement(connection, sql, params, 0);
    }

    /**
     * @return boolean    true if caching is enabled (the default is true)
     */
    public boolean isCacheNamedQueries() {
        return cacheNamedQueries;
    }

    /**
     * Enables named query caching.<br>
     * if <i>cacheNamedQueries</i> is true, cache is created and processed named queries will be cached.
     * if <i>cacheNamedQueries</i> is false, no caching will occur saving memory at the cost of additional processing time.
     *
     * @param cacheNamedQueries the new value
     */
    public void setCacheNamedQueries(boolean cacheNamedQueries) {
        this.cacheNamedQueries = cacheNamedQueries;
    }

    /**
     * @return boolean    true if named query processing is enabled (the default is true)
     */
    public boolean isEnableNamedQueries() {
        return enableNamedQueries;
    }

    /**
     * Enables named query support:
     * <ul>
     *     <li>if <i>enableNamedQueries</i> is true, queries with ':propname' and '?1.propname' style placeholders will be processed.</li>
     *     <li>if <i>enableNamedQueries</i> is false, this feature will be turned off.</li>
     * </ul>
     *
     * @param enableNamedQueries the new value
     */
    public void setEnableNamedQueries(boolean enableNamedQueries) {
        this.enableNamedQueries = enableNamedQueries;
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
        protected abstract Statement execute(Connection conn, String sql) throws SQLException;
    }

    private class CreatePreparedStatementCommand extends AbstractStatementCommand {
        private final int returnGeneratedKeys;

        private CreatePreparedStatementCommand(int returnGeneratedKeys) {
            this.returnGeneratedKeys = returnGeneratedKeys;
        }

        @Override
        protected PreparedStatement execute(Connection connection, String sql) throws SQLException {
            if (returnGeneratedKeys == USE_COLUMN_NAMES && keyColumnNames != null) {
                return connection.prepareStatement(sql, keyColumnNames.toArray(EMPTY_STRING_ARRAY));
            }
            if (returnGeneratedKeys != 0) {
                return connection.prepareStatement(sql, returnGeneratedKeys);
            }
            if (appearsLikeStoredProc(sql)) {
                if (resultSetHoldability == -1) {
                    return connection.prepareCall(sql, resultSetType, resultSetConcurrency);
                }
                return connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
            }
            if (resultSetHoldability == -1) {
                return connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
            }
            return connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        private boolean appearsLikeStoredProc(String sql) {
            return sql.matches("\\s*[{]?\\s*[?]?\\s*[=]?\\s*[cC][aA][lL][lL].*");
        }
    }

    private class CreateCallableStatementCommand extends AbstractStatementCommand {
        @Override
        protected CallableStatement execute(Connection connection, String sql) throws SQLException {
            return connection.prepareCall(sql);
        }
    }

    private class CreateStatementCommand extends AbstractStatementCommand {
        @Override
        protected Statement execute(Connection conn, String sql) throws SQLException {
            return createStatement(conn);
        }
    }

    protected abstract class AbstractQueryCommand {
        protected final String sql;
        protected Statement statement;
        private Connection connection;
        private int maxRows = 0;

        protected AbstractQueryCommand(String sql) {
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
        protected final ResultSet execute() throws SQLException {
            connection = createConnection();
            setInternalConnection(connection);
            statement = null;
            try {
                // The variation in the pattern is isolated
                ResultSet result = runQuery(connection);
                assert (null != statement);
                return result;
            } catch (SQLException e) {
                LOG.warning("Failed to execute: " + sql + " because: " + e.getMessage());
                closeResources();
                connection = null;
                statement = null;
                throw e;
            }
        }

        /**
         * After performing the execute operation and making use of its return, it's necessary
         * to free the resources allocated for the statement.
         */
        protected final void closeResources() {
            Sql.this.closeResources(connection, statement);
        }

        /**
         * After performing the execute operation and making use of its return, it's necessary
         * to free the resources allocated for the statement.
         *
         * @param rs allows the caller to conveniently close its resource as well
         */
        protected final void closeResources(ResultSet rs) {
            Sql.this.closeResources(connection, statement, rs);
        }

        /**
         * Perform the query. Must set statement field so that the main ({@link #execute()}) method can clean up.
         * This is the method that encloses the variant part of the code.
         *
         * @param connection the connection to use
         * @return ResultSet from an executeQuery method.
         * @throws SQLException if a database error occurs
         */
        protected abstract ResultSet runQuery(Connection connection) throws SQLException;

        /**
         * Set the maximum number of rows to return in the ResultSet
         *
         * @param maxRows the maximum number of rows
         */
        protected void setMaxRows(int maxRows) {
            this.maxRows = maxRows;
        }

        /**
         * Get the maximum number of rows to return in the ResultSet
         *
         * @return the maximum number of rows
         */
        protected int getMaxRows() {
            return maxRows;
        }
    }

    private final class PreparedQueryCommand extends AbstractQueryCommand {
        private List<Object> params;

        private PreparedQueryCommand(String sql, List<Object> queryParams) {
            super(sql);
            params = queryParams;
        }

        @Override
        protected ResultSet runQuery(Connection connection) throws SQLException {
            PreparedStatement s = getPreparedStatement(connection, sql, params);
            statement = s;
            if (getMaxRows() != 0) statement.setMaxRows(getMaxRows());
            return s.executeQuery();
        }
    }

    private final class QueryCommand extends AbstractQueryCommand {

        private QueryCommand(String sql) {
            super(sql);
        }

        @Override
        protected ResultSet runQuery(Connection connection) throws SQLException {
            statement = getStatement(connection, sql);
            if (getMaxRows() != 0) statement.setMaxRows(getMaxRows());
            return statement.executeQuery(sql);
        }
    }

    /**
     * Factory for the QueryCommand command pattern object allows subclasses to
     * supply implementations of the command class. The factory will be used in a pattern
     * similar to:
     * <pre>
     * AbstractQueryCommand q = createQueryCommand("update TABLE set count = 0) where count is null");
     * try {
     *     ResultSet rs = q.execute();
     *     return asList(rs);
     * } finally {
     *     q.closeResources();
     * }
     * </pre>
     *
     * @param sql statement to be executed
     * @return a command - invoke its execute() and closeResource() methods
     */
    protected AbstractQueryCommand createQueryCommand(String sql) {
        return new QueryCommand(sql);
    }

    /**
     * Factory for the PreparedQueryCommand command pattern object allows subclass to supply implementations
     * of the command class.
     *
     * @param sql         statement to be executed, including optional parameter placeholders (?)
     * @param queryParams List of parameter values corresponding to parameter placeholders
     * @return a command - invoke its execute() and closeResource() methods
     * @see #createQueryCommand(String)
     */
    protected AbstractQueryCommand createPreparedQueryCommand(String sql, List<Object> queryParams) {
        return new PreparedQueryCommand(sql, queryParams);
    }

    /**
     * Stub needed for testing.  Called when a connection is opened by one of the command-pattern classes
     * so that a test case can monitor the state of the connection through its subclass.
     *
     * @param conn the connection that is about to be used by a command
     */
    protected void setInternalConnection(Connection conn) {
    }

}
