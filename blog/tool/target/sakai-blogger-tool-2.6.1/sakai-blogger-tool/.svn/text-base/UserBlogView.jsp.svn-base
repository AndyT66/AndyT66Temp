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

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://e_science.lancs.ac.uk/sakai-blogger-tool" prefix="blogger" %>

<%
    response.setContentType("text/html; charset=UTF-8");
    response.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
    response.addDateHeader("Last-Modified", System.currentTimeMillis());
    response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
    response.addHeader("Pragma", "no-cache");
%>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="uk.ac.lancs.e_science.sakai.tools.blogger.bundle.Messages"/>
</jsp:useBean>

<f:view>

<sakai:view title="#{msgs.title}">
	<link rel="stylesheet" type="text/css" href="css/blogger.css" />
	<h:form>
           <f:subview id="toolBar">
               <jsp:include page="Toolbar.jsp"></jsp:include>
           </f:subview>
           <f:verbatim><div style="height: 20px;"></div></f:verbatim>
           <h:outputText styleClass="spanPageTitle" value="#{msgs.bloggerOf}: #{postListViewerController.selectedMemberId}" />
           <f:verbatim><div style="height: 20px;"></div></f:verbatim>

           <h:selectBooleanCheckbox value="#{postListViewerController.showComments}" immediate="true" onchange="this.form.submit();" />
           <h:outputText value="#{msgs.showComments}" /> 
           <f:verbatim>&nbsp;&nbsp;</f:verbatim>
           <h:selectBooleanCheckbox value="#{postListViewerController.showFullContent}" immediate="true"  onchange="this.form.submit();"/>
           <h:outputText value="#{msgs.showFullContent}" />
           <f:verbatim><div style="height: 10px;"></div></f:verbatim>
       	   <sakai:pager 
                     totalItems="#{postListViewerController.pagerTotalItems}"
                     firstItem="#{postListViewerController.pagerFirstItem}"
                     pageSize="#{postListViewerController.pagerNumItems}"
                     accesskeys="true"
                     immediate="true" />
        	<sakai:group_box title="#{msgs.search}">
           		<h:inputText id="idSearch" value="#{query.queryString}" size="30"  />
  	      		<h:commandButton  action="#{postListViewerController.doSearchInMemberBlog}"  value="#{msgs.search}"/>
      		</sakai:group_box>    
		   <blogger:listOfPosts posts="#{postListViewerController.postList}"  action="#{postListViewerController.showPostFromListOfPostsJSFComponent}" showComments="#{postListViewerController.showComments}" showFullContent="#{postListViewerController.showFullContent}" showCreator="false"></blogger:listOfPosts>
	</h:form>
        
</sakai:view>
</f:view>
