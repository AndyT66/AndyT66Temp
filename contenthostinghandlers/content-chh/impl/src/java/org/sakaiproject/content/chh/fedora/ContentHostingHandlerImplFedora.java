/**
 * 
 */
package org.sakaiproject.content.chh.fedora;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.*;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.tool.cover.SessionManager;
import uk.ac.uhi.ral.DigitalItemInfo;
import uk.ac.uhi.ral.DigitalRepository;
import uk.ac.uhi.ral.DigitalRepositoryFactory;
import uk.ac.uhi.ral.impl.fedora.FedoraPrivateItemInfo;
import uk.ac.uhi.ral.impl.fedora.util.TypeMapper;
import uk.ac.uhi.ral.impl.fedora.util.TypeResolver;
import uk.ac.uhi.ral.impl.fedora.FedoraItemInfo;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import uk.ac.uhi.ral.impl.fedora.ResourceCache;
/**
 *
 *
 * @author Alistair Young alistairskye@googlemail.com
 */
public class ContentHostingHandlerImplFedora implements ContentHostingHandlerFedora {
  private static final String LOG_MARKER = "[CTREP:ContentHostingHandlerImplFedora] ";
  
  /** Our logger */
  private static final Log log = LogFactory.getLog(ContentHostingHandlerImplFedora.class);

  /** The Sakai content hosting resolver */
  private ContentHostingHandlerResolver contentHostingHandlerResolver = null;

  /** The repository factory implementation to use */
  private DigitalRepositoryFactory repoFactory = null;

  /**
   * Retrieves the DigitalRepositoryFactory we are using
   * @return DigitalRepositoryFactory we are using
   */
  public DigitalRepositoryFactory getRepository() {
    return repoFactory;
  }

  /**
   * Sets the DigitalRepositoryFactory to use. Injected by Spring from components.xml
   * @param repoFactory DigitalRepositoryFactory to use
   */
  public void setRepoFactory(DigitalRepositoryFactory repoFactory) {
    this.repoFactory = repoFactory;
  }

  /**
   * Sets the Sakai ContentHostingHandlerResolver to use. Injected by Spring from components.xml
   * @param chhr ContentHostingHandlerResolver to use
   */
  public void setContentHostingHandlerResolver(ContentHostingHandlerResolver chhr) {
    contentHostingHandlerResolver = chhr;
  }

  /**
   * Retrieves the Sakai ContentHostingHandlerResolver we are using
   * @return ContentHostingHandlerResolver we are using
   */
  public ContentHostingHandlerResolver getContentHostingHandlerResolver() {
    return contentHostingHandlerResolver;
  }

  /**
	 * Cancel an edit to a collection, if this needs to be done in the impl.
	 * 
	 * @param edit
	 */
	public void cancel(ContentCollectionEdit edit) {
    log.info(LOG_MARKER + "cancel:ContentCollectionEdit");
  }

	/**
	 * cancel an edit to a resource ( if this needs to be done )
	 * 
	 * @param edit
	 */
	public void cancel(ContentResourceEdit edit) {		
    log.info(LOG_MARKER + "cancel:ContentResourceEdit");
  }
	
	/**
	 * commit a collection
	 * 
	 * @param edit
	 */
	public void commit(ContentCollectionEdit edit) {
		log.info(LOG_MARKER + "commit:ContentCollectionEdit");    
		ResourceCache.RDFInstance(_rootConfig).clearCache();        
	}
   
	public ContentEntity commit(ContentCollectionEdit edit, String finalId, ContentEntity realParent) {
    DigitalItemInfo item = null;
    
	int lastSlash = finalId.lastIndexOf(Entity.SEPARATOR, finalId.length() - 2);
	String folderName = "";
	if (lastSlash > 0)
	{
		 folderName = finalId.substring(lastSlash + 1,finalId.length() -1);
	}
    
    DigitalRepository repo = repoFactory.create();
    repo.init(_rootConfig);	
	
	item = repo.generateItem();
	item.setCreator(SessionManager.getCurrentSession().getUserEid());
	item.setModifiedDate("TEST");
	item.setDisplayName(folderName);
    item.setIsCollection(true);
    item.setIsResource(false);
        
    String relativePath = finalId;    
    while (relativePath.length() > 0
            && relativePath.charAt(relativePath.length() - 1) == '/')
          relativePath = relativePath.substring(0, relativePath.length() - 1);  
      
    relativePath = relativePath.substring(relativePath.lastIndexOf(".properties/") + ".properties/".length() - 1);   //  .../mountpoint.properties/...    
    repo.commitObject(TypeMapper.updateDigitalItemInfo(item, edit));
    ContentEntity ce = TypeMapper.toContentEntity(item, realParent, relativePath, edit.getContentHandler(), contentHostingHandlerResolver, repo, true);
    if(edit instanceof ContentEntityFedora) 
    {
	    DigitalItemInfo editItem = ((ContentEntityFedora)edit).getItem();
	    editItem.setPrivateInfo(item.getPrivateInfo());
	    editItem.setIdentifier(item.getIdentifier());  	    
    }
    else  // e.g. BaseCollectionEdit
    	((MutateableBaseResource)edit).changeId(item.getIdentifier());
    
    return ce;
  }

	/**
	 * commit a resource
	 * 
	 * @param edit
	 */
	public void commit(ContentResourceEdit edit) {
    log.info(LOG_MARKER + "commit:ContentResourceEdit");
    String repoRootFolder = _rootConfig.getString(DigitalRepository.CONFIG_KEY_CLIF_FEDORA_ROOT_COLLECTION);
    if((!(edit.getId().endsWith(".properties"))) && (!(edit.getId().endsWith(".properties/")))
    	&& (!(edit.getId().endsWith(repoRootFolder))) && (!(edit.getId().endsWith(repoRootFolder + "/")))) {
	    ContentResourceFedora crFedora = null;
	    if (edit instanceof ContentResourceFedora)
	      crFedora = (ContentResourceFedora)edit;
	    else {
	      Object vce = edit.getVirtualContentEntity(); 
	      if (vce instanceof ContentResourceFedora) {
	        crFedora = (ContentResourceFedora)vce;
	      }
	    }
	    if (crFedora == null) return;
		    
	    DigitalItemInfo item = crFedora.getItem();
	    String creator = item.getCreator(); 
	    if((creator == null) || (creator == "NOT_SET") || (creator == ""))
	    	item.setCreator(SessionManager.getCurrentSession().getUserEid());
	    
	    crFedora.getRepository().commitObject(TypeMapper.updateDigitalItemInfo(item, edit));
	    if(edit instanceof ContentEntityFedora) 
	    {	    
		    DigitalItemInfo editItem = ((ContentEntityFedora)edit).getItem();	    	
		    editItem.setPrivateInfo(item.getPrivateInfo());
		    editItem.setIdentifier(item.getIdentifier());
	    }
	    else  // e.g. BaseResourceEdit
	    	((MutateableBaseResource)edit).changeId(item.getIdentifier());	    
    }
  }

	/**
	 * commit a deleted resource
	 * 
	 * @param edit
	 * @param uuid
	 */
	public void commitDeleted(ContentResourceEdit edit, String uuid) {
    log.info(LOG_MARKER + "commitDeleted");
  }
	
	/**
	 * get a list of collections contained within the supplied collection
	 * 
	 * @param collection
	 * @return
	 */
	public List getCollections(ContentCollection collection) {
    log.info(LOG_MARKER + "getCollections : getId = " + collection.getId());
    
    ContentEntity cc = collection.getVirtualContentEntity();

    if (!(cc instanceof ContentCollectionFedora)) {
      return null;
    }

    ContentCollectionFedora fedoraCollection = (ContentCollectionFedora)cc;

    // Find all collections
    DigitalItemInfo[] items = fedoraCollection.getRepository().getCollections(((FedoraPrivateItemInfo)(fedoraCollection.getItem().getPrivateInfo())).getPid());

    ContentEntity[] entities = TypeMapper.toContentEntity(items, cc,
    		fedoraCollection.relativePath,
            this, contentHostingHandlerResolver,
            fedoraCollection.getRepository(), true);

    ArrayList<Edit> collections = new ArrayList<Edit>();
    for (ContentEntity entity : entities) {
    	if(entity != null) {
    		collections.add(((ContentCollectionFedora)(entity)).wrap());
    	}
    }

    return collections;
	}

	/**
	 * get a ContentCollectionEdit for the ID, creating it if necessary, this should not persist until commit is invoked
	 * 
	 * @param id
	 * @return
	 */
	public ContentCollectionEdit getContentCollectionEdit(String id) {
    log.info(LOG_MARKER + "getContentCollectionEdit : id = " + id);
    // CLIF
	ContentCollectionEdit cce = (ContentCollectionEdit) this.contentHostingHandlerResolver.newCollectionEdit(id);
	cce.setContentHandler(this);
	return cce;
	//
	}

	/**
	 * get a content resource edit for the supplied ID, creating it if necesary. This sould not persist until commit is invoked
	 * 
	 * @param id
	 * @return
	 */
	public ContentResourceEdit getContentResourceEdit(String id) {
    log.info(LOG_MARKER + "getContentResourceEdit : id = " + id);
    // CLIF    
    ContentResourceEdit cre = (ContentResourceEdit)contentHostingHandlerResolver.newResourceEdit(id);
    cre.setContentHandler(this);
    return cre;
    //
	}

	/**
	 * get a list of string ids of all resources below this point
	 * 
	 * @param ce
	 * @return
	 */
	public List getFlatResources(ContentEntity ce) {
    log.info(LOG_MARKER + "getFlatResources : id = " + ce.getId());
    return null;
	}

	/**
	 * get the resource body
	 * 
	 * @param resource
	 * @return
	 * @throws ServerOverloadException
	 */
	public byte[] getResourceBody(ContentResource resource) throws ServerOverloadException {
    log.info(LOG_MARKER + "getResourceBody : id = " + resource.getId());
    return null;
	}

	/**
	 * get a list of resource ids as strings within the collection
	 * 
	 * @param collection
	 * @return
	 */
	public List getResources(ContentCollection collection) {
    log.info(LOG_MARKER + "getResources : id = " + collection.getId());

    ContentEntity cc = collection.getVirtualContentEntity();

    if (!(cc instanceof ContentCollectionFedora)) {
      return null;
    }    

    ContentCollectionFedora fedoraCollection = (ContentCollectionFedora)cc;  // List getMembers()
    //return fedoraCollection.getMemberResources();   CLIF
    return fedoraCollection.getMemberNonCollectionResources();
	}

	private static PropertyResourceBundle _rootConfig = null;   // CLIF
	
	/**
	 * Convert the passed-in ContentEntity into a virtual Content Entity. The implementation should check that the passed in entity is managed by this content handler before performing the translation. Additionally it must register the content handler
	 * with the newly proxied ContentEntity so that subsequent invocations are routed back to the correct ContentHostingHandler implementation
	 * 
	 * @param edit
   * @param finalId /public/Fedora/fedora-mountpoint.txt/
	 * @return
	 */
	public ContentEntity getVirtualContentEntity(ContentEntity edit, String finalId) {
    log.info(LOG_MARKER + "getVirtualContentEntity : id = " + edit.getId() + " finalId = " + finalId);

    try {
      // Get the resource content      
      if(_rootConfig == null) {
	      try {
	    	  _rootConfig = new PropertyResourceBundle(new ByteArrayInputStream(((ContentResource)edit).getContent()));   // CLIF  else  subfolders  try to invoke read the config from the sub folder content stream ..sometimes an exception otherwise bizarre alerts like The collection does not exist
	      }
	      catch(IOException ioe) {
	          log.error("Can't load fedora config", ioe);
	      }
	      catch(ServerOverloadException soe) {
	          log.error("Can't get Fedora mountpoint content", soe);
	      }
      } 
      edit.setContentHandler(this);   // putCollection fix      
      DigitalRepository repo = repoFactory.create();
      repo.init(_rootConfig);

      //ThreadLocalManager.set("FEDORA" + edit.getId(), repo);
      ContentEntityFedora entity = (ContentEntityFedora)TypeResolver.resolveEntity(edit, finalId.substring(edit.getId().length()), this,
                                                                                   contentHostingHandlerResolver, repo, edit.getId().endsWith(".properties"));
      return (ContentEntity)entity.wrap();
    }
    catch(Exception ex) {    	
      log.error("getVirtualContentEntity:", ex);
      edit.setContentHandler(this);   // putCollection fix
      return edit;
    }
	}

	/**
	 * perform a wastebasket operation on the names id, if the implementation supports the operation otherwise its safe to ignore.
	 * 
	 * @param id
	 * @param uuid
	 * @param userId
	 * @return
	 */
	public ContentResourceEdit putDeleteResource(String id, String uuid, String userId) {
    log.info(LOG_MARKER + "putDeleteResource");

    ContentResourceEdit cre = (ContentResourceEdit)contentHostingHandlerResolver.newResourceEdit(id);
    cre.setContentHandler(this);
    return cre;
	}

	/**
	 * remove the supplied collection
	 * 
	 * @param edit
	 */
	public void removeCollection(ContentCollectionEdit edit) {
    log.info(LOG_MARKER + "removeCollection");
    
    String repoRootFolder = _rootConfig.getString(DigitalRepository.CONFIG_KEY_CLIF_FEDORA_ROOT_COLLECTION);
    if((!(edit.getId().endsWith(".properties"))) && (!(edit.getId().endsWith(".properties/")))
    	&& (!(edit.getId().endsWith(repoRootFolder))) && (!(edit.getId().endsWith(repoRootFolder + "/")))) {
	    ContentCollectionFedora crc = null;
	    if (edit instanceof ContentCollectionFedora)
	    	crc = (ContentCollectionFedora)edit;
	    else {
	      Object vce = edit.getVirtualContentEntity();
	      if (vce instanceof ContentCollectionFedora) {
	    	  crc = (ContentCollectionFedora)vce;
	      }
	    }
	    if (crc == null) return;
	
	    crc.getRepository().deleteObject(((FedoraPrivateItemInfo)(crc.getItem().getPrivateInfo())).getPid());
    }
  }

	/**
	 * remove the resource
	 * 
	 * @param edit
	 */
	public void removeResource(ContentResourceEdit edit) {
    log.info(LOG_MARKER + "removeResource");
    String repoRootFolder = _rootConfig.getString(DigitalRepository.CONFIG_KEY_CLIF_FEDORA_ROOT_COLLECTION);
    if((!(edit.getId().endsWith(".properties"))) && (!(edit.getId().endsWith(".properties/")))
    	&& (!(edit.getId().endsWith(repoRootFolder))) && (!(edit.getId().endsWith(repoRootFolder + "/")))) {    
	    ContentResourceFedora crf = null;
	    if (edit instanceof ContentResourceFedora)
	      crf = (ContentResourceFedora)edit;
	    else {
	      Object vce = edit.getVirtualContentEntity();
	      if (vce instanceof ContentResourceFedora) {
	        crf = (ContentResourceFedora)vce;
	      }
	    }
	    if (crf == null) return;
	
	    crf.getRepository().deleteObject(((FedoraPrivateItemInfo)(crf.getItem().getPrivateInfo())).getPid());
    }
  }

	/**
	 * stream the body of the resource
	 * 
	 * @param resource
	 * @return
	 * @throws ServerOverloadException
	 */
	public InputStream streamResourceBody(ContentResource resource) throws ServerOverloadException {
    log.info(LOG_MARKER + "streamResourceBody : id = " + resource.getId());

    ContentEntity ce = resource.getVirtualContentEntity();
    if (!(ce instanceof ContentResourceFedora)) return null;
    ContentResourceFedora crfd = (ContentResourceFedora) ce;
    return crfd.streamContent();
	}

	/**
	 * get the number of members
	 * @param ce
	 * @return
	 */
	public int getMemberCount(ContentEntity ce) {
    log.info(LOG_MARKER + "getMemberCount : id = " + ce.getId());

    if (ce instanceof ContentCollectionFedora)
      return ((ContentCollectionFedora)ce).getMemberCount();

    Object vce = ce.getVirtualContentEntity();  
    if (vce instanceof ContentCollectionFedora)
      return ((ContentCollectionFedora)vce).getMemberCount();
    
    return 0;
  }

	/**
	 * @param ce
	 * @return
	 */
	public Collection<String> getMemberCollectionIds(ContentEntity ce) 
	{
	    log.info(LOG_MARKER + "getMemberCollectionIds : id = " + ce.getId());
	    
	    ArrayList<String> idCollection = new ArrayList<String>(0);    
	
	    if (!(ce instanceof ContentCollectionFedora)) {
	    		return null;
	    }
	
	    ContentCollectionFedora fedoraCollection = (ContentCollectionFedora)ce;
	
	    DigitalItemInfo[] items = fedoraCollection.getRepository().getCollections(((FedoraPrivateItemInfo)(fedoraCollection.getItem().getPrivateInfo())).getPid());
	    
	    ArrayList<DigitalItemInfo> itemsFiltered = new ArrayList<DigitalItemInfo>(); 
	    for(DigitalItemInfo di: items)
	    {
	    	if(di.isCollection())
	    		itemsFiltered.add(di);
	    }
	    	    
	    ContentEntity[] entities = TypeMapper.toContentEntity(itemsFiltered.toArray(new DigitalItemInfo[0]), ce,
	    		fedoraCollection.relativePath,
	            this, contentHostingHandlerResolver,
	            fedoraCollection.getRepository(),(!(ce instanceof ContentEntityFedora))&&(!(ce.getId().endsWith(".properties"))));	    

		    
	    for(ContentEntity cEntity : entities)
	    {
	    	if(cEntity != null && (cEntity instanceof ContentCollectionFedora))
	    		idCollection.add(((ContentEntityFedora)cEntity).getId());
	    }
	    
	    
	    return (Collection<String>) idCollection;
	}

	/**
	 * @param ce
	 * @return
	 */
	public Collection<String> getMemberResourceIds(ContentEntity ce) {
		log.info(LOG_MARKER + "getMemberResourceIds : id = " + ce.getId());
    
	    ArrayList<String> idCollection = new ArrayList<String>(0);    
	    
	    ContentEntity cc = ce.getVirtualContentEntity();
	
	    if (!(cc instanceof ContentCollectionFedora)) {
	      return null;
	    }    
	
	    ContentCollectionFedora fedoraCollection = (ContentCollectionFedora)cc;  // List getMembers()

	    List<DigitalItemInfo> items = fedoraCollection.getMembers();
	    ArrayList<DigitalItemInfo> itemsFiltered = new ArrayList<DigitalItemInfo>(); 
	    for(DigitalItemInfo di: items)
	    {
	    	if(!di.isCollection())
	    		itemsFiltered.add(di);
	    }
	    	    
	    ContentEntity[] entities = TypeMapper.toContentEntity(itemsFiltered.toArray(new DigitalItemInfo[0]), fedoraCollection,
	    		fedoraCollection.relativePath,
	            this, contentHostingHandlerResolver,
	            fedoraCollection.getRepository(),false);	    

		    
	    for(ContentEntity cEntity : entities)
	    {
	    	if(cEntity != null && (cEntity instanceof ContentResourceFedora))
	    		idCollection.add(((ContentResourceFedora)cEntity).getId());
	    }	    
	    
	    return (Collection<String>) idCollection;	    
	}

	/**
	 * @param thisResource
	 * @param new_id
	 * @return
	 */
	public String moveResource(ContentResourceEdit thisResource, String new_id) {
    log.info(LOG_MARKER + "moveResource");
    DigitalRepository repo = repoFactory.create();
    repo.init(_rootConfig);
    DigitalItemInfo dInfo = ((ContentEntityFedora)(((MutateableBaseResource)thisResource).getVirtualContentEntity())).getItem();
    dInfo.setIdentifier(new_id);  // restore the full path
    repo.moveObject(dInfo);
    ResourceCache.RDFInstance(_rootConfig).clearCache();     
    return null;
	}

	/**
	 * @param thisCollection
	 * @param new_folder_id
	 * @return
	 */
	public String moveCollection(ContentCollectionEdit thisCollection, String new_folder_id) {
    log.info(LOG_MARKER + "moveCollection");
    DigitalRepository repo = repoFactory.create(); 
    repo.init(_rootConfig);
    DigitalItemInfo dInfo = ((ContentEntityFedora)(((MutateableBaseResource)thisCollection).getVirtualContentEntity())).getItem();
    dInfo.setIdentifier(new_folder_id);  // restore the full path
    repo.moveObject(dInfo);
    ResourceCache.RDFInstance(_rootConfig).clearCache();    
    return null;
	}

	/**
	 * @param resourceId
	 * @param uuid
	 * @return
	 */
	 public void setResourceUuid(String resourceId, String uuid) {
    log.info(LOG_MARKER + "setResourceUuid : resourceId = " + resourceId + " uuid = " + uuid);
   }

	/**
	 * @param id
	 */
	public void getUuid(String id) {
    log.info(LOG_MARKER + "getUuid");
  }

}
