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
import java.util.Map;

import org.sakaiproject.util.ResourceLoader;

import javax.faces.application.Application;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.servlet.http.HttpServletRequest;

import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.File;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Image;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.LinkRule;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Paragraph;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.PostElement;

public class UIEditPost extends UIOutput{
	private static final int EDIT=1;
	private static final int DELETE=2;
	private static final int UP=3;
	private static final int DOWN=4;
	private static final String INDEX_ID="INDEX_ID";
	private static final String ACTION_ID="ACTION_ID";
	private static final String UP_CAPTION="Up";
	private static final String DOWN_CAPTION="Down";
	
	
	private Post post = null;
	private IBloggerJSFEditionController controller = null;
	private String contextPath=null;
	private ResourceLoader messages;
	
	public void encodeBegin(FacesContext context) throws IOException{
		ResponseWriter writer = context.getResponseWriter();
		writer.startElement("div",this);
		post = (Post)getAttributes().get("post");
		controller = (IBloggerJSFEditionController)getAttributes().get("controller");
        messages = new ResourceLoader("uk.ac.lancs.e_science.sakai.tools.blogger.bundle.Messages");
		HttpServletRequest req =((HttpServletRequest)context.getExternalContext().getRequest());
		contextPath = req.getContextPath();
		if (post!=null){
			writePost(writer, post);
		}
	}
	public void endodeEnd(FacesContext context) throws IOException{
		ResponseWriter writer = context.getResponseWriter();
		writer.endElement("div");
	}
	private void writePost(ResponseWriter writer, Post post) throws IOException{
		renderHiddenElementForIndexId(writer);
		renderHiddenElementForAction(writer);
		PostElement[] elements = post.getElements();
		if (elements!=null){
			writer.write(getStyleDefinition());
			writer.startElement("table",this);//--------------------------- 0
			writer.writeAttribute("class","mainTable",null);
			int elementIndex = 0;
			makeHeader(writer);
			for (int i=0;i<elements.length;i++){
				PostElement element = elements[i];
				writer.startElement("tr",this);//-------------------------- 1
				writer.startElement("td",this);//-------------------------- 2
				writer.startElement("table",this);//----------------------- 3
				if (elementIndex<elements.length-1)
					writer.writeAttribute("class","rowTable",null);
				else
					writer.writeAttribute("class","lastRowTable",null);
				
				writer.writeAttribute("cellpading","2px",null);
				writer.writeAttribute("cellspacing","0",null);
				
				writer.startElement("tr",this);//-------------------------- 4
				
				writer.startElement("td",this);//-------------------------- 5
				writer.writeAttribute("class","tdbase td1",null);
				writer.write(""+elementIndex);
				writer.endElement("td"); //-------------------------------- 5# 
				
				writer.startElement("td",this); //------------------------- 6 
				writer.writeAttribute("class","tdbase td2",null);
				if (element instanceof Paragraph)
					writeParagraph(writer, (Paragraph) element);
				if (element instanceof Image)
					writeImage(writer, (Image) element);
				if (element instanceof LinkRule)
					writeLinkRule(writer, (LinkRule)element);
				if (element instanceof File)
					writeFile(writer, (File)element);
				writer.endElement("td"); //-------------------------------- 6#
				
				writer.startElement("td",this); //------------------------- 7
				writer.writeAttribute("class","tdbase td3",null);	
				makeButtons(writer,elementIndex,elements.length);
				writer.endElement("td");//--------------------------------- 7#
				writer.endElement("tr");//--------------------------------- 4#
				
				writer.endElement("table");//------------------------------ 3#
				writer.endElement("td");//--------------------------------- 2#
				writer.endElement("tr");//--------------------------------- 1#
				elementIndex++;
			}
			writer.endElement("table");//---------------------------------- 0#
		}
		
	}
	private void makeHeader(ResponseWriter writer) throws IOException{
		writer.startElement("tr",this);
		writer.startElement("td",this);
		writer.startElement("table",this);
		writer.writeAttribute("cellpading","0",null);
		writer.writeAttribute("cellspacing","0",null);
		writer.startElement("tr",this);
		
		writer.startElement("td",this);
		writer.writeAttribute("class","tdHeader tdH1",null);
		writer.write(messages.getString("index"));
		writer.endElement("td");

		writer.startElement("td",this);
		writer.writeAttribute("class","tdHeader tdH2",null);
		writer.write(messages.getString("element"));
		writer.endElement("td");
		
		writer.startElement("td",this);
		writer.writeAttribute("class","tdHeader tdH3",null);
		writer.write(messages.getString("commands"));
		writer.endElement("td");

		writer.endElement("td");
		writer.endElement("tr");
		writer.endElement("table");
		writer.endElement("td");
		
		writer.endElement("tr");
	}
	
	private void makeButtons(ResponseWriter writer,int elementIndex,int numberOfElements) throws IOException{
		writer.startElement("table",this);//----------------------- 0
		writer.writeAttribute("cellpading","0",null);
		writer.writeAttribute("cellspacing","0",null);
		writer.startElement("tr",null); //------------------------- 1
		
		writer.startElement("td",this); //------------------------- 2
		renderButton(writer,"Edit",messages.getString("edit"),elementIndex);
		writer.endElement("td"); //-------------------------------- 2#
		
		writer.startElement("td",this); //------------------------- 3
		renderButton(writer,"Delete",messages.getString("delete"),elementIndex);
		writer.endElement("td"); //-------------------------------- 3#
		
		writer.startElement("td",this); //------------------------- 4
		writer.startElement("table",this); //---------------------- 5
		writer.writeAttribute("cellpading","0",null);
		writer.writeAttribute("cellspacing","0",null);
		writer.startElement("tr",null); //------------------------- 6
		writer.startElement("td",this);//-------------------------- 7
		if (elementIndex==0)
			writer.write("&nbsp;");
		else
			renderUpDownButton(writer,UP,UP_CAPTION,UP_CAPTION,elementIndex);
		writer.endElement("td"); //-------------------------------- 7#
		writer.endElement("tr"); //-------------------------------- 6#
		writer.startElement("tr",this);//---------------------------8
		writer.startElement("td",this); //------------------------- 9
		if (elementIndex==numberOfElements-1)
			writer.write("&nbsp;");
		else
			renderUpDownButton(writer,DOWN,DOWN_CAPTION,DOWN_CAPTION,elementIndex);
		writer.endElement("td"); //-------------------------------- 9#
		writer.endElement("tr"); //-------------------------------- 8#
		writer.endElement("table"); //----------------------------- 5#
		
		writer.endElement("td"); //-------------------------------- 4#
		writer.endElement("tr"); //-------------------------------- 1#
		writer.endElement("table"); //----------------------------- 0#
		
	}
	
	private void writeParagraph(ResponseWriter writer, Paragraph paragraph) throws IOException{
		writer.write(paragraph.getText());


		
	}
	private void writeImage(ResponseWriter writer, Image image) throws IOException{
		writer.startElement("table",this);
		writer.startElement("tr",this);
		writer.startElement("td",this);
		writer.write("<img src='"+contextPath+"/servletForImages?idImage="+image.getIdImage()+"&size=thumbnail'"+"/>"); //we dont use writeAtrribute because it will transform the & to &amp;
		writer.endElement("td");
		writer.startElement("td",this);
		writer.write(image.getDescription());
		writer.endElement("td");
		writer.endElement("tr");
		writer.endElement("table");
		
	}
	
	private void writeLinkRule(ResponseWriter writer, LinkRule link) throws IOException{
		
		writer.startElement("table",this);
		writer.startElement("tr",this);
		writer.startElement("td",this);
		writer.write(messages.getString("linkDescription")+":");
		writer.endElement("td");
		writer.startElement("td",this);
		writer.write(link.getDescription());
		writer.endElement("td");
		writer.endElement("tr");
		writer.startElement("tr",this);
		writer.startElement("td",this);
		writer.write(messages.getString("linkURL")+":");
		writer.endElement("td");
		writer.startElement("td",this);
		writer.write(link.getLinkExpression());
		writer.endElement("td");
		writer.endElement("tr");
		writer.endElement("table");
	}

	private void writeFile(ResponseWriter writer, File file) throws IOException{
		writer.write(file.getDescription());
	}
	
	public void decode(FacesContext context){
		Map requestMap = context.getExternalContext().getRequestParameterMap();
		
		int action=0;
		if (!requestMap.containsKey(INDEX_ID))
			return;
		int index=Integer.parseInt((String)requestMap.get(INDEX_ID));
		String strAction = (String)requestMap.get(ACTION_ID);
		
		if (strAction.equals("Edit")){
			action=EDIT;
		}
		if (strAction.equals("Delete")){
			action=DELETE;
		}
		if (strAction.equals("Up")){
			action=UP;
		}
		if (strAction.equals("Down")){
			action=DOWN;
		}
		if (action==EDIT)
			controller.setCurrentElementIndex(index);
		if (action==DELETE)
			controller.removeElement(index);
		if (action==UP)
			controller.upElement(index);
		if (action==DOWN)
			controller.downElement(index);
		
		
	}
	
	private void renderButton(ResponseWriter writer,String name, String value,int index) throws IOException{
		writer.startElement("input",this);
		writer.writeAttribute("type","submit",null);
		writer.writeAttribute("value",value,null);
		writer.writeAttribute("name",name,null);
		writer.writeAttribute("onClick","javascript:document.getElementById('"+INDEX_ID+"').value='"+index+"';document.getElementById('"+ACTION_ID+"').value='"+name+"';",null);
		writer.endElement("input");
	}
	private void renderUpDownButton(ResponseWriter writer,int type,String name, String value,int index) throws IOException{
		writer.startElement("input",this);
		writer.writeAttribute("type","image",null);
		String action="";
		if (type==UP){
			writer.writeAttribute("src",contextPath+"/img/up.gif",null);
			writer.writeAttribute("alt","Up",null);
			action = "Up";
		} if (type==DOWN){
			writer.writeAttribute("src",contextPath+"/img/down.gif",null);
			writer.writeAttribute("alt","Down",null);
			action = "Down";
		}
		writer.writeAttribute("value",value,null);
		writer.writeAttribute("name",name,null);
		writer.writeAttribute("onClick","javascript:document.getElementById('"+INDEX_ID+"').value='"+index+"';document.getElementById('"+ACTION_ID+"').value='"+action+"';",null);
		writer.endElement("input");
	}	
	private void renderHiddenElementForAction(ResponseWriter writer) throws IOException{
		writer.startElement("input",this);
		writer.writeAttribute("type","hidden",null);
		writer.writeAttribute("id",ACTION_ID,null);
		writer.writeAttribute("name",ACTION_ID,null);
		writer.writeAttribute("value","",null);
		writer.endElement("input");		
	}
	private void renderHiddenElementForIndexId(ResponseWriter writer) throws IOException{
		writer.startElement("input",this);
		writer.writeAttribute("type","hidden",null);
		writer.writeAttribute("id",INDEX_ID,null);
		writer.writeAttribute("name",INDEX_ID,null);
		writer.writeAttribute("value","-1",null);
		writer.endElement("input");		
	}
	private String getStyleDefinition(){
		int w1=50;
		int w2=450;
		int w3=145;
		StringBuilder sb = new StringBuilder();
		sb.append("<style title=\"css\">");
		sb.append("table.mainTable{");
		sb.append("}");
		sb.append("table.rowTable{");
		sb.append("		border-style:solid;");
		sb.append("		border-width:1px;");
		sb.append("		border-left:none;");
		sb.append("		border-right:none;");
		sb.append("		border-top:none;");
		sb.append("		border-color:#DDDFE4;");		
		sb.append("}");
		sb.append("table.lastRowTable{");
		sb.append("		border-bottom:none;");
		sb.append("}");
		sb.append("td.tdbase{");
		sb.append("		border-style:solid;");
		sb.append("		border-width:1px;");
		sb.append("		border-left:none;");
		sb.append("		border-top:none;");
		sb.append("		border-bottom:none;");
		sb.append("		padding-left:10px;");
		sb.append("		border-color:#DDDFE4;");
		sb.append("}");
		sb.append("td.td1{");
		sb.append("		width:"+w1+"px;");
		sb.append("}");
		sb.append("td.td2{");
		sb.append("		width:"+w2+"px;");
		sb.append("}");
		sb.append("td.td3{");
		sb.append("		width:"+w3+"px;");
		sb.append("		border-right: none;");
		sb.append("}");
		sb.append("td.tdHeader{");
		sb.append("		border-style:solid;");
		sb.append("		border-width:1px;");
		sb.append("		border-left:none;");
		sb.append("		border-top:none;");
		sb.append("		border-bottom:1px;");
		sb.append("		padding-left:10px;");
		sb.append("		background-color:#CFCFCF;");
		sb.append("		border-color:#DDDFE4;");		
		sb.append("}");
		sb.append("td.tdH1{");
		sb.append("		width:"+w1+"px;");
		sb.append("}");
		sb.append("td.tdH2{");
		sb.append("		width:"+w2+"px;");
		sb.append("}");
		sb.append("td.tdH3{");
		sb.append("		width:"+w3+"px;");
		sb.append("		border-right: none;");
		sb.append("}");		
		sb.append("</style>");
		return sb.toString();
	}


}
