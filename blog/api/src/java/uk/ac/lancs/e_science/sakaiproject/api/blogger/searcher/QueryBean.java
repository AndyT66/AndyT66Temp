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

package uk.ac.lancs.e_science.sakaiproject.api.blogger.searcher;

public class QueryBean {
    private String _queryString;
    private int _visibility;
    private long _initDate;
    private long _endDate;
    private String _user;

    public QueryBean(){
        _visibility = -1; //this mean no filter by visibility
        _initDate = -1; //this mean no filter by initDate;
        _endDate = -1; //this mean no filter by endDate
        _user ="";
    }

    public boolean areThereAnyCondition(){
        return _visibility!=-1||
                _initDate!=-1||
                _endDate!=-1;
    }

    public void setQueryString(String queryString){
        _queryString = queryString;
    }
    public String getQueryString(){
        return _queryString;
    }
    public boolean queryByVisibility(){
        return _visibility!=-1;
    }
    public void  setVisibility(int visibility){
        _visibility = visibility;
    }
    public int getVisibility(){
        return _visibility;
    }
    public boolean queryByInitDate(){
        return _initDate!=-1;
    }
    public void setInitDate(long initDate){
        _initDate = initDate;
    }
    public long getInitDate(){
        return _initDate;
    }
    public boolean queryByEndDate(){
        return _endDate!=-1;
    }
    public void setEndDate(long endDate){
        _endDate = endDate;
    }
    public long getEndDate(){
        return _endDate;
    }
    public void setUser(String user){
    	this._user = user;
    }
    public String getUser(){
    	return _user;
    }
}
