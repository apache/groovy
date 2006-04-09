package groovy.sql;
/**
 * Identifies a variable to be expanded into the
 * sql string rather than representing a placeholder.
 * @author rfuller
 *
 */
public interface ExpandedVariable {
    public Object getObject();
}
