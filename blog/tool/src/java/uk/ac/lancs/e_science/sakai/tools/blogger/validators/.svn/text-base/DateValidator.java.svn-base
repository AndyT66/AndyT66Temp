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
package uk.ac.lancs.e_science.sakai.tools.blogger.validators;

import com.sun.faces.util.MessageFactory;

import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.faces.context.FacesContext;
import javax.faces.component.UIComponent;
import java.text.SimpleDateFormat;

public class DateValidator implements Validator{
    public void validate(FacesContext facesContext, UIComponent uiComponent, Object o) throws ValidatorException {
        if ((facesContext==null)||(uiComponent==null))
            throw new IllegalArgumentException(facesContext==null?"facesContext":"uiComponent"+" cannot be null");

        String val = (String) o;

        try{
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
            simpleDateFormat.parse(val);

        } catch (Exception e){
            throw new ValidatorException(MessageFactory.getMessage(facesContext,"Invalid date", new Object[]{val}));
        }
    }
}
