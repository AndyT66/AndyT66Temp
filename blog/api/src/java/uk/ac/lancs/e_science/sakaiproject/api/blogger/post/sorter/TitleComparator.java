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

package uk.ac.lancs.e_science.sakaiproject.api.blogger.post.sorter;


import java.util.Comparator;

import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;

public class TitleComparator implements Comparator{
    public int compare(Object o, Object o1) {
        Post post1 = (Post) o;
        Post post2 = (Post) o1;
        return post1.getTitle().compareTo(post2.getTitle());
     }
}
