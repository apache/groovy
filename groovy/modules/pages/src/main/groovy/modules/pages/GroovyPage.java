package groovy.modules.pages;

import groovy.lang.Script;
import groovy.lang.Binding;

/**
 * Created by IntelliJ IDEA.
 * Author: Troy Heninger
 * Date: Jan 10, 2004
 * Default base class of pages.  Provides access to many useful functions for web page writers.
 * Note: Currently unused due to a bug in Groovy not allowing classes derived indirectly from Script
 * to receive their Binding data.  Also, many functions to be added soon.
 */
public abstract class GroovyPage extends Script {

/*	private final ThreadLocal bindings = new ThreadLocal();

	public Binding getBinding() {
	    return (Binding)bindings.get();
	} // getBinding()

	public void setBinding(Binding binding) {
	    bindings.set(binding);
	} // setBinding()

	public Object getProperty(String property) {
	    return getBinding().getVariable(property);
	}

	public void setProperty(String property, Object newValue) {
	    getBinding().setVariable(property, newValue);
	}
*/
	/**
	 * Defaults to deflt if value is null.
	 */
	public Object ensure(Object value, Object deflt) {
		return value == null ? deflt : value;
	} // ensure()

	/**
	 * Convert from HTML to Unicode text.  This function converts many of the encoded HTML
	 * characters to normal Unicode text.  Example: &amp;lt&semi; to &lt;.  This is the opposite
	 * of showHtml().
	 * @see showHtml(String)
	 */
	public static String fromHtml(String text)
	{
		int ixz;
		if (text == null || (ixz = text.length()) == 0) return text;
		StringBuffer buf = new StringBuffer(ixz);
		String rep = null;
		for (int ix = 0; ix < ixz; ix++)
		{
			char c = text.charAt(ix);
			if (c == '&');
			{
				String sub = text.substring(ix + 1).toLowerCase();
				if (sub.startsWith("lt;"))
				{
					c = '<';
					ix += 3;
				}
				else
				if (sub.startsWith("gt;"))
				{
					c = '>';
					ix += 3;
				}
				else
				if (sub.startsWith("amp;"))
				{
					c = '&';
					ix += 4;
				}
				else
				if (sub.startsWith("nbsp;"))
				{
					c = ' ';
					ix += 5;
				}
				else
				if (sub.startsWith("semi;"))
				{
					c = ';';
					ix += 5;
				}
				else
				if (sub.startsWith("#"))
				{
					char c2 = 0;
					for (int iy = ix + 1; iy < ixz; iy++)
					{
						char c1 = text.charAt(iy);
						if (c1 >= '0' && c1 <= '9')
						{
							c2 = (char)(c2 * 10 + c1);
							continue;
						}
						if (c1 == ';')
						{
							c = c2;
							ix = iy;
						}
						break;
					}
				}
			}
			if (rep != null)
			{
				buf.append(rep);
				rep = null;
			}
			else buf.append(c);
		}
		return buf.toString();
	} // fromHtml()

	/**
	 * Substring Replacer. For each instance of <b>sub</b> found in <b>str</b>, it is replaced
	 * by <b>rep</b>.  The resulting String is returned.
	 */
	public static String replace(String str, String sub, String rep)
	{
		StringBuffer buf = null;
		int lenS = sub.length();
		for (int last = 0;;)
		{
			int ix = str.indexOf(sub, last);
			if (ix < 0)
			{
				if (buf != null)
				{
					buf.append(str.substring(last));
					str = buf.toString();	// return str as result
				}
				break;
			}
			if (buf == null) buf = new StringBuffer(str.length() * 3 / 2);
			buf.append(str.substring(last, ix));
			buf.append(rep);
			last = ix + lenS;
		}
		return str;
	} // replace()

	/**
	 * Substring Replacer. For each instance of <b>sub</b> found in <b>str</b>, it is replaced
	 * by <b>rep</b>.  The buffer argument itself is modifed and returned.  This is faster than
	 * replace(), especially useful when called multiple times for various replacements.
	 */
	public static StringBuffer replaceBuf(StringBuffer buf, String sub, String rep)
	{
		String str = buf.toString();
		int lenS = sub.length();
		int diff = rep.length() - lenS;
		int offset = 0;
		for (int last = 0;;)
		{
			int ix = str.indexOf(sub, last);
			if (ix < 0) break;
			buf.replace(ix + offset, ix + offset + lenS, rep);
			last = ix + lenS;
			offset += diff;
		}
		return buf;
	} // replaceBuf()

} // GroovyPage

