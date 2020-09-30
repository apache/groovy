/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.tools.groovydoc;

import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.groovydoc.GroovyMemberDoc;

public class SimpleGroovyMemberDoc extends SimpleGroovyAbstractableElementDoc implements GroovyMemberDoc {
    protected GroovyClassDoc belongsToClass;

    public SimpleGroovyMemberDoc(String name, GroovyClassDoc belongsToClass) {
        super(name);
        this.belongsToClass = belongsToClass;
    }

    @Override
    public boolean isSynthetic() {/*todo*/
        return false;
    }

    @Override
    public String firstSentenceCommentText() {
        if (super.firstSentenceCommentText() == null) {
            SimpleGroovyClassDoc classDoc = (SimpleGroovyClassDoc) belongsToClass;
            setFirstSentenceCommentText(classDoc.replaceTags(calculateFirstSentence(getRawCommentText())));
        }
        return super.firstSentenceCommentText();
    }

    @Override
    public String commentText() {
        if (super.commentText() == null) {
            SimpleGroovyClassDoc classDoc = (SimpleGroovyClassDoc) belongsToClass;
            setCommentText(classDoc.replaceTags(getRawCommentText()));
        }
        return super.commentText();
    }

}
