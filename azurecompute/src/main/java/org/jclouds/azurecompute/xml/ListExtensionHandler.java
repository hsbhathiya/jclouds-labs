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
import com.google.common.collect.ImmutableList.Builder;
import com.google.inject.Inject;
import org.jclouds.azurecompute.domain.Extension;
import org.jclouds.http.functions.ParseSax;
import org.xml.sax.Attributes;

import java.util.List;

public final class ListExtensionHandler extends ParseSax.HandlerForGeneratedRequestWithResult<List<Extension>> {

   private boolean inExtension;

   private final ExtensionHandler extensionHandler;

   private final Builder<Extension> extensions = ImmutableList.builder();

   @Inject
   ListExtensionHandler(ExtensionHandler extensionHandler) {
      this.extensionHandler = extensionHandler;
   }

   @Override
   public List<Extension> getResult() {
      return extensions.build();
   }

   @Override
   public void startElement(String url, String name, String qName, Attributes attributes) {
      if (qName.equals("Extension")) {
         inExtension = true;
      }
      if (inExtension) {
         extensionHandler.startElement(url, name, qName, attributes);
      }
   }

   @Override
   public void endElement(String uri, String name, String qName) {
      if (qName.equals("Extension")) {
         inExtension = false;
         extensions.add(extensionHandler.getResult());
      } else if (inExtension) {
         extensionHandler.endElement(uri, name, qName);
      }
   }

   @Override
   public void characters(char ch[], int start, int length) {
      if (inExtension) {
         extensionHandler.characters(ch, start, length);
      }
   }
}
