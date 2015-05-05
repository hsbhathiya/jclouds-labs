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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.jclouds.javax.annotation.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * To create a new deployment/role
 *
 * Warning : the OSType must be the one of the source image used to create the VM
 */
// TODO: check which can be null.
@AutoValue
public abstract class DeploymentParams {

   @AutoValue
   public abstract static class ExternalEndpoint {

      public abstract String name();

      /**
       * Either {@code tcp} or {@code udp}.
       */
      public abstract String protocol();

      public abstract int port();

      public abstract int localPort();

      public static ExternalEndpoint inboundTcpToLocalPort(final int port, final int localPort) {
         return new AutoValue_DeploymentParams_ExternalEndpoint(
                  String.format("tcp%s_%s", port, localPort), "tcp", port,localPort);
      }

      public static ExternalEndpoint inboundUdpToLocalPort(final int port, final int localPort) {
         return new AutoValue_DeploymentParams_ExternalEndpoint(
                 String.format("udp%s_%s", port, localPort), "udp", port, localPort);
      }

      ExternalEndpoint() { // For AutoValue only!
      }
   }

   /**
    * The user-supplied name for this deployment.
    */
   public abstract String name();

   // TODO move to RoleParams/NetworkConfigurationParams
   @Nullable public abstract List<ExternalEndpoint> externalEndpoints();

   public abstract List<String> subnetNames();

   public abstract List<RoleParam> roleParams();

   @Nullable public abstract String virtualNetworkName();

   @Nullable public abstract String reservedIpName();

   public Builder toBuilder() {
      return builder().fromDeploymentParams(this);
   }

   public static Builder builder() {
      return new Builder();
   }

   public static final class Builder {
      private String name;
      private List<ExternalEndpoint> externalEndpoints = Lists.newArrayList();
      private List<String> subnetNames = Lists.newArrayList();
      private List<RoleParam> roleParams = Lists.newArrayList();
      private String virtualNetworkName;
      private String reservedIpName;

      public Builder name(String name) {
         this.name = name;
         return this;
      }

      public Builder externalEndpoint(ExternalEndpoint endpoint) {
         this.externalEndpoints.add(endpoint);
         return this;
      }

      public Builder externalEndpoints(Collection<ExternalEndpoint> externalEndpoints) {
         this.externalEndpoints.addAll(externalEndpoints);
         return this;
      }

      public Builder subnetName(String subnetName) {
         this.subnetNames.add(subnetName);
         return this;
      }

      public Builder subnetNames(Collection<String> subnetNames) {
         this.subnetNames.addAll(subnetNames);
         return this;
      }

      public Builder roleParam(RoleParam roleParam) {
         this.roleParams.add(roleParam);
         return this;
      }

      public Builder roleParams(Collection<RoleParam> roleParams) {
         this.roleParams.addAll(roleParams);
         return this;
      }

      public Builder virtualNetworkName(String virtualNetworkName) {
         this.virtualNetworkName = virtualNetworkName;
         return this;
      }

      public Builder reservedIpName(String reservedIpName) {
         this.reservedIpName = reservedIpName;
         return this;
      }

      public DeploymentParams build() {
         return DeploymentParams.create(
               name,
               ImmutableList.copyOf(externalEndpoints),
               ImmutableList.copyOf(subnetNames),
               ImmutableList.copyOf(roleParams),
               virtualNetworkName, reservedIpName);
      }

      public Builder fromDeploymentParams(DeploymentParams in) {
         return name(in.name())
               .externalEndpoints(in.externalEndpoints())
               .subnetNames(in.subnetNames())
               .roleParams(in.roleParams())
               .virtualNetworkName(in.virtualNetworkName())
               .reservedIpName(in.reservedIpName());
      }
   }

   private static DeploymentParams create(String name, List<ExternalEndpoint> externalEndpoints,
         List<String> subnetNames, List<RoleParam> roleParams, String virtualNetworkName, String reservedIpName) {

      return new AutoValue_DeploymentParams(name, externalEndpoints, subnetNames, roleParams, virtualNetworkName,
            reservedIpName);
   }
}
