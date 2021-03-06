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
package org.jclouds.jdbc.repository;

import com.google.inject.Inject;
import com.google.inject.Provider;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;

public abstract class GenericRepository<T, PK extends Serializable> {

   protected final Class<T> entityClass;
   protected final Provider<EntityManager> entityManager;

   @Inject
   @SuppressWarnings("unchecked")
   protected GenericRepository(Provider<EntityManager> entityManager) {
      this.entityManager = entityManager;
      this.entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
   }

   public T create(T entity) {
      entityManager.get().persist(entity);
      return entity;
   }

   public T find(PK id) {
      return entityManager.get().find(entityClass, id);
   }

   public T save(T entity) {
      return entityManager.get().merge(entity);
   }

   public void delete(T entity) {
      entityManager.get().remove(entityManager.get().contains(entity) ? entity : entityManager.get().merge(entity));
   }

}
