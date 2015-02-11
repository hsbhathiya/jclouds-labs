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
import org.jclouds.azurecompute.domain.RoleSize;
import org.jclouds.compute.domain.*;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

@Test(groups = "unit", testName = "RoleSizeToHardwareTest")
public class RoleSizeToHardwareTest {
   public void testRoleSizeTransform() {
      RoleSizeToHardware roleSizeToHardware = new RoleSizeToHardware();

      for (RoleSize roleSize : createRoleSizes()) {
         Hardware transformed = roleSizeToHardware.apply(roleSize);

         assertEquals(transformed.getId(), roleSize.name().toString());
         assertEquals(transformed.getName(), roleSize.label());
         assertEquals(transformed.getHypervisor(), "Azure Hypervisor");

         //Processor
         Processor processor = transformed.getProcessors().get(0);
         assertNotNull(processor);
         assertEquals(processor.getCores(), (double) roleSize.cores());
         if (roleSize.name().equals(RoleSize.Type.A8) || roleSize.name().equals(RoleSize.Type.A9)) {
            assertEquals(processor.getSpeed(), 2.6);
         }else{
            assertEquals(processor.getSpeed(), 1.6);
         }

         //Volumes
         List<? extends Volume> volumes = transformed.getVolumes();
         assertEquals(volumes.size(), 2);

         Volume volume1 = volumes.get(0);
         assertTrue(volume1.isBootDevice());
         assertTrue(volume1.isDurable());
         assertEquals(volume1.getType(), Volume.Type.LOCAL);

         Volume volume2 = volumes.get(1);
         assertFalse(volume2.isBootDevice());
         assertFalse(volume2.isDurable());
         assertEquals(volume2.getType(), Volume.Type.LOCAL);
         assertEquals(volume2.getSize(), (float) roleSize.virtualMachineResourceDiskSizeInMb());
      }
   }

   private static ImmutableList<RoleSize> createRoleSizes() {
      return ImmutableList.of(
            RoleSize.create(
                  RoleSize.Type.BASIC_A2,
                  "Basic A2",
                  2,
                  (int) (3.5 * 1024),
                  Boolean.FALSE,
                  Boolean.TRUE,
                  4,
                  0,
                  127 * 1024
            ),
            RoleSize.create(
                  RoleSize.Type.EXTRALARGE,
                  "A4/extra large",
                  8,
                  (int) (14 * 1024),
                  Boolean.TRUE,
                  Boolean.TRUE,
                  16,
                  2039 * 1024,
                  605 * 1024
            ),
            RoleSize.create(
                  RoleSize.Type.A8,
                  "A8",
                  8,
                  (int) (56 * 1024),
                  Boolean.TRUE,
                  Boolean.TRUE,
                  16,
                  (int)1.77*1024*1024,
                  382 * 1024
            )
      );

   }
}
