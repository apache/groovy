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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.sql.DataSource;

import groovy.lang.Closure;

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.stmt.Statement;

/**
 * Represents an extent of objects
 * 
 * @author Chris Stevenson
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class DataSet extends Sql {

    private Closure where;
    private DataSet parent;
    private String table;
    private SqlWhereVisitor visitor;
    private String sql;
    private List params;

    public DataSet(DataSource dataSource, Class type) {
        super(dataSource);
        String table = type.getName();
        int idx = table.lastIndexOf('.');
        if (idx > 0) {
            table = table.substring(idx + 1);
        }
        this.table = table.toLowerCase();
    }

    public DataSet(DataSource dataSource, String table) {
        super(dataSource);
        this.table = table;
    }

    public DataSet(DataSet parent, Closure where) {
        this(parent.getDataSource(), parent.table);
        this.parent = parent;
        this.where = where;
    }

    public void add(Map values) throws SQLException {
        StringBuffer buffer = new StringBuffer("insert into ");
        buffer.append(table);
        buffer.append(" (");
        StringBuffer paramBuffer = new StringBuffer();
        boolean first = true;
        for (Iterator iter = values.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            String column = entry.getKey().toString();
            if (first) {
                first = false;
                paramBuffer.append("?");
            }
            else {
                buffer.append(", ");
                paramBuffer.append(", ?");
            }
            buffer.append(column);
        }
        buffer.append(") values (");
        buffer.append(paramBuffer.toString());
        buffer.append(")");

        Connection connection = createConnection();
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(buffer.toString());
            int i = 1;
            for (Iterator iter = values.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                setObject(statement, i++, entry.getValue());
            }
            int answer = statement.executeUpdate();
            if (answer != 1) {
                log.log(Level.WARNING, "Should have updated 1 row not " + answer + " when trying to add: " + values);
            }
        }
        catch (SQLException e) {
            log.log(Level.WARNING, "Failed to add row for: " + values, e);
            throw e;
        }
        finally {
            closeResources(connection, statement);
        }
    }

    public DataSet findAll(Closure where) {
        return new DataSet(this, where);
    }

    public void each(Closure closure) throws SQLException {
        queryEach(getSql(), getParameters(), closure);
    }

    public String getSql() {
        if (sql == null) {
            sql = "select * from " + table;
            if (where != null) {
                sql += " where ";
                if (parent != null && parent.where != null) {
                    sql += parent.getSqlVisitor().getWhere() + " and ";
                }
                sql += getSqlVisitor().getWhere();
            }
        }
        return sql;
    }

    public List getParameters() {
        if (params == null) {
            params = new ArrayList();
            if (parent != null && parent.where != null) {
                params.addAll(parent.getParameters());
            }
            params.addAll(getSqlVisitor().getParameters());
        }
        return params;
    }

    protected SqlWhereVisitor getSqlVisitor() {
        if (visitor == null) {
            visitor = new SqlWhereVisitor();
            if (where != null) {
                MethodNode method = where.getMetaClass().getClassNode().getMethod("doCall");
                Statement statement = method.getCode();
                statement.visit(visitor);
            }
        }
        return visitor;
    }
}
