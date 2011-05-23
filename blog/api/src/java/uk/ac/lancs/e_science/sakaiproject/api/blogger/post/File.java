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

import uk.ac.lancs.e_science.sakaiproject.api.blogger.util.UIDGenerator;


public class File extends PostElement{
	private String idFile;
	private String description;
	private byte[] content;
	private String postId;
	
	public File(){
		this.idFile = UIDGenerator.getIdentifier(this); //automatically, we put an oid
	}
	public File(String description, byte[] content){
		this();
		this.description = description;
		this.content = content;
	}
	public void setIdFile(String idFile){
		this.idFile = idFile.trim();
		
	}
	public String getIdFile(){
		return idFile;
	}
	public void setDescription(String description){
		this.description = description;
	}
	public String getDescription(){
		return description;
	}
	
	public void setContent(byte[] content){
		this.content = content;
	}
	public byte[] getContent(){
		return content;
	}
	public void setPostId(String postId) {
		this.postId = postId;
	}
	public String getPostId() {
		return postId;
	}
		
}

