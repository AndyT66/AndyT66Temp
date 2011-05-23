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

import org.sakaiproject.util.FormattedText;

import uk.ac.lancs.e_science.sakaiproject.api.blogger.Blogger;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.SakaiProxy;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Comment;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;
import uk.ac.lancs.e_science.sakaiproject.impl.blogger.BloggerManager;

public class AddCommentController extends BloggerController
{
	private Post post;

	private String commentText;

	private Blogger blogger;

	public AddCommentController()
	{
		super();
		blogger = BloggerManager.getBlogger();
	}

	public void setPost(Post post)
	{
		this.post = post;
	}

	public Post getPost()
	{
		return post;
	}

	public void setCommentText(String text)
	{
		commentText = text;
	}

	public String getCommentText()
	{
		return ""; // initially, the comment text is empty.
	}

	public String doSaveComment()
	{
		if (isEmpty(commentText))
			return "viewPost"; // ignore the comment
		
		StringBuilder errorMessages = new StringBuilder();
		commentText = FormattedText.processFormattedText(commentText, errorMessages, true, false);

		Comment comment = new Comment(commentText);
		blogger.addCommentToPost(comment, post.getOID(), SakaiProxy.getCurrentUserId(), SakaiProxy.getCurrentSiteId());
		return "viewPost";

	}

	private boolean isEmpty(String str)
	{
		str = str.replaceAll("<br />", "");
		str = str.replaceAll("&nbsp;", "");
		str = str.replaceAll(" ", "");
		str = str.trim();
		for (int i = 0; i < str.length(); i++)
		{// some estrange characteres come from the htmleditor that are not cleaned by trim but the debugger shows them as white space
			Character ch = new Character(str.charAt(i));
			if (!Character.isSpaceChar(str.charAt(i)))
				return false;
		}
		return true;
	}
}
