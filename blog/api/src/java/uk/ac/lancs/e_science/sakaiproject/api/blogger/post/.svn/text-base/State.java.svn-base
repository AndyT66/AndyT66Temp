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


public class State {
    public static final int PRIVATE=0;
    public static final int SITE=1;
    public static final int PUBLIC=2;
    public static final int TUTOR=3;
    

    private int visibility;
    private boolean readOnly=true;
    private boolean allowComments=false;

    public State(){
        visibility = PRIVATE;
    }

    public boolean isPrivate(){
        return visibility==PRIVATE;
    }
    public boolean isDraft(){
        return visibility==SITE;
    }
    public boolean isPublic(){
        return visibility==PUBLIC;
    }
    public boolean isTutor(){
        return visibility==TUTOR;
    }
    public int getVisibility(){
        return visibility;
    }
    public void setVisibility(int visibility){
        this.visibility=visibility;
    }
    public void setReadOnly(boolean readOnly){
    	this.readOnly = readOnly;
    }
    public boolean getReadOnly(){
    	return this.readOnly;
    }
    public void setAllowComments(boolean allowComments){
    	this.allowComments = allowComments;
    }
    public boolean getAllowComments(){
    	return allowComments;
    }    


}
