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
package org.sakaiproject.content.chh.dspace;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingHandler;
import org.sakaiproject.content.api.ContentHostingHandlerResolver;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.chh.file.ContentEntityFileSystem;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.ServerOverloadException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/*
 * import org.dspace.app.dav.client.LNISoapServletServiceLocator; import
 * org.dspace.app.dav.client.LNISoapServlet; import
 * org.dspace.app.dav.client.LNIClientUtils;
 */

/**
 * Provides a read/write view of a DSpace repository through virtual content
 * hosting. CH collection IDs are the DSpace handles of the
 * communities/collections. CH resource IDs are the DSpace handles of the items.
 * Bitstreams are not needed to proxy DSpace through CH. In all cases, "handle"
 * excludes the "hdl:" prefix and the body is name-mangled so the '/' appears as
 * an underscore.
 * 
 * @author johnf
 */
public class ContentHostingHandlerImplDSpace implements ContentHostingHandler
{
	private static final String HANDLER_NAME = "DSpaceHandler";

	private static final Log log = LogFactory
			.getLog(ContentHostingHandlerImplDSpace.class);

	public final static String XML_NODE_NAME = "mountpoint";

	public final static String XML_ATTRIBUTE_ENDPOINT = "endpoint";

	public final static String XML_ATTRIBUTE_BASE = "baseHandle";

	public final static String XML_ATTRIBUTE_SEARCHABLE = "searchable";

	protected static final String nameProp = "<propfind xmlns=\"DAV:\"><prop><displayname /></prop></propfind>";

	protected static final String allProp = "<propfind xmlns=\"DAV:\"><allprop /></propfind>";

	public static final boolean DSRESOURCES_AS_COLLECTIONS = true;
	
	/**
	 * Queries DSpace for all objects inside the specified handle to the
	 * specified depth. All DSpace item types are returned and all properties
	 * are retrieved.
	 * 
	 * @param handle
	 *        the dspace handle to be queried (list of handles previxed by dso_
	 *        and separated by forward slashes)
	 * @param depth
	 *        the depth of the dspace community/collection/resource tree to
	 *        explore
	 */
	private static final Document getDSpaceProps(String endpoint, String handle, int depth)
	{
		try
		{
			StringBuffer requestXMLbuf = buildDSpaceXMLRequestForAllProps("dso_"
					+ handle.replaceAll("/", "%24"), depth);
			StringBuffer responseXMLbuf = hitDSpaceWithRequest(endpoint, requestXMLbuf);
			return extractAllPropsResponseFromSOAP(responseXMLbuf);
		}
		catch (Exception e)
		{
			log.warn("Error in CHH DSpace mechanism [" + e.toString() + "]");
			return null; // invalid DSpace endpoint / URL / comms failure /
			// incomprehensible reply header
		}
	}

	private static final StringBuffer buildDSpaceXMLRequestForAllProps(
			String urlEncodedHandle, int depth)
	{
		StringBuffer requestXMLbuf = new StringBuffer(1024);
		requestXMLbuf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		requestXMLbuf
				.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
		requestXMLbuf.append("<soapenv:Body>");
		requestXMLbuf
				.append("<ns1:propfind soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:ns1=\"http://dspace.org/xmlns/lni\">");
		requestXMLbuf.append("<uri xsi:type=\"xsd:string\">" + urlEncodedHandle
				+ "</uri>");
		requestXMLbuf
				.append("<doc xsi:type=\"xsd:string\">&lt;propfind xmlns=&quot;DAV:&quot;&gt;&lt;allprop /&gt;&lt;/propfind&gt;</doc>");
		requestXMLbuf.append("<depth href=\"#id0\"/>");
		requestXMLbuf.append("<types xsi:type=\"xsd:string\" xsi:nil=\"true\"/>");
		requestXMLbuf.append("</ns1:propfind>");
		requestXMLbuf
				.append("<multiRef id=\"id0\" soapenc:root=\"0\" soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xsi:type=\"xsd:int\" xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\">"
						+ depth + "</multiRef>");
		requestXMLbuf.append("</soapenv:Body>");
		requestXMLbuf.append("</soapenv:Envelope>");
		return requestXMLbuf;
	}

	private final static char [] base64chars= new char [64];
	static {
		int i=0;
		for (;i<26;++i) base64chars[i]=(char)(65+i); // A-Z
		for (;i<52;++i) base64chars[i]=(char)(97+i-26); // a-z
		for (;i<62;++i) base64chars[i]=(char)(48+i-52); // 0-9
		base64chars[62]=43;                          // +
		base64chars[63]=47;                          // /
	}
	private static final String base64Encode(String s) {
		// I don't like the sun.misc Base64Encoder because it isn't officially supported.
		// return new sun.misc.BASE64Encoder().encode(s.getBytes());
		byte [] b = s.getBytes();
		int l = b.length;
		// Base64 is a rate 80% code so x2 is long enough for StringBuffer to not have to resize
		StringBuffer result = new StringBuffer(2*l);
		int p = 0;
		while (p < l) {
			byte a0 = b[p];
			byte a1 = p<l-1 ? b[p+1] : 0;
			byte a2 = p<l-2 ? b[p+2] : 0;
			byte o0 = (byte) (a0 >>> 2);
			byte o1 = (byte) ((a0 & 0x03)<<4 | (a1 >>> 4));
			byte o2 = (byte) ((a1 & 0x0F)<<2 | (a2 >>> 6));
			byte o3 = (byte) (a2 & 0x3F);
			result.append(base64chars[o0]);
			result.append(base64chars[o1]);
			result.append(p<l-1 ? base64chars[o2] : '=');
			result.append(p<l-2 ? base64chars[o3] : '=');
			p+= 3;
		}
		return result.toString();
	}
	private static final StringBuffer hitDSpaceWithRequest(String endpoint,
			StringBuffer requestXMLbuf) throws IOException
	{
		URL url = new URL(endpoint);
		HttpURLConnection huc = (HttpURLConnection) url.openConnection();
		huc.setRequestMethod("GET");
		huc.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
		huc.setRequestProperty("Accept",
				"application/soap+xml, application/dime, multipart/related, text/*");
		huc.setRequestProperty("User-Agent", "Axis/1.3");
		// huc.setRequestProperty("Host","localhost:8081");
		huc.setRequestProperty("Cache-Control", "no-cache");
		huc.setRequestProperty("Pragma", "no-cache");
		huc.setRequestProperty("SOAPAction", "");
		huc.setRequestProperty("Content-Length", "" + requestXMLbuf.length());
		huc.setRequestProperty("Authorization","Basic "+base64Encode(endpoint.substring(endpoint.indexOf("http://")+7,endpoint.indexOf("@",endpoint.indexOf("http://")+7))));
		huc.setDoInput(true);
		huc.setDoOutput(true);
		huc.connect();
		huc.getOutputStream().write(requestXMLbuf.toString().getBytes());
		huc.getOutputStream().flush();
		huc.getContent();

		StringBuffer buf = new StringBuffer(1024);
		InputStream is = huc.getInputStream();
		for (;;)
		{
			byte b[] = new byte[256];
			int l = is.read(b);
			if (l > 0)
				buf.append(new String(b, 0, l));
			else
				break;
		}
		return buf;
	}

	private static final Document extractAllPropsResponseFromSOAP(StringBuffer xml)
			throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		if (db == null) return null;

		Document d = db.parse(new ByteArrayInputStream(xml.toString().getBytes()));
		if (d == null) return null;
		// the useful content is burried within 4 layers of enclosing XML junk
		NodeList nl = d.getChildNodes();
		for (int i = 0; i < 4; ++i)
			for (int j = 0; (nl != null) && (j < nl.getLength()); ++j)
				if (nl.item(j).getNodeName() != null
						&& !nl.item(j).getNodeName().equals(""))
				{
					nl = nl.item(j).getChildNodes();
					break;
				}
		if (nl == null) return null; // did not find the expected 4 layers of
		// wrapper crap
		String x = nl.item(0).getNodeValue();

		if (x == null) return null;
		x.replaceAll("&lt;", "<");
		x.replaceAll("&gt;", ">");
		x.replaceAll("&quot;", "\"");
		return db.parse(new ByteArrayInputStream(x.getBytes()));
	}

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
	 * @param contentHandlerResover
	 *        the contentHandlerResover to set
	 */
	public void setContentHostingHandlerResolver(
			ContentHostingHandlerResolver contentHostingHandlerResolver)
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
		ContentCollectionDSpace ccds = null;
		if (edit instanceof ContentCollectionDSpace)
			ccds = (ContentCollectionDSpace) edit;
		else
		{
			ContentEntity tmp = edit.getVirtualContentEntity();
			if (tmp instanceof ContentCollectionDSpace)
				ccds = (ContentCollectionDSpace) tmp;
			else if (tmp instanceof ContentResourceDSpace)
				ccds = ((ContentResourceDSpace)tmp).convertToCollection();
		}
		if (ccds == null) return; // can't do anything if the resource isn't a
		// dspace resource!

		ContentEntityDSpace encloser = resolveDSpace(ccds.realParent, ccds.endpoint,
				ccds.basehandle, ccds.parentRelativePath, ccds.chh,  ccds.searchable);
		if (encloser == null || encloser.dii == null || encloser.dii.handle == null)
		{
			log.warn("Content Hosting Handler DSpace was unable to save the contents of a collection because the enclosing collection was not found.");
			return; // parent collection has been erased
		}
		// encloser.dii.handle is the parent collection handle to which we
		// http/dav PUT this resource

		try
		{
			if (encloser.dii.endpoint != null && encloser.dii.handle != null) {
				String puturl = ccds.dii.endpoint;
				puturl = puturl.substring(0, puturl.lastIndexOf("/"));
				puturl = puturl + "/dso_" + encloser.dii.handle.replace("/", "%24") + "?mkcol=true";
				URL url = new URL(puturl);

				HttpURLConnection huc = (HttpURLConnection) url.openConnection();
				huc.setRequestMethod("PUT");
				huc.setRequestProperty("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2");
				huc.setRequestProperty("User-Agent", "Axis/1.3");
				huc.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
				huc.setRequestProperty("Authorization","Basic am9obmYlNDBjYXJldC5jYW0uYWMudWs6cGFzc3dvcmQ=");
				huc.setDoOutput(true);
				huc.connect();
				huc.getOutputStream().write( ("<mkcol>" + ccds.dii.displayname + "</mkcol>").getBytes() );
				huc.getOutputStream().flush();
				int httpResponseCode = huc.getResponseCode();
				String handleInsideJunk = huc.getHeaderField("Location");
				if (handleInsideJunk == null) return;
				ccds.dii.handle = handleInsideJunk.substring(
					5/* skip the /dso_ prefix */+ handleInsideJunk.lastIndexOf("/")).replace("%24", "/");
			}
		}
		catch (IOException e)
		{
			log.warn("Content Hosting Handler DSpace was unable to save the contents of a collection because DSpace refused the operation.",e);
			return; // file system error -- operation cannot be performed
		}
		catch (SecurityException e)
		{
			log.warn("Content Hosting Handler DSpace was unable to save the contents of a collection because the JVM SecurityManager refused the operation.",e);
			return; // permissions error -- operation cannot be performed
		}
		checkForUnmountRequest(edit);
	}

	public void commit(ContentResourceEdit edit)
	{
		ContentResourceDSpace crds = null;
		if (edit instanceof ContentResourceDSpace)
			crds = (ContentResourceDSpace) edit;
		else
		{
			ContentEntity tmp = edit.getVirtualContentEntity();
			if (tmp instanceof ContentResourceDSpace) crds = (ContentResourceDSpace) tmp;
		}
		if (crds == null) return; // can't do anything if the resource isn't a
		// dspace resource!

		ContentEntityDSpace encloser = resolveDSpace(crds.realParent, crds.endpoint,
				crds.basehandle, crds.parentRelativePath, crds.chh, crds.searchable);
		if (encloser == null || encloser.dii == null || encloser.dii.handle == null)
		{
			log.warn("Content Hosting Handler DSpace was unable to save the contents of a resource because the enclosing collection was not found.");
			return; // parent collection has been erased
		}
		// encloser.dii.handle is the parent collection handle to which we
		// http/dav PUT this resource

		InputStream is = null;
		OutputStream os = null;
		try
		{
			is = edit.streamContent();
			if (is == null) return; // abort the commit if we can't stream it
			// through

			byte b[] = new byte[1024];
			String puturl = crds.dii.endpoint;
			puturl = puturl.substring(0, puturl.lastIndexOf("/"));
			puturl = puturl + "/dso_" + encloser.dii.handle.replace("/", "%24")
					+ "?package=PDF";
			URL url = new URL(puturl);

			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
			huc.setRequestMethod("PUT");
			huc.setRequestProperty("Accept",
					"text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2");
			huc.setRequestProperty("User-Agent", "Axis/1.3");
			huc.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
			huc.setRequestProperty("Content-Length", "" + is.available());
			huc.setRequestProperty("Authorization",
					"Basic am9obmYlNDBjYXJldC5jYW0uYWMudWs6cGFzc3dvcmQ=");
			huc.setDoInput(true);
			huc.setDoOutput(true);
			huc.connect();
			os = huc.getOutputStream();
			while (is.available() > 0)
			{
				int l = is.read(b);
				if (l > 0)
					os.write(b, 0, l);
				else
					break;
			}
			os.flush();
			is.close();
			huc.getResponseCode();
			String handleInsideJunk = huc.getHeaderField("Location");
			if (handleInsideJunk == null) return;
			crds.dii.handle = handleInsideJunk.substring(
					5/* skip the /dso_ prefix */+ handleInsideJunk.lastIndexOf("/"))
					.replace("%24", "/");
		}
		catch (IOException e)
		{
			log.warn("Content Hosting Handler DSpace was unable to save the contents of a resource because DSpace refused the operation.",e);
			return; // file system error -- operation cannot be performed
		}
		catch (SecurityException e)
		{
			log.warn("Content Hosting Handler DSpace was unable to save the contents of a resource because the JVM SecurityManager refused the operation.",e);
			return; // permissions error -- operation cannot be performed
		}
		catch (ServerOverloadException e)
		{
			log.warn("Content Hosting Handler DSpace was unable to save the contents of a resource because the server threw a ServerOverloadException and was unable to stream the resource contents.",e);
			return; // sakai failed to deliver the contents of the file; saving
			// it is obviously impossible
		}
		finally
		{
			if (is != null) try
			{
				is.close();
			}
			catch (IOException e)
			{
			}
			if (os != null) try
			{
				os.flush();
				os.close();
			}
			catch (IOException e)
			{
			}
		}
		
		checkForUnmountRequest(edit);
	}
	protected void checkForUnmountRequest(Edit edit)
	{
		/* Check to see if the resource being saved is the root of the virtual world.
		 * If it is, save any changes to the mount point property to the real parent instead
		 * of to the root virtual object.  In particular, this allows mount points to be
		 * unmounted!
		 */
		ContentEntityDSpace ceds = (ContentEntityDSpace) ((ContentEntity)edit).getVirtualContentEntity();
		if (ceds.relativePath.equals("/")) {
			// take out an edit object on the real parent...
			ContentResourceEdit cre = this.contentHostingHandlerResolver.editResource(ceds.realParent.getId());
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
				log.warn("Content Hosting Handler DSpace was unable to save Sakai properties on the real parent of the virtual mountpoint: "+e.toString());
			}
		}
	}

	public void commitDeleted(ContentResourceEdit edit, String uuid)
	{ /*
		 * No need to do anything -- removeResource/removeCollection does the
		 * work
		 */
	}

	public List getCollections(ContentCollection collection)
	{
		ContentEntity cc = collection.getVirtualContentEntity();
		if (!(cc instanceof ContentCollectionDSpace))
		{
			return null; // this is not the correct handler for this resource
			// -- serious problems!
		}
		ContentCollectionDSpace ccds = (ContentCollectionDSpace) cc;
		List l = ccds.getMembers();
		ArrayList<Edit> collections = new ArrayList<Edit>(l.size());
		for (Iterator i = l.listIterator(); i.hasNext();)
		{
			String id = (String) i.next();
			ContentEntityDSpace ceds = resolveDSpace(ccds.realParent, ccds.endpoint,
					ccds.basehandle, id.substring(ccds.realParent.getId().length() + 1),
					this, ccds.searchable);
			if (ceds instanceof ContentCollectionDSpace) collections.add(ceds.wrap());
		}
		return collections;
	}


	public List getFlatResources(ContentEntity ce)
	{
System.out.println("getFlatResources");
		return null;
	}

	public byte[] getResourceBody(ContentResource resource)
			throws ServerOverloadException
	{
		if (!(resource instanceof ContentResourceDSpace)) return null;
		ContentResourceDSpace crds = (ContentResourceDSpace) resource;
		return crds.getContent();
	}

	public List getResources(ContentCollection collection)
	{
		ContentEntity cc = collection.getVirtualContentEntity();
		if (!(cc instanceof ContentCollectionDSpace))
		{
			return null; // this is not the correct handler for this resource
			// -- serious problems!
		}
		ContentCollectionDSpace ccds = (ContentCollectionDSpace) cc;
		List l = ccds.getMemberResources();
		return l;
	}

	/* ---------------------------------------------------- */

	/*
	 * This is the format of the XML we expect to get back from DSpace-LNI...
	 * <?xml version="1.0" encoding="UTF-8"?> <multistatus xmlns="DAV:">
	 * <response> <href>/dso_123456789%241</href> <propstat> <prop>
	 * <displayname>Test 1</displayname> <resourcetype> <collection />
	 * </resourcetype> <dspace:type
	 * xmlns:dspace="http://www.dspace.org/xmlns/dspace"> <dspace:community />
	 * </dspace:type> <current-user-privilege-set> <privilege> <all />
	 * </privilege> </current-user-privilege-set> <dspace:short_description
	 * xmlns:dspace="http://www.dspace.org/xmlns/dspace">This is the first test
	 * community</dspace:short_description> <dspace:introductory_text
	 * xmlns:dspace="http://www.dspace.org/xmlns/dspace">&lt;b&gt;woot!&lt;/b&gt;</dspace:introductory_text>
	 * <dspace:side_bar_text
	 * xmlns:dspace="http://www.dspace.org/xmlns/dspace">&lt;i&gt;fubar&lt;/i&gt;</dspace:side_bar_text>
	 * <dspace:copyright_text
	 * xmlns:dspace="http://www.dspace.org/xmlns/dspace">legal blurb goes here</dspace:copyright_text>
	 * <dspace:handle
	 * xmlns:dspace="http://www.dspace.org/xmlns/dspace">hdl:123456789/1</dspace:handle>
	 * </prop> <status>HTTP/1.1 200 OK</status> </propstat> <propstat> <prop>
	 * <dspace:logo xmlns:dspace="http://www.dspace.org/xmlns/dspace" /> </prop>
	 * <status>HTTP/1.1 404 Not found</status> </propstat> </response> more of
	 * these: <response>...</response> </multistatus>
	 */

	private final String LAYER1 = "multistatus";

	private final String LAYER2 = "response";

	private final String HREF = "href";

	private final String LAYER3 = "propstat";

	private final String LAYER4 = "prop";

	private final String DISPNM = "displayname";

	private final String LSTMOD = "getlastmodified";

	private final String CNTLEN = "getcontentlength";

	private final String CNTTYP = "getcontenttype";

	private final String STATUS = "status";

	private final String ST_OK = "HTTP/1.1 200 OK";

	private final String DSTYPE = "dspace:type";

	private final String DSWDRN = "dspace:withdrawn";

	private DSpaceItemInfo queryDSpaceFor(String endpoint, String base, String needle)
	{
		try
		{
			/* PARSE XML RESPONSE... */
			// This is a really horrible tree-walk but it is, at least, NOT
			// vulnerable to the whitespace in the XML being reformatted.
			// After *much* debate, we decided that this is no worse than other
			// ways of doing this!
			Document d = getDSpaceProps(endpoint, base, 1);
			if (d == null) return null;

			// find multistatus node
			Node node_multistatus = null;
			NodeList nl = d.getChildNodes();
			for (int j = 0; j < nl.getLength(); ++j)
				if (nl.item(j).getNodeName() != null
						&& nl.item(j).getNodeName().equals(LAYER1))
				{
					node_multistatus = nl.item(j);
					break;
				}
			if (node_multistatus == null) return null;

			boolean foundMatch = false; // haven't found the named node so far
			DSpaceItemInfo dii = new DSpaceItemInfo(); // the return value
			// (unless we return
			// null)
			dii.displayname = needle; // will only be returned if we find the
			// name sought
			dii.endpoint = endpoint;

			// examine each response node, looking for ones which match the
			// string sought
			nl = node_multistatus.getChildNodes();
			for (int j = 0; j < nl.getLength(); ++j)
			{
				// only interested in nodes with name="response"
				if (nl.item(j).getNodeName() == null
						|| !nl.item(j).getNodeName().equals(LAYER2)) continue;
				Node node_response = nl.item(j);
				NodeList resources = node_response.getChildNodes();

				// grab the resource handle
				String handle = null;
				for (int k = 0; k < resources.getLength(); ++k)
					if (resources.item(k).getNodeName() != null
							&& resources.item(k).getNodeName().equals(HREF))
					{
						handle = resources.item(k).getFirstChild().getNodeValue().trim();
						break;
					}
				if (handle == null) continue; // skip this resource if it
				// doesn't have an 'href' node.

				// Communities and collections have a /-imploded handle
				// hierarchy.
				// The comm/coll's own handle is the last one.
				String[] handles = handle.split("/");
				handle = handles[handles.length - 1]; // only interested in
				// the last one
				if (!handle.startsWith("bitstream_")) // this only affects
					// reading bitstreams
					// when queryDSpaceFor
					// is called for a
					// resource
					dii.handle = handle.replace("%24", "/").substring(4);
				else
				{
					if (handles.length > 1)
						dii.handle = handles[handles.length - 2].replace("%24", "/")
								.substring(4);
					else
						continue; // a handle containing only "bitstream_x" is
					// not valid
					try
					{
						int indx;
						String numberPart = handle.substring("bitstream_".length());
						if ((indx = numberPart.indexOf(".")) != -1)
							numberPart = numberPart.substring(0, indx);
						dii.bitstreamID = Integer.parseInt(numberPart);
					}
					catch (NumberFormatException e)
					{
					}
				}

				// now loop over the child nodes again looking at propstat
				// blocks...
				for (int k = 0; k < resources.getLength(); ++k)
				{
					if (resources.item(k).getNodeName() == null
							|| !resources.item(k).getNodeName().equals(LAYER3)) continue; // only
					// want
					// 'propstat'
					// nodes

					NodeList propstats = resources.item(k).getChildNodes();

					// first check whether status is OK (ignore all HTTP status
					// codes except 200)
					// no status node is assumed to mean OK. abort this propstat
					// if status is not OK.
					boolean status_ok = true;
					for (int l = 0; l < propstats.getLength(); ++l)
						if (propstats.item(l).getNodeName() != null
								&& propstats.item(l).getNodeName().equals(STATUS)
								&& !propstats.item(l).getFirstChild().getNodeValue()
										.trim().equals(ST_OK))
						{
							status_ok = false;
							break;
						}
					if (!status_ok) continue; // skip this propstat node

					// Look for child nodes with name 'prop', having nested
					// child called 'displayname'.
					// This is only complicated when the base handle is a DSpace
					// resource. There will
					// be child nodes (with handle=bitstream_<n>) and the same
					// display name as the resource.
					// We need to extract properties from the bitstream_1 child
					// (.._2 is the license).
					for (int l = 0; l < propstats.getLength(); ++l)
					{
						if (propstats.item(l).getNodeName() != null
								&& propstats.item(l).getNodeName().equals(LAYER4))
						{
							boolean recordFromThisChild = false;
							String lastmod = null, contentlength = null, contenttype = null, dstype = null;
							NodeList properties = propstats.item(l).getChildNodes();
							for (int m = 0; m < properties.getLength(); ++m)
								if (properties.item(m).getNodeName() != null)
								{
									if (properties.item(m).getNodeName().equals(DISPNM)
											&& properties.item(m).getFirstChild()
													.getNodeValue().trim().equals(needle))
									{
										foundMatch = true;
										recordFromThisChild = true;
									}
									if (properties.item(m).getNodeName().equals(LSTMOD))
										lastmod = properties.item(m).getFirstChild()
												.getNodeValue().trim();
									if (properties.item(m).getNodeName().equals(CNTLEN))
										contentlength = properties.item(m)
												.getFirstChild().getNodeValue().trim();
									if (properties.item(m).getNodeName().equals(CNTTYP))
										contenttype = properties.item(m).getFirstChild()
												.getNodeValue().trim();
									if (properties.item(m).getNodeName().equals(DSTYPE))
									{
										Node n = properties.item(m).getFirstChild();
										while (n != null)
										{
											String s = n.getNodeName();
											if (!s.equals("#text"))
											{
												dstype = s;
												break;
											}
											n = n.getNextSibling();
										}
									}
								}
							if (recordFromThisChild)
							{
								if (lastmod != null) dii.lastmodified = lastmod;
								if (contentlength != null) try
								{
									dii.contentLength = Long.parseLong(contentlength);
								}
								catch (NumberFormatException nfe)
								{
								}
								if (contenttype != null) dii.contentType = contenttype;
								if (dstype != null) dii.itemType = dstype;
								if (DSRESOURCES_AS_COLLECTIONS) return dii;
							}
						}
					}
				}
			}
			if (foundMatch) return dii;
		}
		catch (Exception e)
		{
			log.warn("Error in CHH DSpace mechanism: parse error: [" + e.toString() + "]");
		}
		return null; // problem talking to DSpace or named resource has gone
	}

	protected ContentEntityDSpace resolveDSpace(ContentEntity realParent,
			String endpoint, String basehandle, String relativePath,
			ContentHostingHandlerImplDSpace chh, boolean searchable)
	{
		// return an item (resource) or a community/collection (collection) as
		// appropriate
		while (relativePath.length() > 0 && relativePath.charAt(0) == '/')
			relativePath = relativePath.substring(1);
		while (relativePath.length() > 0
				&& relativePath.charAt(relativePath.length() - 1) == '/')
			relativePath = relativePath.substring(0, relativePath.length() - 1);
		String[] items = relativePath.split("/");
		DSpaceItemInfo dii = new DSpaceItemInfo();
		dii.handle = basehandle;
		if (relativePath.equals(""))
		{
			dii.displayname = "";
			dii.itemType = "dspace:community";
		}
		else
			for (int i = 0; i < items.length; ++i)
			{
				dii = queryDSpaceFor(endpoint, dii.handle, items[i]); // walk
				// the
				// hierarchy
				if (dii == null || dii.handle == null)
				{
					// the resource is a new resource which has not yet been
					// commited,
					// or it has been removed from dspace
					// we construct a new ContentEntityDSpaceResource to model
					// the non-existent object
					dii = new DSpaceItemInfo(); // handle is left NULL
					// (important!)
					dii.endpoint = endpoint;
					dii.displayname = items[i];
					ContentEntityDSpace ceds = new ContentResourceDSpace(realParent,
							endpoint, basehandle, relativePath, chh, contentHostingHandlerResolver, dii, searchable);
					ceds.wrap();
					return ceds; // was return null;
				}
			}
		relativePath = "/" + relativePath;
		if (dii.isCollection())
		{
			ContentEntityDSpace ceds = new ContentCollectionDSpace(realParent, endpoint,
					basehandle, relativePath, chh, contentHostingHandlerResolver, dii, searchable);
			ceds.wrap();
			return ceds;
		}
		else
		{
			ContentEntityDSpace ceds = new ContentResourceDSpace(realParent, endpoint,
					basehandle, relativePath, chh, contentHostingHandlerResolver, dii, searchable);
			ceds.wrap();
			return ceds;
		}
	}

	/**
	 * getVirtualContentEntity is the entry point for the virtual entity
	 * resolution mechanism. This parses the XML of the real parent to locate
	 * the parameters required to talk to a DSpace instance.
	 * 
	 * @see org.sakaiproject.content.api.ContentHostingHandler#getVirtualContentEntity(org.sakaiproject.content.api.ContentEntity,
	 *      java.lang.String)
	 */
	public ContentEntity getVirtualContentEntity(ContentEntity edit, String finalId)
	{
		// Algorithm: get the mount point from the XML file represented by
		// 'edit' (the real parent)
		// construct a new ContentEntityDSpace and return it
		try
		{
			boolean searchable = false;
			byte[] xml = ((ContentResource) edit).getContent();
			if (xml == null) return null;
			DocumentBuilder db = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			if (db == null) return null;
			Document d = db.parse(new ByteArrayInputStream(xml));
			if (d == null) return null;
			Node node_mountpoint = null;
			NodeList nl = d.getChildNodes();
			for (int j = 0; j < nl.getLength(); ++j)
				if (nl.item(j).getNodeName() != null
						&& nl.item(j).getNodeName().equals(XML_NODE_NAME))
				{
					node_mountpoint = nl.item(j);
					break;
				}
			if (node_mountpoint == null) return null;

			Node node_endpoint = node_mountpoint.getAttributes().getNamedItem(
					XML_ATTRIBUTE_ENDPOINT);
			if (node_endpoint == null) return null;
			final String endpoint = node_endpoint.getNodeValue();
			if (endpoint == null || endpoint.equals("")) return null; // invalid
			// mountpoint
			// specification

			Node node_basehandle = node_mountpoint.getAttributes().getNamedItem(
					XML_ATTRIBUTE_BASE);
			if (node_basehandle == null) return null;
			final String basehandle = node_basehandle.getNodeValue();
			if (basehandle == null || basehandle.equals("")) return null; // invalid
			// mountpoint
			// specification

			Node node_searchable = node_mountpoint.getAttributes().getNamedItem(
					XML_ATTRIBUTE_SEARCHABLE);
			if (node_searchable != null)
				searchable = Boolean.parseBoolean(node_searchable.getNodeValue());

			String relativePath = finalId.substring(edit.getId().length());
			ContentEntityDSpace ceds = resolveDSpace(edit, endpoint, basehandle,
					relativePath, this, searchable);
			Edit ce = ceds.wrap();
			if (ce == null) return null; // happens when the requested URL
			// requires a log on but the user is
			// not logged on
			return (ContentEntity) ce;
		}
		catch (Exception e)
		{
			log.warn("Content Hosting Handler DSpace: Invalid XML for the mountpoint ["
					+ edit.getId() + "], exception was " + e.toString());
			return (ContentEntity)(resolveDSpace(edit, "", "", "", this, false).wrap());
		}
	}

	protected List listDSpaceItemsIn(String endpoint, String base)
	{
		List<String> resultList = new ArrayList<String>();
		try
		{
			Document d = getDSpaceProps(endpoint, base, 1);
			if (d == null) return null;

			// find multistatus node
			Node node_multistatus = null;
			NodeList nl = d.getChildNodes();
			for (int j = 0; j < nl.getLength(); ++j)
				if (nl.item(j).getNodeName() != null
						&& nl.item(j).getNodeName().equals(LAYER1))
				{
					node_multistatus = nl.item(j);
					break;
				}
			if (node_multistatus == null) return null;

			// examine each response node, looking for ones which match the
			// string sought
			nl = node_multistatus.getChildNodes();
			for (int j = 0; j < nl.getLength(); ++j)
			{
				// only interested in nodes with name="response"
				if (nl.item(j).getNodeName() == null
						|| !nl.item(j).getNodeName().equals(LAYER2)) continue;
				Node node_response = nl.item(j);
				NodeList resources = node_response.getChildNodes();

				// grab the resource handle
				String handle = null;
				for (int k = 0; k < resources.getLength(); ++k)
					if (resources.item(k).getNodeName() != null
							&& resources.item(k).getNodeName().equals(HREF))
					{
						handle = resources.item(k).getFirstChild().getNodeValue().trim();
						break;
					}
				if (handle == null) continue; // skip this resource if it
				// doesn't have an 'href' node.
				String[] handles = handle.split("/");
				handle = handles[handles.length - 1]; // only interested in
				// the last one
				handle = handle.replace("%24", "/").substring(4);
				if (isWithdrawn(resources)) return new ArrayList(); // if
				// the
				// base
				// is
				// withdrawn,
				// ignore
				// all
				// children
				if (handle.equals(base)) continue; // response includes the
				// object queried itself --
				// omit this from the output
				// list

				// loop over the child nodes looking at propstat blocks...
				for (int k = 0; k < resources.getLength(); ++k)
				{
					if (resources.item(k).getNodeName() == null
							|| !resources.item(k).getNodeName().equals(LAYER3)) continue; // only
					// want
					// 'propstat'
					// nodes

					NodeList propstats = resources.item(k).getChildNodes();

					// first check whether status is OK (ignore all HTTP status
					// codes except 200)
					// no status node is assumed to mean OK. abort this propstat
					// if status is not OK.
					boolean status_ok = true;
					for (int l = 0; l < propstats.getLength(); ++l)
						if (propstats.item(l).getNodeName() != null
								&& propstats.item(l).getNodeName().equals(STATUS)
								&& !propstats.item(l).getFirstChild().getNodeValue()
										.trim().equals(ST_OK))
						{
							status_ok = false;
							break;
						}
					if (!status_ok) continue; // skip this propstat node

					// look for child nodes with name 'prop', having nested
					// child called 'displayname'
					// if the displayname is the 'needle' being sought, return
					// handle.
					for (int l = 0; l < propstats.getLength(); ++l)
					{
						if (propstats.item(l).getNodeName() != null
								&& propstats.item(l).getNodeName().equals(LAYER4))
						{
							NodeList properties = propstats.item(l).getChildNodes();
							for (int m = 0; m < properties.getLength(); ++m)
								if (properties.item(m).getNodeName() != null
										&& properties.item(m).getNodeName()
												.equals(DISPNM))
									resultList.add(properties.item(m).getFirstChild()
											.getNodeValue().trim());
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			log
					.warn("Error in CHH DSpace mechanism: parse error: [" + e.toString()
							+ "]");
			return new ArrayList();
		}
		return resultList; // problem talking to DSpace or named resource has
		// gone
	}

	private boolean isWithdrawn(NodeList responsechilds)
	{
		for (int k = 0; k < responsechilds.getLength(); ++k)
		{
			if (responsechilds.item(k).getNodeName() == null
					|| !responsechilds.item(k).getNodeName().equals(LAYER3)) continue; // only
			// want
			// 'propstat'
			// nodes
			NodeList propstatchilds = responsechilds.item(k).getChildNodes();
			for (int l = 0; l < propstatchilds.getLength(); ++l)
			{
				if (propstatchilds.item(l).getNodeName() == null
						|| !propstatchilds.item(l).getNodeName().equals(LAYER4))
					continue; // only want 'prop' nodes
				NodeList propchilds = propstatchilds.item(l).getChildNodes();
				for (int m = 0; m < propchilds.getLength(); ++m)
				{
					if (propchilds.item(m).getNodeName() != null
							&& propchilds.item(m).getNodeName().equals(DSWDRN))
					{
						NodeList vals = propchilds.item(m).getChildNodes();
						for (int n = 0; n < vals.getLength(); ++n)
						{
							if (vals.item(n).getNodeValue() != null
									&& vals.item(n).getNodeValue().contains("true"))
								return true;
						}
					}
				}
			}
		}
		return false;
	}


	public void removeCollection(ContentCollectionEdit edit)
	{
		ContentCollectionDSpace ccds = null;
		if (edit instanceof ContentCollectionDSpace)
			ccds = (ContentCollectionDSpace) edit;
		else
		{
			ContentEntity tmp = edit.getVirtualContentEntity();
			if (tmp instanceof ContentCollectionDSpace)
				ccds = (ContentCollectionDSpace) tmp;
		}
		if (ccds == null) return; // can't do anything if the resource isn't a
		// dspace resource!

		try
		{
			byte b[] = new byte[1024];
			String puturl = ccds.dii.endpoint;
			puturl = puturl.substring(0, puturl.lastIndexOf("/"));
			puturl = puturl + "/dso_" + ccds.dii.handle.replace("/", "%24")
					+ "?delete=true";
			URL url = new URL(puturl);
			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
			huc.setRequestMethod("PUT");
			huc.setRequestProperty("Accept",
					"text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2");
			huc.setRequestProperty("User-Agent", "Axis/1.3");
			huc.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
			huc.setRequestProperty("Authorization",
					"Basic am9obmYlNDBjYXJldC5jYW0uYWMudWs6cGFzc3dvcmQ=");
			huc.setDoOutput(true);
			huc.connect();
			huc.getOutputStream().write("<delete />".getBytes());
			int httpResponseCode = huc.getResponseCode();
		}
		catch (IOException e)
		{
			log.warn("Content Hosting Handler DSpace was unable to delete a resource because DSpace refused the operation.",e);
			return; // socket error -- operation cannot be performed
		}
		catch (SecurityException e)
		{
			log.warn("Content Hosting Handler DSpace was unable to delete a resource because the JVM SecurityManager refused the operation.",e);
			return; // permissions error -- operation cannot be performed
		}
	}

	public void removeResource(ContentResourceEdit edit)
	{
		ContentResourceDSpace crds = null;
		if (edit instanceof ContentResourceDSpace)
			crds = (ContentResourceDSpace) edit;
		else
		{
			ContentEntity tmp = edit.getVirtualContentEntity();
			if (tmp instanceof ContentResourceDSpace) crds = (ContentResourceDSpace) tmp;
		}
		if (crds == null) return; // can't do anything if the resource isn't a
		// dspace resource!

		try
		{
			byte b[] = new byte[1024];
			String puturl = crds.dii.endpoint;
			puturl = puturl.substring(0, puturl.lastIndexOf("/"));
			puturl = puturl + "/dso_" + crds.dii.handle.replace("/", "%24")
					+ "?delete=true";
			URL url = new URL(puturl);

			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
			huc.setRequestMethod("PUT");
			huc.setRequestProperty("Accept",
					"text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2");
			huc.setRequestProperty("User-Agent", "Axis/1.3");
			huc.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
			huc.setRequestProperty("Authorization",
					"Basic am9obmYlNDBjYXJldC5jYW0uYWMudWs6cGFzc3dvcmQ=");
			huc.setDoOutput(true);
			huc.connect();
			huc.getOutputStream().write("<delete />".getBytes());
			int httpResponseCode = huc.getResponseCode();
		}
		catch (IOException e)
		{
			log.warn("Content Hosting Handler DSpace was unable to delete a resource because DSpace refused the operation.",e);
			return; // socket error -- operation cannot be performed
		}
		catch (SecurityException e)
		{
			log.warn("Content Hosting Handler DSpace was unable to delete a resource because the JVM SecurityManager refused the operation.",e);
			return; // permissions error -- operation cannot be performed
		}
	}

	public InputStream streamResourceBody(ContentResource resource)
			throws ServerOverloadException
	{
		ContentEntity ce = resource.getVirtualContentEntity();
		if (!(ce instanceof ContentResourceDSpace)) return null;
		ContentResourceDSpace crfs = (ContentResourceDSpace) ce;
		return crfs.streamContent();
	}

	public int getMemberCount(ContentEntity edit)
	{
		if (edit instanceof ContentCollectionDSpace)
			return ((ContentCollectionDSpace) edit).getMemberCount();
		if (edit.getVirtualContentEntity() instanceof ContentCollectionDSpace)
			return ((ContentCollectionDSpace) (edit.getVirtualContentEntity()))
					.getMemberCount();
		return 0;
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