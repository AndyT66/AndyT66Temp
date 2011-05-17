/* CVS Header
   $
   $
*/

package uk.ac.uhi.ral.impl.fedora;

import java.util.List;
import uk.ac.uhi.ral.DigitalItemInfo;
import java.io.*;
import java.util.Vector;
import java.util.Hashtable;
import java.lang.Exception;
import java.util.Calendar;
import java.util.PropertyResourceBundle;
import uk.ac.uhi.ral.DigitalRepository;

// ResourceCache (CLIF).   
// a utility class that enables caching of fedora resources and RDF query results (reducing WebService calls) 
public class ResourceCache
{
	private static int CACHE_SIZE = 10000; // set a default value
	private static int CACHE_EXPIRY_MINS = 2; // set a default value of 2 minutes
	private static String folderLevel = "";
	private static Hashtable resources = new Hashtable(CACHE_SIZE);
	
	private static ResourceCache theCache;
	private static ResourceCache theRDFCache;
	private static boolean expired = true;  // force cache reload even if server time rolled back
	private static Calendar expires = Calendar.getInstance();  
	private static PropertyResourceBundle repoConfig;
	
	// addToCache
	// adds an object the cache and updates the cache expiry time
	public void addToCache(String key, Object objectInstance) throws Exception {
		expired = false;
		if(!(resources.size()>=CACHE_SIZE)) {
			resources.put(new StringBuffer(key).reverse().toString(), objectInstance);
			expires = Calendar.getInstance();
			expires.add(Calendar.MINUTE, CACHE_EXPIRY_MINS);
		}
		else {
			clearCache();
			resources.put(new StringBuffer(key).reverse().toString(), objectInstance);
			expires = Calendar.getInstance();
			expires.add(Calendar.MINUTE, CACHE_EXPIRY_MINS);			
		}
	}
	
	// addToCacheNoExpiryUpdate
	// adds an object to the cache without updating the cache expiry time	
	public void addToCacheNoExpiryUpdate(String key, Object objectInstance) throws Exception {
		if(!(resources.size()>=CACHE_SIZE)) {
			resources.put(new StringBuffer(key).reverse().toString(), objectInstance);
		}
		else {
			clearCache();
			resources.put(new StringBuffer(key).reverse().toString(), objectInstance);
		}
	}	
	
	// clearCache
	// empties the cache
	public void clearCache() {
		resources.clear();
		expired = true;
	}	
	
	// cacheExpired
	// returns whether the cache has expired 
	public boolean cacheExpired() {		
		if(expired)
			return true;
		
		expired = expires.before(Calendar.getInstance());		
		return expired;
	}
	
	// getResource
	// returns a specific resource for a given key
	public Object getResource(String key) {		
		expired = false;
		expires = Calendar.getInstance();
		expires.add(Calendar.MINUTE, CACHE_EXPIRY_MINS);		
		return (Object)resources.get(new StringBuffer(key).reverse().toString());	
	}
	
	// getAllResources
	// returns all resources in the cache	
	public Object[] getAllResources(boolean includeResourcesInCollections) {	    
	    try {	    	
			expired = false;
			expires = Calendar.getInstance();
			expires.add(Calendar.MINUTE, CACHE_EXPIRY_MINS);		    	
			return (Object[]) (resources.values().toArray(new Object[0]));
	    }
	    catch(Exception e) {
	    	return null;	    
	    }
	}
	
	private void ResourceCache() {
		// singleton 
	}
	
	// setFolderLevel
	// sets a new folder level		
	public void setFolderLevel(String level) {
		if (!level.equals(folderLevel)) {
			clearCache();			
		}
		folderLevel = level;
	}
	
	// getFolderLevel
	// gets the current folder level		
	public String getFolderLevel() {
		return folderLevel;
	}	
	
	// Instance
	// gets the singleton instance	
	public static ResourceCache Instance(PropertyResourceBundle config) {
		if(theCache == null) {
			setRepoConfig(config);
			theCache = new ResourceCache();
		}
		return theCache;
	}

	// RDFInstance
	// gets the singleton instance		
	public static ResourceCache RDFInstance(PropertyResourceBundle config) {
		if(theRDFCache == null) {
			setRepoConfig(config);
			theRDFCache = new ResourceCache();
		}
		return theRDFCache;
	}	
	
	// setRepoConfig
	// sets the configuration for an instance	
	private static void setRepoConfig(PropertyResourceBundle repoConfig) {
		ResourceCache.repoConfig = repoConfig;
		CACHE_SIZE = Integer.parseInt(repoConfig.getString(DigitalRepository.CONFIG_KEY_CLIF_CACHE_SIZE));
		resources = new Hashtable(CACHE_SIZE);		
		CACHE_EXPIRY_MINS = Integer.parseInt(repoConfig.getString(DigitalRepository.CONFIG_KEY_CLIF_CACHE_REFRESH_MINS));
	}

}

