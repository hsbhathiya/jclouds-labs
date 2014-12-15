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

import org.jclouds.azurecompute.domain.OSImage;
import org.jclouds.compute.domain.Image;

import com.google.common.base.Function;
import org.jclouds.compute.domain.ImageBuilder;
import org.jclouds.compute.domain.OperatingSystem;
import org.jclouds.compute.domain.OsFamily;

public class OSImageToImage implements Function<OSImage, Image> {

   @Override
   public Image apply(OSImage image) {

      // @TODO add location support
      ImageBuilder builder = new ImageBuilder()
            .id(image.label())
            .name(image.name())
            .description(image.description())
            .status(Image.Status.AVAILABLE)
            .uri(image.mediaLink())
            .providerId(image.publisherName());

      OperatingSystem.Builder osBuilder = setOperatingSystem(image);

      return builder.operatingSystem(osBuilder.build()).build();
   }

   private OperatingSystem.Builder setOperatingSystem(OSImage image) {
      OsFamily family;
      if (image.os() == OSImage.Type.WINDOWS) {
         return OperatingSystem.builder().family(OsFamily.WINDOWS).is64Bit(true).description(image.name());
      }
      return OperatingSystem.builder().family(OsFamily.LINUX).is64Bit(true).description(image.name());
   }
}
