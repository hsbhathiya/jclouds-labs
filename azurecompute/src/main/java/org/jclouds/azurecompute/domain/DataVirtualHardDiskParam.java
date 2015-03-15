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
 * Create DataVirtualHardDisk
 *
 * @see <a href="http://msdn.microsoft.com/en-us/library/azure/jj157186.aspx#DataVirtualHardDisks" >api</a>
 */

@AutoValue public abstract class DataVirtualHardDiskParam {

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
    * Specifies the Logical Unit Number (LUN) for the data disk. If the disk is the first disk that is added,
    * this element is optional and the default value of 0 is used. If more than one disk is being added,
    * this element is required.
    * <p/>
    * You can use Get Role to find the LUN numbers that are already being used.
    * Valid LUN values are 0 through 31
    */
   @Nullable public abstract Integer LUN();

   /**
    * Specifies the size, in GB, of an empty disk to be attached to the Virtual Machine.If the disk that is being added
    * is already registered in the subscription, this element is ignored.If the disk and VHD is being created by Azure
    * as it is added, this element defines the size of the new disk.
    * <p/>
    * The number of disks that can be added to a Virtual Machine is limited by the size of the machine.
    * <p/>
    * This element is used with the MediaLink element.
    */
   @Nullable public abstract Integer logicalDiskSizeInGB();

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
   @Nullable public abstract URI sourceMediaLink();

   public Builder toBuilder() {
      return builder().fromDataVirtualHardDiskParams(this);
   }

   public static Builder builder() {
      return new Builder();
   }

   public static final class Builder {

      private String hostCaching;
      private String diskLabel;
      private String diskName;
      private Integer LUN;
      private Integer logicalDiskSizeInGB;
      private URI mediaLink;
      private URI sourceMediaLink;

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

      public Builder LUN(Integer LUN) {
         this.LUN = LUN;
         return this;
      }

      public Builder logicalDiskSizeInGB(Integer logicalDiskSizeInGB) {
         this.logicalDiskSizeInGB = logicalDiskSizeInGB;
         return this;
      }

      public Builder mediaLink(URI mediaLink) {
         this.mediaLink = mediaLink;
         return this;
      }

      public Builder sourceMediaLink(URI sourceMediaLink) {
         this.sourceMediaLink = sourceMediaLink;
         return this;
      }

      public Builder fromDataVirtualHardDiskParams(DataVirtualHardDiskParam in) {
         return hostCaching(in.hostCaching()).diskLabel(in.diskLabel()).diskName(in.diskName()).LUN(in.LUN())
               .logicalDiskSizeInGB(in.logicalDiskSizeInGB()).mediaLink(in.mediaLink())
               .sourceMediaLink(in.sourceMediaLink());
      }

   }

   public static DataVirtualHardDiskParam create(String hostCaching, String diskLabel, String diskName, Integer lun,
         Integer logicalDiskSizeInGB, URI mediaLink, URI sourceMediaLink) {
      return new AutoValue_DataVirtualHardDiskParam(hostCaching, diskLabel, diskName, lun, logicalDiskSizeInGB,
            mediaLink, sourceMediaLink);
   }
}
