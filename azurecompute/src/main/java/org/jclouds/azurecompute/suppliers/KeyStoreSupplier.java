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
package org.jclouds.azurecompute.suppliers;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jclouds.crypto.Crypto;
import org.jclouds.crypto.Pems;
import org.jclouds.domain.Credentials;
import org.jclouds.location.Provider;

import com.google.common.base.Charsets;
import com.google.common.base.Supplier;
import com.google.common.io.ByteSource;

/**
 * TODO this code needs to be completely refactored. It needs to stop using KeyStore of at all possible and definitely
 * the local filesystem. Please look at oauth for examples on how to do this via PEMs.
 */
@Deprecated
@Singleton
public class KeyStoreSupplier implements Supplier<KeyStore> {

   private final Crypto crypto;

   private final Supplier<Credentials> creds;

   @Inject
   KeyStoreSupplier(Crypto crypto, @Provider Supplier<Credentials> creds) {
      this.crypto = crypto;
      this.creds = creds;
   }

   @Override
   public KeyStore get() {
      final Credentials currentCreds = checkNotNull(creds.get(), "credential supplier returned null");
      final String cert = checkNotNull(currentCreds.identity,
              "credential supplier returned null identity (should be cert)");
      final String keyStorePassword = checkNotNull(currentCreds.credential,
              "credential supplier returned null credential (should be keyStorePassword)");
      try {
         final KeyStore keyStore = KeyStore.getInstance("PKCS12");

         final File certFile = new File(checkNotNull(cert));
         if (certFile.isFile()) { // cert is path to pkcs12 file
            final FileInputStream stream = new FileInputStream(certFile);
            try {
               keyStore.load(stream, keyStorePassword.toCharArray());
            } finally {
               stream.close();
            }
         } else { 
            keyStore.load(null);

            // split in private key and certs
            final int privateKeyBeginIdx = cert.indexOf("-----BEGIN PRIVATE KEY");
            final int privateKeyEndIdx = cert.indexOf("-----END PRIVATE KEY");
            // cert is PEM encoded, containing private key and certs
            if (privateKeyBeginIdx != -1 && privateKeyEndIdx != -1) {
               final String pemPrivateKey = cert.substring(privateKeyBeginIdx, privateKeyEndIdx + 26);

               final StringBuilder pemCerts = new StringBuilder();
               int certsBeginIdx = 0;

               do {
                  certsBeginIdx = cert.indexOf("-----BEGIN CERTIFICATE", certsBeginIdx);

                  if (certsBeginIdx >= 0) {
                     final int certsEndIdx = cert.indexOf("-----END CERTIFICATE", certsBeginIdx) + 26;
                     pemCerts.append(cert.substring(certsBeginIdx, certsEndIdx));
                     certsBeginIdx = certsEndIdx;
                  }
               } while (certsBeginIdx != -1);

               // parse private key
               final KeySpec keySpec = Pems.privateKeySpec(ByteSource.wrap(pemPrivateKey.getBytes(Charsets.UTF_8)));
               final PrivateKey privateKey = crypto.rsaKeyFactory().generatePrivate(keySpec);

               // populate keystore with private key and certs
               final CertificateFactory cf = CertificateFactory.getInstance("X.509");
               @SuppressWarnings("unchecked")
               final Collection<Certificate> certs = (Collection<Certificate>) cf.generateCertificates(
                       new ByteArrayInputStream(pemCerts.toString().getBytes(Charsets.UTF_8)));
               keyStore.setKeyEntry("dummy", privateKey, keyStorePassword.toCharArray(),
                       certs.toArray(new java.security.cert.Certificate[0]));
            }
         }
         return keyStore;
      } catch (NoSuchAlgorithmException e) {
         throw propagate(e);
      } catch (KeyStoreException e) {
         throw propagate(e);
      } catch (CertificateException e) {
         throw propagate(e);
      } catch (FileNotFoundException e) {
         throw propagate(e);
      } catch (IOException e) {
         throw propagate(e);
      } catch (InvalidKeySpecException e) {
         throw propagate(e);
      }
   }
}
