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

import static com.google.common.collect.ImmutableList.copyOf;

@AutoValue
public abstract class VirtualMachine {

   VirtualMachine() {
   } // For AutoValue only!

  @Nullable public abstract String serviceName();

   public abstract String deploymentName();

   public abstract Deployment.Slot slot();

   public abstract Deployment.Status deploymentStatus();

   @Nullable public abstract String deploymentLabel();

   public abstract String roleName();

   public abstract String instanceName();

   public abstract RoleSize.Type instanceSize();

   public abstract Deployment.InstanceStatus instanceStatus();

   @Nullable public abstract String instanceStateDetails();

   @Nullable public abstract String instanceErrorCode();

   public abstract List<Deployment.VirtualIP> virtualIPs();

   @Nullable public abstract List<Deployment.InstanceEndpoint> instanceEndpoints();

   @Nullable public abstract Role role();

   @Nullable public abstract String virtualNetworkName();

   public Builder toBuilder() {
      return builder().fromVirtualMachine(this);
   }

   public static Builder builder() {
      return new Builder();
   }

   public static final class Builder {
      private String serviceName;
      private String deploymentName;
      private Deployment.Slot slot;
      private Deployment.Status deploymentStatus;
      private String deploymentLabel;
      private String roleName;
      private String instanceName;
      private RoleSize.Type instanceSize;
      private Deployment.InstanceStatus instanceStatus;
      private String instanceStateDetails;
      private String instanceErrorCode;
      private List<Deployment.VirtualIP> virtualIPs = Lists.newArrayList();
      private List<Deployment.InstanceEndpoint> instanceEndpoints = Lists.newArrayList();
      private Role role;
      private String virtualNetworkName;

      public Builder serviceName(String serviceName) {
         this.serviceName = serviceName;
         return this;
      }

      public Builder deploymentName(String deploymentName) {
         this.deploymentName = deploymentName;
         return this;
      }

      public Builder slot(Deployment.Slot slot) {
         this.slot = slot;
         return this;
      }

      public Builder deploymentStatus(Deployment.Status deploymentStatus) {
         this.deploymentStatus = deploymentStatus;
         return this;
      }

      public Builder deploymentLabel(String deploymentLabel) {
         this.deploymentLabel = deploymentLabel;
         return this;
      }

      public Builder instanceSize(RoleSize.Type instanceSize) {
         this.instanceSize = instanceSize;
         return this;
      }

      public Builder roleName(String roleName) {
         this.roleName = roleName;
         return this;
      }

      public Builder instanceName(String instanceName) {
         this.instanceName = roleName;
         return this;
      }

      public Builder instanceErrorCode(String instanceErrorCode) {
         this.instanceErrorCode = instanceErrorCode;
         return this;
      }

      public Builder instanceStateDetails(String instanceStateDetails) {
         this.instanceStateDetails = instanceStateDetails;
         return this;
      }

      public Builder instanceStatus(Deployment.InstanceStatus instanceStatus) {
         this.instanceStatus = instanceStatus;
         return this;
      }

      public Builder instanceEndpoint(Deployment.InstanceEndpoint instanceEndpoint) {
         instanceEndpoints.add(instanceEndpoint);
         return this;
      }

      public Builder instanceEndpoints(Collection<Deployment.InstanceEndpoint> instanceEndpoints) {
         this.instanceEndpoints.addAll(instanceEndpoints);
         return this;
      }

      public Builder virtualIps(Deployment.VirtualIP virtualIP) {
         virtualIPs.add(virtualIP);
         return this;
      }

      public Builder virtualIps(Collection<Deployment.VirtualIP> virtualIPs) {
         virtualIPs.addAll(virtualIPs);
         return this;
      }

      public Builder role(Role role) {
         this.role = role;
         return this;
      }

      public Builder virtualNetworkName(String virtualNetworkName) {
         this.virtualNetworkName = virtualNetworkName;
         return this;
      }

      public Builder fromVirtualMachine(VirtualMachine in) {
         return serviceName(in.serviceName())
               .deploymentName(in.deploymentName())
               .slot(in.slot())
               .deploymentStatus(in.deploymentStatus())
               .deploymentLabel(in.deploymentLabel())
               .instanceSize(in.instanceSize())
               .roleName(in.roleName())
               .instanceName(in.instanceName())
               .instanceStatus(in.instanceStatus())
               .instanceEndpoints(in.instanceEndpoints())
               .virtualIps(in.virtualIPs())
               .instanceErrorCode(in.instanceErrorCode())
               .instanceStateDetails(in.instanceStateDetails())
               .role(in.role())
               .virtualNetworkName(in.virtualNetworkName());
      }

      public VirtualMachine build() {
         return VirtualMachine
               .create(serviceName, deploymentName, slot, deploymentStatus, deploymentLabel, roleName, instanceName,
                     instanceSize, instanceStatus, instanceErrorCode, instanceStateDetails, virtualIPs,
                     instanceEndpoints, role, virtualNetworkName);
      }
   }

   public static VirtualMachine create(
         String serviceName,
         String deploymentName,
         Deployment.Slot slot,
         Deployment.Status status,
         String deploymentLabel,
         String roleName,
         String instanceName,
         RoleSize.Type instanceSize,
         Deployment.InstanceStatus instanceStatus,
         String instanceStateDetails,
         String instanceErrorCode,
         List<Deployment.VirtualIP> virtualIPs,
         List<Deployment.InstanceEndpoint> instanceEndpoints,
         Role role,
         String virtualNetworkName) {
      return new AutoValue_VirtualMachine(serviceName, deploymentName, slot, status, deploymentLabel, roleName
            , instanceName, instanceSize, instanceStatus, instanceStateDetails, instanceErrorCode, virtualIPs,
            instanceEndpoints, role, virtualNetworkName);
   }
}
