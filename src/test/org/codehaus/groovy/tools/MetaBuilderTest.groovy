package org.codehaus.groovy.tools

import java.lang.reflect.Modifier
import org.codehaus.groovy.runtime.InvokerHelper

class MetaBuilderTest extends GroovyTestCase {

    void testBuilder () {
        def result = new MetaBuilder([PriceList, PriceListItem, InvoiceItem, Invoice]).build {
            priceList (date : "01/09/2008") {
                  (0..9).each { i ->
                    priceListItem (description:"GINA vol. ${i}", price : i+1, refId:"gina${i}")
                }
            }

            invoices (instanceOf:ArrayList) {
                invoice (date : "01/09/2008") {
                    items {
                        (0..9).each { i ->
                           invoiceItem (number : i, priceListItem : refId("gina${i}"))
                        }
                    }

                    toBePaidBefore = "01/10/2008"
                }
            }
        }

        assertEquals 1, result.size ()
        assertEquals 10, result[0].items.size ()

        result[0].items.each {
            println "${it.description} \$${it.price}"
        }

        assertEquals 10, result.invoices[0].items.size ()
        result.invoices[0].items.each {
            println "${it.number} units of ${it.priceListItem.description} by \$${it.priceListItem.price} each"
        }
    }
}


class PriceList {
    def items, date

    void add (PriceListItem pli) {
        if (items == null)
          items = []
        
        items << pli
    }
}

class PriceListItem {
    String description
    int price
}

class InvoiceItem {
    int number
    def priceListItem
}

class Invoice {
    def date, toBePaidBefore
    def items = new ArrayList ()
}

//------------- IMPLEMENTATION

class MetaBuilder {
    def knownKlasses = [:]
    def refIds = [:]

    MetaBuilder (List klasses) {
        klasses.each { it ->
            knownKlasses [nameForKlass(it)] = it
        }
    }

    def nameForKlass (Class cls) {
        def name = cls.name
        name = name.substring(name.lastIndexOf('.')+1)
        name [0].toLowerCase() + name.substring(1)
    }

    def build (Closure closure) {
        MetaBuilderResult result = new MetaBuilderResult(builder:this, currentObj:new ListWithProperties())
        closure.delegate = result
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure.call ()
        result.currentObj
    }

    def build (Class cls, Closure closure) {
        MetaBuilderResult result = new MetaBuilderResult(builder:this, currentObj:cls.newInstance())
        closure.delegate = result
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure.call ()
        result.currentObj
    }
}

class MetaBuilderResult {
    def currentObj
    MetaBuilder builder

    public Object getProperty(String property) {
        MetaProperty mp = metaClass.getMetaProperty(property)
        if (mp)
          return mp.getProperty(this)
        else
          return currentObj.metaClass.getProperty(property);
    }

    public void setProperty(String property, Object newValue) {
        MetaProperty mp = metaClass.getMetaProperty(property)
        if (mp)
          mp.setProperty(this, newValue)
        else
          currentObj.metaClass.setProperty(property, newValue);
    }

    def invokeMissing(String name, closure, map, refId, instanceOf) {
        if (instanceOf) {
            currentObj.setProperty (name, newInstance(instanceOf, map, closure, refId))
        }
        else {
            def mp = currentObj.metaClass.getMetaProperty(name)
            if (mp) {
               def val = mp.getProperty(currentObj)
               if (val) {
                   initObj(val, map, closure, refId)
               }
               else {
                   Class type = guessType(mp)
                   currentObj.setProperty (name, newInstance(type, map, closure, refId))
               }
            }
            else {
                def type = builder.knownKlasses[name]
                if (!type) {
                    // neither type nor property
                    currentObj.setProperty (name, newInstance(ListWithProperties, map, closure, refId))
                }
                else {
                    currentObj.add(newInstance(type, map, closure, refId))
                }
            }
        }
    }

    private Class guessType(mp) {
        Class type = mp.type
        if (type == Object) {
            type = ListWithProperties
        }
        else {
            if (Modifier.isAbstract(type.modifiers) || type.isInterface()) {
                if (!type.isAssignableFrom(ListWithProperties)) {
                    throw new RuntimeException("Abstract types are not allowed: " + type)
                }
                else
                    type = ListWithProperties
            }
            else {
                if (type.isPrimitive()) {
                    throw new RuntimeException("Primitive types are not allowed: " + type)
                }
            }
        }
        return type
    }

    def newInstance (klass, map, closure, refId) {
        return initObj(klass.newInstance(), refId, closure, map)
    }

    def initObj(newObj, refId, closure, map) {
        if (map)
            InvokerHelper.setProperties(newObj, map)

        if (closure) {
            closure.delegate = new MetaBuilderResult(builder: builder, currentObj: newObj)
            closure.resolveStrategy = Closure.DELEGATE_ONLY
            closure.call()
        }

        if (refId)
            builder.refIds[refId] = newObj

        return newObj
    }


    def refId(key) {
        builder.refIds [key]
    }

    def invokeMethod(String name, Object args) {
        MetaClass mc = currentObj.metaClass
        try {
            return mc.invokeMethod(currentObj, name, args)
        }
        catch (MissingMethodException me) {
            def closure = null, map = null, refId = null, instanceOf = null
            if (args.length > 0 && args [-1] instanceof Closure) {
                closure = args [-1]
                Object [] newArgs = new Object [args.length-1]
                System.arraycopy (args, 0, newArgs, 0, args.length-1)
                args = newArgs
            }

            if (args.length == 1 && args [0] instanceof Map) {
                map = args [0]
                Object [] newArgs = new Object [args.length-1]
                System.arraycopy (args, 1, newArgs, 0, args.length-1)
                args = newArgs

                refId = map['refId']
                if (refId) {
                    map.remove 'refId'
                }

                instanceOf = map['instanceOf']
                if (instanceOf) {
                    map.remove 'instanceOf'
                }
            }

            if (args.length > 0) {
                throw new RuntimeException ("Wrong arguments" + args)
            }

            return invokeMissing(name, closure, map, refId, instanceOf)
        }
    }
}

class ListWithProperties extends ArrayList {
    private def props

    ListWithProperties (Map props = null) {
        props?.each { k, v ->
            setProperty k, v
        }
    }

    public Object getProperty(String property) {
        if (metaClass.getMetaProperty(property))
           return metaClass.getProperty(this, property)
        else
           return props?.getAt(property)
    }

    public void setProperty(String property, Object newValue) {
        if (metaClass.getMetaProperty(property))
           metaClass.setProperty(this, property, newValue)
        else {
           if (props == null)
             props = [:]
           props [property] = newValue
        }
    }
}
