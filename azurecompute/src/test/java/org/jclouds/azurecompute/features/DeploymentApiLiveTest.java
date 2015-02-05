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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.jclouds.util.Predicates2.retry;
import static org.testng.Assert.assertEquals;

import java.util.List;

import org.jclouds.azurecompute.domain.Deployment;
import org.jclouds.azurecompute.domain.Operation;
import org.jclouds.azurecompute.domain.Role;
import org.jclouds.azurecompute.internal.BaseAzureComputeApiLiveTest;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.base.Predicate;

@Test(groups = "live", testName = "DeploymentApiLiveTest")
public class DeploymentApiLiveTest extends BaseAzureComputeApiLiveTest {

   public static final String CLOUD_SERVICE = "bhathiya-jclouds-cloudservice179"; //(System.getProperty("user.name") + "-jclouds-cloudService").toLowerCase();
   public static final String DEPLOYMENT = "bhathiya-jclouds-deployment179";

   private Predicate<String> operationSucceeded;

   @BeforeClass(groups = { "integration", "live" })
   public void setup() {
      super.setup();
      // TODO: filter locations on those who have compute
      operationSucceeded = retry(new Predicate<String>() {
         public boolean apply(String input) {
            return api.getOperationApi().get(input).status() == Operation.Status.SUCCEEDED;
         }
      }, 600, 5, 5, SECONDS);
   }

   public void testGet() {
      Deployment deployment = api().get(DEPLOYMENT);
      List<Role> roles = deployment.roles();
      Role role = roles.get(0);
      assertEquals(deployment.toString(), "");
   }

   private DeploymentApi api() {
      return api.getDeploymentApiForService(CLOUD_SERVICE);
   }
}
