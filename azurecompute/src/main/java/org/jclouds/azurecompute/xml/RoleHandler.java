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

import org.jclouds.azurecompute.domain.DataVirtualHardDisk;
import org.jclouds.azurecompute.domain.OSVirtualHardDisk;
import org.jclouds.azurecompute.domain.Role;
import org.jclouds.azurecompute.domain.RoleSizeName;
import org.jclouds.http.functions.ParseSax;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.net.URI;

import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static org.jclouds.util.SaxUtils.currentOrNull;

/**
 * @see <a href="http://msdn.microsoft.com/en-us/library/ee460804" >Response body description</a>.
 */
public final class RoleHandler extends ParseSax.HandlerForGeneratedRequestWithResult<Role> {
   private String roleName;
   private String roleType;
   private String VMImage;
   private URI mediaLocation;
   private RoleSizeName roleSize;
   private String availabilitySetName;
   private OSVirtualHardDisk OSVirtualHardDisk;
   private Boolean provisionGuestAgent;
   private final ImmutableList.Builder<DataVirtualHardDisk> dataVirtualHardDisks = ImmutableList.builder();


   private final OSVirtualHardDiskHandler osVirtualHardDiskHandler = new OSVirtualHardDiskHandler();
   private final DataVirtualHardDiskHandler dataVirtualHardDiskHandler = new DataVirtualHardDiskHandler();
   private final StringBuilder currentText = new StringBuilder();

   private boolean inOSVHD;
   private boolean inDataVHD;

   @Override
   public Role getResult() { // Fields don't need to be reset as this isn't used in a loop.
      return Role.create(roleName, roleType, VMImage, mediaLocation, roleSize, availabilitySetName,
            dataVirtualHardDisks.build(), OSVirtualHardDisk, provisionGuestAgent);

   }

   @Override
   public void startElement(String url, String name, String qName, Attributes attributes) throws SAXException {
      if (qName.equals("OSVirtualHardDisk")) {
         inOSVHD = true;
      }
      if (inOSVHD) {
         osVirtualHardDiskHandler.startElement(url, name, qName, attributes);
      }
      if (qName.equals("DataVirtualHardDisk")) {
         inDataVHD = true;
      }
      if (inDataVHD) {
         dataVirtualHardDiskHandler.startElement(url, name, qName, attributes);
      }
   }

   @Override
   public void endElement(String ignoredUri, String ignoredName, String qName) {

      if (qName.equals("RoleName")) {
         roleName = currentOrNull(currentText);
      } else if (qName.equals("RoleType")) {
         roleType = currentOrNull(currentText);
      } else if (qName.equals("VMImage")) {
         VMImage = currentOrNull(currentText);
      } else if (qName.equals("MediaLocation")) {
         String mediaLocationText = currentOrNull(currentText);
         if (mediaLocationText != null) {
            mediaLocation = URI.create(mediaLocationText);
         }
      } else if (qName.equals("RoleSize")) {
         String roleSizeText = currentOrNull(currentText);
         if (roleSizeText != null) {
            roleSize = parseRoleSize(roleSizeText);
         }
      } else if (qName.equals("AvailabilitySetName")) {
         availabilitySetName = currentOrNull(currentText);
      } else if (qName.equals("OSVirtualHardDisk")) {
         OSVirtualHardDisk = osVirtualHardDiskHandler.getResult();
         inOSVHD = false;
      } else if (inOSVHD) {
         osVirtualHardDiskHandler.endElement(ignoredUri, ignoredName, qName);
      } else if (qName.equals("DataVirtualHardDisk")) {
         dataVirtualHardDisks.add(dataVirtualHardDiskHandler.getResult());
         inDataVHD = false;
      } else if (inDataVHD) {
         dataVirtualHardDiskHandler.endElement(ignoredUri, ignoredName, qName);
      } else if (qName.equals("ProvisionGuestAgent")) {
         String provisionGuestAgentText = currentOrNull(currentText);
         if (provisionGuestAgentText != null) {
            provisionGuestAgent = parseProvisionGuestAgent(provisionGuestAgentText);
         }
      }
      currentText.setLength(0);
   }

   @Override
   public void characters(char ch[], int start, int length) {
      if (inOSVHD) {
         osVirtualHardDiskHandler.characters(ch, start, length);
      } else if (inDataVHD) {
         dataVirtualHardDiskHandler.characters(ch, start, length);
      } else {
         currentText.append(ch, start, length);
      }
   }

   private static Boolean parseProvisionGuestAgent(String provisionGuestAgent) {
      try {
         return Boolean.getBoolean(provisionGuestAgent);
      } catch (IllegalArgumentException e) {
         return Boolean.FALSE;
      }
   }

   private static RoleSizeName parseRoleSize(String roleSize) {
      try {
         return RoleSizeName.valueOf(UPPER_CAMEL.to(UPPER_UNDERSCORE, roleSize));
      } catch (IllegalArgumentException e) {
         return RoleSizeName.UNRECOGNIZED;
      }
   }
}
