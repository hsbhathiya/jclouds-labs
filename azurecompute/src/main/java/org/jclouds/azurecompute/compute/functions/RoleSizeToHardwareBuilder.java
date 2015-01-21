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

import org.jclouds.azurecompute.domain.RoleSize;

import com.google.common.base.Function;
import org.jclouds.compute.domain.HardwareBuilder;
import org.jclouds.compute.domain.Processor;

public class RoleSizeToHardwareBuilder implements Function<RoleSize, HardwareBuilder> {

   private static final int MB_TO_GB = 1024;

   @Override
   public HardwareBuilder apply(RoleSize input) {
      return hardware(input.name());
   }

   private HardwareBuilder hardware(String roleSize) {

      if (roleSize.equals("Basic_A0")) {
         return setHardware(1, 1.6, 768);
      } else if (roleSize.equals("Basic_A1")) {
         int ram = (int) (MB_TO_GB * 1.75);
         return setHardware(1, 1.6, ram);
      } else if (roleSize.equals("Basic_A2")) {
         int ram = (int) (MB_TO_GB * 3.5);
         return setHardware(2, 1.6, ram);
      } else if (roleSize.equals("Basic_A3")) {
         int ram = (int) (MB_TO_GB * 7);
         return setHardware(4, 1.6, ram);
      } else if (roleSize.equals("Basic_A4")) {
         int ram = (int) (MB_TO_GB * 14);
         return setHardware(8, 1.6, ram);
      }else if (roleSize.equals("extra small")) {
         return setHardware(1, 1.6, 768);
      }else if (roleSize.equals("small")) {
         int ram = (int) (MB_TO_GB * 1.75);
         return setHardware(1, 1.6, ram);
      }else if (roleSize.equals("medium")) {
         int ram = (int) (MB_TO_GB * 3.5);
         return setHardware(2, 1.6, ram);
      }else if (roleSize.equals("large")) {
         int ram = (int) (MB_TO_GB * 7);
         return setHardware(4, 1.6, ram);
      }else if (roleSize.equals("extra large")) {
         int ram = (int) (MB_TO_GB * 14);
         return setHardware(8, 1.6, ram);
      }else if (roleSize.equals("A5")) {
         int ram = (int) (MB_TO_GB * 14);
         return setHardware(2, 1.6, ram);
      }else if (roleSize.equals("A6")) {
         int ram = (int) (MB_TO_GB * 28);
         return setHardware(4, 1.6, ram);
      }else if (roleSize.equals("A7")) {
         int ram = (int) (MB_TO_GB * 56);
         return setHardware(8, 1.6, ram);
      }else if (roleSize.equals("A8")) {
         int ram = (int) (MB_TO_GB * 56);
         return setHardware(8, 1.6, ram);
      }else if (roleSize.equals("A9")) {
         int ram = (int) (MB_TO_GB * 112);
         return setHardware(16, 1.6, ram);
      }
      return null;
   }

   private HardwareBuilder setHardware(int cores, Double speed, int ram) {
      HardwareBuilder builder = new HardwareBuilder();
      Processor processor = new Processor(cores, speed);
      builder.processor(processor);

      builder.ram(ram);
      return builder;
   }
}
