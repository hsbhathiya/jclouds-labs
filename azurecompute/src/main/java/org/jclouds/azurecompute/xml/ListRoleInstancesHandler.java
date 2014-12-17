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
import org.jclouds.azurecompute.domain.RoleInstance;
import org.jclouds.http.functions.ParseSax;
import org.xml.sax.Attributes;

import java.util.List;

public final class ListRoleInstancesHandler extends ParseSax.HandlerForGeneratedRequestWithResult<List<RoleInstance>> {
   private boolean inRoleInstance;
   private final RoleInstanceHandler roleInstanceHandler = new RoleInstanceHandler();
   private final Builder<RoleInstance> instances = ImmutableList.builder();

   @Override
   public List<RoleInstance> getResult() {
      return instances.build();
   }

   @Override
   public void startElement(String url, String name, String qName, Attributes attributes) {
      if (qName.equals("RoleInstance")) {
         inRoleInstance = true;
      }
   }

   @Override
   public void endElement(String uri, String name, String qName) {
      if (qName.equals("RoleInstance")) {
         inRoleInstance = false;
         instances.add(roleInstanceHandler.getResult());
      } else if (inRoleInstance) {
         roleInstanceHandler.endElement(uri, name, qName);
      }
   }

   @Override
   public void characters(char ch[], int start, int length) {
      if (inRoleInstance) {
         roleInstanceHandler.characters(ch, start, length);
      }
   }
}
