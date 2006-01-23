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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.grails.web.taglib.*;
import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * NOTE: Based on work done by on the GSP standalone project (https://gsp.dev.java.net/)
 *
 * Parsing implementation for GSP files
 *
 * @author Troy Heninger
 * @author Graeme Rocher
 * 
 * Date: Jan 10, 2004
 *
 */
public class Parse implements Tokens {
    public static final Log LOG = LogFactory.getLog(Parse.class);

    private static final Pattern PARA_BREAK = Pattern.compile("/p>\\s*<p[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern LINE_BREAK = Pattern.compile("\\r\\n|\\n|\\r");
    private static final Pattern ROW_BREAK = Pattern.compile("((/td>\\s*</tr>\\s*<)?tr[^>]*>\\s*<)?td[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern PARSE_TAG_FIRST_PASS = Pattern.compile("(\\s*(\\S+)\\s*=\\s*[\"]([^\"]*)[\"][\\s|>]{1}){1}");
    private static final Pattern PARSE_TAG_SECOND_PASS = Pattern.compile("(\\s*(\\S+)\\s*=\\s*[']([^']*)['][\\s|>]{1}){1}");

    private Scan scan;
    //private StringBuffer buf;
    private StringWriter sw;
    private GSPWriter out;
    private String className;
    private boolean finalPass = false;
    private int tagIndex;
    private int dynamicTagIndex;
    private Map tagContext;
    private List tagMetaStack = new ArrayList();
    private GrailsTagRegistry tagRegistry = GrailsTagRegistry.getInstance();
    private boolean bufferWhiteSpace ;

    private StringBuffer whiteSpaceBuffer = new StringBuffer();
    private int[] lineNumbers = new int[1000];
    private int currentOutputLine;

    class TagMeta  {
        String name;
        Object instance;
        boolean isDynamic;
        boolean hasAttributes;
    }

    public Parse(String name, InputStream in) throws IOException {
        scan = new Scan(readStream(in));
        makeName(name);
    } // Parse()

    public int[] getLineNumberMatrix() {
        return this.lineNumbers;
    }
    public InputStream parse() {

        sw = new StringWriter();
        out = new GSPWriter(sw);
        page();
        finalPass = true;
        scan.reset();
        page();
//		if (DEBUG) System.out.println(buf);
        InputStream in = new ByteArrayInputStream(sw.toString().getBytes());
        out = null;
        scan = null;
        return in;
    } // parse()

    private void declare(boolean gsp) {
        if (finalPass) return;
        if (LOG.isDebugEnabled()) LOG.debug("parse: declare");
        out.println();
        write(scan.getToken().trim(), gsp);
        out.println();
        out.println();
    } // declare()

    private void direct() {
        if (finalPass) return;
        if (LOG.isDebugEnabled()) LOG.debug("parse: direct");
        String text = scan.getToken();
        text = text.trim();
//		LOG.debug("direct(" + text + ')');
        if (text.startsWith("page ")) directPage(text);
    } // direct()

    private void directPage(String text) {
        text = text.substring(5).trim();
//		LOG.debug("directPage(" + text + ')');
        Pattern pat = Pattern.compile("(\\w+)\\s*=\\s*\"([^\"]*)\"");
        Matcher mat = pat.matcher(text);
        for (int ix = 0;;) {
            if (!mat.find(ix)) return;
            String name = mat.group(1);
            String value = mat.group(2);
            if (name.equals("import")) pageImport(value);
            ix = mat.end();
        }
    } // directPage()

    private void expr() {
        if (!finalPass) return;
        if (LOG.isDebugEnabled()) LOG.debug("parse: expr");

        String text = scan.getToken().trim();
        out.printlnToResponse(GroovyPage.fromHtml(text));
    } // expr()

    private void html() {
        if (!finalPass) return;
        if (LOG.isDebugEnabled()) LOG.debug("parse: html");
        StringBuffer text = new StringBuffer(scan.getToken());
        while (text.length() > 80) {
            int end = 80;
                // don't end a line with a '\'
            while (text.charAt(end - 1) == '\\') end--;
            print(text.subSequence(0, end));
            text.delete(0, end);
        }
        if (text.length() > 0) {
            print(text);
        }
    } // html()

    private void makeName(String uri) {
        String name;
        int slash = uri.lastIndexOf('/');
        if (slash > -1) {
            name = uri.substring(slash + 1);
            uri = uri.substring(0,(uri.length() - 1) - name.length());
            while(uri.endsWith("/")) {
                uri = uri.substring(0,uri.length() -1);
            }
            slash = uri.lastIndexOf('/');
            if(slash > -1) {
                    name = uri.substring(slash + 1) + '_' + name;
            }
        }
        else {
            name = uri;
        }
        StringBuffer buf = new StringBuffer(name.length());
        for (int ix = 0, ixz = name.length(); ix < ixz; ix++) {
            char c = name.charAt(ix);
            if (c < '0' || (c > '9' && c < '@') || (c > 'Z' && c < '_') || (c > '_' && c < 'a') || c > 'z') c = '_';
            else if (ix == 0 && c >= '0' && c <= '9') c = '_';
            buf.append(c);
        }
        className = buf.toString();
    } // makeName()

    private static boolean match(CharSequence pat, CharSequence text, int start) {
        int ix = start, ixz = text.length(), ixy = start + pat.length();
        if (ixz > ixy) ixz = ixy;
        if (pat.length() > ixz - start) return false;
        for (; ix < ixz; ix++) {
            if (Character.toLowerCase(text.charAt(ix)) != Character.toLowerCase(pat.charAt(ix - start))) {
                return false;
            }
        }
        return true;
    } // match()

    private static int match(Pattern pat, CharSequence text, int start) {
        Matcher mat = pat.matcher(text);
        if (mat.find(start) && mat.start() == start) {
            return mat.end();
        }
        return 0;
    } // match()

    private void page() {
        if (LOG.isDebugEnabled()) LOG.debug("parse: page");
        if (finalPass) {
            out.println();
            out.print("class ");
            out.print(className);
            out.println(" extends GroovyPage {");
            out.println("public Object run() {");
        } else {
            out.println("import org.codehaus.groovy.grails.web.pages.GroovyPage");
            out.println("import org.codehaus.groovy.grails.web.taglib.*");
        }
        loop: for (;;) {
            int state = scan.nextToken();
            switch (state) {
                case EOF: break loop;
                case HTML: html(); break;
                case JEXPR: expr(); break;
                case JSCRIPT: script(false); break;
                case JDIRECT: direct(); break;
                case JDECLAR: declare(false); break;
                case GEXPR: expr(); break;
                case GSCRIPT: script(true); break;
                case GDIRECT: direct(); break;
                case GDECLAR: declare(true); break;
                case GSTART_TAG: startTag(); break;
                case GEND_TAG: endTag(); break;
            }
        }
        if (finalPass) {
            out.println("}");
            out.println("}");
        }
    } // page()

    private void endTag() {
        if (!finalPass) return;

       String tagName = scan.getToken().trim();
       if(tagMetaStack.isEmpty())
             throw new GrailsTagException("Found closing Grails tag with no opening ["+tagName+"]");

       TagMeta tm = (TagMeta)tagMetaStack.remove(this.tagMetaStack.size() - 1);
       String lastInStack = tm.name;

       // if the tag name is blank then it has been closed by the start tag ie <tag />
       if(StringUtils.isBlank(tagName))
               tagName = lastInStack;

       if(!lastInStack.equals(tagName)) {
           throw new GrailsTagException("Grails tag ["+tagName+"] was not closed");
       }

       if(tagRegistry.isSyntaxTag(tagName)) {
           if(tm.instance instanceof GroovySyntaxTag) {
               GroovySyntaxTag tag = (GroovySyntaxTag)tm.instance;
               if(tag.isBufferWhiteSpace())
                    bufferWhiteSpace = true;
               tag.doEndTag();
           }
           else {
              throw new GrailsTagException("Grails tag ["+tagName+"] was not closed");
           }
       }
       else {
          out.println("}");
          if(tm.hasAttributes) {
               out.println("invokeTag('"+tagName+"',attrs"+tagIndex+",body"+tagIndex+")");
          }
          else {
               out.println("invokeTag('"+tagName+"',[:],body"+tagIndex+")");
          }
          dynamicTagIndex--;
       }
       tagIndex--;
    }

    private void startTag() {
        if (!finalPass) return;
        tagIndex++;

        String text = scan.getToken().trim();
        String tagName;
        Map attrs = new HashMap();
        if(text.indexOf(' ') > -1) {
               int i = text.indexOf(' ');
               tagName = text.substring(0,i);
               String attrTokens = text.substring(i,text.length());
               attrTokens += '>'; // closing bracket marker

               // do first pass parse which retrieves double quoted attributes
                Matcher m = PARSE_TAG_FIRST_PASS.matcher(attrTokens);
                populateAttributesFromMatcher(m,attrs);

               // do second pass parse which retrieves single quoted attributes
               m = PARSE_TAG_SECOND_PASS.matcher(attrTokens);
               populateAttributesFromMatcher(m,attrs);
        }
        else {
            tagName = text;
        }
        TagMeta tm = new TagMeta();
        tm.name = tagName;
        tm.hasAttributes = !attrs.isEmpty();
        tagMetaStack.add(tm);

        if (tagRegistry.isSyntaxTag(tagName)) {
            if(this.tagContext == null) {
                this.tagContext = new HashMap();
                this.tagContext.put(GroovyPage.OUT,out);
            }
            GroovySyntaxTag tag = (GroovySyntaxTag)tagRegistry.newTag(tagName);
            tag.init(tagContext);
            tag.setAttributes(attrs);
            if(!tag.hasPrecedingContent() && !bufferWhiteSpace) {
                throw new GrailsTagException("Tag ["+tag.getName()+"] cannot have non-whitespace characters directly preceding it.");
            }
            else if(!tag.hasPrecedingContent() && bufferWhiteSpace) {
                whiteSpaceBuffer.delete(0,whiteSpaceBuffer.length());
                bufferWhiteSpace = false;
            } else {
                if(whiteSpaceBuffer.length() > 0) {
                    out.printlnToResponse(whiteSpaceBuffer.toString());
                    whiteSpaceBuffer.delete(0,whiteSpaceBuffer.length());
                }
                bufferWhiteSpace = false;
            }
            tag.doStartTag();
            tm.instance = tag;
        }
        else {
            dynamicTagIndex++;
            if(attrs.size() > 0) {
                out.print("attrs"+tagIndex+" = [");
                for (Iterator i = attrs.keySet().iterator(); i.hasNext();) {
                    String name = (String) i.next();
                    out.print(name);
                    out.print(':');
                    out.print(attrs.get(name));
                    if(i.hasNext())
                        out.print(',');
                    else
                        out.println(']');
                }
            }
            out.println("body"+tagIndex+" = {" );
        }
    }

    private void populateAttributesFromMatcher(Matcher m, Map attrs) {
        while(m.find()) {
            String name = m.group(2);
            String val = m.group(3);
            name = '\"' + name + '\"';
            if(val.startsWith("${") && val.endsWith("}")) {
                val = val.substring(2,val.length() -1);
            }
            else {
                val = '\"' + val + '\"';
            }
             attrs.put(name,val);
        }
    }

    private void pageImport(String value) {
//		LOG.debug("pageImport(" + value + ')');
        String[] imports = Pattern.compile(";").split(value.subSequence(0, value.length()));
        for (int ix = 0; ix < imports.length; ix++) {
            out.print("import ");
            out.print(imports[ix]);
            out.println();
        }
    } // pageImport()

    private void print(CharSequence text) {
        StringBuffer buf = new StringBuffer();
        if(Pattern.compile("\\S").matcher(text).find())
            bufferWhiteSpace = false;

        Matcher m = LINE_BREAK.matcher(text);
        while(m.find()) {
            incrementLineNumber();
        }

        buf.append('\'');
        for (int ix = 0, ixz = text.length(); ix < ixz; ix++) {
            char c = text.charAt(ix);
            String rep = null;
            if (c == '\n') rep = "\\n";
            else if (c == '\r') rep = "\\r";
            else if (c == '\t') rep = "\\t";
            else if (c == '\'') rep = "\\'";
            else if (c == '\\') rep = "\\\\";
            if (rep != null) buf.append(rep);
            else buf.append(c);
        }
        buf.append('\'');
        if(!bufferWhiteSpace) {
           out.printlnToResponse(buf.toString());
        }
        else {
            whiteSpaceBuffer.append(buf.toString());
            whiteSpaceBuffer.delete(0,whiteSpaceBuffer.length());
        }

    } // print()

    private String readStream(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            byte[] buf = new byte[8192];
            for (;;) {
                int read = in.read(buf);
                if (read <= 0) break;
                out.write(buf, 0, read);
            }
            return out.toString();
        } finally {
            out.close();
            in.close();
        }
    } // readStream()

    private void script(boolean gsp) {
        if (!finalPass) return;
        if (LOG.isDebugEnabled()) LOG.debug("parse: script");
        out.println();
        write(scan.getToken().trim(), gsp);
        out.println();
        out.println();
    } // script()

    private void write(CharSequence text, boolean gsp) {
        if (!gsp) {
            out.print(text);
            return;
        }
        for (int ix = 0, ixz = text.length(); ix < ixz; ix++) {
            char c = text.charAt(ix);
            String rep = null;
            if (Character.isWhitespace(c)) {
                for (ix++; ix < ixz; ix++) {
                    if (Character.isWhitespace(text.charAt(ix))) continue;
                    ix--;
                    rep = " ";
                    break;
                }
            } else if (c == '&') {
                if (match("&semi;", text, ix)) {
                    rep = ";";
                    ix += 5;
                } else if (match("&amp;", text, ix)) {
                    rep = "&";
                    ix += 4;
                } else if (match("&lt;", text, ix)) {
                    rep = "<";
                    ix += 3;
                } else if (match("&gt;", text, ix)) {
                    rep = ">";
                    ix += 3;
                }
            } else if (c == '<') {
                if (match("<br>", text, ix) || match("<hr>", text, ix)) {
                    rep = "\n";
                    incrementLineNumber();
                    ix += 3;
                } else {
                    int end = match(PARA_BREAK, text, ix);
                    if (end <= 0) end = match(ROW_BREAK, text, ix);
                    if (end > 0) {
                        rep = "\n";
                        incrementLineNumber();
                        ix = end;
                    }
                }
            }
            if (rep != null) out.print(rep);
            else out.print(c);
        }
    } // write()

    private void incrementLineNumber() {
        if(currentOutputLine >= lineNumbers.length) {
            lineNumbers = (int[])resizeArray(lineNumbers, lineNumbers.length * 2);
        }
        else {
            lineNumbers[currentOutputLine++] = out.getCurrentLineNumber();
        }
    }

    private Object resizeArray (Object oldArray, int newSize) {
       int oldSize = java.lang.reflect.Array.getLength(oldArray);
       Class elementType = oldArray.getClass().getComponentType();
       Object newArray = java.lang.reflect.Array.newInstance(
             elementType,newSize);
       int preserveLength = Math.min(oldSize,newSize);
       if (preserveLength > 0)
          System.arraycopy (oldArray,0,newArray,0,preserveLength);
       return newArray;
   }
} // Parse
