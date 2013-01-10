import groovy.transform.stc.MyType
import org.codehaus.groovy.transform.stc.StaticTypesMarker

def myTypeClassNode = classNodeFor(MyType)

// Dummy extension that removes node metadata on LHS if a method is selected and it's 'toUpperCase'
afterVisitMethod { mn ->
    if (isAnnotatedBy(mn, MyType)) { // using MyType instead of myTypeClassNode to increase code coverage
        mn.putNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE, mn.getAnnotations(myTypeClassNode)[0].getMember('value').type)
    }
}