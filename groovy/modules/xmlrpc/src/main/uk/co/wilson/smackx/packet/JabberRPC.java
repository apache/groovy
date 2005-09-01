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

package uk.co.wilson.smackx.packet;

import org.jivesoftware.smack.packet.IQ;

/**
 * @author John Wilson
 *
 */

public class JabberRPC extends IQ {
  public JabberRPC(final String xml) {
    this.xml = "<query xmlns='jabber:iq:rpc'>\n" + xml + "\n</query>";
  }
  
  public String getChildElementXML() {
    return this.xml;
  }
  
  private final String xml;
}
