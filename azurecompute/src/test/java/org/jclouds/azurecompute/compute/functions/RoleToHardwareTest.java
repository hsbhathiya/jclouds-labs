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
package org.jclouds.azurecompute.compute.functions;

import com.google.common.collect.ImmutableList;
import org.jclouds.azurecompute.domain.*;
import org.jclouds.compute.domain.*;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.List;

import static org.testng.Assert.*;

@Test(groups = "unit", testName = "RoleToHardwareTest")
public class RoleToHardwareTest {
   public void testImageTransform() {
      RoleToHardware roleToHardware = new RoleToHardware();
      Role role = createRole();
      Hardware transformed = roleToHardware.apply(role);

      assertEquals(RoleSizeEnum.EXTRA_SMALL, role.roleSize());
      assertEquals(transformed.getId(), role.roleName());
      assertEquals(transformed.getName(), role.roleName());
      assertEquals(transformed.getId(), role.roleName());
      assertEquals(transformed.getHypervisor(), "Azure Hypervisor" );

      //Processor
      Processor processor = transformed.getProcessors().get(0);
      assertNotNull(processor);
      assertEquals(processor.getCores() , 1.0);
      assertEquals(processor.getSpeed() , 1.6);

      //Volumes
      List<? extends Volume> volumes = transformed.getVolumes();
      assertEquals(volumes.size(), 3);

      Volume volume1 = volumes.get(0);
      OSVirtualHardDisk OSVHD  = role.OSVirtualHardDisk();
      assertEquals(volume1.getId(), OSVHD.diskName());
      assertTrue(volume1.isBootDevice());
      assertTrue(volume1.isDurable());
      assertEquals(volume1.getType(), Volume.Type.LOCAL);

      Volume volume2 = volumes.get(1);
      DataVirtualHardDisk  dataVirtualHardDisk = role.dataVirtualHardDisks().get(0);
      assertEquals(volume2.getId(), dataVirtualHardDisk.diskName());
      assertFalse(volume2.isBootDevice());
      assertTrue(volume2.isDurable());
      assertEquals(volume2.getType(), Volume.Type.LOCAL);
      assertEquals(volume2.getDevice(), dataVirtualHardDisk.mediaLink().toString());
      assertEquals(volume2.getSize(), (float)dataVirtualHardDisk.logicalDiskSizeInGB());

      Volume volume3 = volumes.get(2);
      DataVirtualHardDisk  dataVirtualHardDisk2 = role.dataVirtualHardDisks().get(1);
      assertEquals(volume3.getId(), dataVirtualHardDisk2.diskName());
      assertFalse(volume3.isBootDevice());
      assertTrue(volume3.isDurable());
      assertEquals(volume3.getType(), Volume.Type.LOCAL);
      assertEquals(volume3.getDevice(), dataVirtualHardDisk2.mediaLink().toString());
      assertEquals(volume3.getSize(), (float)dataVirtualHardDisk2.logicalDiskSizeInGB());
   }

   private static Role createRole() {
      return Role.create(
            "testVM",
            "PersistentVMRole",
            "bc322bdc-f685-4002-8efb-4c6089bb2588__Image__openSUSE-12-3-for-Windows-Azure",
            URI.create(
                  "https://bahshstorage.blob.core.windows.net/communityimages/community-12-c59cc53c-80c9-48fb-ba81-9ed6fe46eeb9-1.vhd"),
            RoleSizeEnum.EXTRA_SMALL,
            null,
            DataVHD(),
            OSVHD(),
            false
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
}
