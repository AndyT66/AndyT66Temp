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

package uk.ac.lancs.e_science.sakai.tools.blogger;


import javax.faces.context.FacesContext;

import uk.ac.lancs.e_science.sakaiproject.impl.blogger.BloggerManager;


import java.util.Map;

public class PostEditController extends PostEditionAbstractController{



    public PostEditController(){
    	blogger = BloggerManager.getBlogger();
    }

    public String doSave(){
    	return super.doSave();
    }
    public String doPreview(){
        Map sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        sessionMap.put("back","editPost");    	
    	return super.doPreview();
    }    
    public String addParagraph(){
    	super.addParagraph();
    	return "refreshEditPost";
    }
    
    public String modifyParagraph(){
    	super.modifyParagraph();
    	return "refreshEditPost";
    }    
    
    public String addImage(){
    	super.addImage();
    	return "refreshEditPost";
    }
    
    public String modifyImage(){
    	super.modifyImage();
    	return "refreshEditPost";    	
    }
    public String setCurrentElementIndex(int currentElementIndex) {
    	super.setCurrentElementIndex(currentElementIndex);
    	return "refreshEditPost";    	
    }
}