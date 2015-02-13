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

import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.jclouds.azurecompute.domain.NewDeploymentParams;
import org.jclouds.azurecompute.domain.RoleParam;
import org.jclouds.azurecompute.domain.VMImage;
import org.jclouds.azurecompute.internal.BaseAzureComputeApiMockTest;
import org.jclouds.azurecompute.xml.ListVMImagesHandlerTest;
import org.testng.annotations.Test;

import java.net.URI;

@Test(groups = "unit", testName = "NewDeploymentApiMockTest")
public class NewDeploymentApiMockTest extends BaseAzureComputeApiMockTest {

   public void createVM() throws Exception {
      MockWebServer server = mockAzureManagementServer();
      server.enqueue(requestIdResponse("request-1"));

      try {
         DeploymentApi api = api(server.getUrl("/")).getDeploymentApiForService("myservice");

         VMImage vmImage = ListVMImagesHandlerTest.expected().get(0);

        /* VMImage.OSDiskConfiguration osConfig = vmImage.osDiskConfiguration();
         assertNotNull(osConfig);
         osVirtualHardDiskParam osParam = osVirtualHardDiskParam.builder()
               .diskName(osConfig.name())
               .diskLabel(osConfig.name())
               .hostCaching("ReadWrite")
               .os(osConfig.os())
               .mediaLink(osConfig.mediaLink()).build();*/

         RoleParam roleParam = RoleParam.builder().roleName("instance1")
               .VMImageName(vmImage.name())
               .mediaLocation(URI.create("https://rest.blob.core.windows.net/image"))
               .build();

         NewDeploymentParams deploymentParams = NewDeploymentParams.builder().name("mydeployment")
               .roleParam(roleParam).build();

         String requestId = api.createFromPublicImage(deploymentParams);

         assertSent(server, "POST", "/services/hostedservices/myservice/deployments", "/newdeploymentparams.xml");
      } finally {
         server.shutdown();
      }
   }
}
