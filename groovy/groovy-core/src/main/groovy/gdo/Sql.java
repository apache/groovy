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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.*;

import javax.sql.*;

import groovy.lang.Closure;
import groovy.lang.GString;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents an extent of objects
 * 
 * @author Chris Stevenson
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class Sql {

    private Log log = LogFactory.getLog(getClass());
    private DataSource dataSource;

    public Sql(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Performs the given SQL query calling the closure with the result set
     */
    public void query(String sql, Closure closure) throws SQLException {
        Connection connection = createConnection();
        Statement statement = connection.createStatement();
        ResultSet results = null;
        try {
            log.debug(sql);
            results = statement.executeQuery(sql);
            closure.call(results);
        }
        catch (SQLException e) {
            log.warn("Failed to execute: " + sql, e);
            throw e;
        }
        finally {
            if (results != null) {
                try {
                    results.close();
                }
                catch (SQLException e) {
                    // ignore 
                }
            }
            try {
                statement.close();
            }
            catch (SQLException e) {
                // ignore 
            }
            try {
                connection.close();
            }
            catch (SQLException e) {
                // ignore 
            }
        }
    }
    
    /**
     * Executes the given piece of SQL
     */
    public boolean execute(String sql) throws SQLException {
        Connection connection = createConnection();
        Statement statement = connection.createStatement();
        try {
            log.debug(sql);
            return statement.execute(sql);
        }
        catch (SQLException e) {
            log.warn("Failed to execute: " + sql, e);
            throw e;
        }
        finally {
            try {
                statement.close();
            }
            catch (SQLException e) {
                // ignore 
            }
            try {
                connection.close();
            }
            catch (SQLException e) {
                // ignore 
            }
        }
    }

    /**
     * Executes the given SQL with embedded expressions inside
     */
    public int execute(GString gstring) throws Exception {
        Connection connection = createConnection();
        String sql = asSql(gstring);
        PreparedStatement statement = connection.prepareStatement(sql);
        try {
            log.debug(gstring);
            setParameters(gstring, statement);
            return statement.executeUpdate();
        }
        catch (SQLException e) {
            log.warn("Failed to execute: " + sql, e);
            throw e;
        }
        finally {
            try {
                statement.close();
            }
            catch (SQLException e) {
                // ignore 
            }
            try {
                connection.close();
            }
            catch (SQLException e) {
                // ignore 
            }
        }
    }

    /**
     * Performs a stored procedure call with the given parameters
     */
    public int call(GString gstring) throws Exception {
        Connection connection = createConnection();
        String sql = asSql(gstring);
        CallableStatement statement = connection.prepareCall(sql);
        try {
            log.debug(gstring);
            setParameters(gstring, statement);
            return statement.executeUpdate();
        }
        catch (SQLException e) {
            log.warn("Failed to execute: " + gstring, e);
            throw e;
        }
        finally {
            try {
                statement.close();
            }
            catch (SQLException e) {
                // ignore 
            }
            try {
                connection.close();
            }
            catch (SQLException e) {
                // ignore 
            }
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    protected void setParameters(GString gstring, PreparedStatement statement) throws SQLException {
        Object[] values = gstring.getValues();
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            setObject(statement, i + 1, value);
        }
    }

    protected String asSql(GString gstring) {
        String[] strings = gstring.getStrings();
        if (strings.length <= 0) {
            throw new IllegalArgumentException("No SQL specified in GString: " + gstring);
        } 
        StringBuffer buffer = new StringBuffer(strings[0]);
        for (int i = 1; i < strings.length; i++ ) {
            buffer.append("?");
            buffer.append(strings[i]);
        }

        String sql = buffer.toString();
        return sql;
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

}
