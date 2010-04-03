package groovy.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Class which delegates to a Statement but keeps track of
 * a batch count size. If the batch count reaches the predefined number,
 * this Statement does an executeBatch() automatically. If batchSize is
 * zero, then no batching is performed.
 */
public class BatchingStatementWrapper implements Statement {
    private Statement delegate;
    private int batchSize;
    private int batchCount;
    private Connection connection;
    private Logger log;
    private List<Integer> results;

    public BatchingStatementWrapper(Statement delegate, int batchSize, Logger log, Connection connection) {
        this.delegate = delegate;
        this.batchSize = batchSize;
        this.connection = connection;
        this.log = log;
        this.batchCount = 0;
        results = new ArrayList<Integer>();
    }

    public void addBatch(String sql) throws SQLException {
        delegate.addBatch(sql);
        batchCount++;
        if (batchSize != 0 && batchCount % batchSize == 0) {
            int[] result = delegate.executeBatch();
            connection.commit();
            for (int i : result) {
                results.add(i);
            }
            log.fine("Successfully executed batch with " + result.length + " command(s)");
        }
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        return delegate.executeQuery(sql);
    }

    public int executeUpdate(String sql) throws SQLException {
        return delegate.executeUpdate(sql);
    }

    public void close() throws SQLException {
        delegate.close();
    }

    public int getMaxFieldSize() throws SQLException {
        return delegate.getMaxFieldSize();
    }

    public void setMaxFieldSize(int max) throws SQLException {
        delegate.setMaxFieldSize(max);
    }

    public int getMaxRows() throws SQLException {
        return delegate.getMaxRows();
    }

    public void setMaxRows(int max) throws SQLException {
        delegate.setMaxRows(max);
    }

    public void setEscapeProcessing(boolean enable) throws SQLException {
        delegate.setEscapeProcessing(enable);
    }

    public int getQueryTimeout() throws SQLException {
        return delegate.getQueryTimeout();
    }

    public void setQueryTimeout(int seconds) throws SQLException {
        delegate.setQueryTimeout(seconds);
    }

    public void cancel() throws SQLException {
        delegate.cancel();
    }

    public SQLWarning getWarnings() throws SQLException {
        return delegate.getWarnings();
    }

    public void clearWarnings() throws SQLException {
        delegate.clearWarnings();
    }

    public void setCursorName(String name) throws SQLException {
        delegate.setCursorName(name);
    }

    public boolean execute(String sql) throws SQLException {
        return delegate.execute(sql);
    }

    public ResultSet getResultSet() throws SQLException {
        return delegate.getResultSet();
    }

    public int getUpdateCount() throws SQLException {
        return delegate.getUpdateCount();
    }

    public boolean getMoreResults() throws SQLException {
        return delegate.getMoreResults();
    }

    public void setFetchDirection(int direction) throws SQLException {
        delegate.setFetchDirection(direction);
    }

    public int getFetchDirection() throws SQLException {
        return delegate.getFetchDirection();
    }

    public void setFetchSize(int rows) throws SQLException {
        delegate.setFetchSize(rows);
    }

    public int getFetchSize() throws SQLException {
        return delegate.getFetchSize();
    }

    public int getResultSetConcurrency() throws SQLException {
        return delegate.getResultSetConcurrency();
    }

    public int getResultSetType() throws SQLException {
        return delegate.getResultSetType();
    }

    public void clearBatch() throws SQLException {
        if (batchSize != 0) {
            results = new ArrayList<Integer>();
        }
        delegate.clearBatch();
    }

    public int[] executeBatch() throws SQLException {
        if (batchSize == 0) {
            return delegate.executeBatch();
        } else {
            int[] lastResult = delegate.executeBatch();
            for (int i : lastResult) {
                results.add(i);
            }
            log.fine("Successfully executed batch with " + lastResult.length + " command(s)");
            int[] result = new int[results.size()];
            for (int i = 0; i < results.size(); i++) {
                result[i] = results.get(i);
            }
            results = new ArrayList<Integer>();
            return result;
        }
    }

    public Connection getConnection() throws SQLException {
        return delegate.getConnection();
    }

    public boolean getMoreResults(int current) throws SQLException {
        return delegate.getMoreResults(current);
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        return delegate.getGeneratedKeys();
    }

    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return delegate.executeUpdate(sql, autoGeneratedKeys);
    }

    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return delegate.executeUpdate(sql, columnIndexes);
    }

    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return delegate.executeUpdate(sql, columnNames);
    }

    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return delegate.execute(sql, autoGeneratedKeys);
    }

    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return delegate.execute(sql, columnIndexes);
    }

    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return delegate.execute(sql, columnNames);
    }

    public int getResultSetHoldability() throws SQLException {
        return delegate.getResultSetHoldability();
    }

    public boolean isClosed() throws SQLException {
        return delegate.isClosed();
    }

    public void setPoolable(boolean poolable) throws SQLException {
        delegate.setPoolable(poolable);
    }

    public boolean isPoolable() throws SQLException {
        return delegate.isPoolable();
    }
}
