package org.codehaus.gram.hibernate;

import groovy.lang.Binding;
import org.codehaus.gram.GramSupport;
import org.codehaus.jam.JAnnotation;
import org.codehaus.jam.JProperty;

/**
 * A useful base class for hiberate based Gram scripts
 *
 * @version $Revision$
 */
public abstract class HibernateSupport extends GramSupport {

    public HibernateSupport() {
    }

    public HibernateSupport(Binding binding) {
        super(binding);
    }

    public ColumnInfo getColumnDetails(JProperty property) {
        ColumnInfo answer = new ColumnInfo();
        answer.setCardinality("0..1");
        JAnnotation ann = property.getAnnotation("hibernate.map");
        if (ann != null) {
            answer.setTableName(stringValue(ann, "table"));
            answer.setQualifiedTypeName(annotationValue(property, "hibernate.collection-composite-element", "class"));
            answer.setCardinality("0..N");
            return answer;
        }
        ann = property.getAnnotation("hibernate.set");
        if (ann != null) {
            answer.setTableName(stringValue(ann, "table"));
            answer.setQualifiedTypeName(annotationValue(property, "hibernate.collection-many-to-many", "class"));
            answer.setCardinality("0..N");
            return answer;
        }
        ann = property.getAnnotation("hibernate.bag");
        if (ann != null) {
            answer.setTableName(stringValue(ann, "table"));
            answer.setQualifiedTypeName(annotationValue(property, "hibernate.collection-many-to-many", "class"));
            answer.setCardinality("0..N");
            return answer;
        }
        ann = property.getAnnotation("hibernate.many-to-one");
        if (ann != null) {
            answer.setQualifiedTypeName(stringValue(ann, "class"));
            answer.setForeignKey(stringValue(ann, "foreignKey"));
        }
        ann = property.getAnnotation("hibernate.property");
        if (ann != null) {
            answer.setColumnName(stringValue(ann, "column"));
            answer.setQualifiedTypeName(stringValue(ann, "type"));
            answer.setLength(intValue(ann, "length"));
            answer.setNotNull(booleanValue(ann, "not-null"));
            if (answer.isNotNull()) {
                answer.setCardinality("1..1");
            }
        }
        return answer;
    }


}
