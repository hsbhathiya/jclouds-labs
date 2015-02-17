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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.jclouds.azurecompute.AzureComputeApi;
import org.jclouds.azurecompute.domain.*;
import org.jclouds.azurecompute.domain.DeploymentParams.ExternalEndpoint;
import org.jclouds.azurecompute.domain.NetworkConfiguration.VirtualNetworkSite;
import org.jclouds.azurecompute.functions.DeploymentToVritualMachine;
import org.jclouds.azurecompute.options.AzureComputeTemplateOptions;
import org.jclouds.compute.ComputeServiceAdapter;
import org.jclouds.compute.domain.OsFamily;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.reference.ComputeServiceConstants;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.logging.Logger;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.net.URI;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Iterables.tryFind;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.jclouds.azurecompute.config.AzureComputeProperties.OPERATION_TIMEOUT;
import static org.jclouds.azurecompute.config.AzureComputeProperties.SUBSCRIPTION_ID;
import static org.jclouds.azurecompute.domain.Deployment.InstanceStatus.READY_ROLE;
import static org.jclouds.util.Predicates2.retry;

/**
 * defines the connection between the {@link org.jclouds.azurecompute.AzureComputeApi} implementation and the
 * jclouds {@link org.jclouds.compute.ComputeService}
 */
@Singleton
public class AzureComputeServiceAdapter2 implements ComputeServiceAdapter<VirtualMachine, RoleSize, OSImage, Location> {

   private static final String DEFAULT_VIRTUAL_NETWORK_NAME = "jclouds-virtual-network";
   private static final String DEFAULT_ADDRESS_SPACE_ADDRESS_PREFIX = "10.0.0.0/20";
   private static final String DEFAULT_SUBNET_NAME = "jclouds-1";
   private static final String DEFAULT_SUBNET_ADDRESS_PREFIX = "10.0.0.0/23";
   private static final String DEFAULT_STORAGE_SERVICE_TYPE = "Standard_GRS";

   private static final String DEFAULT_LOGIN_USER = "jclouds";
   private static final String DEFAULT_LOGIN_PASSWORD = "Azur3Compute!";
   private static final String DEFAULT_STORAGE_ACCOUNT_PREFIX = "jclouds";

   @Resource
   @Named(ComputeServiceConstants.COMPUTE_LOGGER)
   protected Logger logger = Logger.NULL;

   private final AzureComputeApi api;
   private final String subscriptionId;
   private final long operationTimeout;
   private Predicate<String> operationSucceeded;
   private DeploymentToVritualMachine deploymentToVritualMachine;

   @Inject
   public AzureComputeServiceAdapter2(final AzureComputeApi api, @Named(SUBSCRIPTION_ID) String subscriptionId,
         @Named(OPERATION_TIMEOUT) long operationTimeout, DeploymentToVritualMachine deploymentToVritualMachine) {
      this.api = checkNotNull(api, "api");
      this.deploymentToVritualMachine = checkNotNull(deploymentToVritualMachine, "deploymentToVritualMachine");
      this.subscriptionId = checkNotNull(subscriptionId, "subscriptionId");
      this.operationTimeout = checkNotNull(operationTimeout, "operationTimeout");
      this.operationSucceeded = retry(new Predicate<String>() {
         public boolean apply(String input) {
            return api.getOperationApi().get(input).status() == Operation.Status.SUCCEEDED;
         }
      }, operationTimeout, 5, 5, SECONDS);
   }

   @Override
   public NodeAndInitialCredentials<VirtualMachine> createNodeWithGroupEncodedIntoName(
         final String group, final String name, Template template) {

      // azure-specific options
      AzureComputeTemplateOptions templateOptions = template.getOptions().as(AzureComputeTemplateOptions.class);
      final String virtualNetworkName = templateOptions.getVirtualNetworkName().or(DEFAULT_VIRTUAL_NETWORK_NAME);
      final String addressSpaceAddressPrefix = templateOptions.getAddressSpaceAddressPrefix()
            .or(DEFAULT_ADDRESS_SPACE_ADDRESS_PREFIX);
      final String subnetName = templateOptions.getSubnetName().or(DEFAULT_SUBNET_NAME);
      final Set<String> networkSecurityGroupNames = templateOptions.getGroups().isEmpty() ?
            Sets.<String>newHashSet() :
            templateOptions.getGroups();
      final String subnetAddressPrefix = templateOptions.getSubnetAddressPrefix().or(DEFAULT_SUBNET_ADDRESS_PREFIX);
      final String storageAccountType = templateOptions.getStorageAccountType().or(DEFAULT_STORAGE_SERVICE_TYPE);

      final String loginUser =
            templateOptions.getLoginUser() != null ? templateOptions.getLoginUser() : DEFAULT_LOGIN_USER;
      final String loginPassword =
            templateOptions.getLoginPassword() != null ? templateOptions.getLoginPassword() : DEFAULT_LOGIN_PASSWORD;
      final String location = template.getLocation().getId();
      final int[] inboundPorts = template.getOptions().getInboundPorts();

      String storageAccountName = templateOptions.getStorageAccountName()
            .or(generateStorageServiceName(DEFAULT_STORAGE_ACCOUNT_PREFIX));

      // get or create storage service
      StorageService storageService = tryFindExistingStorageServiceAccountOrCreate(location, storageAccountName,
            storageAccountType);
      String storageServiceAccountName = storageService.serviceName();

      // check existence or create virtual network
      checkExistingVirtualNetworkNamedOrCreate(virtualNetworkName, location, subnetName, addressSpaceAddressPrefix,
            subnetAddressPrefix);

      // add network security group to the subnet
      if (!networkSecurityGroupNames.isEmpty()) {
         String networkSecurityGroupName = Iterables.get(networkSecurityGroupNames, 0);
         logger.warn("Only network security group '%s' will be applied to subnet '%s'.", networkSecurityGroupName,
               subnetName);
         final NetworkSecurityGroup networkSecurityGroupAppliedToSubnet = api.getNetworkSecurityGroupApi()
               .getNetworkSecurityGroupAppliedToSubnet(virtualNetworkName, subnetName);
         if (networkSecurityGroupAppliedToSubnet != null) {
            if (!networkSecurityGroupAppliedToSubnet.name().equals(networkSecurityGroupName)) {
               logger.debug("Removing a networkSecurityGroup %s is already applied to subnet '%s' ...",
                     networkSecurityGroupName, subnetName);
               // remove existing nsg from subnet
               String removeFromSubnetRequestId = api.getNetworkSecurityGroupApi()
                     .removeFromSubnet(virtualNetworkName, subnetName, networkSecurityGroupAppliedToSubnet.name());
               String operationDescription = format("Remove existing networkSecurityGroup(%s) from subnet(%s)",
                     networkSecurityGroupName, subnetName);
               waitForOperationCompletion(removeFromSubnetRequestId, operationDescription);
            }
         }
         // add nsg to subnet
         logger.debug("Adding a networkSecurityGroup %s is already applied to subnet '%s' of virtual network %s ...",
               networkSecurityGroupName, subnetName, virtualNetworkName);
         String addToSubnetId = api.getNetworkSecurityGroupApi().addToSubnet(virtualNetworkName, subnetName,
               NetworkSecurityGroup.create(networkSecurityGroupName, null, null, null));
         String operationDescription = format("Add networkSecurityGroup(%s) to subnet(%s)", networkSecurityGroupName,
               subnetName);
         waitForOperationCompletion(addToSubnetId, operationDescription);
      }

      logger.debug("Creating a cloud service with name '%s', label '%s' in location '%s'", name, name, location);
      String createCloudServiceRequestId = api.getCloudServiceApi().createWithLabelInLocation(name, name, location);
      waitForOperationCompletion(createCloudServiceRequestId, format("Create cloud service (%s)", name));

      final OSImage.Type os = template.getImage().getOperatingSystem().getFamily().equals(OsFamily.WINDOWS) ?
            OSImage.Type.WINDOWS :
            OSImage.Type.LINUX;
      Set<ExternalEndpoint> externalEndpoints = Sets.newHashSet();
      for (int inboundPort : inboundPorts) {
         externalEndpoints.add(ExternalEndpoint.inboundTcpToLocalPort(inboundPort, inboundPort));
      }
      final DeploymentParams params = DeploymentParams.builder()
            .name(name)
            .os(os)
            .username(loginUser)
            .password(loginPassword)
            .sourceImageName(template.getImage().getId())
            .mediaLink(createMediaLink(storageServiceAccountName, name))
            .size(RoleSize.Type.fromString(template.getHardware().getId()))
            .externalEndpoints(externalEndpoints)
            .subnetName(subnetName)
            .virtualNetworkName(virtualNetworkName)
            .build();
      logger.debug("Creating a deployment with params '%s' ...", params);
      String createDeploymentRequestId = api.getDeploymentApiForService(name).create(params);
      waitForOperationCompletion(createDeploymentRequestId, format("Create deployment with params (%s)", params));

      if (!retry(new Predicate<String>() {
         public boolean apply(String name) {
            return FluentIterable.from(api.getDeploymentApiForService(name).get(name).roleInstanceList())
                  .allMatch(new Predicate<Deployment.RoleInstance>() {
                     @Override
                     public boolean apply(Deployment.RoleInstance input) {
                        return input != null && input.instanceStatus() == READY_ROLE;
                     }
                  });
         }
      }, 30 * 60, 1, SECONDS).apply(name)) {
         logger.warn("Instances %s of %s has not reached the status %s within %sms so it will be destroyed.",
               Iterables.toString(api.getDeploymentApiForService(name).get(name).roleInstanceList()), name,
               READY_ROLE, Long.toString(operationTimeout));
         api.getDeploymentApiForService(group).delete(name);
         api.getCloudServiceApi().delete(name);
         throw new IllegalStateException(format("Deployment %s is being destroyed as its instanceStatus didn't reach " +
               "status %s after %ss. Please, try by increasing `jclouds.azure.operation-timeout` and " +
               " try again", name, READY_ROLE, 30 * 60));
      }

      Deployment deployment = api.getDeploymentApiForService(name).get(name);
      VirtualMachine virtualMachine = deploymentToVritualMachine.apply(deployment).get(0);

      return new NodeAndInitialCredentials(virtualMachine, virtualMachine.instanceName(),
            LoginCredentials.builder().user(loginUser).password(loginPassword).build());
   }

   private void checkExistingVirtualNetworkNamedOrCreate(final String virtualNetworkName, String
         location, String subnetName, String addressSpaceAddressPrefix, String subnetAddressPrefix) {
      logger.debug("Looking for a virtual network named '%s' ...", virtualNetworkName);
      Optional<VirtualNetworkSite> networkSiteOptional = getVirtualNetworkNamed(virtualNetworkName);
      if (networkSiteOptional.isPresent())
         return;
      final NetworkConfiguration networkConfiguration = NetworkConfiguration.create(null,
            ImmutableList.of(VirtualNetworkSite.create(
                  UUID.randomUUID().toString(),
                  virtualNetworkName,
                  location,
                  NetworkConfiguration.AddressSpace.create(addressSpaceAddressPrefix),
                  ImmutableList.of(NetworkConfiguration.Subnet.create(subnetName, subnetAddressPrefix, null)))));
      logger.debug("Creating a virtual network with configuration '%s' ...", networkConfiguration);
      String setNetworkConfigurationRequestId = api.getVirtualNetworkApi().set(networkConfiguration);
      String operationDescription = format("Network configuration (%s)", networkConfiguration);
      waitForOperationCompletion(setNetworkConfigurationRequestId, operationDescription);
   }

   private Optional<VirtualNetworkSite> getVirtualNetworkNamed(final String virtualNetworkName) {
      return FluentIterable.from(api.getVirtualNetworkApi().list())
            .filter(new Predicate<VirtualNetworkSite>() {
               @Override
               public boolean apply(VirtualNetworkSite input) {
                  return input.name().equals(virtualNetworkName);
               }
            })
            .first();
   }

   /**
    * Tries to find a storage service account whose name matches the regex DEFAULT_STORAGE_ACCOUNT_PREFIX+"[a-z]{10}"
    * in the location, otherwise it creates a new storage service account with name and type in the location
    */
   private StorageService tryFindExistingStorageServiceAccountOrCreate(String location, final String name,
         final String type) {
      final List<StorageService> storageServices = api.getStorageAccountApi().list(subscriptionId);
      Predicate<StorageService> storageServicePredicate;
      logger.debug("Looking for a suitable existing storage account ...");
      storageServicePredicate = and(notNull(), new SameLocationAndCreatedStorageServicePredicate(location),
            new Predicate<StorageService>() {
               @Override
               public boolean apply(StorageService input) {
                  return input.serviceName().matches(format("^%s[a-z]{10}$", DEFAULT_STORAGE_ACCOUNT_PREFIX));
               }
            });
      final Optional<StorageService> storageServiceOptional = tryFind(storageServices, storageServicePredicate);
      if (storageServiceOptional.isPresent()) {
         final StorageService storageService = storageServiceOptional.get();
         logger.debug("Found a suitable existing storage service account '%s'", storageService);
         return storageService;
      } else {
         // create
         if (!checkAvailability(name)) {
            logger.warn("The storage service account name %s is not available", name);
            throw new IllegalStateException(format("Can't create a valid storage account with name %s. " +
                  "Please, try by choosing a different `storageAccountName` in templateOptions and try again", name));
         }
         logger.debug("Creating a storage service account '%s' in location '%s' ...", name, location);
         String createStorateServiceRequestId = api.getStorageAccountApi().create(StorageServiceParams.builder()
               .name(name)
               .label(name)
               .location(location)
               .accountType(StorageServiceParams.Type.valueOf(type))
               .build());
         waitForOperationCompletion(createStorateServiceRequestId, format("Create storage service account(%s)", name));
         return api.getStorageAccountApi().get(subscriptionId, name);
      }
   }

   @Override
   public Iterable<RoleSize> listHardwareProfiles() {
      return api.getSubscriptionApi().list(subscriptionId);
   }

   @Override
   public Iterable<OSImage> listImages() {
      return api.getOSImageApi().list();
   }

   @Override
   public OSImage getImage(String id) {
      for(OSImage osImage : listImages()){
         if(osImage.name().equals(id)){
            return  osImage;
         }
      }
      return  null;
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
            .deleteRole(node.roleName());
   }

   @Override
   public void rebootNode(String id) {
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

   @Override public Iterable<VirtualMachine> listNodesByIds(Iterable<String> ids) {
      Map<String, VirtualMachine> vmMap = virtualMachineMap();

      Set<VirtualMachine> virtualMachines = Sets.newHashSet();
      Iterator<String> it = ids.iterator();
      while (it.hasNext()) {
         virtualMachines.add(vmMap.get(it.next()));
      }
      return virtualMachines;
   }

   private static class SameLocationAndCreatedStorageServicePredicate implements Predicate<StorageService> {
      private final String location;

      public SameLocationAndCreatedStorageServicePredicate(String location) {
         this.location = location;
      }

      @Override
      public boolean apply(StorageService input) {
         return input.storageServiceProperties().location().equals(location) && input.storageServiceProperties()
               .status().equals("Created");
      }
   }

   private boolean checkAvailability(String name) {
      return api.getStorageAccountApi().checkAvailable(subscriptionId, name).result();
   }

   private String generateStorageServiceName(String prefix) {
      String characters = "abcdefghijklmnopqrstuvwxyz";
      StringBuilder builder = new StringBuilder();
      builder.append(prefix);
      int charactersLength = characters.length();
      for (int i = 0; i < 10; i++) {
         double index = Math.random() * charactersLength;
         builder.append(characters.charAt((int) index));
      }
      return builder.toString();
   }

   private URI createMediaLink(String storageServiceName, String diskName) {
      return URI
            .create(String.format("https://%s.blob.core.windows.net/vhds/disk-%s.vhd", storageServiceName, diskName));
   }

   public void waitForOperationCompletion(String operationId, String operationDescription) {
      if (!operationSucceeded.apply(operationId)) {
         final String warnMessage = format("%s) has not been completed within %sms.", operationDescription,
               Long.toString(operationTimeout));
         logger.warn(warnMessage);
         String illegalStateExceptionMessage = format(
               "%s. Please, try by increasing `jclouds.azure.operation-timeout` and try again", warnMessage);
         throw new IllegalStateException(illegalStateExceptionMessage);
      } else {
         logger.info("%s (id: %s) terminated successfully", operationDescription, operationId);
      }
   }

   private Map<String, VirtualMachine> virtualMachineMap() {
      Set<VirtualMachine> virtualMachines = Sets.newHashSet();

      List<CloudService> cloudServices = api.getCloudServiceApi().list();

      for (CloudService service : cloudServices) {
         CloudServiceProperties properties = api.getCloudServiceApi().getProperties(service.name());
         List<Deployment> deployments = properties.deployments();
         for (Deployment deployment : deployments) {
            List<VirtualMachine> vmsInDeployment = deploymentToVritualMachine.apply(deployment);

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
