/* CVS Header
   $
   $
*/

package uk.ac.uhi.ral.impl.fedora;

import fedora.fedoraSystemDef.foxml.*;
import fedora.webservices.client.api.a.FedoraAPIAServiceStub;
import fedora.webservices.client.api.m.FedoraAPIMServiceStub;
import info.fedora.definitions.x1.x0.types.*;
import info.fedora.definitions.x1.x0.types.GetNextPIDDocument.GetNextPID;
import info.fedora.definitions.x1.x0.types.GetNextPIDResponseDocument.GetNextPIDResponse;
import info.fedora.definitions.x1.x0.types.impl.GetNextPIDDocumentImpl;
import info.fedora.definitions.x1.x0.types.impl.GetNextPIDDocumentImpl.GetNextPIDImpl;

import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlCursor;
import org.guanxi.common.EntityConnection;
// CLIF
import org.guanxi.common.*;
import java.net.URL;
import java.net.HttpURLConnection;
import org.apache.xml.security.utils.Base64;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.PostMethod;
import java.util.List;
import java.util.LinkedList;
// CLIF END
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import uk.ac.uhi.ral.DigitalItemInfo;
import uk.ac.uhi.ral.DigitalRepository;
import uk.ac.uhi.ral.impl.fedora.FedoraPrivateItemInfo;
import uk.ac.uhi.ral.impl.fedora.util.Utils;

import javax.net.ssl.SSLSocketFactory;
import javax.xml.namespace.QName;
import java.io.*;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.PropertyResourceBundle;
import java.util.Vector;
import java.util.Calendar;
import java.util.Hashtable;
import java.lang.Exception;
import java.net.URLEncoder;

//  RelationData
//  holds relationship information for an digital object
final class RelationData {
	 
	 private boolean inCollection = false;
	 private boolean isCollection = false;
	  	 
	 public boolean getInCollection(){
		 return inCollection;
	 }
	 
	 public boolean getIsCollection(){
		 return isCollection;
	 }
	 
	 public void setInCollection(boolean val){
		  inCollection = val;
	 }
	 
	 public void setIsCollection(boolean val){
		 isCollection = val;
	 }	 
 }

//  RIClient   
//  Provides RDF query functionality
class RIClient {
	
	private PropertyResourceBundle repoConfig = null;
	private HttpHttpsUrlConsumer httpHttpsUrlConsumer = null;
	
	public RIClient(PropertyResourceBundle config, HttpHttpsUrlConsumer urlConsumer) {
		repoConfig = config;
		httpHttpsUrlConsumer = urlConsumer;
	}
	
	//  getCollectionChildrenObjects
	//  find all fedora immediate child objects belonging to a collection (fedora implicit relationships)
	public Vector<FedoraItemInfo> getCollectionChildrenObjects(String collectionPID) throws Exception {
		
		String query = "select $member $isCollection $label $owner $title $lastModified " +
		"from <#ri> " +
		"where $member <fedora-rels-ext:isMemberOf> <" + collectionPID + "> " +
		"and $member  <fedora-rels-ext:isCollection> $isCollection " +
		"and $member <fedora-model:label> $label " +
		"and $member <fedora-model:ownerId> $owner " +
		"and $member <dc:title> $title " +
		"and $member <fedora-view:lastModifiedDate> $lastModified " +		
		"order by $isCollection asc;";	
		
		String response = "";
		if(!ResourceCache.RDFInstance(repoConfig).cacheExpired()) {
			
			Object obj = ResourceCache.RDFInstance(repoConfig).getResource(collectionPID);
			if(obj != null)
				response = (String) obj;		
		}
		else {			
			ResourceCache.RDFInstance(repoConfig).clearCache();
		}
		
		if((response == null) || response.equals("")) {			
			 response = queryIndex("tuples", "itql", "CSV" , String.valueOf(Integer.MAX_VALUE), query);
		}
		ResourceCache.RDFInstance(repoConfig).addToCache(collectionPID, response);
				
		return parseResult(response, 2, collectionPID);
		
	}
	
	// getAllCollectionChildrenObjects  
	// finds all objects belonging to a collection recursing down all sub levels
	public Vector<FedoraItemInfo> getAllCollectionChildrenObjects(String collectionPID) throws Exception {
		
		String query = "select $member $subMember $isCollection " +
		 						"from <#ri> " +
		 						"where walk($member <fedora-rels-ext:isMemberOf> <" + collectionPID + "> " +
		 							"and $subMember <fedora-rels-ext:isMemberOf> $member) " +
		 							"and $subMember <fedora-rels-ext:isCollection> $isCollection " +
		 							"order by $isCollection asc;"; 
		
				
		String response = queryIndex("tuples", "itql", "CSV" , String.valueOf(Integer.MAX_VALUE), query);
		
		return parseResult(response, 3, collectionPID);
		
	}
	
	// parseResult
	// convert data columns to a collection of fedora entity collections
	private Vector<FedoraItemInfo> parseResult(String content, int numOfCols, String collectionPID) {
		
		Vector<FedoraItemInfo> itemList = new Vector<FedoraItemInfo>();
		
		String[] lines = content.split("\n");

		//If empty query
		if (lines.length == 1) {
			return null;
		}

		for (int i = 1; i < lines.length; i++) {
			String[] tokens = lines[i].split(",");
			
			FedoraItemInfo item = new FedoraItemInfo();
				
			//if two columns of results are obtained - member/isCollection
			if (numOfCols == 2) {
				
				String member = tokens[0].split("/")[1];
				String isCollection = tokens[1];
	            String label = tokens[2];
	            String owner = tokens[3];
	            String title = tokens[4];
	            String lastModified = tokens[5];
	            
				item.setIdentifier(member);
				item.setIsCollection(new Boolean(isCollection));
				String[] collectionParts = collectionPID.split("/");  // e.g. info:fedora/PID
				item.setParentPid(collectionParts[collectionParts.length - 1]);
								
		        if (owner.length() > 0)
		          item.setCreator(owner);
		        else
		          item.setCreator("NOT_SET");

		        if (title.length() > 0)
		          item.setDisplayName(title);
		        else if(label.length() > 0)
			      item.setDisplayName(label);		        	
		        else
		          item.setDisplayName("NOT_SET");

		        if (title.length() > 0)
		          item.setTitle(title);
		        else
		          item.setTitle("NOT_SET");

		        item.setIdentifier(member);
		        item.setModifiedDate(lastModified);
		        item.setOriginalFilename("NOT_SET");
		        		        
		        FedoraPrivateItemInfo privateInfo = new FedoraPrivateItemInfo();
		        privateInfo.setPid(member);
		        privateInfo.setOwnerId(item.getCreator());
		        item.setPrivateInfo(privateInfo);								
			}

			//if three columns of results are obtained - member/subMember/isCollection
			if (numOfCols == 3) {
			
				String parentId = tokens[0].split("/")[1];
				String subMember = tokens[1].split("/")[1];
				String isCollection = tokens[2];
				
				item.setIdentifier(subMember);
				item.setIsCollection(new Boolean(isCollection));							
			}
			
			//Add item to the list;
			itemList.add(item);			
		}

		return itemList;
	}
	
	//  queryIndex
	//  performs the RDF querying process
	public String queryIndex(String type, String lang, String format, String limit, String query) throws Exception {
		        
		String riSearchUrl = repoConfig.getString(DigitalRepository.CONFIG_KEY_API_M_ENDPOINT).replaceAll("(?i)/fedora/.+","/fedora/risearch");
						
		String queryParams = String.format("type=%s&lang=%s&format=%s&limit=%s&query=%s&flush=%s",
				URLEncoder.encode(type, "UTF-8"),
				URLEncoder.encode(lang, "UTF-8"),
				URLEncoder.encode(format, "UTF-8"),
				URLEncoder.encode(limit, "UTF-8"),
				URLEncoder.encode(query, "UTF-8"),
				URLEncoder.encode("true", "UTF-8"));
		try {
			String  result = httpHttpsUrlConsumer.UrlPostAsString(riSearchUrl,queryParams);
			return result;			
		} 
		catch (Exception e) {
		    throw (new Exception("[RIClient Error executing search query]" + e.toString()));
		} 

	}
		
}

// FedoraDigitalRepositoryImpl
// the CHH DigitalRepository implementation... originally from the JISC CTREP project and now heavily modified under the JISC CLIF project
public class FedoraDigitalRepositoryImpl implements DigitalRepository, HttpHttpsUrlConsumer {
  /** The format and version of ingest messages */
  private static final String LOG_MARKER = "[CTREP-CLIF:FedoraDigitalRepositoryImpl] ";	
  private static final String DIGITAL_OBJECT_FORMAT_FOXML = "info:fedora/fedora-system:FOXML-1.0";
  private static String FEDORA_OBJECT_NAMESPACE = "";	
  private static String FEDORA_ROOT_COLLECTION = "";  
  private static final boolean INLINE_UPDATE = true;
  private static final boolean NOT_INLINE_UPDATE = false;

  private static final Log log = LogFactory.getLog(FedoraDigitalRepositoryImpl.class);

  /** Connection information. This comes from the mountpoint XML file */
  private PropertyResourceBundle repoConfig = null;
  /** Global keystore settings. These come via the constructor */
  private String keystorePath = null;
  private String keystorePassword = null;
  /** Global truststore settings. These come via the constructor */
  private String truststorePath = null;
  private String truststorePassword = null;
  /** The Axis2 authentication mechanism for communicating with the remote Fedora */
  private HttpTransportProperties.Authenticator authenticator = null;
  /** Our custom protocol handler which supports SSL probing */
  private Protocol customProtocolHandler = null;
  private boolean debug = false;
  private String debugOutputPath = null;

//CLIF  for AXIS2 test framework
  public FedoraDigitalRepositoryImpl()
  {
	  super();
  }
  
  public FedoraDigitalRepositoryImpl(String keystorePath, String keystorePassword,
                                     String truststorePath, String truststorePassword,
                                     boolean debug, String debugOutputPath) {
    this.keystorePath = keystorePath;
    this.keystorePassword = keystorePassword;
    this.truststorePath = truststorePath;
    this.truststorePassword = truststorePassword;
    this.debug = debug;
    this.debugOutputPath = debugOutputPath;
  }

  // getRepoConfig
  // returns the configuration data object for this class instance
  public PropertyResourceBundle getRepoConfig() {
    return repoConfig;
  }

  // generateItem
  // constructs a new DigitalItemInfo instance
  public DigitalItemInfo generateItem() {
    FedoraItemInfo item = new FedoraItemInfo();
    FedoraPrivateItemInfo privateInfo = new FedoraPrivateItemInfo();
    item.setPrivateInfo(privateInfo);
    return item;
  }
  
  // init
  // performs class initialisation..must be done prior to using the class 
  public void init(PropertyResourceBundle repoConfig) {
    this.repoConfig = repoConfig;

    log.info(LOG_MARKER + "init:Commence");    
    FEDORA_OBJECT_NAMESPACE = repoConfig.getString(DigitalRepository.CONFIG_KEY_CLIF_FEDORA_OBJECT_NAMESPACE);
    FEDORA_ROOT_COLLECTION = repoConfig.getString(DigitalRepository.CONFIG_KEY_CLIF_FEDORA_ROOT_COLLECTION);
    
    // Prepare the authentication details for a web service request, i.e. fedora admin username and password
    authenticator = new HttpTransportProperties.Authenticator();
    authenticator.setUsername(repoConfig.getString(DigitalRepository.CONFIG_KEY_CONNECTION_USERNAME));
    authenticator.setPassword(repoConfig.getString(DigitalRepository.CONFIG_KEY_CONNECTION_PASSWORD));

    // This is essential as otherwise the auth creds are not sent with the initial request
    authenticator.setPreemptiveAuthentication(true);

    // We need a custom protocol handler to do SSL probing so we don't have to bother with importing certs
    boolean useGuanxi = Boolean.parseBoolean(repoConfig.getString(DigitalRepository.CONFIG_KEY_CLIF_USE_GUANXI_HTTPS));

    try {
        if(useGuanxi) 
        {    
        	customProtocolHandler = new Protocol("https", new FedoraProtocolSocketFactory(keystorePath, keystorePassword,
                                                                             truststorePath, truststorePassword),
                                                                             Integer.parseInt(repoConfig.getString(DigitalRepository.CONFIG_KEY_CLIF_HTTPS_PORT)));
        
        	EntityConnection connection = new EntityConnection(repoConfig.getString(CONFIG_KEY_API_M_ENDPOINT),
                "test-keystore-alias",
                keystorePath, keystorePassword,
                truststorePath, truststorePassword,
                EntityConnection.PROBING_ON);
        	
			X509Certificate fedoraX509 = connection.getServerCertificate();
			KeyStore fedoraTrustStore = KeyStore.getInstance("jks");
			fedoraTrustStore.load(new FileInputStream(truststorePath), truststorePassword.toCharArray());
			// ...under it's Subject DN as an alias...
			fedoraTrustStore.setCertificateEntry(fedoraX509.getSubjectDN().toString(), fedoraX509);
			// ...and rewrite the trust store
			fedoraTrustStore.store(new FileOutputStream(truststorePath), truststorePassword.toCharArray());        
        }
        else
        {
        	customProtocolHandler = Protocol.getProtocol("http");  // CLIF hydra
        }    	

    }
    catch(Exception e) {
      log.error(LOG_MARKER + e);
    }
    log.info(LOG_MARKER + "init:Finish");      
  }

  // createFolder
  // adds a new folder to the fedora repository
  public String createFolder(DigitalItemInfo item) {  
  
	    try {
	        log.info(LOG_MARKER + "createFolder:Commence");	    	
	        // Initiate the client connection to the API-M endpoint
	        FedoraAPIMServiceStub stub = new FedoraAPIMServiceStub(repoConfig.getString(CONFIG_KEY_API_M_ENDPOINT));

	        // Add the auth creds to the client
	        stub._getServiceClient().getOptions().setProperty(HTTPConstants.AUTHENTICATE, authenticator);
	        // Register our custom SSL handler for this connection
	        stub._getServiceClient().getOptions().setProperty(HTTPConstants.CUSTOM_PROTOCOL_HANDLER, customProtocolHandler);
		  
		    IngestDocument doc = IngestDocument.Factory.newInstance();
		
		    IngestDocument.Ingest ingest = doc.addNewIngest();
		    // java.lang.AssertionError: fedora.server.errors.ObjectValidityException: [DOValidatorImpl]: failed Schematron rules validation. null
		    // if this is wrong
		    ingest.setFormat(DIGITAL_OBJECT_FORMAT_FOXML);
		
		    DigitalObjectDocument objectDoc = DigitalObjectDocument.Factory.newInstance();
		    DigitalObjectDocument.DigitalObject object = objectDoc.addNewDigitalObject();
		
		    /* CLIF */	    	   	    
		    GetNextPID getNextPID = GetNextPID.Factory.newInstance();
			getNextPID.setPidNamespace(FEDORA_OBJECT_NAMESPACE);
			getNextPID.setNumPIDs(BigInteger.valueOf((long) 1));
				    
			GetNextPIDDocument getNextPIDDoc = GetNextPIDDocument.Factory.newInstance();
			getNextPIDDoc.setGetNextPID(getNextPID);
			
		    GetNextPIDResponseDocument newPIDResponseDoc = stub.getNextPID(getNextPIDDoc);
		    String newPID = newPIDResponseDoc.getGetNextPIDResponse().getPidArray()[0];
		    ((FedoraPrivateItemInfo)(item.getPrivateInfo())).setPid(newPID);
		    object.setPID(newPID);
		    /* CLIF END */
		    // /////////////////////////////////////////////////////////////////////////////////////////////////
		    // Fedora object properties
		    ObjectPropertiesType objectProperties = object.addNewObjectProperties();
		
		    // <foxml:property NAME="http://www.w3.org/1999/02/22-rdf-syntax-ns#type" VALUE="FedoraObject"/>
		    PropertyType typeProperty = objectProperties.addNewProperty();
		    typeProperty.setNAME(PropertyType.NAME.HTTP_WWW_W_3_ORG_1999_02_22_RDF_SYNTAX_NS_TYPE);
		    typeProperty.setVALUE("FedoraObject");
		
		    // <foxml:property NAME="info:fedora/fedora-system:def/model#state" VALUE="A"/>
		    PropertyType stateProperty = objectProperties.addNewProperty();
		    stateProperty.setNAME(PropertyType.NAME.INFO_FEDORA_FEDORA_SYSTEM_DEF_MODEL_STATE);
		    stateProperty.setVALUE(StateType.A.toString());
		
		    // <foxml:property NAME="info:fedora/fedora-system:def/model#label" VALUE="FOXML Reference Example"/>
		    PropertyType labelProperty = objectProperties.addNewProperty();
		    labelProperty.setNAME(PropertyType.NAME.INFO_FEDORA_FEDORA_SYSTEM_DEF_MODEL_LABEL);
		    labelProperty.setVALUE(item.getDisplayName());
		
		    PropertyType ownerProperty = objectProperties.addNewProperty();  // CLIF new
		    ownerProperty.setNAME(PropertyType.NAME.INFO_FEDORA_FEDORA_SYSTEM_DEF_MODEL_OWNER_ID);
		    ownerProperty.setVALUE(item.getCreator());
		    
		    // <foxml:property NAME="info:fedora/fedora-system:def/model#contentModel" VALUE="TEST_IMAGE"/>
		    PropertyType contentModelProperty = objectProperties.addNewProperty();
		    contentModelProperty.setNAME(PropertyType.NAME.INFO_FEDORA_FEDORA_SYSTEM_DEF_MODEL_CONTENT_MODEL);
		    contentModelProperty.setVALUE("text/plain");
		
		    // /////////////////////////////////////////////////////////////////////////////////////////////////
		
		    buildNewObjectDataStreams(item, object, true);
		    // /////////////////////////////////////////////////////////////////////////////////////////////////
				
		    XmlCursor cursor = objectDoc.newCursor();
		    if (cursor.toFirstChild())
		      cursor.setAttributeText(new QName("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation"),
		                                        "info:fedora/fedora-system:def/foxml# http://www.fedora.info/definitions/1/0/foxml1-0.xsd");
		
		    ingest.setObjectXML(Utils.xmlToString(objectDoc).getBytes());
		
		    if (debug) {
		      Utils.dumpXML(objectDoc, debugOutputPath + System.getProperty("file.separator") + "ingest.xml");
		    }

	      // Call the web service
	       IngestResponseDocument outDoc = stub.ingest(doc);

	       if (!outDoc.getIngestResponse().getObjectPID().equals(((FedoraPrivateItemInfo)(item.getPrivateInfo())).getPid())) {
	         return null;
	       }

		   log.info(LOG_MARKER + "createFolder:Finish");	       
	       return newPID;
	     }
	     catch(Exception e) {
	      log.error(LOG_MARKER + e);
	      return null;
	    }	  
	  	  
  }
  
  // createObject
  // adds a new fedora resource object to the repository
  public String createObject(DigitalItemInfo item) {
    // Build a new request document
    try {
        // Initiate the client connection to the API-M endpoint
        FedoraAPIMServiceStub stub = new FedoraAPIMServiceStub(repoConfig.getString(CONFIG_KEY_API_M_ENDPOINT));

        // Add the auth creds to the client
        stub._getServiceClient().getOptions().setProperty(HTTPConstants.AUTHENTICATE, authenticator);
        // Register our custom SSL handler for this connection
        stub._getServiceClient().getOptions().setProperty(HTTPConstants.CUSTOM_PROTOCOL_HANDLER, customProtocolHandler);
	  
	    IngestDocument doc = IngestDocument.Factory.newInstance();
	
	    IngestDocument.Ingest ingest = doc.addNewIngest();
	    // java.lang.AssertionError: fedora.server.errors.ObjectValidityException: [DOValidatorImpl]: failed Schematron rules validation. null
	    // if this is wrong
	    ingest.setFormat(DIGITAL_OBJECT_FORMAT_FOXML);
 	
	    DigitalObjectDocument objectDoc = DigitalObjectDocument.Factory.newInstance();
	    DigitalObjectDocument.DigitalObject object = objectDoc.addNewDigitalObject();
	
	    /* CLIF */	    	   	    
	    String newPID = getNextPid(stub);
	    ((FedoraPrivateItemInfo)(item.getPrivateInfo())).setPid(newPID);
	    object.setPID(newPID);
	    /* CLIF END */
	    // /////////////////////////////////////////////////////////////////////////////////////////////////
	    // Fedora object properties
	    ObjectPropertiesType objectProperties = object.addNewObjectProperties();
	
	    // <foxml:property NAME="http://www.w3.org/1999/02/22-rdf-syntax-ns#type" VALUE="FedoraObject"/>
	    PropertyType typeProperty = objectProperties.addNewProperty();
	    typeProperty.setNAME(PropertyType.NAME.HTTP_WWW_W_3_ORG_1999_02_22_RDF_SYNTAX_NS_TYPE);
	    typeProperty.setVALUE("FedoraObject");
	
	    // <foxml:property NAME="info:fedora/fedora-system:def/model#state" VALUE="A"/>
	    PropertyType stateProperty = objectProperties.addNewProperty();
	    stateProperty.setNAME(PropertyType.NAME.INFO_FEDORA_FEDORA_SYSTEM_DEF_MODEL_STATE);
	    stateProperty.setVALUE(StateType.A.toString());
	
	    // <foxml:property NAME="info:fedora/fedora-system:def/model#label" VALUE="FOXML Reference Example"/>
	    PropertyType labelProperty = objectProperties.addNewProperty();
	    labelProperty.setNAME(PropertyType.NAME.INFO_FEDORA_FEDORA_SYSTEM_DEF_MODEL_LABEL);
	    labelProperty.setVALUE(item.getTitle());// CLIF from item.getPrivateInfo())).getPid());
	
	    PropertyType ownerProperty = objectProperties.addNewProperty();  // CLIF new
	    ownerProperty.setNAME(PropertyType.NAME.INFO_FEDORA_FEDORA_SYSTEM_DEF_MODEL_OWNER_ID);
	    ownerProperty.setVALUE(item.getCreator());
	    
	    // <foxml:property NAME="info:fedora/fedora-system:def/model#contentModel" VALUE="TEST_IMAGE"/>
	    PropertyType contentModelProperty = objectProperties.addNewProperty();
	    contentModelProperty.setNAME(PropertyType.NAME.INFO_FEDORA_FEDORA_SYSTEM_DEF_MODEL_CONTENT_MODEL);
	    contentModelProperty.setVALUE("text/plain");
	
	    // /////////////////////////////////////////////////////////////////////////////////////////////////
	
	    buildNewObjectDataStreams(item, object, false);
	    
	    // /////////////////////////////////////////////////////////////////////////////////////////////////
	    // Content Datastream for the object
	    DatastreamType objDatastream = object.addNewDatastream();
	    objDatastream.setID("content");//setID("PDF");   CLIF
	    objDatastream.setSTATE(StateType.A);	    
	    objDatastream.setCONTROLGROUP(DatastreamType.CONTROLGROUP.M);

	    DatastreamVersionType objDatastreamVersion = objDatastream.addNewDatastreamVersion();
	    objDatastreamVersion.setID("content.0");//setID("TEST");  CLIF
	    
	    String mimeType = item.getMimeType();
	    if((mimeType == null) || (mimeType.equals("")) || (mimeType.equals("application/octet-stream"))) {	    
	    	item.setMimeType(getContentType(item.getTitle()));
	    }
	    objDatastreamVersion.setMIMETYPE(item.getMimeType());
	    objDatastreamVersion.setLABEL(item.getTitle());
	
	    objDatastreamVersion.setBinaryContent(item.getBinaryContent());
	    // /////////////////////////////////////////////////////////////////////////////////////////////////
		
	    XmlCursor cursor = objectDoc.newCursor();
	    if (cursor.toFirstChild())
	      cursor.setAttributeText(new QName("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation"),
	                                        "info:fedora/fedora-system:def/foxml# http://www.fedora.info/definitions/1/0/foxml1-0.xsd");
	
	    ingest.setObjectXML(Utils.xmlToString(objectDoc).getBytes());
	
	    if (debug) {
	      Utils.dumpXML(objectDoc, debugOutputPath + System.getProperty("file.separator") + "ingest.xml");
	    }

      // Call the web service
       IngestResponseDocument outDoc = stub.ingest(doc);

       if (!outDoc.getIngestResponse().getObjectPID().equals(((FedoraPrivateItemInfo)(item.getPrivateInfo())).getPid())) {
         return null;
       }

       return newPID;
     }
     catch(Exception e) {
      log.error(LOG_MARKER + e);
      return null;
    }
  }

// getNextPid
// reserves a new persistent Id in the repository  
private String getNextPid(FedoraAPIMServiceStub stub) throws java.rmi.RemoteException {
	GetNextPID getNextPID = GetNextPID.Factory.newInstance();
	getNextPID.setPidNamespace(FEDORA_OBJECT_NAMESPACE);
	getNextPID.setNumPIDs(BigInteger.valueOf((long) 1));
		    
	GetNextPIDDocument getNextPIDDoc = GetNextPIDDocument.Factory.newInstance();
	getNextPIDDoc.setGetNextPID(getNextPID);
	
	GetNextPIDResponseDocument newPIDResponseDoc = stub.getNextPID(getNextPIDDoc);
	String newPID = newPIDResponseDoc.getGetNextPIDResponse().getPidArray()[0];
	return newPID;
}

// getNextPid
// reserves a new persistent Id in the repository 
public String getNextPid() throws java.rmi.RemoteException {
	
    FedoraAPIMServiceStub stub = new FedoraAPIMServiceStub(repoConfig.getString(CONFIG_KEY_API_M_ENDPOINT));
    stub._getServiceClient().getOptions().setProperty(HTTPConstants.AUTHENTICATE, authenticator);
    stub._getServiceClient().getOptions().setProperty(HTTPConstants.CUSTOM_PROTOCOL_HANDLER, customProtocolHandler);

	
	GetNextPID getNextPID = GetNextPID.Factory.newInstance();
	getNextPID.setPidNamespace(FEDORA_OBJECT_NAMESPACE);
	getNextPID.setNumPIDs(BigInteger.valueOf((long) 1));
		    
	GetNextPIDDocument getNextPIDDoc = GetNextPIDDocument.Factory.newInstance();
	getNextPIDDoc.setGetNextPID(getNextPID);
	
	GetNextPIDResponseDocument newPIDResponseDoc = stub.getNextPID(getNextPIDDoc);
	String newPID = newPIDResponseDoc.getGetNextPIDResponse().getPidArray()[0];
	return newPID;
}

  //  getContentType 
  //  a more up to date alternative to javax.activation.MimetypesFileTypeMap 
  private String getContentType(String filePath)
  {	  
	  int dot = filePath.lastIndexOf(".");
	  String ext = "." + filePath.substring(dot + 1).toLowerCase();
	  
      if(ext.equals(".3dm")) return "x-world/x-3dmf";
      if(ext.equals(".3dmf")) return "x-world/x-3dmf";
      if(ext.equals(".a")) return "application/octet-stream";
      if(ext.equals(".aab")) return "application/x-authorware-bin";
      if(ext.equals(".aam")) return "application/x-authorware-map";
      if(ext.equals(".aas")) return "application/x-authorware-seg";
      if(ext.equals(".abc")) return "text/vnd.abc";
      if(ext.equals(".acgi")) return "text/html";
      if(ext.equals(".afl")) return "video/animaflex";
      if(ext.equals(".ai")) return "application/postscript";
      if(ext.equals(".aif")) return "audio/aiff";
      if(ext.equals(".aifc")) return "audio/aiff";
      if(ext.equals(".aiff")) return "audio/aiff";
      if(ext.equals(".aim")) return "application/x-aim";
      if(ext.equals(".aip")) return "text/x-audiosoft-intra";
      if(ext.equals(".ani")) return "application/x-navi-animation";
      if(ext.equals(".aos")) return "application/x-nokia-9000-communicator-add-on-software";
      if(ext.equals(".aps")) return "application/mime";
      if(ext.equals(".arc")) return "application/octet-stream";
      if(ext.equals(".arj")) return "application/arj";
      if(ext.equals(".art")) return "image/x-jg";
      if(ext.equals(".asf")) return "video/x-ms-asf";
      if(ext.equals(".asm")) return "text/x-asm";
      if(ext.equals(".asp")) return "text/asp";
      if(ext.equals(".asx")) return "video/x-ms-asf";
      if(ext.equals(".au")) return "audio/basic";
      if(ext.equals(".avi")) return "video/avi";
      if(ext.equals(".avs")) return "video/avs-video";
      if(ext.equals(".bcpio")) return "application/x-bcpio";
      if(ext.equals(".bin")) return "application/octet-stream";
      if(ext.equals(".bm")) return "image/bmp";
      if(ext.equals(".bmp")) return "image/bmp";
      if(ext.equals(".boo")) return "application/book";
      if(ext.equals(".book")) return "application/book";
      if(ext.equals(".boz")) return "application/x-bzip2";
      if(ext.equals(".bsh")) return "application/x-bsh";
      if(ext.equals(".bz")) return "application/x-bzip";
      if(ext.equals(".bz2")) return "application/x-bzip2";
      if(ext.equals(".c")) return "text/plain";
      if(ext.equals(".c++")) return "text/plain";
      if(ext.equals(".cat")) return "application/vnd.ms-pki.seccat";
      if(ext.equals(".cc")) return "text/plain";
      if(ext.equals(".ccad")) return "application/clariscad";
      if(ext.equals(".cco")) return "application/x-cocoa";
      if(ext.equals(".cdf")) return "application/cdf";
      if(ext.equals(".cer")) return "application/pkix-cert";
      if(ext.equals(".cha")) return "application/x-chat";
      if(ext.equals(".chat")) return "application/x-chat";
      if(ext.equals(".class")) return "application/java";
      if(ext.equals(".com")) return "application/octet-stream";
      if(ext.equals(".conf")) return "text/plain";
      if(ext.equals(".cpio")) return "application/x-cpio";
      if(ext.equals(".cpp")) return "text/x-c";
      if(ext.equals(".cpt")) return "application/x-cpt";
      if(ext.equals(".crl")) return "application/pkcs-crl";
      if(ext.equals(".crt")) return "application/pkix-cert";
      if(ext.equals(".csh")) return "application/x-csh";
      if(ext.equals(".css")) return "text/css";
      if(ext.equals(".cxx")) return "text/plain";
      if(ext.equals(".dcr")) return "application/x-director";
      if(ext.equals(".deepv")) return "application/x-deepv";
      if(ext.equals(".def")) return "text/plain";
      if(ext.equals(".der")) return "application/x-x509-ca-cert";
      if(ext.equals(".dif")) return "video/x-dv";
      if(ext.equals(".dir")) return "application/x-director";
      if(ext.equals(".dl")) return "video/dl";
      if(ext.equals(".doc")) return "application/msword";
      if(ext.equals(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
      if(ext.equals(".dot")) return "application/msword";
      if(ext.equals(".dotm")) return "application/vnd.ms-word.template.macroEnabled.12";
      if(ext.equals(".dotx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.template";
      if(ext.equals(".dp")) return "application/commonground";
      if(ext.equals(".drw")) return "application/drafting";
      if(ext.equals(".dump")) return "application/octet-stream";
      if(ext.equals(".dv")) return "video/x-dv";
      if(ext.equals(".dvi")) return "application/x-dvi";
      if(ext.equals(".dwf")) return "model/vnd.dwf";
      if(ext.equals(".dwg")) return "image/vnd.dwg";
      if(ext.equals(".dxf")) return "image/vnd.dwg";
      if(ext.equals(".dxr")) return "application/x-director";
      if(ext.equals(".el")) return "text/x-script.elisp";
      if(ext.equals(".elc")) return "application/x-elc";
      if(ext.equals(".env")) return "application/x-envoy";
      if(ext.equals(".eps")) return "application/postscript";
      if(ext.equals(".es")) return "application/x-esrehber";
      if(ext.equals(".etx")) return "text/x-setext";
      if(ext.equals(".evy")) return "application/envoy";
      if(ext.equals(".exe")) return "application/octet-stream";
      if(ext.equals(".f")) return "text/plain";
      if(ext.equals(".f77")) return "text/x-fortran";
      if(ext.equals(".f90")) return "text/plain";
      if(ext.equals(".fdf")) return "application/vnd.fdf";
      if(ext.equals(".fif")) return "image/fif";
      if(ext.equals(".fli")) return "video/fli";
      if(ext.equals(".flo")) return "image/florian";
      if(ext.equals(".flx")) return "text/vnd.fmi.flexstor";
      if(ext.equals(".fmf")) return "video/x-atomic3d-feature";
      if(ext.equals(".for")) return "text/x-fortran";
      if(ext.equals(".fpx")) return "image/vnd.fpx";
      if(ext.equals(".frl")) return "application/freeloader";
      if(ext.equals(".funk")) return "audio/make";
      if(ext.equals(".g")) return "text/plain";
      if(ext.equals(".g3")) return "image/g3fax";
      if(ext.equals(".gif")) return "image/gif";
      if(ext.equals(".gl")) return "video/gl";
      if(ext.equals(".gsd")) return "audio/x-gsm";
      if(ext.equals(".gsm")) return "audio/x-gsm";
      if(ext.equals(".gsp")) return "application/x-gsp";
      if(ext.equals(".gss")) return "application/x-gss";
      if(ext.equals(".gtar")) return "application/x-gtar";
      if(ext.equals(".gz")) return "application/x-gzip";
      if(ext.equals(".gzip")) return "application/x-gzip";
      if(ext.equals(".h")) return "text/plain";
      if(ext.equals(".hdf")) return "application/x-hdf";
      if(ext.equals(".help")) return "application/x-helpfile";
      if(ext.equals(".hgl")) return "application/vnd.hp-hpgl";
      if(ext.equals(".hh")) return "text/plain";
      if(ext.equals(".hlb")) return "text/x-script";
      if(ext.equals(".hlp")) return "application/hlp";
      if(ext.equals(".hpg")) return "application/vnd.hp-hpgl";
      if(ext.equals(".hpgl")) return "application/vnd.hp-hpgl";
      if(ext.equals(".hqx")) return "application/binhex";
      if(ext.equals(".hta")) return "application/hta";
      if(ext.equals(".htc")) return "text/x-component";
      if(ext.equals(".htm")) return "text/html";
      if(ext.equals(".html")) return "text/html";
      if(ext.equals(".htmls")) return "text/html";
      if(ext.equals(".htt")) return "text/webviewhtml";
      if(ext.equals(".htx")) return "text/html";
      if(ext.equals(".ice")) return "x-conference/x-cooltalk";
      if(ext.equals(".ico")) return "image/x-icon";
      if(ext.equals(".idc")) return "text/plain";
      if(ext.equals(".ief")) return "image/ief";
      if(ext.equals(".iefs")) return "image/ief";
      if(ext.equals(".iges")) return "application/iges";
      if(ext.equals(".igs")) return "application/iges";
      if(ext.equals(".ima")) return "application/x-ima";
      if(ext.equals(".imap")) return "application/x-httpd-imap";
      if(ext.equals(".inf")) return "application/inf";
      if(ext.equals(".ins")) return "application/x-internett-signup";
      if(ext.equals(".ip")) return "application/x-ip2";
      if(ext.equals(".isu")) return "video/x-isvideo";
      if(ext.equals(".it")) return "audio/it";
      if(ext.equals(".iv")) return "application/x-inventor";
      if(ext.equals(".ivr")) return "i-world/i-vrml";
      if(ext.equals(".ivy")) return "application/x-livescreen";
      if(ext.equals(".jam")) return "audio/x-jam";
      if(ext.equals(".jav")) return "text/plain";
      if(ext.equals(".java")) return "text/plain";
      if(ext.equals(".jcm")) return "application/x-java-commerce";
      if(ext.equals(".jfif")) return "image/jpeg";
      if(ext.equals(".jfif-tbnl")) return "image/jpeg";
      if(ext.equals(".jpe")) return "image/jpeg";
      if(ext.equals(".jpeg")) return "image/jpeg";
      if(ext.equals(".jpg")) return "image/jpeg";
      if(ext.equals(".jps")) return "image/x-jps";
      if(ext.equals(".js")) return "application/x-javascript";
      if(ext.equals(".jut")) return "image/jutvision";
      if(ext.equals(".kar")) return "audio/midi";
      if(ext.equals(".ksh")) return "application/x-ksh";
      if(ext.equals(".la")) return "audio/nspaudio";
      if(ext.equals(".lam")) return "audio/x-liveaudio";
      if(ext.equals(".latex")) return "application/x-latex";
      if(ext.equals(".lha")) return "application/octet-stream";
      if(ext.equals(".lhx")) return "application/octet-stream";
      if(ext.equals(".list")) return "text/plain";
      if(ext.equals(".lma")) return "audio/nspaudio";
      if(ext.equals(".log")) return "text/plain";
      if(ext.equals(".lsp")) return "application/x-lisp";
      if(ext.equals(".lst")) return "text/plain";
      if(ext.equals(".lsx")) return "text/x-la-asf";
      if(ext.equals(".ltx")) return "application/x-latex";
      if(ext.equals(".lzh")) return "application/octet-stream";
      if(ext.equals(".lzx")) return "application/octet-stream";
      if(ext.equals(".m")) return "text/plain";
      if(ext.equals(".m1v")) return "video/mpeg";
      if(ext.equals(".m2a")) return "audio/mpeg";
      if(ext.equals(".m2v")) return "video/mpeg";
      if(ext.equals(".m3u")) return "audio/x-mpequrl";
      if(ext.equals(".man")) return "application/x-troff-man";
      if(ext.equals(".map")) return "application/x-navimap";
      if(ext.equals(".mar")) return "text/plain";
      if(ext.equals(".mbd")) return "application/mbedlet";
      if(ext.equals(".mc$")) return "application/x-magic-cap-package-1.0";
      if(ext.equals(".mcd")) return "application/mcad";
      if(ext.equals(".mcf")) return "text/mcf";
      if(ext.equals(".mcp")) return "application/netmc";
      if(ext.equals(".me")) return "application/x-troff-me";
      if(ext.equals(".mht")) return "message/rfc822";
      if(ext.equals(".mhtml")) return "message/rfc822";
      if(ext.equals(".mid")) return "audio/midi";
      if(ext.equals(".midi")) return "audio/midi";
      if(ext.equals(".mif")) return "application/x-mif";
      if(ext.equals(".mime")) return "message/rfc822";
      if(ext.equals(".mjf")) return "audio/x-vnd.audioexplosion.mjuicemediafile";
      if(ext.equals(".mjpg")) return "video/x-motion-jpeg";
      if(ext.equals(".mm")) return "application/base64";
      if(ext.equals(".mme")) return "application/base64";
      if(ext.equals(".mod")) return "audio/mod";
      if(ext.equals(".moov")) return "video/quicktime";
      if(ext.equals(".mov")) return "video/quicktime";
      if(ext.equals(".movie")) return "video/x-sgi-movie";
      if(ext.equals(".mp2")) return "audio/mpeg";
      if(ext.equals(".mp3")) return "audio/mpeg";
      if(ext.equals(".mpa")) return "audio/mpeg";
      if(ext.equals(".mpc")) return "application/x-project";
      if(ext.equals(".mpe")) return "video/mpeg";
      if(ext.equals(".mpeg")) return "video/mpeg";
      if(ext.equals(".mpg")) return "video/mpeg";
      if(ext.equals(".mpga")) return "audio/mpeg";
      if(ext.equals(".mpp")) return "application/vnd.ms-project";
      if(ext.equals(".mpt")) return "application/vnd.ms-project";
      if(ext.equals(".mpv")) return "application/vnd.ms-project";
      if(ext.equals(".mpx")) return "application/vnd.ms-project";
      if(ext.equals(".mrc")) return "application/marc";
      if(ext.equals(".ms")) return "application/x-troff-ms";
      if(ext.equals(".mv")) return "video/x-sgi-movie";
      if(ext.equals(".my")) return "audio/make";
      if(ext.equals(".mzz")) return "application/x-vnd.audioexplosion.mzz";
      if(ext.equals(".nap")) return "image/naplps";
      if(ext.equals(".naplps")) return "image/naplps";
      if(ext.equals(".nc")) return "application/x-netcdf";
      if(ext.equals(".ncm")) return "application/vnd.nokia.configuration-message";
      if(ext.equals(".nif")) return "image/x-niff";
      if(ext.equals(".niff")) return "image/x-niff";
      if(ext.equals(".nix")) return "application/x-mix-transfer";
      if(ext.equals(".nsc")) return "application/x-conference";
      if(ext.equals(".nvd")) return "application/x-navidoc";
      if(ext.equals(".o")) return "application/octet-stream";
      if(ext.equals(".oda")) return "application/oda";
      if(ext.equals(".omc")) return "application/x-omc";
      if(ext.equals(".omcd")) return "application/x-omcdatamaker";
      if(ext.equals(".omcr")) return "application/x-omcregerator";
      if(ext.equals(".p")) return "text/x-pascal";
      if(ext.equals(".p10")) return "application/pkcs10";
      if(ext.equals(".p12")) return "application/pkcs-12";
      if(ext.equals(".p7a")) return "application/x-pkcs7-signature";
      if(ext.equals(".p7c")) return "application/pkcs7-mime";
      if(ext.equals(".p7m")) return "application/pkcs7-mime";
      if(ext.equals(".p7r")) return "application/x-pkcs7-certreqresp";
      if(ext.equals(".p7s")) return "application/pkcs7-signature";
      if(ext.equals(".part")) return "application/pro_eng";
      if(ext.equals(".pas")) return "text/pascal";
      if(ext.equals(".pbm")) return "image/x-portable-bitmap";
      if(ext.equals(".pcl")) return "application/vnd.hp-pcl";
      if(ext.equals(".pct")) return "image/x-pict";
      if(ext.equals(".pcx")) return "image/x-pcx";
      if(ext.equals(".pdb")) return "chemical/x-pdb";
      if(ext.equals(".pdf")) return "application/pdf";
      if(ext.equals(".pfunk")) return "audio/make";
      if(ext.equals(".pgm")) return "image/x-portable-greymap";
      if(ext.equals(".pic")) return "image/pict";
      if(ext.equals(".pict")) return "image/pict";
      if(ext.equals(".pkg")) return "application/x-newton-compatible-pkg";
      if(ext.equals(".pko")) return "application/vnd.ms-pki.pko";
      if(ext.equals(".pl")) return "text/plain";
      if(ext.equals(".ply")) return "application/acad";
      if(ext.equals(".plx")) return "application/x-pixclscript";
      if(ext.equals(".pm")) return "image/x-xpixmap";
      if(ext.equals(".pm4")) return "application/x-pagemaker";
      if(ext.equals(".pm5")) return "application/x-pagemaker";
      if(ext.equals(".png")) return "image/png";
      if(ext.equals(".pnm")) return "application/x-portable-anymap";
      if(ext.equals(".pot")) return "application/vnd.ms-powerpoint";
      if(ext.equals(".potm")) return "application/vnd.ms-powerpoint.template.macroEnabled.12";
      if(ext.equals(".potx")) return "application/vnd.openxmlformats-officedocument.presentationml.template";
      if(ext.equals(".pov")) return "model/x-pov";
      if(ext.equals(".ppa")) return "application/vnd.ms-powerpoint";
      if(ext.equals(".ppam")) return "application/vnd.ms-powerpoint.addin.macroEnabled.12";
      if(ext.equals(".ppm")) return "image/x-portable-pixmap";
      if(ext.equals(".pps")) return "application/vnd.ms-powerpoint";
      if(ext.equals(".ppsm")) return "application/vnd.ms-powerpoint.slideshow.macroEnabled.12";
      if(ext.equals(".ppsx")) return "application/vnd.openxmlformats-officedocument.presentationml.slideshow";
      if(ext.equals(".pptm")) return "application/vnd.ms-powerpoint.presentation.macroEnabled.12";
      if(ext.equals(".pptx")) return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
      if(ext.equals(".ppt")) return "application/vnd.ms-powerpoint";
      if(ext.equals(".ppz")) return "application/vnd.ms-powerpoint";
      if(ext.equals(".pre")) return "application/x-freelance";
      if(ext.equals(".prt")) return "application/pro_eng";
      if(ext.equals(".ps")) return "application/postscript";
      if(ext.equals(".psd")) return "application/octet-stream";
      if(ext.equals(".pvu")) return "paleovu/x-pv";
      if(ext.equals(".pwz")) return "application/vnd.ms-powerpoint";
      if(ext.equals(".py")) return "text/x-script.phyton";
      if(ext.equals(".pyc")) return "applicaiton/x-bytecode.python";
      if(ext.equals(".qcp")) return "audio/vnd.qcelp";
      if(ext.equals(".qd3")) return "x-world/x-3dmf";
      if(ext.equals(".qd3d")) return "x-world/x-3dmf";
      if(ext.equals(".qif")) return "image/x-quicktime";
      if(ext.equals(".qt")) return "video/quicktime";
      if(ext.equals(".qtc")) return "video/x-qtc";
      if(ext.equals(".qti")) return "image/x-quicktime";
      if(ext.equals(".qtif")) return "image/x-quicktime";
      if(ext.equals(".ra")) return "audio/x-pn-realaudio";
      if(ext.equals(".ram")) return "audio/x-pn-realaudio";
      if(ext.equals(".ras")) return "application/x-cmu-raster";
      if(ext.equals(".rast")) return "image/cmu-raster";
      if(ext.equals(".rexx")) return "text/x-script.rexx";
      if(ext.equals(".rf")) return "image/vnd.rn-realflash";
      if(ext.equals(".rgb")) return "image/x-rgb";
      if(ext.equals(".rm")) return "application/vnd.rn-realmedia";
      if(ext.equals(".rmi")) return "audio/mid";
      if(ext.equals(".rmm")) return "audio/x-pn-realaudio";
      if(ext.equals(".rmp")) return "audio/x-pn-realaudio";
      if(ext.equals(".rng")) return "application/ringing-tones";
      if(ext.equals(".rnx")) return "application/vnd.rn-realplayer";
      if(ext.equals(".roff")) return "application/x-troff";
      if(ext.equals(".rp")) return "image/vnd.rn-realpix";
      if(ext.equals(".rpm")) return "audio/x-pn-realaudio-plugin";
      if(ext.equals(".rt")) return "text/richtext";
      if(ext.equals(".rtf")) return "text/richtext";
      if(ext.equals(".rtx")) return "text/richtext";
      if(ext.equals(".rv")) return "video/vnd.rn-realvideo";
      if(ext.equals(".s")) return "text/x-asm";
      if(ext.equals(".s3m")) return "audio/s3m";
      if(ext.equals(".saveme")) return "application/octet-stream";
      if(ext.equals(".sbk")) return "application/x-tbook";
      if(ext.equals(".scm")) return "application/x-lotusscreencam";
      if(ext.equals(".sdml")) return "text/plain";
      if(ext.equals(".sdp")) return "application/sdp";
      if(ext.equals(".sdr")) return "application/sounder";
      if(ext.equals(".sea")) return "application/sea";
      if(ext.equals(".set")) return "application/set";
      if(ext.equals(".sgm")) return "text/sgml";
      if(ext.equals(".sgml")) return "text/sgml";
      if(ext.equals(".sh")) return "application/x-sh";
      if(ext.equals(".shar")) return "application/x-shar";
      if(ext.equals(".shtml")) return "text/html";
      if(ext.equals(".sid")) return "audio/x-psid";
      if(ext.equals(".sit")) return "application/x-sit";
      if(ext.equals(".skd")) return "application/x-koan";
      if(ext.equals(".skm")) return "application/x-koan";
      if(ext.equals(".skp")) return "application/x-koan";
      if(ext.equals(".skt")) return "application/x-koan";
      if(ext.equals(".sl")) return "application/x-seelogo";
      if(ext.equals(".smi")) return "application/smil";
      if(ext.equals(".smil")) return "application/smil";
      if(ext.equals(".snd")) return "audio/basic";
      if(ext.equals(".sol")) return "application/solids";
      if(ext.equals(".spc")) return "text/x-speech";
      if(ext.equals(".spl")) return "application/futuresplash";
      if(ext.equals(".spr")) return "application/x-sprite";
      if(ext.equals(".sprite")) return "application/x-sprite";
      if(ext.equals(".src")) return "application/x-wais-source";
      if(ext.equals(".ssi")) return "text/x-server-parsed-html";
      if(ext.equals(".ssm")) return "application/streamingmedia";
      if(ext.equals(".sst")) return "application/vnd.ms-pki.certstore";
      if(ext.equals(".step")) return "application/step";
      if(ext.equals(".stl")) return "application/sla";
      if(ext.equals(".stp")) return "application/step";
      if(ext.equals(".sv4cpio")) return "application/x-sv4cpio";
      if(ext.equals(".sv4crc")) return "application/x-sv4crc";
      if(ext.equals(".svf")) return "image/vnd.dwg";
      if(ext.equals(".svr")) return "application/x-world";
      if(ext.equals(".swf")) return "application/x-shockwave-flash";
      if(ext.equals(".t")) return "application/x-troff";
      if(ext.equals(".talk")) return "text/x-speech";
      if(ext.equals(".tar")) return "application/x-tar";
      if(ext.equals(".tbk")) return "application/toolbook";
      if(ext.equals(".tcl")) return "application/x-tcl";
      if(ext.equals(".tcsh")) return "text/x-script.tcsh";
      if(ext.equals(".tex")) return "application/x-tex";
      if(ext.equals(".texi")) return "application/x-texinfo";
      if(ext.equals(".texinfo")) return "application/x-texinfo";
      if(ext.equals(".text")) return "text/plain";
      if(ext.equals(".tgz")) return "application/x-compressed";
      if(ext.equals(".tif")) return "image/tiff";
      if(ext.equals(".tiff")) return "image/tiff";
      if(ext.equals(".tr")) return "application/x-troff";
      if(ext.equals(".tsi")) return "audio/tsp-audio";
      if(ext.equals(".tsp")) return "application/dsptype";
      if(ext.equals(".tsv")) return "text/tab-separated-values";
      if(ext.equals(".turbot")) return "image/florian";
      if(ext.equals(".txt")) return "text/plain";
      if(ext.equals(".uil")) return "text/x-uil";
      if(ext.equals(".uni")) return "text/uri-list";
      if(ext.equals(".unis")) return "text/uri-list";
      if(ext.equals(".unv")) return "application/i-deas";
      if(ext.equals(".uri")) return "text/uri-list";
      if(ext.equals(".uris")) return "text/uri-list";
      if(ext.equals(".ustar")) return "application/x-ustar";
      if(ext.equals(".uu")) return "application/octet-stream";
      if(ext.equals(".uue")) return "text/x-uuencode";
      if(ext.equals(".vcd")) return "application/x-cdlink";
      if(ext.equals(".vcs")) return "text/x-vcalendar";
      if(ext.equals(".vda")) return "application/vda";
      if(ext.equals(".vdo")) return "video/vdo";
      if(ext.equals(".vew")) return "application/groupwise";
      if(ext.equals(".viv")) return "video/vivo";
      if(ext.equals(".vivo")) return "video/vivo";
      if(ext.equals(".vmd")) return "application/vocaltec-media-desc";
      if(ext.equals(".vmf")) return "application/vocaltec-media-file";
      if(ext.equals(".voc")) return "audio/voc";
      if(ext.equals(".vos")) return "video/vosaic";
      if(ext.equals(".vox")) return "audio/voxware";
      if(ext.equals(".vqe")) return "audio/x-twinvq-plugin";
      if(ext.equals(".vqf")) return "audio/x-twinvq";
      if(ext.equals(".vql")) return "audio/x-twinvq-plugin";
      if(ext.equals(".vrml")) return "application/x-vrml";
      if(ext.equals(".vrt")) return "x-world/x-vrt";
      if(ext.equals(".vsd")) return "application/x-visio";
      if(ext.equals(".vst")) return "application/x-visio";
      if(ext.equals(".vsw")) return "application/x-visio";
      if(ext.equals(".w60")) return "application/wordperfect6.0";
      if(ext.equals(".w61")) return "application/wordperfect6.1";
      if(ext.equals(".w6w")) return "application/msword";
      if(ext.equals(".wav")) return "audio/wav";
      if(ext.equals(".wb1")) return "application/x-qpro";
      if(ext.equals(".wbmp")) return "image/vnd.wap.wbmp";
      if(ext.equals(".web")) return "application/vnd.xara";
      if(ext.equals(".wiz")) return "application/msword";
      if(ext.equals(".wk1")) return "application/x-123";
      if(ext.equals(".wmf")) return "windows/metafile";
      if(ext.equals(".wml")) return "text/vnd.wap.wml";
      if(ext.equals(".wmlc")) return "application/vnd.wap.wmlc";
      if(ext.equals(".wmls")) return "text/vnd.wap.wmlscript";
      if(ext.equals(".wmlsc")) return "application/vnd.wap.wmlscriptc";
      if(ext.equals(".word")) return "application/msword";
      if(ext.equals(".wp")) return "application/wordperfect";
      if(ext.equals(".wp5")) return "application/wordperfect";
      if(ext.equals(".wp6")) return "application/wordperfect";
      if(ext.equals(".wpd")) return "application/wordperfect";
      if(ext.equals(".wq1")) return "application/x-lotus";
      if(ext.equals(".wri")) return "application/mswrite";
      if(ext.equals(".wrl")) return "application/x-world";
      if(ext.equals(".wrz")) return "x-world/x-vrml";
      if(ext.equals(".wsc")) return "text/scriplet";
      if(ext.equals(".wsrc")) return "application/x-wais-source";
      if(ext.equals(".wtk")) return "application/x-wintalk";
      if(ext.equals(".xbm")) return "image/x-xbitmap";
      if(ext.equals(".xdr")) return "video/x-amt-demorun";
      if(ext.equals(".xgz")) return "xgl/drawing";
      if(ext.equals(".xif")) return "image/vnd.xiff";
      if(ext.equals(".xl")) return "application/excel";
      if(ext.equals(".xla")) return "application/vnd.ms-excel";
      if(ext.equals(".xlam")) return "application/vnd.ms-excel.addin.macroEnabled.12";
      if(ext.equals(".xlb")) return "application/vnd.ms-excel";
      if(ext.equals(".xlc")) return "application/vnd.ms-excel";
      if(ext.equals(".xld")) return "application/vnd.ms-excel";
      if(ext.equals(".xlk")) return "application/vnd.ms-excel";
      if(ext.equals(".xll")) return "application/vnd.ms-excel";
      if(ext.equals(".xlm")) return "application/vnd.ms-excel";
      if(ext.equals(".xls")) return "application/vnd.ms-excel";
      if(ext.equals(".xlsb")) return "application/vnd.ms-excel.sheet.binary.macroEnabled.12";
      if(ext.equals(".xlsm")) return "application/vnd.ms-excel.sheet.macroEnabled.12";
      if(ext.equals(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
      if(ext.equals(".xlt")) return "application/vnd.ms-excel";
      if(ext.equals(".xltm")) return "application/vnd.ms-excel.template.macroEnabled.12";
      if(ext.equals(".xltx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.template";
      if(ext.equals(".xlv")) return "application/vnd.ms-excel";
      if(ext.equals(".xlw")) return "application/vnd.ms-excel";
      if(ext.equals(".xm")) return "audio/xm";
      if(ext.equals(".xml")) return "application/xml";
      if(ext.equals(".xmz")) return "xgl/movie";
      if(ext.equals(".xpix")) return "application/x-vnd.ls-xpix";
      if(ext.equals(".xpm")) return "image/xpm";
      if(ext.equals(".x-png")) return "image/png";
      if(ext.equals(".xsr")) return "video/x-amt-showrun";
      if(ext.equals(".xwd")) return "image/x-xwd";
      if(ext.equals(".xyz")) return "chemical/x-pdb";
      if(ext.equals(".z")) return "application/x-compressed";
      if(ext.equals(".zip")) return "application/zip";
      if(ext.equals(".zoo")) return "application/octet-stream";
      if(ext.equals(".zsh")) return "text/x-script.zsh";
      return "application/octet-stream";	  
	  	  
  }

//  isImage
//  given the file extension determines whether this is an image file  
private boolean isImage(String filePath) {
    
	  int dot = filePath.lastIndexOf(".");
	  String fileExtension = "." + filePath.substring(dot + 1).toLowerCase();

	  if (fileExtension.equals(".png")) return true;        	  
	  if (fileExtension.equals(".emf")) return true;        	  
	  if (fileExtension.equals(".wmf")) return true;        	  
      if (fileExtension.equals(".gif")) return true;        	  
	  if (fileExtension.equals(".jpeg")) return true;        	  
	  if (fileExtension.equals(".tiff")) return true;        	  
	  if (fileExtension.equals(".jpg")) return true;        	  
	  if (fileExtension.equals(".bmp")) return true;
      
      if (getContentType(filePath).indexOf("image/") == 0)
    	  return true;
      
      return false;
}  
  
// isVideo
// given the file extension determines whether this is a video file  
private boolean isVideo(String filePath) {
    	
    if (getContentType(filePath).indexOf("video/") == 0)
  	  return true;	
        
    return false;
}

// isVideo
// given the file extension determines whether this is an audio file 
private boolean isAudio(String filePath) {
  
    if (getContentType(filePath).indexOf("audio/") == 0)
    	  return true;
    
    return false;
}
  
// is3d
// given the file extension determines whether this is a 3d file 
private boolean is3d(String filePath) {
	  
	int dot = filePath.lastIndexOf(".");
	String fileExtension = filePath.substring(dot + 1).toLowerCase();
 
	if(fileExtension == "vrml")
		return true;
	
    if (getContentType(filePath).indexOf("x-world/") == 0)
    	  return true;
    
    if (getContentType(filePath).indexOf("-vrml") > 0)
  	  return true;    
    
    return false;
}

// isText
// given the file extension determines whether this is a text file 
private boolean isText(String filePath) {
	  
	  int dot = filePath.lastIndexOf(".");
	  String fileExtension = "." + filePath.substring(dot + 1).toLowerCase();

	  if (fileExtension.equals(".txt")) return true;        	  
	  if (fileExtension.equals(".doc")) return true;          	  
	  if (fileExtension.equals(".docx")) return true;          	  
	  if (fileExtension.equals(".dot")) return true;          	  
	  if (fileExtension.equals(".rtf")) return true;          	  
	  if (fileExtension.equals(".pdf")) return true;          	  
	  if (fileExtension.equals(".word")) return true;          	  
	  if (fileExtension.equals(".wp")) return true;          	  
	  if (fileExtension.equals(".wp5")) return true;          	  
	  if (fileExtension.equals(".wp6")) return true;          	  
	  if (fileExtension.equals(".wpd")) return true;          	  
      
      if (getContentType(filePath).indexOf("text/") == 0)
      	  return true; 
            
    return false;
}

// getTypeOfResource
// given the file extension determines the metadata resource type
private String getTypeOfResource(String filePath)
{
	if(isText(filePath))
		return "text";
	
	if(isAudio(filePath))
		return "sound recording";
	
	if(isImage(filePath))
		return "still image";
	
	if(isVideo(filePath))
		return "moving image";	
	
	if(is3d(filePath))
		return "three dimensional object";
	
	return "mixed material";	
}

// buildNewObjectDataStreams
// builds the core Hydra datastreams for a new object
private void buildNewObjectDataStreams(DigitalItemInfo item,
		DigitalObjectDocument.DigitalObject object, boolean isFolder) throws Exception {
	// /////////////////////////////////////////////////////////////////////////////////////////////////
	// Dublin Core record for the digital object
    log.info(LOG_MARKER + "buildNewObjectDataStreams:Commence"); 	
	DatastreamType dcDatastream = object.addNewDatastream();
	dcDatastream.setID("DC");
	dcDatastream.setSTATE(StateType.A);
	dcDatastream.setCONTROLGROUP(DatastreamType.CONTROLGROUP.X);

	DatastreamVersionType dcDatastreamVersion = dcDatastream.addNewDatastreamVersion();
	dcDatastreamVersion.setID("DC.0");
	dcDatastreamVersion.setMIMETYPE("text/xml");
	dcDatastreamVersion.setLABEL("Default Dublin Core Record");

	XmlContentType xmlContent = dcDatastreamVersion.addNewXmlContent();

	Element dc = buildDCXml(item, isFolder, xmlContent);
	
	xmlContent.getDomNode().appendChild(dc);
	
	// /////////////////////////////////////////////////////////////////////////////////////////////////

	// /////////////////////////////////////////////////////////////////////////////////////////////////
	// CLIF MODs record for the digital object
	DatastreamType modsDatastream = object.addNewDatastream();
	modsDatastream.setID("descMetadata");
	modsDatastream.setSTATE(StateType.A);
	modsDatastream.setCONTROLGROUP(DatastreamType.CONTROLGROUP.M);	
	DatastreamVersionType modsDatastreamVersion = modsDatastream.addNewDatastreamVersion();
	modsDatastreamVersion.setID("descMetadata.0");
	modsDatastreamVersion.setMIMETYPE("text/xml");
	modsDatastreamVersion.setLABEL("MODS metadata");
	XmlContentType modsXmlContent = modsDatastreamVersion.addNewXmlContent();
	
	Element modsCollection = buildMODSXml(item, isFolder, modsXmlContent);	    
	modsXmlContent.getDomNode().appendChild(modsCollection);
	// /////////////////////////////////////////////////////////////////////////////////////////////////
		
	// /////////////////////////////////////////////////////////////////////////////////////////////////
	// RELS-EXT (RDF) relationships for the object
	DatastreamType rdfDatastream = object.addNewDatastream();
	rdfDatastream.setID("RELS-EXT");
	rdfDatastream.setCONTROLGROUP(DatastreamType.CONTROLGROUP.X);

	DatastreamVersionType rdfDatastreamVersion = rdfDatastream.addNewDatastreamVersion();
	rdfDatastreamVersion.setID("RELS-EXT.0");
	rdfDatastreamVersion.setMIMETYPE("text/xml");
	rdfDatastreamVersion.setLABEL("Fedora Object-to-Object Relationship Metadata");

	XmlContentType rdfXmlContent = rdfDatastreamVersion.addNewXmlContent();

	Element rdfRoot = buildRDFXml(item, isFolder, rdfXmlContent);
	rdfXmlContent.getDomNode().appendChild(rdfRoot);
    log.info(LOG_MARKER + "buildNewObjectDataStreams:Finish"); 	
}

// buildRDFXml
// builds the RDF xml for a new object
private Element buildRDFXml(DigitalItemInfo item, boolean isFolder,
		XmlContentType rdfXmlContent) throws Exception {
	Element rdfRoot = rdfXmlContent.getDomNode().getOwnerDocument().createElementNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "RDF");

	Element rdfDescription = rdfXmlContent.getDomNode().getOwnerDocument().createElementNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "Description");
	rdfDescription.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "about", "info:fedora/" + ((FedoraPrivateItemInfo)(item.getPrivateInfo())).getPid());
	rdfRoot.appendChild(rdfDescription);
	
	Element metadataModel = rdfXmlContent.getDomNode().getOwnerDocument().createElementNS("info:fedora/fedora-system:def/model#", "hasModel");
	metadataModel.setAttribute("xmlns:mod", "info:fedora/fedora-system:def/model#");	    
	metadataModel.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource", "info:fedora/hydra-cModel:commonMetadata");
	rdfDescription.appendChild(metadataModel);
	
	Element contentModel = rdfXmlContent.getDomNode().getOwnerDocument().createElementNS("info:fedora/fedora-system:def/model#", "hasModel");
	contentModel.setAttribute("xmlns:mod", "info:fedora/fedora-system:def/model#");
	contentModel.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource", "info:fedora/hydra-cModel:genericContent");
	rdfDescription.appendChild(contentModel);	    
   
	Element isMemberOf = rdfXmlContent.getDomNode().getOwnerDocument().createElementNS("info:fedora/fedora-system:def/relations-external#", "isMemberOf");
	isMemberOf.setAttribute("xmlns:rel", "info:fedora/fedora-system:def/relations-external#");
	
	String parentPID = ((FedoraItemInfo)item).deriveParentObjectPID();
	
	if((parentPID == "") | (parentPID == null))
		throw new Exception(" parent ID cannot be empty");
	
	String parentCollectionPID = "info:fedora/" + parentPID;
	
	CharSequence cs = ".properties";	    
	if(parentPID.contains(cs)) {
		parentCollectionPID = FEDORA_ROOT_COLLECTION;	    	
	}
		
	isMemberOf.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource", parentCollectionPID);
	rdfDescription.appendChild(isMemberOf); 
	
	Element isCollection = rdfXmlContent.getDomNode().getOwnerDocument().createElementNS("info:fedora/fedora-system:def/relations-external#", "isCollection");
	isCollection.setAttribute("xmlns:rel", "info:fedora/fedora-system:def/relations-external#");	    
	Text isCollectionText = rdfXmlContent.getDomNode().getOwnerDocument().createTextNode(isFolder ? "True" : "False");
	isCollection.appendChild(isCollectionText);
	rdfDescription.appendChild(isCollection); 
	/* CLIF END */
	return rdfRoot;
}

// buildMODSXml
// builds the MODS metadata xml for a new object
private Element buildMODSXml(DigitalItemInfo item, boolean isFolder,
		XmlContentType modsXmlContent) {
	Element modsCollection = modsXmlContent.getDomNode().getOwnerDocument().createElementNS("http://www.loc.gov/mods/v3", "modsCollection");
	Element mods = modsXmlContent.getDomNode().getOwnerDocument().createElementNS("http://www.loc.gov/mods/v3", "mods");
	mods.setAttribute("version", "3.3");
	Element modsTitleInfo = modsXmlContent.getDomNode().getOwnerDocument().createElementNS("http://www.loc.gov/mods/v3", "titleInfo");
	Element modsTitle = modsXmlContent.getDomNode().getOwnerDocument().createElementNS("http://www.loc.gov/mods/v3", "title");
	Text modsTitleText = modsXmlContent.getDomNode().getOwnerDocument().createTextNode(isFolder ? item.getDisplayName() :  item.getTitle());
	modsTitle.appendChild(modsTitleText);
	
	Element modsTypeOfResource = null;
	if(isFolder) {
		modsTypeOfResource = modsXmlContent.getDomNode().getOwnerDocument().createElementNS("http://www.loc.gov/mods/v3", "typeOfResource");
		modsTypeOfResource.setAttribute("collection", "true");	
		Text modsTypeOfResourceText = modsXmlContent.getDomNode().getOwnerDocument().createTextNode("mixed material");
		modsTypeOfResource.appendChild(modsTypeOfResourceText);			
	}
	else {
		modsTypeOfResource = modsXmlContent.getDomNode().getOwnerDocument().createElementNS("http://www.loc.gov/mods/v3", "typeOfResource");
		modsTypeOfResource.setAttribute("collection", "false");	    
		Text modsTypeOfResourceText = modsXmlContent.getDomNode().getOwnerDocument().createTextNode(getTypeOfResource(item.getTitle()));
		modsTypeOfResource.appendChild(modsTypeOfResourceText);	        
	}
		
	Element modsName = modsXmlContent.getDomNode().getOwnerDocument().createElementNS("http://www.loc.gov/mods/v3", "name");
	modsName.setAttribute("type", "personal");	    
	Element modsNamePart = modsXmlContent.getDomNode().getOwnerDocument().createElementNS("http://www.loc.gov/mods/v3", "namePart");
	Text modsNamePartText = modsXmlContent.getDomNode().getOwnerDocument().createTextNode(item.getCreator());
	modsNamePart.appendChild(modsNamePartText);	        
	Element modsRole = modsXmlContent.getDomNode().getOwnerDocument().createElementNS("http://www.loc.gov/mods/v3", "role");
	Element modsRoleTerm = modsXmlContent.getDomNode().getOwnerDocument().createElementNS("http://www.loc.gov/mods/v3", "roleTerm");
	modsRoleTerm.setAttribute("type", "text");	
	Text modsRoleTermText = modsXmlContent.getDomNode().getOwnerDocument().createTextNode("creator");
	modsRoleTerm.appendChild(modsRoleTermText);	
	
	Element modsPhysicalDescription = null;	
	Element modsExtent = null;
	Element modsInternetMediaType = null;
	Element modsDigitalOrigin = null;
	
	if(!isFolder) {
		modsPhysicalDescription = modsXmlContent.getDomNode().getOwnerDocument().createElementNS("http://www.loc.gov/mods/v3", "physicalDescription");
		modsExtent = modsXmlContent.getDomNode().getOwnerDocument().createElementNS("http://www.loc.gov/mods/v3", "extent");
		modsInternetMediaType = modsXmlContent.getDomNode().getOwnerDocument().createElementNS("http://www.loc.gov/mods/v3", "internetMediaType");
		modsDigitalOrigin = modsXmlContent.getDomNode().getOwnerDocument().createElementNS("http://www.loc.gov/mods/v3", "digitalOrigin");
		Text modsExtentText = modsXmlContent.getDomNode().getOwnerDocument().createTextNode(item.getContentLength() + " bytes");	    	    
		modsExtent.appendChild(modsExtentText);	   	    
		Text modsInternetMediaTypeText = modsXmlContent.getDomNode().getOwnerDocument().createTextNode(getContentType(item.getTitle()));	    	    
		modsInternetMediaType.appendChild(modsInternetMediaTypeText);		    
		Text modsDigitalOriginText = modsXmlContent.getDomNode().getOwnerDocument().createTextNode("born digital");	    	    
		modsDigitalOrigin.appendChild(modsDigitalOriginText);
	}
	modsTitleInfo.appendChild(modsTitle);	    
	modsName.appendChild(modsNamePart);		    
	modsRole.appendChild(modsRoleTerm);
	modsName.appendChild(modsRole);	
	if(!isFolder) {
		modsPhysicalDescription.appendChild(modsExtent);	    
		modsPhysicalDescription.appendChild(modsInternetMediaType);	    
		modsPhysicalDescription.appendChild(modsDigitalOrigin);
	}
	mods.appendChild(modsTitleInfo);
	mods.appendChild(modsTypeOfResource);	
	mods.appendChild(modsName);
	if(!isFolder) {	
		mods.appendChild(modsPhysicalDescription);
	}
	modsCollection.appendChild(mods);
	return modsCollection;
}

// buildDCXml
// builds the Dublin core metadata xml for a new object
private Element buildDCXml(DigitalItemInfo item, boolean isFolder,
		XmlContentType xmlContent) {
	Element dc = xmlContent.getDomNode().getOwnerDocument().createElementNS("http://www.openarchives.org/OAI/2.0/oai_dc/", "dc");

	dc.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");

	Element dcTitle = xmlContent.getDomNode().getOwnerDocument().createElementNS("http://purl.org/dc/elements/1.1/", "title");
	Text textNode = xmlContent.getDomNode().getOwnerDocument().createTextNode(isFolder ? item.getDisplayName() :  item.getTitle());
	dcTitle.appendChild(textNode);
	dc.appendChild(dcTitle);

	String subject = item.getSubject(); 
	if( (!isFolder) && (subject != null ) && (subject != "") && (!subject.endsWith("NOT_SET")) )
	{
	    Element dcSubject = xmlContent.getDomNode().getOwnerDocument().createElementNS("http://purl.org/dc/elements/1.1/", "subject");
	    textNode = xmlContent.getDomNode().getOwnerDocument().createTextNode(subject);
	    dcSubject.appendChild(textNode);
	    dc.appendChild(dcSubject);
	}
	
	String description = item.getDescription(); 
	if( (!isFolder) && (description != null ) && (description != "") && (!description.endsWith("NOT_SET")) )
	{	
	    Element dcDescription = xmlContent.getDomNode().getOwnerDocument().createElementNS("http://purl.org/dc/elements/1.1/", "description");
	    textNode = xmlContent.getDomNode().getOwnerDocument().createTextNode(description);
	    dcDescription.appendChild(textNode);
	    dc.appendChild(dcDescription);	
	}
	
	Element dcCreator = xmlContent.getDomNode().getOwnerDocument().createElementNS("http://purl.org/dc/elements/1.1/", "creator");
	textNode = xmlContent.getDomNode().getOwnerDocument().createTextNode(item.getCreator());
	dcCreator.appendChild(textNode);
	dc.appendChild(dcCreator);

	String publisher = item.getPublisher(); 	
	if( (!isFolder) && (publisher != null ) && (publisher != "") && (!publisher.endsWith("NOT_SET")) )
	{	
		Element dcPublisher = xmlContent.getDomNode().getOwnerDocument().createElementNS("http://purl.org/dc/elements/1.1/", "publisher");
		textNode = xmlContent.getDomNode().getOwnerDocument().createTextNode(publisher);
		dcPublisher.appendChild(textNode);
		dc.appendChild(dcPublisher);
	}
	Element dcIdentifier = xmlContent.getDomNode().getOwnerDocument().createElementNS("http://purl.org/dc/elements/1.1/", "identifier");
	textNode = xmlContent.getDomNode().getOwnerDocument().createTextNode(isFolder ? item.getDisplayName() :  item.getTitle());
	dcIdentifier.appendChild(textNode);
	dc.appendChild(dcIdentifier);

	if(!isFolder) {
		Element dcFormat = xmlContent.getDomNode().getOwnerDocument().createElementNS("http://purl.org/dc/elements/1.1/", "format");
		textNode = xmlContent.getDomNode().getOwnerDocument().createTextNode(getContentType(item.getTitle()));
		dcFormat.appendChild(textNode);
		dc.appendChild(dcFormat);	
	}
	
	Element dcTypeOfResource = null;
	if(!isFolder) {
		dcTypeOfResource = xmlContent.getDomNode().getOwnerDocument().createElementNS("http://purl.org/dc/elements/1.1/", "type");    
		Text dcTypeOfResourceText = xmlContent.getDomNode().getOwnerDocument().createTextNode(getTypeOfResource(item.getTitle()));
		dcTypeOfResource.appendChild(dcTypeOfResourceText);	 
		dc.appendChild(dcTypeOfResource);
	}	
	//TODO Genre ??
	return dc;
}

// getDCBytes
// from a stream retrieve the dublin core metadata as a byte array
private byte[] getDCBytes(DigitalItemInfo item, Datastream ds) {
    DatastreamType dcDatastream = DatastreamType.Factory.newInstance();
    dcDatastream.setID("DC");
    dcDatastream.setSTATE(StateType.A);
    dcDatastream.setCONTROLGROUP(DatastreamType.CONTROLGROUP.X);

    DatastreamVersionType dcDatastreamVersion = dcDatastream.addNewDatastreamVersion();
    dcDatastreamVersion.setID(ds.getID());
    dcDatastreamVersion.setMIMETYPE(ds.getMIMEType());
    dcDatastreamVersion.setLABEL(ds.getLabel());

    XmlContentType xmlContent = dcDatastreamVersion.addNewXmlContent();
	Element dc = buildDCXml(item, false, xmlContent);        
    xmlContent.getDomNode().appendChild(dc);

    return xmlContent.toString().getBytes();
  }

// getMODSBytes
// from a stream retrieve the MODs core metadata as a byte array
private byte[] getMODSBytes(DigitalItemInfo item, Datastream ds) {
    DatastreamType dcDatastream = DatastreamType.Factory.newInstance();
    dcDatastream.setID("descMetadata");
    dcDatastream.setSTATE(StateType.A);
    dcDatastream.setCONTROLGROUP(DatastreamType.CONTROLGROUP.X);

    DatastreamVersionType dcDatastreamVersion = dcDatastream.addNewDatastreamVersion();
    dcDatastreamVersion.setID(ds.getID());
    dcDatastreamVersion.setMIMETYPE(ds.getMIMEType());
    dcDatastreamVersion.setLABEL(ds.getLabel());

    XmlContentType xmlContent = dcDatastreamVersion.addNewXmlContent();
	Element mods = buildMODSXml(item, false, xmlContent);        
    xmlContent.getDomNode().appendChild(mods);

    return xmlContent.toString().getBytes();
  }

// getRELSEXTBytes
// from a stream retrieve the RELS-EXT xml as a byte array
private byte[] getRELSEXTBytes(DigitalItemInfo item, Datastream ds) throws Exception {
    DatastreamType dcDatastream = DatastreamType.Factory.newInstance();
    dcDatastream.setID("RELS-EXT");
    dcDatastream.setSTATE(StateType.A);
    dcDatastream.setCONTROLGROUP(DatastreamType.CONTROLGROUP.X);

    DatastreamVersionType dcDatastreamVersion = dcDatastream.addNewDatastreamVersion();
    dcDatastreamVersion.setID(ds.getID());
    dcDatastreamVersion.setMIMETYPE(ds.getMIMEType());
    dcDatastreamVersion.setLABEL(ds.getLabel());

    XmlContentType xmlContent = dcDatastreamVersion.addNewXmlContent();
	Element mods = buildRDFXml(item, item.isCollection(), xmlContent);        
    xmlContent.getDomNode().appendChild(mods);

    return xmlContent.toString().getBytes();
  }

// getContentAsStream
// posts to a fedora url and passes back the content return data as a stream
  public InputStream getContentAsStream(String endpoint) {
	  
	  return UrlPostAsStream(endpoint, null);

  }
  
// UrlPostAsString
// posts to a url and passes back the return data as a String  
  public String UrlPostAsString(String url, String postParams) {
	  
	  InputStream iptStream = UrlPostAsStream(url, postParams);
	  if (iptStream != null)
	  {  
		  ByteArrayOutputStream bos = readStreamAsByteArrayStream(iptStream);
		  if(bos != null)
			  return bos.toString();		  
	  }
	  return "";
  }  
  
// UrlPostAsStream
// posts to a url and passes back the return data as an input stream    
  public InputStream  UrlPostAsStream(String url, String postParams)
  {
	  boolean useGuanxi = Boolean.parseBoolean(repoConfig.getString(DigitalRepository.CONFIG_KEY_CLIF_USE_GUANXI_HTTPS));
	
	  try {
	  	
	      if(useGuanxi) 
	      {  		  
			      EntityConnection connection = new EntityConnection(url,
			                                                         "test-keystore-alias",
			                                                         keystorePath, keystorePassword,
			                                                         truststorePath, truststorePassword,
			                                                         EntityConnection.PROBING_ON);
			      X509Certificate fedoraX509 = connection.getServerCertificate();
			      KeyStore fedoraTrustStore = KeyStore.getInstance("jks");
			      fedoraTrustStore.load(new FileInputStream(truststorePath), truststorePassword.toCharArray());
			      // ...under it's Subject DN as an alias...
			      fedoraTrustStore.setCertificateEntry(fedoraX509.getSubjectDN().toString(), fedoraX509);
			      // ...and rewrite the trust store
			      fedoraTrustStore.store(new FileOutputStream(truststorePath), truststorePassword.toCharArray());
			
			      connection.setAuthentication(repoConfig.getString(CONFIG_KEY_CONNECTION_USERNAME),
			                                   repoConfig.getString(CONFIG_KEY_CONNECTION_PASSWORD));
			      
			      if(postParams != null && postParams != "")
			      {
				      connection.setDoOutput(true); // Triggers POST. 
				      connection.setRequestProperty("Accept-Charset", "UTF-8"); 
				      connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
				      OutputStream output = connection.getOutputStream();
				      output.write(postParams.getBytes("UTF-8"));				      
			      }
			      
			      return connection.getInputStream();			      
	      }
	      else
	      {
	      	// CLIF for HTTP
			      HttpURLConnection httpConnection = (HttpURLConnection)(new URL(url)).openConnection();	
			      String authentication = Base64.encode((repoConfig.getString(CONFIG_KEY_CONNECTION_USERNAME) + ':' + repoConfig.getString(CONFIG_KEY_CONNECTION_PASSWORD)).getBytes());
			      httpConnection.setRequestProperty("Authorization", "Basic " + authentication);
			      if(postParams != null && postParams != "")
			      {
			    	  httpConnection.setDoOutput(true); // Triggers POST. 
			    	  httpConnection.setRequestProperty("Accept-Charset", "UTF-8"); 
			    	  httpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
				      OutputStream output = httpConnection.getOutputStream();
				      output.write(postParams.getBytes("UTF-8"));				      
			      }
			      return httpConnection.getInputStream();		      
	      }
	      
	  }
	  catch(Exception e) {
	    return null;
	    
	  }  
  }
  
  // deleteObject
  // deletes an object in the fedora repository
  public boolean deleteObject(String pid) {
    PurgeObjectDocument doc = PurgeObjectDocument.Factory.newInstance();
    PurgeObjectDocument.PurgeObject purge = doc.addNewPurgeObject();

    purge.setPid(pid);
    //purge.setLogMessage(repositoryProperties.getString(PROPS_KEY_PURGE_LOG_MESSAGE));
    // fedora.server.errors.GeneralException: Forced object removal is not yet supported.
    purge.setForce(false);

    try {
      // Initiate the client connection to the API-A endpoint
      FedoraAPIMServiceStub stub = new FedoraAPIMServiceStub(repoConfig.getString(CONFIG_KEY_API_M_ENDPOINT));

      // Add the auth creds to the client
      stub._getServiceClient().getOptions().setProperty(HTTPConstants.AUTHENTICATE, authenticator);
      // Register our custom SSL handler for this connection
      stub._getServiceClient().getOptions().setProperty(HTTPConstants.CUSTOM_PROTOCOL_HANDLER, customProtocolHandler);

      // Call the web service
      PurgeObjectResponseDocument outDoc = stub.purgeObject(doc);

      ResourceCache.RDFInstance(repoConfig).clearCache();     
      // 2007-10-15T08:59:46.827Z
      if (outDoc.getPurgeObjectResponse().getPurgedDate() != null)
        return true;
      else
        return false;
    }
    catch(RemoteException re) {
      log.error(LOG_MARKER + re);
      return false;
    }
  }

  // modifyObject
  // performs an update of an object in the fedora repository  
  public boolean modifyObject(DigitalItemInfo item, String dsID, byte[] dsContent, boolean inline) {
    try {
      log.info(LOG_MARKER + "modifyObject:Commence");    	
      // Initiate the client connection to the API-A endpoint
      FedoraAPIMServiceStub stub = new FedoraAPIMServiceStub(repoConfig.getString(CONFIG_KEY_API_M_ENDPOINT));

      // Add the auth creds to the client
      stub._getServiceClient().getOptions().setProperty(HTTPConstants.AUTHENTICATE, authenticator);
      // Register our custom SSL handler for this connection
      stub._getServiceClient().getOptions().setProperty(HTTPConstants.CUSTOM_PROTOCOL_HANDLER, customProtocolHandler);

      GetDatastreamDocument dsInDoc = GetDatastreamDocument.Factory.newInstance();
      GetDatastreamDocument.GetDatastream dsIn = dsInDoc.addNewGetDatastream();
      dsIn.setPid(((FedoraPrivateItemInfo)(item.getPrivateInfo())).getPid());
      dsIn.setDsID(dsID);
      GetDatastreamResponseDocument dsOutDoc = stub.getDatastream(dsInDoc);
      Datastream ds = dsOutDoc.getGetDatastreamResponse().getDatastream();

      if (inline) {
        ModifyDatastreamByValueDocument inDoc = ModifyDatastreamByValueDocument.Factory.newInstance();
        ModifyDatastreamByValueDocument.ModifyDatastreamByValue in = inDoc.addNewModifyDatastreamByValue();
        if(dsID.contains("descMetadata"))
        	in.setDsContent(getMODSBytes(item, ds));
        else if(dsID.contains("RELS-EXT"))
        	in.setDsContent(getRELSEXTBytes(item, ds)); 
        else
        	in.setDsContent(getDCBytes(item, ds));        	
        in.setDsID(dsID);
        in.setDsLabel(ds.getLabel());
        in.setForce(true);
        in.setLogMessage("Update from Sakai");
        in.setMIMEType(ds.getMIMEType());
        in.setPid(((FedoraPrivateItemInfo)(item.getPrivateInfo())).getPid());
        // Call the web service
        ModifyDatastreamByValueResponseDocument outDoc = stub.modifyDatastreamByValue(inDoc);        
                
        ResourceCache.Instance(repoConfig).clearCache();
        log.info(LOG_MARKER + "modifyObject:Finish");  
        if (outDoc.getModifyDatastreamByValueResponse().getModifiedDate() != null)
          return true;
        else
          return false;
      }
      else {
        // Updating content requires an upload url from which the new content will be ingested.
        ModifyDatastreamByReferenceDocument inDoc = ModifyDatastreamByReferenceDocument.Factory.newInstance();
        ModifyDatastreamByReferenceDocument.ModifyDatastreamByReference in = inDoc.addNewModifyDatastreamByReference();
        in.setDsLocation(getUploadURL(dsContent));
        in.setDsID(dsID);
        in.setDsLabel(ds.getLabel());
        in.setForce(true);
        in.setLogMessage("Update from Sakai");
        in.setMIMEType(ds.getMIMEType());
        in.setPid(((FedoraPrivateItemInfo)(item.getPrivateInfo())).getPid());

        ModifyDatastreamByReferenceResponseDocument outDoc = stub.modifyDatastreamByReference(inDoc);

        log.info(LOG_MARKER + "modifyObject:Finish");          
        if (outDoc.getModifyDatastreamByReferenceResponse().getModifiedDate() != null)
          return true;
        else
          return false;
      }
    }
    catch(Exception re) {
      log.error(LOG_MARKER + re);
      return false;
    }  
  }

  // moveObject
  // moves an object in the fedora repository from one collection to another    
  public boolean moveObject(DigitalItemInfo item) {
	    // If the object already exists, update it
	    ResourceCache.RDFInstance(repoConfig).clearCache();		      
		return modifyObject(item, ((FedoraPrivateItemInfo)(item.getPrivateInfo())).getRelsExtDatastreamID(),
		                        null, INLINE_UPDATE);
		
		
  }
  
  // commitObject
  // moves an object in the fedora repository from one collection to another   
  public boolean commitObject(DigitalItemInfo item) {
    // If the object already exists, update it	 
	log.info(LOG_MARKER + "commitObject:Commence"); 	  
	try {
		String[] identParts = item.getIdentifier().split("/");
		String parentPid = "";
		
		if(identParts.length > 1)
			parentPid=identParts[identParts.length - 2];   // a paste operation take precedence

		if(parentPid == "")
			parentPid = item.getParentPid();
		
		String realPathId = parentPid;   // for the case that ROOT_COLLECTION  ==  ***.properties
		
		if(parentPid.endsWith(".properties")) {
			String[] pidParts = FEDORA_ROOT_COLLECTION.split("/");
			parentPid = pidParts[pidParts.length - 1];
		}
		
		if(!item.isCollection()) {  
			
			byte[] newContent = item.getBinaryContent();			
			DigitalItemInfo currentResource = getResource(item.getIdentifier());
						
		    if ((currentResource != null) && ((newContent == null)||((item.getContentLength() == currentResource.getContentLength()))) && (((FedoraPrivateItemInfo)(item.getPrivateInfo())).getPid() != null) && (currentResource.getParentPid().equals(parentPid))) {		
		      // Now update the DC metadata - the method will get the binary content from Fedora
		      
		      modifyObject(item, ((FedoraPrivateItemInfo)(item.getPrivateInfo())).getMODsDatastreamID(),
	                  null, INLINE_UPDATE);
		      
		      return modifyObject(item, ((FedoraPrivateItemInfo)(item.getPrivateInfo())).getDCDatastreamID(),
		                                 null, INLINE_UPDATE);
		      	      
		    }
		    else if ((currentResource != null) && (((FedoraPrivateItemInfo)(item.getPrivateInfo())).getPid() != null) && (currentResource.getParentPid().equals(parentPid))) {
		    	
			      if (newContent != null) {
				        return modifyObject(item, ((FedoraPrivateItemInfo)(item.getPrivateInfo())).getContentDatastreamID(),
				        		newContent, NOT_INLINE_UPDATE);
				      }
			      else
			    	  return false;
		    }
		    // Otherwise, create it
		    else {
		    
			  String newPid = createObject(item);
			  ((FedoraPrivateItemInfo)(item.getPrivateInfo())).setPid(newPid);
			  if(currentResource != null)  { //  a paste operation  .. not required for a new upload
				  int lastSlashPos =-1 ;		  
				  String oldId = item.getIdentifier();
				  if(!oldId.endsWith("/"))
					  lastSlashPos = oldId.lastIndexOf("/");
				  else
					  lastSlashPos = oldId.substring(0,oldId.length() - 2).lastIndexOf("/");
				  
				  if(!currentResource.getParentPid().equals(parentPid)) {
					  lastSlashPos = oldId.substring(0,lastSlashPos - 2).lastIndexOf("/");
					  oldId = oldId.substring(0,lastSlashPos + 1) + realPathId + "/" + newPid;
				  }
				  else
					  oldId = oldId.substring(0,lastSlashPos + 1) + newPid;
				  
				  item.setIdentifier(oldId);
				  if(!currentResource.getParentPid().equals(parentPid)) {
					  if(parentPid != "") 
						  item.setParentPid(parentPid);
				  }	
			  }
			  else {  // new upload
				  
				  if(parentPid != "")
					  item.setParentPid(parentPid);				  
			  }
			
			  ResourceCache.RDFInstance(repoConfig).clearCache();			  
		      return (newPid != null);	      
		    }
		}
		else {
		    if (getResource(((FedoraPrivateItemInfo)(item.getPrivateInfo())).getPid()) == null) {
		    	
		    	  String newPid = createFolder(item);
		    	
				  ((FedoraPrivateItemInfo)(item.getPrivateInfo())).setPid(newPid);
				  
				  int lastSlashPos =-1 ;		  
				  String oldId = item.getIdentifier();
				  if(!oldId.endsWith("/"))
					  lastSlashPos = oldId.lastIndexOf("/");
				  else
					  lastSlashPos = oldId.substring(0,oldId.length() - 2).lastIndexOf("/");
				  
				  oldId = oldId.substring(0,lastSlashPos + 1) + newPid + "/";
				  item.setIdentifier(oldId);
				  if(parentPid != "")    // this is a paste operation not an upload
					  item.setParentPid(parentPid);	
				  
			      return (newPid != null);	    	
		    }
		    else
		    	return false;	    	
		}
	}
	finally {
		log.info(LOG_MARKER + "commitObject:Finish"); 			
		
	}
  }

  //  search
  //  performs a search operation
  public void search() { throw new UnsupportedOperationException(LOG_MARKER + "search not implemented");}

  // readStreamAsByteArrayStream
  // converts a stream to a ByteArrayOutputStream
  private  ByteArrayOutputStream readStreamAsByteArrayStream(InputStream in) {
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    byte[] buffer = new byte[50000];
	    int read;

	    try {
	      while ((read = in.read(buffer)) != -1) {
	        out.write(buffer, 0, read);
	      }
	      return out;
	    }
	    catch(IOException ioe) {
	      // Reading from the stream failed
	      return null;
	    }
	    finally {
	      try {
	        in.close();
	      }
	      catch(IOException ioe) {
	        // Closing the stream failed. Shouldn't stop us returning the data though
	      }
	    }  
  }
  
  // readStream
  // converts a stream to a byte array  
  private  byte[] readStream(InputStream in) {
	  ByteArrayOutputStream optStream = null;	  
	  optStream = readStreamAsByteArrayStream(in);
	  if(optStream != null)
	      return optStream.toByteArray();
	  else
		  return null;
  }  
  
  // getUploadURL
  // uploads content file to fedora temporary area and returns a subsequent Url for it
  private String getUploadURL(byte[] content) {
    String lineEnd = "\r\n";
    String twoHyphens = "--";
    String boundary =  "*****";
    String exsistingFileName = "tmp"; // must be this for Fedora upload servlet
    String uploadURL;
    
    boolean useGuanxi = Boolean.parseBoolean(repoConfig.getString(DigitalRepository.CONFIG_KEY_CLIF_USE_GUANXI_HTTPS));
    
    try {
      if (useGuanxi) { 	
	      EntityConnection connection = new EntityConnection(repoConfig.getString(CONFIG_KEY_UPLOAD_URL),
	                                                         "test-keystore-alias",
	                                                         keystorePath, keystorePassword,
	                                                         truststorePath, truststorePassword,
	                                                         EntityConnection.PROBING_ON);
	      X509Certificate fedoraX509 = connection.getServerCertificate();
	      KeyStore fedoraTrustStore = KeyStore.getInstance("jks");
	      fedoraTrustStore.load(new FileInputStream(truststorePath), truststorePassword.toCharArray());
	      // ...under it's Subject DN as an alias...
	      fedoraTrustStore.setCertificateEntry(fedoraX509.getSubjectDN().toString(), fedoraX509);
	      // ...and rewrite the trust store
	      fedoraTrustStore.store(new FileOutputStream(truststorePath), truststorePassword.toCharArray());
	      connection.setDoOutput(true);   
	      connection.setRequestMethod("POST");
	      connection.setRequestProperty("Connection", "Keep-Alive");
	      connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
	      DataOutputStream out = new DataOutputStream(connection.getOutputStream());
	      out.writeBytes(twoHyphens + boundary + lineEnd);
	      out.writeBytes("Content-Disposition: form-data; name=\"file\";" +
	                     " filename=\"" + exsistingFileName + "\"" + lineEnd);
	      out.writeBytes(lineEnd);
	      out.write(content);
	      out.writeBytes(lineEnd);
	      out.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
	      out.flush();
	      out.close();
	      byte[] streamBytes = readStream(connection.getInputStream());      
	      uploadURL = new String(streamBytes);      
	      
	      connection.disconnect();	      
      }
      else {
	      HttpURLConnection connection = (HttpURLConnection)(new URL(repoConfig.getString(CONFIG_KEY_UPLOAD_URL))).openConnection();  // CLIF for HTTP
	      connection.setDoOutput(true);
	      connection.setConnectTimeout(30 * 1000);       
	      connection.setRequestMethod("POST");
	      connection.setRequestProperty("Connection", "Keep-Alive");
	      connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
	      String authentication = Base64.encode((repoConfig.getString(CONFIG_KEY_CONNECTION_USERNAME) + ':' + repoConfig.getString(CONFIG_KEY_CONNECTION_PASSWORD)).getBytes()); // CLIF for HTTP
	      connection.setRequestProperty("Authorization", "Basic " + authentication);
	      DataOutputStream out = new DataOutputStream(connection.getOutputStream());
	      out.writeBytes(twoHyphens + boundary + lineEnd);
	      out.writeBytes("Content-Disposition: form-data; name=\"file\";" +
	                     " filename=\"" + exsistingFileName + "\"" + lineEnd);
	      out.writeBytes(lineEnd);
	      out.write(content);
	      out.writeBytes(lineEnd);
	      out.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
	      out.flush();
	      out.close();
	      byte[] streamBytes = readStream(connection.getInputStream());      
	      uploadURL = new String(streamBytes);      
	      
	      connection.disconnect();	      
      }
     
      return uploadURL.trim();
    }
    catch(Exception e) {
      log.error(LOG_MARKER + e);
      return null;
    }
  }

  // getSingleResource
  // given an object PID, issues a search in the fedora repository for that object, returning a DigitalItem entity if found
  private DigitalItemInfo getSingleResource(String resourcePid)
  {	  
	  try {	
	  log.info(LOG_MARKER + "getSingleResource:Commence"); 		  
	  FindObjectsDocument doc = FindObjectsDocument.Factory.newInstance();
	  FindObjectsDocument.FindObjects params = doc.addNewFindObjects();

	  FieldSearchQuery query = params.addNewQuery();
	  FieldSearchQuery.Conditions conditions = query.addNewConditions();
	  Condition condition = conditions.addNewCondition();
    
	  condition.setProperty("pid");
	  condition.setOperator(ComparisonOperator.HAS);
	  condition.setValue(resourcePid);	  
	  
	  ArrayOfString resultFields = params.addNewResultFields();
	  resultFields.addItem("pid");
	  resultFields.addItem("label");
	  resultFields.addItem("ownerId");
	  resultFields.addItem("title");
	  resultFields.addItem("creator");
	  resultFields.addItem("description");
	  resultFields.addItem("type");
	  resultFields.addItem("mDate");
	  resultFields.addItem("publisher");
	  resultFields.addItem("subject");
    	
      params.setMaxResults(new BigInteger("1"));
      
      FedoraAPIAServiceStub stub = new FedoraAPIAServiceStub(repoConfig.getString(CONFIG_KEY_API_A_ENDPOINT));

      // Add the auth creds to the client
      stub._getServiceClient().getOptions().setProperty(HTTPConstants.AUTHENTICATE, authenticator);
      // Register our custom SSL handler for this connection
      stub._getServiceClient().getOptions().setProperty(HTTPConstants.CUSTOM_PROTOCOL_HANDLER, customProtocolHandler);
      
      // Call the web service
      FindObjectsResponseDocument outDoc = stub.findObjects(doc);

      ObjectFields[] fields = outDoc.getFindObjectsResponse().getResult().getResultList().getObjectFieldsArray();

      if(fields == null || fields.length == 0 )
    	  return null;
      
	    FedoraItemInfo item = new FedoraItemInfo();
	
	    // Assume the object is a non-collection object until we find otherwise
	    item.setIsCollection(false);
	    item.setIsResource(true);
	
	    if (fields[0].getCreatorArray().length > 0)
	      item.setCreator(fields[0].getCreatorArray(0));
	    else
	      item.setCreator("NOT_SET");
	
	    if (fields[0].getDescriptionArray().length > 0)
	      item.setDescription(fields[0].getDescriptionArray(0));
	    else
	      item.setDescription("NOT_SET");
	
	    if (fields[0].getTitleArray().length > 0)
	      item.setDisplayName(fields[0].getTitleArray(0));
	    else
	      item.setDisplayName("NOT_SET");
	
	    if (fields[0].getPublisherArray().length > 0)
	      item.setPublisher(fields[0].getPublisherArray(0));
	    else
	      item.setPublisher("NOT_SET");
	
	    if (fields[0].getSubjectArray().length > 0)
	      item.setSubject(fields[0].getSubjectArray(0));
	    else
	      item.setSubject("NOT_SET");
	
	    if (fields[0].getTitleArray().length > 0)
	      item.setTitle(fields[0].getTitleArray(0));
	    else
	      item.setTitle("NOT_SET");
	
	    item.setIdentifier(fields[0].getPid());
	    item.setModifiedDate(fields[0].getMDate());
	    item.setOriginalFilename(fields[0].getPid());
	    item.setIsCollection(false);
	    item.setIsResource(true);
	    //item.setType(fields[0].getTypeArray(0));
	
	    FedoraPrivateItemInfo privateInfo = new FedoraPrivateItemInfo();
	    privateInfo.setPid(fields[0].getPid());
	    privateInfo.setOwnerId(fields[0].getOwnerId());
	    item.setPrivateInfo(privateInfo);

	   	if(item != null) {
			 
			 gatherMoreResourceData(item);
		}
		log.info(LOG_MARKER + "getSingleResource:Finish");  
		return (DigitalItemInfo)item;
		
	  }
	  
      catch(Exception e) {
    	log.error(LOG_MARKER + e);
        return null;
      }    
  }
    
  // getResources
  // given a collection PID, issues a search in the fedora repository for all objects in that collection, returning a list of DigitalItem entities  
  public DigitalItemInfo[] getResources(boolean includeResourcesInCollections, String searchPid) {
    // Build a new request document
    /* -- CLIF caching -- */
	log.info(LOG_MARKER + "getResources:Commence"); 	  
	DigitalItemInfo[] resources = null;

	// TODO (possibly) As we navigate fedora collection tree need to call ResourceCache.Instance()setFolderLevel() probably higher up in call stack though	
	// e.g. String[] folderLevel = id.split("/");
	// ResourceCache.Instance().setFolderLevel(String.valueOf(folderLevel));
	
	/* ------------------- */  	
    FindObjectsDocument doc = FindObjectsDocument.Factory.newInstance();
    FindObjectsDocument.FindObjects params = doc.addNewFindObjects();

    try {
    	
	    RIClient riClient = new RIClient(repoConfig, this);
	    
	    String searchId = searchPid;	    
	    if(searchPid == "*") {
	    	
	    	searchId = FEDORA_ROOT_COLLECTION;
	    }
	    else {
		    if(!searchPid.startsWith("info:fedora/")) {
		        searchId = "info:fedora/" + searchPid;
		    }
	    }
	    
	    Vector<FedoraItemInfo> collectionItems = riClient.getCollectionChildrenObjects(searchId);
	    
	    if(collectionItems == null)
	    	return resources;
	    
	    FedoraItemInfo[] items = collectionItems.toArray(new FedoraItemInfo[0]);
	    for(int i=0; i < items.length;i++)
	    {
	    	boolean moreInfoReqd = true;
			if(!ResourceCache.Instance(repoConfig).cacheExpired()) {
				
				Object obj = ResourceCache.Instance(repoConfig).getResource(((FedoraPrivateItemInfo)(items[i].getPrivateInfo())).getPid());
				DigitalItemInfo resource = null;
				if(obj != null)
					resource = (DigitalItemInfo) obj;
				
				if(resource != null) {
					items[i] = (FedoraItemInfo)resource;
					moreInfoReqd = false;
				}				
			}
						
			if(moreInfoReqd) {
		    	gatherMoreResourceData(items[i]);	        
		        ResourceCache.Instance(repoConfig).addToCacheNoExpiryUpdate(((FedoraPrivateItemInfo)(items[i].getPrivateInfo())).getPid(),items[i]);		        	
			}
        	
    	/* ------------------- */        
	    }
   	  
        resources = (DigitalItemInfo[])items;
    	log.info(LOG_MARKER + "getResources:Finish count=" + (resources != null ? resources.length : 0));
 	    return resources;      
    }
    catch(Exception e) {
      log.error(LOG_MARKER + e);
      return null;
    }  
    
  }

  // gatherMoreResourceData
  // further populates a FedoraItemInfo object e.g. filling in mimeType and size
  private void gatherMoreResourceData(FedoraItemInfo item) {
	  
	  try {
		log.info(LOG_MARKER + "gatherMoreResourceData:Commence"); 
	// Initiate the client connection to the API-A endpoint
	    FedoraAPIAServiceStub stub = new FedoraAPIAServiceStub(repoConfig.getString(CONFIG_KEY_API_A_ENDPOINT));
	
	      // Add the auth creds to the client
	    stub._getServiceClient().getOptions().setProperty(HTTPConstants.AUTHENTICATE, authenticator);
	      // Register our custom SSL handler for this connection	
	    stub._getServiceClient().getOptions().setProperty(HTTPConstants.CUSTOM_PROTOCOL_HANDLER, customProtocolHandler);
	    
	    FedoraAPIMServiceStub mstub = new FedoraAPIMServiceStub(repoConfig.getString(CONFIG_KEY_API_M_ENDPOINT));
	
	      // Add the auth creds to the client
	    mstub._getServiceClient().getOptions().setProperty(HTTPConstants.AUTHENTICATE, authenticator);
	      // Register our custom SSL handler for this connection
	    mstub._getServiceClient().getOptions().setProperty(HTTPConstants.CUSTOM_PROTOCOL_HANDLER, customProtocolHandler);
      
        ListDatastreamsDocument dsDoc = ListDatastreamsDocument.Factory.newInstance();
        ListDatastreamsDocument.ListDatastreams ds = dsDoc.addNewListDatastreams();
        ds.setPid(item.getIdentifier());
        ListDatastreamsResponseDocument dsOutDoc = stub.listDatastreams(dsDoc);
        DatastreamDef[] defs = dsOutDoc.getListDatastreamsResponse().getDatastreamDefArray();

        RelationData relData = new RelationData();
        
        for (DatastreamDef def : defs) {
//  CLIF    GetDatastreamDisseminationDocument dissDoc = GetDatastreamDisseminationDocument.Factory.newInstance();
//          GetDatastreamDisseminationDocument.GetDatastreamDissemination diss = dissDoc.addNewGetDatastreamDissemination();
//          diss.setDsID(def.getID());
//          diss.setPid(field.getPid());

          // The first one seems to be the default content, such as PDF etc, with the rest being the DC and RELS-EXT
          if ((def.getID().equals("content"))) {//||(count == 0)) {  // CLIF Hydra
	        //GetDatastreamDisseminationResponseDocument dissOutDoc = stub.getDatastreamDissemination(dissDoc);
	        //MIMETypedStream stream = dissOutDoc.getGetDatastreamDisseminationResponse().getDissemination();
        	  
              GetDatastreamDocument dsInDoc = GetDatastreamDocument.Factory.newInstance();
              GetDatastreamDocument.GetDatastream dsIn = dsInDoc.addNewGetDatastream();
              dsIn.setPid(item.getIdentifier());
              dsIn.setDsID(def.getID());
              GetDatastreamResponseDocument dstreamOutDoc = mstub.getDatastream(dsInDoc);
              Datastream dstream = dstreamOutDoc.getGetDatastreamResponse().getDatastream();
              //GetDatastreamResponseDocument stream = mstub.getDatastream(dissOutDoc);
        	
            //item.setMimeType(stream.getMIMEType());
            item.setMimeType(dstream.getMIMEType());
            item.setContentLength((int)dstream.getSize());   // FCREPO-64 bug in fedora might cause this to come back as zero...its downhill in rendering performance  from here on if thats the case
        	
            //item.setBinaryContent(stream.getStream());    CLIF we want a lazy content read

            item.setURL(repoConfig.getString(CONFIG_KEY_DISSEMINATION_ENDPOINT) + "/" + 
            		item.getIdentifier() + "/" + def.getID());
            ((FedoraPrivateItemInfo)(item.getPrivateInfo())).setContentDatastreamID(def.getID());
          }
          
          // Dublin core datastream
          else if (def.getID().equals("DC")) {
            ((FedoraPrivateItemInfo)(item.getPrivateInfo())).setDCDatastreamID(def.getID());
          }
          
          // CLIF Hydra MODs
          else if (def.getID().equals("descMetadata")) {
              ((FedoraPrivateItemInfo)(item.getPrivateInfo())).setMODsDatastreamID(def.getID());
          }          
          
        }
       
        // N.B isCollection / isResource must be set outside this routine..requires ITQL query
        
        item.setIsInCollection(true);    // CLIF  must be in the top  folder at least
        
	    }
	  
	    catch(Exception e) {
	    	log.error(LOG_MARKER + e);
	    }
		log.info(LOG_MARKER + "gatherMoreResourceData:Finish"); 	    
  }

  // getResource
  // given a sakai resource path, issues a search in the fedora repository for that object, returning a DigitalItem entity if found
  public DigitalItemInfo getResource(String relativePath) {
	
    log.info(LOG_MARKER + "getResource:Commence"); 	
    DigitalItemInfo item = null;
	
	try { 
		boolean isCollection = relativePath.endsWith("/");
		String[] pathParts = relativePath.split("/");
		relativePath = pathParts[pathParts.length - 1];
		
		if(!ResourceCache.Instance(repoConfig).cacheExpired()) {
			
			Object obj = ResourceCache.Instance(repoConfig).getResource(relativePath);
			if(obj != null)
				item = (DigitalItemInfo) obj;			
			
		}
		
		if(item == null) {
	    	
	    	 item = getSingleResource(relativePath);
	    	 
	    	 if(item != null) {
		    	 if(!addParentPid(item))
		    		 log.info(LOG_MARKER + "getResource:addParentPid Failure!!");
		    	 item.setIsCollection(isCollection);
	    		 ResourceCache.Instance(repoConfig).addToCacheNoExpiryUpdate(relativePath,item);		    	 
	    	 }
	    }
		    
   }
    catch(Exception e) {
    	log.error(LOG_MARKER + e);
	    }
	log.info(LOG_MARKER + "getResource:Finish");     
    return item;    
  }

  // getCollections
  // given a collection id, issues a search in the fedora repository for any child objects in the collection that also happen to be collections, returning a collection of DigitalItem entities 
  public DigitalItemInfo[] getCollections(String collection) { 
    log.info(LOG_MARKER + "getCollections:Commence"); 	  
    Vector<DigitalItemInfo> collections = new Vector<DigitalItemInfo>();
    DigitalItemInfo[] items = null;
    if(collection != null && collection != "") {  // clif
    	items = getResources(DO_NOT_INCLUDE_RESOURCES_IN_COLLECTIONS, collection );    	
    }	
    else {
    	items = getResources(DO_NOT_INCLUDE_RESOURCES_IN_COLLECTIONS, "*" );
    }
    
    if(items != null) {    // CLIF
    	for (DigitalItemInfo item : items) {
/*    		02.04.11
    	if ((exludeThisCollection == null) ||
          (!(((FedoraPrivateItemInfo)(item.getPrivateInfo())).getPid().equals(exludeThisCollection)))) {
*/               
    	  // CLIF speed improvement
    		
    	  if ((item != null) && item.isCollection()) {
              collections.add(item);
            }

//    	  }    	  
    	}
    }

    log.info(LOG_MARKER + "getCollections:Finish");     
    return (DigitalItemInfo[])collections.toArray( new DigitalItemInfo[0]);
  }

  // getMembersInCollection
  // given a collection id, issues a search in the fedora repository for any child objects in the collection, returning a collection of DigitalItem entities   
  public DigitalItemInfo[] getMembersInCollection(String collectionPid) {
    try {
        log.info(LOG_MARKER + "getMembersOfCollection:Commence");     	
  	  RIClient riClient = new RIClient(repoConfig, this);
  	  
  	  String searchId = collectionPid;	    
  	  if(searchId == null || searchId == "") {
  		  
  	  	searchId = FEDORA_ROOT_COLLECTION;
  	  	
  	  } 	  
  	  return getResources(true, searchId);	  

      }
      catch(Exception e) {
        log.error(LOG_MARKER + e);
        return null;
      }
      finally {
    	  log.info(LOG_MARKER + "getMembersOfCollection:Finish");    	
      }
  }
  
  //  addParentPid
  //  updates the RES-EXT for a fedora object to specify a new parent PID...achieves an object 'move' in the repository
  private boolean addParentPid(DigitalItemInfo item) {
    char[] parentPidBuff = new char[256];
    XmlCursor cursor = null;
    try {
      FedoraAPIMServiceStub mStub = new FedoraAPIMServiceStub(repoConfig.getString(CONFIG_KEY_API_M_ENDPOINT));
      mStub._getServiceClient().getOptions().setProperty(HTTPConstants.AUTHENTICATE, authenticator);
      mStub._getServiceClient().getOptions().setProperty(HTTPConstants.CUSTOM_PROTOCOL_HANDLER, customProtocolHandler);
      GetDatastreamDocument dsInDoc = GetDatastreamDocument.Factory.newInstance();
      GetDatastreamDocument.GetDatastream dsIn = dsInDoc.addNewGetDatastream();
      dsIn.setPid(((FedoraPrivateItemInfo)(item.getPrivateInfo())).getPid());
      dsIn.setDsID(((FedoraPrivateItemInfo)(item.getPrivateInfo())).getRelsExtDatastreamID());
      GetDatastreamResponseDocument dstreamOutDoc = mStub.getDatastream(dsInDoc);
      Datastream dstream = dstreamOutDoc.getGetDatastreamResponse().getDatastream();

      FedoraAPIAServiceStub stub = new FedoraAPIAServiceStub(repoConfig.getString(CONFIG_KEY_API_A_ENDPOINT));
      stub._getServiceClient().getOptions().setProperty(HTTPConstants.AUTHENTICATE, authenticator);
      stub._getServiceClient().getOptions().setProperty(HTTPConstants.CUSTOM_PROTOCOL_HANDLER, customProtocolHandler);
      GetDatastreamDisseminationDocument dsdDoc = GetDatastreamDisseminationDocument.Factory.newInstance();
      GetDatastreamDisseminationDocument.GetDatastreamDissemination dsd = dsdDoc.addNewGetDatastreamDissemination();
      dsd.setDsID(dstream.getID());
      dsd.setPid(((FedoraPrivateItemInfo)(item.getPrivateInfo())).getPid());
      GetDatastreamDisseminationResponseDocument dsdOutDoc = stub.getDatastreamDissemination(dsdDoc);
      MIMETypedStream dsStream = dsdOutDoc.getGetDatastreamDisseminationResponse().getDissemination();
      XmlContentType xml = XmlContentType.Factory.parse(new ByteArrayInputStream(dsStream.getStream()));

      cursor = xml.newCursor();
      // Move to the root RDF node
      cursor.toFirstChild();
      // See if the object is in any collections 
      String namespaceDecl = "declare namespace rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'; declare namespace rel='info:fedora/fedora-system:def/relations-external#'; ";
      cursor.selectPath(namespaceDecl + "$this//rdf:Description/rel:isMemberOf/@rdf:resource");
      char[] attrBuffer;
      if (cursor.toNextSelection()) {
    	  String[] parts = cursor.getTextValue().split("/");
          item.setParentPid(parts[parts.length-1]);
          return true;          
      }
      
      return false;
    }    
    catch(Exception e) {
      log.error(e);
      return false;
    }
    finally{
    	if(cursor !=null)
    		cursor.dispose();       	
    }
  }  
  
}
