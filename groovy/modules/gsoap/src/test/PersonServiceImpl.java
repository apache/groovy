/*
 * PersonService.java
 *
 * Created on 13 février 2006, 14:38
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author SU16766
 */
public class PersonServiceImpl implements PersonService {
    
    private static Logger logger=Logger.getLogger(PersonServiceImpl.class);
    
    private List directory;
    
    public PersonServiceImpl() {
        directory = new ArrayList();
        
        Person person1 = new Person();
        Person person2 = new Person();
        Person person3 = new Person();
        
        person1.setFirstName("Guillaume");
        person1.setLastName("Laforge");
        person1.setId(new Integer(1));
        
        person2.setFirstName("Dan");
        person2.setLastName("Diephouse");
        person2.setId(new Integer(2));
        
        person3.setFirstName("Jochen");
        person3.setLastName("Theodorou");
        person3.setId(new Integer(3));
        
        directory.add(person1);
        directory.add(person2);
        directory.add(person3);
        
        logger.debug("org.codehaus.groovy.gsoap.test.PersonServiceImpl: "+person1.toString());
    }
    
    public Person[] getPersons(){
        return (Person[])directory.toArray(new Person[directory.size()]);
    }
    
    public Person findPerson(Integer id){
       
        Person moa = new Person();
        moa.setFirstName("Guillaume");
        moa.setLastName("Alleon");
        moa.setId(new Integer(7));
        logger.debug("org.codehaus.groovy.gsoap.test.findPerson: "+moa.toString());
        
        Person p = null;
        for (Iterator it = directory.iterator(); it.hasNext();){
            p = (Person) it.next();
            logger.debug("org.codehaus.groovy.gsoap.test.findPerson: "+p.toString());
            if (p.getId() == id) return p;
        }
        
        return moa;
    }

}
