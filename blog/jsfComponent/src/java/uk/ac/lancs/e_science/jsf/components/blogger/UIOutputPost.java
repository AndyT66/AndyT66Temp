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
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;


import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;

public class UIOutputPost extends UIOutput {
	public void encodeBegin(FacesContext context) throws IOException{
		Post post = (Post)getAttributes().get("post");
		
		if (post!=null){
			PostWriter postWriter = new PostWriter(context, this);
			postWriter.printFullContent(post,true,false,true);
			LegendWriter lw = new LegendWriter(context,this);
			lw.writeLegend();
		}
	}
	public void endodeEnd(FacesContext context) throws IOException{
	}

}
