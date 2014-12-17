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

import java.net.URI;
import java.util.List;

import org.jclouds.javax.annotation.Nullable;

import com.google.auto.value.AutoValue;

/**
 * @see <a href="http://msdn.microsoft.com/en-us/library/azure/jj157193.aspx" >api</a>
 */
@AutoValue
public abstract class Role {

   /* Specifies the name for the Virtual Machine
    */
   public abstract String roleName();

   /* Specifies the type of role to use. For Virtual Machines, this must be PersistentVMRole
   */
   public abstract String roleType();

   @Nullable public abstract String VMImageName();

   /**
    * Required if the Virtual Machine is being created from a published VM Image.
    * Specifies the location of the VHD file that is created when VMImageName specifies a published VM Image.
    * <p/>
    * The MediaLocation element is only available using version 2014-05-01 or higher.
    */
   @Nullable public abstract URI mediaLocation();

   /**
    * Specifies the size of the Virtual Machine. The default size is Small.
    */

   @Nullable public abstract RoleSize roleSize();

   /**
    * Specifies the name of a collection of Virtual Machines.
    * Virtual Machines specified in the same availability set are allocated to different nodes to maximize availability.
    * For more information about availability sets, see Manage the Availability of Virtual Machines
    */
   @Nullable public abstract String availabilitySetName();

   /**
    * Contains the parameters that are used to add a data disk to a Virtual Machine.
    * If you are creating a Virtual Machine by using a VM Image, this element is not used.
    * For more information about data disks
    */

   @Nullable public abstract List<DataVirtualHardDisk> dataVirtualHardDisks();

   /**
    * Contains the parameters that are used to add a data disk to a Virtual Machine.
    * If you are creating a Virtual Machine by using a VM Image, this element is not used.
    * For more information about data disks
    */
   @Nullable public abstract OSVirtualHardDisk OSVirtualHardDisk();

   /**
    * Indicates whether the VM Agent is installed on the Virtual Machine.
    * To run a resource extension in a Virtual Machine, this service must be installed.
    */
   @Nullable public abstract Boolean provisionGuestAgent();

   public static Role create(String roleName, String roleType, String VMImageName, URI mediaLocation, RoleSize roleSize,
         String availabilitySetName, List<DataVirtualHardDisk> dataVirtualHardDisks,
         OSVirtualHardDisk OSVirtualHardDisk, Boolean provisionGuestAgent) {
      return new AutoValue_Role(roleName, roleType, VMImageName, mediaLocation, roleSize,
            availabilitySetName, dataVirtualHardDisks, OSVirtualHardDisk, provisionGuestAgent);
   }
}
