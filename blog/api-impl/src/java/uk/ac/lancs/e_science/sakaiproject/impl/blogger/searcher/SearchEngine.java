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

package uk.ac.lancs.e_science.sakaiproject.impl.blogger.searcher;


import java.util.*;

import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.File;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Image;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.reader.PostReader;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.reader.XMLConverter;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.searcher.QueryBean;
import uk.ac.lancs.e_science.sakaiproject.impl.blogger.persistence.PersistenceException;
import uk.ac.lancs.e_science.sakaiproject.impl.blogger.persistence.SakaiPersistenceManager;

public class SearchEngine {
    private SakaiPersistenceManager persistenceManager;
    
    public SearchEngine () throws SearchException{
    	try{
    		persistenceManager = new SakaiPersistenceManager();
    	} catch (PersistenceException e){
    		throw new SearchException();
    	}
    }
    public List doSearch(QueryBean query, String siteId) throws SearchException{
        try{
            List posts = persistenceManager.getPosts(query, siteId);
            return filter(posts, query.getQueryString());
        } catch (PersistenceException e){
            throw new SearchException();
        }
    }
    public List getAllPost(String siteId) throws SearchException{
        try{
            return persistenceManager.getAllPost(siteId);
        } catch (Exception e){
           throw new SearchException();
        }
    }
    public Post getPost(String OID) throws SearchException{
    	try{
            return persistenceManager.getPost(OID);
        } catch (Exception e){
        	throw new SearchException();
        }
    }
    public Image getImage(String imageId, int size) throws SearchException{
    	try{
    		return persistenceManager.getImage(imageId, size);
        } catch (Exception e){
        	throw new SearchException();
        }
    }
    public File getFile(String fileId) throws SearchException{
    	try{
    		return persistenceManager.getFile(fileId);
        } catch (Exception e){
        	throw new SearchException();
        }
    }    
    private List filter(List source, String queryString) {
        ArrayList result = new ArrayList();
        Iterator it = source.iterator();
        if (queryString==null || queryString.trim().equals(""))
            return source;

        XMLConverter xmlConverter = new XMLConverter();
        PostReader reader = new PostReader(xmlConverter);

        while (it.hasNext()){
            Post post= (Post)it.next();
            reader.parsePost(post);
            String xmlPost = xmlConverter.getXML();
            if (isContained(queryString,xmlPost.toUpperCase()))
                result.add(post);
        }

        return result;  //To change body of implemented methods use File | Settings | File Templates.
    }
    /*
    this method checks if the text contains all the tokens in the list but no necessarily in a row or in
    the same order
    */
    private boolean isContained(String queryString, String text){
        StringTokenizer st = new StringTokenizer(queryString);  //we can not put out the st because we need go to the begining each time
        while (st.hasMoreElements()){
            String token = st.nextToken();
            if (!containsTheWord(text,token.toUpperCase()))
                return false;
        }
        return true;
    }

    /*
     * NOTE: we known that text is a XML document, for that, the word never will be in the start or in the finish.
     * NOTE: a word will be found in the text if it is between delimiters
     * NOTE: this method ignore the xml tags in the ResourceInfo internalXMLRepresentation because the tags havent attributes and namespaces
     */
    private boolean containsTheWord(String text, String word){
        try{
            int wordPosition = text.indexOf(word);
            if (wordPosition==-1)
                return false;
            char previousCharacter, nextCharacter;
            int wordLength = word.length();
            int textLength = text.length();
            while (wordPosition!=-1){
                if (wordPosition!=0){
                    previousCharacter = text.charAt(wordPosition-1);
                    if (Character.isLetterOrDigit(previousCharacter)||previousCharacter=='<'){
                        wordPosition = text.indexOf(word, wordPosition+1);
                        continue;
                    }
                }
                if (wordPosition == textLength-wordLength)
                    return true;
                nextCharacter = text.charAt(wordPosition+wordLength);
                if (Character.isLetterOrDigit(nextCharacter)||nextCharacter=='>'){
                    wordPosition = text.indexOf(word, wordPosition+1);
                    continue;
                }
                return true;
            }
            return false;

        } catch (Throwable e){
            return false;
        }
    }



}
