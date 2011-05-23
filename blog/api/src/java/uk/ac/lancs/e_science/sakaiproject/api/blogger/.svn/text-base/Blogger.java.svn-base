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

package uk.ac.lancs.e_science.sakaiproject.api.blogger;



import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Comment;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.File;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Image;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.searcher.QueryBean;


public interface Blogger {
	//these values are used to define what image size must be loaded by getImage method
	public final int ALL=0;
	public final int THUMBNAIL=1;
	public final int WEB=2;
	public final int ORIGINAL=3;
	
	public void storePost(Post post, String userId, String siteId);
	public void addCommentToPost(Comment comment, String postId, String userId, String siteId);
	public void deletePost(String postId, String userId);
	public Post getPost(String postId, String userId);
	//SAK-14611
	public Post getPost(String postId);
	public Post[] getPosts(String siteId, String userId);
	public Post[] searchPosts(QueryBean query, String siteId, String userId);
	public Image  getImage(String imageId, int size);
	public File  getFile(String fileId);

}
