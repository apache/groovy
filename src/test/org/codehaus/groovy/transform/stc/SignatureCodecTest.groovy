package org.codehaus.groovy.transform.stc

import static org.codehaus.groovy.ast.ClassHelper.*
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.GenericsType
import org.codehaus.groovy.ast.tools.WideningCategories

/**
 * Unit tests for signature codecs.
 *
 * @author Cedric Champeau
 */
class SignatureCodecTest extends GroovyTestCase {
    SignatureCodec codec

    @Override
    protected void setUp() {
        super.setUp()
        codec = StaticTypeCheckingVisitor.SignatureCodecFactory.getCodec(1,this.class.classLoader)
    }

    void testVariousSimpleClassNodes() {
        [OBJECT_TYPE, STRING_TYPE, int_TYPE, float_TYPE, double_TYPE, GSTRING_TYPE, Number_TYPE,
        MAP_TYPE, PATTERN_TYPE, SCRIPT_TYPE, LIST_TYPE, RANGE_TYPE].each {
            String signature = codec.encode(it)
            assert codec.decode(signature) == it
        }
    }
    
    void testCodecWithGenericType() {
        ClassNode cn = LIST_TYPE.getPlainNodeReference()
        cn.genericsTypes = [ new GenericsType(STRING_TYPE) ] as GenericsType[]
        String signature = codec.encode(cn)
        ClassNode decoded = codec.decode(signature)
        assert decoded == cn
        assert decoded.genericsTypes[0].type == STRING_TYPE
    }
    
    void testCodecWithUnionType() {
        ClassNode cn = new UnionTypeClassNode(STRING_TYPE, LIST_TYPE)
        String signature = codec.encode(cn)
        ClassNode decoded = codec.decode(signature)
        assert decoded == cn
    }

    void testCodecWithLUBoundType() {
        ClassNode cn = new WideningCategories.LowestUpperBoundClassNode("foo", LIST_TYPE, make(Comparable))
        String signature = codec.encode(cn)
        ClassNode decoded = codec.decode(signature)
        assert decoded == cn
    }
    
    void testCodecWithUnionTypeAndGenerics() {
        ClassNode list = LIST_TYPE.getPlainNodeReference()
        list.genericsTypes = [ new GenericsType(STRING_TYPE) ] as GenericsType[]
        ClassNode cn = new UnionTypeClassNode(STRING_TYPE, list)
        String signature = codec.encode(cn)
        ClassNode decoded = codec.decode(signature)
        assert decoded == cn
        assert decoded.delegates[1] == LIST_TYPE
        assert decoded.delegates[1].genericsTypes[0].type == STRING_TYPE
        
    }
}
