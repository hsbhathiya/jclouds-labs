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

import org.jclouds.azurecompute.domain.DataVirtualHardDisk;
import org.jclouds.azurecompute.domain.OSVirtualHardDisk;
import org.jclouds.azurecompute.domain.Role;
import org.jclouds.azurecompute.domain.RoleSizeName;
import org.jclouds.azurecompute.domain.OSImage;
import org.jclouds.http.functions.BaseHandlerTest;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

import static org.testng.Assert.assertEquals;

@Test(groups = "unit", testName = "RoleHandlerTest")
public class RoleHandlerTest extends BaseHandlerTest {

   public void test() {
      InputStream is = getClass().getResourceAsStream("/role.xml");
      Role result = factory.create(new RoleHandler()).parse(is);
      assertEquals(result, expected());
   }

   public static Role expected() {
      return Role.create(
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
