package com.javanicus.bpwj;

import sjm.parse.*;

import java.util.Vector;
import java.util.Enumeration;

/** purely a wrapper for alternation to provide debug information */
public class LoggingAlternation extends Alternation{
    public LoggingAlternation(String s) {
        super(s);
    }

    public Vector match(Vector in) {

        String inString = prettyPrint(best(in).getStack());
        if (!inString.equals(LoggingJavaParser.lastIn)) {
            LoggingJavaParser.out.println("<node TEXT='" + inString + "' POSITION='right'/>");
            LoggingJavaParser.lastIn = inString;
        }

        String name = this.name;
        if (name == null) name = this.toString();
        name = name.replaceAll("<","&lt;");
        name = name.replaceAll(">","&gt;");
        LoggingJavaParser.out.println("<node TEXT='"+ name +"' COLOR=\"#009900\" POSITION='right'>");
        LoggingJavaParser.level++;

        //System.out.println("START " + name + "  " + prettyPrint(best(in).getStack()));
        Vector out = null;
        try {
            out = super.match(in);
        } catch (RuntimeException ex) {
            LoggingJavaParser.out.println("<node TEXT='" + ex.getClass() + "' COLOR=\"#ff0000\" POSITION='right'/>");
            throw ex;
        } finally {
            LoggingJavaParser.level--;
            LoggingJavaParser.out.println("</node>");
        }
        //todo - outString

        //System.out.println("END " + name);
        //System.out.println();

        return out;
    }

    private String prettyPrint(Vector s) {
        StringBuffer sb = new StringBuffer();
        if (s != null) {
            Enumeration e = s.elements();
            while (e.hasMoreElements()) {
                Object i = e.nextElement();
                sb.append(i);
                if (e.hasMoreElements()) { sb.append(' ');}
            }
        }
        return sb.toString();
    }

}
