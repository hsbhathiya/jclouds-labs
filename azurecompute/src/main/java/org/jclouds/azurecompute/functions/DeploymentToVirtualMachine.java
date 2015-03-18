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
package org.jclouds.azurecompute.functions;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jclouds.azurecompute.domain.Deployment;
import org.jclouds.azurecompute.domain.Role;
import org.jclouds.azurecompute.domain.VirtualMachine;

import java.util.List;
import java.util.Map;

public class DeploymentToVirtualMachine implements Function<Deployment, List<VirtualMachine>> {

    @Override
    public List<VirtualMachine> apply(Deployment input) {

        List<Deployment.RoleInstance> roleInstances = input.roleInstanceList();
        List<Role> roles = input.roleList();

        Map<String, Role> mappedRoles = Maps.uniqueIndex(roles, new Function<Role, String>() {
                    public String apply(Role from) {
                        return from.roleName(); // or something else
                    }
                }
        );

        List<VirtualMachine> virtualMachines = Lists.newArrayList();
        for (Deployment.RoleInstance instance : roleInstances) {

            VirtualMachine vm = VirtualMachine.builder()
                    .serviceName(input.name())
                    .deploymentName(input.name())
                    .slot(input.slot())
                    .deploymentStatus(input.status())
                    .deploymentLabel(input.label())
                    .roleName(instance.roleName())
                    .instanceSize(instance.instanceSize())
                    .instanceStatus(instance.instanceStatus())
                    .instanceName(instance.instanceName())
                    .instanceErrorCode(input.instanceErrorCode())
                    .instanceStateDetails(input.instanceStateDetails())
                    .instanceEndpoints(instance.instanceEndpoints())
                    .role(mappedRoles.get(instance.roleName()))
                    .virtualNetworkName(input.virtualNetworkName())
                    .virtualIps(input.virtualIPs())
                    .build();

            virtualMachines.add(vm);
        }
        return virtualMachines;
    }
}
