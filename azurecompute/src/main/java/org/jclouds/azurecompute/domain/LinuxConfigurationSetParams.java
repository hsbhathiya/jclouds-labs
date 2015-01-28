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
import org.jclouds.javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A disk in the image repository.
 *
 * @see <a href="https://msdn.microsoft.com/en-us/library/azure/jj157186.aspx" >api</a>
 */
@AutoValue
public abstract class LinuxConfigurationSetParams {

   @AutoValue
   public abstract static class SSH {

      public abstract List<PublicKey> publicKeys();

      public abstract List<KeyPair> keyPairs();

      public static SSH create(List<PublicKey> publicKeys, List<KeyPair> keyPairs) {
         return new AutoValue_LinuxConfigurationSetParams_SSH(publicKeys, keyPairs);
      }
   }

   @AutoValue
   public abstract static class PublicKey {

      public abstract String fingerPrint();

      public abstract String path();

      public static PublicKey create(String fingerPrint, String path) {
         return new AutoValue_LinuxConfigurationSetParams_PublicKey(fingerPrint, path);
      }
   }

   @AutoValue
   public abstract static class KeyPair {

      public abstract String fingerPrint();

      public abstract String path();

      public static KeyPair create(String fingerPrint, String path) {
         return new AutoValue_LinuxConfigurationSetParams_KeyPair(fingerPrint, path);
      }
   }

   public abstract String configurationSetType();

   public abstract String hostName();

   public abstract String userName();

   public abstract String userPassword();

   public abstract Boolean disableSshPasswordAuthentication();

   @Nullable public abstract SSH ssh();

   public Builder toBuilder() {
      return builder().fromLinuxConfigurationSetParams(this);
   }

   public static Builder builder() {
      return new Builder();
   }

   public static final class Builder {

      private final String configurationType = "LinuxProvisioningConfiguration";
      private String hostName;
      private String userName;
      private String userPassword;
      private Boolean disableSshPasswordAuthentication = Boolean.TRUE;
      private SSH ssh;

      public Builder hostName(String hostName) {
         this.hostName = hostName;
         return this;
      }

      public Builder userName(String userName) {
         this.userName = userName;
         return this;
      }

      public Builder userPassword(String userPassword) {
         this.userPassword = userPassword;
         return this;
      }

      public Builder disableSshPasswordAuthentication(Boolean disableSshPasswordAuthentication) {
         this.disableSshPasswordAuthentication = disableSshPasswordAuthentication;
         return this;
      }

      public Builder ssh(SSH ssh) {
         this.ssh = ssh;
         return this;
      }

      public LinuxConfigurationSetParams build() {
         return LinuxConfigurationSetParams
               .create(configurationType, hostName, userName, userPassword, disableSshPasswordAuthentication,
                     ssh);
      }

      public Builder fromLinuxConfigurationSetParams(LinuxConfigurationSetParams in) {
         return hostName(in.hostName())
               .userName(in.userName())
               .userPassword(in.userPassword())
               .disableSshPasswordAuthentication(in.disableSshPasswordAuthentication());
      }
   }

   public static LinuxConfigurationSetParams create(String configurationType, String hostName,
         String userName, String userPassword, Boolean disableSshPasswordAuthentication, SSH ssh) {
      return new AutoValue_LinuxConfigurationSetParams(configurationType, hostName, userName, userPassword,
            disableSshPasswordAuthentication, ssh);
   }
}
