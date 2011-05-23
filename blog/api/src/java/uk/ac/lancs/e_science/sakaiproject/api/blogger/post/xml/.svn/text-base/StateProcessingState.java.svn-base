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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.State;


public class StateProcessingState implements XMLPostContentHandleState {
    private Post _post;
    private String _currentText;
    private State _stateUnderConstruction;
    private XMLPostContentHandleState _previousState;

    public StateProcessingState(Post post, XMLPostContentHandleState previousState){
        _currentText = new String();
        _previousState = previousState;
        _post = post;
        _stateUnderConstruction = new State();
    }
    public XMLPostContentHandleState startElement(String uri, String localName, String qName, Attributes atts) throws SAXException{
        return this;
    }
    public XMLPostContentHandleState endElement(String uri, String localName, String qName) throws SAXException{
       if (localName.equals("state")){
            _post.setState(_stateUnderConstruction);
            return _previousState;
        }
        if (localName.equals("visibility")){
            _stateUnderConstruction.setVisibility(Integer.parseInt(_currentText));
            _currentText = new String();
            return this;
        }
        if (localName.equals("readOnly")){
            _stateUnderConstruction.setReadOnly(parseBoolean(_currentText));
            _currentText = new String();
            return this;
        }
        if (localName.equals("allowComments")){
            _stateUnderConstruction.setAllowComments(parseBoolean(_currentText));
            _currentText = new String();
            return this;
        }
        return this;
    }
    public XMLPostContentHandleState characters(char ch[], int start, int length) throws SAXException{
        if (_currentText == null) {
            _currentText = new String(ch, start, length);
        } else {
            _currentText+=new String(ch,start, length);
        }
        return this;
    }

    private boolean parseBoolean(String parseString) {

      if("true".equals(parseString)) {

        return true;

      } else {

        return false;

      } 

    }
}
