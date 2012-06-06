/*
 * Copyright 2003-2012 the original author or authors.
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
import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.stmt.Statement;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An enhancement of Groovy's Sql class providing support for accessing
 * and querying databases using POGO fields and operators rather than
 * JDBC-level API calls and RDBMS column names. So, instead of a query like:
 * <pre>
 * def db = // an instance of groovy.sql.Sql
 * def sql = '''select * from Person
 *     where (purchaseCount > ? and birthMonth = ?)
 *     and (lastName &lt; ? or lastName > ?)
 *     and age &lt; ? and age > ? and firstName != ?
 *     order by firstName DESC, age'''
 * def params = [10, "January", "Zulu", "Alpha", 99, 5, "Bert"]
 * def sortedPeopleOfInterest = db.rows(sql, params)
 * </pre>
 * You can write code like this:
 * <pre>
 * def person = new DataSet(db, 'Person') // or db.dataSet('Person'), or db.dataSet(Person)
 * def janFrequentBuyers = person.findAll { it.purchaseCount > 10 && it.lastName == "January" }
 * def sortedPeopleOfInterest = janFrequentBuyers.
 *     findAll{ it.lastName &lt; 'Zulu' || it.lastName > 'Alpha' }.
 *     findAll{ it.age &lt; 99 }.
 *     findAll{ it.age > 5 }.
 *     sort{ it.firstName }.reverse().
 *     findAll{ it.firstName != 'Bert' }.
 *     sort{ it.age }
 * </pre>
 * Currently, the Groovy source code for any accessed POGO must be on the
 * classpath at runtime. Also, at the moment, the expressions (or nested expressions) can only contain
 * references to fields of the POGO or literals (i.e. constant Strings or numbers). This limitation
 * may be removed in a future version of Groovy.
 *
 * @author Chris Stevenson
 * @author Paul King
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 */
public class DataSet extends Sql {

    private Closure where;
    private Closure sort;
    private boolean reversed = false;
    private DataSet parent;
    private String table;
    private SqlWhereVisitor visitor;
    private SqlOrderByVisitor sortVisitor;
    private String sql;
    private List<Object> params;
    private Sql delegate;

    public DataSet(Sql sql, Class type) {
        super(sql);
        delegate = sql;
        String table = type.getName();
        int idx = table.lastIndexOf('.');
        if (idx > 0) {
            table = table.substring(idx + 1);
        }
        this.table = table.toLowerCase();
    }

    public DataSet(Sql sql, String table) {
        super(sql);
        delegate = sql;
        this.table = table;
    }

    private DataSet(DataSet parent, Closure where) {
        super(parent);
        this.delegate = parent.delegate;
        this.table = parent.table;
        this.parent = parent;
        this.where = where;
    }

    private DataSet(DataSet parent, Closure where, Closure sort) {
        super(parent);
        this.delegate = parent.delegate;
        this.table = parent.table;
        this.parent = parent;
        this.where = where;
        this.sort = sort;
    }

    private DataSet(DataSet parent) {
        super(parent);
        this.delegate = parent.delegate;
        this.table = parent.table;
        this.parent = parent;
        this.reversed = true;
    }

    @Override
    protected Connection createConnection() throws SQLException {
        return delegate.createConnection();
    }

    @Override
    protected void closeResources(Connection connection, java.sql.Statement statement, ResultSet results) {
        delegate.closeResources(connection, statement, results);
    }

    @Override
    protected void closeResources(Connection connection, java.sql.Statement statement) {
        delegate.closeResources(connection, statement);
    }

    @Override
    public void cacheConnection(Closure closure) throws SQLException {
        delegate.cacheConnection(closure);
    }

    @Override
    public void withTransaction(Closure closure) throws SQLException {
        delegate.withTransaction(closure);
    }

    @Override
    public void commit() throws SQLException {
        delegate.commit();
    }

    @Override
    public void rollback() throws SQLException {
        delegate.rollback();
    }

    public void add(Map<String, Object> map) throws SQLException {
        StringBuilder buffer = new StringBuilder("insert into ");
        buffer.append(table);
        buffer.append(" (");
        StringBuilder paramBuffer = new StringBuilder();
        boolean first = true;
        for (String column : map.keySet()) {
            if (first) {
                first = false;
                paramBuffer.append("?");
            } else {
                buffer.append(", ");
                paramBuffer.append(", ?");
            }
            buffer.append(column);
        }
        buffer.append(") values (");
        buffer.append(paramBuffer.toString());
        buffer.append(")");

        int answer = executeUpdate(buffer.toString(), new ArrayList<Object>(map.values()));
        if (answer != 1) {
            LOG.warning("Should have updated 1 row not " + answer + " when trying to add: " + map);
        }
    }

    public DataSet findAll(Closure where) {
        return new DataSet(this, where);
    }

    public DataSet sort(Closure sort) {
        return new DataSet(this, null, sort);
    }

    public DataSet reverse() {
        if (sort == null) {
            throw new GroovyRuntimeException("reverse() only allowed immediately after a sort()");
        }
        return new DataSet(this);
    }

    /**
     * Calls the provided closure for each of the rows of the table represented by this DataSet.
     *
     * @param closure called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     * @see groovy.sql.Sql#eachRow(String, java.util.List, groovy.lang.Closure)
     */
    public void each(Closure closure) throws SQLException {
        eachRow(getSql(), getParameters(), closure);
    }

    /**
     * Calls the provided closure for a "page" of rows from the table represented by this DataSet.
     * A page is defined as starting at a 1-based offset, and containing a maximum number of rows.
     *
     * @param offset  the 1-based offset for the first row to be processed
     * @param maxRows the maximum number of rows to be processed
     * @param closure called for each row with a GroovyResultSet
     * @throws SQLException if a database access error occurs
     * @see groovy.sql.Sql#eachRow(String, java.util.List, int, int, groovy.lang.Closure)
     */
    public void each(int offset, int maxRows, Closure closure) throws SQLException {
        eachRow(getSql(), getParameters(), offset, maxRows, closure);
    }

    private String getSqlWhere() {
        String whereClaus = "";
        String parentClaus = "";
        if (parent != null) {
            parentClaus = parent.getSqlWhere();
        }
        if (where != null) {
            whereClaus += getSqlWhereVisitor().getWhere();
        }
        if (parentClaus.length() == 0) return whereClaus;
        if (whereClaus.length() == 0) return parentClaus;
        return parentClaus + " and " + whereClaus;
    }

    private String getSqlOrderBy() {
        String sortByClaus = "";
        String parentClaus = "";
        if (parent != null) {
            parentClaus = parent.getSqlOrderBy();
        }
        if (reversed) {
            if (parentClaus.length() > 0) parentClaus += " DESC";
        }
        if (sort != null) {
            sortByClaus += getSqlOrderByVisitor().getOrderBy();
        }
        if (parentClaus.length() == 0) return sortByClaus;
        if (sortByClaus.length() == 0) return parentClaus;
        return parentClaus + ", " + sortByClaus;
    }

    public String getSql() {
        if (sql == null) {
            sql = "select * from " + table;
            String whereClaus = getSqlWhere();
            if (whereClaus.length() > 0) {
                sql += " where " + whereClaus;
            }
            String orderByClaus = getSqlOrderBy();
            if (orderByClaus.length() > 0) {
                sql += " order by " + orderByClaus;
            }
        }
        return sql;
    }

    public List<Object> getParameters() {
        if (params == null) {
            params = new ArrayList<Object>();
            if (parent != null) {
                params.addAll(parent.getParameters());
            }
            params.addAll(getSqlWhereVisitor().getParameters());
        }
        return params;
    }

    protected SqlWhereVisitor getSqlWhereVisitor() {
        if (visitor == null) {
            visitor = new SqlWhereVisitor();
            visit(where, visitor);
        }
        return visitor;
    }

    protected SqlOrderByVisitor getSqlOrderByVisitor() {
        if (sortVisitor == null) {
            sortVisitor = new SqlOrderByVisitor();
            visit(sort, sortVisitor);
        }
        return sortVisitor;
    }

    private void visit(Closure closure, CodeVisitorSupport visitor) {
        if (closure != null) {
            ClassNode classNode = closure.getMetaClass().getClassNode();
            if (classNode == null) {
                throw new GroovyRuntimeException(
                        "DataSet unable to evaluate expression. AST not available for closure: " + closure.getMetaClass().getTheClass().getName() +
                                ". Is the source code on the classpath?");
            }
            List methods = classNode.getDeclaredMethods("doCall");
            if (!methods.isEmpty()) {
                MethodNode method = (MethodNode) methods.get(0);
                if (method != null) {
                    Statement statement = method.getCode();
                    if (statement != null) {
                        statement.visit(visitor);
                    }
                }
            }
        }
    }

    /*
    * create a subset of the original dataset
    */
    public DataSet createView(Closure criteria) {
        return new DataSet(this, criteria);
    }

    /**
     * Returns a List of all of the rows from the table a DataSet
     * represents.
     *
     * @return Returns a list of GroovyRowResult objects from the dataset
     * @throws SQLException if a database error occurs
     */
    public List rows() throws SQLException {
        return rows(getSql(), getParameters());
    }

    /**
     * Returns a "page" of the rows from the table a DataSet represents. A page
     * is defined as starting at a 1-based offset, and containing a maximum number
     * of rows.
     *
     * @param offset the 1-based offset for the first row to be processed
     * @param maxRows the maximum number of rows to be processed
     * @return a list of GroovyRowResult objects from the dataset
     * @throws SQLException if a database error occurs
     */
    public List rows(int offset, int maxRows) throws SQLException {
        return rows(getSql(), getParameters(), offset, maxRows);
    }

    /**
     * Returns the first row from a DataSet's underlying table
     *
     * @return Returns the first GroovyRowResult object from the dataset
     * @throws SQLException if a database error occurs
     */
    public Object firstRow() throws SQLException {
        List rows = rows();
        if (rows.isEmpty()) return null;
        return (rows.get(0));
    }

    @Override
    public void close() {
        delegate.close();
        super.close();
    }
}
