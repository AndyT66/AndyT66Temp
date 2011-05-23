<%-- Copyright (c) 2006, 2007, 2008, 2009 The Sakai Foundation.
 
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
<%@ taglib uri="http://java.sun.com/upload" prefix="corejsf" %>
<%@ taglib uri="http://e_science.lancs.ac.uk/sakai-blogger-tool" prefix="blogger" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>


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
<script src="/sakai-blogger-tool/script/blogger.js"></script> 
<style>
td.tabStyle{
	width:720px;
}
td.td1{
	width:80px;
}
td.td2{
	width:440px;
}

</style>
<script language="javascript">
	isChanged =false;
	tabContentIsChanged = false;
	buttonPressed="";
	doSubmit = false;
	desactivateVerify=false;
	function verifySave(otherChanges){
		tabContentIsChanged = FCKeditorAPI.GetInstance('PostForm:main_text_inputRichText').IsDirty();
		var result = false;
		if (!desactivateVerify){
			if (buttonPressed=='SAVE' && tabContentIsChanged){
				result=window.confirm("You have changed an element but you do not have add or modify it in the document. Press OK to return to the editor. Press Cancel to ignore this message.");
			}
			if (buttonPressed=='PREVIEW' && tabContentIsChanged){
				result=window.confirm("You have changed an element but you do not have add or modify it in the document. Press OK to return to the editor. Press Cancel to ignore this message.");
			}
			if (buttonPressed=='CANCEL' && (isChanged || tabContentIsChanged || otherChanges)){
				result=window.confirm("You have modified the document, but you do not have save the changes. Press OK to return to the editor. Press Cancel to ignore this message.");
			}
		}
		doSubmit = !result;
		return !result;
	}
	function functionOnSubmitForTextArea(){
		return doSubmit;
	}
	function functionOnChangeInAbstract(){
		isChanged = true;
	}
	function functionOnChangeInText(){
		tabContentIsChanged = true;
	}	
</script>

	<sakai:view_content>
		<link rel="stylesheet" type="text/css" href="css/blogger.css" />
	
	    <h:form id="PostForm" onsubmit="javascript:return verifySave(#{postEditController.isChanged});" enctype="multipart/form-data">
            <f:subview id="toolBar">
                <jsp:include page="Toolbar.jsp"></jsp:include>
            </f:subview>
            <sakai:messages />
			<f:verbatim><div style="height: 20px;"></div></f:verbatim>
            <h:outputText styleClass="spanPageTitle" value="#{msgs.postEditor}" />
	        <f:verbatim><div style="height: 20px;"></div></f:verbatim>   
               <h:panelGrid columns="2">
                <h:panelGrid columns="2"  columnClasses="td1,td2">
	                <h:outputText value="#{msgs.postTitle} *:"/>
		            <h:inputText id="idTitle" value="#{postEditController.post.title}" size="71" required="true" onkeypress="javascript:isChanged=true;">
		            	<f:validator validatorId="PostTitleValidator"/>
		            </h:inputText>
        	        <h:outputText value="#{msgs.keywords}:"/>
            	    <h:inputText size="71" value="#{postEditController.keywords}" style="color:#CCCCCC" onkeyup="javascript:checkInputOnKeyUp(this,'#{postEditController.keywordsMessage}');" onkeypress="javascript:checkInputOnKeyPress(this,'#{postEditController.keywordsMessage}');isChanged=true;"/>
	                <h:outputText value="#{msgs.abstract}:"/>
              		<sakai:inputRichText value="#{postEditController.shortText}" rows="3" cols="120" textareaOnly="true"/>
    	            <%-- <blogger:rich_text_area  onChange="functionOnChangeInAbstract" onSubmit="functionOnSubmitForTextArea" height="50" width="448" value="#{postEditController.shortText}" toolbarButtonRows="0"/> --%>
                </h:panelGrid>                
                <h:panelGrid columns="2">
		                <h:outputText value="#{msgs.postVisibility}:"/>
        		        <h:selectOneMenu id="selectVisibility" value ="#{postEditController.post.state.visibility}" onchange="javascript: isChanged=true;">
	            	        <f:selectItems value="#{postEditController.visibilityList}"/>
    	            	    <f:converter converterId="VisibilityCode"/>
        		        </h:selectOneMenu>
		                <h:outputText value="#{msgs.readOnly}:"/>
		                <h:selectBooleanCheckbox id="readOnlyCheckBox" value ="#{postEditController.post.state.readOnly}"></h:selectBooleanCheckbox>

		                <h:outputText value="#{msgs.allowComments}:" id="allowCommentsLabel"/>
		                <h:selectBooleanCheckbox id="allowCommentsCheckBox" value ="#{postEditController.post.state.allowComments}"></h:selectBooleanCheckbox>
                </h:panelGrid>                
               </h:panelGrid>                              
               
			<t:panelTabbedPane id="tabbedPane" bgcolor="#DDDFE4" tabContentStyleClass="tabStyle" >
				<t:panelTab id="tab0" label="#{msgs.text}">
					
              			<sakai:inputRichText id="main_text" value="#{postEditController.editingText}" rows="10" cols="127"/>
              			<%--<blogger:rich_text_area onChange="functionOnChangeInText" onSubmit="functionOnSubmitForTextArea"  rows="15" columns="92" value="#{postEditController.editingText}" toolbarButtonRows="1"/>--%>
	                <sakai:button_bar>
       		            <h:commandButton action="#{postEditController.addParagraph}" value="#{msgs.addToDocument}"  onclick="javascript:desactivateVerify=true;" rendered="#{postEditController.showAddParagraphButton}"/>
            	        <h:commandButton action="#{postEditController.modifyParagraph}" value="#{msgs.modifyInDocument}" rendered="#{postEditController.showModifyParagraphButton}"   onclick="javascript:desactivateVerify=true;"/>
            	        <%--
	                    <h:commandButton action="#{postEditController.doReset}" value="#{msgs.resetEditor}" immediate="true"   onclick="javascript:desactivateVerify=true;"/>
	                    --%>
	                </sakai:button_bar>
				</t:panelTab>
				<t:panelTab id="tab1" label="#{msgs.images}">
					<h:panelGrid columns="2">
						<h:outputText value="#{msgs.imageName}:"  rendered="#{postEditController.showModifyImageButton}"></h:outputText>
	                	<h:outputText value="#{postEditController.imageDescription}"  rendered="#{postEditController.showModifyImageButton}"/>
						<h:outputText value="#{msgs.image}:"></h:outputText>
		        		<corejsf:upload target="image.jpg" value="#{postEditController.image}"></corejsf:upload>
						<h:outputText value="#{msgs.note}:"></h:outputText>
						<h:outputText value="#{msgs.noteImages}"></h:outputText>
						<h:outputText value=""></h:outputText>
						<h:outputText value="#{msgs.maxFileSize}"></h:outputText>						
					</h:panelGrid>
	                <sakai:button_bar>
	                    <h:commandButton action="#{postEditController.addImage}" value="#{msgs.addToDocument}"  onclick="javascript:desactivateVerify=true;"/>
	                    <h:commandButton action="#{postEditController.modifyImage}" value="#{msgs.modifyInDocument}" rendered="#{postEditController.showModifyImageButton}"  onclick="javascript:desactivateVerify=true;"/>
	                </sakai:button_bar>
				</t:panelTab>
				<t:panelTab id="tab2" label="#{msgs.links}">
					<h:panelGrid columns="2">
						<h:outputText value="#{msgs.description}:"></h:outputText>
						<h:panelGrid columns="2">
		                	<h:inputText id="idLinkDescription" value="#{postEditController.linkDescription}" size="50"  onkeypress="javascript: tabContentIsChanged=true;"/>
		    	            <h:outputText value=""></h:outputText>
		    	        </h:panelGrid>
						<h:outputText value="URL:"></h:outputText>
						<h:panelGrid columns="2">
		    	            <h:inputText id="idLinkExpression" value="#{postEditController.linkExpression}" size="50"  onkeypress="javascript: tabContentIsChanged=true;"/>
		    	            <h:outputText value="#{msgs.exampleURL}"></h:outputText>
		    	        </h:panelGrid>
	                </h:panelGrid>
	                <sakai:button_bar>
	                    <h:commandButton action="#{postEditController.addLink}" value="#{msgs.addToDocument}"  onclick="javascript:desactivateVerify=true;"/>
	                    <h:commandButton action="#{postEditController.modifyLink}" value="#{msgs.modifyInDocument}" rendered="#{postEditController.showModifyLinkButton}"  onclick="javascript:desactivateVerify=true;"/>
	                </sakai:button_bar>
				</t:panelTab>
				<t:panelTab id="tab3" label="#{msgs.files}">
					<h:panelGrid columns="2">
						<h:outputText value="#{msgs.fileName}:"  rendered="#{postEditController.showModifyFileButton}"></h:outputText>
	                	<h:outputText value="#{postEditController.fileDescription}"  rendered="#{postEditController.showModifyFileButton}"></h:outputText>
						<h:outputText value="URL:"></h:outputText>
		        		<corejsf:upload target="image.jpg" value="#{postEditController.file}"></corejsf:upload>
						<h:outputText value="#{msgs.note}:"></h:outputText>
						<h:outputText value="#{msgs.maxFileSize}"></h:outputText>		                
	                </h:panelGrid>
	                <sakai:button_bar>
	                    <h:commandButton action="#{postEditController.addFile}" value="#{msgs.addToDocument}"  onclick="javascript:desactivateVerify=true;"/>
	                    <h:commandButton action="#{postEditController.modifyFile}" value="#{msgs.modifyInDocument}" rendered="#{postEditController.showModifyFileButton}"  onclick="javascript:desactivateVerify=true;"/>
	                </sakai:button_bar>
				
				</t:panelTab>
			</t:panelTabbedPane>
			

               <sakai:button_bar>
                   <h:commandButton action="#{postEditController.doPreview}" value="#{msgs.preview}"  onclick="javascript:buttonPressed='PREVIEW';" />
                   <h:commandButton action="#{postEditController.doSave}" value="#{msgs.save}" onclick="javascript:buttonPressed='SAVE';"/>
                   <h:commandButton action="main" value="#{msgs.cancel}" immediate="true"  onclick="javascript:buttonPressed='CANCEL';"/>
               </sakai:button_bar>
		    <sakai:group_box title="#{msgs.currentStructure}:">
				<blogger:editPost post="#{postEditController.post}" controller="#{postEditController}"></blogger:editPost>
			</sakai:group_box>	    
	    </h:form>
    </sakai:view_content>
    
</sakai:view_container>


</f:view>