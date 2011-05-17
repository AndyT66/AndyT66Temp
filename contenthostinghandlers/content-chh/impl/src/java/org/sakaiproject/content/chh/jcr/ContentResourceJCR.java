/**********************************************************************************
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

package org.sakaiproject.content.chh.jcr;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.io.SequenceInputStream;
import java.io.ByteArrayInputStream;

import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingHandlerResolver;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.GroupAwareEdit;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.ServerOverloadException;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Value;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.PropertyIterator;
import javax.jcr.NodeIterator;

/**
 * A concrete class representing virtual content entities corresponding to JCR files.
 * 
 * @author johnf (johnf@caret.cam.ac.uk)
 */
public class ContentResourceJCR extends ContentEntityJCR implements ContentResource
{
	public ContentResourceJCR(ContentEntity realParent, String basePath, String relativePath, ContentHostingHandlerImplJCR chh, ContentHostingHandlerResolver resolver, boolean searchable, String username,String password,String vwr)
	{
		super(realParent, basePath, relativePath, chh, resolver, searchable, username, password, vwr);
	}

	/**
	 * Converts this resource to a collection.  This is necessary when a new collection
	 * is created -- the handler represents files/directories WHICH DO NOT EXIST as 
	 * RESOURCES and hence, when we create a new collection, we have to convert the 
	 * resource which initially represents it to a collection so it can be committed.
	 * @return a collection representing the same path in the file system as this resource
	 */
	ContentCollectionJCR convertToCollection() {
		return new ContentCollectionJCR(realParent,basePath,relativePath,chh,resolver,searchable,username,password,virtualWorldRoot);
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
		// load properties from JCR Repo (which might overwrite some of the defaults above)
		Map metaprops = loadProperties();
		for (Iterator i = metaprops.keySet().iterator() ; i.hasNext() ;)
		{
			String prop = (String) i.next();
			wrappedMe.getProperties().addProperty(prop,(String)metaprops.get(prop));
		}

		// set the properties required for a sensible display in the resources list view
		String tmp;
		if (this.relativePath.equals("/") || this.relativePath.equals(""))
			tmp = this.basePath+virtualWorldRoot;
		else
		{
			if (ContentHostingHandlerImplJCR.SHOW_FULL_PATHS)
				tmp = virtualWorldRoot+this.relativePath;
			else
			{
				tmp = virtualWorldRoot+this.relativePath;
				tmp = tmp.substring(tmp.lastIndexOf("/", this.relativePath.length() - 2)).substring(1);
			}
		}
		wrappedMe.getProperties().addProperty(ResourceProperties.PROP_DISPLAY_NAME, tmp);
		wrappedMe.getProperties().addProperty(ResourceProperties.PROP_CREATOR, ""); // not supported on JCRs
/* TODO! */		wrappedMe.getProperties().addProperty(ResourceProperties.PROP_MODIFIED_DATE, "");
		wrappedMe.getProperties().addProperty(ResourceProperties.PROP_ORIGINAL_FILENAME, this.basePath + this.virtualWorldRoot + this.relativePath);
		wrappedMe.getProperties().addProperty(ResourceProperties.PROP_DESCRIPTION, virtualWorldRoot+this.relativePath);

		// resource-only properties
		wrappedMe.getProperties().addProperty(ResourceProperties.PROP_CONTENT_LENGTH, "" + this.getContentLengthLong());
		wrappedMe.getProperties().addProperty(ResourceProperties.PROP_IS_COLLECTION, Boolean.FALSE.toString());
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
	 * Returns the length of the file represented by the path or 0 if the file is not found or is inaccessible. Note that the maximum filelength is 2^31-1 bytes (the size of a java int) because the ContentResource::getContentLength API requires that
	 * the return type be an integer, not a long. See also getContentLengthLong().
	 * 
	 * @return length of the file, subject to the file not exceeding 2^31-1 bytes; -1 if the file does not exist (or is now a directory).
	 */
	public int getContentLength()
	{
		return (int)getContentLengthLong();
	}

	/**
	 * Returns the length of the file represented by the path or 0 if the file is not found or is inaccessible. See also getContentLength().
	 * 
	 * @return length of the file
	 */
	public long getContentLengthLong()
	{
		long tot = 0;
		try
		{
			Node n = getNode();
			if (n == null) return 0L;
			PropertyIterator pi = n.getProperties();
			if (pi == null) return 0L;
			long s = pi.getSize();
			if (s==0) return 0L;

			for (;s>0;--s)
			{
				Property p = pi.nextProperty();
				if (p.getDefinition().isMultiple()) {
					long [] lens = p.getLengths();
					tot += p.getName().length();   // <prop_name>
					tot += 2;                      //            =[
					tot += lens.length-1;          //               n-1 commas (even if some items are null)
					tot += 2;                      //                          ]\n
					for (int i = 0 ; i < lens.length ; ++i) if (lens[i]>0) tot += lens[i];
				} else {
					tot += p.getName().length();   // <prop_name>
					tot += 2;                      //            =  ... \n
					long l = p.getLength();
					if (l>0) tot += l;
				}
			}
		} catch (javax.jcr.RepositoryException e) {}
		return tot;
	}

	/**
	 * Returns the mimetype of the file represented
	 * 
	 * @return MIME type of the file represented
	 */
	public String getContentType()
	{
		return "text/plain"; // TODO
	}

	/**
	 * Retrieves and returns the contents of the file
	 * 
	 * @return An array containing the data of the file or null if the file is not found or is not accessible
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
			return null;  // file is unavailable / no-longer exists, so return null
		}
	}

	/**
	 * Returns an Input Stream from which the contents of the file can be streamed, or null if the file does not exist or is inaccessible.
	 */
	public InputStream streamContent() throws ServerOverloadException
	{ /* NOTE THAT THE LENGTH OF THE STRING GENERATED BY THIS METHOD MUST AGREE WITH getContentLength() !! */
	  /* YOU WILL SEE BROWSER-SPECIFIC PARTIAL RESULTS IF THE CONTENT-LENGTH DISAGREES WITH THE ACTUAL LENGTH !! */
		try
		{
			Node n = getNode();
			if (n == null) return null;
			PropertyIterator pi = n.getProperties();
			if (pi == null) return null;
			long s = pi.getSize();
			if (s==0) return null;

			// we concatenate properties in a way that does NOT cause
			// us to have to load the actual properties into memory,
			// which is important because they could be large files!
			InputStream is = new ByteArrayInputStream(new byte[0]); // a nil-item (base for stream concatenation)
			for (;s>0;--s) {
				Property p = pi.nextProperty();
				if (p.getDefinition().isMultiple()) {
					// generate a stream reading "[<p1>,<p2>,<p3>,...<pn>]"  where "<px>" is a property's own stream
					is = new SequenceInputStream(is,new ByteArrayInputStream((p.getName()+"=[").getBytes()));
					Value [] vals = p.getValues();
					for (int i = 0 ; i < vals.length ; ++i)
					{
						if (i > 0)
						{
							is = new SequenceInputStream(is,new ByteArrayInputStream((",").getBytes()));
						}
						if (vals[i]!=null && vals[i].getStream() != null)
						{
							is = new SequenceInputStream(is,vals[i].getStream());
						}
					}
					is = new SequenceInputStream(is,new ByteArrayInputStream(("]\n").getBytes()));
				}
				else
				{
					is = new SequenceInputStream(is,new ByteArrayInputStream((p.getName()+"=").getBytes()));
					if (p!=null && p.getStream() != null) is = new SequenceInputStream(is,p.getStream());
					is = new SequenceInputStream(is,new ByteArrayInputStream(("\n").getBytes()));
				}
			}
			return is;
		}
		catch (javax.jcr.PathNotFoundException e)
		{
			return null;
		}
		catch (javax.jcr.ValueFormatException e)
		{
			return null;
		}
		catch (javax.jcr.RepositoryException e)
		{
			return null;
		}
	}

	public String getResourceType()
	{
		return ResourceType.TYPE_UPLOAD;
	}
}
