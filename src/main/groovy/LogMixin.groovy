package groovy; 
 
import org.apache.commons.logging.LogFactory; 
 
/** A simple mixin to add logging to a class using an instance variable */
class StaticLogMixin { 
	// lets make a logger available 
	static log = LogFactory.getLog(thisClass); 
} 