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
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.File;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Image;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.searcher.QueryBean;


public interface ISQLGenerator {

	public static final String DEFAULT_PREFIX = "BLOGGER_";

	public static final String TABLE_POST = "POST";

	public static final String XMLCOLUMN = "XML";

	public static final String POST_ID = "POST_ID";

	public static final String TITLE = "TITLE";

	public static final String DATE = "DATEPOST";

	public static final String VISIBILITY = "VISIBILITY";

	public static final String IDCREATOR = "IDCREATOR";

	public static final String SITE_ID = "SITE_ID";

	public static final String TABLE_IMAGE = "IMAGE";

	public static final String IMAGE_ID = "IMAGE_ID";

	public static final String IMAGE_CONTENT = "IMAGE_CONTENT";

	public static final String THUMBNAIL_IMAGE = "THUMNAIL_IMAGE";

	public static final String WEBSIZE_IMAGE = "WEBSIZE_IMAGE";

	public static final String TABLE_FILE = "FILE";

	public static final String FILE_ID = "FILE_ID";

	public static final String FILE_CONTENT = "FILE_CONTENT";

	public abstract Collection getCreateStatementsForPost();

	public abstract Collection getCreateStatementsForPost(String prefix);

	public abstract Collection getDropStatementForPost();

	public abstract Collection getDropStatementForPost(String prefix);

	public abstract String getSelectStatementForQuery(QueryBean query,
			String siteId);

	public abstract String getSelectStatementForQuery(String prefix,
			QueryBean query, String siteId);

	public abstract String getSelectAllPost(String siteId);

	public abstract String getSelectPost(String OID);

	public abstract String getSelectImage(String imageId);

	public abstract String getSelectFile(String fileId);

	public abstract Collection getInsertStatementsForPost(Post post,String siteId, Connection connection) throws SQLException;
	public abstract Collection getInsertStatementsForPostExcludingImagesAndFiles(Post post,String siteId,Connection connection);
    public abstract List getInsertStatementsForImages(Image[] images, String postOID, Connection connection) throws SQLException;
    public abstract List getInsertStatementsForFiles(File[] files, String postOID, Connection connection) throws SQLException;
	
	public abstract String getSelectIdImagesFromPost(Post post);
	public abstract String getSelectIdFilesFromPost(Post post);
	
	/**
	 * @param post
	 * @param prefix
	 * @param siteId
	 * @return
	 */
	public abstract Collection getInsertStatementsForPost(Post post,
			String prefix, String siteId, Connection connection)
			throws SQLException;

	/**
	 * @param postId
	 * @return
	 */
	public abstract Collection getDeleteStatementsForPost(String postId);
	public abstract Collection getDeleteStatementsForPostExcludingImagesAndFiles(String postId);

	/**
	 * @param postId
	 * @param prefix
	 * @return
	 */
	public abstract Collection getDeleteStatementsForPost(String postId, String prefix);
	public abstract String getDeleteStatementForImage(String imageId);
	public abstract String getDeleteStatementForFile(String idFile);

}