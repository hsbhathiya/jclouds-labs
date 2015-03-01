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

import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;

import org.jclouds.azurecompute.domain.VMImage;
import org.jclouds.azurecompute.domain.VMImageParams;
import org.jclouds.azurecompute.domain.OSImage;
import org.jclouds.azurecompute.internal.BaseAzureComputeApiLiveTest;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.List;

@Test(groups = "live", testName = "VMImageApiLiveTest")
public class VMImageApiLiveTest extends BaseAzureComputeApiLiveTest {

   public void testList() {
      List<VMImage> vmImageList = api().list();
      assertTrue(vmImageList.size() > 0);
      for (VMImage VMImage : vmImageList) {
         checkVMImage(VMImage);
      }
   }

   public void testCreate() {
      VMImage image = api().list().get(5);
      VMImageParams.OSDiskConfigurationParams osParams = VMImageParams.OSDiskConfigurationParams
            .OSDiskConfiguration("myImage_os_disk",
                  VMImageParams.OSDiskConfigurationParams.Caching.READ_ONLY,
                  VMImageParams.OSDiskConfigurationParams.OSState.SPECIALIZED,
                  OSImage.Type.LINUX,
                  URI.create(
                        "https://" + storageService.serviceName()
                              + ".blob.core.windows.net/communityimages/community-77355-11c444b5-9758-4488-acdd-db6ec101ab02-2.vhd"),
                  30,
                  "Standard");
      VMImageParams params = VMImageParams.builder().name("MyImage").label("MyImage")
            .description(image.description()).recommendedVMSize(image.recommendedVMSize())
            .osDiskConfiguration(osParams).imageFamily(image.imageFamily())
            .build();

      String requestId = api().create(params);

      assertTrue(operationSucceeded.apply(requestId), requestId);
   }

   private void checkVMImage(VMImage image) {
      assertNotNull(image.label(), "Label cannot be null for " + image);
      assertNotNull(image.name(), "Name cannot be null for " + image);
      assertNotNull(image.location(), "Location cannot be null for " + image);

      //OSImage
      VMImage.OSDiskConfiguration osDiskConfiguration = image.osDiskConfiguration();
      assertNotNull(osDiskConfiguration);
      assertNotNull(osDiskConfiguration.name());
      assertTrue(osDiskConfiguration.logicalSizeInGB() > 0);

      if (osDiskConfiguration.mediaLink() != null) {
         assertTrue(ImmutableSet.of("http", "https").contains(osDiskConfiguration.mediaLink().getScheme()),
               "MediaLink should be an http(s) url" + image);
      }

      if (image.category() != null) {
         assertNotEquals("", image.category().trim(), "Invalid Category for " + image);
      }
   }

   private VMImageApi api() {
      return api.getVMImageApi();
   }

}
