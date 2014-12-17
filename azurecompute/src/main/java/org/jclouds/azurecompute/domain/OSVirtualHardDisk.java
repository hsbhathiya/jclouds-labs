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
 * @see <a href="http://msdn.microsoft.com/en-us/library/azure/jj157186.aspx#OSVirtualHardDisk" >api</a>
 */
@AutoValue
public abstract class OSVirtualHardDisk {

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
   public abstract String diskName();

   /**
    * Required if the Virtual Machine is being created from a platform image.
    * Specifies the location of the VHD file that is created when SourceImageName specifies a platform image.
    * This element is not used if the Virtual Machine is being created using an existing disk.
    * <p/>
    * Example:
    * http://contoso.blob.core.windows.net/disks/mydisk.vhd
    */
   public abstract URI mediaLink();

   /**
    * Specifies the name of the image to use to create the Virtual Machine.You can specify a user image or a platform image.
    * An image is always associated with a VHD, which is a .vhd file stored as a page blob in a storage account in Azure.
    * If you specify a platform image, an associated VHD is created and you must use the MediaLink element to specify the
    * location in storage where the VHD will be located.
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
    * When an OS Image or a RemoteSourceImage is used to create an OSVirtualHardDisk, this parameter can be used to resize
    * the new OSVirtualHardDisk to a larger size. ResizedSizeInGB must be larger than the underlying OS Image’s LogicalSizeInGB.
    * The ResizedSizeInGB element is only available using version 2014-10-01 or higher.
    */
   @Nullable public abstract Integer resizedSizeInGB();

   /**
    * This property identifies the type of the storage account for the backing VHD.
    * If the backing VHD is in an Provisioned Storage account, “Provisioned” is returned otherwise “Standard”
    * is returned.
    * <p/>
    * This property is only returned with a version header of 2014-10-01 or newer
    */
   @Nullable public abstract String IOType();

   public static OSVirtualHardDisk create(String hostCaching, String diskLabel, String diskName, URI mediaLink,
         String sourceImageName, OSImage.Type OS, Integer resizedSizeInGB, URI remoteSourceImageLink, String IOType) {
      return new AutoValue_OSVirtualHardDisk(hostCaching, diskLabel, diskName, mediaLink, sourceImageName, OS,
            remoteSourceImageLink, resizedSizeInGB, IOType);
   }
}
