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
<sakai:view_container title="#{msgs.title}">
    <h:form>
        <sakai:view_content>
            <f:subview id="toolBar">
                <jsp:include page="Toolbar.jsp"></jsp:include>
            </f:subview>
            <sakai:messages />
                <sakai:group_box title="#{msgs.confirm}">
                    <h:outputText value="#{msgs.sureDelete}"/>
                    <sakai:button_bar>
                        <sakai:button_bar_item action="#{postViewerController.doDeletePost}" value="#{msgs.confirm}"/>
                        <sakai:button_bar_item action="#{postViewerController.doBack}" value="#{msgs.cancel}"/>
                     </sakai:button_bar>
                </sakai:group_box>
        </sakai:view_content>
    </h:form>
</sakai:view_container>

</f:view>