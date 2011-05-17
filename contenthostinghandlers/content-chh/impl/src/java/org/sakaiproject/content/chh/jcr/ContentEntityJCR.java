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

import java.util.Collection;
import java.util.Stack;
import java.util.Map;
import java.util.HashMap;
import java.io.FileInputStream;

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

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.Node;
import javax.jcr.Item;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.core.TransientRepository;	// specific binding to
														// apache jackrabbit
import org.apache.jackrabbit.core.config.RepositoryConfig;

/**
 * An abstract superclass representing virtual content entities corresponding to
 * JCR entities.
 * 
 * @author johnf (johnf@caret.cam.ac.uk)
 */
public abstract class ContentEntityJCR implements ContentEntity
{
	private static final Log log = LogFactory.getLog(ContentEntityJCR.class);

	protected String relativePath;	// location of item within virtual
									// repository

	protected String basePath;	// location of root of JCR repository in host
								// file system

	protected String virtualWorldRoot;	// location of mountpoint within JCR
										// repo (doesn't have to be root)

	protected ContentEntity realParent;

	protected boolean searchable;	// set by resolveToFileOrDirectory; read by
									// derived classes
	protected String username;
	protected String password;

	protected final static String JCR_CONFIG_FILE = "/home/jkf/testjcrrepo.xml";

	/**
	 * Object reference to the content hosting handler which looks after this
	 * virtual content resource.
	 */
	protected ContentHostingHandlerImplJCR chh;

	/**
	 * Wrapped version of itself (a base content edit)
	 */
	protected Edit wrappedMe;

	abstract protected Edit wrap();

	abstract protected void setVirtualProperties();

	/**
	 * ID of the parent collection object
	 */
	protected String parent;

	protected String parentRelativePath;

	protected ContentHostingHandlerResolver resolver;

	/* ------------------------------------------------ */
	static Map<String,TransientRepository> repoMap = new HashMap();
	static private TransientRepository getRepository(String cfgfile,String basePath) {
		String key = cfgfile+"___"+basePath;
		TransientRepository tr = repoMap.get(key);
		if (tr == null) {
			try {
				tr = new TransientRepository(
						RepositoryConfig.create(new FileInputStream(JCR_CONFIG_FILE),basePath)
						);
				repoMap.put(key,tr);
			}
			catch (Exception e)
			{
				log.warn("Content Hosting Handler JCR failed to load root node of repository due to ["+e+"] -- XML Mountpoint probably contains incorrect parameters.");
			}
		}
		return tr;
	}
	/* ------------------------------------------------ */

	/**
	 * Constructs a new instance
	 * 
	 * @param filename -
	 *        filename or URI of the file or directory to represent
	 */
	public ContentEntityJCR(ContentEntity realParent, String basePath,
			String relativePath, ContentHostingHandlerImplJCR chh,
			ContentHostingHandlerResolver resolver, boolean searchable,
			String username, String password, String vwr)
	{
		this.realParent = realParent;
		this.basePath = basePath;
		this.username = username;
		this.password = password;
		this.virtualWorldRoot = vwr;
		this.relativePath = relativePath;
		this.chh = chh;
		this.searchable = searchable;
		this.resolver = resolver;

		int lastSlash = relativePath.lastIndexOf('/');
		if (relativePath.length()>1 && lastSlash == relativePath.length()-1)
			lastSlash = relativePath.lastIndexOf('/',lastSlash-1);
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
			this.parentRelativePath = "/"; // root cyclically parents itself
			// :-S
			parent = realParent.getId();
		}
		else
		{
			this.parentRelativePath = relativePath.substring(0, lastSlash);
			parent = realParent.getId() + "/" + parentRelativePath;
		}
	}

	protected Map loadProperties()
	{
		return ((ContentHostingHandlerImplJCR) chh).loadProperties(this);
	}

	/**
	 * @return enclosing collection
	 */
	public ContentCollection getContainingCollection()
	{
		return (ContentCollection) chh.resolveToFileOrDirectory(realParent,
				basePath, parentRelativePath, chh,
				searchable,username,password,virtualWorldRoot);
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
		if (chh instanceof ContentHostingHandlerImplJCR)
		{
			this.chh = (ContentHostingHandlerImplJCR) chh;
		}
		else
		{
			log.error("ContentHostingHandler is not an instance of ContentHostingHandlerImplJCR");
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
	 * The response is not cached and will reflect the current state of the JCR
	 * represented at all times.
	 * 
	 * @return true if the path/URI is a file, false if it exists but is a
	 *         non-file object, true if inaccessible/no-longer exists.
	 */
	abstract public boolean isResource();

	/**
	 * Returns true if the represented path is a directory. The response is not
	 * cached and will reflect the current state of the JCR represented at all
	 * times.
	 * 
	 * @return true if path/URI is a directory, false otherwise.
	 */
	abstract public boolean isCollection();

	public ContentEntity getMember(String nextId)
	{
		// cut real parent's ID off the start of the string
		String newpath = nextId.substring(realParent.getId().length());
		return chh.resolveToFileOrDirectory(realParent, basePath, newpath, chh,
				searchable,username,password,virtualWorldRoot);
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
	} // wild guess

	public String getUrl(String rootProperty)
	{
		return join(realParent.getUrl(rootProperty), relativePath);
	}

	public String getReference(String rootProperty)
	{
		return join(realParent.getReference(rootProperty), relativePath);
	} // wild guess

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

	/*Default access*/
	Node getNode() {
		try
		{
			Repository repository = getRepository(JCR_CONFIG_FILE,basePath);
			Session session = repository.login(new SimpleCredentials(username,password.toCharArray()));
			Item item = session.getItem(virtualWorldRoot + relativePath);
			if ((item == null) || !(item instanceof Node)) return null;
			return (Node)item;
		}
		catch (javax.jcr.PathNotFoundException e)
		{
			return null;
		}
		catch (javax.jcr.RepositoryException e)
		{
			return null;
		}
	}
	/*Default access*/
	Node getParentNode() {
		try
		{
			// virtualWorldRoot might be "/", parentRelativePath might be "/" too, if this is root node -- chop off (max 1) trailing "/"
			String path = virtualWorldRoot + parentRelativePath;
			if (path.endsWith("/")) path = path.substring(0,path.length()-1);
			Repository repository = getRepository(JCR_CONFIG_FILE,basePath);
			Session session = repository.login(new SimpleCredentials(username,password.toCharArray()));
			Item item = session.getItem(path);
			if ((item == null) || !(item instanceof Node)) return null;
			return (Node)item;
		}
		catch (javax.jcr.PathNotFoundException e)
		{
			return null;
		}
		catch (javax.jcr.RepositoryException e)
		{
			return null;
		}
	}

	static Node getRootNode(String username,String password,
							String basePath,
							String virtualWorldRoot) {
		try {
			Repository repository = getRepository(JCR_CONFIG_FILE,basePath);
			Session session = repository.login(new SimpleCredentials(username,password.toCharArray()));
			Item item = session.getItem(virtualWorldRoot);
			if ((item == null) || !(item instanceof Node))
			{
				log.warn("Content Hosting Handler JCR failed to load root node of repository -- XML Mountpoint probably contains incorrect parameters.");
				return null;
			}
			return (Node)item;
		}
		catch (Exception e)
		{
			log.warn("Content Hosting Handler JCR failed to load root node of repository due to ["+e+"] -- XML Mountpoint probably contains incorrect parameters.");
			return null;
		}
	}

	/*Default access*/
	/* This needs to update the node AND GET ITS PARENT IN THE SAME JCR 'session', then save */
	boolean removeNode() {
		try
		{
			Repository repository = getRepository(JCR_CONFIG_FILE,basePath);
			Session session = repository.login(new SimpleCredentials(username,password.toCharArray()));
			Item item = session.getItem(virtualWorldRoot + relativePath);
			if ((item == null) || !(item instanceof Node)) return false;
			Node par = ((Node)item).getParent();
			((Node)item).remove();
			par.save();
			return true;
		}
        catch (Exception e) {e.printStackTrace();return false;}
        /* Possible exceptions are numerous:
         * javax.jcr.PathNotFoundException
         * javax.jcr.RepositoryException
         * javax.jcr.version.VersionException
         * javax.jcr.lock.LockException
         * javax.jcr.nodetype.ConstraintViolationException
         */
	}
}
