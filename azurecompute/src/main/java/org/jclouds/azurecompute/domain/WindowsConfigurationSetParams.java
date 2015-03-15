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
public abstract class WindowsConfigurationSetParams {

   @AutoValue
   public abstract static class CertificateSetting {

      public abstract String storeName();

      public abstract String thumbPrint();

      public static CertificateSetting create(String storeName, String thumbPrint) {
         return new AutoValue_WindowsConfigurationSetParams_CertificateSetting(storeName, thumbPrint);
      }
   }

   @AutoValue
   public abstract static class Credential {

      public abstract String domain();

      public abstract String userName();

      public abstract String password();

      public static Credential create(String domain, String userName, String password) {
         return new AutoValue_WindowsConfigurationSetParams_Credential(domain, userName, password);
      }
   }

   @AutoValue
   public abstract static class DomainJoin {

      @Nullable public abstract List<Credential> credentials();

      public abstract String joinDomain();

      public static DomainJoin create(List<Credential> credentials, String joinDomain) {
         return new AutoValue_WindowsConfigurationSetParams_DomainJoin(credentials, joinDomain);
      }
   }

   public abstract String configurationSetType();

   @Nullable public abstract String computerName();

   @Nullable public abstract String adminPassword();

   @Nullable public abstract DomainJoin domainJoin();

   @Nullable public abstract String adminUserName();

   @Nullable public abstract List<CertificateSetting> storedCertificateSettings();

   public Builder toBuilder() {
      return builder().fromWindowsConfigurationSetParams(this);
   }

   public static Builder builder() {
      return new Builder();
   }

   public static final class Builder {

      private final String configurationType = "WindowsProvisioningConfiguration";
      private String computerName;
      private DomainJoin domainJoin;
      private String adminPassword;
      private String adminUserName;
      private List<CertificateSetting> storedCertificateSettings = new ArrayList<CertificateSetting>();

      public Builder computerName(String computerName) {
         this.computerName = computerName;
         return this;
      }

      public Builder adminPassword(String adminPassword) {
         this.adminPassword = adminPassword;
         return this;
      }

      public Builder domainJoin(DomainJoin domainJoin) {
         this.domainJoin = domainJoin;
         return this;
      }

      public Builder adminUserName(String adminUserName) {
         this.adminUserName = adminUserName;
         return this;
      }

      public Builder storedCertificateSetting(CertificateSetting certificateSetting) {
         storedCertificateSettings.add(certificateSetting);
         return this;
      }

      public Builder storedCertificateSettings(Collection<CertificateSetting> storedCertificateSettings) {
         storedCertificateSettings.addAll(storedCertificateSettings);
         return this;
      }

      public WindowsConfigurationSetParams build() {
         return WindowsConfigurationSetParams
               .create(configurationType, computerName, adminPassword, domainJoin, adminUserName,
                     ImmutableList.copyOf(storedCertificateSettings));
      }

      public Builder fromWindowsConfigurationSetParams(WindowsConfigurationSetParams in) {
         return computerName(in.computerName())
               .adminPassword(in.adminPassword())
               .domainJoin(in.domainJoin())
               .adminUserName(in.adminUserName())
               .storedCertificateSettings(in.storedCertificateSettings());
      }
   }

   public static WindowsConfigurationSetParams create(String configurationType, String computerName,
         String adminPassword, DomainJoin domainJoin, String adminUsername,
         List<CertificateSetting> storedCertificateSettings) {
      return new AutoValue_WindowsConfigurationSetParams(configurationType, computerName, adminPassword, domainJoin,
            adminUsername, storedCertificateSettings);
   }
}
