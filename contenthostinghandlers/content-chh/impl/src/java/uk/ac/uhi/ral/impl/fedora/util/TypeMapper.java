/* CVS Header
   $
   $
*/

package uk.ac.uhi.ral.impl.fedora.util;

import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingHandler;
import org.sakaiproject.content.api.ContentHostingHandlerResolver;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.chh.fedora.ContentCollectionFedora;
import org.sakaiproject.content.chh.fedora.ContentResourceFedora;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.tool.cover.SessionManager;
import uk.ac.uhi.ral.DigitalItemInfo;
import uk.ac.uhi.ral.DigitalRepository;
import uk.ac.uhi.ral.impl.fedora.FedoraPrivateItemInfo;

import java.rmi.server.UID;

public class TypeMapper {
  public static ContentEntity toContentEntity(DigitalItemInfo item,
                                              ContentEntity realParent, String relativePath,
                                              ContentHostingHandler chh,
                                              ContentHostingHandlerResolver chhResolver,
                                              DigitalRepository repo, boolean appendPaths) {
	if(item !=null)  // CLIF 
	{
	    if (item.isResource()) {
	      ContentResourceFedora resource = new ContentResourceFedora(realParent, relativePath, chh, chhResolver, repo, item);
	      resource.wrap(appendPaths);
	      return resource;
	    }
	    else {
	      ContentCollectionFedora collection = new ContentCollectionFedora(realParent, relativePath, chh, chhResolver, repo, item);
	      collection.wrap(appendPaths);
	      return collection;
	    }
	}
	else
		return null;
  }

  public static ContentEntity[] toContentEntity(DigitalItemInfo[] items,
                                                ContentEntity realParent, String relativePath,
                                                ContentHostingHandler chh,
                                                ContentHostingHandlerResolver chhResolver,
                                                DigitalRepository repo, boolean appendPaths) {
    ContentEntity[] entities = new ContentEntity[items.length];

    int count = 0;
    for (DigitalItemInfo item : items) {
      if(item != null) {  // CLIF   
/*    	  
	      entities[count] = toContentEntity(item, realParent,
	                                        ((FedoraPrivateItemInfo)(item.getPrivateInfo())).getPid(),
	                                        chh, chhResolver, repo);
*/
		  if(relativePath.equals("")) {
			  
			  relativePath = "/";
		  }
    	  
	      entities[count] = toContentEntity(item, realParent,
	    		  relativePath.charAt(relativePath.length() - 1) != '/' ? relativePath +  "/" + ((FedoraPrivateItemInfo)(item.getPrivateInfo())).getPid() : relativePath + ((FedoraPrivateItemInfo)(item.getPrivateInfo())).getPid(),
                  chh, chhResolver, repo, appendPaths); 							
							
	      count++;
      }
    }

    return entities;
  }

  public static DigitalItemInfo updateDigitalItemInfo(DigitalItemInfo item, ContentResourceEdit edit) {
	  	  
    if (edit.getProperties().get(ResourceProperties.PROP_DISPLAY_NAME) != null)
      item.setTitle((String)edit.getProperties().get(ResourceProperties.PROP_DISPLAY_NAME));
    if (edit.getProperties().get(ResourceProperties.PROP_CREATOR) != null)
      item.setCreator((String)edit.getProperties().get(ResourceProperties.PROP_CREATOR));    
    if (edit.getProperties().get(ResourceProperties.PROP_SUBJECT) != null)      // CLIF
      item.setSubject((String)edit.getProperties().get(ResourceProperties.PROP_SUBJECT));      
    if (edit.getProperties().get(ResourceProperties.PROP_DESCRIPTION) != null)
      item.setDescription((String)edit.getProperties().get(ResourceProperties.PROP_DESCRIPTION));    
    if (edit.getProperties().get(ResourceProperties.PROP_PUBLISHER) != null)    // CLIF
      item.setPublisher((String)edit.getProperties().get(ResourceProperties.PROP_PUBLISHER));      
    if (edit.getProperties().get(ResourceProperties.PROP_DISPLAY_NAME) != null)
      item.setDisplayName((String)edit.getProperties().get(ResourceProperties.PROP_DISPLAY_NAME));
    if (edit.getProperties().get(ResourceProperties.PROP_MODIFIED_DATE) != null)
      item.setModifiedDate((String)edit.getProperties().get(ResourceProperties.PROP_MODIFIED_DATE));
    if (edit.getProperties().get(ResourceProperties.PROP_ORIGINAL_FILENAME) != null)
      item.setOriginalFilename((String)edit.getProperties().get(ResourceProperties.PROP_ORIGINAL_FILENAME));

    item.setType("TEST-TYPE");
    item.setIdentifier(edit.getId());
    item.setMimeType(edit.getContentType());
    
    try {
      if (edit.streamContent() != null) {
    	byte[] newContent = Utils.getContentBytes(edit.streamContent());
        item.setBinaryContent(newContent);
        item.setContentLength(newContent.length);
      }
    }
    catch(ServerOverloadException soe) {
    }

    if (item.getTitle() == null) item.setTitle("TITLE_NOT_SET");
    if (item.getCreator() == null) item.setCreator("CREATOR_NOT_SET");
    if (item.getSubject() == null) item.setSubject("SUBJECT_NOT_SET");
    if (item.getDescription() == null) item.setDescription("DESCRIPTION_NOT_SET");
    if (item.getPublisher() == null) item.setPublisher("PUBLISHER_NOT_SET");
    if (item.getIdentifier() == null) item.setIdentifier("IDENTIFIER_NOT_SET");

    // @todo resource or collection?
    item.setIsResource(true);
    item.setIsCollection(false);
    if (((FedoraPrivateItemInfo)(item.getPrivateInfo())).getPid() == null)
      ((FedoraPrivateItemInfo)(item.getPrivateInfo())).setPid("::TEMPRY__PID::");//"demo:" + new UID().toString().replaceAll(":", ""));   CLIF
    if (((FedoraPrivateItemInfo)(item.getPrivateInfo())).getOwnerId() == null)
      ((FedoraPrivateItemInfo)(item.getPrivateInfo())).setOwnerId(SessionManager.getCurrentSession().getUserEid());

    return item;
  }
    
  public static DigitalItemInfo updateDigitalItemInfo(DigitalItemInfo item, ContentCollectionEdit edit) {
	    if (edit.getProperties().get(ResourceProperties.PROP_DISPLAY_NAME) != null)
	      item.setTitle((String)edit.getProperties().get(ResourceProperties.PROP_DISPLAY_NAME));
	    if (edit.getProperties().get(ResourceProperties.PROP_CREATOR) != null)
	      item.setCreator((String)edit.getProperties().get(ResourceProperties.PROP_CREATOR));
	    if (edit.getProperties().get(ResourceProperties.PROP_DESCRIPTION) != null)
	      item.setDescription((String)edit.getProperties().get(ResourceProperties.PROP_DESCRIPTION));
	    if (edit.getProperties().get(ResourceProperties.PROP_DISPLAY_NAME) != null)
	      item.setDisplayName((String)edit.getProperties().get(ResourceProperties.PROP_DISPLAY_NAME));
	    if (edit.getProperties().get(ResourceProperties.PROP_MODIFIED_DATE) != null)
	      item.setModifiedDate((String)edit.getProperties().get(ResourceProperties.PROP_MODIFIED_DATE));

	    item.setType("TEST-TYPE");
	    item.setIdentifier(edit.getId());
	    
	    if (item.getTitle() == null) item.setTitle("TITLE_NOT_SET");
	    if (item.getCreator() == null) item.setCreator("CREATOR_NOT_SET");
	    if (item.getDescription() == null) item.setDescription("DESCRIPTION_NOT_SET");
	    if (item.getIdentifier() == null) item.setIdentifier("IDENTIFIER_NOT_SET");

	    item.setIsResource(false);
	    item.setIsCollection(true);
	    if (((FedoraPrivateItemInfo)(item.getPrivateInfo())).getPid() == null)
	      ((FedoraPrivateItemInfo)(item.getPrivateInfo())).setPid("::TEMPRY__PID::");//"demo:" + new UID().toString().replaceAll(":", ""));   CLIF
	    if (((FedoraPrivateItemInfo)(item.getPrivateInfo())).getOwnerId() == null)
	      ((FedoraPrivateItemInfo)(item.getPrivateInfo())).setOwnerId(SessionManager.getCurrentSession().getUserEid());

	    return item;
	  }  
  
}
