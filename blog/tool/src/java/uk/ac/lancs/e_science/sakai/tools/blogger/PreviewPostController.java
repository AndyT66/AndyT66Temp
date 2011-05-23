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

import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.ServletRequest;

import uk.ac.lancs.e_science.sakai.tools.blogger.cacheForImages.CacheForImages;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.Blogger;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.SakaiProxy;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Image;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;
import uk.ac.lancs.e_science.sakaiproject.impl.blogger.BloggerManager;


public class PreviewPostController extends BloggerController{
    private Post post;
    private Blogger blogger;
    
    public PreviewPostController(){
    	blogger = BloggerManager.getBlogger();
    }
    
    public String doSave(){
        blogger.storePost(post,SakaiProxy.getCurrentUserId(), SakaiProxy.getCurrentSiteId());
        //we have to remove the images from cache
        if (post.getElements()!=null){
        	for (int i=0;i<post.getElements().length;i++){
        		if (post.getElements()[i] instanceof Image){
        			CacheForImages cache = CacheForImages.getInstance();
        			cache.removeImage(((Image)post.getElements()[i]).getIdImage());
        		}
        	}
        }    	
    	return doBack();
    }    
    
    public void setPost(Post post){
        //nothing
    }
   
    public Post getPost(){
        ServletRequest request = (ServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        post = (Post) request.getAttribute("post");
        return post;

    }    
    public String doBack(){
        Map sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        String back = (String) sessionMap.get("back");
        return back;
    }
}
