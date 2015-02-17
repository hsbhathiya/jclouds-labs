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

import org.jclouds.azurecompute.domain.Deployment;
import org.jclouds.azurecompute.domain.VirtualMachine;
import org.jclouds.azurecompute.xml.DeploymentHandlerTest;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

@Test(groups = "unit", testName = "DeploymentToVritualMachineTest")
public class DeploymentToVritualMachineTest {
   public void testDeploymentTransform() {
      DeploymentToVritualMachine deploymentToVritualMachine = new DeploymentToVritualMachine();
      Deployment deployment = DeploymentHandlerTest.expected();
      List<VirtualMachine> transformed  = deploymentToVritualMachine.apply(deployment);

      int i=0;
      for(VirtualMachine vm : transformed) {
         Deployment.RoleInstance instance = deployment.roleInstanceList().get(i);
         assertEquals(vm.deploymentName(), deployment.name());
         assertEquals(vm.deploymentLabel(), deployment.label());
         assertEquals(vm.deploymentStatus(), deployment.status());
         assertEquals(vm.roleName(),instance.roleName());
         assertEquals(vm.slot(),deployment.slot());
         assertEquals(vm.virtualNetworkName(), deployment.virtualNetworkName());
         assertEquals(vm.instanceSize(),instance.instanceSize());
         i++;
      }
   }
}
