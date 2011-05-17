/**********************************************************************************
 * $URL: https://saffron.caret.cam.ac.uk/svn/projects/Content/tags/contenthostinghandlers-clabs/content-chh/impl/src/java/org/sakaiproject/content/chh/dspace/ContentCollectionDSpace.java $
 * $Id: ContentCollectionDSpace.java 5454 2007-10-09 15:54:01Z johnf $
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingHandlerResolver;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.GroupAwareEdit;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.ResourceProperties;

/**
 * @author ieb
 *
 */
/**
 * A concrete class representing virtual content entities corresponding to
 * filesystem directories.
 * 
 * @author johnf (johnf@caret.cam.ac.uk)
 */
public class ContentCollectionDSpace extends ContentEntityDSpace implements
		ContentCollection
{
	public ContentCollectionDSpace(ContentEntity realParent, String endpoint,
			String basehandle, String relativePath, ContentHostingHandlerImplDSpace chh,
			ContentHostingHandlerResolver resolver, DSpaceItemInfo dii, boolean searchable )
	{
		super(realParent, endpoint, basehandle,
				(relativePath.length() == 0 || relativePath.charAt(relativePath
						.length() - 1) != '/') ? relativePath + "/" : relativePath,
				chh, resolver, dii, searchable);
	}

	protected Edit wrap()
	{
		if (wrappedMe == null)
		{
			wrappedMe = resolver.newCollectionEdit(getId());
			
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
	
				// remove mountpoint property, except on the root node of the virtual world (needed to unmount)
				if (!this.relativePath.equals("/"))
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
			if (this.relativePath.lastIndexOf("/", this.relativePath.length() - 2) == -1)
				tmp = this.relativePath;
			else
				tmp = this.relativePath.substring(
						this.relativePath.lastIndexOf("/",
								this.relativePath.length() - 2)).substring(1);
			if (tmp.length() > 0 && tmp.charAt(tmp.length() - 1) == '/')
				tmp = tmp.substring(0, tmp.length() - 1); // remove
			// trailing "/"
			// as that
			// causes 2 /'s
			// to appear in
			// the
			// breadcrumbs
			// trail
		}
		wrappedMe.getProperties().addProperty(ResourceProperties.PROP_DISPLAY_NAME,
				tmp);
		wrappedMe.getProperties().addProperty(ResourceProperties.PROP_CREATOR, ""); // not
		// supported
		wrappedMe.getProperties().addProperty(ResourceProperties.PROP_MODIFIED_DATE,
				""); // not supported on collections/communities
		wrappedMe.getProperties().addProperty(
				ResourceProperties.PROP_ORIGINAL_FILENAME,
				this.basehandle + this.relativePath);
		wrappedMe.getProperties().addProperty(ResourceProperties.PROP_DESCRIPTION,
				this.relativePath);

		// collection-only properties
		wrappedMe.getProperties().addProperty(ResourceProperties.PROP_IS_COLLECTION,
				Boolean.TRUE.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isResource()
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isCollection()
	{
		return true;
	}

	/**
	 * Returns a list of String Ids of the members of this collection (i.e.
	 * files in this directory).
	 * 
	 * @return a list of the String Ids of the members of this collection
	 *         (i.e. files in this directory). Returns empty list if this is
	 *         not a directory, no-longer exists, or is inaccessible.
	 */
	public List getMembers()
	{
		if (!this.searchable)
		{
			// Trap requests from sakai-search to prevent it digging into
			// the virtual filesystem
			// Can anyone think of a non-evil way of doing this check? I
			// look in the stack trace
			// to find a particular method in the search engine's call
			// signature :-S
			boolean requestIsFromSakaiSearch = false;
			try
			{
				throw new Exception();
			}
			catch (Exception e)
			{
				StackTraceElement te[] = e.getStackTrace();
				if (te != null
						&& te.length > 1
						&& te[te.length - 2]
								.getClassName()
								.equals(
										"org.sakaiproject.search.component.service.impl.SearchIndexBuilderWorkerImpl"))
					requestIsFromSakaiSearch = true;
			}
			if (requestIsFromSakaiSearch) return new ArrayList();
		}

		List l = chh.listDSpaceItemsIn(
				this.endpoint, this.dii.handle);
		if (l == null) return new ArrayList(0);
		List<String> result = new ArrayList<String>(l.size());
		for (Iterator i = l.iterator(); i.hasNext();)
		{
			String name = (String) (i.next());
			result.add(this.realParent.getId() + this.relativePath + name);
		}
		return result;
	}

	/**
	 * Access a List of the collections' internal members as full
	 * ContentResource or ContentCollection objects.
	 * 
	 * @return a List of the full objects of the members of the collection.
	 */
	public List getMemberResources()
	{
		List l = getMembers();
		List<Edit> resources = new ArrayList<Edit>(l.size());
		for (Iterator i = l.iterator(); i.hasNext();)
		{
			ContentEntity ce = getMember((String) (i.next()));
			if (ce instanceof ContentResource)
				resources.add(((ContentResourceDSpace) ce).wrap());
		}
		return resources;
	}

	/**
	 * Access the size of all the resource body bytes within this collection
	 * in Kbytes.
	 * 
	 * @return The size of all the resource body bytes within this
	 *         collection in Kbytes.
	 */
	public long getBodySizeK()
	{
		long totalsize = 0L;
		List x = getMemberResources();
		for (Iterator i = x.iterator(); i.hasNext();)
		{
			Object o = i.next();
			if ((o != null) && (o instanceof ContentResource))
			{
				ContentResource crds = (ContentResource) o;
				if (crds instanceof ContentResourceDSpace)
					totalsize += ((ContentResourceDSpace) crds)
							.getContentLengthLong();
				else
					totalsize += crds.getContentLength();
			}
		}
		return totalsize / 1024L;
	}

	public int getMemberCount()
	{
		return getMembers().size();
	}

	public String getResourceType()
	{
		return ResourceType.TYPE_FOLDER;
	}
}
