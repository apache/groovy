
package groovy.sql;

/**
 * @author rfuller
 *
 * A typed parameter to pass to a query
 */
public interface InParameter {
	/**
	 * The JDBC data type.
	 * @return
	 */
	public int getType();
	/**
	 * The object holding the data value.
	 * @return
	 */
	public Object getValue();
}