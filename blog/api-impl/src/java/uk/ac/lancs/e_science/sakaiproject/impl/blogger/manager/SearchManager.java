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

import java.util.List;

import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.File;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Image;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.searcher.QueryBean;
import uk.ac.lancs.e_science.sakaiproject.impl.blogger.searcher.SearchEngine;
import uk.ac.lancs.e_science.sakaiproject.impl.blogger.searcher.SearchException;

public class SearchManager {
	
	private SecurityManager securityManager;
	private SearchEngine searchEngine;
	
	public SearchManager() throws SearchException{
		try{
			securityManager = new SecurityManager();
			searchEngine = new SearchEngine();
		} catch (Exception e){
			throw new SearchException();
		}
	}
	
	public Post getPost(String postId, String userId) throws SearchException{
        return securityManager.filterSearch(userId,searchEngine.getPost(postId));
	}
	//SAK-14611 do not use. naughty!
	public Post getPost(String postId) throws SearchException{
        return searchEngine.getPost(postId);
	}
	public Post[] getPosts(String siteId, String userId) throws SearchException{
		List result = securityManager.filterSearch(userId,searchEngine.getAllPost(siteId));
		if (result.size()==0)
			return null;
		return (Post[])result.toArray(new Post[0]);
	}
	public Post[] searchPosts(QueryBean query, String siteId, String userId) throws SearchException{
		List result = securityManager.filterSearch(userId,searchEngine.doSearch(query, siteId)); 
		if (result.size()==0)
			return null;
        return (Post[]) result.toArray(new Post[0]);
	}
	public Image getImage(String imageId, int size) throws SearchException{
		return searchEngine.getImage(imageId,size);
	}
	public File getFile(String fileId) throws SearchException{
		return searchEngine.getFile(fileId);
		
	}	
}
