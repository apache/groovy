/*
  $Id$

   Copyright (c) 1999, 2004 Steven John Metsker. All Rights Reserved.

   Steve Metsker makes no representations or warranties about
   the fitness of this software for any particular purpose,
   including the implied warranty of merchantability.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.javanicus.bpwj;

import java.util.*;

import sjm.parse.*;

/**
 * A LoggingTrackSequence is a sequence that throws a <code>
 * TrackSequenceException</code> if the sequence begins but
 * does not complete.  It will output the parse tree as
 * XML in a form suitable for viewing in freemind.sf.net
 * 
 * @author Jeremy Rayner
 */


public class LoggingTrackSequence extends TrackSequence {
/**
 * Constructs a LoggingTrackSequence with the given name.
 *
 * @param    name    a name to be known by
 */
public LoggingTrackSequence(String name) {
	super(name);
}
/**
 * Given a collection of assemblies, this method matches 
 * this track against all of them, and returns a new 
 * collection of the assemblies that result from the 
 * matches.
 *
 * If the match begins but does not complete, this method
 * throws a <code>TrackSequenceException</code>.
 *
 * @return   a Vector of assemblies that result from matching
 *           against a beginning set of assemblies
 *
 * @param in a vector of assemblies to match against
 *
 */
public Vector match(Vector in) {

    // This println is useful for examining left recursion during a parse

    String inString = prettyPrint(best(in));
    if (!inString.equals(LoggingJavaParser.lastIn)) {
        LoggingJavaParser.out.println("<node TEXT='" + inString + "' POSITION='right'/>");
        LoggingJavaParser.lastIn = inString;
    }

    name = name.replaceAll("<","&lt;");
    name = name.replaceAll(">","&gt;");
    String foldNode = "";
    if (LoggingJavaParser.level == 4 || LoggingJavaParser.level == 5 ) {
        foldNode = " FOLDED='true' ";
    }
    LoggingJavaParser.out.println("<node TEXT='" + name + "' COLOR=\"#0000cc\" POSITION='right'" + foldNode + ">");
    LoggingJavaParser.level++;

    //System.out.println("START " + name + "  " + prettyPrint(best(in)));

    boolean inTrack = false;
	Vector last = in;
	Vector out = in;
	Enumeration e = subparsers.elements();
	while (e.hasMoreElements()) {
		Parser p = (Parser) e.nextElement();
		try {
            out = p.matchAndAssemble(last);
        } catch (RuntimeException ex) {
            // close all open <node> elements on way out
            LoggingJavaParser.out.println("<node TEXT='" + ex.getClass() + "' COLOR=\"#ff0000\" POSITION='right'/>");
            throw ex;
        }
		if (out.isEmpty()) {
            LoggingJavaParser.level--;

			LoggingJavaParser.out.println("</node>");
            if (inTrack) {
				throwTrackSequenceException(last, p);
			}
			return out;
		}
		inTrack = true;
		last = out;
	}
    String outString = prettyPrint(best(out));
    if (!outString.equals(LoggingJavaParser.lastOut)) {
        LoggingJavaParser.out.println("<node TEXT='" + outString + "' POSITION='right'/>");
        LoggingJavaParser.lastOut = outString;
    }
    LoggingJavaParser.level--;
    LoggingJavaParser.out.println("</node>");
    //System.out.println("END " + name + "  " + prettyPrint(best(out)));
    //System.out.println();
	return out;
}

private String prettyPrint(Assembly a) {
    Stack s = a.getStack();
    Enumeration e = s.elements();
    StringBuffer sb = new StringBuffer();
    while (e.hasMoreElements()) {
        Object i = e.nextElement();
        sb.append(i);
        if (e.hasMoreElements()) { sb.append(' ');}
    }
    return sb.toString();
}
}
