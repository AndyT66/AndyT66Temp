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

package uk.ac.lancs.e_science.sakaiproject.impl.blogger;



import uk.ac.lancs.e_science.sakaiproject.api.blogger.Blogger;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Comment;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.File;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Image;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.searcher.QueryBean;
import uk.ac.lancs.e_science.sakaiproject.impl.blogger.manager.PostManager;
import uk.ac.lancs.e_science.sakaiproject.impl.blogger.manager.SearchManager;


public class BloggerImpl implements Blogger
{
	
	private static BloggerImpl instance=null;
    private PostManager postManager;
    private SearchManager searchManager;


    public static synchronized BloggerImpl getInstance(){
    	if (instance==null)
    		instance = new BloggerImpl();
    	return instance;
    }
    private BloggerImpl(){
    	try{
    		postManager = new PostManager();
    		searchManager = new SearchManager();
    		initRepository();
    	} catch (Exception e){
    		e.printStackTrace();
    	}
    }
    
    public void initRepository(){
    	postManager.initRepository();
    }
    
    public void storePost(Post post, String userId, String siteId){
		postManager.storePost(post,userId,siteId);
		
	}
	public void deletePost(String postId, String userId){
		postManager.deletePost(postId,userId);
	}
	
	public void addCommentToPost(Comment comment, String postId, String userId, String siteId){
		postManager.addCommentToPost(comment,postId,userId,siteId);
	}
	
	
	public Post getPost(String postId, String userId){
		try{
			return searchManager.getPost(postId,userId);
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	//SAK-14611
	public Post getPost(String postId){
		try{
			return searchManager.getPost(postId);
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	public Post[] getPosts(String siteId, String userId){
		try{
			return searchManager.getPosts(siteId,userId);
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
	public Post[] searchPosts(QueryBean query, String siteId, String userId){
		try{
			return searchManager.searchPosts(query,siteId,userId);
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
	public Image getImage(String imageId, int size){
		try{
			return searchManager.getImage(imageId, size);
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
	public File getFile(String fileId){
		try{
			return searchManager.getFile(fileId);
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}

}
