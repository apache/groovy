/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package groovy.gdo;

import groovy.lang.Closure;
import groovy.lang.GString;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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

    public Sql(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSet dataSet(String table) { 
        return new DataSet(dataSource, table);
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
     * Performs the given SQL query with parameters calling the closure with the result set
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
     * Performs the given SQL query calling the closure with each row of the result set
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
     * Executes the given SQL with embedded expressions inside
     */
    public boolean execute(GString gstring) throws SQLException {
        String sql = asSql(gstring);
        List params = getParameters(gstring);
        return execute(sql, params);
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
    
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * @return the SQL version of the given query using ? instead of any parameter
     */
    protected String asSql(GString gstring) {
        String[] strings = gstring.getStrings();
        if (strings.length <= 0) {
            throw new IllegalArgumentException("No SQL specified in GString: " + gstring);
        }
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < strings.length; i++) {
            buffer.append(strings[i]);
            buffer.append("?");
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
        for (Iterator iter = params.iterator(); iter.hasNext(); ) {
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
        return dataSource.getConnection();
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
        try {
            statement.close();
        }
        catch (SQLException e) {
            log.log(Level.SEVERE, "Caught exception closing statement: " + e, e);
        }
        try {
            connection.close();
        }
        catch (SQLException e) {
            log.log(Level.SEVERE, "Caught exception closing connection: " + e, e);
        }
    }

}
