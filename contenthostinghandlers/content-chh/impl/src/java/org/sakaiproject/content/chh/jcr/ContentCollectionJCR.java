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

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingHandlerResolver;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.GroupAwareEdit;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.ResourceProperties;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.PropertyIterator;
import javax.jcr.NodeIterator;

/**
 * A concrete class representing virtual content entities corresponding to JCR directories.
 * 
 * @author johnf (johnf@caret.cam.ac.uk)
 */
public class ContentCollectionJCR extends ContentEntityJCR implements ContentCollection
{
	private static final Log log = LogFactory.getLog(ContentCollectionJCR.class);

	public ContentCollectionJCR(ContentEntity realParent, String basePath, String relativePath, ContentHostingHandlerImplJCR chh, ContentHostingHandlerResolver resolver,boolean searchable, String username,String password,String vwr)
	{
		super(realParent, basePath,
				(relativePath.length() > 0 && relativePath.charAt(relativePath.length() - 1) != '/')
				 ? relativePath + "/" : relativePath,  /* IDs of Collections MUST end with a "/" */
				chh, resolver,searchable,
				username,password,vwr);
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
//				tmp = tmp.substring(0, tmp.length() - 1); // remove trailing "/" as that causes 2 /'s to appear in the breadcrumbs trail
			}
		}
		wrappedMe.getProperties().addProperty(ResourceProperties.PROP_DISPLAY_NAME, tmp);
		wrappedMe.getProperties().addProperty(ResourceProperties.PROP_CREATOR, ""); // not supported on JCRs
/* TODO! */		wrappedMe.getProperties().addProperty(ResourceProperties.PROP_MODIFIED_DATE, "");
		wrappedMe.getProperties().addProperty(ResourceProperties.PROP_ORIGINAL_FILENAME, this.basePath + this.virtualWorldRoot + this.relativePath);
		wrappedMe.getProperties().addProperty(ResourceProperties.PROP_DESCRIPTION, virtualWorldRoot+this.relativePath);

		// collection-only properties
		wrappedMe.getProperties().addProperty(ResourceProperties.PROP_IS_COLLECTION, Boolean.TRUE.toString());

		// load resources from meta data spool file (which might overwrite some of the defaults above)
		Map metaprops = loadProperties();
		for (Iterator i = metaprops.keySet().iterator() ; i.hasNext() ;)
		{
			String prop = (String) i.next();
			wrappedMe.getProperties().addProperty(prop,(String)metaprops.get(prop));
		}
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
	 * Returns a list of String Ids of the members of this collection (i.e. files in this directory).
	 * 
	 * @return a list of the String Ids of the members of this collection (i.e. files in this directory). Returns empty list if this is not a directory, no-longer exists, or is inaccessible.
	 */
	public List getMembers()
	{
		if (!this.searchable) {
			// Trap requests from sakai-search to prevent it digging into the virtual JCR
			// Can anyone think of a non-evil way of doing this check?  I look in the stack trace
			// to find a particular method in the search engine's call signature  :-S
			boolean requestIsFromSakaiSearch = false;
			try {throw new Exception();}catch (Exception e) {
				StackTraceElement te[] = e.getStackTrace();
				if (te != null && te.length>1 && te[te.length-2].getClassName().equals("org.sakaiproject.search.component.service.impl.SearchIndexBuilderWorkerImpl"))
					requestIsFromSakaiSearch = true;
			}
			if (requestIsFromSakaiSearch)
				return new ArrayList();
		}

		try {
			Node n = getNode();
			NodeIterator ni = n.getNodes();
			int cnt = (int)ni.getSize();
			ArrayList<String> mems = new ArrayList<String>(cnt);
			String newpath = getId();
			if (newpath.charAt(newpath.length() - 1) != '/') newpath = newpath + "/";
			for (int x = 0; x < cnt; ++x)
			{
				String nodeRelPath = ni.nextNode().getName();
				if (ContentHostingHandlerImplJCR.SAKAI_IS_COLLECTION.equals(nodeRelPath)) continue; // skip the "magic" collection marker
				String nodePath = newpath + nodeRelPath;
				if (n.getNode(nodeRelPath).hasNodes())
					mems.add(nodePath + "/");
				else
					mems.add(nodePath);
			}
			return mems;
		}
		catch (Exception e)
		{
			log.warn("Unable to list members of virtual collection ["+getId()+"] due to ["+e+"]");
		}
		return new ArrayList();
	}

	/**
	 * Access a List of the collections' internal members as full ContentResource or ContentCollection objects.
	 * 
	 * @return a List of the full objects of the members of the collection.
	 */
	public List getMemberResources()
	{
		List l = getMembers();
		ArrayList<Edit> resources = new ArrayList<Edit>(l.size());
		for (Iterator i = l.iterator(); i.hasNext();)
		{
			ContentEntity ce = getMember((String) (i.next()));
			if (ce instanceof ContentResource) resources.add(((ContentResourceJCR) ce).wrap());
		}
		return resources;
	}

	/**
	 * Access the size of all the resource body bytes within this collection in Kbytes.
	 * 
	 * @return The size of all the resource body bytes within this collection in Kbytes.
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
				ContentResource crjcr = (ContentResource) o;
				if (crjcr instanceof ContentResourceJCR)
					totalsize += ((ContentResourceJCR) crjcr).getContentLengthLong();
				else
					totalsize += crjcr.getContentLength();
			}
		}
		return totalsize / 1024L;
	}

	public int getMemberCount()
	{
		int count=0;
		try {
			Node n = getNode();
			NodeIterator ni = n.getNodes();

			int cnt = (int)ni.getSize();
			for (int x = 0; x < cnt; ++x)
			{
				String nodeRelPath = ni.nextNode().getName();
				if (ContentHostingHandlerImplJCR.SAKAI_IS_COLLECTION.equals(nodeRelPath)) continue; // skip the "magic" collection marker
				++count;
			}
		} catch (Exception e) {}
		return count;
	}

	public String getResourceType()
	{
		return ResourceType.TYPE_FOLDER;
	}
}
