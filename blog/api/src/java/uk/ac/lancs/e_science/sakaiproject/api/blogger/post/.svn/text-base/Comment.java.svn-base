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

package uk.ac.lancs.e_science.sakaiproject.api.blogger.post;

import java.util.Date;

public class Comment {
    private String _text;
    private long _date;
    private Creator _creator;

    public Comment(){

    }
    public Comment(String text){
        _text = text;
        _date = new Date().getTime();
    }
    public Comment(String text, long date){
        _text = text;
        _date = date;
    }
    public void addCreator(Creator creator){
        _creator = creator;
    }
    public void setText(String text){
        _text = text;
    }
    public String getText(){
        return _text;
    }
    public void setDate(long date){
        _date = date;
    }
    public long getDate(){
        return _date;
    }
    public Creator getCreator(){
        return _creator;
    }
    public void setCreator(Creator creator){
        _creator = creator;
    }
}
