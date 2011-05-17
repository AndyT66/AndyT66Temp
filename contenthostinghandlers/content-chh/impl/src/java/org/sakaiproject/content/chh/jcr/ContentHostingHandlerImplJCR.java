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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingHandler;
import org.sakaiproject.content.api.ContentHostingHandlerResolver;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.chh.file.ContentCollectionFileSystem;
import org.sakaiproject.content.chh.file.ContentEntityFileSystem;
import org.sakaiproject.content.chh.file.ContentResourceFileSystem;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.ServerOverloadException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;		/* NOTE!! First class classed "Node"!! */
import org.w3c.dom.NodeList;

import javax.jcr.Item;
//import javax.jcr.Node;			/* NOTE!! Second class classed "Node"!! */
import javax.jcr.Property;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.PropertyIterator;
import javax.jcr.NodeIterator;

/**
 * Provides a read/write view of a JCR through virtual content hosting.
 * 
 * @author johnf
 */
public class ContentHostingHandlerImplJCR implements ContentHostingHandler
{
	static final String SAKAI_IS_COLLECTION = ".sakai.isCollection";
	private static final String HANDLER_NAME="JCRHandler";
	private static final Log log = LogFactory.getLog(ContentHostingHandlerImplJCR.class);

	public final static String XML_NODE_NAME = "mountpoint";

	public final static String XML_ATTRIBUTE_USERNAME = "username";
	public final static String XML_ATTRIBUTE_PASSWORD = "password";
	public final static String XML_ATTRIBUTE_WORLDROOT = "worldRoot";
	public final static String XML_ATTRIBUTE_NAME = "path";
	public final static String XML_ATTRIBUTE_SEARCHABLE = "searchable";

	public final static boolean SHOW_FULL_PATHS = false; /* list full paths in the file list view */


	/* ---------------------------------------------------- */
	private ContentHostingHandlerResolver contentHostingHandlerResolver = null;
	/**
	 * @return the contentHandlerResover
	 */
	public ContentHostingHandlerResolver getContentHostingHandlerResolver()
	{
		return contentHostingHandlerResolver;
	}

	/**
	 * @param contentHandlerResover the contentHandlerResover to set
	 */
	public void setContentHostingHandlerResolver(ContentHostingHandlerResolver contentHostingHandlerResolver)
	{
		this.contentHostingHandlerResolver = contentHostingHandlerResolver;
	}
    /* ---------------------------------------------------- */

	
	public void cancel(ContentCollectionEdit edit)
	{ /* no work required -- no temporary changes to reverse */
	}

	public void cancel(ContentResourceEdit edit)
	{ /* no work required -- no temporary changes to reverse */
	}

	public void commit(ContentCollectionEdit edit)
	{
		ContentCollectionJCR ccjcr = null;
		if (edit instanceof ContentCollectionJCR)
			ccjcr = (ContentCollectionJCR) edit;
		else
		{
			ContentEntity tmp = edit.getVirtualContentEntity();
			if (tmp instanceof ContentCollectionJCR)
				ccjcr = (ContentCollectionJCR) tmp;
			else if (tmp instanceof ContentResourceJCR)
				ccjcr = ((ContentResourceJCR)tmp).convertToCollection();
		}
		if (ccjcr == null) return; // can't do anything if the resource isn't a JCR resource!

		// save directory
		javax.jcr.Node n = ccjcr.getParentNode();
		try
		{
			String fullid = ccjcr.getId();
			int lastslash = fullid.lastIndexOf("/",fullid.length()-2);
			n.addNode(fullid.substring(lastslash+1));
			n.save();
		}
		catch (SecurityException e)
		{
			log.warn("Content Hosting Handler JCR was unable to save the contents of a collection because the JVM SecurityManager refused the operation: ["+e+"]");
			return;  // permissions error -- operation cannot be performed
		}
		catch (Exception e)
		{
			log.warn("Content Hosting Handler JCR was unable to save the contents of a collection because the JCR Repository failed the request: ["+e+"]");
		}

		// save properties
		saveProperties(edit,ccjcr);
	}

	public void commit(ContentResourceEdit edit)
	{
		ContentResourceJCR crjcr = null;
		if (edit instanceof ContentResourceJCR)
			crjcr = (ContentResourceJCR) edit;
		else
		{
			ContentEntity tmp = edit.getVirtualContentEntity();
			if (tmp instanceof ContentResourceJCR)
				crjcr = (ContentResourceJCR) tmp;
		}
		if (crjcr == null) return; // can't do anything if the resource isn't a JCR resource!

		// create a JCR node for this resource
		javax.jcr.Node np = crjcr.getParentNode();
		try
		{
			String fullid = crjcr.getId();
			int lastslash = fullid.lastIndexOf("/",fullid.length()-2);
			np.addNode(fullid.substring(lastslash+1));
			np.save();
		}
		catch (Exception e)
		{
			// node already exists (OK), or total JCR failure (will recur and be caught+reported later)
System.out.println("Exception ["+e+"] but I think this is non-fatal because you might be editing an existing resource.  Continuing regardless...");
		}

		// save properties
		saveProperties(edit,crjcr);

		// save file contents
		javax.jcr.Node n = crjcr.getNode();
		InputStream is = null;
		try
		{
			is = edit.streamContent();
			if (is != null) {
				n.setProperty("value",is);
				n.save();
			}
		}
		catch (Exception e)
		{
			log.warn("Content Hosting Handler JCR was unable to save the contents of a resource because the JCR Repository failed the request.");
			return;  // often a permissions error -- operation cannot be performed
		}
		finally
		{
			if (is != null) try {is.close();} catch (IOException e) {}
		}
	}

	public void commitDeleted(ContentResourceEdit edit, String uuid)
	{ /* No need to do anything -- removeResource/removeCollection does the work */
	}

	public List getCollections(ContentCollection collection)
	{
		ContentEntity cc = collection.getVirtualContentEntity();
		if (!(cc instanceof ContentCollectionJCR))
		{
			return null; // this is not the correct handler for this resource -- serious problems!
		}
		ContentCollectionJCR ccjcr = (ContentCollectionJCR) cc;
		List l = ccjcr.getMembers();
		ArrayList<Edit> collections = new ArrayList<Edit>(l.size());
		for (Iterator i = l.listIterator(); i.hasNext();)
		{
			String id = (String) i.next();
			ContentEntityJCR cejcr = resolveToFileOrDirectory(ccjcr.realParent, ccjcr.basePath,
					id.substring(ccjcr.realParent.getId().length() + 1), this, ccjcr.searchable,
					ccjcr.username,ccjcr.password,ccjcr.virtualWorldRoot);
			if (cejcr instanceof ContentCollectionJCR) collections.add(cejcr.wrap());
		}
		return collections;
	}


	public List getFlatResources(ContentEntity ce)
	{
System.out.println("getFlatResources");
		return null;
	}

	public byte[] getResourceBody(ContentResource resource) throws ServerOverloadException
	{
		if (!(resource instanceof ContentResourceJCR)) return null;
		ContentResourceJCR crjcr = (ContentResourceJCR) resource;
		return crjcr.getContent();
	}

	public List getResources(ContentCollection collection)
	{
		ContentEntity cc = collection.getVirtualContentEntity();
		if (!(cc instanceof ContentCollectionJCR))
		{
			return null; // this is not the correct handler for this resource -- serious problems!
		}
		ContentCollectionJCR ccjcr = (ContentCollectionJCR) cc;
		List l = ccjcr.getMemberResources();
		return l;
	}
	protected ContentEntityJCR resolveToFileOrDirectory(ContentEntity realParent, String basePath, String relativePath,
			ContentHostingHandlerImplJCR chh,boolean searchable, String username,String password,String virtWorldRoot)
	{
		// return a file (resource) or a directory (collection) as appropriate
		while (relativePath.length() > 0 && relativePath.charAt(0) == '/')
			relativePath = relativePath.substring(1);
		while (relativePath.length() > 0 && relativePath.charAt(relativePath.length() - 1) == '/')
			relativePath = relativePath.substring(0, relativePath.length() - 1);

		javax.jcr.Node n = null;
		boolean hasChildren = false;
		try {
			n = ContentEntityJCR.getRootNode(username,password,basePath,virtWorldRoot);
			// get node below the root (unless asked for root itself)
			if (relativePath.length()>0) n = n.getNode(relativePath);
			hasChildren = n!=null && n.hasNodes();
		}
		catch (javax.jcr.PathNotFoundException e)
		{
			// ignore
		}
		catch (javax.jcr.RepositoryException e)
		{
			// ignore
		}
		if (n!=null && hasChildren)
		{
			ContentEntityJCR cejcr = new ContentCollectionJCR(realParent, basePath, relativePath, chh, contentHostingHandlerResolver, searchable,username,password,virtWorldRoot);
			cejcr.wrap();
			return cejcr;
		}
		else
		{
			ContentEntityJCR cejcr = new ContentResourceJCR(realParent, basePath, relativePath, chh, contentHostingHandlerResolver, searchable,username,password,virtWorldRoot);
			cejcr.wrap();
			return cejcr;
		}
	}


	public ContentEntity getVirtualContentEntity(ContentEntity edit, String finalId)
	{
		// Algorithm: get the mount point from the XML file represented by 'edit'
		// construct a new ContentEntityJCR and return it
		try
		{
			boolean searchable = false;	// allow the sakai-search tool to index the virtual hierarchy?
			String username = "";		// credentials for JCR repo
			String password = "";		// credentials for JCR repo
			String virtWorldRoot = "/";	// relative path within the JCR repo that you want to expose

			byte[] xml = ((ContentResource) edit).getContent();
			if (xml == null) return null;
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			if (db == null) return null;
			Document d = db.parse(new ByteArrayInputStream(xml));
			if (d == null) return null;
			Node node_mountpoint = null;
			NodeList nl = d.getChildNodes();
			for (int j = 0; j < nl.getLength(); ++j)
				if (nl.item(j).getNodeName() != null && nl.item(j).getNodeName().equals(XML_NODE_NAME))
				{
					node_mountpoint = nl.item(j);
					break;
				}
			if (node_mountpoint == null) return null;

			Node node_username = node_mountpoint.getAttributes().getNamedItem(XML_ATTRIBUTE_USERNAME);
			if (node_username == null) return null;
			username = node_username.getNodeValue();

			Node node_password = node_mountpoint.getAttributes().getNamedItem(XML_ATTRIBUTE_PASSWORD);
			if (node_password == null) return null;
			password = node_password.getNodeValue();

			Node node_vwr = node_mountpoint.getAttributes().getNamedItem(XML_ATTRIBUTE_WORLDROOT);
			if (node_vwr == null) return null;
			virtWorldRoot = node_vwr.getNodeValue();

			Node node_basepath = node_mountpoint.getAttributes().getNamedItem(XML_ATTRIBUTE_NAME);
			if (node_basepath == null) return null;
			final String basepath = node_basepath.getNodeValue();

			Node node_searchable = node_mountpoint.getAttributes().getNamedItem(XML_ATTRIBUTE_SEARCHABLE);
			if (node_searchable != null)
				searchable = Boolean.parseBoolean(node_searchable.getNodeValue());

			if (basepath == null || basepath.equals("")) return null; // invalid mountpoint specification
			String relativePath = finalId.substring(edit.getId().length());
			ContentEntityJCR cejcr = resolveToFileOrDirectory(edit, basepath, relativePath, this, searchable, username, password, virtWorldRoot);
			Edit ce = cejcr.wrap();
			if (ce == null) return null; // happens when the requested URL requires a log on but the user is not logged on
			return (ContentEntity) ce;
		}
		catch (Exception e)
		{
			log.warn("Invalid XML for the mountpoint ["+edit.getId()+"], error is ["+e+"]");
			return (ContentEntity)(new ContentResourceJCR(edit, "/ERROR:invalid_sakai_mount_point", "/", this, contentHostingHandlerResolver, false,"","","/").wrap());
		}
	}


	public void removeCollection(ContentCollectionEdit edit)
	{
		ContentCollectionJCR ccjcr = null;
		if (edit instanceof ContentCollectionJCR)
			ccjcr = (ContentCollectionJCR) edit;
		else
		{
			ContentEntity tmp = edit.getVirtualContentEntity();
			if (tmp instanceof ContentCollectionJCR)
				ccjcr = (ContentCollectionJCR) tmp;
		}
		if (ccjcr == null) return; // can't do anything if the resource isn't a JCR collection!
		if (!ccjcr.removeNode())
			log.warn("Content Hosting Handler JCR was unable to delete a collection because the JCR Repository failed the request.");
	}

	public void removeResource(ContentResourceEdit edit)
	{
		ContentResourceJCR crjcr = null;
		if (edit instanceof ContentResourceJCR)
			crjcr = (ContentResourceJCR) edit;
		else
		{
			ContentEntity tmp = edit.getVirtualContentEntity();
			if (tmp instanceof ContentResourceJCR)
				crjcr = (ContentResourceJCR) tmp;
		}
		if (crjcr == null) return; // can't do anything if the resource isn't a JCR resource!
		if (!crjcr.removeNode())
			log.warn("Content Hosting Handler JCR was unable to delete a resource because the JCR Repository failed the request.");
	}

	public InputStream streamResourceBody(ContentResource resource) throws ServerOverloadException
	{
		ContentEntity ce = resource.getVirtualContentEntity();
		if (ce instanceof ContentResourceJCR) {
			ContentResourceJCR crjcr = (ContentResourceJCR) ce;
			InputStream is = crjcr.streamContent();
			if (is != null) return is;
		}
		return resource.streamContent();
	}




	public int getMemberCount(ContentEntity edit)
	{
		if (edit instanceof ContentCollectionJCR)
			return ((ContentCollectionJCR) edit).getMemberCount();
		if (edit.getVirtualContentEntity() instanceof ContentCollectionJCR)
			return ((ContentCollectionJCR) (edit.getVirtualContentEntity())).getMemberCount();
		return 0;
	}

	
	
	/* ------------------------------------------------------------------------ */
	/* Support for saving resource/collection properties */
	private synchronized void saveProperties(Edit edit,ContentEntityJCR cejcr)
	{
		/* First check to see if the resource being saved is the root of the virtual world.
		 * If it is, save any changes to the mount point property to the real parent instead
		 * of to the root virtual object.  In particular, this allows mount points to be
		 * unmounted!
		 */
		if (cejcr.relativePath.equals("/")) {
			// take out an edit object on the real parent...
			ContentResourceEdit cre = this.contentHostingHandlerResolver.editResource(cejcr.realParent.getId());
			// ...take out an edit object on its properties...
			ResourcePropertiesEdit rpe = cre.getPropertiesEdit();
			// ...set the CHH Bean property from the virtual object...
			String prop = edit.getPropertiesEdit().getProperty(ContentHostingHandlerResolver.CHH_BEAN_NAME);
			if (prop == null || prop.equals(""))
				rpe.removeProperty(ContentHostingHandlerResolver.CHH_BEAN_NAME);
			else
				rpe.addProperty(ContentHostingHandlerResolver.CHH_BEAN_NAME, prop);
			// ...and save it back again to the storage.
			try {
				this.contentHostingHandlerResolver.commitResource(cre);
			}
			catch (Exception e) {
				log.warn("Content Hosting Handler JCR was unable to save Sakai properties on the real parent of the virtual mountpoint: "+e);
			}
		}

		// Replace existing properties for content entity cefs with the new ones.
		javax.jcr.Node n = cejcr.getNode();
		try {
			ResourceProperties rp = edit.getPropertiesEdit();
			for (Iterator i = rp.getPropertyNames() ; i.hasNext(); )
			{
				String propname = (String) i.next();
				String propval  = rp.getProperty(propname);
				propname = propname.replaceAll("/","_");  // JCR does not permit '/' chars in property names
				propname = propname.replaceAll(":","_");  // JCR treats ':' as namespace indicator
				n.setProperty(propname,propval);
			}
			n.save();
		}
		catch (Exception e)
		{
			log.warn("Content Hosting Handler JCR was unable to save Sakai properties: "+e);
		}

		try {
			// Create a dummy child if this object should be a collection (so we read it back as a collection later)
			if (cejcr instanceof ContentCollectionJCR)
			{
				n.addNode(SAKAI_IS_COLLECTION);
				n.save();
			}
		}
		catch (Exception e)
		{
			log.warn("Content Hosting Handler JCR was unable to save Sakai isCollection flag: "+e.toString());
		}
	}
	protected synchronized Map loadProperties(ContentEntityJCR cejcr)
	{
		Map<String, String> m = new HashMap<String, String>();  // the loaded properties (string -> string lookup table)
		try {
			javax.jcr.Node n = cejcr.getNode();
			PropertyIterator pi = n.getProperties();
			for (int i = 0 ; i < pi.getSize() ; ++i) {
				Property p = pi.nextProperty();
				String name = p.getName();
				try {
					String value= p.getString();
					m.put(name,value);
				} catch (Exception e) {}
			}
		}
		catch (Exception e)
		{
			// ignore
		}
		return m;
	}
	/* ------------------------------------------------------------------------ */



	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentHostingHandler#getContentCollectionEdit(java.lang.String)
	 */
	public ContentCollectionEdit getContentCollectionEdit(String id)
	{
		ContentCollectionEdit cce = (ContentCollectionEdit) this.contentHostingHandlerResolver.newCollectionEdit(id);
		cce.setContentHandler(this);
		return cce;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentHostingHandler#getContentResourceEdit(java.lang.String)
	 */
	public ContentResourceEdit getContentResourceEdit(String id)
	{
		ContentResourceEdit cre = (ContentResourceEdit) this.contentHostingHandlerResolver.newResourceEdit(id);
		cre.setContentHandler(this);
		return cre;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentHostingHandler#putDeleteResource(java.lang.String, java.lang.String, java.lang.String)
	 */
	public ContentResourceEdit putDeleteResource(String id, String uuid, String userId)
	{
		ContentResourceEdit cre = (ContentResourceEdit) this.contentHostingHandlerResolver.newResourceEdit(id);
		cre.setContentHandler(this);
		return cre;
	}
	
	public void getUuid(String id)
	{	
	}

	public void setResourceUuid(String resourceId, String uuid)
	{
	}

	public Collection<String> getMemberCollectionIds(ContentEntity ce)
	{
		return null;
	}
	
	public Collection<String> getMemberResourceIds(ContentEntity ce)
    {
		return null;
    }

    public String moveResource(ContentResourceEdit thisResource, String new_id)
    {
		return null;
    }

    public String moveCollection(ContentCollectionEdit thisCollection, String new_folder_id)
    {
		return null;
    }
}