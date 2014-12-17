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
package org.jclouds.azurecompute.xml;

import com.google.common.collect.ImmutableList;
import org.jclouds.azurecompute.domain.RoleInstance;
import org.jclouds.azurecompute.domain.RoleInstance.InstanceEndpoint;
import org.jclouds.http.functions.BaseHandlerTest;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.List;

import static org.testng.Assert.assertEquals;

@Test(groups = "unit", testName = "RoleInstanceHandlerTest")
public class RoleInstanceHandlerTest extends BaseHandlerTest {

   public void test() {
      InputStream is = getClass().getResourceAsStream("/roleinstance.xml");
      RoleInstance result = factory.create(new RoleInstanceHandler()).parse(is);
      assertEquals(result, expected());
   }

   public static RoleInstance expected() {
      return
            RoleInstance.create(
                  "test1-role-name-1", // RoleName
                  "test1-instance-name-1", // InstanceName
                  RoleInstance.InstanceStatus.READY_ROLE, // InstanceStatus
                  "WaitTimeOut", // InstanceErrorCode
                  "Basic_A0", // InstanceSize
                  null,
                  "10.10.10.10", // IPAddress
                  Endpoint(), // InstanceEndpoints
                  RoleInstance.PowerState.Started, // PowerState
                  "test1-hostname", // attachedTo
                  PublicIP()  // PublicIPs
            );

   }

   private static List<InstanceEndpoint> Endpoint() {
      return ImmutableList.of(
            RoleInstance.InstanceEndpoint.create("SSH", "168.63.27.148", "22", "22", "tcp"),
            RoleInstance.InstanceEndpoint.create("SSH", "168.63.27.149", "24", "24", "tcp")
      );
   }

   private static List<RoleInstance.PublicIP> PublicIP() {
      return ImmutableList.of(
            RoleInstance.PublicIP.create("testIP1", 30),
            RoleInstance.PublicIP.create("testIP2", 60)
      );
   }
}
