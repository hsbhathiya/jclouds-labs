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

import org.jclouds.javax.annotation.Nullable;

import com.google.auto.value.AutoValue;

import java.net.URI;
import java.util.List;

@AutoValue
public abstract class Deployment {

   public enum Slot {
      PRODUCTION, STAGING,
      UNRECOGNIZED;
   }

   public enum Status {
      RUNNING, SUSPENDED, RUNNING_TRANSITIONING, SUSPENDED_TRANSITIONING, STARTING, SUSPENDING, DEPLOYING, DELETING,
      UNRECOGNIZED;
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

   /**
    * The user-supplied name for this deployment.
    */
   public abstract String name();

   /**
    * The environment to which the cloud service is deployed.
    */
   public abstract Slot slot();

   /**
    * Specifies a unique identifier generated internally by Azure for this deployment
    */
   public abstract String privateID();

   public abstract Status status();

   /**
    * The user-supplied name of the deployment returned as a base-64 encoded
    * string. This name can be used identify the deployment for your tracking
    * purposes.
    */
   public abstract String label();

   @Nullable public abstract URI url();

   @Nullable public abstract String configuration();

   @Nullable public abstract List<RoleInstance> roleInstances();

   @Nullable public abstract List<Role> roles();

   public static Deployment create(String name, Slot slot, String privateId, Status status, String label, URI url,
         String configuration, List<RoleInstance> roleInstances, List<Role> roles) {
      return new AutoValue_Deployment(name, slot, privateId, status, label, url, configuration, roleInstances, roles);
   }
}
