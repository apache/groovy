package org.codehaus.groovy.ast.expr;

import org.codehaus.groovy.ast.ClassNode;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents pattern variable.
 * <p>
 * <pre><code>
 *     // for `someVar instanceof Person(String name, int age) p`,
 *     // the structure of `PatternVariableExpression` is shown as follows:
 *     PatternVariableExpression("p", Person).destructedPatternVariableExpressionList =
 *             [PatternVariableExpression("name", String), PatternVariableExpression("age", int)]
 *
 * </code></pre>
 *
 * @since 5.0.0
 */
public class PatternVariableExpression extends VariableExpression {
    private static final String DUMMY = "<dummy>";
    private final List<PatternVariableExpression> destructedPatternVariableExpressionList = new LinkedList<>();

    public PatternVariableExpression(String name, ClassNode type) {
        super(name, type);
    }

    public PatternVariableExpression(ClassNode type) {
        this(DUMMY, type);
    }

    public boolean isDummy() {
        return DUMMY.equals(this.getName());
    }

    public void addDestructedPatternVariableExpression(PatternVariableExpression destructedPatternVariableExpression) {
        destructedPatternVariableExpressionList.add(destructedPatternVariableExpression);
    }

    public List<PatternVariableExpression> getDestructedPatternVariableExpressionList() {
        return destructedPatternVariableExpressionList;
    }

    @Override
    public String toString() {
        return super.toString() + "[pattern variable: " + this.getName() + (this.isDynamicTyped() ? "" : " type: " + getType())
                + (destructedPatternVariableExpressionList.isEmpty()
                                ? ""
                                : "(destructedPatternVariableExpressionList: " + destructedPatternVariableExpressionList + ")") + "]";
    }
}
