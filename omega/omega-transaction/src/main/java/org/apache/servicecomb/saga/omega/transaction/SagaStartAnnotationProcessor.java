/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.saga.omega.transaction;

import org.apache.servicecomb.saga.omega.context.OmegaContext;

class SagaStartAnnotationProcessor implements EventAwareInterceptor {

  private final OmegaContext omegaContext;
  private final MessageSender sender;

  SagaStartAnnotationProcessor(OmegaContext omegaContext, MessageSender sender) {
    this.omegaContext = omegaContext;
    this.sender = sender;
  }

  @Override
  public void preIntercept(String parentTxId, String compensationMethod, Object... message) {
    String globalTxId = globalTxId();
    // reuse the globalTxId as localTxId to differ localTxId in SagaStartedEvent and the first TxStartedEvent
    sender.send(new SagaStartedEvent(globalTxId, globalTxId));
  }

  @Override
  public void postIntercept(String parentTxId, String compensationMethod) {
    String globalTxId = omegaContext.globalTxId();
    sender.send(new SagaEndedEvent(globalTxId, globalTxId));
    omegaContext.clear();
  }

  @Override
  public void onError(String parentTxId, String compensationMethod, Throwable throwable) {
    String globalTxId = omegaContext.globalTxId();
    sender.send(new TxAbortedEvent(globalTxId, globalTxId, null, compensationMethod, throwable));
    omegaContext.clear();
  }

  private String globalTxId() {
    String globalTxId = omegaContext.newGlobalTxId();
    omegaContext.setLocalTxId(globalTxId);
    return globalTxId;
  }
}
