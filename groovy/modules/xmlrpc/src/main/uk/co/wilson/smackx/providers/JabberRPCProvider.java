/*
 * Copyright 2005 John G. Wilson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package uk.co.wilson.smackx.providers;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.util.StringUtils;
import org.xmlpull.v1.XmlPullParser;

import uk.co.wilson.smackx.packet.JabberRPC;

/**
 * @author John Wilson
 *
 */

public class JabberRPCProvider implements IQProvider {

  public IQ parseIQ(final XmlPullParser parser) throws Exception {
  final StringBuffer buffer = new StringBuffer();

    // skip the <query> tag by calling parser.next()
    while (true) {
      switch (parser.next()) {
        case XmlPullParser.TEXT:
          // We need to escape characters like & and <
          buffer.append(StringUtils.escapeForXML(parser.getText()));
          break;
  
        case XmlPullParser.START_TAG:
          buffer.append('<' + parser.getName() + '>');
          break;
  
        case XmlPullParser.END_TAG:
          if ("query".equals(parser.getName())) {
            // don't save the </query> end tag
            return new JabberRPC(buffer.toString().trim());
          } else {
            buffer.append("</" + parser.getName() + '>');
            break;
          }
  
        default:
      }
    }
  }
}
