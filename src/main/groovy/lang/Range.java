/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package groovy.lang;

import java.util.List;

/**
 * Represents the interface of a Range implementation which includes the
 * from and to values.
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public interface Range extends List {
    /**
     * Gets the lower value in the range.
     *
     * @return the lower value in the range.
     */
    Comparable getFrom();

    /**
     * Gets the lower value in the range.
     *
     * @return the upper value in the range
     */
    Comparable getTo();

    /**
     * Indicates whether this is a reverse range which iterates backwards
     * starting from the to value and ending on the from value
     *
     * @return <code>true</code> if this is a reverse range
     */
    boolean isReverse();

    /**
     * Indicates whether this is a reverse range which iterates backwards
     * starting from the to value and ending on the from value
     *
     * @return <code>true</code> if the object is between the from and to values
     */
    boolean containsWithinBounds(Object o);

    /**
     * Steps through the range, calling a closure for each number.
     *
     * @param step    the amount by which to step. If negative, steps through the
     *                range backwards.
     * @param closure the {@link Closure} to call
     */
    public void step(int step, Closure closure);

    /**
     * Forms a list by stepping through the range by the indicated interval.
     *
     * @param step the amount by which to step. If negative, steps through the
     *             range backwards.
     * @return the list formed by stepping through the range by the indicated
     *         interval.
     */
    public List step(int step);

    /**
     * @return the verbose {@link String} representation of this {@link Range} as would be typed into a console
     *         to create the {@link Range} instance
     */
    String inspect();
}
