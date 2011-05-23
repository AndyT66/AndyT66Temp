package uk.ac.lancs.e_science.jsf.components.blogger;

import java.io.IOException;

import org.sakaiproject.util.ResourceLoader;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.servlet.http.HttpServletRequest;

public class LegendWriter {
	
	private UIComponent uicomponent;
	private String contextPath;
	private ResourceLoader messages;
	private ResponseWriter writer;
	private String formClientId; 
	
	public LegendWriter( FacesContext context, UIComponent uicomponent){
		this.uicomponent = uicomponent;
		writer = context.getResponseWriter();
        messages = new ResourceLoader("uk.ac.lancs.e_science.sakai.tools.blogger.bundle.Messages");
		HttpServletRequest req =((HttpServletRequest)context.getExternalContext().getRequest());
		contextPath = req.getContextPath();
		
	}
	
	public void writeLegend() throws IOException{
		writer.startElement("table", uicomponent);
		writer.writeAttribute("align", "right",null);
		writer.writeAttribute("cellpadding", "0",null);
		writer.writeAttribute("cellspacing", "0",null);
		writer.writeAttribute("border", "0",null);
     	writer.startElement("tr",uicomponent);
		writer.startElement("td", uicomponent);
		writer.write("<img src='img/private.gif'/>");
		writer.write("&nbsp;&nbsp;"+messages.getString("isPrivate")+"<br/>");
		writer.write("<img src='img/pencil.gif'/>");
		writer.write("&nbsp;&nbsp;"+messages.getString("isModificable")+"<br/>");
		writer.write("<img src='img/commentsAllowed.gif'/>");
		writer.write("&nbsp;&nbsp;"+messages.getString("isCommentable"));
		writer.endElement("td");;
		writer.endElement("tr");
		writer.endElement("table");
	}
}
