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
package uk.ac.lancs.e_science.sakai.tools.blogger.servlet;


import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import uk.ac.lancs.e_science.sakaiproject.api.blogger.Blogger;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.File;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;
import uk.ac.lancs.e_science.sakaiproject.impl.blogger.BloggerManager;

public class ServletForFiles extends HttpServlet { 
	/**
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest, HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAction(request, response);
	}

	/**
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAction(request, response);
	}
	private void doAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		Blogger blogger = BloggerManager.getBlogger();
		uk.ac.lancs.e_science.sakaiproject.api.blogger.post.File file = blogger.getFile(request.getParameter("fileId"));
		if (file!=null){ 
			//SAK-14611 - get description of file from post rather than request param
			Post post = blogger.getPost(file.getPostId());
			
			String fileDescription = "";
			for(File testFile : post.getFiles())
			{
				if(testFile.getIdFile().equals(file.getIdFile()))
				{
					fileDescription = testFile.getDescription();
					break;
				}
			}
			
			//set the headers into the request. "application/octet-stream means that this is a binary file
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition","attachment;filename="+fileDescription);
			//Write the file to the output stream
			//TODO: what if file.getContent returns null?
			response.setContentLength(file.getContent().length);
			response.getOutputStream().write(file.getContent());
			response.getOutputStream().flush();
			response.setHeader("Cache-Control","no-cache");
		}
	}
	
	
}

