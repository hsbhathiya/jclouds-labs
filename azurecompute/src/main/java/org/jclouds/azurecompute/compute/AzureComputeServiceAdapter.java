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
package org.jclouds.azurecompute.compute;

import static com.google.common.base.Predicates.notNull;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.jclouds.azurecompute.domain.Deployment.InstanceStatus.READY_ROLE;
import static org.jclouds.util.Predicates2.retry;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.collect.*;
import org.jclouds.azurecompute.AzureComputeApi;
import org.jclouds.azurecompute.compute.config.AzureComputeServiceContextModule.AzureComputeConstants;
import org.jclouds.azurecompute.config.AzureComputeProperties;
import org.jclouds.azurecompute.domain.*;
import org.jclouds.azurecompute.domain.Deployment.RoleInstance;
import org.jclouds.azurecompute.domain.DeploymentParams.ExternalEndpoint;
import org.jclouds.azurecompute.functions.DeploymentToVirtualMachine;
import org.jclouds.azurecompute.options.AzureComputeTemplateOptions;
import org.jclouds.compute.ComputeServiceAdapter;
import org.jclouds.compute.domain.OsFamily;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.reference.ComputeServiceConstants;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.logging.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;

/**
 * defines the connection between the {@link org.jclouds.azurecompute.AzureComputeApi} implementation and the
 * jclouds {@link org.jclouds.compute.ComputeService}
 */
@Singleton
public class AzureComputeServiceAdapter implements ComputeServiceAdapter<VirtualMachine, RoleSize, OSImage, Location> {

   private static final String DEFAULT_LOGIN_USER = "jclouds";
   private static final String DEFAULT_LOGIN_PASSWORD = "Azur3Compute!";

   @Resource
   @Named(ComputeServiceConstants.COMPUTE_LOGGER)
   protected Logger logger = Logger.NULL;

   private final AzureComputeApi api;
   private final Predicate<String> operationSucceededPredicate;
   private final AzureComputeConstants azureComputeConstants;
   private DeploymentToVirtualMachine deploymentToVirtualMachine;

   @Inject AzureComputeServiceAdapter(final AzureComputeApi api, Predicate<String> operationSucceededPredicate,
         AzureComputeConstants azureComputeConstants, DeploymentToVirtualMachine deploymentToVirtualMachine) {
      this.api = api;
      this.operationSucceededPredicate = operationSucceededPredicate;
      this.azureComputeConstants = azureComputeConstants;
      this.deploymentToVirtualMachine = deploymentToVirtualMachine;
   }

   @Override
   public NodeAndInitialCredentials<VirtualMachine> createNodeWithGroupEncodedIntoName(
         final String group, final String name, Template template) {

      // azure-specific options
      AzureComputeTemplateOptions templateOptions = template.getOptions().as(AzureComputeTemplateOptions.class);

      final String loginUser =
            templateOptions.getLoginUser() != null ? templateOptions.getLoginUser() : DEFAULT_LOGIN_USER;
      final String loginPassword =
            templateOptions.getLoginPassword() != null ? templateOptions.getLoginPassword() : DEFAULT_LOGIN_PASSWORD;
      final String location = template.getLocation().getId();
      final int[] inboundPorts = template.getOptions().getInboundPorts();

      final String storageAccountName = templateOptions.getStorageAccountName().get().toLowerCase();
     // final String virtualNetworkName = templateOptions.getVirtualNetworkName().get();
     // final String subnetName = templateOptions.getSubnetName().get();

      StorageServiceParams serviceParams = StorageServiceParams.builder()
            .name(storageAccountName)
            .label(storageAccountName)
            .accountType(StorageServiceParams.Type.Standard_GRS)
            .location(location)
            .build();

      if(api.getStorageAccountApi().checkAvailable(storageAccountName).result()) {
         String createStorageServiceRequestId = api.getStorageAccountApi().create(serviceParams);
         if (!operationSucceededPredicate.apply(createStorageServiceRequestId)) {
            final String message = generateIllegalStateExceptionMessage(createStorageServiceRequestId,
                  azureComputeConstants.operationTimeout());
            logger.warn(message);
            throw new IllegalStateException(message);
         }
         logger.info("Storage Service (%s) created with operation id: %s", name, createStorageServiceRequestId);
      }
      logger.debug("Creating a cloud service with name '%s', label '%s' in location '%s'", name, name, location);
      String createCloudServiceRequestId = api.getCloudServiceApi().createWithLabelInLocation(name, name, location);
      if (!operationSucceededPredicate.apply(createCloudServiceRequestId)) {
         final String message = generateIllegalStateExceptionMessage(createCloudServiceRequestId,
               azureComputeConstants.operationTimeout());
         logger.warn(message);
         throw new IllegalStateException(message);
      }
      logger.info("Cloud Service (%s) created with operation id: %s", name, createCloudServiceRequestId);

      final OSImage.Type os = template.getImage().getOperatingSystem().getFamily().equals(OsFamily.WINDOWS) ?
            OSImage.Type.WINDOWS : OSImage.Type.LINUX;
      Set<ExternalEndpoint> externalEndpoints = Sets.newHashSet();
      for (int inboundPort : inboundPorts) {
         externalEndpoints.add(ExternalEndpoint.inboundTcpToLocalPort(inboundPort, inboundPort));
      }
      List<LinuxConfigurationSetParams.KeyPair> keyPairs  = Lists.newArrayList();

      LinuxConfigurationSetParams linuxConfig = LinuxConfigurationSetParams.builder().hostName("bhash90.jclouds.azure")
            .userName(loginUser)
            .userPassword(loginPassword)
            .build();
      WindowsConfigurationSetParams winConfig = WindowsConfigurationSetParams.builder()
            .adminUserName(loginUser)
            .adminPassword(loginPassword)
            .build();

      String roleName = name;
      String diskName = roleName + "-osdisk";
      OSVirtualHardDiskParam osParam = OSVirtualHardDiskParam.builder()
            .sourceImageName(template.getImage().getId())
            .mediaLink(createMediaLink(storageAccountName, name))
            .os(org.jclouds.azurecompute.domain.OSImage.Type.LINUX)
            .diskName(diskName)
            .diskLabel(diskName)
            .hostCaching("ReadWrite")
            .build();
      RoleParam roleParam;
      if (os == OSImage.Type.LINUX) {
         roleParam = RoleParam.builder()
               .roleName(roleName)
               .roleSize(RoleSize.Type.fromString(template.getHardware().getId()))
               .osVirtualHardDiskParam(osParam)
               .linuxConfigurationSet(linuxConfig)
               .build();
      } else {
         roleParam = RoleParam.builder()
               .roleName(roleName)
               .roleSize(RoleSize.Type.fromString(template.getHardware().getId()))
               .osVirtualHardDiskParam(osParam)
               .windowsConfigurationSet(winConfig)
               .build();
      }
      DeploymentParams params = DeploymentParams.builder()
            .name(name)
            .roleParam(roleParam)
            .externalEndpoints(externalEndpoints)
            .build();
      logger.debug("Creating a deployment with params '%s' ...", params);
      String createDeploymentRequestId = api.getDeploymentApiForService(name).create(params);
      if (!operationSucceededPredicate.apply(createDeploymentRequestId)) {
         final String message = generateIllegalStateExceptionMessage(createCloudServiceRequestId,
               azureComputeConstants.operationTimeout());
         logger.warn(message);
         logger.debug("Deleting cloud service (%s) ...", name);
         deleteCloudService(name);
         logger.debug("Cloud service (%s) deleted.", name);
      }
      logger.info("Deployment created with operation id: %s", createDeploymentRequestId);

      if (!retry(new Predicate<String>() {
         public boolean apply(String name) {
            return FluentIterable.from(api.getDeploymentApiForService(name).get(name).roleInstanceList())
                  .allMatch(new Predicate<RoleInstance>() {
                     @Override
                     public boolean apply(RoleInstance input) {
                        return input != null && input.instanceStatus() == READY_ROLE;
                     }
                  });
         }
      }, 30 * 60, 1, SECONDS).apply(name)) {
         logger.warn("Instances %s of %s has not reached the status %s within %sms so it will be destroyed.",
               Iterables.toString(api.getDeploymentApiForService(name).get(name).roleInstanceList()), name,
               READY_ROLE, azureComputeConstants.operationTimeout());
         api.getDeploymentApiForService(group).delete(name);
         api.getCloudServiceApi().delete(name);
         throw new IllegalStateException(format("Deployment %s is being destroyed as its instanceStatus didn't reach " +
               "status %s after %ss. Please, try by increasing `jclouds.azure.operation-timeout` and " +
               " try again", name, READY_ROLE, 30 * 60));
      }

      Deployment deployment = api.getDeploymentApiForService(name).get(name);
      VirtualMachine virtualMachine = deploymentToVirtualMachine.apply(deployment).get(0);

      return new NodeAndInitialCredentials(virtualMachine, virtualMachine.instanceName(),
            LoginCredentials.builder().user(loginUser).password(loginPassword).build());
   }

   public static String generateIllegalStateExceptionMessage(String operationId, long timeout) {
      final String warnMessage = format("%s has not been completed within %sms.", operationId, timeout);
      return format("%s. Please, try by increasing `%s` and try again", warnMessage,
            AzureComputeProperties.OPERATION_TIMEOUT);
   }

   @Override
   public Iterable<RoleSize> listHardwareProfiles() {
      return api.getSubscriptionApi().listRoleSizes();
   }

   @Override
   public Iterable<OSImage> listImages() {
      List<OSImage> osImages = Lists.newArrayList();
      for (OSImage osImage : api.getOSImageApi().list()) {
         Iterable<String> locations = Splitter.on(";").split(osImage.location());
         for (String location : locations) {
            osImages.add(OSImage.create(
                  osImage.name(),
                  location,
                  osImage.affinityGroup(),
                  osImage.label(),
                  osImage.description(),
                  osImage.category(),
                  osImage.os(),
                  osImage.publisherName(),
                  osImage.mediaLink(),
                  osImage.logicalSizeInGB(),
                  osImage.eula()
            ));
         }
      }
      return osImages;
   }

   @Override
   public OSImage getImage(final String id) {
      return Iterables.find(api.getOSImageApi().list(), new Predicate<OSImage>() {
         @Override
         public boolean apply(OSImage input) {
            return input.name().equals(id);
         }
      });
   }

   @Override
   public Iterable<Location> listLocations() {
      return api.getLocationApi().list();
   }

   @Override
   public VirtualMachine getNode(final String id) {
      return virtualMachineMap().get(id);
   }

   @Override
   public void destroyNode(final String id) {
      VirtualMachine node = getNode(id);
      api.getVirtualMachineApiForDeploymentInService(node.deploymentName(), node.serviceName())
            .deleteRole(node.instanceName());
   }

   @Override
   public void rebootNode(final String id) {
      VirtualMachine node = getNode(id);
      api.getVirtualMachineApiForDeploymentInService(node.deploymentName(), node.serviceName())
            .restart(node.roleName());
   }

   @Override
   public void resumeNode(String id) {
      VirtualMachine node = getNode(id);
      api.getVirtualMachineApiForDeploymentInService(node.deploymentName(), node.serviceName())
            .start(node.roleName());
   }

   @Override
   public void suspendNode(String id) {
      VirtualMachine node = getNode(id);
      api.getVirtualMachineApiForDeploymentInService(node.deploymentName(), node.serviceName())
            .shutdown(node.roleName());
   }

   @Override
   public Iterable<VirtualMachine> listNodes() {
      return virtualMachineMap().values();
   }

   @Override
   public Iterable<VirtualMachine> listNodesByIds(final Iterable<String> ids) {
      Map<String, VirtualMachine> vmMap = virtualMachineMap();

      Set<VirtualMachine> virtualMachines = Sets.newHashSet();
      Iterator<String> it = ids.iterator();
      while (it.hasNext()) {
         virtualMachines.add(vmMap.get(it.next()));
      }
      return virtualMachines;
   }

   @VisibleForTesting
   public static URI createMediaLink(String storageServiceName, String diskName) {
      return URI
            .create(String.format("https://%s.blob.core.windows.net/vhds/disk-%s.vhd", storageServiceName, diskName));
   }

   private void deleteCloudService(String name) {
      String deleteCloudServiceId = api.getCloudServiceApi().delete(name);
      if (!operationSucceededPredicate.apply(deleteCloudServiceId)) {
         final String deleteMessage = generateIllegalStateExceptionMessage(deleteCloudServiceId,
               azureComputeConstants.operationTimeout());
         logger.warn(deleteMessage);
         throw new IllegalStateException(deleteMessage);
      }
   }

   private void deleteDeployment(String id, String cloudServiceName) {
      String deleteDeploymentId = api.getDeploymentApiForService(cloudServiceName).delete(id);
      if (!operationSucceededPredicate.apply(deleteDeploymentId)) {
         final String deleteMessage = generateIllegalStateExceptionMessage(deleteDeploymentId,
               azureComputeConstants.operationTimeout());
         logger.warn(deleteMessage);
         throw new IllegalStateException(deleteMessage);
      }
   }

   private Map<String, VirtualMachine> virtualMachineMap() {
      Set<VirtualMachine> virtualMachines = Sets.newHashSet();
      List<CloudService> cloudServices = api.getCloudServiceApi().list();

      for (CloudService service : cloudServices) {
         CloudServiceProperties properties = api.getCloudServiceApi().getProperties(service.name());
         List<Deployment> deployments = properties.deployments();
         for (Deployment deployment : deployments) {
            List<VirtualMachine> vmsInDeployment = deploymentToVirtualMachine.apply(deployment);

            virtualMachines.addAll(vmsInDeployment);
         }
      }

      Map<String, VirtualMachine> virtualMachinesMap = Maps
            .uniqueIndex(virtualMachines, new Function<VirtualMachine, String>() {
                     public String apply(VirtualMachine from) {
                        return from.instanceName(); // or something else
                     }
                  }
            );
      return virtualMachinesMap;
   }
}
