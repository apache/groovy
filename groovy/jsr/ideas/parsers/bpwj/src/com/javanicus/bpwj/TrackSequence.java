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
 * A TrackSequence is a sequence that throws a <code>
 * TrackSequenceException</code> if the sequence begins but
 * does not complete.
 * 
 * @author Steven J. Metsker
 */


public class TrackSequence extends Sequence {
/**
 * Constructs a nameless TrackSequence.
 */
public TrackSequence () {
}
/**
 * Constructs a TrackSequence with the given name.
 *
 * @param    name    a name to be known by
 */
public TrackSequence(String name) {
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
    //System.out.println(name);

    boolean inTrack = false;
	Vector last = in;
	Vector out = in;
	Enumeration e = subparsers.elements();
	while (e.hasMoreElements()) {
		Parser p = (Parser) e.nextElement();
		out = p.matchAndAssemble(last);
		if (out.isEmpty()) {
			if (inTrack) {
				throwTrackSequenceException(last, p);
			}
			return out;
		}
		inTrack = true;
		last = out;
	}
	return out;
}
/*
 * Throw an exception showing how far the match had 
 * progressed, what it found next, and what it was 
 * expecting.
 */
protected void throwTrackSequenceException(
	Vector previousState, Parser p) {
		
	Assembly best = best(previousState);
	String after = best.consumed(" ");
	if (after.equals("")) {
		after = "-nothing-";
	}
	
	String expected = p.toString();
	
	Object next = best.peek();
	String found = 
		(next == null) ? "-nothing-" : next.toString();
		
	throw new TrackSequenceException(after, expected, found);
}
}
