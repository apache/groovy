/*
 * Created on Jan 4, 2004
 */
package uk.co.wilson.net.xmlrpc;

/*
 Copyright (c) 2004 John Wilson (tug@wilson.co.uk).
 All rights reserved.
 Redistribution and use in source and binary forms,
 with or without modification, are permitted provided
 that the following conditions are met:

 Redistributions of source code must retain the above
 copyright notice, this list of conditions and the
 following disclaimer.

 Redistributions in binary form must reproduce the
 above copyright notice, this list of conditions and
 the following disclaimer in the documentation and/or
 other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY JOHN WILSON ``AS IS'' AND ANY
 EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JOHN WILSON
 BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE
 */

import groovy.lang.GString;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.AttributeList;
import org.xml.sax.SAXException;

import uk.co.wilson.xml.MinML;



public class XMLRPCMessageProcessor extends MinML {
	private static final byte[] translateTable = (
			//
			"\u0042\u0042\u0042\u0042\u0042\u0042\u0042\u0042"
			//                    \t    \n                \r
			+ "\u0042\u0042\u0041\u0041\u0042\u0042\u0041\u0042"
			//
			+ "\u0042\u0042\u0042\u0042\u0042\u0042\u0042\u0042"
			//
			+ "\u0042\u0042\u0042\u0042\u0042\u0042\u0042\u0042"
			//        sp    !     "     #     $     %     &     '
			+ "\u0041\u0042\u0042\u0042\u0042\u0042\u0042\u0042"
			//         (    )     *     +     ,     -     .     /
			+ "\u0042\u0042\u0042\u003E\u0042\u0042\u0042\u003F"
			//         0    1     2     3     4     5     6     7
			+ "\u0034\u0035\u0036\u0037\u0038\u0039\u003A\u003B"
			//         8    9     :     ;     <     =     >     ?
			+ "\u003C\u003D\u0042\u0042\u0042\u0040\u0042\u0042"
			//         @    A     B     C     D     E     F     G
			+ "\u0042\u0000\u0001\u0002\u0003\u0004\u0005\u0006"
			//         H    I   J K   L     M   N   O
			+ "\u0007\u0008\t\n\u000B\u000C\r\u000E"
			//         P    Q     R     S     T     U     V    W
			+ "\u000F\u0010\u0011\u0012\u0013\u0014\u0015\u0016"
			//         X    Y     Z     [     \     ]     ^    _
			+ "\u0017\u0018\u0019\u0042\u0042\u0042\u0042\u0042"
			//         '    a     b     c     d     e     f     g
			+ "\u0042\u001A\u001B\u001C\u001D\u001E\u001F\u0020"
			//        h   i   j     k     l     m     n     o    p
			+ "\u0021\"\u0023\u0024\u0025\u0026\u0027\u0028"
			//        p     q     r     s     t     u     v     w
			+ "\u0029\u002A\u002B\u002C\u002D\u002E\u002F\u0030"
			//        x     y     z
			+ "\u0031\u0032\u0033").getBytes();
	
	private interface Emitter {
		void emit(StringBuffer buffer, Object value) throws SAXException;
	}
	
	private final static Map elements = new HashMap();
	static {
		final char[] tTable = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".toCharArray();
		
		elements.put(Integer.class,
				new Emitter() {
					public void emit(final StringBuffer buffer, final Object integer) {
						buffer.append("<value><i4>").append(integer).append("</i4></value>");
					}
				});
		elements.put(Character.class, elements.get(Integer.class));
		elements.put(Byte.class, elements.get(Integer.class));
		elements.put(Short.class, elements.get(Integer.class));
		
		elements.put(Double.class,
				new Emitter() {
					public void emit(final StringBuffer buffer, final Object number) {
						buffer.append("<value><double>").append(number).append("</double></value>");
					}
				});
		elements.put(Float.class, elements.get(Double.class));
		elements.put(BigDecimal.class, elements.get(Double.class));
		
		elements.put(String.class,
				new Emitter() {
					public void emit(final StringBuffer buffer, final Object string) throws SAXException {
						encodeString(buffer.append("<value><string>"), string.toString()).append("</string></value>");
					}
				});
		elements.put(GString.class, elements.get(String.class));
		
		elements.put(Boolean.class,
				new Emitter() {
					public void emit(final StringBuffer buffer, final Object bool) {
						buffer.append("<value><boolean>").append((((Boolean)bool).booleanValue()) ? "1" : "0").append("</boolean></value>");
					}
				});
		
		elements.put(byte [].class,
				new Emitter() {
					public void emit(final StringBuffer buffer, final Object bytes) {
						int charCount = 0;
						final byte[] data = (byte[])bytes;
						final int numChars = ((data.length + 2) / 3) * 4;
						final int dLimit = (data.length / 3) * 3;
						
						buffer.ensureCapacity(numChars + 128 + (data.length / 54));
						buffer.append("<value><base64>\n");

						for (int dIndex = 0; dIndex != dLimit; dIndex += 3) {
							int d = ((data[dIndex] & 0XFF) << 16) |  ((data[dIndex + 1] & 0XFF) << 8) | (data[dIndex + 2] & 0XFF);

							buffer.append(tTable[d >> 18]);
							buffer.append(tTable[(d >> 12) & 0X3F]);
							buffer.append(tTable[(d >> 6) & 0X3F]);
							buffer.append(tTable[d & 0X3F]);

							if (++charCount == 18) {
								buffer.append('\n');
								charCount = 0;
							}
						}

						if (dLimit != data.length) {
							int d = (data[dLimit] & 0XFF) << 16;

							if (dLimit + 1 != data.length) {
								d |= (data[dLimit + 1] & 0XFF) << 8;
							}

							buffer.append(tTable[d >> 18]);
							buffer.append(tTable[(d >> 12) & 0X3F]);
							buffer.append((dLimit + 1 < data.length) ? tTable[(d >> 6) & 0X3F] : '=');
							buffer.append('=');
						}
						
						buffer.append("\n</base64></value>");
					}
				});
		
		elements.put(Date.class,
				new Emitter() {
					private final DateFormat dateTime = new SimpleDateFormat ("yyyyMMdd'T'HH:mm:ss");
					
					public synchronized void emit(final StringBuffer buffer, final Object date) {
						buffer.append("<value><dateTime.iso8601>").append(this.dateTime.format((Date)date)).append("</dateTime.iso8601></value>");
					}
				});
	}
	
	public static StringBuffer emit(final StringBuffer buffer, final Object param) throws SAXException {
		final Emitter emitter = (Emitter)elements.get(param.getClass());
		
		if (emitter == null) {
			if (param instanceof List) {
				final Iterator iterator = ((List)param).iterator();
				
				buffer.append("<value><array><data>");
				
				while (iterator.hasNext()) {
					emit(buffer, iterator.next());
				}
				
				buffer.append("</data></array></value>");
				
			} else if (param instanceof Map) {
				final Iterator iterator =((Map)param).entrySet().iterator();
				
				buffer.append("<value><struct>");
				
				while (iterator.hasNext()) {
				final Map.Entry entry = (Map.Entry)iterator.next();
					
					emit(encodeString(buffer.append("<member><name>"), (String)entry.getKey()).append("</name>"), entry.getValue()).append("</member>");
				}
				
				buffer.append("</struct></value>");
			} else if (param instanceof GString) {	 // GString can be subclassed so the initial elements.get() can fail
				((Emitter)elements.get(GString.class)).emit(buffer, param);
			} else if (param instanceof BigDecimal) {	 // BigDecimal can be subclassed so the initial elements.get() can fail
				((Emitter)elements.get(BigDecimal.class)).emit(buffer, param);
			} else if (param instanceof Date) {	 // Date can be subclassed so the initial elements.get() can fail
				((Emitter)elements.get(Date.class)).emit(buffer, param);
			} else {
				throw new SAXException(param.getClass() + " is not a supported XML-RPC data type");
			}
		} else {
			emitter.emit(buffer, param);
		}
		
		return buffer;
	}
	
	public static StringBuffer encodeString(final StringBuffer buffer, final String string) throws SAXException {
		for (int i = 0; i != string.length(); i++) {
			final char c = string.charAt(i);
			
			if (c >= 0X20 || c == 0X09 || c == 0X0A || c == 0X0D) {
				if (c == '>') {
					buffer.append("&gt;");
				} else if (c == '<') {
					buffer.append("&lt;");
				} else if (c == '&') {
					buffer.append("&amp;");
				} else if (c > 0xff) {
					if (c > 0XD800 && !(c >= 0XE000 && c < 0XFFFE)) 
						throw new SAXException("Can't include character with value 0x"
											   + Integer.toHexString(c)
											   + " in a well formed XML document");
					

					buffer.append("&#x").append(Integer.toHexString(c)).append(';');
				} else {
					buffer.append(c);
				}
			} else {
				throw new SAXException("Can't include character with value 0x"
						               + Integer.toHexString(c)
						               + " in a well formed XML document");
			}
		}
		
		return buffer;
	}
	
	/*
	 * A really cheap and cheerful XML-RPC reponse parser 
	 * It only really cares if the reponse contains a <param> or <fault>
	 * element. When the server is implemented this will be redone to be lots pickier.
	 */
	
	
	private Object params = null;
	private Object name = null;
	private String methodName = null;
	private Map struct = null;
	private List array = null;
	private Boolean inArray = Boolean.FALSE;
	private Stack aggregateStack = new Stack();
	private boolean gotValue = false;
	private final StringBuffer buffer = new StringBuffer();
	private final DateFormat dateTime = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");
	private final DateFormat dateTime1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	private final Boolean bools[] = {new Boolean(false), new Boolean(true)};
	
	public void parseMessage(final InputStream in) throws SAXException, IOException {
		parse(new InputStreamReader(in, "ISO-8859-1"));
	}
	
	public List getParams() {
		return (List)this.params;
	}
	
	public String getMethodname() {
		return this.methodName;
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.DocumentHandler#startElement(java.lang.String, org.xml.sax.AttributeList)
	 */
	public void startElement(final String name, final AttributeList attributes) {
		if ("value".equals(name) || "name".equals(name) || "methodName".equals(name)) {
			this.buffer.setLength(0);
			this.gotValue = false;
			this.aggregateStack.push(this.inArray);
			this.inArray = Boolean.FALSE;
		} else if ("struct".equals(name)) {
			this.aggregateStack.push(this.struct);
			this.aggregateStack.push(this.name);
			this.struct = new HashMap();
		} else if ("array".equals(name) || "params".equals(name)) {
			this.aggregateStack.push(this.inArray);
			this.inArray = Boolean.TRUE;
			this.aggregateStack.push(this.array);
			this.array = new ArrayList();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.DocumentHandler#characters(char[], int, int)
	 */
	public void characters(final char[] ch, final int start, final int length) {
		this.buffer.append(ch, start, length);
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.DocumentHandler#endElement(java.lang.String)
	 */
	public void endElement(final String name) throws SAXException {
		if ("value".equals(name)) {
			if (!this.gotValue) {
				this.params = this.buffer.toString();
				this.gotValue = true;
			}
			
			this.inArray = (Boolean)this.aggregateStack.pop();
			
			if (this.inArray.booleanValue())
				this.array.add(this.params);
		} else if ("string".equals(name)) {
			this.params = this.buffer.toString();
			this.gotValue = true;
		} else if ("i4".equals(name) || "int".equals(name)) {
			this.params = new Integer(this.buffer.toString());
			this.gotValue = true;
		} else if ("boolean".equals(name)) {
			try {
				this.params = this.bools[Integer.parseInt(this.buffer.toString())];
			}
			catch (final RuntimeException e) {
				throw new SAXException("bad Boolean value: " + this.buffer.toString());
			}
			
			this.gotValue = true;
		} else if ("dateTime.iso8601".equals(name)) {
			try {
				synchronized (this) {
					this.params = this.dateTime.parse(this.buffer.toString());
				}
			} catch (final ParseException e) {
				//
				// Some implementations produce funny formats 
				// Try an alternate format
				//
				try {
					synchronized (this) {
						this.params = this.dateTime1.parse(this.buffer.toString());
					}
				} catch (final ParseException e1) {
					throw new SAXException(e);	// throw the original exception
				}
			}
			this.gotValue = true;
		} else if ("base64".equals(name)) {
			int bytesIndex = 0;
			int byteShift = 4;
			int tmp = 0;
			boolean done = false;
			
			for (int i = 0; i != this.buffer.length(); i++) {
			final char c = this.buffer.charAt(i);
			final int sixBit = (c < 123) ? translateTable[c] : 66;

				if (sixBit < 64) {
					if (done) throw new SAXException("= character not at end of base64 value");
	
					tmp = (tmp << 6) | sixBit;
	
					if (byteShift-- != 4) {
						this.buffer.setCharAt(bytesIndex++, (char)((tmp >> (byteShift * 2)) & 0XFF));
					}
	
				} else if (sixBit == 64) {
	
					byteShift--;
					done = true;
	
				} else if (sixBit == 66) {
					// RFC 2045 says that I'm allowed to take the presence of 
					// these characters as evedence of data corruption
					// So I will
					throw new SAXException("bad character in base64 value");
				}

				if (byteShift == 0) byteShift = 4;
			}
			this.buffer.setLength(bytesIndex);
			try {
				this.params = this.buffer.toString().getBytes("ISO-8859-1");
			} catch (UnsupportedEncodingException e) {
				throw new SAXException("Base 64 decode produced byte values > 255");
			}
			this.gotValue = true;
		} else if ("double".equals(name)) {
			this.params = new java.math.BigDecimal(this.buffer.toString());
			this.gotValue = true;
		} else if ("name".equals(name)) {
			this.name = this.buffer.toString();
			this.inArray = (Boolean)this.aggregateStack.pop();
		} else if ("member".equals(name)) {
			this.struct.put(this.name, this.params);
		} else if ("struct".equals(name)) {
			this.params = this.struct;
			this.name = (String)this.aggregateStack.pop();
			this.struct = (Map)this.aggregateStack.pop();
		} else if ("array".equals(name) || "params".equals(name)) {
			this.params = this.array;
			this.array = (List)this.aggregateStack.pop();
			this.inArray = (Boolean)this.aggregateStack.pop();
		} else if ("methodName".equals(name)) {
			this.methodName = this.buffer.toString();
			this.inArray = (Boolean)this.aggregateStack.pop();
		} else if ("fault".equals(name)) {
			throw new RuntimeException("XML-RPC call Failure: " +
					                   ((Map)this.params).get("faultString") +
					                   ", fault code = " +
					                   ((Map)this.params).get("faultCode"));
		}
	}
}