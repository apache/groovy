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
package groovy.sql;

import groovy.lang.GroovyObjectSupport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.groovy.runtime.NoSuchPropertyException;

/**
 * Represents an extent of objects
 * 
 * @author Chris Stevenson
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class GroovyResultSet extends GroovyObjectSupport {

    private ResultSet resultSet;
    private boolean updated;

    public GroovyResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    public Object getProperty(String property) {
        try {
            return resultSet.getObject(property);
        }
        catch (SQLException e) {
            throw new NoSuchPropertyException(property, GroovyResultSet.class, e);
        }
    }

    public void setProperty(String property, Object newValue) {
        try {
            resultSet.updateObject(property, newValue);
            updated = true;
        }
        catch (SQLException e) {
            throw new NoSuchPropertyException(property, GroovyResultSet.class, e);
        }
    }

    /**
     * Iterates to the next row
     */
    public boolean next() throws SQLException {
        if (updated) {
            resultSet.updateRow();
            updated = false;
        }
        return resultSet.next();
    }

    /**
     * Adds a new row to this result set
     * @param values
     */
    public void add(Map values) throws SQLException {
        resultSet.moveToInsertRow(); 
        for (Iterator iter = values.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            resultSet.updateObject(entry.getKey().toString(), entry.getValue());
        }
        resultSet.insertRow();
    }
}
