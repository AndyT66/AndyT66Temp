/* CVS Header
   $
   $
*/

package uk.ac.uhi.ral;

import java.util.PropertyResourceBundle;
import java.io.InputStream;

public interface DigitalRepository {
  /** Defines the name of the repository for displaying in Sakai */
  public static final String CONFIG_KEY_DISPLAY_NAME = "display.name";
  /** Defines the Fedora API-A access web service endpoint */
  public static final String CONFIG_KEY_API_A_ENDPOINT = "api-a.endpoint";
  /** Defines the Fedora API-M management web service endpoint */
  public static final String CONFIG_KEY_API_M_ENDPOINT = "api-m.endpoint";
  /** Defines the Fedora API-M management web service endpoint */
  public static final String CONFIG_KEY_DISSEMINATION_ENDPOINT = "dissemination.endpoint";
  /** Defines the URL for uploading content to modify datastreams */
  public static final String CONFIG_KEY_UPLOAD_URL = "upload.url";
  /** The username for the connection */
  public static final String CONFIG_KEY_CONNECTION_USERNAME = "connection.username";
  /** The password for the connection */
  public static final String CONFIG_KEY_CONNECTION_PASSWORD = "connection.password";
  // CLIF
  public static final String CONFIG_KEY_CLIF_USE_GUANXI_HTTPS = "clif.use-guanxi-https";
  public static final String CONFIG_KEY_CLIF_HTTPS_PORT = "clif.https-port"; 
  public static final String CONFIG_KEY_CLIF_FEDORA_ROOT_COLLECTION = "clif.fedora-root-collection";
  public static final String CONFIG_KEY_CLIF_FEDORA_OBJECT_NAMESPACE = "clif.fedora-object-namespace";
  public static final String CONFIG_KEY_CLIF_CACHE_SIZE = "clif.cache-size";
  public static final String CONFIG_KEY_CLIF_CACHE_REFRESH_MINS = "clif.cache-refresh-mins";
  
  public static final boolean INCLUDE_RESOURCES_IN_COLLECTIONS = true;
  public static final boolean DO_NOT_INCLUDE_RESOURCES_IN_COLLECTIONS = false;

  public PropertyResourceBundle getRepoConfig();
  public void init(PropertyResourceBundle config);

//  public String createObject(DigitalItemInfo item);
  public boolean modifyObject(DigitalItemInfo item, String dsID, byte[] dsContent, boolean inline);
  public boolean deleteObject(String pid);
  public boolean commitObject(DigitalItemInfo item);
  public boolean moveObject(DigitalItemInfo item);
//  public void search();
//  public DigitalItemInfo queryFedora(String pid);
//  public DigitalItemInfo[] queryFedora(String pid, boolean collectionsOnly, String collectionName);

  public InputStream getContentAsStream(String endpoint);
  public DigitalItemInfo generateItem();

  public DigitalItemInfo getResource(String pid);
  public DigitalItemInfo[] getResources(boolean includeResourcesInCollections,String searchPid);
  public DigitalItemInfo[] getCollections(String exludeThisCollection);
  public DigitalItemInfo[] getMembersInCollection(String collectionPid);
}
