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

import org.jclouds.azurecompute.domain.RoleSize;
import org.jclouds.http.functions.BaseHandlerTest;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.List;

@Test(groups = "unit", testName = "RoleSizesHandlerTest")
public class ListRoleSizeHandlerTest extends BaseHandlerTest {

   public void test() {
      InputStream is = getClass().getResourceAsStream("/rolesizes.xml");
      List<RoleSize> result = factory.create(new ListRoleSizesHandler()).parse(is);
      //assertEquals(result, expected());
   }

   public static List<RoleSize> expected() {
      return null;
     /* return ImmutableList.of(
            RoleSize.create(
                  RoleSize.Type.BASIC_A2,
                  "Basic A2",
                  2,
                  (int) (3.5 * 1024),
                  Boolean.FALSE,
                  Boolean.TRUE,
                  4,
                  0,
                  127 * 1024
            ),
            RoleSize.create(
                  RoleSize.Type.EXTRALARGE,
                  "A4/extra large",
                  8,
                  (int) (14 * 1024),
                  Boolean.TRUE,
                  Boolean.TRUE,
                  16,
                  2039 * 1024,
                  605 * 1024
            )
      );*/

   }
}
