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

package uk.ac.lancs.e_science.sakaiproject.impl.blogger.persistence.sql.util;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.File;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Image;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.reader.PostReader;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.reader.XMLConverter;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.searcher.QueryBean;

public class SQLGenerator implements ISQLGenerator{

    //by default, oracle values
    public String BLOB="BLOB";
    public String BIGINT = "NUMBER"; 
    public String CLOB="CLOB";
    public static String APOSTROFE="&&-apos-s-&k";
 
    /* (non-Javadoc)
	 * @see uk.ac.lancs.e_science.sakaiproject.component.blogger.persistence.sql.util.ISQLGenerator#getCreateStatementsForPost()
	 */
    public Collection getCreateStatementsForPost(){
        return getCreateStatementsForPost(DEFAULT_PREFIX);
    }

    /* (non-Javadoc)
	 * @see uk.ac.lancs.e_science.sakaiproject.component.blogger.persistence.sql.util.ISQLGenerator#getCreateStatementsForPost(java.lang.String)
	 */
    public Collection getCreateStatementsForPost(String prefix){
        ArrayList result = new ArrayList();

        result.add(doTableForPost(prefix));
        result.add(doTableForImages(prefix));
        result.add(doTableForFiles(prefix));
        return result;
    }


    /* (non-Javadoc)
	 * @see uk.ac.lancs.e_science.sakaiproject.component.blogger.persistence.sql.util.ISQLGenerator#getDropStatementForPost()
	 */
    public Collection getDropStatementForPost(){
        return getDropStatementForPost(DEFAULT_PREFIX);
    }

    /* (non-Javadoc)
	 * @see uk.ac.lancs.e_science.sakaiproject.component.blogger.persistence.sql.util.ISQLGenerator#getDropStatementForPost(java.lang.String)
	 */
    public Collection getDropStatementForPost(String prefix){
        ArrayList result = new ArrayList();
        result.add("DROP TABLE "+prefix+TABLE_POST);
        result.add("DROP TABLE "+prefix+TABLE_IMAGE);
        result.add("DROP TABLE "+prefix+TABLE_FILE);
        return result;
    }
    /* (non-Javadoc)
	 * @see uk.ac.lancs.e_science.sakaiproject.component.blogger.persistence.sql.util.ISQLGenerator#getSelectStatementForQuery(uk.ac.lancs.e_science.sakaiproject.service.blogger.searcher.QueryBean, java.lang.String)
	 */
    public String getSelectStatementForQuery(QueryBean query, String siteId){
        return getSelectStatementForQuery(DEFAULT_PREFIX, query, siteId);
    }
    /* (non-Javadoc)
	 * @see uk.ac.lancs.e_science.sakaiproject.component.blogger.persistence.sql.util.ISQLGenerator#getSelectStatementForQuery(java.lang.String, uk.ac.lancs.e_science.sakaiproject.service.blogger.searcher.QueryBean, java.lang.String)
	 */
    public String getSelectStatementForQuery(String prefix, QueryBean query, String siteId){

        StringBuilder statement = new StringBuilder();
        statement.append("SELECT * FROM ").append(prefix).append(TABLE_POST);
        //we know that there are conditions. Build the statement
        statement.append(" WHERE ").append(SITE_ID).append("='").append(siteId).append("' AND ");
        if (query.queryByVisibility())
            statement.append(VISIBILITY).append("='").append(query.getVisibility()).append("' AND ");
        if (query.queryByInitDate())
            statement.append(DATE).append(">='").append(query.getInitDate()).append("' AND ");
        if (query.queryByEndDate())
            statement.append(DATE).append("<='").append(query.getEndDate()).append("' AND ");
        if (!query.getUser().trim().equals(""))
            statement.append(IDCREATOR).append("='").append(query.getUser()).append("' AND ");
        

        //in this point, we know that there is a AND at the end of the statement. Remove it.
        statement =new StringBuilder(statement.toString().substring(0,statement.length()-4)); //4 is the length of AND with the last space
        statement.append(" ORDER BY ").append(DATE).append(" DESC ");
        return statement.toString();
    }


    protected String doTableForPost(String prefix){
        StringBuilder statement = new StringBuilder();
        statement.append("CREATE TABLE ").append(prefix).append(TABLE_POST);
        statement.append("(");
        statement.append(POST_ID+" CHAR(32),");
        statement.append(TITLE+" VARCHAR(255), ");
        statement.append(DATE+" "+BIGINT+", ");
        statement.append(IDCREATOR+" VARCHAR(255), ");
        statement.append(VISIBILITY+" INT, ");
        statement.append(SITE_ID+" VARCHAR(255), ");
        statement.append(XMLCOLUMN+" "+CLOB+", ");
        statement.append("CONSTRAINT post_pk PRIMARY KEY ("+POST_ID+")");
        statement.append(")");
        return statement.toString();
    }
    protected String doTableForImages(String prefix){
    	StringBuilder statement = new StringBuilder();
    	statement.append("CREATE TABLE ").append(prefix).append(TABLE_IMAGE);
    	statement.append("(");
    	statement.append(IMAGE_ID+" CHAR(32),");
        statement.append(POST_ID+" CHAR(32),");
    	statement.append(IMAGE_CONTENT+" "+BLOB+", ");
    	statement.append(THUMBNAIL_IMAGE+" "+BLOB+", ");
    	statement.append(WEBSIZE_IMAGE+" "+BLOB+", ");
        statement.append("CONSTRAINT image_pk PRIMARY KEY ("+IMAGE_ID+")");
        statement.append(")");
        return statement.toString();
    }
    protected String doTableForFiles(String prefix){
    	StringBuilder statement = new StringBuilder();
    	statement.append("CREATE TABLE ").append(prefix).append(TABLE_FILE);
    	statement.append("(");
    	statement.append(FILE_ID+" CHAR(32),");
        statement.append(POST_ID+" CHAR(32),");
    	statement.append(FILE_CONTENT+" "+BLOB+", ");
        statement.append("CONSTRAINT file_pk PRIMARY KEY ("+FILE_ID+")");
        statement.append(")");
        return statement.toString();
    }
    /* (non-Javadoc)
	 * @see uk.ac.lancs.e_science.sakaiproject.component.blogger.persistence.sql.util.ISQLGenerator#getSelectAllPost(java.lang.String)
	 */
    public String getSelectAllPost(String siteId){
        return "SELECT * FROM "+DEFAULT_PREFIX+TABLE_POST +" WHERE "+SITE_ID+"='"+siteId+"' ORDER BY "+DATE+" DESC";
    }
    /* (non-Javadoc)
	 * @see uk.ac.lancs.e_science.sakaiproject.component.blogger.persistence.sql.util.ISQLGenerator#getSelectPost(java.lang.String)
	 */
    public String getSelectPost(String OID){
        return "SELECT * FROM "+DEFAULT_PREFIX+TABLE_POST +" WHERE "+POST_ID +"='"+OID+"'";
    }
    

    
    /* (non-Javadoc)
	 * @see uk.ac.lancs.e_science.sakaiproject.component.blogger.persistence.sql.util.ISQLGenerator#getSelectImage(java.lang.String)
	 */
    public String getSelectImage(String imageId){
    	return "SELECT * FROM "+DEFAULT_PREFIX+TABLE_IMAGE +" WHERE "+IMAGE_ID +"='"+imageId+"'";
    }
    
    /* (non-Javadoc)
	 * @see uk.ac.lancs.e_science.sakaiproject.component.blogger.persistence.sql.util.ISQLGenerator#getSelectFile(java.lang.String)
	 */
    public String getSelectFile(String fileId){
    	return "SELECT * FROM "+DEFAULT_PREFIX+TABLE_FILE +" WHERE "+FILE_ID +"='"+fileId+"'";
    }
	public String getSelectIdImagesFromPost(Post post){
		return "SELECT "+IMAGE_ID+" FROM "+DEFAULT_PREFIX+TABLE_IMAGE+" WHERE "+POST_ID+"='"+post.getOID()+"'";
	}
	public String getSelectIdFilesFromPost(Post post){
		return "SELECT "+FILE_ID+" FROM "+DEFAULT_PREFIX+TABLE_FILE+" WHERE "+POST_ID+"='"+post.getOID()+"'";		
	}

    /* (non-Javadoc)
	 * @see uk.ac.lancs.e_science.sakaiproject.component.blogger.persistence.sql.util.ISQLGenerator#getInsertStatementsForPost(uk.ac.lancs.e_science.sakaiproject.service.blogger.post.Post, java.lang.String, java.sql.Connection)
	 */
    public Collection getInsertStatementsForPost(Post post, String siteId, Connection connection) throws SQLException{
        return getInsertStatementsForPost(post,DEFAULT_PREFIX, siteId, connection);
    }
    /* (non-Javadoc)
	 * @see uk.ac.lancs.e_science.sakaiproject.component.blogger.persistence.sql.util.ISQLGenerator#getInsertStatementsForPost(uk.ac.lancs.e_science.sakaiproject.service.blogger.post.Post, java.lang.String, java.lang.String, java.sql.Connection)
	 */
    public Collection getInsertStatementsForPost(Post post, String prefix, String siteId, Connection connection) throws SQLException{
        ArrayList result = new ArrayList();
        result.add(doInsertStatementForPost(post, prefix,siteId,connection));
        result.addAll(getInsertStatementsForImages(post.getImages(),post.getOID(), connection));
        result.addAll(getInsertStatementsForFiles(post.getFiles(),post.getOID(), connection));
        return result;
    }
	public Collection getInsertStatementsForPostExcludingImagesAndFiles(Post post,String siteId,Connection connection){
        ArrayList result = new ArrayList();
        result.add(doInsertStatementForPost(post, DEFAULT_PREFIX,siteId,connection));
        return result;
	}


    /* (non-Javadoc)
	 * @see uk.ac.lancs.e_science.sakaiproject.component.blogger.persistence.sql.util.ISQLGenerator#getDeleteStatementsForPost(java.lang.String)
	 */
    public Collection getDeleteStatementsForPost(String postId){
        return getDeleteStatementsForPost(postId, DEFAULT_PREFIX);
    }
    
    /* (non-Javadoc)
	 * @see uk.ac.lancs.e_science.sakaiproject.component.blogger.persistence.sql.util.ISQLGenerator#getDeleteStatementsForPost(java.lang.String, java.lang.String)
	 */
    public Collection getDeleteStatementsForPost(String postId, String prefix){
        ArrayList result = new ArrayList();
        StringBuilder statement = new StringBuilder("");
        
        //the order is important
        statement = new StringBuilder("");
        statement.append("DELETE FROM ").append(prefix).append(TABLE_IMAGE).append(" WHERE ");
        statement.append(POST_ID).append("='").append(postId).append("'");
        result.add(statement.toString());

        statement = new StringBuilder("");
        statement.append("DELETE FROM ").append(prefix).append(TABLE_POST).append(" WHERE ");
        statement.append(POST_ID).append("='").append(postId).append("'");
        result.add(statement.toString());

        statement = new StringBuilder("");
        statement.append("DELETE FROM ").append(prefix).append(TABLE_FILE).append(" WHERE ");
        statement.append(POST_ID).append("='").append(postId).append("'");
        result.add(statement.toString());
        return result;
    }
	public Collection getDeleteStatementsForPostExcludingImagesAndFiles(String postId){
		return getDeleteStatementsForPostExcludingImagesAndFiles(postId,DEFAULT_PREFIX);
	}

	public Collection getDeleteStatementsForPostExcludingImagesAndFiles(String postId,String prefix){
        ArrayList result = new ArrayList();
        StringBuilder statement = new StringBuilder("");
        
        statement.append("DELETE FROM ").append(prefix).append(TABLE_POST).append(" WHERE ");
        statement.append(POST_ID).append("='").append(postId).append("'");
        result.add(statement.toString());
        return result;
	}
   
	public String getDeleteStatementForImage(String imageId){
        StringBuilder statement = new StringBuilder("");
		statement.append("DELETE FROM ").append(DEFAULT_PREFIX).append(TABLE_IMAGE).append(" WHERE ");
		statement.append(IMAGE_ID).append("='").append(imageId).append("'");
		return statement.toString();		
	}
	public String getDeleteStatementForFile(String idFile){
        StringBuilder statement = new StringBuilder("");
		statement.append("DELETE FROM ").append(DEFAULT_PREFIX).append(TABLE_FILE).append(" WHERE ");
		statement.append(FILE_ID).append("='").append(idFile).append("'");
		return statement.toString();
	}
	
    protected PreparedStatement doInsertStatementForPost(Post post, String prefix, String siteId,Connection connection)
    {
    	/*
    	 	Changed this from a normal statement to a PreparedStatement in
    	 	response to SAK-13376. Oracle will not allow a string literal of 
    	 	length exceeding 4000 characters unless it is bound - AF.
    	 */
    	
    	XMLConverter xmlConverter = new XMLConverter();
        PostReader reader = new PostReader(xmlConverter);
        reader.parsePost(post);
        String postAsXML = xmlConverter.getXML();
        
        StringBuilder statement = new StringBuilder();
        statement.append("INSERT INTO ").append(prefix).append(TABLE_POST).append(" (");
        statement.append(POST_ID+",");
        statement.append(TITLE+",");
        statement.append(DATE+",");
        statement.append(IDCREATOR+",");
        statement.append(VISIBILITY+",");
        statement.append(SITE_ID+",");
        statement.append(XMLCOLUMN);
        statement.append(") VALUES (");
        statement.append("'").append(post.getOID()).append("',");
        String title = post.getTitle().replaceAll("'",APOSTROFE); //we can't have any ' because hypersonic complains. so we can't reeplace ' for ////', what it is valid in mysql
        statement.append("'").append(title).append("',");
        statement.append("'").append(post.getDate()).append("',");
        String creator = post.getCreator().getId().replaceAll("'",APOSTROFE);
        statement.append("'").append(creator).append("',");
        statement.append("'").append(post.getState().getVisibility()) .append("',");
        statement.append("'").append(siteId).append("',?)");
        
        String sql = statement.toString();
        
        String xml = postAsXML.replaceAll("'",APOSTROFE);
        
        try
        {
        	PreparedStatement ps = connection.prepareStatement(sql);
        
        	ps.setString(1,xml);
        	return ps;
        }
        catch(SQLException sqle)
        {
        	sqle.printStackTrace();
        	return null;
        }
    }
    
    public List getInsertStatementsForImages(Image[] images, String postOID, Connection connection) throws SQLException{
   		ArrayList result = new ArrayList();

     	if (images==null)
	    	return result;
	
    	StringBuilder sqlStatement = new StringBuilder(); 
	    sqlStatement.append("INSERT INTO ").append(DEFAULT_PREFIX).append(TABLE_IMAGE).append(" (");
	    sqlStatement.append(IMAGE_ID+",");
	    sqlStatement.append(POST_ID+",");
	    sqlStatement.append(IMAGE_CONTENT+",");
	    sqlStatement.append(THUMBNAIL_IMAGE+",");
	    sqlStatement.append(WEBSIZE_IMAGE);
	    sqlStatement.append(") VALUES (?,?,?,?,?)");
	    	
	    for (int i=0;i<images.length;i++){
	       	String id = images[i].getIdImage();
	       	PreparedStatement statement = connection.prepareStatement(sqlStatement.toString());
	       	statement.setString(1,id);
	       	statement.setString(2,postOID);
	       	statement.setBytes(3,images[i].getContent());
        	statement.setBytes(4,images[i].getImageContentWithThumbnailSize());
        	statement.setBytes(5,images[i].getImageContentWithWebSize());
	       	result.add(statement);
	    }
	    return result;
    }

    public List getInsertStatementsForFiles(File[] files, String postOID,  Connection connection) throws SQLException{
   		ArrayList result = new ArrayList();

    	if (files==null)
	    	return result;
	
    	StringBuilder sqlStatement = new StringBuilder(); 
	    sqlStatement.append("INSERT INTO ").append(DEFAULT_PREFIX).append(TABLE_FILE).append(" (");
	    sqlStatement.append(FILE_ID+",");
	    sqlStatement.append(POST_ID+",");
	    sqlStatement.append(FILE_CONTENT);
	    sqlStatement.append(") VALUES (?,?,?)");
	    	
	    for (int i=0;i<files.length;i++){
	       	String id = files[i].getIdFile();
	       	PreparedStatement statement = connection.prepareStatement(sqlStatement.toString());
	       	statement.setString(1,id);
	       	statement.setString(2,postOID);
        	statement.setBytes(3,files[i].getContent());
	       	result.add(statement);
	    }
	    return result;
    }
    
}
