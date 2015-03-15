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
package org.jclouds.azurecompute.features;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.jclouds.azurecompute.domain.Deployment.InstanceStatus.READY_ROLE;
import static org.jclouds.util.Predicates2.retry;
import static org.testng.Assert.assertTrue;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;

import org.jclouds.azurecompute.compute.AzureComputeServiceAdapter;
import org.jclouds.azurecompute.domain.LinuxConfigurationSetParams;
import org.jclouds.azurecompute.domain.OSVirtualHardDiskParam;
import org.jclouds.azurecompute.domain.RoleParam;
import org.jclouds.azurecompute.domain.RoleSize;
import org.jclouds.azurecompute.domain.CloudService;
import org.jclouds.azurecompute.domain.Role;
import org.jclouds.azurecompute.domain.Deployment;
import org.jclouds.azurecompute.domain.Deployment.RoleInstance;
import org.jclouds.azurecompute.domain.DeploymentParams;
import org.jclouds.azurecompute.internal.BaseAzureComputeApiLiveTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.jclouds.azurecompute.util.ConflictManagementPredicate;

@Test(groups = "live", testName = "VirtualMachineApiLiveTest", singleThreaded = true)
public class VirtualMachineApiLiveTest extends BaseAzureComputeApiLiveTest {

   private static final String CLOUD_SERVICE = String.format("%s%d-%s",
         System.getProperty("user.name"), RAND, VirtualMachineApiLiveTest.class.getSimpleName()).toLowerCase();
   private static final String DEPLOYMENT = String.format("%s%d-%s",
         System.getProperty("user.name"), RAND, VirtualMachineApiLiveTest.class.getSimpleName()).toLowerCase();

   private String roleName;
   private Predicate<String> roleInstanceReady;
   private Predicate<String> roleInstanceStopped;
   private CloudService cloudService;

   @BeforeClass(groups = { "integration", "live" })
   public void setup() {
      super.setup();
      cloudService = getOrCreateCloudService(CLOUD_SERVICE, LOCATION);

      roleInstanceReady = retry(new Predicate<String>() {
         @Override
         public boolean apply(String input) {
            RoleInstance roleInstance = getFirstRoleInstanceInDeployment(input);
            return roleInstance != null && roleInstance.instanceStatus() == READY_ROLE;
         }
      }, 600, 5, 5, SECONDS);

      roleInstanceStopped = retry(new Predicate<String>() {
         @Override
         public boolean apply(String input) {
            RoleInstance roleInstance = getFirstRoleInstanceInDeployment(input);
            return roleInstance != null && roleInstance.instanceStatus() == Deployment.InstanceStatus.STOPPED_VM;
         }
      }, 600, 5, 5, SECONDS);

          LinuxConfigurationSetParams linuxConfig = LinuxConfigurationSetParams.builder().hostName("bhash90.jclouds.azure")
            .userName("test")
            .userPassword("supersecurePassword1!").build();

      roleName = DEPLOYMENT;
      String diskName = roleName + "osdisk" + (int) (Math.random() * 100);
      OSVirtualHardDiskParam osParam = OSVirtualHardDiskParam.builder()
            .sourceImageName(IMAGE_NAME)
            .mediaLink(AzureComputeServiceAdapter.createMediaLink(storageService.serviceName(), DEPLOYMENT))
            .os(org.jclouds.azurecompute.domain.OSImage.Type.LINUX)
            .diskName(diskName)
            .diskLabel(diskName)
            .hostCaching("ReadWrite")
            .build();

      RoleParam roleParam = RoleParam.builder()
            .roleName(roleName)
            .roleSize(RoleSize.Type.BASIC_A2)
            .osVirtualHardDiskParam(osParam)
            .linuxConfigurationSet(linuxConfig)
            .build();

      DeploymentParams params = DeploymentParams.builder()
            .name(DEPLOYMENT)
            .roleParam(roleParam)
            .virtualNetworkName(virtualNetworkSite.name())
            .externalEndpoint(DeploymentParams.ExternalEndpoint.inboundTcpToLocalPort(22, 22))
            .build();

      getOrCreateDeployment(cloudService.name(), params);
      roleInstanceReady.apply(DEPLOYMENT);

   }

   public void testUpdate() {
      final Role role = api().getRole(roleName);
      assertTrue(new ConflictManagementPredicate(api) {
         @Override
         protected String operation() {
            return api().updateRole(roleName,
                    Role.create(
                            role.roleName(),
                            role.roleType(),
                            role.vmImage(),
                            role.mediaLocation(),
                            role.configurationSets(),
                            role.resourceExtensionReferences(),
                            role.availabilitySetName(),
                            role.dataVirtualHardDisks(),
                            role.osVirtualHardDisk(),
                            role.roleSize(),
                            role.provisionGuestAgent(),
                            role.defaultWinRmCertificateThumbprint()));
         }
      }.apply(role.roleName()));
   }

   @Test(dependsOnMethods = "testUpdate")
   public void testShutdown() {
      assertTrue(new ConflictManagementPredicate(api) {

         @Override
         protected String operation() {
            return api().shutdown(roleName);
         }
      }.apply(roleName));
      RoleInstance roleInstance = getFirstRoleInstanceInDeployment(DEPLOYMENT);
      assertTrue(roleInstanceStopped.apply(DEPLOYMENT), roleInstance.toString());
      Logger.getAnonymousLogger().log(Level.INFO, "roleInstance stopped: {0}", roleInstance);
   }

   @Test(dependsOnMethods = "testShutdown")
   public void testStart() {
      assertTrue(new ConflictManagementPredicate(api) {

         @Override
         protected String operation() {
            return api().start(roleName);
         }
      }.apply(roleName));

      RoleInstance roleInstance = getFirstRoleInstanceInDeployment(DEPLOYMENT);
      assertTrue(roleInstanceReady.apply(DEPLOYMENT), roleInstance.toString());
      Logger.getAnonymousLogger().log(Level.INFO, "roleInstance started: {0}", roleInstance);
   }

   @Test(dependsOnMethods = "testStart")
   public void testRestart() {
      assertTrue(new ConflictManagementPredicate(api) {

         @Override
         protected String operation() {
            return api().restart(roleName);
         }
      }.apply(roleName));

      final RoleInstance roleInstance = getFirstRoleInstanceInDeployment(DEPLOYMENT);
      assertTrue(roleInstanceReady.apply(DEPLOYMENT), roleInstance.toString());
      Logger.getAnonymousLogger().log(Level.INFO, "roleInstance restarted: {0}", roleInstance);
   }

   @AfterClass(alwaysRun = true)
   public void cleanup() {
      if (cloudService != null && api.getDeploymentApiForService(cloudService.name()).get(DEPLOYMENT) != null) {
         final List<Role> roles = api.getDeploymentApiForService(cloudService.name()).get(DEPLOYMENT).roleList();

         assertTrue(new ConflictManagementPredicate(api) {

            @Override
            protected String operation() {
               return api.getDeploymentApiForService(cloudService.name()).delete(DEPLOYMENT);
            }
         }.apply(DEPLOYMENT));

         assertTrue(new ConflictManagementPredicate(api) {

            @Override
            protected String operation() {
               return api.getCloudServiceApi().delete(cloudService.name());
            }
         }.apply(cloudService.name()));

         for (Role r : roles) {
            final Role.OSVirtualHardDisk disk = r.osVirtualHardDisk();
            if (disk != null) {
               assertTrue(new ConflictManagementPredicate(api) {

                  @Override
                  protected String operation() {
                     return api.getDiskApi().delete(disk.diskName());
                  }
               }.apply(disk.diskName()));
            }
         }
      }
   }

   private VirtualMachineApi api() {
      return api.getVirtualMachineApiForDeploymentInService(DEPLOYMENT, cloudService.name());
   }

   private RoleInstance getFirstRoleInstanceInDeployment(String deployment) {
      return Iterables.getOnlyElement(api.getDeploymentApiForService(cloudService.name()).get(deployment).
            roleInstanceList());
   }
}
