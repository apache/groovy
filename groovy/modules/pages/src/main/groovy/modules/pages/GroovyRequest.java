package groovy.modules.pages;

import groovy.util.Proxy;

import java.util.*;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by IntelliJ IDEA.
 * Author: Troy Heninger
 * Date: Jan 13, 2004
 * Proxy for ServletRequest.  All calls get forwarded to actual request, but also provides Map
 * functionality to request parameters.
 */
public class GroovyRequest extends Proxy implements Map {
	public static final String PREFIX = "groovy.var.";
	public static final int PREFIX_LEN = PREFIX.length();
	public static final Null NULL = new Null();

	private Map map = new HashMap();

	/**
	 * Singleton to represent a nulled out/cleared parameter at runtime.
	 */
	static class Null {
		Null() {}
		public boolean equals(Object obj) {
			return obj instanceof Null;
		}
	} // Null

	/**
	 * Constructor.  All request attributes, parameters, and headers become members.
	 * @param request encapsulated request
	 */
	GroovyRequest(HttpServletRequest request) {
		super(request);
		String name;
		Enumeration en = request.getHeaderNames();
		while (en.hasMoreElements()) {
			name = (String)en.nextElement();
			String header = request.getHeader(name);
			if (!header.equals(NULL)) {
				map.put(name, header);
			}
		}
		en = request.getParameterNames();
		while (en.hasMoreElements()) {
			name = (String)en.nextElement();
			String[] values = request.getParameterValues(name);
			if (values == null) continue;
			if (values.length == 1) map.put(name, values[0]);
			else map.put(name, Arrays.asList(values));
		}
		en = request.getAttributeNames();
		while (en.hasMoreElements()) {
			name = (String)en.nextElement();
			if (name.startsWith(PREFIX)) {
				Object value = request.getAttribute(name);
				if (!value.equals(NULL)) {
					map.put(name.substring(PREFIX_LEN), value);
				}
			}
		}
	} // GroovyRequest()

	/**
	 * Clear all parameters.
	 */
	public void clear() { map.clear(); }

	/**
	 * Returns true if parameter key has or had a value.
	 */
	public boolean containsKey(Object key) { return map.containsKey(key); }

	/**
	 * Returns true if value exists.
	 * @param value
	 * @return
	 */
	public boolean containsValue(Object value) { return map.containsValue(value); }

	/**
	 * Returns the complete set of parameters.
	 * @return
	 */
	public Set entrySet() { return map.entrySet(); }

	/**
	 * Returns the parameter or null.
	 * @param key
	 * @return
	 */
	public Object get(Object key) { return map.get(key); }

	/**
	 * Lookup parameter or bean property.
	 * @param property
	 * @return
	 */
	public Object getProperty(String property) {
		Object result = super.getProperty(property);
		if (result.equals(NULL)) result = null;
		return result;
	} // getProperty()

	/**
	 * Returns the original request.
	 * @return
	 */
	public ServletRequest getRequest() { return (ServletRequest)getRealObject(); }

	/**
	 * Probably never true, because there's always some headers.
	 * @return
	 */
	public boolean isEmpty() { return map.isEmpty(); }

	/**
	 * Returns the complete set of keys.
	 * @return
	 */
	public Set keySet() { return map.keySet(); }

	/**
	 * Change a parameter for later use in the page or for sending to a called page.
	 * @param key
	 * @param value
	 * @return
	 */
	public Object put(Object key, Object value) {
		if (value == null) value = NULL;
		getRequest().setAttribute(PREFIX + key, value);
		if (value.equals(NULL)) return map.remove(key);
		else return map.put(key, value);
	} // put()

	/**
	 * Change all included parameters.
	 * @param t
	 */
	public void putAll(Map t) {
		Iterator it = t.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			put(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Set's the parameter to null.
	 * @param key
	 * @return
	 */
	public Object remove(Object key) { return put(key, null); }

	/**
	 * Returns the number of parameters.
	 * @return
	 */
	public int size() { return map.size(); }

	/**
	 * Returns the complete set of values.
	 * @return
	 */
	public Collection values() { return map.values(); }

} // GroovyRequest
