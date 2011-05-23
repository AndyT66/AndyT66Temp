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


import java.util.Arrays;
import java.util.Date;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;



import uk.ac.lancs.e_science.sakaiproject.api.blogger.user.IUser;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.util.UIDGenerator;


public class Post {
    private String oid;
    private String title; 
    private String shortText;
    private long date;
    private State state;
    private Creator creator = null;
    private Collection keywords;
    private ArrayList elements; //PostElement
    private Collection authors;
    private Collection comments;


    public Post(){
        oid = UIDGenerator.getIdentifier(this);
        title="";
        date=new Date().getTime();
        keywords = new ArrayList();
        elements = new ArrayList();
        comments = new ArrayList();
        state = new State();

    }
    public void setOID(String oid){
        this.oid = oid;
    }
    public String getOID(){
        return oid;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public String getTitle(){
        return title;
    }

    public void setShortText(String shortText){
    	this.shortText = shortText.trim();
    }
    public String getShortText(){
    	return shortText;
    }
    
    public void setDate(long date){
        this.date = date;
    }
    public long getDate(){
        return date;
    }

    public void setState(State state){
        this.state = state;
    }
    public State getState(){
        return state;
    }

    public void setCreator(Creator creator){
        this.creator = creator;
    }
    public Creator getCreator(){
        return creator;
    }

    public void addKeyword(String keyword){
        keywords.add(keyword);
    }
    public String[] getKeywords(){
        return (String[])keywords.toArray(new String[0]);
    }
    public void setKeywords(String[] keywords){
    	if (keywords == null)
    		this.keywords = new ArrayList();
    	else{
    		this.keywords = Arrays.asList(keywords);
    	}
    }

    public void addElement(PostElement element){
        elements.add(element);
    }
    public void replaceElement(PostElement newElement, int index){
    	elements.remove(index);
    	elements.add(index,newElement);
    }

    public void addAuthor(IUser author){
        authors.add(author);
    }
    public IUser[] getAuthors(){
        return (IUser[])authors.toArray(new IUser[0]);
    }
    public void setPlainBody(String body){
        elements = new ArrayList();
        elements.add(new Paragraph(body));
    }
    public void addComment(Comment comment){
        comments.add(comment);
    }
    public Comment[] getComments(){
        return (Comment[])comments.toArray(new Comment[0]);
    }
    public void setImages(Image[] images){
    	//so far, nothing to do. Only to be java beans spec compliant
    }
    public Image[] getImages(){
    	ArrayList result = new ArrayList();
    	Iterator it = elements.iterator();
    	while (it.hasNext()){
    		Object element = it.next();
    		if (element instanceof Image){
    			result.add(element);
    		}
    	}
    	if (result.size()==0)
    		return null;
    	return (Image[])result.toArray(new Image[0]);
    	
    }
    public File[] getFiles(){
    	ArrayList result = new ArrayList();
    	Iterator it = elements.iterator();
    	while (it.hasNext()){
    		Object element = it.next();
    		if (element instanceof File){
    			result.add(element);
    		}
    	}
    	if (result.size()==0)
    		return null;
    	return (File[])result.toArray(new File[0]);
    	
    }    
    public void setElements(PostElement[] images){
    	//so far, nothing to do. Only to be java beans spec compliant
    }
    public PostElement[] getElements(){
    	if (elements.size()==0)
    		return null;
    	return (PostElement[])elements.toArray(new PostElement[0]);
    	
    }    
    public void removeElement(int index){
    	elements.remove(index);
    }
    
    public boolean hasImage(String imageId){
    	if (getImages()==null)
    		return false;
    		
    	Image[] images = this.getImages();
    	
    	for (int i=0;i<images.length;i++){
    		if (images[i].getIdImage().equals(imageId.trim()))
    			return true;
    	}
    	return false;
    }
    public boolean hasFile(String fileId){
    	if (getFiles()==null)
    		return false;
    	File[] files= this.getFiles();
    	for (int i=0;i<files.length;i++){
    		if (files[i].getIdFile().equals(fileId.trim()))
    			return true;
    	}
    	return false;
    }
}
