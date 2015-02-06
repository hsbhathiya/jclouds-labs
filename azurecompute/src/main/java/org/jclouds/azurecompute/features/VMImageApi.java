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

import org.jclouds.azurecompute.binders.VMImageParamsToXML;
import org.jclouds.azurecompute.domain.VMImage;
import org.jclouds.azurecompute.domain.VMImageParams;
import org.jclouds.azurecompute.functions.ParseRequestIdHeader;
import org.jclouds.azurecompute.xml.ListVMImagesHandler;
import org.jclouds.rest.annotations.*;

import javax.inject.Named;
import javax.ws.rs.*;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static org.jclouds.Fallbacks.EmptyListOnNotFoundOr404;
import static org.jclouds.Fallbacks.NullOnNotFoundOr404;

/**
 * The Service Management API includes operations for managing the VM Images in your subscription.
 *
 * @see <a href="https://msdn.microsoft.com/en-us/library/azure/dn499771.aspx">docs</a>
 */
@Path("/services/vmimages")
@Headers(keys = "x-ms-version", values = "{jclouds.api-version}")
@Consumes(APPLICATION_XML)
public interface VMImageApi {

   /**
    * The List VM Images operation retrieves a list of the VM Images from the image repository that is associated with
    * the specified subscription.
    */
   @Named("ListVMImages")
   @GET
   @XMLResponseParser(ListVMImagesHandler.class)
   @Fallback(EmptyListOnNotFoundOr404.class) List<VMImage> list();

   /**
    * The Create VM Image operation creates a VM Image in the image repository that is associated with the specified
    * subscription using a specified set of virtual hard disks.
    */
   @Named("CreateVMImage")
   @POST
   @Produces(APPLICATION_XML)
   @ResponseParser(ParseRequestIdHeader.class)
   String create(@BinderParam(VMImageParamsToXML.class) VMImageParams params);

   /**
    * The Create VM Image operation creates a VM Image in the image repository that is associated with the specified
    * subscription using a specified set of virtual hard disks.
    */
   @Named("UpdateVMImage")
   @PUT
   @Path("/{imageName}")
   @Produces(APPLICATION_XML)
   @ResponseParser(ParseRequestIdHeader.class)
   String update(String imageName, @BinderParam(VMImageParamsToXML.class) VMImageParams params);

   /**
    * The Delete VM Image operation deletes the specified VM Image from the image repository that is associated with
    * the specified subscription.
    */
   @Named("DeleteImage")
   @DELETE
   @Path("/{imageName}")
   @Fallback(NullOnNotFoundOr404.class)
   @ResponseParser(ParseRequestIdHeader.class) String delete(@PathParam("imageName") String imageName);



}
