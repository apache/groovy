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
 
/**
 * Signals that a parser could not match text after
 * a specific point.
 */
public class TrackSequenceException extends RuntimeException {
	protected String after, expected, found;

/**
 * Constructs a <code>TrackSequenceException</code> with the
 * specified reasons for the exception.
 *
 * @param   after   an indication of what text was parsed
 *
 * @param   expected   an indication of what kind of thing 
 *                     was expected, such as a ')' token        
 *
 * @param   found   the text the thrower actually found
 */
public TrackSequenceException(
	String after, String expected, String found) {
		
	super("After   : " + after +
		"\nExpected: " + expected +
		"\nFound   : " + found);
	this.after = after;
	this.found = found;
	this.expected = expected;
}
/**
 * Returns some indication of what text was interpretable.
 *
 * @return   some indication of what text was interpretable
 */
public String getAfter() {
	return after;
}
/**
 * Returns some indication of what kind of thing was 
 * expected, such as a ')' token.
 *
 * @return   some indication of what kind of thing was 
 *           expected, such as a ')' token
 */
public String getExpected() {
	return expected;
}
/**
 * Returns the text element the thrower actually found when 
 * it expected something else.
 *
 * @return   the text element the thrower actually found 
 *           when it expected something else
 */
public String getFound() {
	return found;
}
}
