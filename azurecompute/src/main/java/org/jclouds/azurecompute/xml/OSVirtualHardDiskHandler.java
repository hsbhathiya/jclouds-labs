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

import org.jclouds.azurecompute.domain.OSImage;
import org.jclouds.azurecompute.domain.OSVirtualHardDisk;
import org.jclouds.http.functions.ParseSax;

import java.net.URI;

import static org.jclouds.util.SaxUtils.currentOrNull;

/**
 * @see <a href="http://msdn.microsoft.com/en-us/library/jj157191" >api</a>
 */
final class OSVirtualHardDiskHandler extends ParseSax.HandlerForGeneratedRequestWithResult<OSVirtualHardDisk> {
   private String hostCaching;
   private String diskLabel;
   private String diskName;
   private URI mediaLink;
   private String sourceImageName;
   private OSImage.Type OS;
   private URI remoteSourceImageLink;
   private Integer resizedSizeInGB;
   private String IOType;

   private final StringBuilder currentText = new StringBuilder();

   @Override
   public OSVirtualHardDisk getResult() {
      OSVirtualHardDisk result = OSVirtualHardDisk
            .create(hostCaching, diskLabel, diskName, mediaLink, sourceImageName, OS, resizedSizeInGB,
                  remoteSourceImageLink, IOType);
      resetState(); // handler is called in a loop.
      return result;
   }

   private void resetState() {
      hostCaching = diskLabel = diskName = null;
      mediaLink = null;
      sourceImageName = null;
      OS = null;
      remoteSourceImageLink = null;
      resizedSizeInGB = null;
      IOType = null;
   }

   @Override
   public void endElement(String ignoredUri, String ignoredName, String qName) {

      if (qName.equals("HostCaching")) {
         hostCaching = currentOrNull(currentText);
      } else if (qName.equals("DiskLabel")) {
         diskLabel = currentOrNull(currentText);
      } else if (qName.equals("DiskName")) {
         diskName = currentOrNull(currentText);
      } else if (qName.equals("MediaLink")) {
         String link = currentOrNull(currentText);
         if (link != null) {
            mediaLink = URI.create(link);
         }
      } else if (qName.equals("SourceImageName")) {
         sourceImageName = currentOrNull(currentText);
      } else if (qName.equals("OS")) {
         String osText = currentOrNull(currentText);
         if (osText != null) {
            OS = OSImage.Type.valueOf(osText.toUpperCase());
         }
      } else if (qName.equals("ResizedSizeInGB")) {
         String resizedSizeInGBText = currentOrNull(currentText);
         if (resizedSizeInGBText != null) {
            resizedSizeInGB = Integer.parseInt(resizedSizeInGBText);
         }
      } else if (qName.equals("RemoteSourceImageLink")) {
         String link = currentOrNull(currentText);
         if (link != null) {
            remoteSourceImageLink = URI.create(link);
         }
      } else if (qName.equals("IOType")) {
         IOType = currentOrNull(currentText);
      }
      currentText.setLength(0);
   }

   @Override
   public void characters(char ch[], int start, int length) {
      currentText.append(ch, start, length);
   }
}
