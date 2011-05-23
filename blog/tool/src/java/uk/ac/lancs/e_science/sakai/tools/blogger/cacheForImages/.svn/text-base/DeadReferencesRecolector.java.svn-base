/*************************************************************************************
 * Copyright (c) 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.

 *************************************************************************************/
package uk.ac.lancs.e_science.sakai.tools.blogger.cacheForImages;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TimerTask;

public class DeadReferencesRecolector extends TimerTask{
	private CacheForImages cache;
	private static long TIME=1000*60*60*3; //those elements that are not accessed in the last three hours will be deleted
	public DeadReferencesRecolector(CacheForImages cache){
		this.cache = cache;
	}
	public void run(){
		long currentMilliseconds = System.currentTimeMillis();
		Hashtable table = cache.getTable();
		Enumeration elements = table.elements();
		ArrayList toDeleting = new ArrayList();
		while (elements.hasMoreElements()){
			CacheForImagesEntry entry = (CacheForImagesEntry)elements.nextElement();
			if (currentMilliseconds-entry.getMilliseconds()>TIME)
				toDeleting.add(entry);
		}
		Iterator it = toDeleting.iterator();
		while (it.hasNext()){
			CacheForImagesEntry entry=(CacheForImagesEntry)it.next();
			cache.removeImage(entry.getImage().getIdImage());
		}

		
	}

}
