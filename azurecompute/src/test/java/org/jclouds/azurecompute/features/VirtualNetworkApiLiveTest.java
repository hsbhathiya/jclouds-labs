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

import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static org.assertj.core.api.Assertions.assertThat;

import org.jclouds.azurecompute.domain.NetworkConfiguration;
import org.jclouds.azurecompute.domain.NetworkConfiguration.VirtualNetworkSite;
import org.jclouds.azurecompute.internal.BaseAzureComputeApiLiveTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

@Test(groups = "live", testName = "VirtualNetworkApiLiveTest")
public class VirtualNetworkApiLiveTest extends BaseAzureComputeApiLiveTest {

   private NetworkConfiguration originalNetworkConfiguration;

   @BeforeClass
   public void init() {
      originalNetworkConfiguration = api().get();
   }

   @AfterClass
   public void cleanup() {
      if (originalNetworkConfiguration != null) {
         api().set(originalNetworkConfiguration);
      } else {
         api().set(NetworkConfiguration.create(null, ImmutableList.<VirtualNetworkSite> of()));
      }
   }

   @Test public void testSet() {
      String id = "39d0d14b-fc1d-496f-8928-b5a13a6f4123";
      final String name = UPPER_CAMEL.to(LOWER_UNDERSCORE, getClass().getSimpleName());
      final String location = "West Europe";
      final NetworkConfiguration.AddressSpace addressSpace = NetworkConfiguration.AddressSpace.create("10.0.0.1/20");
      final ImmutableList<NetworkConfiguration.Subnet> subnets = ImmutableList.of(NetworkConfiguration.Subnet.create("Subnet-jclouds", "10.0.0.1/23", null));
      final VirtualNetworkSite virtualNetworkSite = VirtualNetworkSite.create(id, name, location, addressSpace, subnets);
      api().set(NetworkConfiguration.create(null, ImmutableList.of(virtualNetworkSite)));
      final NetworkConfiguration networkConfiguration = api().get();
      assertThat(networkConfiguration.dns()).isEqualTo(networkConfiguration.dns());
      assertThat(networkConfiguration.virtualNetworkSites().size()).isEqualTo(1);
      assertThat(networkConfiguration.virtualNetworkSites().get(0).name()).isEqualTo(name);
      assertThat(networkConfiguration.virtualNetworkSites().get(0).location()).isEqualTo(location);
      assertThat(networkConfiguration.virtualNetworkSites().get(0).addressSpace()).isEqualTo(addressSpace);
      assertThat(networkConfiguration.virtualNetworkSites().get(0).subnets()).isEqualTo(subnets);
   }

   @Test(dependsOnMethods = "testSet")
   public void testGet() {
      final NetworkConfiguration networkConfiguration = api().get();
      assertThat(networkConfiguration).isNotNull();
      for (VirtualNetworkSite virtualNetworkSite : networkConfiguration.virtualNetworkSites()) {
         assertThat(virtualNetworkSite.name()).isNotEqualTo("not-existing");
      }
   }

   private VirtualNetworkApi api() {
      return api.getVirtualNetworkApi();
   }

}
