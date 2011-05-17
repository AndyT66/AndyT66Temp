/* CVS Header
   $
   $
*/

package uk.ac.uhi.ral.impl.fedora;

public class FedoraPrivateItemInfo {
  private String pid = null;
  private String ownerId = null;
  private String contentDatastreamID = null;
  private String dcDatastreamID = null;
  private String relsextDatastreamID = "RELS-EXT";
  private String modsDatastreamID = null;
  
  public String getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  public String getPid() {
    return pid;
  }

  public void setPid(String pid) {
    this.pid = pid;
  }

  public void setContentDatastreamID(String contentDatastreamID) {
    this.contentDatastreamID = contentDatastreamID;
  }

  public String getContentDatastreamID() {
    return contentDatastreamID;
  }

  public void setDCDatastreamID(String dcDatastreamID) {
    this.dcDatastreamID = dcDatastreamID;
  }

  public String getDCDatastreamID() {
    return dcDatastreamID;
  }

  public void setRelsExtDatastreamID(String relsextDatastreamID) {
    this.relsextDatastreamID = relsextDatastreamID;
  }

  public String getRelsExtDatastreamID() {
    return relsextDatastreamID;
  }
  
  public void setMODsDatastreamID(String modsDatastreamID) {
	  this.modsDatastreamID = modsDatastreamID;
  }
  
  public String getMODsDatastreamID() {
	    return modsDatastreamID;
  }  
}
