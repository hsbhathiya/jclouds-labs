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
package org.jclouds.azurecompute.compute.strategy.impl;

import com.google.common.base.Function;
import java.util.Map;
import javax.inject.Inject;
import org.jclouds.azurecompute.compute.NewAzureComputeServiceAdapter;
import org.jclouds.azurecompute.domain.*;
import org.jclouds.compute.ComputeServiceAdapter;
import org.jclouds.compute.config.ComputeServiceAdapterContextModule.AddDefaultCredentialsToImage;
import org.jclouds.compute.domain.Image;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.strategy.PrioritizeCredentialsFromTemplate;
import org.jclouds.compute.strategy.impl.AdaptingComputeServiceStrategies;
import org.jclouds.domain.Credentials;

public class AzureAdaptingComputeServiceStrategies
        extends AdaptingComputeServiceStrategies<VirtualMachine, RoleSize, OSImage, Location> {

   private final NewAzureComputeServiceAdapter client;

   private final Function<VirtualMachine, NodeMetadata> nodeMetadataAdapter;

   @Inject
   public AzureAdaptingComputeServiceStrategies(
           final Map<String, Credentials> credentialStore,
           final PrioritizeCredentialsFromTemplate prioritizeCredentialsFromTemplate,
           final ComputeServiceAdapter<VirtualMachine, RoleSize, OSImage, Location> client,
           final Function<VirtualMachine, NodeMetadata> nodeMetadataAdapter,
           final Function<OSImage, Image> imageAdapter,
           final AddDefaultCredentialsToImage addDefaultCredentialsToImage) {

      super(credentialStore,
              prioritizeCredentialsFromTemplate,
              client,
              nodeMetadataAdapter,
              imageAdapter,
              addDefaultCredentialsToImage);

      this.client = (NewAzureComputeServiceAdapter) client;
      this.nodeMetadataAdapter = nodeMetadataAdapter;
   }

   @Override
   public NodeMetadata destroyNode(final String id) {
      VirtualMachine vm = client.getNode(id);
      client.destroyNode(id);
      return vm == null
              ? null
              : nodeMetadataAdapter.apply(vm);
   }

}
