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

import org.jclouds.azurecompute.domain.RoleSize;
import org.jclouds.azurecompute.domain.RoleSizeName;
import org.jclouds.http.functions.ParseSax;

import static org.jclouds.util.SaxUtils.currentOrNull;

/**
 * @see <a href="https://msdn.microsoft.com/en-us/library/azure/dn469422.aspx" ></a>.
 */
public final class RoleSizeHandler extends ParseSax.HandlerForGeneratedRequestWithResult<RoleSize> {

   private RoleSizeName name;
   private String label;
   private Integer cores;
   private Integer memoryInMb;
   private Boolean supportedByWebWorkerRoles;
   private Boolean supportedByVirtualMachines;
   private Integer maxDataDiskCount;
   private Integer webWorkerResourceDiskSizeInMb;
   private Integer virtualMachineResourceDiskSizeInMb;

   private final StringBuilder currentText = new StringBuilder();

   @Override
   public RoleSize getResult() { // Fields don't need to be reset as this isn't used in a loop.
      return RoleSize.create(name, label, cores, memoryInMb, supportedByWebWorkerRoles, supportedByVirtualMachines,
            maxDataDiskCount, webWorkerResourceDiskSizeInMb, virtualMachineResourceDiskSizeInMb);

   }

   @Override
   public void endElement(String ignoredUri, String ignoredName, String qName) {

      if (qName.equals("Name")) {
         String nameText = currentOrNull(currentText);
         if (nameText != null) {
            name = parseRoleSizeName(nameText);
         }
      } else if (qName.equals("Label")) {
         label = currentOrNull(currentText);
      } else if (qName.equals("Cores")) {
         String coresText = currentOrNull(currentText);
         if (coresText != null) {
            cores = Integer.parseInt(coresText);
         }
      } else if (qName.equals("MemoryInMb")) {
         String memoryText = currentOrNull(currentText);
         if (memoryText != null) {
            memoryInMb = Integer.parseInt(memoryText);
         }
      } else if (qName.equals("SupportedByWebWorkerRoles")) {
         String webRoleText = currentOrNull(currentText);
         if (webRoleText != null) {
            supportedByWebWorkerRoles = Boolean.parseBoolean(webRoleText);
         }
      } else if (qName.equals("SupportedByVirtualMachines")) {
         String VMText = currentOrNull(currentText);
         if (VMText != null) {
            supportedByVirtualMachines = Boolean.parseBoolean(VMText);
         }
      } else if (qName.equals("MaxDataDiskCount")) {
         String maxDisksText = currentOrNull(currentText);
         if (maxDisksText != null) {
            maxDataDiskCount = Integer.parseInt(maxDisksText);
         }
      } else if (qName.equals("WebWorkerResourceDiskSizeInMb")) {
         String WWRText = currentOrNull(currentText);
         if (WWRText != null) {
            webWorkerResourceDiskSizeInMb = Integer.parseInt(WWRText);
         }
      } else if (qName.equals("VirtualMachineResourceDiskSizeInMb")) {
         String VMRText = currentOrNull(currentText);
         if (VMRText != null) {
            virtualMachineResourceDiskSizeInMb = Integer.parseInt(VMRText);
         }
      }
      currentText.setLength(0);
   }

   @Override
   public void characters(char ch[], int start, int length) {
      currentText.append(ch, start, length);
   }

   private static RoleSizeName parseRoleSizeName(String roleSize) {

      try {
         if ("extra small".equals(roleSize) || "small".equals(roleSize) || "medium".equals(roleSize) || "large"
               .equals(roleSize) || "extra large".equals(roleSize)) {
            return RoleSizeName.valueOf(roleSize.toUpperCase().replace(" ", "_"));
         } else {
            return RoleSizeName.valueOf(roleSize);
         }
      } catch (IllegalArgumentException e) {
         return RoleSizeName.UNRECOGNIZED;
      }
   }
}
