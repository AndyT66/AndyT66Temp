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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.lancs.e_science.sakai.tools.blogger.cacheForImages.CacheForImages;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.Blogger;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Image;
import uk.ac.lancs.e_science.sakaiproject.impl.blogger.BloggerManager;

public class ServletForImages extends HttpServlet { 
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
		
		Image image=null;
		String size = request.getParameter("size");
		if (size!=null && size.equals("original"))
			image = blogger.getImage(request.getParameter("idImage"),Blogger.ORIGINAL);	
		else if (size!=null && size.equals("thumbnail"))
			image = blogger.getImage(request.getParameter("idImage"),Blogger.THUMBNAIL);	
		else
			image = blogger.getImage(request.getParameter("idImage"),Blogger.WEB);	
		
		if (image==null){ //maybe, the image is in a post in memory. This happends when the post is being builded
			CacheForImages imageCache = CacheForImages.getInstance();
			image = imageCache.getImage(request.getParameter("idImage"));
			if (image==null)
				return;
				
			
		}
		BufferedImage bimage;
		if (size!=null && size.equals("original"))
			bimage = ImageIO.read(new ByteArrayInputStream(image.getContent()));
		else if (size!=null && size.equals("thumbnail"))
			bimage = ImageIO.read(new ByteArrayInputStream(image.getImageContentWithThumbnailSize()));
		else
			bimage = ImageIO.read(new ByteArrayInputStream(image.getImageContentWithWebSize()));
		
		if (bimage!=null){
			OutputStream out = response.getOutputStream();
			response.setContentType("image/jpg");
			ImageIO.write(bimage,"jpg",out);
			out.close();
		} else {
			//System.out.println("Image is null!!!!");
		}
		
	}
	
	
}
