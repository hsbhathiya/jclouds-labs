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
package org.jclouds.azurecompute.compute.extensions;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.jclouds.azurecompute.config.AzureComputeProperties.OPERATION_TIMEOUT;
import static org.jclouds.util.Predicates2.retry;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;

import org.jclouds.azurecompute.AzureComputeApi;
import org.jclouds.azurecompute.config.AzureComputeConstants;
import org.jclouds.azurecompute.domain.CloudService;
import org.jclouds.azurecompute.domain.Deployment;
import org.jclouds.azurecompute.domain.NetworkConfiguration;
import org.jclouds.azurecompute.domain.NetworkConfiguration.VirtualNetworkSite;
import org.jclouds.azurecompute.domain.NetworkSecurityGroup;
import org.jclouds.azurecompute.domain.Operation;
import org.jclouds.azurecompute.domain.Role;
import org.jclouds.azurecompute.domain.Rule;
import org.jclouds.azurecompute.util.NetworkSecurityGroups;
import org.jclouds.compute.domain.SecurityGroup;
import org.jclouds.compute.domain.SecurityGroupBuilder;
import org.jclouds.compute.extensions.SecurityGroupExtension;
import org.jclouds.compute.reference.ComputeServiceConstants;
import org.jclouds.domain.Location;
import org.jclouds.logging.Logger;
import org.jclouds.net.domain.IpPermission;
import org.jclouds.net.domain.IpProtocol;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

/**
 * An extension to compute service to allow for the manipulation of {@link org.jclouds.compute.domain.SecurityGroup}s. Implementation
 * is optional by providers.
 *
 * It considers only the custom rules added by the user and ignores the default rules created by Azure
 */
public class AzureComputeSecurityGroupExtension implements SecurityGroupExtension {

   protected final AzureComputeApi api;
   private final long operationTimeout;
   private Predicate<String> operationSucceeded;

   @Resource
   @Named(ComputeServiceConstants.COMPUTE_LOGGER)
   protected Logger logger = Logger.NULL;

   @Inject
   public AzureComputeSecurityGroupExtension(final AzureComputeApi api, @Named(OPERATION_TIMEOUT) long operationTimeout) {
      this.api = checkNotNull(api, "api");
      this.operationTimeout = checkNotNull(operationTimeout, "operationTimeout");
      this.operationSucceeded = retry(new Predicate<String>() {
         public boolean apply(String input) {
            final Operation operation = api.getOperationApi().get(input);
            return operation.status() == Operation.Status.SUCCEEDED;
         }
      }, operationTimeout, 5, 5, SECONDS);
   }

   @Override
   public Set<SecurityGroup> listSecurityGroups() {
      return FluentIterable.from(api.getNetworkSecurityGroupApi().list())
                           .transform(new NetworkSecurityGroupSecurityGroupFunction())
                           .toSet();
   }

   @Override
   public Set<SecurityGroup> listSecurityGroupsInLocation(final Location location) {
      return FluentIterable.from(api.getNetworkSecurityGroupApi().list())
              .transform(new NetworkSecurityGroupSecurityGroupFunction())
              .toSet();
   }

   /**
    * @param name it represents both cloudservice and deployment name
    * @return Set<SecurityGroup>
    */
   @Override
   public Set<SecurityGroup> listSecurityGroupsForNode(String name) {
      checkNotNull(name, "name");

      final String virtualNetworkName;
      Deployment deployment = api.getDeploymentApiForService(name).get(name);
      virtualNetworkName = deployment.virtualNetworkName();

      List<String> subnetNames = FluentIterable.from(deployment.roles())
                    .transformAndConcat(new Function<Role, Iterable<Role.ConfigurationSet>>() {
                       @Override
                       public Iterable<Role.ConfigurationSet> apply(Role input) {
                          return input.configurationSets();
                       }
                    })
                    .transformAndConcat(new Function<Role.ConfigurationSet, Iterable<Role.ConfigurationSet.SubnetName>>() {
                       @Override
                       public Iterable<Role.ConfigurationSet.SubnetName> apply(Role.ConfigurationSet input) {
                          return input.subnetNames();
                       }
                    })
                    .transform(new Function<Role.ConfigurationSet.SubnetName, String>() {
                       @Override
                       public String apply(Role.ConfigurationSet.SubnetName input) {
                          return input.name();
                       }
                    })
                    .toList();

      return FluentIterable.from(subnetNames)
              .transform(new Function<String, NetworkSecurityGroup>() {
                 @Override
                 public NetworkSecurityGroup apply(String input) {
                    return api.getNetworkSecurityGroupApi().getNetworkSecurityGroupAppliedToSubnet(virtualNetworkName, input);
                 }
              })
              .transform(new NetworkSecurityGroupSecurityGroupFunction())
              .toSet();
   }

   @Override
   public SecurityGroup getSecurityGroupById(String id) {
      return transformNetworkSecurityGroupToSecurityGroup(id);
   }

   @Override
   public SecurityGroup createSecurityGroup(final String name, Location location) {
      checkNotNull(name, "name");
      checkNotNull(location, "location");

      final NetworkSecurityGroup networkSecurityGroup = NetworkSecurityGroup.create(name, name, location.getId(), null);
      String createNSGRequestId =
              api.getNetworkSecurityGroupApi().create(networkSecurityGroup);
      if (!operationSucceeded.apply(createNSGRequestId)) {
         // TODO
      }
      return transformNetworkSecurityGroupToSecurityGroup(name);
   }

   private SecurityGroup transformNetworkSecurityGroupToSecurityGroup(String name) {
      final NetworkSecurityGroup fullDetails = api.getNetworkSecurityGroupApi().getFullDetails(name);
      if (fullDetails == null) return null;
      return new NetworkSecurityGroupSecurityGroupFunction().apply(fullDetails);
   }

   @Override
   public boolean removeSecurityGroup(String id) {

      final NetworkConfiguration networkConfiguration = api.getVirtualNetworkApi().get();
      if (networkConfiguration != null) {
         for (VirtualNetworkSite virtualNetworkSite : networkConfiguration.virtualNetworkSites()) {
            for (NetworkConfiguration.Subnet subnet : virtualNetworkSite.subnets()) {
               final String virtualNetworkName = virtualNetworkSite.name();
               final String subnetName = subnet.name();
               if (virtualNetworkName != null && subnetName != null) {
                  NetworkSecurityGroup networkSecurityGroupAppliedToSubnet = api.getNetworkSecurityGroupApi()
                          .getNetworkSecurityGroupAppliedToSubnet(virtualNetworkName, subnetName);
                  if (networkSecurityGroupAppliedToSubnet != null) {
                     if (!networkSecurityGroupAppliedToSubnet.name().equals(id)) {
                        logger.debug("Removing a networkSecurityGroup %s is already applied to subnet '%s' ...", id, subnetName);
                        // remove existing nsg from subnet
                        String removeFromSubnetRequestId = api.getNetworkSecurityGroupApi().removeFromSubnet
                                (virtualNetworkName, subnetName, networkSecurityGroupAppliedToSubnet.name());
                        String operationDescription = format("Remove existing networkSecurityGroup(%s) from subnet(%s)", id, subnetName);
                        waitForOperationCompletion(removeFromSubnetRequestId, operationDescription);
                     }
                  }
               }
            }
         }
      }
      String deleteRequestId = api.getNetworkSecurityGroupApi().delete(id);
      if (!operationSucceeded.apply(deleteRequestId)) {
         return false;
      }
      return true;
   }

   @Override
   public SecurityGroup addIpPermission(IpPermission ipPermission, SecurityGroup group) {
      checkNotNull(group, "group");
      checkNotNull(ipPermission, "ipPermission");
      String id = checkNotNull(group.getId(), "group.getId()");

      NetworkSecurityGroup networkSecurityGroup = api.getNetworkSecurityGroupApi().getFullDetails(group.getName());
      List<Rule> filteredRules = NetworkSecurityGroups.getCustomRules(networkSecurityGroup);
      int priority = NetworkSecurityGroups.getFirstAvailablePriority(filteredRules);

      String ruleName = NetworkSecurityGroups.createRuleName(ipPermission.getFromPort(), ipPermission.getToPort());

      // add rule to NSG
      addRuleToNetworkSecurityGroup(id, ruleName, priority, ipPermission);

      // add endpoint to VM
      Set<Deployment> deployments = FluentIterable.from(api.getCloudServiceApi().list())
              .transform(new Function<CloudService, Deployment>() {
                 @Override
                 public Deployment apply(CloudService cloudService) {
                    return api.getDeploymentApiForService(cloudService.name()).get(cloudService.name());
                 }
              })
              .filter(Predicates.notNull())
              .toSet();
      // TODO filter deployments
      for (Deployment deployment : deployments) {
         Deployment.VirtualIP virtualIP = Iterables.tryFind(deployment.virtualIPs(), Predicates.notNull()).orNull();
         if (virtualIP == null) throw new IllegalStateException("");

         for (Role role : deployment.roles()) {
            for (Role.ConfigurationSet configurationSet : role.configurationSets()) {
               if (ipPermission.getFromPort() < ipPermission.getToPort()) {
                  for (int i = ipPermission.getFromPort(); i <= ipPermission.getToPort(); i++) {
                     String name = String.format(AzureComputeConstants.TCP_FORMAT, i, i);
                     configurationSet.inputEndpoints().add(createInputEndpoint(name, ipPermission.getIpProtocol().name(),
                             virtualIP.address(), i));
                  }
               } else {
                  configurationSet.inputEndpoints().add(createInputEndpoint(ruleName, ipPermission.getIpProtocol().name(),
                          virtualIP.address(), ipPermission.getToPort()));
               }
               String updateRoleRequestId = api.getVirtualMachineApiForDeploymentInService(deployment.name(), deployment.name()).updateRole(role.roleName(), role);
               if (!operationSucceeded.apply(updateRoleRequestId)) {
                  // TODO
               }
            }
         }

      }
      return transformNetworkSecurityGroupToSecurityGroup(id);
   }

   @Override
   public SecurityGroup addIpPermission(IpProtocol protocol, int startPort, int endPort,
                                        Multimap<String, String> tenantIdGroupNamePairs,
                                        Iterable<String> ipRanges,
                                        Iterable<String> groupIds, SecurityGroup group) {
      IpPermission.Builder permBuilder = IpPermission.builder();
      permBuilder.ipProtocol(protocol);
      permBuilder.fromPort(startPort);
      permBuilder.toPort(endPort);
      permBuilder.tenantIdGroupNamePairs(tenantIdGroupNamePairs);
      permBuilder.cidrBlocks(ipRanges);
      permBuilder.groupIds(groupIds);

      return addIpPermission(permBuilder.build(), group);
   }

   @Override
   public SecurityGroup removeIpPermission(IpPermission ipPermission, SecurityGroup group) {
      checkNotNull(group, "group");
      checkNotNull(ipPermission, "ipPermission");
      String id = checkNotNull(group.getId(), "group.getId()");

      String ruleName = NetworkSecurityGroups.createRuleName(ipPermission.getFromPort(), ipPermission.getToPort());
      // remove rule to NSG
      removeRuleFromNetworkSecurityGroup(id, ruleName);

      // TODO remove endpoint from VM
      Set<Deployment> deployments = FluentIterable.from(api.getCloudServiceApi().list())
              .transform(new Function<CloudService, Deployment>() {
                 @Override
                 public Deployment apply(CloudService cloudService) {
                    return api.getDeploymentApiForService(cloudService.name()).get(cloudService.name());
                 }
              })
              .filter(Predicates.notNull())
              .toSet();
      // TODO filter deployments
      for (Deployment deployment : deployments) {
         Deployment.VirtualIP virtualIP = Iterables.tryFind(deployment.virtualIPs(), Predicates.notNull()).orNull();
         if (virtualIP == null) throw new IllegalStateException("");

         for (Role role : deployment.roles()) {
            for (Role.ConfigurationSet configurationSet : role.configurationSets()) {
               for (int i = ipPermission.getFromPort(); i <= ipPermission.getToPort(); i++) {
                  String name = String.format(AzureComputeConstants.TCP_FORMAT, i, i);
                  configurationSet.inputEndpoints().remove(createInputEndpoint(name, ipPermission.getIpProtocol()
                          .name().toLowerCase(), virtualIP.address(), i));
               }
               String updateRoleRequestId = api.getVirtualMachineApiForDeploymentInService(deployment.name(), deployment.name()).updateRole(role.roleName(), role);
               if (!operationSucceeded.apply(updateRoleRequestId)) {
                  // TODO
               }
            }
         }
      }
      return transformNetworkSecurityGroupToSecurityGroup(id);
   }

   @Override
   public SecurityGroup removeIpPermission(IpProtocol protocol, int startPort, int endPort,
                                           Multimap<String, String> tenantIdGroupNamePairs,
                                           Iterable<String> ipRanges,
                                           Iterable<String> groupIds, SecurityGroup group) {
      IpPermission.Builder permBuilder = IpPermission.builder();
      permBuilder.ipProtocol(protocol);
      permBuilder.fromPort(startPort);
      permBuilder.toPort(endPort);
      permBuilder.tenantIdGroupNamePairs(tenantIdGroupNamePairs);
      permBuilder.cidrBlocks(ipRanges);
      permBuilder.groupIds(groupIds);

      return removeIpPermission(permBuilder.build(), group);
   }

   @Override
   public boolean supportsTenantIdGroupNamePairs() {
      return false;
   }

   @Override
   public boolean supportsTenantIdGroupIdPairs() {
      return false;
   }

   @Override
   public boolean supportsGroupIds() {
      return false;
   }

   @Override
   public boolean supportsPortRangesForGroups() {
      return false;
   }

   @Override
   public boolean supportsExclusionCidrBlocks() {
      return false;
   }

   public static class RuleToIpPermission implements Function<Rule, IpPermission> {

      @Override
      public IpPermission apply(Rule rule) {
         IpPermission.Builder builder = IpPermission.builder();
         if (rule.name().matches("tcp_\\d{1,5}-\\d{1,5}")) {
            builder.fromPort(extractPort(rule.name(), 0))
                    .toPort(extractPort(rule.name(), 1));
         }
         builder.ipProtocol(rule.protocol().equals("*") ? IpProtocol.ALL : IpProtocol.valueOf(rule.protocol()));
         if (rule.destinationAddressPrefix().equals("*")) {
            builder.cidrBlock("0.0.0.0/0");
         } else {
            builder.cidrBlock(rule.destinationAddressPrefix());
         }
         return builder.build();
      }

      private int extractPort(String ruleName, int position) {
         return Integer.parseInt(Iterables.get(Splitter.on("-").omitEmptyStrings().split(ruleName.substring(4, ruleName.length())),
                 position));
      }
   }

   private static class NetworkSecurityGroupSecurityGroupFunction implements Function<NetworkSecurityGroup, SecurityGroup> {
      @Override
      public SecurityGroup apply(NetworkSecurityGroup networkSecurityGroup) {
         SecurityGroupBuilder securityGroupBuilder = new SecurityGroupBuilder()
                 .id(networkSecurityGroup.name())
                 .providerId(networkSecurityGroup.label())
                 .name(networkSecurityGroup.name());
         if (networkSecurityGroup.rules() != null) {

            List<Rule> filteredRules = NetworkSecurityGroups.getCustomRules(networkSecurityGroup);

            Iterable<IpPermission> permissions = Iterables.transform(filteredRules, new RuleToIpPermission());
            securityGroupBuilder.ipPermissions(permissions);
         }
         return securityGroupBuilder.build();
      }
   }

   private void addRuleToNetworkSecurityGroup(String networkSecurityGroupId, String ruleName, int
           priority, IpPermission ipPermission) {

      String protocol = ipPermission.getIpProtocol().name();
      String destinationPortRange;
      if (ipPermission.getFromPort() != ipPermission.getToPort()) {
         destinationPortRange = String.format("%s-%s", ipPermission.getFromPort(), ipPermission.getToPort());
      } else {
         destinationPortRange = String.valueOf(ipPermission.getToPort());
      }
      final String destinationAddressPrefix =
              ipPermission.getCidrBlocks().isEmpty() || Iterables.get(ipPermission.getCidrBlocks(), 0).equals("0.0.0.0/0") ?
                      "*" : Iterables.get(ipPermission.getCidrBlocks(), 0);
      String setRuleToNSGRequestId = api.getNetworkSecurityGroupApi().setRule(networkSecurityGroupId, ruleName,
              Rule.create(ruleName, // name
                      "Inbound", // type
                      String.valueOf(priority), // priority
                      "Allow", // action
                      "INTERNET", // sourceAddressPrefix
                      "*", // sourcePortRange
                      destinationAddressPrefix, // destinationAddressPrefix
                      destinationPortRange, // destinationPortRange
                      protocol,  // protocol
                      "Active", // state
                      true // isDefault
              ));
      if (!operationSucceeded.apply(setRuleToNSGRequestId)) {
         // TODO
      }
   }

   private void removeRuleFromNetworkSecurityGroup(String id, String ruleName) {
      String setRuleToNSGRequestId = api.getNetworkSecurityGroupApi().deleteRule(id, ruleName);
      if (!operationSucceeded.apply(setRuleToNSGRequestId)) {
         // TODO
      }
   }

   private Role.ConfigurationSet.InputEndpoint createInputEndpoint(String ruleName, String protocol, String address, int port) {
      return Role.ConfigurationSet.InputEndpoint.create(
              ruleName,
              protocol,
              port,
              port,
              address,
              false, // enabledDirectServerReturn
              null, // loadBalancerName
              null, // loadBalancerProbe
              null //idleTimeoutInMinutes
      );
   }

   public void waitForOperationCompletion(String operationId, String operationDescription) {
      if (!operationSucceeded.apply(operationId)) {
         final String warnMessage = format("%s) has not been completed within %sms.", operationDescription, Long.toString(operationTimeout));
         logger.warn(warnMessage);
         String illegalStateExceptionMessage = format("%s. Please, try by increasing `jclouds.azure.operation-timeout` and try again", warnMessage);
         throw new IllegalStateException(illegalStateExceptionMessage);
      } else {
         logger.info("%s (id: %s) terminated successfully", operationDescription, operationId);
      }
   }

}
