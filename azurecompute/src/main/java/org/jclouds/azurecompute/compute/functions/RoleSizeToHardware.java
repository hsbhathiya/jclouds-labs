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

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import org.jclouds.azurecompute.domain.*;
import org.jclouds.compute.domain.*;

import java.util.ArrayList;
import java.util.List;

public class RoleSizeToHardware implements Function<RoleSize, Hardware> {

   private static String HYPERVISOR = "Azure Hypervisor";

   @Override
   public Hardware apply(RoleSize input) {

      int cores = input.cores();
      double speed = speed(input.name());
      Processor processor = new Processor(cores, speed);

      return new HardwareBuilder()
            .id(input.name().toString())
            .name(input.label())
            .processor(processor)
            .ram(input.memoryInMB())
            .hypervisor(HYPERVISOR)
            .volumes(collectVolumes(input))
            .supportsImage(Predicates.<Image>alwaysTrue())
            .build();
   }

   private List<Volume> collectVolumes(RoleSize input) {
      List<Volume> volumes = new ArrayList<Volume>();

      Volume osDisk = new VolumeBuilder()
            .bootDevice(Boolean.TRUE)
            .type(Volume.Type.LOCAL)
            .durable(Boolean.TRUE)
            .size((float) 127 * 1024)
            .build();

      volumes.add(osDisk);

      float dataDiskSize = input.virtualMachineResourceDiskSizeInMb();
      Volume dataDisk = new VolumeBuilder()
            .bootDevice(Boolean.FALSE)
            .type(Volume.Type.LOCAL)
            .durable(Boolean.FALSE)
            .size(dataDiskSize)
            .build();

      volumes.add(dataDisk);
      return volumes;
   }

   private double speed(RoleSizeName roleSizeName) {
      if (roleSizeName.equals(RoleSizeName.A8) || roleSizeName.equals(RoleSizeName.A9)) {
         return 2.6;
      } else {
         return 1.6;
      }
   }
}
