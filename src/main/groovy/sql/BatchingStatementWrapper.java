package groovy.sql;

import groovy.lang.GroovyObjectSupport;
import org.codehaus.groovy.runtime.InvokerHelper;

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
public class BatchingStatementWrapper extends GroovyObjectSupport {
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

    @Override
    public Object invokeMethod(String name, Object args) {
        return InvokerHelper.invokeMethod(delegate, name, args);
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

    public void clearBatch() throws SQLException {
        if (batchSize != 0) {
            results = new ArrayList<Integer>();
        }
        delegate.clearBatch();
    }

    public int[] executeBatch() throws SQLException {
        if (batchSize == 0) {
            int[] result = delegate.executeBatch();
            log.fine("Successfully executed batch with " + result.length + " command(s)");
            return result;
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

    public void close() throws SQLException {
        delegate.close();
    }
}
