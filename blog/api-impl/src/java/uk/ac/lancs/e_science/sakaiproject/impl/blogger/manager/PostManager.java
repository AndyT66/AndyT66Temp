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

package uk.ac.lancs.e_science.sakaiproject.impl.blogger.manager;

import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Comment;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Creator;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;
import uk.ac.lancs.e_science.sakaiproject.impl.blogger.persistence.SakaiPersistenceManager;

public class PostManager {
	private SakaiPersistenceManager persistenceManager;
	private SecurityManager securityManager;
	
    public PostManager (){
    	try{
    		persistenceManager = new SakaiPersistenceManager();
    		securityManager = new SecurityManager();
    	} catch (Exception e){
    		e.printStackTrace();
    	}
    }
    public void storePost(Post post, String userId,String siteId){
        try{
            if (securityManager.isAllowedToStorePost(userId,post)){
            	SakaiPersistenceManager persistenceManager = new SakaiPersistenceManager();
                if (post.getCreator()==null){
                    Creator creator = new Creator(userId);
                    post.setCreator(creator);
                }
                persistenceManager.storePost(post, siteId);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public void deletePost(String postId, String userId){
         try{
            if (securityManager.isAllowedToDeletePost(userId,postId)){
                persistenceManager.deletePost(postId);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public void addCommentToPost(Comment comment, String postId, String userId, String siteId){
         try{
            if (securityManager.isAllowedToComment(userId,postId)){
                Creator creator = new Creator(userId);
                comment.addCreator(creator);
                Post post = persistenceManager.getPost(postId);
                post.addComment(comment);
                persistenceManager.storePost(post, siteId);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public void initRepository(){
    	try{
    		persistenceManager.initRepository();
    	} catch (Exception e){
    		e.printStackTrace();
    	}
    }

}
