/**
 * Created by IntelliJ IDEA.
 * User: Danno.Ferrin
 * Date: Apr 26, 2008
 * Time: 8:31:21 AM
 */
package swing.greet

import java.awt.Cursor
import java.beans.PropertyChangeListener
import javax.swing.*

lookAndFeel('nimbus', 'mac', ['metal', [boldFonts: false]])

actions() {
    loginAction = action(
        name: 'Login',
        enabled: bind(source: controller, sourceProperty: 'allowLogin'),
        closure: controller.&login
    )

    filterTweets = action(
        name: 'Filter',
        enabled: bind(source: controller, sourceProperty: 'allowSelection'),
        closure: controller.&filterTweets
    )

    userSelected = action(
        name: 'Select User',
        enabled: bind(source: controller, sourceProperty: 'allowSelection'),
        closure: controller.&userSelected
    )

    tweetAction = action(
        name: 'Update',
        enabled: bind(source: controller, sourceProperty: 'allowTweet'),
        closure: controller.&tweet
    )
}

tweetLineFont = new java.awt.Font("Ariel", 0, 12)
tweetLine = panel(border: emptyBorder(3), preferredSize:[250,84]) {
    gridBagLayout()
    tweetIcon = label(verticalTextPosition:SwingConstants.BOTTOM,
        horizontalTextPosition:SwingConstants.CENTER,
        //anchor: BASELINE, insets: [3, 3, 3, 3])
        anchor: CENTER, insets: [3, 3, 3, 3])
    tweetText = textArea(rows: 4, lineWrap: true, wrapStyleWord: true,
        opaque: false, editable: false, font: tweetLineFont,
        gridwidth: REMAINDER, weightx: 1.0, fill: BOTH, insets: [3, 3, 3, 3])
}
tweetRenderer = {list, tweet, index, isSelected, isFocused ->
    if (tweet?.user as String) {
        tweetIcon.icon = controller.api.imageMap[tweet.user.profile_image_url as String]
        tweetIcon.text = tweet.user.screen_name
        tweetText.text = tweet.text
    } else if (tweet?.text as String) {
        tweetIcon.icon = controller.api.imageMap[tweet.parent().profile_image_url as String]
        tweetIcon.text = tweet.parent().screen_name
        tweetText.text = tweet.text
    } else {
        tweetIcon.icon = null
        tweetIcon.text = null
        tweetText.text = null
    }
    tweetLine
} as ListCellRenderer


userCell = label(border: emptyBorder(3))
userCellRenderer = {list, user, index, isSelected, isFocused ->
    if (user) {
        userCell.icon = controller.api.imageMap[user.profile_image_url as String]
        userCell.text = "<html>$user.screen_name<br>$user.name<br>$user.location<br>"
    } else {
        userCell.icon = null
        userCell.text = null
    }
    userCell
} as ListCellRenderer

greetFrame = frame(title: "Greet - A Groovy Twitter Client",
    defaultCloseOperation: javax.swing.JFrame.DISPOSE_ON_CLOSE, size: [320, 480],
    locationByPlatform:true)
{
    panel(cursor: bind(source: controller, sourceProperty: 'allowSelection',
        converter: {it ? null : Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)})
    ) {

        gridBagLayout()
        users = comboBox(renderer: userCellRenderer, action: userSelected,
            gridwidth: REMAINDER, insets: [6, 6, 3, 6], fill: HORIZONTAL)
        label('Search:', insets: [3, 6, 3, 3])
        searchField = textField(columns: 20, action: filterTweets,
            insets: [3, 3, 3, 3], weightx: 1.0, fill: BOTH)
        button(action: filterTweets,
            gridwidth: REMAINDER, insets: [3, 3, 3, 6], fill:HORIZONTAL)
        tabbedPane(gridwidth: REMAINDER, weighty: 1.0, fill: BOTH) {
            scrollPane(title: 'Timeline') {
                timelineList = list(visibleRowCount: 20, cellRenderer: tweetRenderer)
            }
            scrollPane(title: 'Tweets') {
                tweetList = list(visibleRowCount: 20, cellRenderer: tweetRenderer)
            }
            scrollPane(title: 'Statuses') {
                statusList = list(visibleRowCount: 20, cellRenderer: tweetRenderer)
            }
            // add data change listeners
            [timeline:timelineList, tweets:tweetList, statuses:statusList].each {p, w ->
                controller.addPropertyChangeListener(p,
                    {evt -> w.listData = evt.newValue as Object[]} as PropertyChangeListener
                )
            }
        }
        separator(fill: HORIZONTAL, gridwidth: REMAINDER)
        tweetBox = textField(action:tweetAction,
            fill:BOTH, weightx:1.0, insets:[3,3,3,3], gridwidth:2)
        tweetButton = button(tweetAction,
            enabled:bind(source:tweetBox, sourceProperty:'text', converter:{it.length() < 140}),
            gridwidth:REMAINDER, insets:[3,3,3,3])
        separator(fill: HORIZONTAL, gridwidth: REMAINDER)
        statusLine = label(text: bind(source: controller.api, sourceProperty: 'status'),
            gridwidth: REMAINDER, insets: [3, 6, 3, 6], anchor: WEST
        )
    }


    loginDialog = dialog(
        title: "Login to Greet", pack: true, resizable: false,
        defaultCloseOperation: WindowConstants.DISPOSE_ON_CLOSE,
        locationByPlatform:true)
    {
        panel(border: emptyBorder(3),
            cursor: bind(source: controller, sourceProperty: 'allowLogin',
                converter: {it ? null : Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)})
        ) {
            gridBagLayout()
            label("Username:",
                anchor: EAST, insets: [3, 3, 3, 3])
            twitterNameField = textField(action:loginAction, columns: 20,
                gridwidth: REMAINDER, insets: [3, 3, 3, 3])
            label("Password:",
                anchor: EAST, insets: [3, 3, 3, 3])
            twitterPasswordField = passwordField(action:loginAction, columns: 20,
                gridwidth: REMAINDER, insets: [3, 3, 3, 3])
            panel()
            button(loginAction, defaultButton: true,
                anchor: EAST, insets: [3, 3, 3, 3])
        }
    }
}

controller.addPropertyChangeListener("friends", {evt ->
    view.edt { users.model = new DefaultComboBoxModel(evt.newValue as Object[]) }
} as PropertyChangeListener)

new Timer(120000, filterTweets).start()
