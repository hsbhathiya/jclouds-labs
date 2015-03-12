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
package org.jclouds.azurecompute.compute.extensions;

import org.jclouds.azurecompute.options.AzureComputeTemplateOptions;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.extensions.internal.BaseSecurityGroupExtensionLiveTest;
import org.jclouds.scriptbuilder.statements.login.AdminAccess;
import org.jclouds.sshj.config.SshjSshClientModule;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

import java.util.Properties;
import java.util.Random;

import static org.jclouds.azurecompute.config.AzureComputeProperties.OPERATION_TIMEOUT;

/**
 * Live test for AzureCompute {@link org.jclouds.compute.extensions.SecurityGroupExtension} implementation.
 */
@Test(groups = "live", singleThreaded = true, testName = "AzureComputeSecurityGroupExtensionLiveTest")
public class AzureComputeSecurityGroupExtensionLiveTest extends BaseSecurityGroupExtensionLiveTest {

   @Override protected Properties setupProperties() {
      Properties overrides =  super.setupProperties();
      overrides.setProperty(OPERATION_TIMEOUT, "" + 2 * 60 * 1000);
      return  overrides;
   }

   public AzureComputeSecurityGroupExtensionLiveTest() {
      provider = "azurecompute";
   }



   @Override public Template getNodeTemplate() {
      final String group = "storage" + (int)(Math.random()*100);

      TemplateBuilder templateBuilder = view.getComputeService().templateBuilder();
      templateBuilder.imageId("b39f27a8b8c64d52b05eac6a62ebad85__Ubuntu-14_04_1-LTS-amd64-server-20150123-en-us-30GB");
      templateBuilder.hardwareId("BASIC_A2");
      templateBuilder.locationId("North Central US");

      // test passing custom options
      AzureComputeTemplateOptions options = new AzureComputeTemplateOptions();//template.getOptions().as(AzureComputeTemplateOptions.class);
      options.inboundPorts(22);
      options.runScript(AdminAccess.standard());
      options.storageAccountName(group);
      Template template = templateBuilder.options(options).build();
      return template;
   }

/*   @Override
   @Test
   public void testAddIpPermission() {
      Properties properties = super.setupProperties();
      properties.setProperty(OPERATION_TIMEOUT, "" + 5 * 60 * 1000);
      super.testAddIpPermission();
   }
*/
   @Override
   protected Iterable<Module> setupModules() {
      return ImmutableSet.of(getLoggingModule(), credentialStoreModule, getSshModule());
   }

   protected Module getSshModule() {
      return new SshjSshClientModule();
   }

}
