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

package uk.ac.lancs.e_science.sakaiproject.api.blogger.post;

import uk.ac.lancs.e_science.sakaiproject.api.blogger.SakaiProxy;

public class Creator {
    String _id;
    String _description;
    public Creator(){
        _id = "";
        _description="";
    }
    public Creator(String id){
        _id = id;
        _description="";
    }
    public Creator(String id, String description){
        _id = id;
        _description = description;
    }
    public void setId(String id){
        _id = id;
    }
    public String getId(){
        return _id;
    }
    
    public String getDisplayName(){
    	return SakaiProxy.getDiplayNameForTheUser(_id);
    }
    public void setDescription(String description){
        _description = description;
    }
    public String getDescription(){
        if (_description==null)
            _description="";
        return _description;
    }
}
