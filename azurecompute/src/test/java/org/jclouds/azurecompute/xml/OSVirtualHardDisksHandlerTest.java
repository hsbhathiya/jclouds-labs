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

import org.jclouds.azurecompute.domain.OSImage;
import org.jclouds.azurecompute.domain.OSVirtualHardDisk;
import org.jclouds.http.functions.BaseHandlerTest;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.net.URI;

import static org.testng.Assert.assertEquals;

@Test(groups = "unit", testName = "OSVirtualHardDiskHandlerTest")
public class OSVirtualHardDisksHandlerTest extends BaseHandlerTest {

   public void test() {
      InputStream is = getClass().getResourceAsStream("/osvirtualharddisk.xml");
      OSVirtualHardDisk result = factory.create(new OSVirtualHardDiskHandler()).parse(is);
      assertEquals(result, expected());
   }

   public static OSVirtualHardDisk expected() {
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
