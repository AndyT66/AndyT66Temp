<%-- Copyright (c) 2006, 2007, 2008 The Sakai Foundation.
 
 Licensed under the Educational Community License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
       http://www.osedu.org/licenses/ECL-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>

<html>
<head>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
</head>
<body>
    <sakai:tool_bar>
        <sakai:tool_bar_item value="#{msgs.home}" action="#{postListViewerController.doShowAll}" immediate="true" />
        <sakai:tool_bar_item value="#{msgs.viewMembersBlog}" action="membersView" immediate="true" />
        <sakai:tool_bar_item value="#{msgs.viewMyBlog}" action="#{postListViewerController.doShowMyBlogger}" immediate="true" />
        <sakai:tool_bar_item value="#{msgs.new}" action="#{postCreateController.newPost}" immediate="true" />
    </sakai:tool_bar>
</body>
</html>