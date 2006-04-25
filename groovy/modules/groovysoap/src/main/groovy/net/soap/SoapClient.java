package groovy.net.soap;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.xml.transform.TransformerException;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.xfire.client.Client;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import groovy.lang.Binding;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.GroovyShell;

import org.apache.log4j.Logger;

import org.codehaus.xfire.util.DOMUtils;
/**
 * <p>Dynamic Groovy proxy around Xfire stack.</p>
 *
 * @author Guillaume Alleon
 *
 */
public class SoapClient extends GroovyObjectSupport {
    
    private Client client;
    static private Logger logger=Logger.getLogger(SoapClient.class);
    /**
     * <p>Transform a string so that it can be used in a Groovy
     * bean. Whitespace are removed and  the first letter is
     * replaced by its lower case counterpart.</p>
     * <p/>
     *
     * @param str the string to be uncapitalized.
     */
    private static String uncapitalize(String str) {
        int len = str.length();
        StringBuffer buffer = new StringBuffer(len);
        
        for (int i = 0; i < len; i++) {
            char ch = str.charAt(i);
            
            if (i != 0) {
                if (ch != ' ' && ch != ':') buffer.append(ch);
            } else {
                buffer.append(Character.toLowerCase(ch));
            }
        }
        return buffer.toString();
    }
    /**
     * <p>Generate a Map representing the data types corresponding
     * to the XML document</p>
     * <p/>
     *
     * @param node a Node of tha XML document.
     * @param type a Map representing the datatypes
     */
    private void generateType(Node node, Map type) {
        // TODO rajouter le test d'existence
        if (node.hasChildNodes() && !type.containsKey(node.getNodeName())) {
            Set members = new HashSet();
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++){
                Node n = children.item(i);
                if(n.getNodeType()==Node.ELEMENT_NODE)
                    members.add(n.getNodeName());
                generateType(n, type);
            }
            if(members.size()!=0)
                type.put(node.getNodeName(), members);
        }
    }
    /**
     * <p>Transform the xfire response to a java/groovy type
     * by generating and interpreting a Groovy script when a
     * XML document</p>
     * <p/>
     *
     * @param obj the xfire response.
     */
    private Object toReturn(Object obj) {
        if (obj instanceof Document) {
            Map type = new HashMap();
            StringBuffer classSource = new StringBuffer();
            
            // Extract the root node from the Document
            Element root = ((Document) obj).getDocumentElement();
            // Clean the XML document
            cleanNode(root);
//            try {
//                DOMUtils.writeXml((Node) root, System.out);
//            } catch (TransformerException ex) {
//                ex.printStackTrace();
//            }
            generateType(root, type);
            
            for (Iterator iterator = type.keySet().iterator(); iterator.hasNext();) {
                String aType = (String) iterator.next();
                classSource.append("class ")
                .append(uncapitalize(aType))
                .append(" {\n");
                Set members = (Set) type.get(aType);
                for (Iterator it1 = members.iterator(); it1.hasNext();) {
                    String member = (String)it1.next();
                    classSource.append("  @Property ");
                    if (type.containsKey(member)) classSource.append("List ");
                    classSource.append(uncapitalize(member))
                    .append("\n");
                }
                classSource.append("}\n");
            }
            
            classSource.append("result = ");
            createCode(root, classSource);
            
            if (logger.isDebugEnabled()) logger.debug(classSource);
            
            Binding binding = new Binding();
            
            try {
                Object back = new GroovyShell(binding).evaluate(classSource.toString());
                if (logger.isDebugEnabled()) logger.debug(back.toString());
                return back;
            } catch (CompilationFailedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            return null;
        } else {
            if (logger.isDebugEnabled()) logger.debug("returned object is of type " + obj.getClass().getName());
            return obj;
        }
    }
    
    /**
     * <p>Remove dead text in an XML tree.</p>
     * <p/>
     *
     * @param element the considered element of the XML document.
     */
    private void cleanNode(Element element){
        // TODO rajouter le test d'existence
        Node child;
        Node next = (Node)element.getFirstChild();
        
        while ((child = next) != null){
            next = child.getNextSibling();
            if (child.getNodeType() == Node.TEXT_NODE) {
                if (child.getNodeValue().trim().length() == 0) element.removeChild(child);
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                cleanNode((Element)child);
            }
        }
    }
    
    /**
     * <p>Create instances of the data types previously generated
     * from the XML document.</p>
     *
     * @param element     Element of the XML document.
     * @param classSource StringBuffer containing the generated code.
     */
    private void createCode(Element element, StringBuffer classSource){
        Node child = null;
        Node fnode = (Node)element.getFirstChild();
        Node lnode = (Node)element.getLastChild();
        
        Node next = fnode;
        Node prev = null;
        
        boolean opened = false;
        
        if (logger.isDebugEnabled()) logger.debug("Entering createCode");
        
        classSource.append("new "+ uncapitalize(element.getNodeName())+"(");
        while ((child = next) != null){
            next = child.getNextSibling();
            
            if (logger.isDebugEnabled()) {
                logger.debug("1");
                if (child != null) logger.debug(" child = "+child.getNodeName()+child.hasChildNodes()+child.getChildNodes().getLength());
                if (next  != null) logger.debug(" next  = "+next.getNodeName());
                if (prev  != null) logger.debug(" prev  = "+prev.getNodeName());
                logger.debug("\n");
            }
            
            if (child.hasChildNodes() && (child.getChildNodes().getLength() == 1) && (child.getFirstChild().getNodeType() == Node.TEXT_NODE)) {
                if (logger.isDebugEnabled()) logger.debug("Create basic type for"+child.getNodeName());
                classSource.append(uncapitalize(child.getNodeName())+":\""+child.getFirstChild().getNodeValue()+"\"");
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("2");
                    if (child != null) logger.debug(" child = "+child.getNodeName());
                    if (next  != null) logger.debug(" next  = "+next.getNodeName());
                    if (prev  != null) logger.debug(" prev  = "+prev.getNodeName());
                    logger.debug("\n");
                }
                if ( (prev == null) || !((prev.getNodeName()).equals(child.getNodeName()))) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("...Opening [");
                        if (prev !=null) logger.debug(">"+prev.getNodeName()+"<>"+child.getNodeName()+"<");
                    }
                    opened = true;
                    classSource.append(uncapitalize(child.getNodeName())+":[");
                }
                createCode((Element)child, classSource);
                //if ((next == null) && (prev.getNodeName() == child.getNodeName())) classSource.append("]");
                if ((next == null) && opened) {
                    opened = false;
                    classSource.append("]");
                }
            }
            
            if (child != lnode) {
                classSource.append(",");
            }
            
            prev = child;
            
        }
        classSource.append(")");
    }
    /**
     * Invoke a method on a gsoap component using the xfire
     * dynamic client </p>
     * <p>Example of Groovy code:</p>
     * <code>
     * client = new SoapClient("http://www.webservicex.net/WeatherForecast.asmx?WSDL")
     * def answer = client.GetWeatherByPlaceName("Seattle")
     * </code>
     *
     * @param name name of the method to call
     * @param args parameters of the method call
     * @return the value returned by the method call
     */
    public Object invokeMethod(String name, Object args) {
        Object[] objs = InvokerHelper.getInstance().asArray(args);
        
        try {
            Object[] response = client.invoke(name, objs);
            // TODO Parse the answer
            return toReturn(response[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
    }
    /**
     * Create a SoapClient using a URL
     * <p>Example of Groovy code:</p>
     * <code>
     * client = new SoapClient("http://www.webservicex.net/WeatherForecast.asmx?WSDL")
     * </code>
     *
     * @param URLLocation the URL pointing to the WSDL
     */
    public SoapClient(String URLLocation){
        try {
            client = new Client(new URL(URLLocation));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}
