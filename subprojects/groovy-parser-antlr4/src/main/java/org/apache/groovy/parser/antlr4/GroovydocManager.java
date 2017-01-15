package org.apache.groovy.parser.antlr4;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;

import java.util.List;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.asBoolean;

/**
 * A utilities for managing groovydoc, e.g.
 * 1) extracting groovydoc from groovy AST;
 * 2) TODO extracting tags from groovydoc;
 * 3) attach groovydoc to AST node as metadata
 */
public class GroovydocManager {
    public static final String DOC_COMMENT = "_DOC_COMMENT"; // keys for meta data
    private static final String DOC_COMMENT_PREFIX = "/**";
    private static final String EXTRACT_DOC_COMMENT = "groovy.extract.doc.comment";
    private static final String TRUE_STR = "true";
    private static final boolean EXTRACTING_DOC_COMMENT_ENABLED;
    private AstBuilder astBuilder;

    static {
        boolean edce;
        try {
            edce = TRUE_STR.equals(System.getProperty(EXTRACT_DOC_COMMENT));
        } catch (Exception e) {
            edce = false;
        }

        EXTRACTING_DOC_COMMENT_ENABLED = edce;
    }

    public GroovydocManager(AstBuilder astBuilder) {
        this.astBuilder = astBuilder;
    }

    /**
     * Attach doc comment to member node as meta data
     * <p>
     */
    public void attachDocCommentAsMetaData(ASTNode node, GroovyParser.GroovyParserRuleContext ctx) {
        if (!EXTRACTING_DOC_COMMENT_ENABLED) {
            return;
        }

        if (!asBoolean(node) || !asBoolean(ctx)) {
            return;
        }

        String docCommentNodeText = this.findDocCommentByNode(ctx);

        if (!asBoolean((Object) docCommentNodeText)) {
            return;
        }

        node.putNodeMetaData(DOC_COMMENT, docCommentNodeText);
    }

    private String findDocCommentByNode(ParserRuleContext node) {
        if (!asBoolean(node)) {
            return null;
        }

        if (node instanceof GroovyParser.ClassBodyContext) {
            return null;
        }

        ParserRuleContext parentContext = node.getParent();

        if (!asBoolean(parentContext)) {
            return null;
        }

        String docCommentNodeText = null;
        boolean sameTypeNodeBefore = false;
        for (ParseTree child : parentContext.children) {

            if (node == child) {
                // if no doc comment node found and no siblings of same type before the node,
                // try to find doc comment node of its parent
                if (!asBoolean((Object) docCommentNodeText) && !sameTypeNodeBefore) {
                    return findDocCommentByNode(parentContext);
                }

                return docCommentNodeText;
            }

            if (node.getClass() == child.getClass()) { // e.g. ClassBodyDeclarationContext == ClassBodyDeclarationContext
                docCommentNodeText = null;
                sameTypeNodeBefore = true;
                continue;
            }

            if (!(child instanceof GroovyParser.NlsContext || child instanceof GroovyParser.SepContext)) {
                continue;
            }

            // doc comments are treated as NL
            List<? extends TerminalNode> nlList =
                    child instanceof GroovyParser.NlsContext
                            ? ((GroovyParser.NlsContext) child).NL()
                            : ((GroovyParser.SepContext) child).NL();

            int nlListSize = nlList.size();
            if (0 == nlListSize) {
                continue;
            }

            for (int i = nlListSize - 1; i >= 0; i--) {
                String text = nlList.get(i).getText();

                if (text.matches("\\s+")) {
                    continue;
                }

                if (text.startsWith(DOC_COMMENT_PREFIX)) {
                    docCommentNodeText = text;
                } else {
                    docCommentNodeText = null;
                }

                break;
            }
        }

        throw new GroovyBugError("node can not be found: " + node.getText()); // The exception should never be thrown!
    }
}
