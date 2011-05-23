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
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;

public class CommentsProcessingState implements XMLPostContentHandleState {
    private Post _post;
    private String _currentText;
    private Comment _commentUnderConstruction;
    private XMLPostContentHandleState _previousState;

    public CommentsProcessingState(Post post, XMLPostContentHandleState previousState){
        _currentText = new String();
        _previousState = previousState;
        _post = post;
        _commentUnderConstruction = new Comment();
    }
    public XMLPostContentHandleState startElement(String uri, String localName, String qName, Attributes atts) throws SAXException{
        if (localName.equals("commentCreator")){
            return new CommentCreatorProcessingState(_commentUnderConstruction,this);
        }
        if (!isKnowTag(localName)){
             //Non expected tags, probabily they are html tags. We will add them to the text
            _currentText+="<"+localName+" ";
            int numberAttributes = atts.getLength();
            for (int i=0;i<numberAttributes;i++){
                String attName = atts.getLocalName(i);
                String attValue = atts.getValue(i);
                _currentText+=attName+"=\""+attValue+"\" ";
            }
            _currentText+=">";
        }

        return this;
    }
    public XMLPostContentHandleState endElement(String uri, String localName, String qName) throws SAXException{
        if (localName.equals("comments"))
            return _previousState;

        if (localName.equals("comment")){
            _post.addComment(_commentUnderConstruction);
            _commentUnderConstruction = new Comment();
            return this;
        }
        if (localName.equals("commentText")){
            _commentUnderConstruction.setText(_currentText);
            _currentText = new String();
            return this;
        }
        if (localName.equals("commentDate")){
            _commentUnderConstruction.setDate(Long.parseLong(_currentText));
            _currentText = new String();
            return this;
        }
        if (!isKnowTag(localName)){
            //Non expected tags, probabily they are html tags. We will add them to the text
            _currentText+="</"+localName+">";
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
    private boolean isKnowTag(String tag){
        if (tag.equals("comments") ||
            tag.equals("comment") ||
            tag.equals("commentText") ||
            tag.equals("commentDate") ||
            tag.equals("commentCreator"))
            return true;
        return false;
    }
}
