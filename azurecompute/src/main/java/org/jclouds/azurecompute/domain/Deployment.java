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

import static com.google.common.collect.ImmutableList.copyOf;

import java.util.List;
import java.net.URI;

import org.jclouds.javax.annotation.Nullable;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Deployment {

   public enum Slot {

      PRODUCTION,
      STAGING,
      UNRECOGNIZED;

      public static Slot fromString(final String text) {
         if (text != null) {
            for (Slot slot : Slot.values()) {
               if (text.equalsIgnoreCase(slot.name())) {
                  return slot;
               }
            }
         }
         return UNRECOGNIZED;
      }
   }

   public enum Status {

      RUNNING,
      SUSPENDED,
      RUNNING_TRANSITIONING,
      SUSPENDED_TRANSITIONING,
      STARTING,
      SUSPENDING,
      DEPLOYING,
      DELETING,
      UNRECOGNIZED;

      public static Status fromString(final String text) {
         if (text != null) {
            for (Status status : Status.values()) {
               if (text.equalsIgnoreCase(status.name())) {
                  return status;
               }
            }
         }
         return UNRECOGNIZED;
      }
   }

   public enum InstanceStatus {

      CREATING_VM,
      STARTING_VM,
      CREATING_ROLE,
      STARTING_ROLE,
      READY_ROLE,
      BUSY_ROLE,
      STOPPING_ROLE,
      STOPPING_VM,
      DELETING_VM,
      STOPPED_VM,
      RESTARTING_ROLE,
      CYCLING_ROLE,
      FAILED_STARTING_ROLE,
      FAILED_STARTING_VM,
      UNRESPONSIVE_ROLE,
      STOPPED_DEALLOCATED,
      PREPARING,
      /**
       * Unknown to Azure.
       */
      UNKNOWN,
      /**
       * Not parsable into one of the above.
       */
      UNRECOGNIZED;

      public static InstanceStatus fromString(final String text) {
         if (text != null) {
            for (InstanceStatus status : InstanceStatus.values()) {
               // Azure isn't exactly upper-camel, as some states end in VM, not Vm.
               if (text.replace("V_M", "VM").equalsIgnoreCase(status.name())) {
                  return status;
               }
            }
         }
         return UNRECOGNIZED;
      }
   }

   @AutoValue
   public abstract static class VirtualIP {

      public abstract String address();

      public abstract Boolean isDnsProgrammed();

      public abstract String name();

      VirtualIP() { // For AutoValue only!
      }

      public static VirtualIP create(final String address, final Boolean isDnsProgrammed, final String name) {
         return new AutoValue_Deployment_VirtualIP(address, isDnsProgrammed, name);
      }
   }

   @AutoValue
   public abstract static class InstanceEndpoint {

      public abstract String name();

      public abstract String vip();

      public abstract int publicPort();

      public abstract int localPort();

      public abstract String protocol();

      InstanceEndpoint() { // For AutoValue only!
      }

      public static InstanceEndpoint create(final String name, final String vip,
              final int publicPort, final int localPort, final String protocol) {

         return new AutoValue_Deployment_InstanceEndpoint(name, vip, publicPort, localPort, protocol);
      }
   }

   @AutoValue
   public abstract static class RoleInstance {

      public abstract String roleName();

      public abstract String instanceName();

      public abstract InstanceStatus instanceStatus();

      @Nullable // null value in case of StoppedDeallocated
      public abstract int instanceUpgradeDomain();

      @Nullable // null value in case of StoppedDeallocated
      public abstract int instanceFaultDomain();

      @Nullable // null value in case of StoppedDeallocated
      public abstract RoleSize.Type instanceSize();

      @Nullable // null value in case of StoppedDeallocated
      public abstract String ipAddress();

      @Nullable
      public abstract String hostname();

      @Nullable
      public abstract List<InstanceEndpoint> instanceEndpoints();

      RoleInstance() { // For AutoValue only!
      }

      public static RoleInstance create(final String roleName, final String instanceName,
              final InstanceStatus instanceStatus, final int instanceUpgradeDomain,
              final int instanceFaultDomain, final RoleSize.Type instanceSize,
              final String ipAddress, final String hostname, final List<InstanceEndpoint> instanceEndpoints) {

         return new AutoValue_Deployment_RoleInstance(roleName, instanceName, instanceStatus, instanceUpgradeDomain,
                 instanceFaultDomain, instanceSize, ipAddress, hostname,
                 instanceEndpoints == null ? null : copyOf(instanceEndpoints));
      }
   }

   Deployment() {
   } // For AutoValue only!

   /**
    * The user-supplied name for this deployment.
    */
   public abstract String name();

   /* Specifies the URL that is used to access the cloud service.
    For example, if the service name is MyService you could access the access the service by calling: http:// MyService.cloudapp.net*/

   public abstract URI url();

   /**
    * The environment to which the cloud service is deployed.
    */
   public abstract Slot slot();

   public abstract Status status();

   /**
    * The user-supplied name of the deployment returned as a base-64 encoded string. This name can be used identify the
    * deployment for your tracking purposes.
    */
   public abstract String label();

   /**
    * The instance state is returned as an English human-readable string that, when present, provides a snapshot of the
    * state of the virtual machine at the time the operation was called.
    *
    * For example, when the instance is first being initialized a "Preparing Windows for first use." could be returned.
    */
   @Nullable
   public abstract String instanceStateDetails();

   /**
    * Error code of the latest role or VM start
    *
    * For VMRoles the error codes are:
    *
    * WaitTimeout - The virtual machine did not communicate back to Azure infrastructure within 25 minutes. Typically
    * this indicates that the virtual machine did not start or that the guest agent is not installed.
    *
    * VhdTooLarge - The VHD image selected was too large for the virtual machine hosting the role.
    *
    * AzureInternalError – An internal error has occurred that has caused to virtual machine to fail to start. Contact
    * support for additional assistance.
    *
    * For web and worker roles this field returns an error code that can be provided to Windows Azure support to assist
    * in resolution of errors. Typically this field will be empty.
    */
   @Nullable
   public abstract String instanceErrorCode();

   public abstract List<VirtualIP> virtualIPs();

   public abstract List<RoleInstance> roleInstanceList();

   @Nullable
   public abstract List<Role> roles();

   @Nullable
   public abstract String virtualNetworkName();

   public static Deployment create(final String name, final URI url, final Slot slot, final Status status, final String label,
           final String instanceStateDetails, final String instanceErrorCode,
           final List<VirtualIP> virtualIPs, final List<RoleInstance> roleInstanceList,
           final List<Role> roles, final String virtualNetworkName) {

      return new AutoValue_Deployment(name, url, slot, status, label, instanceStateDetails,
              instanceErrorCode, copyOf(virtualIPs), copyOf(roleInstanceList),
              roles == null ? null : copyOf(roles), virtualNetworkName);
   }
}
