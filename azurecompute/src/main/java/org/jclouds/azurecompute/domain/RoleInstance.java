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

import java.util.List;

/**
 * @see <a href="http://msdn.microsoft.com/en-us/library/azure/jj157193.aspx" >api</a>
 */

@AutoValue
public abstract class RoleInstance {

   @AutoValue
   public abstract static class PublicIP {
      /**
       * The deployment in which the disk is being used.
       */
      public abstract String name();

      /**
       * The cloud service in which the disk is being used.
       */
      public abstract Integer idleTimeoutInMinutes();

      public static PublicIP create(String name, Integer idleTimeOutInMinute) {
         return new AutoValue_RoleInstance_PublicIP(name, idleTimeOutInMinute);
      }
   }

   @AutoValue
   public abstract static class InstanceEndpoint {
      /**
       * The deployment in which the disk is being used.
       */
      public abstract String name();

      /**
       * Specifies the virtual IP address of the endpoint.
       * The Vip element is only available using version 2011-06-01 or higher.
       */
      public abstract String vip();

      /**
       * Specifies the external port that is used by the endpoint.
       */
      public abstract String publicPort();

      /**
       * Specifies the internal port that is used by the endpoint.
       */
      public abstract String localPort();

      /**
       * Specifies the protocol of traffic on the endpoint
       */

      public abstract String protocol();

      public static InstanceEndpoint create(String name, String vip, String publicPort, String localPort,
            String protocol) {
         return new AutoValue_RoleInstance_InstanceEndpoint(name, vip, publicPort, localPort, protocol);
      }
   }

   public enum InstanceStatus {
      CREATING_VM, STARTING_VM, CREATING_ROLE, STARTING_ROLE, READY_ROLE, BUSY_ROLE, STOPPING_ROLE, STOPPING_VM,
      DELETING_VM, STOPPED_VM, RESTARTING_ROLE, CYCLING_ROLE, FAILED_STARTING_ROLE, FAILED_STARTING_VM, UNRESPONSIVE_ROLE,
      STOPPED_DEALLOCATED, PREPARING,
      /**
       * Unknown to Azure.
       */
      UNKNOWN,
      /**
       * Not parsable into one of the above.
       */
      UNRECOGNIZED,
   }

   public enum PowerState {
      Starting,
      Started,
      Stopping,
      Stopped,
      Unknown,
      UNRECOGNIZED,
   }

   public enum InstanceSize {
      Starting,
      Started,
      Stopping,
      Stopped,
      Unknown,
      UNRECOGNIZED,
   }

   /**
    * Specifies the name for the Virtual Machine
    */
   public abstract String roleName();

   /**
    * Specifies the name of a specific role instance, if an instance of the role is running
    */
   public abstract String instanceName();

   /**
    * The current status of this instance.
    */
   public abstract InstanceStatus instanceStatus();

   /**
    * Specifies the size of the role instance.The InstanceSize element is only available using
    * version 2011-06-01 or higher
    */
   @Nullable public abstract String instanceSize();

   /**
    * The instance state is returned as an English human-readable string that,
    * when present, provides a snapshot of the state of the virtual machine at
    * the time the operation was called.
    * <p/>
    * For example, when the instance is first being initialized a
    * "Preparing Windows for first use." could be returned.
    */
   @Nullable public abstract String instanceStateDetails();

   /**
    * Error code of the latest role or VM start
    * <p/>
    * For VMRoles the error codes are:
    * <p/>
    * WaitTimeout - The virtual machine did not communicate back to Azure
    * infrastructure within 25 minutes. Typically this indicates that the
    * virtual machine did not start or that the guest agent is not installed.
    * <p/>
    * VhdTooLarge - The VHD image selected was too large for the virtual
    * machine hosting the role.
    * <p/>
    * AzureInternalError – An internal error has occurred that has caused to
    * virtual machine to fail to start. Contact support for additional
    * assistance.
    * <p/>
    * For web and worker roles this field returns an error code that can be provided to Windows Azure support to assist
    * in resolution of errors. Typically this field will be empty.
    */

   @Nullable public abstract String instanceErrorCode();

   /**
    * Specifies the IP address of the role instance (DIP).
    * The IpAddress element is only available using version 2012-03-01 or higher
    */

   @Nullable public abstract String ipAddress();

   /**
    * Contains the list of instance endpoints for the role.
    */
   @Nullable public abstract List<InstanceEndpoint> instanceEndpoints();

   /**
    * The running state of the virtual machine.
    */
   @Nullable public abstract PowerState powerState();

   /**
    * Specifies the DNS host name of the cloud service in which the role instance is running.
    * This element is only listed for Virtual Machine deployments.
    */
   @Nullable public abstract String hostName();

   /**
    * Contains a public IP address that can be used in addition to default virtual IP address for the Virtual Machine.
    * The PublicIPs element is only available using version 2014-05-01 or higher.
    */
   @Nullable public abstract List<PublicIP> publicIPs();

   public static RoleInstance create(String roleName, String instanceName, InstanceStatus instanceStatus,
         String instanceErrorCode, String instanceSize, String instanceState, String ipAddress,
         List<InstanceEndpoint> instanceEndpoints, PowerState powerState, String hostName, List<PublicIP> publicIPs) {
      return new AutoValue_RoleInstance(roleName, instanceName, instanceStatus, instanceErrorCode,
            instanceSize, instanceState, ipAddress, instanceEndpoints, powerState, hostName, publicIPs);
   }
}
