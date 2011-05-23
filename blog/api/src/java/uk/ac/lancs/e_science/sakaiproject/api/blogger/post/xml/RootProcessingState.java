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

import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Paragraph;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;


public class RootProcessingState implements XMLPostContentHandleState {
    private Post _post;
    private String _currentText;

    public RootProcessingState(Post post){
        _currentText = new String();
        _post = post;
    }
    public XMLPostContentHandleState startElement(String uri, String localName, String qName, Attributes atts) throws SAXException{
        if (localName.equals("state")){
            return new StateProcessingState(_post,this);
        }
        if (localName.equals("comments")){
            return new CommentsProcessingState(_post,this);
        }
        if (localName.equals("image")){
            return new ImageProcessingState(_post,this);
        }
        if (localName.equals("file")){
            return new FileProcessingState(_post,this);
        }        
        if (localName.equals("linkRule")){
            return new LinkRuleProcessingState(_post,this);
        }
        if (localName.equals("keywordsList")){
            return new KeywordsProcessingState(_post,this);
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
       if (localName.equals("post")){
            //nothing to do. It is the final of the document
            return this;
        }
        if (localName.equals("oid")){
            _currentText = new String();
            return this;
        }
        // Commented out as we are now starting to use the database column content
        // instead of the stuff embedded in the xml
        if (localName.equals("title")){
            _currentText = new String();
            return this;

        }
        if (localName.equals("shortText")){
            _post.setShortText(_currentText);
            _currentText = new String();
            return this;

        }        
        if (localName.equals("date")){
            _currentText = new String();
            return this;

        }
        if (localName.equals("keywordsList")){
            _currentText = new String();
            return this;

        }
        if (localName.equals("paragraph")){
            Paragraph paragraph = new Paragraph();
            paragraph.setText(_currentText.trim());
            _post.addElement(paragraph);
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

    private  boolean isKnowTag(String tag){
        if (tag.equals("post") ||
            tag.equals("oid") ||
            tag.equals("title") ||
            tag.equals("shortText") ||
            tag.equals("date") ||
            tag.equals("keywordsList") ||
            tag.equals("paragraph") ||
            tag.equals("creator") ||
            tag.equals("comment") ||
            tag.equals("linkRule") ||
            tag.equals("keywordsList") ||
            tag.equals("file") ||
            tag.equals("image"))
           return true;

        return false;


    }
}
