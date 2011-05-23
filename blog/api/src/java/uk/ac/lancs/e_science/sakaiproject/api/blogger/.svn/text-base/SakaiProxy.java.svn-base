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
package uk.ac.lancs.e_science.sakaiproject.api.blogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.AuthenticationManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;


public class SakaiProxy {
    private static ToolManager toolManager = org.sakaiproject.tool.cover.ToolManager.getInstance();
    private static SessionManager sessionManager = org.sakaiproject.tool.cover.SessionManager.getInstance();
    private static AuthzGroupService authzGroupService = org.sakaiproject.authz.cover.AuthzGroupService.getInstance();
    private static SiteService siteService = org.sakaiproject.site.cover.SiteService.getInstance();
    private static AuthenticationManager authManager = org.sakaiproject.user.cover.AuthenticationManager.getInstance();

    
    public static String getCurrentSiteId(){
		return toolManager.getCurrentPlacement().getContext(); //equivalent to PortalService.getCurrentSiteId();
	}
	public static String getCurrentUserId(){
		return UserDirectoryService.getCurrentUser().getId();
	}
    public static String getDiplayNameForTheUser(String userId){
    	try{
    		User sakaiUser = UserDirectoryService.getInstance().getUser(userId);
    		return sakaiUser.getDisplayName();
    	} catch (Exception e){
    		return userId; //this can happen if the user does not longer exist in the system
    	}
    }
    
	public static boolean isAutoDDL()
	{
		String autoDDL = ServerConfigurationService.getString("auto.ddl");
		return autoDDL.equals("true");
	}
    
    public static String getPageId(){
		Placement placement = toolManager.getCurrentPlacement();
		
		return ((ToolConfiguration) placement).getPageId();
    	
    }
    public static boolean isMaintainer(String userId){
    	try{
    		Site site = siteService.getSite(getCurrentSiteId());
    		AuthzGroup realm = authzGroupService.getAuthzGroup(site.getReference());
    		User sakaiUser = UserDirectoryService.getInstance().getUser(userId);
    		Role r = realm.getUserRole(sakaiUser.getId());
    		if(r.getId().equals(realm.getMaintainRole())) // This bit could be wrong
    		{
    			return true;
    		} else {
    			return false;
    		}
		
    		
    	} catch (Exception e){
    		e.printStackTrace();
    		return false;
    	}
    }
    
    public static List<Member> getSiteMembers(){
		ArrayList<Member> result = new ArrayList<Member>();
    	try{
    		Site site = siteService.getSite(getCurrentSiteId());
    		Set<org.sakaiproject.authz.api.Member> members = site.getMembers();
    		for (org.sakaiproject.authz.api.Member sakaiMember: members){
    				Member member = new Member();
    				member.setUserId(sakaiMember.getUserId());
    				member.setUserDisplayId(getDiplayNameForTheUser(sakaiMember.getUserId())); //I dont know why but the Member for sakai doesn't contains the displayId. For this reason I am doing this
    				result.add(member);
   			}
    		return result;
    	} catch (Exception e){
    		e.printStackTrace();
    		return result;
    	}
    	
    }    
    
    public static boolean isCurrentUserMaintainer(){
    	return isMaintainer(getCurrentUserId());
    }
}
