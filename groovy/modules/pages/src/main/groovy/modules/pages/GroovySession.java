package groovy.modules.pages;

import groovy.util.Proxy;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * Author: Troy Heninger
 * Date: Jan 13, 2004
 * Proxy for HttpSession.  All calls get forwarded to actual session, but also provides Map
 * functionality to session variables.
 */
public class GroovySession extends Proxy implements Map {
	private Map map = Collections.synchronizedMap(new HashMap());

	/**
	 * Constructor, takes request, extracts and encapsulates the session.
	 * @param request
	 */
	GroovySession(HttpServletRequest request) {
		super(request.getSession(true));
		HttpSession session = getSession();
		Enumeration en = session.getAttributeNames();
		while (en.hasMoreElements()) {
			String name = (String)en.nextElement();
			map.put(name, session.getAttribute(name));
		}
	} // GroovySession()

	/**
	 * Clear all variables.
	 */
	public void clear() {
		map.clear();
		Collection names = new HashSet();
		HttpSession session = getSession();
		Enumeration en = session.getAttributeNames();
		while (en.hasMoreElements()) {
			names.add(en.nextElement());
		}
		Iterator it = names.iterator();
		while (it.hasNext()) {
			remove(it.next());
		}
	} // clear()

	/**
	 * Return true if the variables is set.
	 * @param key
	 * @return
	 */
	public boolean containsKey(Object key) { return map.containsKey(key); }

	/**
	 * Return true if value is in one of the variables.
	 * @param value
	 * @return
	 */
	public boolean containsValue(Object value) { return map.containsValue(value); }

	/**
	 * Return the complete set of variables.
	 * @return
	 */
	public Set entrySet() { return map.entrySet(); }

	/**
	 * Get the variable.
	 * @param key
	 * @return
	 */
	public Object get(Object key) { return map.get(key); }

	/**
	 * Return the real session object.
	 * @return
	 */
	public HttpSession getSession() { return (HttpSession)getRealObject(); }

	/**
	 * Return true if no variables have been set.
	 * @return
	 */
	public boolean isEmpty() { return map.isEmpty(); }

	/**
	 * Return the complete set of variable names.
	 * @return
	 */
	public Set keySet() { return map.keySet(); }

	/**
	 * Set the variable, or clear it if value = null.
	 * @param key
	 * @param value
	 * @return
	 */
	public Object put(Object key, Object value) {
		getSession().setAttribute(String.valueOf(key), value);
		if (value == null) return map.remove(key);
		else return map.put(key, value);
	} // put()

	/**
	 * Copy all members of t in.
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
	 * Remove the variable.
	 * @param key
	 * @return
	 */
	public Object remove(Object key) {
		return put(key, null);
	}

	/**
	 * Return the number of variables.
	 * @return
	 */
	public int size() { return map.size(); }

	/**
	 * Return the complete collection of values.
	 * @return the values
	 */
	public Collection values() { return map.values(); }
	
} // GroovySession
