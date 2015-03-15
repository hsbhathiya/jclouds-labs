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
package org.jclouds.azurecompute.binders;
import static com.google.common.base.Preconditions.checkNotNull;
import com.jamesmurty.utils.XMLBuilder;
import org.jclouds.azurecompute.domain.DeploymentParams;
import org.jclouds.azurecompute.domain.RoleParam;
import org.jclouds.azurecompute.domain.OSImage;
import org.jclouds.azurecompute.domain.WindowsConfigurationSetParams;
import org.jclouds.azurecompute.domain.LinuxConfigurationSetParams;
import org.jclouds.azurecompute.domain.DataVirtualHardDiskParam;
import org.jclouds.azurecompute.domain.OSVirtualHardDiskParam;
import org.jclouds.azurecompute.domain.RoleSize;
import org.jclouds.http.HttpRequest;
import org.jclouds.rest.Binder;
import java.net.URI;
import static com.google.common.base.Throwables.propagate;
public final class DeploymentParamsToXML implements Binder {
   @Override public <R extends HttpRequest> R bindToRequest(R request, Object input) {
      DeploymentParams params = DeploymentParams.class.cast(input);
      try {
         XMLBuilder builder = XMLBuilder.create("Deployment", "http://schemas.microsoft.com/windowsazure")
                 .namespace("i", "http://www.w3.org/2001/XMLSchema-instance")
                 .e("Name").t(params.name()).up()
                 .e("DeploymentSlot").t("Production").up()
                 .e("Label").t(params.name()).up();
         XMLBuilder roleListBuilder = builder.e("RoleList");
         for (RoleParam roleParam : params.roleParams()) {
            XMLBuilder roleBuilder = roleListBuilder.e("Role");
            roleBuilder
                    .e("RoleName").t(roleParam.roleName()).up()
                    .e("RoleType").t("PersistentVMRole").up();
// .e("ConfigurationSets").up();
            String vmImage = roleParam.VMImageName();
            if (vmImage == null || vmImage.isEmpty()) {
               checkNotNull(roleParam.OSVirtualHardDiskParam(), "OSVirtualHardDiskParams cannot be null");
               checkNotNull(roleParam.OSVirtualHardDiskParam().OS(), "OS cannot be null");
               XMLBuilder configSetsBuilder = roleBuilder.e("ConfigurationSets");
               if (roleParam.OSVirtualHardDiskParam().OS() == OSImage.Type.WINDOWS) {
                  WindowsConfigurationSetParams winParams = roleParam.windowsConfigurationSet();
                  XMLBuilder configBuilder = configSetsBuilder.e("ConfigurationSet")
                          .a("i:type", "WindowsProvisioningConfigurationSet"); // Windows
                  configBuilder.e("ConfigurationSetType").t("WindowsProvisioningConfiguration").up();
                  add(configBuilder, "ComputerName", winParams.computerName());
                  add(configBuilder, "AdminPassword", winParams.adminPassword());
                  add(configBuilder, "AdminUserName", winParams.adminUserName());
                  WindowsConfigurationSetParams.DomainJoin domainJoin = winParams.domainJoin();
                  if (domainJoin != null) {
                     configBuilder.e("DomainJoin");
                     XMLBuilder credentialsBuilder = configBuilder.e("Credentials");
                     for (WindowsConfigurationSetParams.Credential credential : winParams.domainJoin().credentials()) {
                        credentialsBuilder
                                .e("Domain").up()
                                .e("Username").t(credential.userName()).up()
                                .e("Password").t(credential.password()).up()
                                .up(); // Credentials
                     }
                     credentialsBuilder.up();
                     add(configBuilder, "JoinDomain", domainJoin.joinDomain());
                     configBuilder.e("JoinDomain").t(winParams.domainJoin().joinDomain()).up()
                             .up(); // Domain Join
                  }
                  configBuilder.e("StoredCertificateSettings").up()
                          .up(); // Windows ConfigurationSet
               } else if (roleParam.OSVirtualHardDiskParam().OS() == OSImage.Type.LINUX) {
                  LinuxConfigurationSetParams linuxParams = roleParam.linuxConfigurationSet();
                  XMLBuilder configBuilder = configSetsBuilder.e("ConfigurationSet")
                          .a("i:type", "LinuxProvisioningConfigurationSet"); // Linux
                  configBuilder.e("ConfigurationSetType").t("LinuxProvisioningConfiguration").up();
                  add(configBuilder, "HostName", params.name());
                  add(configBuilder, "UserName", linuxParams.userName());
                  add(configBuilder, "UserPassword", linuxParams.userPassword());
                  configBuilder.e("DisableSshPasswordAuthentication").t("false").up()
                          .e("SSH").up()
                          .up(); // Linux ConfigurationSet
// ConfigurationsSet
               } else {
                  throw new IllegalArgumentException("Unrecognized os type " + params);
               }
               XMLBuilder networkConfigBuilder = configSetsBuilder.e("ConfigurationSet"); // Network
               networkConfigBuilder.e("ConfigurationSetType").t("NetworkConfiguration").up();
               XMLBuilder inputEndpoints = networkConfigBuilder.e("InputEndpoints");
               for (DeploymentParams.ExternalEndpoint endpoint : params.externalEndpoints()) {
                  XMLBuilder inputBuilder = inputEndpoints.e("InputEndpoint");
                  inputBuilder.e("LocalPort").t(Integer.toString(endpoint.localPort())).up()
                          .e("Name").t(endpoint.name()).up()
                          .e("Port").t(Integer.toString(endpoint.port())).up()
                          .e("Protocol").t(endpoint.protocol().toLowerCase()).up()
                          .up(); //InputEndpoint
               }
               inputEndpoints.up();
               XMLBuilder subnetNames = networkConfigBuilder.e("SubnetNames");
               for (String subnetName : params.subnetNames()) {
                  subnetNames.e("SubnetName").t(subnetName).up()
                          .up(); //subnetName
               }
               networkConfigBuilder.up();
               configSetsBuilder.up(); //Configurations Sets
               roleBuilder.up(); //role
               XMLBuilder dataDisks = roleBuilder.e("DataVirtualHardDisks");
               for (DataVirtualHardDiskParam dataDisk : roleParam.dataVirtualHardDiskParams()) {
                  XMLBuilder dataDiskBuilder = dataDisks.e("DataVirtualHardDisks");
                  DataVirtualHardDiskParam dataDiskParam = roleParam.dataVirtualHardDiskParams().get(0);
                  dataDiskBuilder.e("DataVirtualHardDisk");
                  if (dataDiskParam.hostCaching() != null) {
                     dataDiskBuilder.e("HostCaching").t(dataDiskParam.hostCaching()).up();
                  }
                  dataDiskBuilder
                          .e("DiskName").t(dataDiskParam.diskName()).up()
                          .e("DiskLabel").t(dataDiskParam.diskLabel()).up();
                  add(dataDiskBuilder, "Lun", dataDiskParam.LUN());
                  add(dataDiskBuilder, "LogicalDiskSizeInGB", dataDiskParam.logicalDiskSizeInGB());
                  dataDiskBuilder.up(); //DataVirtualHardDisk
               }
               dataDisks.up();
               OSVirtualHardDiskParam osDiskParam = roleParam.OSVirtualHardDiskParam();
               XMLBuilder osDiskBuilder = roleBuilder.e("OSVirtualHardDisk");
               if (osDiskParam.hostCaching() != null) {
                  osDiskBuilder.e("HostCaching").t(osDiskParam.hostCaching()).up();
               }
               add(osDiskBuilder, "DiskName", osDiskParam.diskName());
               add(osDiskBuilder, "DiskLabel", osDiskParam.diskLabel());
               add(osDiskBuilder, "MediaLink", osDiskParam.mediaLink());
               add(osDiskBuilder, "SourceImageName", osDiskParam.sourceImageName());
               if (osDiskParam.OS() != null) {
                  osDiskBuilder.e("OS").t(osDiskParam.OS().toString()).up();
               }
               add(osDiskBuilder, "ReSizedSizeInGB", osDiskParam.resizedSizeInGB());
               if (osDiskParam.sourceImageName() == null) {
                  add(osDiskBuilder, "RemoteSourceImageLink", osDiskParam.remoteSourceImageLink());
               }
               osDiskBuilder.up(); //OSVirtualHardDisk
               roleBuilder.e("RoleSize").t(roleParam.roleSize().getText()).up()
                       .up(); //Role
            } else {
               OSVirtualHardDiskParam osDiskParam = roleParam.OSVirtualHardDiskParam();
// builder.up(); // configsets
// builder.e("ResourceExtensionReferences").up();
               roleBuilder.e("VMImageName").t(vmImage).up();
               URI mediaLocation = roleParam.mediaLocation();
               if (mediaLocation != null) {
                  roleBuilder.e("MediaLocation").t(mediaLocation.toASCIIString()).up();
               }
               RoleSize.Type roleSize = roleParam.roleSize();
               if (roleSize != null) {
                  roleBuilder.e("RoleSize").t(roleSize.getText()).up();
               }
// add(builder, "AvailabilitySetName", params.availabilitySetName);
               Boolean agent = roleParam.provisionGuestAgent();
               if (agent != null) {
                  roleBuilder.e("ProvisionGuestAgent").t(agent.toString()).up();
               }
            }
            roleBuilder.up(); // role
         }
         roleListBuilder.up(); // RoleList
         add(builder, "VirtualNetworkName", params.virtualNetworkName());
         add(builder, "ReservedIPName", params.reservedIpName());
         builder.up();
// TODO: Undeprecate this method as forcing users to wrap a String in guava's ByteSource is not great.
         return (R) request.toBuilder().payload(builder.asString()).build();
      } catch (Exception e) {
         throw propagate(e);
      }
   }
   private XMLBuilder add(XMLBuilder xmlBuilder, String entity, String text) {
      if (text != null) {
         return xmlBuilder.e(entity).t(text).up();
      } else {
         return xmlBuilder.e(entity).up();
      }
   }
   private XMLBuilder add(XMLBuilder xmlBuilder, String entity, URI text) {
      if (text != null) {
         return xmlBuilder.e(entity).t(text.toASCIIString()).up();
      } else {
         return xmlBuilder.e(entity).up();
      }
   }
   private XMLBuilder add(XMLBuilder xmlBuilder, String entity, Integer text) {
      if (text != null) {
         return xmlBuilder.e(entity).t(text.toString()).up();
      } else {
         return xmlBuilder.e(entity).up();
      }
   }
}
