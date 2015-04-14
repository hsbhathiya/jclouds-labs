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
package org.jclouds.azurecompute.features;

import org.jclouds.azurecompute.binders.ExtensionParamsToXML;
import org.jclouds.azurecompute.domain.Extension;
import org.jclouds.azurecompute.domain.ExtensionParams;
import org.jclouds.azurecompute.functions.Base64EncodeLabel;
import org.jclouds.azurecompute.functions.ParseRequestIdHeader;
import org.jclouds.azurecompute.xml.ListExtensionHandler;
import org.jclouds.azurecompute.xml.ExtensionHandler;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.rest.annotations.*;

import javax.inject.Named;
import javax.ws.rs.*;

import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static org.jclouds.Fallbacks.EmptyListOnNotFoundOr404;
import static org.jclouds.Fallbacks.NullOnNotFoundOr404;

/**
 * The Service Management API includes operations for managing the cloud services beneath your subscription.
 *
 * @see <a href="http://msdn.microsoft.com/en-us/library/ee460812">docs</a>
 */
@Path("/services")
@Headers(keys = "x-ms-version", values = "{jclouds.api-version}")
@Consumes(APPLICATION_XML)
public interface ExtensionApi {

    @Named("ListAvailableExtensions")
    @GET
    @Path("/extensions")
    @XMLResponseParser(ListExtensionHandler.class)
    @Fallback(EmptyListOnNotFoundOr404.class)
    List<Extension> list();


    @Named("ListExtensions")
    @GET
    @Path("/hostedservices/{cloudService}/extensions")
    @XMLResponseParser(ListExtensionHandler.class)
    @Fallback(EmptyListOnNotFoundOr404.class)
    List<Extension> list(@PathParam("cloudService") String cloudService);

    @Named("GetExtension")
    @GET
    @Path("/extensions/{cloudService}/extensions/{extensionId}")
    @XMLResponseParser(ExtensionHandler.class)
    @Fallback(NullOnNotFoundOr404.class)
    @Nullable
    Extension get(@PathParam("cloudService") String cloudService, @PathParam("extensionId") String extensionId);

    @Named("DeleteExtension")
    @DELETE
    @Path("/extensions/{cloudService}/extensions/{extensionId}")
    @XMLResponseParser(ExtensionHandler.class)
    @Fallback(NullOnNotFoundOr404.class)
    @ResponseParser(ParseRequestIdHeader.class)
    String delete(@PathParam("cloudService") String cloudService, @PathParam("extensionId") String extensionId);

    @Named("AddExtension")
    @POST
    @Path("/hostedservices/{cloudService}/extensions")
    @Produces(APPLICATION_XML)
    @ResponseParser(ParseRequestIdHeader.class)
    String add(@PathParam("cloudService") String cloudService, @BinderParam(ExtensionParamsToXML.class) ExtensionParams extensionParams);

}
