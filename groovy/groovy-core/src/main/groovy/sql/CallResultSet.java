
package groovy.sql;


import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author rfuller
 *
 * Represents a ResultSet retrieved as a callable statement out parameter.
 */
class CallResultSet extends GroovyResultSetExtension {
	int indx;
	CallableStatement call;
	ResultSet resultSet;
	boolean firstCall = true;
	
	CallResultSet(CallableStatement call, int indx){
        super(null);
		this.call = call;
		this.indx = indx;
	}
	
	protected ResultSet getResultSet() throws SQLException{
		if(firstCall){
		    resultSet = (ResultSet) call.getObject(indx+1);
			firstCall = false;
		}
		return resultSet;
	}
    
    protected static GroovyResultSet getImpl(CallableStatement call, int idx) {
        GroovyResultSetProxy proxy = new GroovyResultSetProxy(new CallResultSet(call,idx));
        return proxy.getImpl();
    }
}
