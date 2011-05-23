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

import java.io.IOException;

import java.util.Collection;
import java.util.Map;
import org.sakaiproject.util.ResourceLoader;

import javax.faces.component.UICommand;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.MethodBinding;
import javax.faces.event.ActionEvent;

import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;

public class UIListOfPosts extends UICommand {
	private ResourceLoader messages;
	
	public UIListOfPosts(){
		super();
	}
	
	public void encodeBegin(FacesContext context) throws IOException{
		
        messages = new ResourceLoader("uk.ac.lancs.e_science.sakai.tools.blogger.bundle.Messages");
		
		Collection listOfPost = (Collection)getAttributes().get("posts");
		Boolean showComments= (Boolean)getAttributes().get("showComments");
		Boolean showFullContent = (Boolean)getAttributes().get("showFullContent");
		Boolean showCreator= (Boolean)getAttributes().get("showCreator");
				
		
		
		if (showComments==null)
			showComments = new Boolean(false);
		if (showFullContent==null)
			showFullContent = new Boolean(false);
		if (showCreator==null)
			showCreator = new Boolean(true);

		if (listOfPost == null || listOfPost.size()==0){
			ResponseWriter writer = context.getResponseWriter();
			writer.write("<br/><br/><br/>");
			writer.write("<span class='spanEmtpyBlogger'>"+messages.getString("emptyBlogger")+"</span>");
		
		} else{
			writePosts(listOfPost,context, showComments.booleanValue(), showFullContent.booleanValue(),showCreator.booleanValue());
			
			
		}
	}
	public void endodeEnd(FacesContext context) throws IOException{
		
	}
	
	private void writePosts(Collection<Post> listOfPosts, FacesContext context, boolean showComments, boolean showFullContent, boolean showCreator) throws IOException{
		
		ResponseWriter writer = context.getResponseWriter();

		
		PostWriter postWriter = new PostWriter(context,this);


		writer.write("<br/>");
		
		writer.startElement("input",this);
		writer.writeAttribute("type","hidden",null);
		writer.writeAttribute("id","idSelectedPost",null);
		writer.writeAttribute("name","idSelectedPost",null);
		writer.endElement("input");
		
		writer.startElement("table",this);
		writer.writeAttribute("class","tableHeader",null);

		for (Post post: listOfPosts){
			writer.startElement("tr", this);
			writer.startElement("td", this);
			if (showFullContent)
				postWriter.printFullContent(post, showComments,true,showCreator);
			else
				postWriter.printShortContent(post, showComments,showCreator);
			writer.endElement("td");
			writer.endElement("tr");
			writer.startElement("tr", this);
			writer.startElement("td", this);
			writer.writeAttribute("class","tdGapWithLine",null);
			writer.endElement("td");
			writer.endElement("tr");
		}
		writer.write("<tr><td>");
		LegendWriter lw = new LegendWriter(context,this);
		lw.writeLegend();
		writer.write("</td></tr>");
		writer.endElement("table");
	}

	public void decode(FacesContext context){
		Map requestMap = context.getExternalContext().getRequestParameterMap();

		if (!requestMap.containsKey("idSelectedPost"))
			return;
		String postOID=(String)requestMap.get("idSelectedPost");
		if (postOID.equals("")) //this happens when clicking in the pager
			return;

		Collection<Post> listOfPost = (Collection)getAttributes().get("posts");
		
		for (Post post: listOfPost){
			if (post.getOID().equals(postOID)){
				context.getExternalContext().getSessionMap().put("post",post);
			}
		}

		queueEvent(new ActionEvent(this));
		
	}

	public MethodBinding getAction() {
		return super.getAction();
	}

	public void setAction(MethodBinding action) {
		super.setAction(action);
	}
	public MethodBinding getActionListener() {
		return super.getActionListener();
	}

	public void setActionListener(MethodBinding actionListener) {
		super.setActionListener(actionListener);
	}	
	
		
}

