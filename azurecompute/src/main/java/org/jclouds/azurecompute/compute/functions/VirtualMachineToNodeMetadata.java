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
package org.jclouds.azurecompute.compute.functions;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.jclouds.azurecompute.AzureComputeApi;
import org.jclouds.azurecompute.domain.CloudService;
import org.jclouds.azurecompute.domain.Deployment;
import org.jclouds.azurecompute.domain.VirtualMachine;
import org.jclouds.collect.Memoized;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.NodeMetadataBuilder;
import org.jclouds.compute.functions.GroupNamingConvention;
import org.jclouds.domain.Credentials;
import org.jclouds.domain.Location;
import org.jclouds.location.predicates.LocationPredicates;

import javax.inject.Inject;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class VirtualMachineToNodeMetadata implements Function<VirtualMachine, NodeMetadata> {

	private final Supplier<Set<? extends Location>> locations;
	private final GroupNamingConvention nodeNamingConvention;
	private final OSImageToImage osImageToImage;
	private final RoleSizeToHardware roleSizeToHardware;
	private final Map<String, Credentials> credentialStore;
	private final AzureComputeApi api;

	private static final Map<Deployment.InstanceStatus, NodeMetadata.Status> INSTANCESTATUS_TO_NODESTATUS =
			ImmutableMap.<Deployment.InstanceStatus, NodeMetadata.Status>builder().
					put(Deployment.InstanceStatus.BUSY_ROLE, NodeMetadata.Status.PENDING).
					put(Deployment.InstanceStatus.CREATING_ROLE, NodeMetadata.Status.PENDING).
					put(Deployment.InstanceStatus.CREATING_VM, NodeMetadata.Status.PENDING).
					put(Deployment.InstanceStatus.CYCLING_ROLE, NodeMetadata.Status.PENDING).
					put(Deployment.InstanceStatus.DELETING_VM, NodeMetadata.Status.PENDING).
					put(Deployment.InstanceStatus.FAILED_STARTING_ROLE, NodeMetadata.Status.ERROR).
					put(Deployment.InstanceStatus.FAILED_STARTING_VM, NodeMetadata.Status.ERROR).
					put(Deployment.InstanceStatus.PREPARING, NodeMetadata.Status.PENDING).
					put(Deployment.InstanceStatus.READY_ROLE, NodeMetadata.Status.RUNNING).
					put(Deployment.InstanceStatus.RESTARTING_ROLE, NodeMetadata.Status.PENDING).
					put(Deployment.InstanceStatus.STARTING_ROLE, NodeMetadata.Status.PENDING).
					put(Deployment.InstanceStatus.STARTING_VM, NodeMetadata.Status.PENDING).
					put(Deployment.InstanceStatus.STOPPED_DEALLOCATED, NodeMetadata.Status.SUSPENDED).
					put(Deployment.InstanceStatus.STOPPED_VM, NodeMetadata.Status.SUSPENDED).
					put(Deployment.InstanceStatus.STOPPING_ROLE, NodeMetadata.Status.PENDING).
					put(Deployment.InstanceStatus.STOPPING_VM, NodeMetadata.Status.PENDING).
					put(Deployment.InstanceStatus.ROLE_STATE_UNKNOWN, NodeMetadata.Status.UNRECOGNIZED).
					put(Deployment.InstanceStatus.UNRECOGNIZED, NodeMetadata.Status.UNRECOGNIZED).
					build();

	private static final Map<Deployment.Status, NodeMetadata.Status> STATUS_TO_NODESTATUS =
			ImmutableMap.<Deployment.Status, NodeMetadata.Status>builder().
					put(Deployment.Status.DELETING, NodeMetadata.Status.PENDING).
					put(Deployment.Status.SUSPENDED_TRANSITIONING, NodeMetadata.Status.PENDING).
					put(Deployment.Status.RUNNING_TRANSITIONING, NodeMetadata.Status.PENDING).
					put(Deployment.Status.DEPLOYING, NodeMetadata.Status.PENDING).
					put(Deployment.Status.STARTING, NodeMetadata.Status.PENDING).
					put(Deployment.Status.SUSPENDED, NodeMetadata.Status.SUSPENDED).
					put(Deployment.Status.RUNNING, NodeMetadata.Status.RUNNING).
					put(Deployment.Status.UNRECOGNIZED, NodeMetadata.Status.UNRECOGNIZED).
					build();

	@Inject VirtualMachineToNodeMetadata(AzureComputeApi api,
										 @Memoized Supplier<Set<? extends Location>> locations,
										 GroupNamingConvention.Factory namingConvention,
										 OSImageToImage osImageToImage,
										 RoleSizeToHardware roleSizeToHardware,
										 Map<String, Credentials> credentialStore) {
		this.nodeNamingConvention = checkNotNull(namingConvention, "namingConvention").createWithoutPrefix();
		this.locations = checkNotNull(locations, "locations");
		this.osImageToImage = checkNotNull(osImageToImage, "osImageToImage");
		this.roleSizeToHardware = checkNotNull(roleSizeToHardware, "roleSizeToHardware");
		this.credentialStore = checkNotNull(credentialStore, "credentialStore cannot be null");
		this.api = api;
	}

	@Override
	public NodeMetadata apply(VirtualMachine from) {
		final NodeMetadataBuilder builder = new NodeMetadataBuilder();
		builder.id(from.roleName());
		builder.providerId(from.roleName());
		builder.name(from.roleName());
		builder.hostname(from.serviceName());
		builder.group(nodeNamingConvention.groupInUniqueNameOrNull(from.serviceName()));
		/* TODO
		if (from.getDatacenter() != null) {
			builder.location(from(locations.get()).firstMatch(
					  LocationPredicates.idEquals(from.getDatacenter().getId() + "")).orNull());
		}
		builder.group(nodeNamingConvention.groupInUniqueNameOrNull(from.getHostname()));
		builder.hardware(roleSizeToHardware.apply(from.instanceSize()));
		Image image = osImageToImage.apply(from);
		if (image != null) {
			builder.imageId(image.getId());
			builder.operatingSystem(image.getOperatingSystem());
		}
		*/
		final CloudService cloudService = api.getCloudServiceApi().get(from.serviceName());
		if (cloudService != null) {
			builder.location(FluentIterable.from(locations.get()).
					firstMatch(LocationPredicates.idEquals(cloudService.location())).
					orNull());
		}

		if (from.instanceStatus() != null) {
				builder.status(INSTANCESTATUS_TO_NODESTATUS.get(from.instanceStatus()));
			} else if(from.deploymentStatus() != null){
				builder.status(STATUS_TO_NODESTATUS.get(from.deploymentStatus()));
			}

		/*if (from.deploymentStatus() != null) {
			builder.status(serverStateToNodeStatus.get(from.deploymentStatus()));
		}*/

		Set<String> publicIpAddresses = Sets.newLinkedHashSet();
		if (from.virtualIPs() != null) {
			for (Deployment.VirtualIP virtualIP : from.virtualIPs()) {
				publicIpAddresses.add(virtualIP.address());
			}
			builder.publicAddresses(publicIpAddresses);
		}
		Set<String> privateIpAddresses = Sets.newLinkedHashSet();
		if (from.instanceEndpoints() != null) {
			for (Deployment.InstanceEndpoint instanceEndpoint : from.instanceEndpoints()) {
				privateIpAddresses.add(instanceEndpoint.vip());
			}
			builder.privateAddresses(privateIpAddresses);
		}
		return builder.build();
	}
}
