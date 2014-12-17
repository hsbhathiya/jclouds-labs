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

import org.jclouds.azurecompute.domain.RoleInstance;
import org.jclouds.http.functions.ParseSax;

import static org.jclouds.util.SaxUtils.currentOrNull;

/**
 * @see <a href="http://msdn.microsoft.com/en-us/library/jj157176" >api</a>
 */
final class PublicIPHandler extends ParseSax.HandlerForGeneratedRequestWithResult<RoleInstance.PublicIP> {

   private String name;
   private Integer idleTimeoutInMinutes;
   private final StringBuilder currentText = new StringBuilder();

   @Override public RoleInstance.PublicIP getResult() {
      RoleInstance.PublicIP result = RoleInstance.PublicIP.create(name, idleTimeoutInMinutes);
      return result;
   }

   @Override public void endElement(String ignoredUri, String ignoredName, String qName) {
      if (qName.equals("Name")) {
         name = currentOrNull(currentText);
      } else if (qName.equals("IdleTimeoutInMinutes")) {
         String idleTimeOutText = currentOrNull(currentText);
         if (idleTimeOutText != null) {
            idleTimeoutInMinutes = Integer.parseInt(idleTimeOutText);
         }
      }
      currentText.setLength(0);
   }

   @Override public void characters(char ch[], int start, int length) {
      currentText.append(ch, start, length);
   }
}
