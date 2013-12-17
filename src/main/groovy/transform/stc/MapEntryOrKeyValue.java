/*
 * Copyright 2003-2013 the original author or authors.
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

package groovy.transform.stc;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MapEntryOrKeyValue extends ClosureSignatureHint {
    private final static ClassNode MAPENTRY_TYPE = ClassHelper.make(Map.Entry.class);

    public List<ClassNode[]> getClosureSignatures(final MethodNode node, final String[] options) {
        GenericsType[] genericsTypes = node.getParameters()[0].getOriginType().getGenericsTypes();
        if (genericsTypes==null) {
            // would happen if you have a raw Map type for example
            genericsTypes = new GenericsType[] {
                new GenericsType(ClassHelper.OBJECT_TYPE),
                new GenericsType(ClassHelper.OBJECT_TYPE)
            };
        }
        ClassNode[] firstSig = {genericsTypes[0].getType(), genericsTypes[1].getType()};
        ClassNode mapEntry = MAPENTRY_TYPE.getPlainNodeReference();
        mapEntry.setGenericsTypes(genericsTypes);
        ClassNode[] secondSig = {mapEntry};
        return Arrays.asList(firstSig, secondSig);
    }
}
