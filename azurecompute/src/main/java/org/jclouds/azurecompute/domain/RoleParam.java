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
import com.google.common.collect.Lists;
import org.jclouds.javax.annotation.Nullable;

import java.net.URI;
import java.util.Collection;
import java.util.List;

/**
 * Create a Role.
 *
 * @see <a href="http://msdn.microsoft.com/en-us/library/azure/jj157186" >api</a>
 */
@AutoValue
public abstract class RoleParam {

   /* Specifies the name for the Virtual Machine
    */
   public abstract String roleName();

   /* Specifies the type of role to use. For Virtual Machines, this must be PersistentVMRole
   */
   public abstract String roleType();

   @Nullable public abstract WindowsConfigurationSetParams windowsConfigurationSet();

   @Nullable public abstract LinuxConfigurationSetParams linuxConfigurationSet();

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

   @Nullable public abstract RoleSize.Type roleSize();

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

   @Nullable public abstract List<DataVirtualHardDiskParam> dataVirtualHardDiskParams();

   /**
    * Contains the parameters that are used to add a data disk to a Virtual Machine.
    * If you are creating a Virtual Machine by using a VM Image, this element is not used.
    * For more information about data disks
    */
   @Nullable public abstract OSVirtualHardDiskParam OSVirtualHardDiskParam();

   /**
    * Indicates whether the VM Agent is installed on the Virtual Machine.
    * To run a resource extension in a Virtual Machine, this service must be installed.
    */
   @Nullable public abstract Boolean provisionGuestAgent();

   public Builder toBuilder() {
      return builder().fromRoleParams(this);
   }

   public static Builder builder() {
      return new Builder();
   }

   public static final class Builder {

      private final String ROLE_TYPE = "PersistentVMRole";

      private String roleName;
      private String roleType = ROLE_TYPE;
      private WindowsConfigurationSetParams windowsConfigurationSet;
      private LinuxConfigurationSetParams linuxConfigurationSet;
      private String VMImageName;
      private URI mediaLocation;
      private RoleSize.Type roleSize;
      private String availabilitySetName;
      private OSVirtualHardDiskParam osVirtualHardDiskParam;
      private List<DataVirtualHardDiskParam> dataVirtualHardDiskParams = Lists.newArrayList();

      public Builder roleName(String roleName) {
         this.roleName = roleName;
         return this;
      }

      public Builder windowsConfigurationSet(WindowsConfigurationSetParams windowsConfigurationSet) {
         this.windowsConfigurationSet = windowsConfigurationSet;
         return this;
      }

      public Builder linuxConfigurationSet(LinuxConfigurationSetParams linuxConfigurationSet) {
         this.linuxConfigurationSet = linuxConfigurationSet;
         return this;
      }

      public Builder roleType(String roleType) {
         this.roleType = roleType;
         return this;
      }

      public Builder VMImageName(String vmImageName) {
         this.VMImageName = vmImageName;
         return this;
      }

      public Builder mediaLocation(URI mediaLocation) {
         this.mediaLocation = mediaLocation;
         return this;
      }

      public Builder roleSize(RoleSize.Type roleSize) {
         this.roleSize = roleSize;
         return this;
      }

      public Builder availabilitySetName(String availabilitySetName) {
         this.availabilitySetName = availabilitySetName;
         return this;
      }

      public Builder osVirtualHardDiskParam(OSVirtualHardDiskParam osVirtualHardDiskParam) {
         this.osVirtualHardDiskParam = osVirtualHardDiskParam;
         return this;
      }

      public Builder dataVirtualHardDiskParam(DataVirtualHardDiskParam dataVirtualHardDiskParam) {
         dataVirtualHardDiskParams.add(dataVirtualHardDiskParam);
         return this;
      }

      public Builder dataVirtualHardDiskParams(Collection<DataVirtualHardDiskParam> dataVirtualHardDiskParams) {
         dataVirtualHardDiskParams.addAll(dataVirtualHardDiskParams);
         return this;
      }

      public Builder fromRoleParams(RoleParam in) {
         return roleName(in.roleName()).roleType(in.roleType()).windowsConfigurationSet(in.windowsConfigurationSet())
               .linuxConfigurationSet(in.linuxConfigurationSet()).VMImageName(in.VMImageName()).roleSize(in.roleSize())
               .osVirtualHardDiskParam(in.OSVirtualHardDiskParam())
               .dataVirtualHardDiskParams(in.dataVirtualHardDiskParams()).mediaLocation(in.mediaLocation())
               .availabilitySetName(in.availabilitySetName());

      }

      public RoleParam build() {
         return RoleParam
               .create(roleName, roleType, windowsConfigurationSet, linuxConfigurationSet, VMImageName, mediaLocation,
                     roleSize, availabilitySetName, dataVirtualHardDiskParams, osVirtualHardDiskParam, Boolean.FALSE);
      }
   }

   public static RoleParam create(String roleName, String roleType,
         WindowsConfigurationSetParams windowsConfigurationSet, LinuxConfigurationSetParams linuxConfigurationSet,
         String VMImageName, URI mediaLocation, RoleSize.Type roleSize, String availabilitySetName,
         List<DataVirtualHardDiskParam> dataVirtualHardDiskParams, OSVirtualHardDiskParam OSVirtualHardDiskParam,
         Boolean provisionGuestAgent) {
      return new AutoValue_RoleParam(roleName, roleType, windowsConfigurationSet, linuxConfigurationSet, VMImageName,
            mediaLocation, roleSize, availabilitySetName, dataVirtualHardDiskParams, OSVirtualHardDiskParam,
            provisionGuestAgent);
   }
}
