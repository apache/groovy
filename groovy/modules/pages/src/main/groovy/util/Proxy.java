package groovy.util;

import groovy.lang.GroovyObjectSupport;
import groovy.lang.MetaClass;
import groovy.lang.MissingPropertyException;
import groovy.lang.MissingMethodException;
import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * Created by IntelliJ IDEA.
 * User: Troy Heninger
 * Date: Jan 21, 2004
 * Proxy for another object.  All property accesses and method invokations get forwarded to actual object.
 * This class is abstract because it is meant to be subclassed, with new properties and methods provided.
 */
public class Proxy extends GroovyObjectSupport {

	private Object real;
	private MetaClass meta;

	/**
	 * Constructor.  Takes real object to be excapsulated.
	 * @param real
	 */
	public Proxy(Object real) {
		this.real = real;
		this.meta = InvokerHelper.getMetaClass(real);
	} // Proxy()

	/**
	 * Get the property of this proxy, or the real object if property doesn't exist.
	 * @param property
	 * @return
	 */
	public Object getProperty(String property) {
		try {
			return getMetaClass().getProperty(this, property);
		} catch (MissingPropertyException e) {
			return meta.getProperty(real, property);
		}
	} // getProperty()

	/**
	 * Set the property of this proxy, or the real object if property doesn't exist.
	 * @param property
	 * @param newValue
	 */
	public void setProperty(String property, Object newValue) {
		try {
			getMetaClass().setProperty(this, property, newValue);
		} catch (MissingPropertyException e) {
			meta.setProperty(real, property, newValue);
		} catch (MissingMethodException e) {
			meta.setProperty(real, property, newValue);
		}
	} // setProperty()

	/**
	 * Returns the encapsulated object.
	 * @return
	 */
	public Object getRealObject() { return real; }

	/**
	 * Call a method of this proxy, or the real object if method doesn't exist.
	 * @param name
	 * @param args
	 * @return
	 */
	public Object invokeMethod(String name, Object args) {
		try {
			return getMetaClass().invokeMethod(this, name, args);
		} catch (MissingMethodException e) {
			return meta.invokeMethod(this, name, args);
		}
	} // invokeMethod()

} // Proxy
