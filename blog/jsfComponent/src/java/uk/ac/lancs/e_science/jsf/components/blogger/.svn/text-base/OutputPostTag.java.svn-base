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
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;

public class OutputPostTag extends UIComponentTag {

	private String post;
	/**
	 * @return the symbolic name of the component type. We will define the clas for this tape latter in the faces.config file
	 */
	public String getComponentType() {
		return "uk.ac.lancs.e_science.jsf.components.blogger.OutputPost";
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
		post = null;
	}
	
	/**
	 * this method is used to pass attributes taken fron the JSP page to the renderer. You can use
	 * the JSF EL in the value for the tag attribute.
	 */
	protected void setProperties(UIComponent component){
		//the super classs method should be called
		super.setProperties(component);
		
		if (post!=null){
			if (isValueReference(post)){
				FacesContext context = FacesContext.getCurrentInstance();
				Application app = context.getApplication();
				ValueBinding vb = app.createValueBinding(post);
				component.setValueBinding("post",vb);
			}else{
				component.getAttributes().put("post",post);
			}
		}
	}
	
	public void setPost(String value){
		this.post = value;
	}
	
	public String getPost(){
		return post;
	}

}
