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
import javax.faces.context.FacesContext;
import javax.faces.component.UIComponent;
import java.util.Date;
import java.text.SimpleDateFormat;

public class DateConverter implements Converter{
    public Object getAsObject(FacesContext context, UIComponent component, String target) throws ConverterException{
        try{
            SimpleDateFormat sdf = new SimpleDateFormat();
            Date date = sdf.parse(target);
            return new Long(date.getTime());
        } catch (Exception e){
           // throw new ConverterException
            return new Long(new Date().getTime());
        }
    }

    public String getAsString(FacesContext context, UIComponent component, Object target) throws ConverterException{
        long value = ((Long)target).longValue();
        Date date = new Date(value);
        SimpleDateFormat sdf = new SimpleDateFormat();
      
        return sdf.format(date);
    }
}
