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

import org.jclouds.azurecompute.domain.DataVirtualHardDisk;
import org.jclouds.http.functions.ParseSax;

import java.net.URI;

import static org.jclouds.util.SaxUtils.currentOrNull;

/**
 * @see <a href="http://msdn.microsoft.com/en-us/library/jj157191" >api</a>
 */
final class DataVirtualHardDiskHandler extends ParseSax.HandlerForGeneratedRequestWithResult<DataVirtualHardDisk> {

   private String hostCaching;
   private String diskLabel;
   private String diskName;
   private Integer LUN;
   private Integer logicalDiskSizeInGB;
   private URI mediaLink;
   private URI sourceMediaLink;
   private String IOType;

   private final StringBuilder currentText = new StringBuilder();

   @Override
   public DataVirtualHardDisk getResult() {
      DataVirtualHardDisk result = DataVirtualHardDisk
            .create(hostCaching, diskLabel, diskName, LUN, logicalDiskSizeInGB, mediaLink, sourceMediaLink, IOType);
      resetState(); // handler is called in a loop.
      return result;
   }

   private void resetState() {
      hostCaching = diskLabel = diskName = null;
      LUN = null;
      logicalDiskSizeInGB = null;
      mediaLink = null;
      sourceMediaLink = null;
      IOType = null;
   }

   @Override public void endElement(String ignoredUri, String ignoredName, String qName) {

      if (qName.equals("HostCaching")) {
         hostCaching = currentOrNull(currentText);
      } else if (qName.equals("DiskLabel")) {
         diskLabel = currentOrNull(currentText);
      } else if (qName.equals("DiskName")) {
         diskName = currentOrNull(currentText);
      }
      if (qName.equals("Lun")) {
         String lun = currentOrNull(currentText);
         if (lun != null) {
            LUN = Integer.parseInt(lun);
         }
      } else if (qName.equals("LogicalDiskSizeInGB")) {
         String gb = currentOrNull(currentText);
         if (gb != null) {
            logicalDiskSizeInGB = Integer.parseInt(gb);
         }
      } else if (qName.equals("MediaLink")) {
         String link = currentOrNull(currentText);
         if (link != null) {
            mediaLink = URI.create(link);
         }
      } else if (qName.equals("SourceMediaLink")) {
         String link = currentOrNull(currentText);
         if (link != null) {
            sourceMediaLink = URI.create(link);
         }
      } else if (qName.equals("IOType")) {
         IOType = currentOrNull(currentText);
      }
      currentText.setLength(0);
   }

   @Override public void characters(char ch[], int start, int length) {
      currentText.append(ch, start, length);
   }
}
