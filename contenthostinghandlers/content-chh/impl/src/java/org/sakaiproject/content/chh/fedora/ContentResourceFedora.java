/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/content/trunk/content-api/api/src/java/org/sakaiproject/content/api/ContentResource.java $
 * $Id: ContentResource.java 13360 2006-08-03 21:35:13Z jimeng@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.content.chh.fedora;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.*;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.ServerOverloadException;
import uk.ac.uhi.ral.DigitalItemInfo;
import uk.ac.uhi.ral.DigitalRepository;
import uk.ac.uhi.ral.impl.fedora.FedoraPrivateItemInfo;

import java.io.InputStream;

/**
* <p>ContentResource is the core interface for a Resource object in the GenericContentHostingService.</p>
*/
public class ContentResourceFedora extends ContentEntityFedora implements ContentResource {
  private static final Log log = LogFactory.getLog(ContentCollectionFedora.class);
  private boolean _appendPaths = true;
  
  public ContentResourceFedora(ContentEntity realParent, String relativePath,
                               ContentHostingHandler chh,
                               ContentHostingHandlerResolver chhResolver,
                               DigitalRepository repo,
                               DigitalItemInfo item) {
    super(realParent, relativePath, chh, chhResolver, repo, item);
  }
  
  public Edit wrap() {
    if (wrapped == null) {
      if(_appendPaths)
    	  wrapped = chhResolver.newResourceEdit(getId());  // getId() actually is compound of realParent.Id and relativePath
      else
          wrapped = chhResolver.newCollectionEdit(join(((ContentEntityFedora)realParent).getWrapped().getId(),"/" + item.getIdentifier()));// CLIF    	  

      ((ContentEntity)wrapped).setContentHandler(chh);
      ((ContentEntity)wrapped).setVirtualContentEntity(this);

      // set the resource type
      ((GroupAwareEdit)wrapped).setResourceType(this.getResourceType());

      // copy properties from real parent, then overwrite specific properties
      // CLIF **************
      Edit realParentWrapped = null;
      if(realParent instanceof ContentEntityFedora) {
    	      	  
    	  realParentWrapped = ((ContentEntityFedora)realParent).getWrapped();
      }
      if((realParentWrapped == null) && (realParent instanceof Edit)) {
    	  
    	  realParentWrapped = (Edit)realParent;
      }
      // **************  CLIF
      
      wrapped.getProperties().addAll(realParentWrapped.getProperties());
      wrapped.getProperties().removeProperty(ContentHostingHandlerResolver.CHH_BEAN_NAME);
      setVirtualProperties();
    }

    return wrapped;
  }

  public Edit wrap(boolean appendPaths) {
	  try
	  {
		  _appendPaths = appendPaths;
		  return wrap();
	  }
	  finally
	  {
		  _appendPaths = true;
	  }
  }
  
  protected void setVirtualProperties() {
    String tmp;
    if (this.relativePath.equals("/"))
      tmp = ((FedoraPrivateItemInfo)(item.getPrivateInfo())).getPid();
    else {
      int lastSlash = this.relativePath.lastIndexOf("/", this.relativePath
          .length() - 2);
      // if there are no slashes, the display name should be the whole
      // relative path
      if (lastSlash == -1)
        tmp = this.relativePath;
      else
        tmp = this.relativePath.substring(lastSlash).substring(1);
    }

    wrapped.getProperties().addProperty(ResourceProperties.PROP_DISPLAY_NAME, item.getDisplayName());
    wrapped.getProperties().addProperty(ResourceProperties.PROP_CREATOR, item.getCreator());
    wrapped.getProperties().addProperty(ResourceProperties.PROP_MODIFIED_DATE, item.getModifiedDate());  // CLIF bug fix
    wrapped.getProperties().addProperty(ResourceProperties.PROP_ORIGINAL_FILENAME, item.getOriginalFilename());
    wrapped.getProperties().addProperty(ResourceProperties.PROP_DESCRIPTION, item.getDescription());

    // resource-only properties
    wrapped.getProperties().addProperty(ResourceProperties.PROP_CONTENT_LENGTH, String.valueOf(item.getContentLength()));
    wrapped.getProperties().addProperty(ResourceProperties.PROP_IS_COLLECTION, Boolean.FALSE.toString());
  }

  /**
	* Access the content byte length.
	* @return The content byte length.
	*/
	public int getContentLength() {
		return item.getContentLength();
	}

	public void setContentLength(int length) {   // CLIF
		item.setContentLength(length);
	}	
	
	/**
	* Access the resource MIME type.
	* @return The resource MIME type.
	*/
	public String getContentType() {
    return item.getMimeType();
	}

	/**
	* Access an array of the bytes of the resource.
	* @return An array containing the bytes of the resource's content.
	* @exception ServerOverloadException
	* 			if server is configured to save resource body in filesystem and an error occurs while 
	* 			trying to access the filesystem.
	* 
	* Modified for CLIF to read content in a lazy fetch manner. setBinaryContent will no longer gernerally be used !
	*/
  public byte[] getContent() throws ServerOverloadException {
	  
	  	byte[] content = item.getBinaryContent();
	  	
	  	if ((content != null) && (content.length > 0)) {
	  		return content;
	  	}
	  	else {
	  		
	    try {
	    	InputStream contentStream = repo.getContentAsStream(item.getURL());
	    	
			if (contentStream == null) return content;
			byte[] content2 = new byte[contentStream.available()];
			
			try {
				contentStream.read(content2);
			}
			finally {
				contentStream.close();
				contentStream = null; // try to close the stream to conserve resources			
			}
			return content2;	    	    	    
	      }	    
	      catch(Exception e) {
	        return content;
	      }	  
	  	}

	}

	/**
	 * Access the content as a stream.
	 * Please close the stream when done as it may be holding valuable system resources.
	 * @return an InputStream through which the bytes of the resource can be read.
	 * @throws ServerOverloadException if the server cannot produce the content stream at this time.
	 */
	public InputStream streamContent() throws ServerOverloadException {
    try {
      return repo.getContentAsStream(item.getURL());
    }
    catch(Exception e) {
      return null;
    }
  }

  public String getResourceType() {
    return ResourceType.TYPE_UPLOAD;
  }

  public boolean isResource() {
    return true;
  }

  public boolean isCollection() {
    return false;
  }
  
  public String getId() {
	  return super.getId();
  }  
}



