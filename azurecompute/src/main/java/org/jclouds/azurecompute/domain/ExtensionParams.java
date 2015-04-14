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
package org.jclouds.azurecompute.domain;

import com.google.auto.value.AutoValue;

import java.net.URI;
import java.util.List;

import static com.google.common.collect.ImmutableList.copyOf;

@AutoValue
public abstract class ExtensionParams {

    public abstract String providerNameSpace();

    public abstract String type();

    public abstract String id();

    public abstract String thumbprint();

    public abstract String thumbprintAlgorithm();

    public abstract String publicConfiguration();

    public abstract String privateConfiguration();

    public abstract String version();

    public Builder toBuilder() {
        return builder().fromExtensionParams(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String providerNameSpace;
        private String type;
        private String id;
        private String thumbprint;
        private String thumbprintAlgorithm;
        private String publicConfiguration;
        private String privateConfiguration;
        private String version;

        public Builder providerNameSpace(final String providerNameSpace) {
            this.providerNameSpace = providerNameSpace;
            return this;
        }

        public Builder type(final String type) {
            this.type = type;
            return this;
        }

        public Builder id(final String id) {
            this.id = id;
            return this;
        }

        public Builder thumbprint(final String thumbprint) {
            this.thumbprint = thumbprint;
            return this;
        }

        public Builder thumbprintAlgorithm(final String thumbprintAlgorithm) {
            this.thumbprintAlgorithm = thumbprintAlgorithm;
            return this;
        }

        public Builder publicConfiguration(final String publicConfiguration) {
            this.publicConfiguration = publicConfiguration;
            return this;
        }

        public Builder privateConfiguration(final String privateConfiguration) {
            this.privateConfiguration = privateConfiguration;
            return this;
        }

        public Builder version(final String version) {
            this.version = version;
            return this;
        }

        public ExtensionParams build() {
            return ExtensionParams.create(providerNameSpace, type, id, thumbprint, thumbprintAlgorithm, publicConfiguration, privateConfiguration, version);
        }

        public Builder fromExtensionParams(final ExtensionParams extensionParams) {
            return providerNameSpace(extensionParams.providerNameSpace())
                    .type(extensionParams.type())
                    .id(extensionParams.id())
                    .privateConfiguration(extensionParams.privateConfiguration())
                    .publicConfiguration(extensionParams.publicConfiguration())
                    .thumbprint(extensionParams.thumbprint())
                    .thumbprintAlgorithm(extensionParams.thumbprintAlgorithm())
                    .version(extensionParams.version());
        }
    }

    private static ExtensionParams create(String providerNameSpace,
                                          String type,
                                          String id,
                                          String thumbprint,
                                          String thumbprintAlgorithm,
                                          String publicConfiguration,
                                          String privateConfiguration,
                                          String version) {

        return new AutoValue_ExtensionParams(providerNameSpace, type, id, thumbprint, thumbprintAlgorithm, publicConfiguration, privateConfiguration, version);
    }
}
