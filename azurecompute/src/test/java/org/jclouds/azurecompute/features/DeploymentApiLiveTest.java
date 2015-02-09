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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.jclouds.azurecompute.domain.CloudService;
import org.jclouds.azurecompute.domain.CloudService.Status;
import org.jclouds.azurecompute.domain.Deployment;
import org.jclouds.azurecompute.domain.Operation;
import org.jclouds.azurecompute.internal.BaseAzureComputeApiLiveTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.logging.Logger;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.jclouds.util.Predicates2.retry;

@Test(groups = "live", testName = "CloudServiceApiLiveTest")
public class DeploymentApiLiveTest extends BaseAzureComputeApiLiveTest {

   public static final String CLOUD_SERVICE = (System.getProperty("user.name") + "-jclouds-cloudService2")
         .toLowerCase();
   public static final String DEPLOYMENT = (System.getProperty("user.name") + "-jclouds-deployment").toLowerCase();

   private Predicate<String> operationSucceeded;
   private Predicate<CloudService> cloudServiceCreated;
   private Predicate<CloudService> cloudServiceGone;

   private String location;

   @BeforeClass(groups = { "integration", "live" })
   public void setup() {
      super.setup();
      // TODO: filter locations on those who have compute
      location = Iterables.get(api.getLocationApi().list(), 0).name();
      operationSucceeded = retry(new Predicate<String>() {
         public boolean apply(String input) {
            return api.getOperationApi().get(input).status() == Operation.Status.SUCCEEDED;
         }
      }, 600, 5, 5, SECONDS);
      cloudServiceCreated = retry(new Predicate<CloudService>() {
         public boolean apply(CloudService input) {
            return cloudServiceApi().get(input.name()).status() == Status.CREATED;
         }
      }, 600, 5, 5, SECONDS);
      cloudServiceGone = retry(new Predicate<CloudService>() {
         public boolean apply(CloudService input) {
            return cloudServiceApi().get(input.name()) == null;
         }
      }, 600, 5, 5, SECONDS);
   }

   private CloudService cloudService;

   public void testCreateCloudService() {

      String requestId = cloudServiceApi().createWithLabelInLocation(CLOUD_SERVICE, CLOUD_SERVICE, location);
      assertTrue(operationSucceeded.apply(requestId), requestId);
      Logger.getAnonymousLogger().info("operation succeeded: " + requestId);

      cloudService = cloudServiceApi().get(CLOUD_SERVICE);
      Logger.getAnonymousLogger().info("created cloudService: " + cloudService);

      assertEquals(cloudService.name(), CLOUD_SERVICE);

      assertTrue(cloudServiceCreated.apply(cloudService), cloudService.toString());
      cloudService = cloudServiceApi().get(cloudService.name());
      Logger.getAnonymousLogger().info("cloudService available: " + cloudService);
   }

   @Test(dependsOnMethods = " testCreateCloudService")
   public void testCreateVMDeployment() {
      Deployment response = api().get(DEPLOYMENT);
      checkDeployment(response);
   }

   @Test(dependsOnMethods = " testCreateCloudService")
   public void testGet() {
      Deployment response = api().get(DEPLOYMENT);
      checkDeployment(response);
   }

   @Test(dependsOnMethods = "testGet")
   public void testDelete() {
      String requestId = api().delete(DEPLOYMENT);
      assertTrue(operationSucceeded.apply(requestId), requestId);
      Logger.getAnonymousLogger().info("operation succeeded: " + requestId);

      assertTrue(cloudServiceGone.apply(cloudService), cloudService.toString());
      Logger.getAnonymousLogger().info("cloudService deleted: " + cloudService);
   }

   private void checkDeployment(Deployment deployment) {
      assertNotNull(deployment);
      assertNotNull(deployment.name(), "Name cannot be Null for Deployment" + deployment);
      assertTrue(deployment.roles().size() > 0, "There should be atleast 1 Virtual machine for a deployment  ");
      assertNotNull(deployment.label(), "Label cannot be Null for Deployment" + deployment);

      Deployment.Slot slot = deployment.slot();
      assertTrue((slot == Deployment.Slot.PRODUCTION) || (slot == Deployment.Slot.STAGING));
      assertEquals(deployment.name(), DEPLOYMENT);
   }

   @Override @AfterClass(groups = "live")
   protected void tearDown() {
      String requestId = cloudServiceApi().delete(CLOUD_SERVICE);
      if (requestId != null) {
         operationSucceeded.apply(requestId);
      }
      super.tearDown();
   }

   private CloudServiceApi cloudServiceApi() {
      return api.getCloudServiceApi();
   }

   private DeploymentApi api() {
      return api.getDeploymentApiForService(CLOUD_SERVICE);
   }
}
