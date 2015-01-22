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

import org.jclouds.azurecompute.domain.RoleSizeEnum;

import com.google.common.base.Function;
import org.jclouds.compute.domain.HardwareBuilder;
import org.jclouds.compute.domain.Processor;

public class RoleSizeToHardwareBuilder implements Function<RoleSizeEnum, HardwareBuilder> {

   private static final int MB_TO_GB = 1024;

   @Override
   public HardwareBuilder apply(RoleSizeEnum input) {
      return hardware(input);
   }

   private HardwareBuilder hardware(RoleSizeEnum roleSize) {

      if (roleSize.equals(RoleSizeEnum.Basic_A0)) {
         return setHardware(1, 1.6, 768);
      } else if (roleSize.equals(RoleSizeEnum.Basic_A1)) {
         int ram = (int) (MB_TO_GB * 1.75);
         return setHardware(1, 1.6, ram);
      } else if (roleSize.equals(RoleSizeEnum.Basic_A2)) {
         int ram = (int) (MB_TO_GB * 3.5);
         return setHardware(2, 1.6, ram);
      } else if (roleSize.equals(RoleSizeEnum.Basic_A3)) {
         int ram = (int) (MB_TO_GB * 7);
         return setHardware(4, 1.6, ram);
      } else if (roleSize.equals(RoleSizeEnum.Basic_A4)) {
         int ram = (int) (MB_TO_GB * 14);
         return setHardware(8, 1.6, ram);
      }else if (roleSize.equals(RoleSizeEnum.EXTRA_SMALL)) {
         return setHardware(1, 1.6, 768);
      }else if (roleSize.equals(RoleSizeEnum.SMALL)) {
         int ram = (int) (MB_TO_GB * 1.75);
         return setHardware(1, 1.6, ram);
      }else if (roleSize.equals(RoleSizeEnum.MEDIUM)) {
         int ram = (int) (MB_TO_GB * 3.5);
         return setHardware(2, 1.6, ram);
      }else if (roleSize.equals(RoleSizeEnum.LARGE)) {
         int ram = (int) (MB_TO_GB * 7);
         return setHardware(4, 1.6, ram);
      }else if (roleSize.equals(RoleSizeEnum.EXTRA_LARGE)) {
         int ram = (int) (MB_TO_GB * 14);
         return setHardware(8, 1.6, ram);
      }else if (roleSize.equals(RoleSizeEnum.A5)) {
         int ram = (int) (MB_TO_GB * 14);
         return setHardware(2, 1.6, ram);
      }else if (roleSize.equals(RoleSizeEnum.A6)) {
         int ram = (int) (MB_TO_GB * 28);
         return setHardware(4, 1.6, ram);
      }else if (roleSize.equals(RoleSizeEnum.A7)) {
         int ram = (int) (MB_TO_GB * 56);
         return setHardware(8, 1.6, ram);
      }else if (roleSize.equals(RoleSizeEnum.A8)) {
         int ram = (int) (MB_TO_GB * 56);
         return setHardware(8, 1.6, ram);
      }else if (roleSize.equals(RoleSizeEnum.A9)) {
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
