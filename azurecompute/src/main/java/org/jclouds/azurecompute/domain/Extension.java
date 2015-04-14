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
import org.jclouds.javax.annotation.Nullable;

import java.net.URI;

/**
 * A disk in the image repository.
 *
 * @see <a href="http://msdn.microsoft.com/en-us/library/jj157176" >api</a>
 */
@AutoValue
public abstract class Extension {

    Extension() {
    } // For AutoValue only!

    public abstract String providerNameSpace();

    public abstract String type();

    public abstract String id();

    public abstract String version();

    public abstract String thumbprint();

    public abstract String thumbprintAlgorithm();

    public abstract String publicConfiguration();

    public abstract boolean isJsonExtension();

    public abstract boolean disallowMajorVersionUpgrade();

    public static Extension create(final String providerNameSpace, final String type, final String id,
                                   final String version, final String thumbprint,
                                   final String thumbprintAlgorithm, final String publicConfiguration, final boolean isJsonExtension, final boolean disallowMajorVersionUpgrade) {

        return new AutoValue_Extension(providerNameSpace, type, id, version, thumbprint, thumbprintAlgorithm, publicConfiguration, isJsonExtension, disallowMajorVersionUpgrade);
    }
}
