/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/rwiki/trunk/rwiki-tool/tool/src/java/uk/ac/cam/caret/sakai/rwiki/tool/ModelMigrationContextListener.java $
 * $Id: ModelMigrationContextListener.java 20354 2007-01-17 10:30:57Z ian@caret.cam.ac.uk $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 University of Cambridge.
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
package org.sakaiproject.content.chh.file;

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
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.ServerOverloadException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Provides a read/write view of a filesystem through virtual content hosting.
 * 
 * @author johnf
 */
public class ContentHostingHandlerImplFileSystem implements ContentHostingHandler
{
	private static final String HANDLER_NAME="FSHandler";
	private static final Log log = LogFactory.getLog(ContentHostingHandlerImplFileSystem.class);

	public final static String XML_NODE_NAME = "mountpoint";

	public final static String XML_ATTRIBUTE_NAME = "path";
	public final static String XML_ATTRIBUTE_HIDDEN = "showHiddenFiles";
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
		ContentCollectionFileSystem ccfs = null;
		if (edit instanceof ContentCollectionFileSystem)
			ccfs = (ContentCollectionFileSystem) edit;
		else
		{
			ContentEntity tmp = edit.getVirtualContentEntity();
			if (tmp instanceof ContentCollectionFileSystem)
				ccfs = (ContentCollectionFileSystem) tmp;
			else if (tmp instanceof ContentResourceFileSystem)
				ccfs = ((ContentResourceFileSystem)tmp).convertToCollection();
		}
		if (ccfs == null) return; // can't do anything if the resource isn't a file system resource!

		// save directory
		String path = ccfs.basePath + ccfs.relativePath;
		try
		{
			new File(path).mkdirs();
		}
		catch (SecurityException e)
		{
			log.warn("Content Hosting Handler File System was unable to save the contents of a collection to disk because the JVM SecurityManager refused the operation.");
			return;  // permissions error -- operation cannot be performed
		}

		// save properties (in a file in the new directory)
		saveProperties(edit,getPropertiesFileName(ccfs),ccfs.basePath+propertiesFileFieldSeparator+ccfs.relativePath+propertiesFileFieldSeparator);
	}

	public void commit(ContentResourceEdit edit)
	{
		ContentResourceFileSystem crfs = null;
		if (edit instanceof ContentResourceFileSystem)
			crfs = (ContentResourceFileSystem) edit;
		else
		{
			ContentEntity tmp = edit.getVirtualContentEntity();
			if (tmp instanceof ContentResourceFileSystem)
				crfs = (ContentResourceFileSystem) tmp;
		}
		if (crfs == null) return; // can't do anything if the resource isn't a file system resource!

		// save properties
		saveProperties(edit,getPropertiesFileName(crfs),crfs.basePath+propertiesFileFieldSeparator+crfs.relativePath+propertiesFileFieldSeparator);

		// save file contents
		String path = crfs.basePath + crfs.relativePath;
		InputStream is = null;
		OutputStream os = null;
		try
		{
			byte b[] = new byte[1024];
			is = edit.streamContent();

			// If the source is another FILE input stream, then this is a copy/paste operation (and not an upload or an
			// edit of a file through the content-tool interface).  Rename target to "Copy of ..." to avoid overwrites.
			if (is instanceof java.io.FileInputStream)
			{
				while (new File(path).exists())
				{
					int p = path.lastIndexOf("/")+1;
					path = path.substring(0,p)+"Copy of "+path.substring(p);
				}
			}

			if (is != null) {
				os = new FileOutputStream(path);
				while (is.available()>0) {
					int l = is.read(b);
					if (l>0) os.write(b,0,l); else break;
				}
			}
		}
		catch (IOException e)
		{
			log.warn("Content Hosting Handler File System was unable to save the contents of a resource to disk because the file system refused the operation.");
			return;  // file system error -- operation cannot be performed
		}
		catch (SecurityException e)
		{
			log.warn("Content Hosting Handler File System was unable to save the contents of a resource to disk because the JVM SecurityManager refused the operation.");
			return;  // permissions error -- operation cannot be performed
		}
		catch (ServerOverloadException e)
		{
			log.warn("Content Hosting Handler File System was unable to save the contents of a resource to disk because the server threw a ServerOverloadException and was unable to stream the file contents.");
			return;  // sakai failed to deliver the contents of the file; saving it is obviously impossible
		}
		finally
		{
			if (is != null) try {is.close();} catch (IOException e) {}
			if (os != null) try {os.flush();os.close();} catch (IOException e) {}
		}
	}

	public void commitDeleted(ContentResourceEdit edit, String uuid)
	{ /* No need to do anything -- removeResource/removeCollection does the work */
	}

	public List getCollections(ContentCollection collection)
	{
		ContentEntity cc = collection.getVirtualContentEntity();
		if (!(cc instanceof ContentCollectionFileSystem))
		{
			return null; // this is not the correct handler for this resource -- serious problems!
		}
		ContentCollectionFileSystem ccfs = (ContentCollectionFileSystem) cc;
		List l = ccfs.getMembers();
		ArrayList<Edit> collections = new ArrayList<Edit>(l.size());
		for (Iterator i = l.listIterator(); i.hasNext();)
		{
			String id = (String) i.next();
			ContentEntityFileSystem cefs = resolveToFileOrDirectory(ccfs.realParent, ccfs.basePath,
					id.substring(ccfs.realParent.getId().length() + 1), this, ccfs.showHiddenFiles,ccfs.searchable);
			if (cefs instanceof ContentCollectionFileSystem) collections.add(cefs.wrap());
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
		if (!(resource instanceof ContentResourceFileSystem)) return null;
		ContentResourceFileSystem crfs = (ContentResourceFileSystem) resource;
		return crfs.getContent();
	}

	public List getResources(ContentCollection collection)
	{
		ContentEntity cc = collection.getVirtualContentEntity();
		if (!(cc instanceof ContentCollectionFileSystem))
		{
			return null; // this is not the correct handler for this resource -- serious problems!
		}
		ContentCollectionFileSystem ccfs = (ContentCollectionFileSystem) cc;
		List l = ccfs.getMemberResources();
		return l;
	}
	protected ContentEntityFileSystem resolveToFileOrDirectory(ContentEntity realParent, String basePath, String relativePath,
			ContentHostingHandlerImplFileSystem chh, boolean showHiddenFiles,boolean searchable)
	{
		// return a file (resource) or a directory (collection) as appropriate
		while (relativePath.length() > 0 && relativePath.charAt(0) == '/')
			relativePath = relativePath.substring(1);
		while (relativePath.length() > 0 && relativePath.charAt(relativePath.length() - 1) == '/')
			relativePath = relativePath.substring(0, relativePath.length() - 1);
		relativePath = "/" + relativePath;
		if (basePath.charAt(basePath.length() - 1) == '/') basePath = basePath.substring(0, basePath.length() - 1);
		String newpath = basePath + relativePath;
		File f = new File(newpath);
		if (f.isDirectory())
		{
			ContentEntityFileSystem cefs = new ContentCollectionFileSystem(realParent, basePath, relativePath, chh, contentHostingHandlerResolver, showHiddenFiles,searchable);
			cefs.wrap();
			return cefs;
		}
		else
		{
			ContentEntityFileSystem cefs = new ContentResourceFileSystem(realParent, basePath, relativePath, chh, contentHostingHandlerResolver, showHiddenFiles,searchable);
			cefs.wrap();
			return cefs;
		}
	}


	public ContentEntity getVirtualContentEntity(ContentEntity edit, String finalId)
	{
		// Algorithm: get the mount point from the XML file represented by 'edit'
		// construct a new ContentEntityFileSystem and return it
		try
		{
			boolean showHiddenFiles = false;
			boolean searchable = false;  // allow the sakai-search tool to index the virtual hierarchy?
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
			
			Node node_basepath = node_mountpoint.getAttributes().getNamedItem(XML_ATTRIBUTE_NAME);
			if (node_basepath == null) return null;
			final String basepath = node_basepath.getNodeValue();
			
			Node node_hiddenFiles = node_mountpoint.getAttributes().getNamedItem(XML_ATTRIBUTE_HIDDEN);
			if (node_hiddenFiles != null)
				showHiddenFiles = Boolean.parseBoolean(node_hiddenFiles.getNodeValue());
			
			Node node_searchable = node_mountpoint.getAttributes().getNamedItem(XML_ATTRIBUTE_SEARCHABLE);
			if (node_searchable != null)
				searchable = Boolean.parseBoolean(node_searchable.getNodeValue());

			if (basepath == null || basepath.equals("")) return null; // invalid mountpoint specification

			String relativePath = finalId.substring(edit.getId().length());
			ContentEntityFileSystem cefs = resolveToFileOrDirectory(edit, basepath, relativePath, this, showHiddenFiles,searchable);
			if ("/".equals(finalId.substring(finalId.length()-1))
					&& (cefs instanceof ContentResourceFileSystem))
				cefs = ((ContentResourceFileSystem)cefs).convertToCollection();
			Edit ce = cefs.wrap();
			if (ce == null) return null; // happens when the requested URL requires a log on but the user is not logged on
			return (ContentEntity) ce;
		}
		catch (Exception e)
		{
			log.warn("Invalid XML for the mountpoint ["+edit.getId()+"], error is ["+e+"]");
			return (ContentEntity)(new ContentResourceFileSystem(edit, "/ERROR:invalid_sakai_mount_point", "/", this, contentHostingHandlerResolver, false,false).wrap());
		}
	}


	public void removeCollection(ContentCollectionEdit edit)
	{
		ContentCollectionFileSystem ccfs = null;
		if (edit instanceof ContentCollectionFileSystem)
			ccfs = (ContentCollectionFileSystem) edit;
		else
		{
			ContentEntity tmp = edit.getVirtualContentEntity();
			if (tmp instanceof ContentCollectionFileSystem)
				ccfs = (ContentCollectionFileSystem) tmp;
		}
		if (ccfs == null) return; // can't do anything if the resource isn't a file system resource!

		// remove meta properties (so new files/dirs created with same name don't 'inherit' them)
		removeProperties(ccfs);  // not necessary -- properties file is IN the directory which is about to be removed.
								 // even so, i call this incase someone decides to move the properties file later.

		String path = ccfs.basePath + ccfs.relativePath;
		try
		{
			// remove the properties file (otherwise directory is non-empty and directory removal fails)
			File f = new File(path +propertiesFileName);
			if (f.exists()) f.delete();

			new File(path).delete();
		}
		catch (SecurityException e)
		{
			log.warn("Content Hosting Handler File System was unable to delete a directory because the JVM SecurityManager refused the operation.");
			return;  // permissions error -- operation cannot be performed
		}
	}

	public void removeResource(ContentResourceEdit edit)
	{
		ContentResourceFileSystem crfs = null;
		if (edit instanceof ContentResourceFileSystem)
			crfs = (ContentResourceFileSystem) edit;
		else
		{
			ContentEntity tmp = edit.getVirtualContentEntity();
			if (tmp instanceof ContentResourceFileSystem)
				crfs = (ContentResourceFileSystem) tmp;
		}
		if (crfs == null) return; // can't do anything if the resource isn't a file system resource!

		// remove meta properties (so new files/dirs created with same name don't 'inherit' them)
		removeProperties(crfs);
		
		String path = crfs.basePath + crfs.relativePath;
		try
		{
			new File(path).delete();
		}
		catch (SecurityException e)
		{
			log.warn("Content Hosting Handler File System was unable to delete a file because the JVM SecurityManager refused the operation.");
			return;  // permissions error -- operation cannot be performed
		}
	}

	public InputStream streamResourceBody(ContentResource resource) throws ServerOverloadException
	{
		ContentEntity ce = resource.getVirtualContentEntity();
		if (ce instanceof ContentResourceFileSystem) {
			ContentResourceFileSystem crfs = (ContentResourceFileSystem) ce;
			InputStream is = crfs.streamContent();
			if (is != null) return is;
		}
		return resource.streamContent();
	}




	public int getMemberCount(ContentEntity edit)
	{
		if (edit instanceof ContentCollectionFileSystem)
			return ((ContentCollectionFileSystem) edit).getMemberCount();
		if (edit.getVirtualContentEntity() instanceof ContentCollectionFileSystem)
			return ((ContentCollectionFileSystem) (edit.getVirtualContentEntity())).getMemberCount();
		return 0;
	}

	
	
	/* ------------------------------------------------------------------------ */
	/* Support for saving resource/collection properties in a meta file on disk */
	private static final String propertiesFileFieldSeparator = "|";
	private static final String propertiesFileFieldSeparatorAsRegExp = "\\|";
	private static final String propertiesFileName = ".sakai.chhprops";
	private synchronized void saveProperties(Edit edit,String propFileName,String fileIdPrefix)
	{
		/* First check to see if the resource being saved is the root of the virtual world.
		 * If it is, save any changes to the mount point property to the real parent instead
		 * of to the root virtual object.  In particular, this allows mount points to be
		 * unmounted!
		 */
		ContentEntityFileSystem cefs = (ContentEntityFileSystem) ((ContentEntity)edit).getVirtualContentEntity();
		if (cefs.relativePath.equals("/")) {
			// take out an edit object on the real parent...
			ContentResourceEdit cre = this.contentHostingHandlerResolver.editResource(cefs.realParent.getId());
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
				log.warn("Content Hosting Handler File System was unable to save Sakai properties on the real parent of the virtual mountpoint: "+e.toString());
			}
		}

		// Replace existing properties for content entity cefs with the new ones.
		// Things to take care about:
		//  1. concurrent access to the properties file
		//  2. the directory could be mountable/mounted by two different mountpoints
		//     simultaneously, with DIFFERENT properties on the same file in each case
		//  3. don't load the whole file of properties into RAM because it is HUGE, in many cases
		// Lines in the properties file have the format:
		//  [base mountpoint path] | [relative path of file] | [prop name] | [prop value]
		// the division of the absolute file path into the first two elements ensures that
		// different mount points can have different properties for the same file.
		BufferedReader r = null;
		Writer w = null;
		// Algorithm: write out properties for this file to a new file;
		//            copy properties from old file for everything except this filename;
		//            rename new properties file on top of old one.
		try {
			w = new BufferedWriter(new FileWriter(propFileName+".new"));
			// write new properties
			ResourceProperties rp = edit.getPropertiesEdit();
			for (Iterator i = rp.getPropertyNames() ; i.hasNext(); )
			{
				String s = (String) i.next();
//				if (!s.startsWith("http://purl.org/dc/")) continue;  // only save dublin core properties
				w.write(fileIdPrefix+s+propertiesFileFieldSeparator+rp.getProperty(s)+"\n");
			}

			// copy old properties (minus any existing ones for this file)
			try {
				r = new BufferedReader(new FileReader(propFileName));
				String line = null;
				while ((line=r.readLine()) != null)
					if (!line.startsWith(fileIdPrefix)) w.write(line+"\n");
				r.close();
			}
			catch (InvalidObjectException e) {/* file doesn't exist / unreadable -- ignore */}
			catch (FileNotFoundException e) {/* file doesn't exist / unreadable -- ignore */}

			// move new file over old one
			w.flush();
			w.close();
			new File(propFileName).delete();
			new File(propFileName+".new").renameTo(new File(propFileName));
		}
		catch (IOException e)
		{
			log.warn("Content Hosting Handler File System was unable to save Sakai properties: "+e.toString());
		}
		finally {
			if (w != null) try {w.flush(); w.close();} catch (Exception e) {}
			if (r != null) try {r.close();} catch (Exception e) {}
			r = null;
			w = null;
		}
	}
	protected synchronized Map loadProperties(ContentEntityFileSystem cefs)
	{
		Map<String, String> m = new HashMap<String, String>();  // the loaded properties (string -> string lookup table)

		String propFileName = getPropertiesFileName(cefs);
		BufferedReader r = null;
		String fileIdPrefix = cefs.basePath+propertiesFileFieldSeparator+cefs.relativePath+propertiesFileFieldSeparator;
		try {
			r = new BufferedReader(new FileReader(propFileName));
			String line = null;
			while ((line=r.readLine()) != null)
				if (line.startsWith(fileIdPrefix))
				{
					String fields[] = line.split(propertiesFileFieldSeparatorAsRegExp,4);
					m.put(fields[2],fields[3]);
				}
		}
		catch (IOException e)
		{
			/* file doesn't exist / unreadable -- ignore */
		}
		finally {
			if (r != null) try {r.close();} catch (Exception e) {}
		}
		return m;
	}
	private synchronized void removeProperties(ContentEntityFileSystem cefs)
	{
		String propFileName = getPropertiesFileName(cefs);
		BufferedReader r = null;
		Writer w = null;
		// Algorithm: copy properties from old file for everything except this filename;
		//            rename new properties file on top of old one.
		String fileIdPrefix = cefs.basePath+propertiesFileFieldSeparator+cefs.relativePath+propertiesFileFieldSeparator;
		try {
			w = new BufferedWriter(new FileWriter(propFileName+".new"));
			try {
				r = new BufferedReader(new FileReader(propFileName));
				String line = null;
				while ((line=r.readLine()) != null)
					if (!line.startsWith(fileIdPrefix)) w.write(line+"\n");
				r.close();
			}
			catch (InvalidObjectException e) {/* file doesn't exist / unreadable -- ignore */}
			catch (FileNotFoundException e) {/* file doesn't exist / unreadable -- ignore */}

			// move new file over old one
			w.flush();
			w.close();
			new File(propFileName).delete();
			new File(propFileName+".new").renameTo(new File(propFileName));
		}
		catch (IOException e)
		{
			log.warn("Content Hosting Handler File System was unable to remove Sakai properties: "+e.toString());
		}
		finally {
			if (w != null) try {w.flush(); w.close();} catch (Exception e) {}
			if (r != null) try {r.close();} catch (Exception e) {}
			r = null;
			w = null;
		}
	}

	private String getPropertiesFileName(ContentEntityFileSystem cefs)
	{
		return cefs.basePath + cefs.parentRelativePath + (!cefs.parentRelativePath.equals("/") ? "/":"") + propertiesFileName;
	}

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