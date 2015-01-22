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

import java.net.URI;
import java.util.List;

/**
 * Specifies information about a role size that is available in your subscription
 * @see <a href="https://msdn.microsoft.com/en-us/library/azure/dn469422.aspx" ></a>.
 */
@AutoValue
public abstract class RoleSize {

   public enum RoleSizeName {
      //Basic Tier
      Basic_A0, Basic_A1, Basic_A2, Basic_A3, Basic_A4,

      //Standard A Series
      EXTRA_SMALL, SMALL, MEDIUM, LARGE, EXTRA_LARGE, A5, A6, A7, A8, A9,

      //Standard D Series
      STANDARD_D1, STANDARD_D2, STANDARD_D3, STANDARD_D4,
      STANDARD_D11, STANDARD_D12, STANDARD_D13, STANDARD_D14,

      //Standard DS Series
      STANDARD_DS1, STANDARD_DS2, STANDARD_DS3, STANDARD_DS4,
      STANDARD_DS11, STANDARD_DS12, STANDARD_DS13, STANDARD_DS14,

      //Standard G Series
      STANDARD_G1, STANDARD_G2, STANDARD_G3, STANDARD_G4, STANDARD_G5,

      UNRECOGNIZED;
   }

   /**
    * Specifies the name of the role size
    */
   public abstract RoleSizeName name();

   /**
    * Specifies the description of the role size
    */
   public abstract String label();

   /**
    * Specifies the number of cores that are available in the role size
    */
   public abstract Integer cores();

   /**
    * Specifies the amount of memory that is available in the role size
    */
   public abstract Integer memoryInMB();

   /**
    * Indicates whether the role size supports web roles or worker roles
    * Possible values are:
    *    true
    *    false
    */
   public abstract Boolean supportedByWebWorkerRoles();

   /**
    * Indicates whether the role size supports Virtual Machines.
    * Possible values are:
    *    true
    *    false
    */
   public abstract Boolean supportedByVirtualMachines();

   /**
    * Specifies the maximum number of data disks that can be attached to the role.
    */
   public abstract Integer maxDataDiskCount();

   /**
    * Specifies the size of the resource disk for a web role or worker role
    */
   public abstract Integer webWorkerResourceDiskSizeInMb();

   /**
    * Specifies the size of the resource disk for a Virtual Machine
    */
   public abstract Integer virtualMachineResourceDiskSizeInMb();

   public static RoleSize create(RoleSizeName name, String label, Integer cores, Integer memoryInMB,
         Boolean supportedByWebWorkerRoles, Boolean supportedByVirtualMachines, Integer maxDataDiskCount,
         Integer webWorkerResourceDiskSizeInMb, Integer virtualMachineResourceDiskSizeInMb) {
      return new AutoValue_RoleSize(name, label, cores, memoryInMB, supportedByWebWorkerRoles,
            supportedByVirtualMachines, maxDataDiskCount, webWorkerResourceDiskSizeInMb, virtualMachineResourceDiskSizeInMb);
   }
}
