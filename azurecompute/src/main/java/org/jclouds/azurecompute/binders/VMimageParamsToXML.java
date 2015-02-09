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
package org.jclouds.azurecompute.binders;

import com.google.common.base.CaseFormat;
import com.jamesmurty.utils.XMLBuilder;
import org.jclouds.azurecompute.domain.VMImageParams;
import org.jclouds.http.HttpRequest;
import org.jclouds.rest.Binder;

import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static com.google.common.base.Throwables.propagate;

public final class VMImageParamsToXML implements Binder {

   @Override public <R extends HttpRequest> R bindToRequest(R request, Object input) {
      VMImageParams params = VMImageParams.class.cast(input);

      try {
         XMLBuilder builder = XMLBuilder.create("VMImage", "http://schemas.microsoft.com/windowsazure")
               .e("Name").t(params.name()).up()
               .e("Label").t(params.label()).up()
               .e("Description").t(params.description()).up();
         //OSConfig
         VMImageParams.OSDiskConfigurationParams osDiskConfig = params.osDiskConfiguration();
         String cache = CaseFormat.UPPER_UNDERSCORE.to(UPPER_CAMEL, osDiskConfig.hostCaching().toString());
         XMLBuilder osConfigBuilder = builder.e("OSDiskConfiguration");
         osConfigBuilder
               .e("HostCaching").t(cache).up()
               .e("OSState").t(osDiskConfig.osState().toString()).up()
               .e("OS").t(osDiskConfig.os().toString()).up()
               .e("MediaLink").t(osDiskConfig.mediaLink().toASCIIString()).up()
               .up(); //OSDiskConfiguration

         builder.up()
               .e("DataDiskConfigurations").up()
               .e("Language").t(params.language()).up()
               .e("ImageFamily").t(params.imageFamily()).up()
               .e("RecommendedVMSize").t(params.recommendedVMSize().toString()).up()
               .e("Eula").t(params.eula()).up()
               .e("IconUri").t(params.iconUri().toASCIIString()).up()
               .e("SmallIconUri").t(params.smallIconUri().toASCIIString()).up()
               .e("PrivacyUri").t(params.privacyUri().toASCIIString()).up()
               .e("showGui").t(params.showGui().toString()).up()
               .up();

         return (R) request.toBuilder().payload(builder.asString()).build();
      } catch (Exception e) {
         throw propagate(e);
      }
   }
}
