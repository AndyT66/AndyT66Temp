/**********************************************************************************
 * $URL: https://saffron.caret.cam.ac.uk/svn/projects/Content/tags/contenthostinghandlers-clabs/content-chh/impl/src/java/org/sakaiproject/content/chh/dspace/ContentResourceDSpace.java $
 * $Id: ContentResourceDSpace.java 5454 2007-10-09 15:54:01Z johnf $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.chh.dspace;

import java.io.InputStream;
import java.net.URL;

import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingHandlerResolver;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.GroupAwareEdit;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.ServerOverloadException;

/**
 * A concrete class representing virtual content entities corresponding to
 * filesystem files.
 * 
 * @author johnf (johnf@caret.cam.ac.uk)
 */
public class ContentResourceDSpace extends ContentEntityDSpace implements
		ContentResource
{
	public ContentResourceDSpace(ContentEntity realParent, String endpoint,
			String basehandle, String relativePath, ContentHostingHandlerImplDSpace chh,
			ContentHostingHandlerResolver resolver,
			DSpaceItemInfo dii, boolean searchable)
	{
		super(realParent, endpoint, basehandle, relativePath, chh, resolver, dii, searchable);
	}

	/**
	 * Converts this resource to a collection. This is necessary when a new
	 * collection is created -- the handler represents files/directories
	 * WHICH DO NOT EXIST as RESOURCES and hence, when we create a new
	 * collection, we have to convert the resource which initially
	 * represents it to a collection so it can be committed.
	 * 
	 * @return a collection representing the same path in the file system as
	 *         this resource
	 */
	ContentCollectionDSpace convertToCollection()
	{
		ContentCollectionDSpace ccds = new ContentCollectionDSpace(realParent,
				endpoint, basehandle, relativePath, chh, resolver, dii, searchable);

		// this gets miscomputed because the new collection name has a "/" appended
		ccds.parentRelativePath = this.parentRelativePath;

		return ccds;
	}

	protected Edit wrap()
	{
		if (wrappedMe == null)
		{
			wrappedMe = resolver.newResourceEdit(getId());

			if (wrappedMe != null)
			{
				// link it back to this CHH
				((ContentEntity) wrappedMe).setContentHandler(chh);
				((ContentEntity) wrappedMe).setVirtualContentEntity(this);
	
				// set the resource type
				((GroupAwareEdit) wrappedMe).setResourceType(this.getResourceType());
	
				// copy properties from real parent, then overwrite specific properties
				wrappedMe.getProperties().addAll(((Edit)realParent).getProperties());
				setVirtualProperties();
				wrappedMe.getProperties().removeProperty(ContentHostingHandlerResolver.CHH_BEAN_NAME);
			}
		}
		return wrappedMe;
	}

	protected void setVirtualProperties()
	{
		// set the properties required for a sensible display in the
		// resources list view
		String tmp;
		if (this.relativePath.equals("/"))
			tmp = this.basehandle;
		else
		{
			int lastSlash = this.relativePath.lastIndexOf("/", this.relativePath
					.length() - 2);
			// if there are no slashes, the display name should be the whole
			// relative path
			if (lastSlash == -1)
				tmp = this.relativePath;
			else
				tmp = this.relativePath.substring(lastSlash).substring(1);
		}
		wrappedMe.getProperties()
				.addProperty(
						ResourceProperties.PROP_DISPLAY_NAME,
						tmp + " (" + dii.contentLength + " bytes of " + dii.contentType + ")");
		wrappedMe.getProperties().addProperty(ResourceProperties.PROP_CREATOR, ""); // not supported
		wrappedMe.getProperties().addProperty(ResourceProperties.PROP_MODIFIED_DATE,dii.lastmodified);
		wrappedMe.getProperties().addProperty(ResourceProperties.PROP_ORIGINAL_FILENAME,this.basehandle + this.relativePath);
		wrappedMe.getProperties().addProperty(ResourceProperties.PROP_DESCRIPTION,this.relativePath);

		// resource-only properties
		wrappedMe.getProperties().addProperty(ResourceProperties.PROP_CONTENT_LENGTH,"" + dii.contentLength);
		wrappedMe.getProperties().addProperty(ResourceProperties.PROP_IS_COLLECTION,Boolean.FALSE.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isResource()
	{
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isCollection()
	{
		return false;
	}

	/**
	 * Returns the length of the file represented by the path or 0 if the
	 * file is not found or is inaccessible. Note that the maximum
	 * filelength is 2^31-1 bytes (the size of a java int) because the
	 * ContentResource::getContentLength API requires that the return type
	 * be an integer, not a long. See also getContentLengthLong().
	 * 
	 * @return length of the file, subject to the file not exceeding 2^31-1
	 *         bytes; -1 if the file does not exist (or is now a directory).
	 */
	public int getContentLength()
	{
		return (int) dii.contentLength;
	}

	/**
	 * Returns the length of the file represented by the path or 0 if the
	 * file is not found or is inaccessible. See also getContentLength().
	 * 
	 * @return length of the file
	 */
	public long getContentLengthLong()
	{
		return dii.contentLength;
	}

	/**
	 * Returns the mimetype of the file represented
	 * 
	 * @return MIME type of the file represented
	 */
	public String getContentType()
	{
		return dii.contentType;
	}

	/**
	 * Retrieves and returns the contents of the file
	 * 
	 * @return An array containing the data of the file or null if the file
	 *         is not found or is not accessible
	 */
	public byte[] getContent() throws ServerOverloadException
	{
		try
		{
			InputStream fis = streamContent();
			if (fis == null) return null;
			byte[] b = new byte[fis.available()];
			fis.read(b);
			fis.close();
			fis = null; // try to close the stream to conserve resources
			return b;
		}
		catch (Exception e)
		{
			return null; // file in unavailable / no-longer exists, so
			// return null
		}
	}

	/**
	 * Returns an Input Stream from which the contents of the file can be
	 * streamed, or null if the file does not exist or is inaccessible.
	 */
	public InputStream streamContent() throws ServerOverloadException
	{
		try
		{
			return new URL(dii.getDownloadURL()).openStream();
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public String getResourceType()
	{
		return ResourceType.TYPE_UPLOAD;
	}
}
