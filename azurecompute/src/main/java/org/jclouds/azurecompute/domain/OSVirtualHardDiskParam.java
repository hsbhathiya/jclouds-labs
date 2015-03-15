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
package org.jclouds.azurecompute.domain;

import com.google.auto.value.AutoValue;
import org.jclouds.javax.annotation.Nullable;

import java.net.URI;

/**
 * Create OSVirtualHardDisk
 *
 * @see <a href="http://msdn.microsoft.com/en-us/library/azure/jj157186.aspx#OSVirtualHardDisk" >api</a>
 */

@AutoValue
public abstract class OSVirtualHardDiskParam {

   /**
    * Specifies the caching mode of the operating system disk.
    * This setting impacts the consistency and performance of the disk.
    * Possible values are:
    * ReadOnly
    * ReadWrite
    * The default value is ReadWrite
    */
   @Nullable public abstract String hostCaching();

   /**
    * Specifies the description of the disk.
    */
   @Nullable public abstract String diskLabel();

   /**
    * Required if an existing disk is being used to create a Virtual Machine.
    * Specifies the name of a new or existing disk
    */
   @Nullable public abstract String diskName();

   /**
    * If the disk that is being added is already registered in the subscription or the VHD for the disk already exists
    * in blob storage, this element is ignored. If a VHD file does not exist in blob storage, this element defines the
    * location of the new VHD that is created when the new disk is added.
    * Example:
    * http://example.blob.core.windows.net/disks/mydatadisk.vhd
    */
   @Nullable public abstract URI mediaLink();

   /**
    * If the disk that is being added is already registered in the subscription or the VHD for the disk does not exist
    * in blob storage, this element is ignored. If the VHD file exists in blob storage, this element defines the path to
    * the VHD and a disk is registered from it and attached to the virtual machine.
    */
   @Nullable public abstract String sourceImageName();

   /**
    * Specifies the type of operating system that is installed in the image.
    * Possible values are:
    * Windows
    * Linux
    */
   @Nullable public abstract OSImage.Type OS();

   /**
    * Specifies a URI to the location where an OS image is stored that is used to create the Virtual Machine.
    * An image is always associated with a VHD, which is a .vhd file stored as a page blob in a storage account
    * in Windows Azure. If you specify the path to an image with this element, an associated VHD is created and
    * you must use the MediaLink element to specify the location in storage where the VHD will be located.
    * If this element is used, SourceImageName is not used.
    * <p/>
    * The RemoteSourceImageLink element is only available using version 2014-05-01 or higher.
    */
   @Nullable public abstract URI remoteSourceImageLink();

   /**
    * When an OS Image or a RemoteSourceImage is used to create an OSVirtualHardDisk, this parameter can be used to
    * resize the new OSVirtualHardDisk to a larger size. ResizedSizeInGB must be larger than the underlying OS Image’s
    * LogicalSizeInGB. The ResizedSizeInGB element is only available using version 2014-10-01 or higher.
    */
   @Nullable public abstract Integer resizedSizeInGB();

   public Builder toBuilder() {
      return builder().fromOSVirtualHardDiskParams(this);
   }

   public static Builder builder() {
      return new Builder();
   }

   public static final class Builder {

      private String hostCaching;
      private String diskLabel;
      private String diskName;
      private URI mediaLink;
      private String sourceImageName;
      private OSImage.Type os;
      private URI remoteSourceImageLink;
      private Integer resizedSizeInGB;

      public Builder hostCaching(String hostCaching) {
         this.hostCaching = hostCaching;
         return this;
      }

      public Builder diskLabel(String diskLabel) {
         this.diskLabel = diskLabel;
         return this;
      }

      public Builder diskName(String diskName) {
         this.diskName = diskName;
         return this;
      }

      public Builder mediaLink(URI mediaLink) {
         this.mediaLink = mediaLink;
         return this;
      }

      public Builder sourceImageName(String sourceImageName) {
         this.sourceImageName = sourceImageName;
         return this;
      }

      public Builder os(OSImage.Type os) {
         this.os = os;
         return this;
      }

      public Builder remoteSourceImageLink(URI remoteSourceImageLink) {
         this.remoteSourceImageLink = remoteSourceImageLink;
         return this;
      }

      public Builder resizedSizeInGB(Integer resizedSizeInGB) {
         this.resizedSizeInGB = resizedSizeInGB;
         return this;
      }

      public Builder fromOSVirtualHardDiskParams(OSVirtualHardDiskParam in) {
         return hostCaching(in.hostCaching()).diskLabel(in.diskLabel()).diskName(in.diskName())
               .mediaLink(in.mediaLink()).sourceImageName(in.sourceImageName()).os(in.OS())
               .remoteSourceImageLink(in.remoteSourceImageLink()).resizedSizeInGB(in.resizedSizeInGB());
      }

      public OSVirtualHardDiskParam build() {
         return OSVirtualHardDiskParam
               .create(hostCaching, diskLabel, diskName, mediaLink, sourceImageName, os, remoteSourceImageLink,
                     resizedSizeInGB);
      }

   }

   public static OSVirtualHardDiskParam create(String hostCaching, String diskLabel, String diskName, URI mediaLink,
         String sourceImageName, OSImage.Type os, URI remoteSourceImageLink, Integer resizedSizeInGB) {
      return new AutoValue_OSVirtualHardDiskParam(hostCaching, diskLabel, diskName, mediaLink, sourceImageName, os,
            remoteSourceImageLink, resizedSizeInGB);
   }

}
