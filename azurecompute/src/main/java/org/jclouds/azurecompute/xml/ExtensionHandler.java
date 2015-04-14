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

import org.jclouds.azurecompute.domain.Disk;
import org.jclouds.azurecompute.domain.Disk.Attachment;
import org.jclouds.azurecompute.domain.Extension;
import org.jclouds.azurecompute.domain.OSImage;
import org.jclouds.http.functions.ParseSax;
import org.xml.sax.Attributes;

import java.net.URI;

import static org.jclouds.util.SaxUtils.currentOrNull;

/**
 * @see <a href="http://msdn.microsoft.com/en-us/library/jj157176" >api</a>
 */
public final class ExtensionHandler extends ParseSax.HandlerForGeneratedRequestWithResult<Extension> {

    private String providerNameSpace;
    private String type;
    private String id;
    private String version;
    private String thumbprint;
    private String thumbprintAlgorithm;
    private String publicConfiguration;
    private boolean isJsonExtension;
    private boolean disallowMajorVersionUpgrade;

    private final StringBuilder currentText = new StringBuilder();

    @Override
    public Extension getResult() {
        Extension result = Extension.create(providerNameSpace, type, id, version, thumbprint, thumbprintAlgorithm, publicConfiguration, isJsonExtension, disallowMajorVersionUpgrade);
        resetState(); // handler is called in a loop.
        return result;
    }

    private void resetState() {
        providerNameSpace = type = id = version = thumbprint = thumbprintAlgorithm = publicConfiguration = null;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
    }

    @Override
    public void endElement(String ignoredUri, String ignoredName, String qName) {
        if (qName.equals("ProviderNameSpace")) {
            providerNameSpace = currentOrNull(currentText);
        } else if (qName.equals("Type")) {
            type = currentOrNull(currentText);
        } else if (qName.equals("Id")) {
            id = currentOrNull(currentText);
        } else if (qName.equals("Version")) {
            version = currentOrNull(currentText);
        } else if (qName.equals("Thumbprint")) {
            thumbprint = currentOrNull(currentText);
        } else if (qName.equals("ThumbprintAlgorithm")) {
            thumbprintAlgorithm = currentOrNull(currentText);
        } else if (qName.equals("PublicConfiguration")) {
            publicConfiguration = currentOrNull(currentText);
        } else if (qName.equals("IsJsonExtension")) {
            String isJsonText = currentOrNull(currentText);
            if (isJsonText != null) {
                isJsonExtension = Boolean.parseBoolean(isJsonText);
            }
        } else if (qName.equals("DisallowMajorVersionUpgrade")) {
            String upgradeText = currentOrNull(currentText);
            if (upgradeText != null) {
                disallowMajorVersionUpgrade = Boolean.parseBoolean(upgradeText);
            }
        }

        currentText.setLength(0);
    }

    @Override
    public void characters(char ch[], int start, int length) {
        currentText.append(ch, start, length);
    }
}
