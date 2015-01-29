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
package org.jclouds.azurecompute.compute;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Singleton;
import javax.inject.Inject;

import com.google.common.collect.Lists;
import org.jclouds.azurecompute.AzureComputeApi;

import org.jclouds.azurecompute.domain.*;
import org.jclouds.compute.ComputeServiceAdapter;
import org.jclouds.compute.domain.Image;
import org.jclouds.compute.domain.OsFamily;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.options.TemplateOptions;

import java.util.Iterator;
import java.util.List;

@Singleton
public final class AzureComputeServiceAdapter implements ComputeServiceAdapter<Role, RoleSize, OSImage, String> {

   private static final String SERVICE_NAME = "";
   private static final String DEPLOYMENT_NAME = "";

   private final AzureComputeApi api;

   @Inject AzureComputeServiceAdapter(AzureComputeApi api) {
      this.api = api;
   }

   @Override
   public NodeAndInitialCredentials<Role> createNodeWithGroupEncodedIntoName(
         String group, String name, Template template) {

      checkNotNull(template.getImage().getUri(), "image URI is null");
      checkNotNull(template.getHardware().getUri(), "hardware must have a uri");

      TemplateOptions options = template.getOptions();
      Image image = template.getImage();
      OsFamily osFamily = image.getOperatingSystem().getFamily();

      OSImage.Type  os;
      if(osFamily == OsFamily.WINDOWS){
         os = OSImage.Type.WINDOWS;
      }else{
         os = OSImage.Type.LINUX;
      }


      DeploymentParams deploymentParams = DeploymentParams.builder().name(DEPLOYMENT_NAME)
            .mediaLink(image.getUri()).os(os).build();

      return null;
   }

   @Override
   public Iterable<RoleSize> listHardwareProfiles() {
      return api.getRoleSizesApi().list();
   }

   @Override
   public Iterable<OSImage> listImages() {
      return api.getOSImageApi().list();
   }

   @Override
   public OSImage getImage(String id) {
      Iterable<OSImage> images = listImages();
      Iterator<OSImage> iterator = images.iterator();
      while (iterator.hasNext()) {
         OSImage osImage = iterator.next();
         if (id.equals(osImage.name())) {
            return osImage;
         }
      }
      return null;
   }

   @Override
   public Iterable<String> listLocations() {
      List<Location> locations = api.getLocationApi().list();
      List<String> locationsName = Lists.newArrayList();
      for (int i = 0; i < locations.size(); i++) {
         locationsName.add(locations.get(i).name());
      }
      return locationsName;
   }

   @Override
   public Role getNode(String id) {
      return null;//api.getVirtualMachineApiForDeploymentInService(DEPLOYMENT_NAME, SERVICE_NAME).get(id);
   }

   @Override
   public void destroyNode(String id) {
      api.getVirtualMachineApiForDeploymentInService(DEPLOYMENT_NAME, SERVICE_NAME).shutdown(id);
   }

   @Override
   public void rebootNode(String id) {
      api.getVirtualMachineApiForDeploymentInService(DEPLOYMENT_NAME, SERVICE_NAME).restart(id);
   }

   @Override
   public void resumeNode(String id) {
      api.getVirtualMachineApiForDeploymentInService(DEPLOYMENT_NAME, SERVICE_NAME).start(id);
   }

   @Override
   public void suspendNode(String id) {
      api.getVirtualMachineApiForDeploymentInService(DEPLOYMENT_NAME, SERVICE_NAME).shutdown(id);
   }

   @Override
   public Iterable<Role> listNodes() {
      List<CloudService> cloudServices = api.getCloudServiceApi().list();
      for (CloudService service : cloudServices) {
      }
      throw new UnsupportedOperationException("listNodes is not implemented");
      /*
      * Implement Get Cloud Service Properties
      * http://msdn.microsoft.com/en-us/library/azure/ee460806.aspx
      */
   }

   @Override
   public Iterable<Role> listNodesByIds(Iterable<String> ids) {
      List<Deployment> deployments = Lists.newArrayList();
      for (String id : ids) {
         Deployment deployment = api.getDeploymentApiForService(SERVICE_NAME).get(id);
         deployments.add(deployment);
      }
      return  null; //deployments;
   }
}
