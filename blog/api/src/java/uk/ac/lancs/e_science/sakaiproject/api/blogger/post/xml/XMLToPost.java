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

import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;

import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;

import java.io.StringReader;
import java.io.IOException;

public class XMLToPost {
    public Post convertXMLInPost(String xml){

		// SAK-13408 - The specified parserClass is only in the Sun JRE. This is an issue for Websphere,
    	// which uses the IBM JRE. The solution is to let the JRE itself pick the right SAXParser to use.
    	// In the Sun JRE's case, it will choose the specified parserClass by default anyways.
    	
        //String parserClass = "com.sun.org.apache.xerces.internal.parsers.SAXParser";
        try{
            //XMLReader reader = XMLReaderFactory.createXMLReader(parserClass);
            XMLReader reader = XMLReaderFactory.createXMLReader();
            XMLPostContentHandler ch = new XMLPostContentHandler();
            reader.setContentHandler(ch);
            InputSource is = new InputSource(new StringReader(xml));
            reader.parse(is);
            Post post = ch.getPost();
            return post;
        } catch (SAXException e){
            e.printStackTrace();
            return null;
        } catch (IOException e){
            e.printStackTrace();
            return null;
        } 
    }
}
