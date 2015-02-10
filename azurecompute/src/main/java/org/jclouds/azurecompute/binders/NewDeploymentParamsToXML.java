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

import com.jamesmurty.utils.XMLBuilder;
import org.jclouds.azurecompute.domain.*;
import org.jclouds.http.HttpRequest;
import org.jclouds.rest.Binder;

import java.net.URI;

import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static com.google.common.base.Throwables.propagate;
import static org.jclouds.azurecompute.domain.OSImage.Type.LINUX;

public final class NewDeploymentParamsToXML implements Binder {

   @Override public <R extends HttpRequest> R bindToRequest(R request, Object input) {
      NewDeploymentParams params = NewDeploymentParams.class.cast(input);
      try {
         XMLBuilder builder = XMLBuilder.create("Deployment", "http://schemas.microsoft.com/windowsazure")
               .e("Name").t(params.name()).up()
               .e("DeploymentSlot").t("Production").up()
               .e("Label").t(params.name()).up();

         XMLBuilder roleListBuilder = builder.e("RoleList");
         for (RoleParam roleParam : params.roleParams()) {
            XMLBuilder roleBuilder = roleListBuilder.e("Role");
            roleBuilder
                  .e("RoleName").t(roleParam.roleName()).up()
                  .e("RoleType").t("PersistentVMRole").up();
            //.e("ConfigurationSets").up();
            String vmImage = roleParam.VMImageName();
            if (vmImage == null || vmImage.isEmpty()) {
               if (roleParam.OSVirtualHardDiskParam().OS() == OSImage.Type.WINDOWS) {
                  WindowsConfigurationSetParams winParams = roleParam.windowsConfigurationSet();
                  XMLBuilder configBuilder = roleBuilder.e("ConfigurationSet"); // Windows
                  configBuilder.e("ConfigurationSetType").t("WindowsProvisioningConfiguration").up()
                        .e("ComputerName").t(winParams.computerName()).up()
                        .e("AdminPassword").t(winParams.adminPassword()).up()
                        .e("ResetPasswordOnFirstLogon").t("false").up()
                        .e("EnableAutomaticUpdate").t("false").up()
                        .e("DomainJoin");

                  XMLBuilder credentials = configBuilder.e("Credentials");
                  for (WindowsConfigurationSetParams.Credential credential : winParams.domainJoin().credentials()) {
                     XMLBuilder credentailBuilder = configBuilder
                           .e("Domain").up()
                           .e("Username").t(credential.userName()).up()
                           .e("Password").t(credential.password()).up()
                           .up(); // Credentials
                  }
                  credentials.up();
                  configBuilder.e("JoinDomain").t(winParams.domainJoin().joinDomain()).up()
                        .up(); // Domain Join

                  configBuilder.e("StoredCertificateSettings").up()
                        .up(); // Windows ConfigurationSet
               } else if (roleParam.OSVirtualHardDiskParam().OS() == OSImage.Type.LINUX) {
                  LinuxConfigurationSetParams linuxParams = roleParam.linuxConfigurationSet();
                  XMLBuilder configBuilder = builder.e("ConfigurationSet"); // Linux
                  configBuilder.e("ConfigurationSetType").t("LinuxProvisioningConfiguration").up()
                        .e("HostName").t(linuxParams.hostName()).up()
                        .e("UserName").t(linuxParams.userName()).up()
                        .e("UserPassword").t(linuxParams.userPassword()).up()
                        .e("DisableSshPasswordAuthentication").t("false").up()
                        .e("SSH").up()
                        .up(); // Linux ConfigurationSet
               } else {
                  throw new IllegalArgumentException("Unrecognized os type " + params);
               }

               XMLBuilder configBuilder = roleBuilder.e("ConfigurationSet"); // Network
               configBuilder.e("ConfigurationSetType").t("NetworkConfiguration").up();

               XMLBuilder inputEndpoints = configBuilder.e("InputEndpoints");
               for (NewDeploymentParams.ExternalEndpoint endpoint : params.externalEndpoints()) {
                  XMLBuilder inputBuilder = inputEndpoints.e("InputEndpoint");
                  inputBuilder.e("LocalPort").t(Integer.toString(endpoint.localPort())).up()
                        .e("Name").t(endpoint.name()).up()
                        .e("Port").t(Integer.toString(endpoint.port())).up()
                        .e("Protocol").t(endpoint.protocol().toLowerCase()).up()
                        .up(); //InputEndpoint
               }

               inputEndpoints.up();
               configBuilder.e("SubnetNames").up()
                     .up();
               roleBuilder.up(); //ConfigurationSets
               XMLBuilder dataDisks = roleBuilder.e("DataVirtualHardDisks");
               for (DataVirtualHardDiskParam dataDisk : roleParam.dataVirtualHardDiskParams()) {
                  XMLBuilder dataDiskBuilder = dataDisks.e("DataVirtualHardDisks");
                  DataVirtualHardDiskParam dataDiskParam = roleParam.dataVirtualHardDiskParams().get(0);
                  dataDiskBuilder.e("DataVirtualHardDisk")
                        .e("HostCaching").t(dataDiskParam.hostCaching()).up()
                        .e("DiskName").t(dataDiskParam.diskName()).up()
                        .e("DiskLabel").t(dataDiskParam.diskLabel()).up()
                        .e("LUN").t(dataDiskParam.LUN().toString()).up()
                        .e("LogicalDiskSizeInGB").t(dataDiskParam.logicalDiskSizeInGB().toString()).up()
                        .e("MediaLink").t(dataDiskParam.mediaLink().getPath()).up()
                        .up(); //DataVirtualHardDisk
               }
               dataDisks.up();

               OSVirtualHardDiskParam osDiskParam = roleParam.OSVirtualHardDiskParam();
               XMLBuilder osDiskBuilder = roleBuilder.e("OSVirtualHardDisk");
               osDiskBuilder
                     .e("OSVirtualHardDisk")
                     .e("HostCaching").t(osDiskParam.hostCaching()).up()
                     .e("DiskName").t(osDiskParam.diskName()).up()
                     .e("DiskLabel").t(osDiskParam.diskLabel()).up()
                     .e("MediaLink").t(osDiskParam.mediaLink().getPath()).up()
                     .e("SourceImageName").t(osDiskParam.sourceImageName().getPath()).up()
                     .e("OS").t(osDiskParam.OS() == LINUX ? "Linux" : "Windows").up();
               roleBuilder.up() //OSVirtualHardDisk
                     .e("RoleSize").t(UPPER_UNDERSCORE.to(UPPER_CAMEL, roleParam.roleSize().getText())).up()
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
                  builder.e("RoleSize").t(roleSize.getText()).up();
               }
             /*  builder.e("AvailabilitySetName").up();
               builder.e("DataVirtualHardDisks").up();
               builder.e("OSVirtualHardDisk")
                     .e("HostCaching").t(osDiskParam.hostCaching()).up()
                     .e("DiskName").t(osDiskParam.diskName()).up()
                     .e("DiskLabel").t(osDiskParam.diskLabel()).up()
                     .e("MediaLink").t(mediaLocation.toASCIIString()).up()
                //     .e("SourceImageName").t(osDiskParam.sourceImageName().getPath()).up()
                     .e("OS").t(osDiskParam.OS() == LINUX ? "Linux" : "Windows").up().up();

               builder.e("ProvisionGuestAgent").t("False").up();*/
            }
            roleBuilder.up(); // role
         }
         roleListBuilder.up(); // RoleList
         if (params.virtualNetworkName() != null) {
            builder.e("VirtualNetworkName").t(params.virtualNetworkName()).up();
         }
         builder.up();
         // TODO: Undeprecate this method as forcing users to wrap a String in guava's ByteSource is not great.
         return (R) request.toBuilder().payload(builder.asString()).build();
      } catch (Exception e) {
         throw propagate(e);
      }
   }
}
