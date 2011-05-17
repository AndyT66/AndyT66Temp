/* CVS Header
   $
   $
*/

package uk.ac.uhi.ral.impl.fedora;

import uk.ac.uhi.ral.DigitalItemInfo;

public class FedoraItemInfo implements DigitalItemInfo {
  private String title = null;
  private String subject = null;
  private String description = null;
  private String publisher = null;
  private String identifier = null;
  private String mimeType = null;
  private byte[] binaryContent = null;
  private String displayName = null;
  private String creator = null;
  private String modifiedDate = null;
  private String originalFilename = null;
  private String type = null;
  private boolean resource;
  private boolean collection;
  private Object privateInfo;
  private String url = null;
  private String[] memberships = null;
  private int contentLength = 0;   // CLIF
  private boolean incollection = false; // CLIF
  private String parentPID = "";  // CLIF
  
  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getSubject() {
    return subject;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public void setPublisher(String publisher) {
    this.publisher = publisher;
  }

  public String getPublisher() {
    return publisher;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setBinaryContent(byte[] binaryContent) {
    this.binaryContent = binaryContent;
  }

  public byte[] getBinaryContent() {
    return binaryContent;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public String getCreator() {
    return creator;
  }

  public void setModifiedDate(String modifiedDate) {
    this.modifiedDate = modifiedDate;
  }

  public String getModifiedDate() {
    return modifiedDate;
  }

  public void setOriginalFilename(String originalFilename) {
    this.originalFilename = originalFilename;
  }

  public String getOriginalFilename() {
    return originalFilename;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public int getContentLength() {
	    if (binaryContent == null)
	      return contentLength;      // CLIF
	    else
	      return binaryContent.length;
  } 
  
  public void setContentLength(int length) {   // CLIF 
	   contentLength = length;      
  }
  
  public boolean isInCollection() {
	  return incollection; // CLIF  
  }
  
  public void setIsInCollection(boolean incoll) {
	  incollection = incoll; // CLIF  
  }  
  
  public void setIsResource(boolean resource) {
    this.resource = resource;
    this.collection = !resource;
  }

  public void setIsCollection(boolean collection) {
    this.collection = collection;
    this.resource = !collection;
  }

  public boolean isResource() {
    return resource;
  }

  public boolean isCollection() {
    return collection;
  }

  public void setPrivateInfo(Object privateInfo) {
    this. privateInfo = privateInfo;
  }

  public Object getPrivateInfo() {
    return privateInfo;
  }

  public void setURL(String url) {
    this.url = url;
  }

  public String getURL() {
    return url;
  }

  public void setParentPid(String pid) {
	 this.parentPID = pid;
  }
  
  public String getParentPid() {
	  return this.parentPID;
  }
  
  public void setCollectionMemberships(String[] memberships) {
    this.memberships = memberships;
  }
  public String[] getCollectionMemberships() {
    return memberships;
  }
  
  // CLIF  
  // when a new resource is uploaded in the Resource Tool UI the following code will 
  // determine the currently displayed collection PID
  public String deriveParentObjectPID() {
	  
	  CharSequence char0 = "/"; 
	  if(identifier != null && identifier.contains(char0)) {		  
		  String[] idParts = identifier.split(char0.toString());
		  if(idParts.length > 1)
			  return idParts[idParts.length - 2];
	  }
	  
	  return "";
	  	  
  }  
  
}
