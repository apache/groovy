
package groovy.sql;

/**
 * @author rfuller
 *
 * A parameter to be returned from a CallableStatement.
 */
public interface OutParameter {
	/**
	 * Get the JDBC datatype for this parameter.
	 */
     public int getType();
}
