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
package org.jclouds.azurecompute.xml;

import com.google.common.collect.ImmutableList;
import org.jclouds.azurecompute.domain.RoleInstance;
import org.jclouds.azurecompute.domain.RoleInstance.PowerState;
import org.jclouds.azurecompute.domain.RoleInstance.InstanceStatus;
import org.jclouds.azurecompute.domain.RoleInstance.PublicIP;
import org.jclouds.azurecompute.domain.RoleInstance.InstanceEndpoint;
import org.jclouds.http.functions.ParseSax;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static org.jclouds.util.SaxUtils.currentOrNull;

/**
 * @see <a href="http://msdn.microsoft.com/en-us/library/jj157176" >api</a>
 */
final class RoleInstanceHandler extends ParseSax.HandlerForGeneratedRequestWithResult<RoleInstance> {

   private String roleName;
   private String instanceName;
   private InstanceStatus instanceStatus;
   private String instanceSize;
   private String instanceState;
   private String instanceErrorCode;
   private String ipAddress;
   private PowerState powerState;
   private String hostName;

   private final ImmutableList.Builder<InstanceEndpoint> instanceEndpoints = ImmutableList.builder();
   private final ImmutableList.Builder<PublicIP> publicIPs = ImmutableList.builder();

   private boolean inPublicIP;
   private boolean inInstanceEndpoint;

   private final PublicIPHandler publicIPHandler = new PublicIPHandler();
   private final InstanceEndpointHandler instanceEndpointHandler = new InstanceEndpointHandler();

   private final StringBuilder currentText = new StringBuilder();

   @Override public RoleInstance getResult() {
      RoleInstance result = RoleInstance.create(roleName, instanceName, instanceStatus, instanceErrorCode,
            instanceSize, instanceState, ipAddress, instanceEndpoints.build(), powerState, hostName, publicIPs.build()
      );
      return result;
   }

   @Override public void startElement(String uri, String name, String qName, Attributes attributes)
         throws SAXException {
      if (qName.equals("InstanceEndpoint")) {
         inInstanceEndpoint = true;
      }
      if (inInstanceEndpoint) {
         instanceEndpointHandler.startElement(uri, name, qName, attributes);
      }

      if (qName.equals("PublicIP")) {
         inPublicIP = true;
      }

      if (inPublicIP) {
         publicIPHandler.startElement(uri, name, qName, attributes);
      }
   }

   @Override public void endElement(String ignoredUri, String ignoredName, String qName) {

      if (qName.equals("RoleName")) {
         roleName = currentOrNull(currentText);
      } else if (qName.equals("InstanceName")) {
         instanceName = currentOrNull(currentText);
      } else if (qName.equals("InstanceStatus")) {
         String instanceStatusText = currentOrNull(currentText);
         if (instanceStatusText != null) {
            instanceStatus = parseInstanceStatus(instanceStatusText);
         }
      } else if (qName.equals("InstanceSize")) {
         instanceSize = currentOrNull(currentText);
      } else if (qName.equals("InstanceErrorCode")) {
         instanceErrorCode = currentOrNull(currentText);
      } else if (qName.equals("IpAddress")) {
         ipAddress = currentOrNull(currentText);
      } else if (qName.equals("InstanceSize")) {
         instanceSize = currentOrNull(currentText);
      } else if (qName.equals("InstanceStateDetails")) {
         instanceState = currentOrNull(currentText);
      } else if (qName.equals("InstanceEndpoint")) {
         instanceEndpoints.add(instanceEndpointHandler.getResult());
         inInstanceEndpoint = false;
      } else if (inInstanceEndpoint) {
         instanceEndpointHandler.endElement(ignoredUri, ignoredName, qName);
      } else if (qName.equals("PowerState")) {
         String powerStateText = currentOrNull(currentText);
         if (powerStateText != null) {
            powerState = parsePowerState(powerStateText);
         }
      } else if (qName.equals("HostName")) {
         hostName = currentOrNull(currentText);
      } else if (qName.equals("PublicIP")) {
         publicIPs.add(publicIPHandler.getResult());
         inPublicIP = false;
      } else if (inPublicIP) {
         publicIPHandler.endElement(ignoredUri, ignoredName, qName);
      }
      currentText.setLength(0);
   }

   @Override public void characters(char ch[], int start, int length) {
      if (inPublicIP) {
         publicIPHandler.characters(ch, start, length);
      } else if (inInstanceEndpoint) {
         instanceEndpointHandler.characters(ch, start, length);
      } else {
         currentText.append(ch, start, length);
      }
   }

   private static InstanceStatus parseInstanceStatus(String instanceStatus) {
      try {
         return InstanceStatus.valueOf(UPPER_CAMEL.to(UPPER_UNDERSCORE, instanceStatus));
      } catch (IllegalArgumentException e) {
         return InstanceStatus.UNRECOGNIZED;
      }
   }

   private static PowerState parsePowerState(String powerState) {
      try {
         return PowerState.valueOf(powerState);
      } catch (IllegalArgumentException e) {
         return PowerState.UNRECOGNIZED;
      }
   }
}
