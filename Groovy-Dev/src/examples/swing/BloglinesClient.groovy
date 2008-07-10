/*
 * BloglinesClient.groovy - an example of the Bloglines Web Services
 *
 * Written by Marc Hedlund <marc@precipice.org>, September 2004.
 * 
 * Mangled by John Wilson September 2004
 *
 * Small adaptions to JSR Version by Dierk Koenig, June 2005
 *
 * Used in Marc's article at:
 *    http://www.oreillynet.com/pub/a/network/2004/09/28/bloglines.html
 *
 * Requirements:
 *   - install Groovy as detailed at <http://groovy.codehaus.org/>.
 *   - put commons-httpclient-3.0-rc3.jar into GROOVY_HOME/lib
 *       see <http://jakarta.apache.org/commons/httpclient/>.
 *       note: this is currently designed for HttpClient2.x and not HttpClient3.x
 *
 * To Launch:
 *   groovy BloglinesClient.groovy
 *
 * This work is licensed under the Creative Commons Attribution
 * License. To view a copy of this license, visit
 * <http://creativecommons.org/licenses/by/2.0/> or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 */

import groovy.swing.SwingBuilder
import java.awt.BorderLayout
import javax.swing.JOptionPane
import javax.swing.JSplitPane
import javax.swing.JTree
import javax.swing.ListSelectionModel
import javax.swing.WindowConstants
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeSelectionModel
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.UsernamePasswordCredentials
import org.apache.commons.httpclient.methods.GetMethod

//Set up global variables and data types
server = 'rpc.bloglines.com'

class Feed { def name; def id; def unread; String toString() { (unread == "0" ? name : "${name} (${unread})") } }

class Item { def title; def description; String toString() { title } }

//Ask the user for account information (using simple dialogs)
email = 
JOptionPane.showInputDialog(null, "Email address:", "Log in to Bloglines", 
			      JOptionPane.QUESTION_MESSAGE)
password = 
JOptionPane.showInputDialog(null, "Password:", "Log in to Bloglines", 
			      JOptionPane.QUESTION_MESSAGE)

//Use HTTPClient for web requests since the server requires authentication
client = new HttpClient()
credentials = new UsernamePasswordCredentials(email, password)
client.state.setCredentials("Bloglines RPC", server, credentials)

abstractCallBloglines = { method, parameters ->
  url = "http://${server}/${method}${parameters}"
  try {
    get = new GetMethod(url)
    get.doAuthentication = true
    client.executeMethod(get)
    return get.responseBodyAsStream
  } catch (Exception e) {
    println "Error retrieving <${url}>: ${e}"
  }
}

callBloglinesListsub = abstractCallBloglines.curry('listsubs', '')
callBloglinesGetItems = abstractCallBloglines.curry('getitems')

//Get the list of subscriptions and parse it into a GPath structure
opml = new XmlSlurper().parse(callBloglinesListsub())

//Descend into the subscription outline, adding to the feed tree as we go
treeTop = new DefaultMutableTreeNode("My Feeds")
parseOutline(opml.body.outline.outline, treeTop)

def parseOutline(parsedXml, treeLevel) {
  parsedXml.each{ outline ->
    if (outline['@xmlUrl'] != null) {  // this is an individual feed
      feed = new Feed(name:outline['@title'], id:outline['@BloglinesSubId'], 
                      unread:outline['@BloglinesUnread'])
      treeLevel.add(new DefaultMutableTreeNode(feed))
    } else {  // this is a folder of feeds
      folder = new DefaultMutableTreeNode(outline['@title'])
      parseOutline(outline.outline, folder)
      treeLevel.add(folder)
    }
  }
}

//Build the base user interface objects and configure them
swing = new SwingBuilder()
feedTree = new JTree(treeTop)
itemList = swing.list()
itemText = swing.textPane(contentType:'text/html', editable:false)
model = feedTree.selectionModel
model.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
itemList.selectionMode = ListSelectionModel.SINGLE_SELECTION

//Set up the action closures that will react to user selections
listItems = { feed ->
  rssStream = callBloglinesGetItems("?s=${feed.id}&n=0")  
  if (rssStream != null) {
    try {
      rss = new XmlSlurper().parse(rssStream)
      itemList.listData =  rss.channel.item.collect(new Vector()) {
		new Item(title:it.title, description:it.description)
       }
       feed.unread = "0"  // update the unread item count in the feed list
     } catch (Exception e) {
       println "Error during <${feed.name}> RSS parse: ${e}"
    }
  }
}

feedTree.valueChanged = { event ->
  itemText.text = ""  // clear any old item text
  node = (DefaultMutableTreeNode) feedTree.getLastSelectedPathComponent()
  if (node != null) {
    feed = node.userObject
     if (feed instanceof Feed && feed.unread != "0") {
       listItems(feed)
     }
  }
}

itemList.valueChanged = { event ->
  item = event.source.selectedValue
  if (item instanceof Item && item?.description != null) {
    itemText.text = "<html><body>${item.description}</body></html>"
  }
}

//Put the user interface together and display it
gui = 
swing.frame(title:'Bloglines Client', location:[100,100], size:[800,600], 
	      defaultCloseOperation:WindowConstants.EXIT_ON_CLOSE) {

 panel(layout:new BorderLayout()) {
   splitPane(orientation:JSplitPane.HORIZONTAL_SPLIT, dividerLocation:200) {
     scrollPane {
		  widget(feedTree)
	 }

      splitPane(orientation:JSplitPane.VERTICAL_SPLIT, dividerLocation:150) {
        scrollPane(constraints:BorderLayout.CENTER) {
	        widget(itemList)
	    }

	    scrollPane(constraints:BorderLayout.CENTER) {
	      widget(itemText)
	    }
      }
    }
  }
}

gui.show()
