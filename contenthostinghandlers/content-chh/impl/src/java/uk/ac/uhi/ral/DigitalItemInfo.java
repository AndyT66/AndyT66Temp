/* CVS Header
   $
   $
*/

package uk.ac.uhi.ral;

public interface DigitalItemInfo {
  public void setTitle(String title);
  public String getTitle();

  public void setCreator(String creator);
  public String getCreator();

  public void setSubject(String subject);
  public String getSubject();

  public void setDescription(String description);
  public String getDescription();

  public void setPublisher(String publisher);
  public String getPublisher();

  public void setIdentifier(String identifier);
  public String getIdentifier();

  public void setMimeType(String mimeType);
  public String getMimeType();

  public void setBinaryContent(byte[] binaryContent);
  public byte[] getBinaryContent();

  public int getContentLength();
  public void setContentLength(int length);   // CLIF
  public void setIsInCollection(boolean incoll);  // CLIF
  
  public void setIsResource(boolean resource);
  public void setIsCollection(boolean collection);

  public void setURL(String url);
  public String getURL();

  public boolean isResource();
  public boolean isCollection();
  public boolean isInCollection(); // CLIF
  public void setParentPid(String pid);  // CLIF
  public String getParentPid();  // CLIF
  public void setCollectionMemberships(String[] memberships);
  public String[] getCollectionMemberships();
  
  // //////////////////
  
  public void setDisplayName(String displayName);
  public String getDisplayName();

  public void setModifiedDate(String modifiedDate);
  public String getModifiedDate();

  public void setOriginalFilename(String originalFilename);
  public String getOriginalFilename();

  public void setType(String type);
  public String getType();

  // //////////////////

  public void setPrivateInfo(Object privateInfo);
  public Object getPrivateInfo();
}
