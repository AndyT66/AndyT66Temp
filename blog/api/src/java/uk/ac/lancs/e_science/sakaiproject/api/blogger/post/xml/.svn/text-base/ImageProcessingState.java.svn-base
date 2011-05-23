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

import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Image;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;

public class ImageProcessingState implements XMLPostContentHandleState {
    private Post _post;
    private String _currentText;
    private Image _imageUnderConstruction;
    private XMLPostContentHandleState _previousState;

    public ImageProcessingState(Post post, XMLPostContentHandleState previousState){
        _currentText = new String();
        _previousState = previousState;
        _post = post;
        _imageUnderConstruction= new Image();
    }
    public XMLPostContentHandleState startElement(String uri, String localName, String qName, Attributes atts) throws SAXException{
        return this;
    }
    public XMLPostContentHandleState endElement(String uri, String localName, String qName) throws SAXException{
       if (localName.equals("image")){
            _post.addElement(_imageUnderConstruction);
            return _previousState;
        }
        if (localName.equals("imageId")){
            _imageUnderConstruction.setIdImage(_currentText);
            _currentText = new String();
            return this;
        }
        if (localName.equals("imageDescription")){
        	_imageUnderConstruction.setDescription(_currentText);
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
