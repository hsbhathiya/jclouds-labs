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
import org.jclouds.azurecompute.domain.DataVirtualHardDisk;
import org.jclouds.azurecompute.domain.OSVirtualHardDisk;
import org.jclouds.azurecompute.domain.Role;
import org.jclouds.compute.domain.*;

import java.util.ArrayList;
import java.util.List;

public class RoleToHardware implements Function<Role, Hardware> {

   @Override
   public Hardware apply(Role input) {
      return new RoleSizeToHardwareBuilder().apply(input.roleSize())
            .id(input.roleName())
            .providerId(input.mediaLocation().toString())
            .name(input.roleName())
            .hypervisor("Azure Hypervisor")
            .uri(input.mediaLocation())
            .volumes(collectVolumes(input))
            .supportsImage(Predicates.<Image>alwaysTrue())
            .build();
   }

   private List<Volume> collectVolumes(Role input) {

      List<Volume> volumes = new ArrayList<Volume>();

      OSVirtualHardDisk OSVHD = input.OSVirtualHardDisk();
      Volume OSVolume = new VolumeBuilder()
            .id(OSVHD.diskName())
            .bootDevice(Boolean.TRUE)
            .type(Volume.Type.LOCAL)
            .durable(Boolean.TRUE)
            .device(OSVHD.remoteSourceImageLink().toString())
            .build();

      volumes.add(OSVolume);

      List<DataVirtualHardDisk> dataVirtualHardDisks = input.dataVirtualHardDisks();

      for (DataVirtualHardDisk disk : dataVirtualHardDisks) {
         Volume dataVolume = new VolumeBuilder().bootDevice(Boolean.TRUE).id(disk.diskName())
               .type(Volume.Type.LOCAL).durable(Boolean.TRUE).device(disk.mediaLink().toString()).build();
         volumes.add(dataVolume);
      }
      return volumes;
   }
}
