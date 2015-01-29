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

import com.google.common.collect.ImmutableList;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import org.jclouds.azurecompute.domain.*;
import org.jclouds.azurecompute.internal.BaseAzureComputeApiMockTest;
import org.jclouds.azurecompute.xml.DeploymentHandlerTest;
import org.jclouds.azurecompute.xml.ListOSImagesHandlerTest;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.List;

import static org.jclouds.azurecompute.domain.NewDeploymentParams.ExternalEndpoint.inboundTcpToLocalPort;
import static org.jclouds.azurecompute.domain.NewDeploymentParams.ExternalEndpoint.inboundUdpToLocalPort;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@Test(groups = "unit", testName = "NewDeploymentApiMockTest")
public class NewDeploymentApiMockTest extends BaseAzureComputeApiMockTest {

   public void createLinux() throws Exception {
      MockWebServer server = mockAzureManagementServer();
      server.enqueue(requestIdResponse("request-1"));

      try {
         DeploymentApi api = api(server.getUrl("/")).getDeploymentApiForService("myservice");

         OSImage OSImage = ListOSImagesHandlerTest.expected().get(5); // CentOS

         NewDeploymentParams params = NewDeploymentParams.builder()
               .name("mydeployment")
               .externalEndpoint(inboundTcpToLocalPort(80, 8080))
               .externalEndpoint(inboundUdpToLocalPort(53, 53)).build();

         assertEquals(api.create(params), "request-1");

         assertSent(server, "POST", "/services/hostedservices/myservice/deployments", "/deploymentparams.xml");
      } finally {
         server.shutdown();
      }
   }

   public void createWindows() throws Exception {
      MockWebServer server = mockAzureManagementServer();
      server.enqueue(requestIdResponse("request-1"));

      try {
         DeploymentApi api = api(server.getUrl("/")).getDeploymentApiForService("myservice");

         OSImage OSImage = ListOSImagesHandlerTest.expected().get(1); // Windows

         NewDeploymentParams params = NewDeploymentParams.builder()
               .name("mydeployment")
               .roleParam(expected())
               .externalEndpoint(inboundTcpToLocalPort(80, 8080))
               .externalEndpoint(inboundUdpToLocalPort(53, 53)).build();

         assertEquals(api.create(params), "request-1");

         assertSent(server, "POST", "/services/hostedservices/myservice/deployments", "/deploymentparams-windows.xml");
      } finally {
         server.shutdown();
      }
   }

   public void getWhenFound() throws Exception {
      MockWebServer server = mockAzureManagementServer();
      server.enqueue(xmlResponse("/deployment.xml"));

      try {
         DeploymentApi api = api(server.getUrl("/")).getDeploymentApiForService("myservice");

         assertEquals(api.get("mydeployment"), DeploymentHandlerTest.expected());

         assertSent(server, "GET", "/services/hostedservices/myservice/deployments/mydeployment");
      } finally {
         server.shutdown();
      }
   }

   public void getWhenNotFound() throws Exception {
      MockWebServer server = mockAzureManagementServer();
      server.enqueue(new MockResponse().setResponseCode(404));

      try {
         DeploymentApi api = api(server.getUrl("/")).getDeploymentApiForService("myservice");

         assertNull(api.get("mydeployment"));

         assertSent(server, "GET", "/services/hostedservices/myservice/deployments/mydeployment");
      } finally {
         server.shutdown();
      }
   }

   public void deleteWhenFound() throws Exception {
      MockWebServer server = mockAzureManagementServer();
      server.enqueue(requestIdResponse("request-1"));

      try {
         DeploymentApi api = api(server.getUrl("/")).getDeploymentApiForService("myservice");

         assertEquals(api.delete("mydeployment"), "request-1");

         assertSent(server, "DELETE", "/services/hostedservices/myservice/deployments/mydeployment");
      } finally {
         server.shutdown();
      }
   }

   public void deleteWhenNotFound() throws Exception {
      MockWebServer server = mockAzureManagementServer();
      server.enqueue(new MockResponse().setResponseCode(404));

      try {
         DeploymentApi api = api(server.getUrl("/")).getDeploymentApiForService("myservice");

         assertNull(api.delete("mydeployment"));

         assertSent(server, "DELETE", "/services/hostedservices/myservice/deployments/mydeployment");
      } finally {
         server.shutdown();
      }
   }

   public static RoleParam expected() {
      return RoleParam.builder().roleName("testVM").roleType("PersistentVMRole").availabilitySetName("testSet").mediaLocation( URI.create(
            "https://bahshstorage.blob.core.windows.net/communityimages/community-12-c59cc53c-80c9-48fb-ba81-9ed6fe46eeb9-1.vhd")).build();
   }

   private static List<DataVirtualHardDiskParam> DataVHD() {
      return  null;
      /*return ImmutableList.of(
            DataVirtualHardDisk.create(
                  "ReadOnly",
                  "MyTestImage_1",
                  "testimage1-testimage1-0-20120817095145",
                  10,
                  30,
                  URI.create("http://blobs/disks/neotysss/MSFT__Win2K8R2SP1-ABCD-en-us-30GB.vhd"),
                  URI.create("http://blobs/disks/neotysss/MSFT__Win2K8R2SP1-ABCD-en-us-30GB.vhd"),
                  "Standard"
            ),
            DataVirtualHardDisk.create(
                  "ReadWrite",
                  "MyTestImage_2",
                  "testimage2-testimage2-0-20120817095145",
                  10,
                  30,
                  URI.create("http://blobs/disks/neotysss/MSFT__Win2K8R2SP1-ABCD-en-us-30GB.vhd"),
                  URI.create("http://blobs/disks/neotysss/MSFT__Win2K8R2SP1-ABCD-en-us-30GB.vhd"),
                  "Standard"
            )
      );*/
   }

   private static OSVirtualHardDisk OSVHD() {
      return OSVirtualHardDisk.create(
            "ReadOnly",
            "MyTestImage_1",
            "testosimage1-testosimage1-0-20120817095145",
            URI.create("http://blobs/disks/neotysss/MSFT__Win2K8R2SP1-ABCD-en-us-30GB.vhd"),
            "Ubuntu Server 12.04 LTS",
            OSImage.Type.LINUX,
            30,
            URI.create("http://blobs/disks/neotysss/MSFT__Win2K8R2SP1-ABCD-en-us-30GB.vhd"),
            "Standard"
      );
   }
}
