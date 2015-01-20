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
import org.jclouds.azurecompute.domain.OSVirtualHardDisk.Caching;
import org.jclouds.http.functions.ParseSax;

import java.net.URI;

import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static org.jclouds.util.SaxUtils.currentOrNull;

/**
 * @see <a href="http://msdn.microsoft.com/en-us/library/azure/ee460804.aspx#OSVirtualHardDisk" >api</a>
 */
final class OSVirtualHardDiskHandler extends ParseSax.HandlerForGeneratedRequestWithResult<OSVirtualHardDisk> {
   private Caching hostCaching;
   private String diskName;
   private URI mediaLink;
   private String sourceImageName;
   private OSImage.Type OS;
   private URI remoteSourceImageLink;
   private String IOType;

   private final StringBuilder currentText = new StringBuilder();

   @Override
   public OSVirtualHardDisk getResult() {
      OSVirtualHardDisk result = OSVirtualHardDisk
            .create(hostCaching, diskName, mediaLink, sourceImageName, OS, remoteSourceImageLink, IOType);
      return result;
   }

   @Override
   public void endElement(String ignoredUri, String ignoredName, String qName) {
      if (qName.equals("HostCaching")) {
         String hostCachingText = currentOrNull(currentText);
         if (hostCachingText != null) {
            hostCaching = parseHostCache(hostCachingText);
         }
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

   private static Caching parseHostCache(String hostCaching) {
      try {
         return Caching.valueOf(UPPER_CAMEL.to(UPPER_UNDERSCORE, hostCaching));
      } catch (IllegalArgumentException e) {
         return Caching.READ_ONLY;
      }
   }
}
