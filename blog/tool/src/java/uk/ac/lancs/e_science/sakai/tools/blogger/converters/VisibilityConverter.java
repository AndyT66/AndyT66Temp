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
package uk.ac.lancs.e_science.sakai.tools.blogger.converters;

import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.State;

public class VisibilityConverter implements Converter{
    public Object getAsObject(FacesContext context, UIComponent component, String target) throws ConverterException{
        if (target==null || target.trim().equals(""))
            throw new ConverterException();
        if (target.equals("PRIVATE"))
            return new Integer(State.PRIVATE);
        if (target.equals("SITE"))
            return new Integer(State.SITE);
        if (target.equals("PUBLIC"))
            return new Integer(State.PUBLIC);
        if (target.equals("TUTOR"))
            return new Integer(State.TUTOR);
        
        throw new ConverterException(); //Unknow target
    }

    public String getAsString(FacesContext context, UIComponent component, Object target) throws ConverterException{
        Integer value = (Integer) target;
        if (value == null)
            throw new ConverterException();
        if (value.intValue()==State.PRIVATE)
            return "PRIVATE";
        if (value.intValue()==State.SITE)
            return "SITE";
        if (value.intValue()==State.PUBLIC)
            return "PUBLIC";
        if (value.intValue()==State.TUTOR)
            return "TUTOR";
        
        return value.toString();
    }
}
