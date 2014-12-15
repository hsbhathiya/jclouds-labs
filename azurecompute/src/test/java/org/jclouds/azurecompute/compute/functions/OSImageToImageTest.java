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

import static org.testng.Assert.assertEquals;

import java.net.URI;
import java.util.Arrays;

import org.jclouds.azurecompute.domain.OSImage;
import org.testng.annotations.Test;

@Test(groups = "unit", testName = "OSImageToImageTest")
public class OSImageToImageTest {
   public void testArbitraryImageName() {
      OSImageToImage imageToImage = new OSImageToImage();
      OSImage oSImage = createOSImage("openSUSE-12-3-for-Windows-Azure");
      org.jclouds.compute.domain.Image transformed = imageToImage.apply(oSImage);
      assertEquals(transformed.getName(), oSImage.name());
      assertEquals(transformed.getId(), oSImage.label());
      assertEquals(transformed.getProviderId(), oSImage.publisherName());
      assertEquals(transformed.getOperatingSystem().getFamily().toString(), oSImage.os().toString().toLowerCase());
   }

   private static OSImage createOSImage(String name) {
      return OSImage.create(name, Arrays.asList("East Asia", "West US", "Central US"), null, "OpenSUSE-Azure123",
            "Test-data", "MSDN", OSImage.Type.WINDOWS, "SUSE",
            URI.create("http://example.blob.core.windows.net/disks/myimage.vhd"), 30,
            Arrays.asList("http://www.ubuntu.com/project/about-ubuntu/licensing"));
   }
}
