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

import groovy.lang.GString;
import groovy.lang.GroovyObjectSupport;
import org.codehaus.groovy.runtime.ArrayGroovyMethods;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Class which delegates to a Statement but keeps track of a batch count size.
 * If the batch count reaches the predefined number, this Statement does an executeBatch()
 * automatically. If batchSize is zero, then no batching is performed.
 */
public class BatchingStatementWrapper extends GroovyObjectSupport implements AutoCloseable {
    private final Statement delegate;
    /** Automatic execution threshold; {@code 0} disables automatic partitioning. */
    protected int batchSize;
    /** Number of commands added since the last delegate batch execution. */
    protected int batchCount;
    /** Logger used for batch diagnostics. */
    protected Logger log;
    /** Accumulated update counts across delegate batch executions. */
    protected List<Integer> results;
    /** Whether the interpolation warning has already been logged for this wrapper. */
    private boolean interpolationWarned;

    /**
     * Creates a batching wrapper for a statement.
     *
     * @param delegate the statement to wrap
     * @param batchSize the automatic execution threshold; {@code 0} disables automatic partitioning
     * @param log the logger to use for batch diagnostics
     */
    public BatchingStatementWrapper(Statement delegate, int batchSize, Logger log) {
        this.delegate = delegate;
        this.batchSize = batchSize;
        this.log = log;
        reset();
    }

    /**
     * Resets the wrapper's in-memory batch bookkeeping.
     */
    protected void reset() {
        batchCount = 0;
        results = new ArrayList<Integer>();
    }

    /**
     * Delegates unknown method calls to the wrapped {@link Statement}.
     *
     * @param name the method name
     * @param args the method arguments
     * @return the delegated result
     */
    @Override
    public Object invokeMethod(String name, Object args) {
        return InvokerHelper.invokeMethod(delegate, name, args);
    }

    /**
     * Adds a SQL command to the current batch.
     * <p>
     * The command is used as SQL text. Unlike the query methods on {@link Sql}, a {@link GString}
     * passed here is <b>not</b> converted into {@code PreparedStatement} placeholders: a JDBC
     * {@link Statement} batch may hold different statements, so there is nothing to bind against.
     * Interpolating an untrusted value therefore composes it into the SQL. To bind values, batch
     * against a single statement with {@link Sql#withBatch(String, groovy.lang.Closure)}, whose
     * wrapper takes parameters rather than text.
     *
     * @param sql the SQL command to add
     * @throws SQLException if the command cannot be added
     */
    public void addBatch(String sql) throws SQLException {
        delegate.addBatch(sql);
        incrementBatchCount();
    }

    /**
     * Adds a SQL command to the current batch, composed from a {@link GString}.
     * <p>
     * Present so the coercion is visible rather than silent: the {@code GString} is rendered to text
     * and added as SQL, and a warning is logged the first time this happens with an interpolated
     * value that is not a {@link Sql#expand deliberate expansion}. It is not rejected, because a
     * {@link Statement} batch has no parameterised equivalent to redirect callers to &mdash; batching
     * heterogeneous statements, generated DDL or identifiers that cannot be bound are all legitimate
     * uses of this method.
     *
     * @param sql the SQL command to add
     * @throws SQLException if the command cannot be added
     * @since 6.0.0
     */
    public void addBatch(GString sql) throws SQLException {
        warnIfInterpolated(sql);
        addBatch(render(sql));
    }

    /**
     * Renders the command to text, substituting the value behind any {@link Sql#expand deliberate
     * expansion} rather than the marker object itself, as {@link Sql#asSql} does. A command with no
     * expansions renders exactly as the {@code GString} would on its own, leaving the long-standing
     * behaviour of this method untouched.
     *
     * @param sql the command to render
     * @return the SQL text
     */
    private static String render(GString sql) {
        Object[] values = sql.getValues();
        boolean expanded = false;
        for (Object value : values) {
            if (value instanceof ExpandedVariable) {
                expanded = true;
                break;
            }
        }
        if (!expanded) {
            return sql.toString();
        }
        String[] strings = sql.getStrings();
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            buffer.append(strings[i]);
            if (i < values.length) {
                Object value = values[i];
                buffer.append(value instanceof ExpandedVariable ? ((ExpandedVariable) value).getObject() : value);
            }
        }
        return buffer.toString();
    }

    /**
     * Logs, at most once per wrapper, that a value has been composed into batch SQL as text.
     * <p>
     * Mirrors what {@link Sql#asSql} does when it inlines rather than binds: wherever Groovy puts a
     * dynamic value into SQL text, it says so. A {@code GString} with no values, or whose values are
     * all {@link ExpandedVariable}, is deliberate and passes quietly.
     *
     * @param sql the command being added
     */
    private void warnIfInterpolated(GString sql) {
        if (interpolationWarned) {
            return;
        }
        for (Object value : sql.getValues()) {
            if (!(value instanceof ExpandedVariable)) {
                log.warning("A dynamic expression (one starting with $) was composed into batch SQL as text. " +
                        "A JDBC Statement batch cannot bind parameters, so the value is part of the statement " +
                        "and an untrusted value here is a SQL injection vulnerability. To bind values, batch " +
                        "against a single statement using Sql.withBatch(String, Closure). If the interpolation " +
                        "is deliberate, wrap the value in Sql.expand(...) to say so and silence this warning. " +
                        "The statement so far is: " + sql);
                interpolationWarned = true;
                return;
            }
        }
    }

    /**
     * Increments batch count (after addBatch(..) has been called)
     * and execute {@code delegate.executeBatch()} if batchSize has been reached.
     */
    protected void incrementBatchCount() throws SQLException {
        batchCount++;
        if (batchCount == batchSize /* never true for batchSize of 0 */) {
            int[] result = delegate.executeBatch();
            processResult(result);
            batchCount = 0;
        }
    }

    /**
     * Clears the current batch and resets this wrapper's batch bookkeeping.
     *
     * @throws SQLException if the wrapped statement fails to clear its batch
     */
    public void clearBatch() throws SQLException {
        if (batchSize != 0) {
            reset();
        }
        delegate.clearBatch();
    }

    /**
     * Executes any pending batched commands and returns the aggregated update counts.
     *
     * @return one update count per executed batch command
     * @throws SQLException if batch execution fails
     */
    public int[] executeBatch() throws SQLException {
        if (shouldCallDelegate()) {
            int[] lastResult = delegate.executeBatch();
            processResult(lastResult);
        }
        int[] result = new int[results.size()];
        for (int i = 0; i < results.size(); i++) {
            result[i] = results.get(i);
        }
        reset();
        return result;
    }

    private boolean shouldCallDelegate() {
        if (batchCount > 0) {
            return true;
        } else if (results.isEmpty()) {
            log.warning("Nothing has been added to batch. This might cause the JDBC driver to throw an exception.");
            return true;
        }
        // Nothing added since last delegate execution. No need to call the delegate this time.
        return false;
    }

    /**
     * Incorporates one delegate batch execution result into this wrapper's state.
     *
     * @param lastResult the update counts returned by the wrapped statement
     */
    protected void processResult(int[] lastResult) {
        boolean foundError = false;
        for (int i : lastResult) {
            if (i == Statement.EXECUTE_FAILED) foundError = true;
            results.add(i);
        }
        // A little bit of paranoid checking here? Most drivers will throw BatchUpdateException perhaps?
        if (batchCount != lastResult.length) {
            log.warning("Problem executing batch - expected result length of " + batchCount + " but got " + lastResult.length);
        } else if (foundError) {
            log.warning("Problem executing batch - at least one result failed in: " + ArrayGroovyMethods.toList(lastResult));
        } else {
            log.fine("Successfully executed batch with " + lastResult.length + " command(s)");
        }
    }

    /**
     * Closes the wrapped statement.
     *
     * @throws SQLException if the statement cannot be closed
     */
    @Override
    public void close() throws SQLException {
        delegate.close();
    }
}
