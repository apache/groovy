package groovy.net.soap;

import java.io.FileInputStream;
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
            Map members = new HashMap();
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++){
                Node n = children.item(i);
                if (n.getNodeType()==Node.ELEMENT_NODE) {
                  Integer valence = (Integer)members.get(n.getNodeName());
                  if (valence == null) {
                    valence = new Integer(1);
                  } else {
                    valence ++;
                  }
                  members.put(n.getNodeName(), valence);
                }
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

//          if (logger.isDebugEnabled()) {
//              try {
//                  DOMUtils.writeXml((Node) root, System.out);
//              } catch (TransformerException ex) {
//                  ex.printStackTrace();
//              }
//          }

            // generate a map that associates to each type its members
            generateType(root, type);

            // Test if we have a complex type
            if ((type.keySet().size() != 1) || (((Map)type.get(root.getNodeName())).keySet().size() != 1)) {
                for (Iterator iterator = type.keySet().iterator(); iterator.hasNext();) {
                    String aType = (String) iterator.next();
//                  classSource.append("class ")
//                  .append(uncapitalize(aType))
//                  .append(" {\n");
                    String tt = new String(uncapitalize(aType));
                    if ("return".equals(tt)) tt = "out";
                    classSource.append("class ")
                    .append(tt)
                    .append(" {\n");
                    Map members = (Map) type.get(aType);
                    for (Iterator it1 = members.keySet().iterator(); it1.hasNext();) {
                        String member = (String)it1.next();
                        classSource.append("  @Property ");
                        if ((Integer)members.get(member) > 1) classSource.append("List ");
                        classSource.append(uncapitalize(member))
                        .append("\n");
                    }
                    classSource.append("}\n");
                }
            }

            // Test if the return type is different from void
            if (type.get(root.getNodeName()) != null) {
              classSource.append("result = ");
              createCode(root, type, classSource);
            }

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
    private void createCode(Element root, Map type, StringBuffer classSource){
        if ((type.keySet().size() == 1) && (((Map)type.get(root.getNodeName())).keySet().size() == 1)) {
            createCodeST(root, classSource);
        } else {
            createCodeCT(root, type, classSource);
        }
    }

    /**
     * <p>Create Complex types instances of the data types previously generated
     * from the XML document.</p>
     *
     * @param element     Element of the XML document.
     * @param classSource StringBuffer containing the generated code.
     */
    private void createCodeCT(Element element, Map type, StringBuffer classSource){
        Node child = null;
        Node fnode = (Node)element.getFirstChild();
        Node lnode = (Node)element.getLastChild();
        
        Node next = fnode;
        Node prev = null;

        boolean opened = false;

        Map  members = (Map)type.get(element.getNodeName());
        
        if (logger.isDebugEnabled()) logger.debug("Entering createCode");
        
        String tt = new String(uncapitalize(element.getNodeName()));
        if ("return".equals(tt)) tt = "out";

        if (members == null) {
            classSource.append("\"\"");
        } else {
//          classSource.append("new " + uncapitalize(element.getNodeName()) + "(");
            classSource.append("new " + tt + "(");

            while ((child = next) != null){
                next = child.getNextSibling();
 
                if (!opened) {
                    classSource.append(uncapitalize(child.getNodeName()) + ":");
                    if ((Integer)members.get(child.getNodeName()) > 1) {
                      classSource.append("[");
                      opened = true;
                    }
                }

                if (child.hasChildNodes() && (child.getChildNodes().getLength() == 1) && (child.getFirstChild().getNodeType() == Node.TEXT_NODE)) {
                    // Test for the depth 
                    classSource.append("\""+child.getFirstChild().getNodeValue()+"\"");
                } else {
                    createCodeCT((Element)child, type, classSource);
                }
           
                if (child != lnode) {
                    classSource.append(",");
                }
            
                prev = child;
            
            }
            if (opened) classSource.append("]");
            classSource.append(")");
        }
    }

    /**
     * <p>Create Simple types instances of the data types previously generated
     * from the XML document.</p>
     *
     * @param element     Element of the XML document.
     * @param classSource StringBuffer containing the generated code.
     */
    private void createCodeST(Element element, StringBuffer classSource){
        Node child = null;
        Node fnode = (Node)element.getFirstChild();
        Node lnode = (Node)element.getLastChild();
        
        Node next = fnode;
        Node prev = null;

        boolean opened = false;

        if (logger.isDebugEnabled()) logger.debug("Entering createCodeST");
        
        classSource.append("[");

        while ((child = next) != null){
          next = child.getNextSibling();

          classSource.append("\""+child.getFirstChild().getNodeValue()+"\"");
           
          if (child != lnode) {
              classSource.append(",");
          }
            
          prev = child;
            
        }
        classSource.append("]");
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
            if (response.length == 0) {
              return true;
            } else {
              return toReturn(response[0]);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
    }

    Object testGoogle(String fileName) {
        try {
            return toReturn(DOMUtils.readXml(new FileInputStream(fileName)));
        } catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public SoapClient() {}

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
