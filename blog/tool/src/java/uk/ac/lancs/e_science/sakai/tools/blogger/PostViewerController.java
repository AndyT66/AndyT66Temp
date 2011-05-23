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
import javax.faces.el.ValueBinding;
import javax.servlet.ServletRequest;

import uk.ac.lancs.e_science.sakaiproject.api.blogger.Blogger;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.SakaiProxy;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;
import uk.ac.lancs.e_science.sakaiproject.impl.blogger.BloggerManager;


import com.sun.faces.util.Util;


public class PostViewerController extends BloggerController{
    private Post post;
    private Blogger blogger;

  
    public PostViewerController(){
    	blogger = BloggerManager.getBlogger();
    }
    
    public void setPost(Post post){
        this.post = post;
    }
    public Post getPost(){
        if (post!=null){
        	//we have to recover the post from database, because is possible that it would be changed in other controller.
        	//for example, if somebody has change the post or has added a comment, the post in the database is different than memory one 
        	post = blogger.getPost(post.getOID(),SakaiProxy.getCurrentUserId());
        }
        return post;
    }
    public Post getPostFromMemory(){
        ServletRequest request = (ServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        post = (Post) request.getAttribute("post");
        return post;

    }    

    public String getTitle(){
        return post.getTitle();
    }

    public int getVisibility(){
        return post.getState().getVisibility();
    }

    public String getCreator(){
        return post.getCreator().getDescription();
    }
    public String doEditPost(){
        ValueBinding binding =  Util.getValueBinding("#{postEditController}");
        PostEditController postEditController = (PostEditController)binding.getValue(FacesContext.getCurrentInstance());
        postEditController.setPost( post);

        return "editPost";
    }
    public String doDeletePost(){
    	if (post!=null){
    		blogger.deletePost(post.getOID(), SakaiProxy.getCurrentUserId());
    	}
		ValueBinding binding =  Util.getValueBinding("#{postListViewerController}");
		PostListViewerController controller = (PostListViewerController)binding.getValue(FacesContext.getCurrentInstance());
		controller.reloadPosts();
		
        return controller.getLastView();
    }
    public String doConfirmDeletePost(){
        return "confirmDeletePost";
    }
    public String doBack(){
        return "viewPost";
    }

    public String doAddComment(){
      ValueBinding binding =  Util.getValueBinding("#{addCommentController}");
      AddCommentController addCommentController = (AddCommentController)binding.getValue(FacesContext.getCurrentInstance());
      addCommentController.setPost(post);        
      return "AddCommentView";
    }
    
    public boolean getActivetAddCommentCommand(){
    	if (SakaiProxy.getCurrentUserId().equals(post.getCreator().getId()))
    		return true; //the autor can add comments
    	return post.getState().getAllowComments();
    }
    public boolean getActivateEditCommand(){
    	if (SakaiProxy.getCurrentUserId().equals(post.getCreator().getId()))
    		return true; //the creator can always modify the post
    	return !post.getState().getReadOnly(); //depending the flag
    }
    public boolean getActivateDeleteCommand(){
    	if (SakaiProxy.getCurrentUserId().equals(post.getCreator().getId()))
    		return true; //only the creator can delete the post
    	return false; 
    }

}
