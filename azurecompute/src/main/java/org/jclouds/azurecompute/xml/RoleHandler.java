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

import static org.jclouds.util.SaxUtils.currentOrNull;
import java.util.List;

import org.jclouds.azurecompute.domain.Role;
import org.jclouds.azurecompute.domain.Role.ConfigurationSet;
import org.jclouds.azurecompute.domain.Role.DataVirtualHardDisk;
import org.jclouds.azurecompute.domain.Role.OSVirtualHardDisk;
import org.jclouds.azurecompute.domain.Role.ResourceExtensionReference;
import org.jclouds.azurecompute.domain.RoleSize;
import org.jclouds.http.functions.ParseSax;
import org.xml.sax.Attributes;

import com.google.common.collect.Lists;

public class RoleHandler extends ParseSax.HandlerForGeneratedRequestWithResult<Role> {

   private String roleName;
   private String osVersion;
   private String roleType;
   private List<ConfigurationSet> configurationSets = Lists.newArrayList();
   private List<ResourceExtensionReference> resourceExtensionReferences = Lists.newArrayList();
   private List<DataVirtualHardDisk> dataVirtualHardDisks = Lists.newArrayList();
   private OSVirtualHardDisk osVirtualHardDisk;
   private RoleSize.Type roleSize;

   private boolean inConfigurationSets;
   private boolean inOSVirtualHardDisk;
   private final ConfigurationSetHandler configurationSetHandler = new ConfigurationSetHandler();
   private final OSVirtualHardDiskHandler osVirtualDiskHandler = new OSVirtualHardDiskHandler();

   private StringBuilder currentText = new StringBuilder();

   @Override public void startElement(String uri, String localName, String qName, Attributes attributes) {
      if (qName.equals("ConfigurationSets")) {
         inConfigurationSets = true;
      }
      if (inConfigurationSets) {
         configurationSetHandler.startElement(uri, localName, qName, attributes);
      }
      if (qName.equals("OSVirtualHardDisk")) {
         inOSVirtualHardDisk = true;
      }
   }

   private void resetState() {
      roleName = osVersion = roleType = null;
      configurationSets = null;
      osVirtualHardDisk = null;
      configurationSets = Lists.newArrayList();
      resourceExtensionReferences = Lists.newArrayList();
      dataVirtualHardDisks = Lists.newArrayList();
      roleSize = null;
   }

   @Override
   public Role getResult() {
      Role result = Role.create(roleName, osVersion, roleType, configurationSets, resourceExtensionReferences,
              dataVirtualHardDisks, osVirtualHardDisk, roleSize);
      resetState(); // handler is called in a loop.
      return result;
   }

   @Override public void endElement(String ignoredUri, String ignoredName, String qName) {
      if (qName.equals("ConfigurationSets")) {
         inConfigurationSets = false;
      } else if (qName.equals("ConfigurationSet")) {
         configurationSets.add(configurationSetHandler.getResult());
      } else if (inConfigurationSets) {
         configurationSetHandler.endElement(ignoredUri, ignoredName, qName);
      } else if (qName.equals("RoleName")) {
         roleName = currentOrNull(currentText);
      } else if (qName.equals("OsVersion")) {
         osVersion = currentOrNull(currentText);
      } else if (qName.equals("RoleType")) {
         roleType = currentOrNull(currentText);
      } else if (qName.equals("OSVirtualHardDisk")) {
            osVirtualHardDisk = osVirtualDiskHandler.getResult();
            inOSVirtualHardDisk = false;
         } else if (inOSVirtualHardDisk) {
         osVirtualDiskHandler.endElement(ignoredUri, ignoredName, qName);
      } else if (qName.equals("RoleSize")) {
         roleSize = RoleSize.Type.valueOf(currentOrNull(currentText).toUpperCase());
      }
      currentText.setLength(0);
   }

   @Override public void characters(char ch[], int start, int length) {
      if (inConfigurationSets) {
         configurationSetHandler.characters(ch, start, length);
      } else if (inOSVirtualHardDisk) {
         osVirtualDiskHandler.characters(ch, start, length);
      } else {
         currentText.append(ch, start, length);
      }
   }

}
