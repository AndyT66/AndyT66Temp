/**********************************************************************************
 * $Id: $
 * $Revision: $
 * $Date: $
 ***********************************************************************************
 * Copyright (c) 2008 The Sakai Foundation
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
 **********************************************************************************/

package uk.ac.lancs.e_science.jsf.components.blogger.tag;

import javax.faces.component.UIComponent;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;

public class RichTextEditArea extends UIComponentTag
{
    private String value;
    private String width;
    private String height;
    private String toolbarButtonRows;
    private String javascriptLibrary;
    private String autoConfig;
    private String columns;
    private String rows;
    private String justArea;
    private String onChange;
    private String onSubmit;

    public String getComponentType()
    {
        return "RichTextEditArea";
    }

    public String getRendererType()
    {
        return "RichTextEditArea";
    }

    // getters and setters for component properties

    public void setValue(String newValue) { value = newValue; }
    public String getValue() { return value; }
    public void setWidth(String newWidth) { width = newWidth; }
    public String getWidth() { return width; }
    public void setHeight(String newHeight) { height = newHeight; }
    public String getHeight() { return height; }
    public void setToolbarButtonRows(String str) { toolbarButtonRows = str; }
    public String getToolbarButtonRows() { return toolbarButtonRows; }
    public void setJavascriptLibrary(String str) { javascriptLibrary = str; }
    public String getJavascriptLibrary() { return javascriptLibrary; }
    public void setAutoConfig(String str) { autoConfig = str; }
    public String getAutoConfig() { return autoConfig; }
    public void setColumns(String newC) { columns = newC; }
    public String getColumns() { return columns; }
    public void setRows(String newRows) { rows = newRows; }
    public String getRows() { return rows; }
    public void setJustArea(String newJ) { justArea = newJ; }
    public String getJustArea() { return justArea; }
    public void setOnChange(String onC) {onChange = onC;}
    public String getOnChange(){ return onChange;};
    public void setOnSubmit(String onS) {onSubmit = onS;}
    public String getOnSubmit(){ return onSubmit;};

    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        setString(component, "value", value);
        setString(component, "width", width);
        setString(component, "height", height);
        setString(component, "toolbarButtonRows", toolbarButtonRows);
        setString(component, "javascriptLibrary", javascriptLibrary);
        setString(component, "autoConfig", autoConfig);
        setString(component, "columns", columns);
        setString(component, "rows", rows);
        setString(component, "justArea", justArea);
        setString(component, "onChange", onChange);
        setString(component, "onSubmit", onSubmit);
    }

    public void release(){
        super.release();
    }

    public static void setString(UIComponent component, String attributeName,String attributeValue){
        if (attributeValue == null) return;
        if (UIComponentTag.isValueReference(attributeValue)) 
        	setValueBinding(component, attributeName, attributeValue);
        else
            component.getAttributes().put(attributeName, attributeValue);
    }

    public static void setValueBinding(UIComponent component,String attributeName, String attributeValue){
        FacesContext context = FacesContext.getCurrentInstance();
        Application app = context.getApplication();
        ValueBinding vb = app.createValueBinding(attributeValue);
        component.setValueBinding(attributeName, vb);
    }
}
