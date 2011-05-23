/*************************************************************************************
 * Copyright (c) 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.

 *************************************************************************************/

package uk.ac.lancs.e_science.sakaiproject.api.blogger.post.xml;

import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;

public class XMLPostContentHandler implements ContentHandler{
    private Post _post;
    private XMLPostContentHandleState _state;

    public XMLPostContentHandler(){
     }

    public Post getPost(){
        return _post;
    }

    public void setDocumentLocator(Locator locator) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void startDocument() throws SAXException {

        _post = new Post();
        _state = new RootProcessingState(_post);
     }

    public void endDocument() throws SAXException {
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
     }

    public void endPrefixMapping(String prefix) throws SAXException {
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        _state = _state.startElement(uri,localName,qName,atts);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        _state = _state.endElement(uri,localName,qName);
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        _state = _state.characters(ch,start,length);
    }

    public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void processingInstruction(String target, String data) throws SAXException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void skippedEntity(String name) throws SAXException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}
