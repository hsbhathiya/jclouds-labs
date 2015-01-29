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

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import com.squareup.okhttp.mockwebserver.MockResponse;
import org.jclouds.azurecompute.internal.BaseAzureComputeApiMockTest;
import org.jclouds.azurecompute.xml.RoleHandlerTest;
import org.testng.annotations.Test;

import com.squareup.okhttp.mockwebserver.MockWebServer;

@Test(groups = "unit", testName = "VirtualMachineApiMockTest")
public class VirtualMachineApiMockTest extends BaseAzureComputeApiMockTest {

   public void start() throws Exception {
      MockWebServer server = mockAzureManagementServer();
      server.enqueue(requestIdResponse("request-1"));

      try {
         VirtualMachineApi api = vmApi(server);

         assertThat(api.start("myvm")).isEqualTo("request-1");

         assertSent(server, "POST",
               "/services/hostedservices/my-service/deployments/mydeployment/roleinstances/myvm/Operations",
               "/startrolepayload.xml");
      } finally {
         server.shutdown();
      }
   }

   public void restart() throws Exception {
      MockWebServer server = mockAzureManagementServer();
      server.enqueue(requestIdResponse("request-1"));

      try {
         VirtualMachineApi api = vmApi(server);

         assertThat(api.restart("myvm")).isEqualTo("request-1");

         assertSent(server, "POST",
               "/services/hostedservices/my-service/deployments/mydeployment/roleinstances/myvm/Operations",
               "/restartrolepayload.xml");
      } finally {
         server.shutdown();
      }
   }

   public void shutdown() throws Exception {
      MockWebServer server = mockAzureManagementServer();
      server.enqueue(requestIdResponse("request-1"));

      try {
         VirtualMachineApi api = vmApi(server);

         assertThat(api.shutdown("myvm")).isEqualTo("request-1");

         assertSent(server, "POST",
               "/services/hostedservices/my-service/deployments/mydeployment/roleinstances/myvm/Operations",
               "/shutdownrolepayload.xml");
      } finally {
         server.shutdown();
      }
   }

   public void capture() throws Exception {
      MockWebServer server = mockAzureManagementServer();
      server.enqueue(requestIdResponse("request-1"));

      try {
         VirtualMachineApi api = vmApi(server);

         assertThat(api.capture("myvm", "myImageName", "myImageLabel")).isEqualTo("request-1");

         assertSent(server, "POST",
               "/services/hostedservices/my-service/deployments/mydeployment/roleinstances/myvm/Operations",
               "/capturerolepayload.xml");
      } finally {
         server.shutdown();
      }
   }

   public void getWhenFound() throws Exception {
      MockWebServer server = mockAzureManagementServer();
      server.enqueue(xmlResponse("/role.xml"));

      try {
         VirtualMachineApi api = vmApi(server);

         assertEquals(api.get("testVM"), RoleHandlerTest.expected());

         assertSent(server, "GET", "/services/hostedservices/my-service/deployments/mydeployment/roles/testVM");
      } finally {
         server.shutdown();
      }
   }

   public void getWhenNotFound() throws Exception {
      MockWebServer server = mockAzureManagementServer();
      server.enqueue(new MockResponse().setResponseCode(404));

      try {
         VirtualMachineApi api = vmApi(server);

         assertNull(api.get("testVM"));

         assertSent(server, "GET", "/services/hostedservices/my-service/deployments/mydeployment/roles/testVM");
      } finally {
         server.shutdown();
      }
   }

   private VirtualMachineApi vmApi(MockWebServer server) {
      return api(server.getUrl("/")).getVirtualMachineApiForDeploymentInService("mydeployment", "my-service");
   }
}
