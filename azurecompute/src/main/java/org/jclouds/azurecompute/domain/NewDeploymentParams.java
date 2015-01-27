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

import java.net.URI;
import java.util.Collection;
import java.util.List;

/**
 * To create a new deployment/role
 * <p/>
 * Warning : the OSType must be the one of the source image used to create the VM
 */
// TODO: check which can be null.
@AutoValue
public abstract class NewDeploymentParams {
   @AutoValue
   public abstract static class ExternalEndpoint {

      public abstract String name();

      /**
       * Either {@code tcp} or {@code udp}.
       */
      public abstract String protocol();

      public abstract int port();

      public abstract int localPort();

      public static ExternalEndpoint inboundTcpToLocalPort(int port, int localPort) {
         return new AutoValue_NewDeploymentParams_ExternalEndpoint(String.format("tcp %s:%s", port, localPort), "tcp",
               port, localPort);
      }

      public static ExternalEndpoint inboundUdpToLocalPort(int port, int localPort) {
         return new AutoValue_NewDeploymentParams_ExternalEndpoint(String.format("udp %s:%s", port, localPort), "udp",
               port, localPort);
      }

      ExternalEndpoint() { // For AutoValue only!
      }
   }

   /**
    * The user-supplied name for this deployment.
    */
   public abstract String name();

   public abstract List<ExternalEndpoint> externalEndpoints();

   public abstract List<RoleParam> roleParams();

   public abstract String virtualNetworkName();

   public abstract String reservedIpName();

   public Builder toBuilder() {
      return builder().fromDeploymentParams(this);
   }

   public static Builder builder() {
      return new Builder();
   }

   public static final class Builder {
      private String name;
      private List<ExternalEndpoint> externalEndpoints = Lists.newArrayList();
      private List<RoleParam> roleParams = Lists.newArrayList();
      private String virtualNetworkName;
      private String reservedIpName;

      public Builder name(String name) {
         this.name = name;
         return this;
      }

      public Builder externalEndpoint(ExternalEndpoint endpoint) {
         externalEndpoints.add(endpoint);
         return this;
      }

      public Builder externalEndpoints(Collection<ExternalEndpoint> externalEndpoints) {
         externalEndpoints.addAll(externalEndpoints);
         return this;
      }

      public Builder roleParam(RoleParam roleParam) {
         roleParams.add(roleParam);
         return this;
      }

      public Builder roleParams(Collection<RoleParam> roleParams) {
         roleParams.addAll(roleParams);
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

      public NewDeploymentParams build() {
         return NewDeploymentParams
               .create(name, ImmutableList.copyOf(externalEndpoints), ImmutableList.copyOf(roleParams),virtualNetworkName,reservedIpName);
      }

      public Builder fromDeploymentParams(NewDeploymentParams in) {
         return name(in.name())
               .externalEndpoints(in.externalEndpoints())
               .roleParams(in.roleParams())
               .virtualNetworkName(in.virtualNetworkName())
               .reservedIpName(in.reservedIpName());
      }
   }

   private static NewDeploymentParams create(String name, List<ExternalEndpoint> externalEndpoints,
         List<RoleParam> roleParams, String virtualNetworkName,String reservedIpName ) {
      return new AutoValue_NewDeploymentParams(name, externalEndpoints, roleParams, virtualNetworkName, reservedIpName);
   }
}
