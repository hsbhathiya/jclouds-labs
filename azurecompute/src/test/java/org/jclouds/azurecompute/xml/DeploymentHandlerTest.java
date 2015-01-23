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

import static org.jclouds.azurecompute.xml.DeploymentHandler.parseInstanceStatus;
import static org.testng.Assert.assertEquals;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

import com.google.common.collect.ImmutableList;
import org.jclouds.azurecompute.domain.*;
import org.jclouds.azurecompute.domain.Deployment.InstanceStatus;
import org.jclouds.azurecompute.domain.Deployment.Slot;
import org.jclouds.azurecompute.domain.Deployment.Status;
import org.jclouds.azurecompute.domain.RoleSizeName;
import org.jclouds.http.functions.BaseHandlerTest;
import org.testng.annotations.Test;

@Test(groups = "unit", testName = "DeploymentHandlerTest")
public class DeploymentHandlerTest extends BaseHandlerTest {

   /**
    * Covers values listed <a href="http://msdn.microsoft.com/en-us/library/azure/ee460804.aspx#RoleInstanceList">here</a>.
    */
   public void parseInstanceStatus_Recognized() {
      assertEquals(parseInstanceStatus("Unknown"), InstanceStatus.UNKNOWN);
      assertEquals(parseInstanceStatus("CreatingVM"), InstanceStatus.CREATING_VM);
      assertEquals(parseInstanceStatus("StartingVM"), InstanceStatus.STARTING_VM);
      assertEquals(parseInstanceStatus("CreatingRole"), InstanceStatus.CREATING_ROLE);
      assertEquals(parseInstanceStatus("StartingRole"), InstanceStatus.STARTING_ROLE);
      assertEquals(parseInstanceStatus("ReadyRole"), InstanceStatus.READY_ROLE);
      assertEquals(parseInstanceStatus("BusyRole"), InstanceStatus.BUSY_ROLE);
      assertEquals(parseInstanceStatus("StoppingRole"), InstanceStatus.STOPPING_ROLE);
      assertEquals(parseInstanceStatus("StoppingVM"), InstanceStatus.STOPPING_VM);
      assertEquals(parseInstanceStatus("DeletingVM"), InstanceStatus.DELETING_VM);
      assertEquals(parseInstanceStatus("StoppedVM"), InstanceStatus.STOPPED_VM);
      assertEquals(parseInstanceStatus("RestartingRole"), InstanceStatus.RESTARTING_ROLE);
      assertEquals(parseInstanceStatus("CyclingRole"), InstanceStatus.CYCLING_ROLE);
      assertEquals(parseInstanceStatus("FailedStartingRole"), InstanceStatus.FAILED_STARTING_ROLE);
      assertEquals(parseInstanceStatus("FailedStartingVM"), InstanceStatus.FAILED_STARTING_VM);
      assertEquals(parseInstanceStatus("UnresponsiveRole"), InstanceStatus.UNRESPONSIVE_ROLE);
      assertEquals(parseInstanceStatus("StoppedDeallocated"), InstanceStatus.STOPPED_DEALLOCATED);
      assertEquals(parseInstanceStatus("Preparing"), InstanceStatus.PREPARING);
   }

   public void parseInstanceStatus_Unrecognized() {
      assertEquals(parseInstanceStatus("FooAddedToday"), InstanceStatus.UNRECOGNIZED);
   }

   public void test() {
      InputStream is = getClass().getResourceAsStream("/deployment.xml");
      Deployment result = factory.create(new DeploymentHandler()).parse(is);
      assertEquals(result, expected());
   }

   public static Deployment expected() {
      return Deployment.create( //
            "deployment_name", // name
            Slot.PRODUCTION, // slot
            "05aa8ec5d8ee4215894431c7db401b31", //privateId
            Status.RUNNING, // status
            "neotysss", // label
            URI.create("http://neotysss.cloudapp.net"), // Url
            "PFNlcnZpY2VDb25maWd1cmF0aW9uIHhtbG5zOnhza", // Configuration
            instanceList(), //RoleInstances
            roleList() // Roles
      );
   }

   private static List<Role> roleList() {
      return ImmutableList.of(
            Role.create(
                  "testVM",
                  "PersistentVMRole",
                  "bc322bdc-f685-4002-8efb-4c6089bb2588__Image__openSUSE-12-3-for-Windows-Azure",
                  URI.create(
                        "https://bahshstorage.blob.core.windows.net/communityimages/community-12-c59cc53c-80c9-48fb-ba81-9ed6fe46eeb9-1.vhd"),
                  RoleSizeName.EXTRA_SMALL,
                  null,
                  DataVHD(),
                  OSVHD(),
                  false
            )
      );
   }

   private static List<DataVirtualHardDisk> DataVHD() {
      return ImmutableList.of(
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
      );
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

   public static List<RoleInstance> instanceList() {
      return ImmutableList.of(
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
            )
      );

   }

   private static List<RoleInstance.InstanceEndpoint> Endpoint() {
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
