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

package uk.ac.lancs.e_science.sakaiproject.api.blogger.post;

public class PostUtilities {
    /**
     *
     * @param post
     * @return the sum of al paragraphs text. If the Post hasn't any Paragraph the empty string is returned.<br/>
     * Each paragraph is separed for the previos by a '\n' char
     */
    public String getTextBodyAsPlainText(Post post){
        StringBuilder result = new StringBuilder("");
        PostElement[] elements = post.getElements();
        if (elements==null)
        	return "";

        for (int i=0;i<elements.length;i++){
            PostElement element = elements[i];
            if (element instanceof Paragraph){
                result.append(((Paragraph)element).getText());
                result.append("\n");
          }
        }
        return result.toString();

    }

    public String getFirstParagraphOrNFirstCharacters(Post post,int numberOfCharacters){
        StringBuilder result = new StringBuilder("");
        PostElement[] elements = post.getElements();
        
        if (elements==null)
        	return "";

        for (int i=0;i<elements.length;i++){
            PostElement element = elements[i];

            if (element instanceof Paragraph){
                String text =  ((Paragraph)element).getText();
                if (text.length()>numberOfCharacters){
                    text = text.substring(0,numberOfCharacters);
                    int posLastSpace = text.lastIndexOf(" ");
                    if (posLastSpace>=0)
                        text = text.substring(0,posLastSpace);

                }
                result.append(text);
                return result.toString();
          }
        }
        return result.toString();
    }
}
