/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jclouds.azurecompute.xml;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import org.jclouds.azurecompute.domain.Disk;
import org.jclouds.azurecompute.domain.RoleSize;
import org.jclouds.http.functions.ParseSax;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.List;

public final class ListRoleSizeHandler extends ParseSax.HandlerForGeneratedRequestWithResult<List<RoleSize>> {
   private boolean inRoleSize;
   private final RoleSizeHandler roleSizeHandler = new RoleSizeHandler();
   private final Builder<RoleSize> roleSizeList = ImmutableList.builder();

   @Override public List<RoleSize> getResult() {
      return roleSizeList.build();
   }

   @Override public void startElement(String url, String name, String qName, Attributes attributes) throws
         SAXException {
      if (qName.equals("RoleSize")) {
         inRoleSize = true;
      }
      if (inRoleSize) {
         roleSizeHandler.startElement(url, name, qName, attributes);
      }
   }

   @Override public void endElement(String uri, String name, String qName) {
      if (qName.equals("RoleSize")) {
         inRoleSize = false;
         roleSizeList.add(roleSizeHandler.getResult());
      } else if (inRoleSize) {
         roleSizeHandler.endElement(uri, name, qName);
      }
   }

   @Override public void characters(char ch[], int start, int length) {
      if (inRoleSize) {
         roleSizeHandler.characters(ch, start, length);
      }
   }
}
