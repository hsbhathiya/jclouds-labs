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

import static com.google.common.collect.ImmutableList.copyOf;

@AutoValue
public abstract class VirtualMachine {

   VirtualMachine() {
   } // For AutoValue only!

   public abstract String serviceName();

   public abstract String deploymentName();

   public abstract Deployment.Slot slot();

   public abstract Deployment.Status status();

   public abstract String deploymentLabel();

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
