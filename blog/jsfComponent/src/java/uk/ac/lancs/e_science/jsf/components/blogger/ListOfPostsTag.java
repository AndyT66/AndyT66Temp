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
package uk.ac.lancs.e_science.jsf.components.blogger;

import javax.faces.application.Application;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;

import com.sun.faces.util.ConstantMethodBinding;


public class ListOfPostsTag extends UIComponentTag {

	private String posts;
	private String action;
	private String showComments;
	private String showFullContent;
	private String showCreator;
	/**
	 * @return the symbolic name of the component type. We will define the clas for this tape latter in the faces.config file
	 */
	public String getComponentType() {
		return "uk.ac.lancs.e_science.jsf.components.blogger.ListOfPosts";
	}
	/**
	 * @return the symbolic name of the renderer. If null, it means that the renderer name is not defined and the component will render it by itself
	 */
	public String getRendererType() {
		// null means the component renders itself
		return null;
	}
	/**
	 * This method releases any resources allocated during the execution of this tag handler. 
	 */
	public void realease(){
		//the super class method should be called
		super.release();
		posts = null;
		action = null;
	}
	
	/**
	 * this method is used to pass attributes taken fron the JSP page to the renderer. You can use
	 * the JSF EL in the value for the tag attribute.
	 */
	protected void setProperties(UIComponent component){
		//the super classs method should be called
		super.setProperties(component);
		UICommand command = (UICommand)component;

		FacesContext context = FacesContext.getCurrentInstance();
		Application app = context.getApplication();
		
		if (posts!=null){
			if (isValueReference(posts)){
				ValueBinding vb = app.createValueBinding(posts);
				component.setValueBinding("posts",vb);
			}else{
				component.getAttributes().put("posts",posts);
			}
		}
	    if (action != null) {
	    	MethodBinding actionBinding = null;
	    	if (isValueReference(action)){
	    		actionBinding = app.createMethodBinding(action,null);
	    	} else {
	    		actionBinding = new ConstantMethodBinding(action);
	    	}
	    	command.setAction(actionBinding);
	    		
	    }
		if (showComments!=null){
			if (isValueReference(showComments)){
				ValueBinding vb = app.createValueBinding(showComments);
				component.setValueBinding("showComments",vb);
			}else{
				component.getAttributes().put("showComments",new Boolean(showComments));
			}			
		}	   
		if (showFullContent!=null){
			if (isValueReference(showFullContent)){
				ValueBinding vb = app.createValueBinding(showFullContent);
				component.setValueBinding("showFullContent",vb);
			}else{
				component.getAttributes().put("showFullContent",new Boolean(showFullContent));
			}			
		}	
		if (showCreator!=null){
			if (isValueReference(showCreator)){
				ValueBinding vb = app.createValueBinding(showCreator);
				component.setValueBinding("showCreator",vb);
			}else{
				component.getAttributes().put("showCreator",new Boolean(showCreator));
			}			
		}		
   

	}
	
	public void setPosts(String value){
		this.posts = value;
	}
	
	public String getPosts(){
		return posts;
	}
	public void setAction(String action) {
	    this.action = action;
	}
	public void setShowComments(String s){
		this.showComments =s;
	}
	public void setShowFullContent(String s){
		this.showFullContent=s;
	}
	public void setShowCreator(String s){
		this.showCreator=s;
	}	
}
