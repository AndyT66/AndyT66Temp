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

import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Comment;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Creator;

public class CommentCreatorProcessingState implements XMLPostContentHandleState {
    private Comment _comment;
    private String _currentText;
    private Creator _creatorUnderConstruction;
    private XMLPostContentHandleState _previousState;

    public CommentCreatorProcessingState(Comment comment, XMLPostContentHandleState previousState){
        _currentText = new String();
        _previousState = previousState;
        _comment = comment;
        _creatorUnderConstruction = new Creator();
    }
    public XMLPostContentHandleState startElement(String uri, String localName, String qName, Attributes atts) throws SAXException{
        return this;
    }
    public XMLPostContentHandleState endElement(String uri, String localName, String qName) throws SAXException{
       if (localName.equals("commentCreator")){
            _comment.setCreator(_creatorUnderConstruction);
            return _previousState;
        }
        if (localName.equals("idCommentCreator")){
            _creatorUnderConstruction.setId(_currentText);
            _currentText = new String();
            return this;
        }
        if (localName.equals("descriptionCommentCreator")){
            _creatorUnderConstruction.setDescription(_currentText);
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
}
