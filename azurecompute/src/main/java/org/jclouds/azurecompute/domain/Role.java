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

@AutoValue
public abstract class Role {

   @AutoValue
   public abstract static class ConfigurationSet {

      @AutoValue
      public abstract static class InputEndpoint {

         @AutoValue
         public abstract static class LoadBalancerProbe {

            public abstract String path();

            public abstract int port();

            public abstract String protocol();

            LoadBalancerProbe() { // For AutoValue only!
            }

            public static LoadBalancerProbe create(String path, int port, String protocol) {
               return new AutoValue_Role_ConfigurationSet_InputEndpoint_LoadBalancerProbe(path, port, protocol);
            }
         }

         public abstract int localPort();

         public abstract String name();

         public abstract Integer port();

         public abstract String protocol();

         public abstract String vip();

         public abstract Boolean enableDirectServerReturn();

         @Nullable public abstract String loadBalancerName();

         @Nullable public abstract LoadBalancerProbe loadBalancerProbe();

         @Nullable public abstract Integer idleTimeoutInMinutes();

         InputEndpoint() { // For AutoValue only!
         }

         public static InputEndpoint create(String name, String protocol, int localPort, int port,
                                            String vip, boolean enableDirectServerReturn, String loadBalancerName,
                                            LoadBalancerProbe loadBalancerProbe, Integer idleTimeoutInMinutes) {
            return new AutoValue_Role_ConfigurationSet_InputEndpoint(localPort, name, port, protocol, vip,
                    enableDirectServerReturn, loadBalancerName, loadBalancerProbe, idleTimeoutInMinutes);
         }
      }

      @AutoValue
      public abstract static class SubnetName {

         @Nullable public abstract String name();

         SubnetName() { // For AutoValue only!
         }

         public static SubnetName create(String name) {
            return new AutoValue_Role_ConfigurationSet_SubnetName(name);
         }
      }

      @AutoValue
      public abstract static class PublicIP {

         public abstract String name();

         public abstract int idleTimeoutInMinutes();

         PublicIP() { // For AutoValue only!
         }

         public static PublicIP create(String name, int idleTimeoutInMinutes) {
            return new AutoValue_Role_ConfigurationSet_PublicIP(name, idleTimeoutInMinutes);
         }
      }

      public abstract String configurationSetType();

      public abstract List<InputEndpoint> inputEndpoints();

      @Nullable public abstract List<SubnetName> subnetNames();

      @Nullable public abstract String staticVirtualNetworkIPAddress();

      @Nullable public abstract List<PublicIP> publicIPs();

      ConfigurationSet() { // For AutoValue only!
      }

      public static ConfigurationSet create(String configurationSetType, List<InputEndpoint> inputEndpoints,
                                            List<SubnetName> subnetNames, String staticVirtualNetworkIPAddress, List<PublicIP> publicIPs) {
         return new AutoValue_Role_ConfigurationSet(configurationSetType, inputEndpoints, subnetNames,
                 staticVirtualNetworkIPAddress, publicIPs);
      }
   }

   @AutoValue
   public abstract static class ResourceExtensionReference {

      @AutoValue
      public abstract static class ResourceExtensionParameterValue {

         public abstract String key();

         public abstract String value();

         public abstract String type();

         ResourceExtensionParameterValue() { // For AutoValue only!
         }

         public static ResourceExtensionParameterValue create(String key, String value, String type) {
            return new AutoValue_Role_ResourceExtensionReference_ResourceExtensionParameterValue(key, value, type);
         }
      }

      public abstract String referenceName();

      public abstract String publisher();

      public abstract String name();

      public abstract String version();

      public abstract List<ResourceExtensionParameterValue> resourceExtensionParameterValues();

      public abstract String state();

      ResourceExtensionReference() { // For AutoValue only!
      }

      public static ResourceExtensionReference create(String referenceName, String publisher, String name, String
              version, List<ResourceExtensionParameterValue> resourceExtensionParameterValues, String state) {
         return new AutoValue_Role_ResourceExtensionReference(referenceName, publisher, name, version,
                 resourceExtensionParameterValues, state);
      }
   }

   @AutoValue
   public abstract static class DataVirtualHardDisk {

      public abstract String hostCaching();

      public abstract String diskName();

      public abstract int lun();

      public abstract int logicalDiskSizeInGB();

      public abstract String mediaLink();

      public abstract String ioType();

      DataVirtualHardDisk() { // For AutoValue only!
      }

      public static DataVirtualHardDisk create(String hostCaching, String diskName, int lun, int logicalDiskSizeInGB, String mediaLink, String ioType) {
         return new AutoValue_Role_DataVirtualHardDisk(hostCaching, diskName, lun, logicalDiskSizeInGB, mediaLink, ioType);
      }

   }

   @AutoValue
   public abstract static class OSVirtualHardDisk {

      public abstract String hostCaching();

      public abstract String diskName();

      @Nullable public abstract Integer lun();

      @Nullable public abstract Integer logicalDiskSizeInGB();

      public abstract URI mediaLink();

      public abstract String sourceImageName();

      public abstract OSImage.Type os();

      OSVirtualHardDisk() { // For AutoValue only!
      }

      public static OSVirtualHardDisk create(String hostCaching, String diskName, Integer lun, Integer logicalDiskSizeInGB, URI mediaLink, String sourceImageName, OSImage.Type os) {
         return new AutoValue_Role_OSVirtualHardDisk(hostCaching, diskName, lun, logicalDiskSizeInGB, mediaLink, sourceImageName, os);
      }
   }

   /**
    * Represents the name of the Virtual Machine.
    */
   public abstract String roleName();

   @Nullable public abstract String osVersion();

   public abstract String roleType();

   public abstract List<ConfigurationSet> configurationSets();

   @Nullable public abstract List<ResourceExtensionReference> resourceExtensionReferences();

   @Nullable public abstract List<DataVirtualHardDisk> dataVirtualHardDisks();

   public abstract OSVirtualHardDisk oSVirtualHardDisk();

   public abstract RoleSize.Type roleSize();

   public static Role create(String roleName, String osVersion, String roleType, List<ConfigurationSet> configurationSets,
                             List<ResourceExtensionReference> resourceExtensionReferences,
                             List<DataVirtualHardDisk> dataVirtualHardDisks,
                             OSVirtualHardDisk oSVirtualHardDisk, RoleSize.Type roleSize) {
      return new AutoValue_Role(roleName, osVersion, roleType, configurationSets, resourceExtensionReferences, dataVirtualHardDisks, oSVirtualHardDisk, roleSize);
   }
}
