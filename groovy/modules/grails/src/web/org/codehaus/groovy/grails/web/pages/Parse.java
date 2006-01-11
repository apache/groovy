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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * NOTE: Based on work done by on the GSP standalone project (https://gsp.dev.java.net/)
 *
 * Parsing implementation for GSP files
 *
 * @author Troy Heninger
 * Date: Jan 10, 2004
 *
 */
public class Parse implements Tokens {
    public static final boolean DEBUG = false;

    private static final Pattern paraBreak = Pattern.compile("/p>\\s*<p[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern rowBreak = Pattern.compile("((/td>\\s*</tr>\\s*<)?tr[^>]*>\\s*<)?td[^>]*>", Pattern.CASE_INSENSITIVE);

    private Scan scan;
    private StringBuffer buf;
    private String className;
    private boolean finalPass = false;
    private int tagIndex;

    public Parse(String name, InputStream in) throws IOException {
        scan = new Scan(readStream(in));
        makeName(name);
    } // Parse()

    public InputStream parse() {
        buf = new StringBuffer();
        page();
        finalPass = true;
        scan.reset();
        page();
//		if (DEBUG) System.out.println(buf);
        InputStream out = new ByteArrayInputStream(buf.toString().getBytes());
        buf = null;
        scan = null;
        return out;
    } // parse()

    private void declare(boolean gsp) {
        if (finalPass) return;
        if (DEBUG) System.out.println("parse: declare");
        buf.append("\n");
        write(scan.getToken().trim(), gsp);
        buf.append("\n\n");
    } // declare()

    private void direct() {
        if (finalPass) return;
        if (DEBUG) System.out.println("parse: direct");
        String text = scan.getToken();
        text = text.trim();
//		System.out.println("direct(" + text + ')');
        if (text.startsWith("page ")) directPage(text);
    } // direct()

    private void directPage(String text) {
        text = text.substring(5).trim();
//		System.out.println("directPage(" + text + ')');
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
        if (DEBUG) System.out.println("parse: expr");
        buf.append("out.print(");
        String text = scan.getToken().trim();
        buf.append(GroovyPage.fromHtml(text));
        buf.append(")\n");
    } // expr()

    private void html() {
        if (!finalPass) return;
        if (DEBUG) System.out.println("parse: html");
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

    private void makeName(String name) {
        int slash = name.lastIndexOf('/');
        if (slash >= 0) name = name.substring(slash + 1);
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
        if (DEBUG) System.out.println("parse: page");
        if (finalPass) {
            buf.append("\nclass ");
            buf.append(className);
			buf.append(" extends GroovyPage {\n");
//            buf.append(" extends Script {\n");  //implements GroovyPage {\n");
            buf.append("public Object run() {\n");
        } else {
            buf.append("import org.codehaus.groovy.grails.web.pages.GroovyPage\n");
            buf.append("import org.codehaus.groovy.grails.web.taglib.*\n");
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
            buf.append("}\n}\n");
//			buf.append("} // run()\n");
        }
    } // page()

    private void endTag() {
        if (!finalPass) return;

       buf.append("tag")
            .append(tagIndex)
            .append(".doEndTag()\n");
    }

    private void startTag() {
        if (!finalPass) return;
        tagIndex++;
        String text = scan.getToken();
        buf.append("tag")
            .append(tagIndex)
            .append("= grailsTagRegistry.loadTag('");
        if(text.indexOf(' ') > -1) {
            String[] tagTokens = text.split( " ");
            String tagName = tagTokens[0];
            buf.append(tagName)
               .append("',request,response,out)\n");

            for (int i = 1; i < tagTokens.length; i++) {
                String[] attr = tagTokens[i].split("=");
                String val = attr[1].substring(1,attr[1].length() - 1);
                buf.append("tag")
                   .append(tagIndex)
                   .append(".setAttribute('")
                   .append(attr[0])
                   .append("', resolveVariable('")
                   .append(val)
                   .append("'))\n");
            }
        } else {
            buf.append(text)
                .append("',request,response,out)\n");
        }



        buf.append("tag")
           .append(tagIndex)
           .append(".doStartTag()")
           .append('\n');
    }

    private void pageImport(String value) {
//		System.out.println("pageImport(" + value + ')');
        String[] imports = Pattern.compile(";").split(value.subSequence(0, value.length()));
        for (int ix = 0; ix < imports.length; ix++) {
            buf.append("import ");
            buf.append(imports[ix]);
            buf.append('\n');
        }
    } // pageImport()

    private void print(CharSequence text) {
        buf.append("out.print('");
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
        buf.append("')\n");
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
        if (DEBUG) System.out.println("parse: script");
        buf.append("\n");
        write(scan.getToken().trim(), gsp);
        buf.append("\n\n");
    } // script()

    private void write(CharSequence text, boolean gsp) {
        if (!gsp) {
            buf.append(text);
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
                    ix += 3;
                } else {
                    int end = match(paraBreak, text, ix);
                    if (end <= 0) end = match(rowBreak, text, ix);
                    if (end > 0) {
                        rep = "\n";
                        ix = end;
                    }
                }
            }
            if (rep != null) buf.append(rep);
            else buf.append(c);
        }
    } // write()

} // Parse
