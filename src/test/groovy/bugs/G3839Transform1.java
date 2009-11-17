package groovy.bugs;

import org.objectweb.asm.Opcodes;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.control.*;
import org.codehaus.groovy.transform.*;

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class G3839Transform1 implements ASTTransformation, Opcodes{

    public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        ClassNode classNode = (ClassNode) nodes[1];
        classNode.addField(new FieldNode("f1", ACC_PUBLIC, ClassHelper.OBJECT_TYPE, classNode, null));
    }

}
