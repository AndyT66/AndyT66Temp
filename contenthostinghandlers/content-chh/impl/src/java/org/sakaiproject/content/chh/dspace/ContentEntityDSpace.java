/**********************************************************************************
 * $URL: https://saffron.caret.cam.ac.uk/svn/projects/Content/tags/contenthostinghandlers-clabs/content-chh/impl/src/java/org/sakaiproject/content/chh/dspace/ContentEntityDSpace.java $
 * $Id: ContentEntityDSpace.java 6055 2008-02-28 09:46:08Z johnf $
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

import java.util.Collection;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingHandler;
import org.sakaiproject.content.api.ContentHostingHandlerResolver;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.time.api.Time;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * An abstract superclass representing virtual content entities corresponding to
 * DSpace entities.
 * 
 * @author johnf (johnf@caret.cam.ac.uk)
 */
public abstract class ContentEntityDSpace implements ContentEntity
{
	private static final Log log = LogFactory.getLog(ContentEntityDSpace.class);

	/**
	 * Handle of this entity. This is intended to be used as a virtual content
	 * entity.
	 */
	protected DSpaceItemInfo dii;

	protected ContentEntity realParent;

	/**
	 * A stored copy of the info that was used to construct this object
	 * initially. This name might be out-of-date if the resource has been
	 * renamed since creation.
	 */
	protected String endpoint; // URI of DSpace server

	protected String basehandle;// handle of root community in DSpace

	protected String relativePath;

	protected String parentRelativePath;

	protected boolean searchable;

	/**
	 * Object reference to the content hosting handler which looks after this
	 * virtual content resource.
	 */
	protected ContentHostingHandlerImplDSpace chh;

	/**
	 * Wrapped version of itself (a base content edit)
	 */
	protected Edit wrappedMe;

	protected ContentHostingHandlerResolver resolver;

	abstract protected Edit wrap();

	abstract protected void setVirtualProperties();

	/**
	 * Constructs a new instance
	 * 
	 * @param filename -
	 *        filename or URI of the file or directory to represent
	 */
	public ContentEntityDSpace(ContentEntity realParent, String endpoint,
			String basehandle, String relativePath, ContentHostingHandlerImplDSpace chh,
			ContentHostingHandlerResolver resolver, DSpaceItemInfo dii, boolean searchable)
	{
		this.realParent = realParent;
		this.endpoint = endpoint;
		this.basehandle = basehandle;
		this.relativePath = relativePath;
		this.chh = chh;
		this.dii = dii;
		this.searchable = searchable;
		this.resolver = resolver;

		int lastSlash = relativePath.lastIndexOf('/');
		if (lastSlash < 1)
		{
			/*
			 * PROBLEM: getContainingCollection must return a Collection but
			 * what do we want to return when you recurse out of the top of the
			 * virtual object tree? We can't return the realParent since that is
			 * not a Collection. One choice is to make the root of the virtual
			 * tree a parent of itself, and that is what we do. Other than
			 * changing the return type of getContainingCollection there is no
			 * nice solution to this problem.
			 */
			this.parentRelativePath = "/"; // root cyclically parents
			// itself :-S
		}
		else
		{
			this.parentRelativePath = relativePath.substring(0, lastSlash);
		}
	}

	/**
	 * @return enclosing collection
	 */
	public ContentCollection getContainingCollection()
	{
		return (ContentCollection) chh.resolveDSpace(realParent, endpoint, basehandle,
				parentRelativePath, chh, searchable);
	}

	/**
	 * @return Object reference to the content hosting handler which looks after
	 *         this virtual content resource.
	 */
	public ContentHostingHandler getContentHandler()
	{
		return chh;
	}

	public void setContentHandler(ContentHostingHandler chh)
	{

		if (chh instanceof ContentHostingHandlerImplDSpace)
		{
			this.chh = (ContentHostingHandlerImplDSpace) chh;
		}
		else
		{
			log.error("ContentHostingHandler is not a ContentHostingHandlerImplDSpace instance");
		}
	} // re-parent a virtual entity?! you probably don't want to call this!

	public ContentEntity getVirtualContentEntity()
	{
		return this;
	} // method is used by BaseResourceEdit, not really useful here

	public void setVirtualContentEntity(ContentEntity ce)
	{
	} // method is used by BaseResourceEdit, not really useful here

	/**
	 * Returns true unless the represented file is a directory. Note that this
	 * returns true if the file/directory does not exist or no-longer exists.
	 * The response is not cached and will reflect the current state of the
	 * filesystem represented at all times.
	 * 
	 * @return true if the path/URI is a file, false if it exists but is a
	 *         non-file object, true if inaccessible/no-longer exists.
	 */
	abstract public boolean isResource();

	/**
	 * Returns true if the represented path is a directory. The response is not
	 * cached and will reflect the current state of the filesystem represented
	 * at all times.
	 * 
	 * @return true if path/URI is a directory, false otherwise.
	 */
	abstract public boolean isCollection();

	public ContentEntity getMember(String nextId)
	{
		String newpath = nextId.substring(realParent.getId().length());
		// cut real parent's ID off the start of the string
		return chh.resolveDSpace(realParent, endpoint, basehandle, newpath, chh,
				searchable);
	}

	/* Junk required by GroupAwareEntity superinterface */
	public Collection getGroups()
	{
		return realParent.getGroups();
	}

	public Collection getGroupObjects()
	{
		return realParent.getGroupObjects();
	}

	public AccessMode getAccess()
	{
		return realParent.getAccess();
	}

	public Collection getInheritedGroups()
	{
		return realParent.getInheritedGroups();
	}

	public Collection getInheritedGroupObjects()
	{
		return realParent.getInheritedGroupObjects();
	}

	public AccessMode getInheritedAccess()
	{
		return realParent.getInheritedAccess();
	}

	public Time getReleaseDate()
	{
		return realParent.getReleaseDate();
	}

	public Time getRetractDate()
	{
		return realParent.getRetractDate();
	}

	public boolean isHidden()
	{
		return realParent.isHidden();
	}

	public boolean isAvailable()
	{
		return realParent.isAvailable();
	}

	/* Junk required by Entity superinterface */
	private String join(String base, String extension)
	{ // joins two strings with precisely one / between them
		while (base.length() > 0 && base.charAt(base.length() - 1) == '/')
			base = base.substring(0, base.length() - 1);
		while (extension.length() > 0 && extension.charAt(0) == '/')
			extension = extension.substring(1);
		return base + "/" + extension;
	}

	public String getUrl()
	{
		return join(realParent.getUrl(), relativePath);
	}

	public String getUrl(boolean b)
	{
		return join(realParent.getUrl(b), relativePath);
	}

	public String getReference()
	{
		return join(realParent.getReference(), relativePath);
	}

	public String getUrl(String rootProperty)
	{
		return join(realParent.getUrl(rootProperty), relativePath);
	}

	public String getReference(String rootProperty)
	{
		return join(realParent.getReference(rootProperty), relativePath);
	}

	public String getId()
	{
		return join(realParent.getId(), relativePath);
	}

	public ResourceProperties getProperties()
	{
		return realParent.getProperties();
	}

	public Element toXml(Document doc, Stack stack)
	{
		return realParent.toXml(doc, stack);
	}
}
