/*
 * Copyright 2004-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.grails.web.pages;

import groovy.lang.Script;

/**
 * NOTE: Based on work done by on the GSP standalone project (https://gsp.dev.java.net/)
 *
 * Base class for a GroovyPage (at the moment there is nothing in here but could be useful for providing utility methods
 * etc.
 *
 * @author Troy Heninger
 * Date: Jan 10, 2004
 *
 */
public abstract class GroovyPage extends Script {

/*	do noething in here for the moment
*/
	/**
	 * Convert from HTML to Unicode text.  This function converts many of the encoded HTML
	 * characters to normal Unicode text.  Example: &amp;lt&semi; to &lt;.
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
} // GroovyPage

