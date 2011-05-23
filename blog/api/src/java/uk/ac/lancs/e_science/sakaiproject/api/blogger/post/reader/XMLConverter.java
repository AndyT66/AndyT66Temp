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

package uk.ac.lancs.e_science.sakaiproject.api.blogger.post.reader;



import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Comment;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.File;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Image;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.LinkRule;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Paragraph;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.State;


public class XMLConverter implements PostConverter {

    private StringBuilder _stringBuffer;
    public XMLConverter(){
        _stringBuffer = new StringBuilder("");
    }
    public void reset(){
        _stringBuffer = new StringBuilder("");
    }

    public void convertShortText(String shortText){
    	if (shortText==null)
    		shortText="";
        _stringBuffer.append("<shortText>").append("<![CDATA[").append(shortText).append("]]>").append("</shortText>");

    }    
    public void convertState(State state){
        _stringBuffer.append("<state>");
        _stringBuffer.append("<visibility>").append(state.getVisibility()).append("</visibility>");
        _stringBuffer.append("<readOnly>").append(state.getReadOnly()).append("</readOnly>");
        _stringBuffer.append("<allowComments>").append(state.getAllowComments()).append("</allowComments>");
        _stringBuffer.append("</state>");
    }
    public void convertKeywords(String[] keywords){

        _stringBuffer.append("<keywordsList>");
        for (int i=0;i<keywords.length;i++){
            _stringBuffer.append("<keyword>").append("<![CDATA[").append(keywords[i]).append("]]>").append("</keyword>");
        }
        _stringBuffer.append("</keywordsList>");
    }
    public void convertLinkRules(LinkRule linkRule){
    	_stringBuffer.append("<linkRule>");
    	_stringBuffer.append("<linkRuleDescription>").append("<![CDATA[").append(linkRule.getDescription()).append("]]>").append("</linkRuleDescription>");
    	_stringBuffer.append("<linkExpression>").append("<![CDATA[").append(linkRule.getLinkExpression()).append("]]>").append("</linkExpression>");
    	_stringBuffer.append("</linkRule>");
    }
    public void convertImage(Image image){
    	_stringBuffer.append("<image>");
    	_stringBuffer.append("<imageId>").append(image.getIdImage()).append("</imageId>");
    	_stringBuffer.append("<imageDescription>").append("<![CDATA[").append(image.getDescription()).append("]]>").append("</imageDescription>");
    	_stringBuffer.append("</image>");
    }
    
    public void convertFile(File file){
    	_stringBuffer.append("<file>");
    	_stringBuffer.append("<fileId>").append(file.getIdFile()).append("</fileId>");
    	_stringBuffer.append("<fileDescription>").append("<![CDATA[").append(file.getDescription()).append("]]>").append("</fileDescription>");
    	_stringBuffer.append("</file>");
    }

    public void convertParagraph(Paragraph paragraph){
    	
        _stringBuffer.append("<paragraph>").append("<![CDATA[").append(paragraph.getText()).append("]]>").append("</paragraph>");
    }

    public void convertComments(Comment[] comments){
        _stringBuffer.append("<comments>");
        for (int i=0;i<comments.length;i++){
            _stringBuffer.append("<comment>");
            _stringBuffer.append("<commentText>").append("<![CDATA[").append(comments[i].getText()).append("]]>").append("</commentText>");
            _stringBuffer.append("<commentDate>").append(comments[i].getDate()).append("</commentDate>");
            _stringBuffer.append("<commentCreator>");
            _stringBuffer.append("<idCommentCreator>").append(comments[i].getCreator().getId()).append("</idCommentCreator>");
            _stringBuffer.append("<descriptionCommentCreator>").append("<![CDATA[").append(comments[i].getCreator().getDescription()).append("]]>").append("</descriptionCommentCreator>");
            _stringBuffer.append("</commentCreator>");
            _stringBuffer.append("</comment>");
        }
        _stringBuffer.append("</comments>");
    }


    public String getXML(){
        StringBuilder result=new StringBuilder("");
        result.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        result.append("<post>");
        result.append(_stringBuffer);
        result.append("</post>");
        return result.toString();

    }

}
