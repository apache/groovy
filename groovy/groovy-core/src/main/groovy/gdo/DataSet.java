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
package groovy.gdo;

import groovy.lang.Closure;

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.stmt.Statement;

/**
 * Represents an extent of objects
 * 
 * @author Chris Stevenson
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class DataSet {

    private Class type;
    private Closure where;
    private DataSet parent;

    public DataSet(Class type) {
        this.type = type;
    }
    
    public DataSet(DataSet parent, Closure where) {
        this(parent.type);
        this.parent = parent;
        this.where = where;
    }
    
    public DataSet findAll(Closure where) {
        return new DataSet(this, where);
    }
    
    public String getSql() {
        String typeName = type.getName();
        int idx = typeName.lastIndexOf('.');
        if (idx > 0) {
            typeName = typeName.substring(idx + 1);
        }
        String sql = "select * from " + typeName.toLowerCase();
        if (where != null) {
            sql += " where ";
            if (parent != null && parent.where != null)  {
                sql += parent.getWhereSql() + " and ";
            }
            sql += getWhereSql();
        }
        return sql;
    }

    private String getWhereSql() {
        MethodNode method = where.getMetaClass().getClassNode().getMethod("doCall");
        Statement statement = method.getCode();
        SqlWhereVisitor visitor = new SqlWhereVisitor();
        statement.visit(visitor);
        return visitor.getWhere();
    }
}
