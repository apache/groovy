
package groovy.sql;

/**
 * @author rfuller
 *
 * A typed parameter to pass to a query
 */
public interface InParameter {

	/**
	 * The JDBC data type.
	 */
	public int getType();

	/**
	 * The object holding the data value.
	 */
	public Object getValue();
}