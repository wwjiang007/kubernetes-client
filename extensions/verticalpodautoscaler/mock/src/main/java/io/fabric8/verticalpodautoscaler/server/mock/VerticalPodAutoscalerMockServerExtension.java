/**
 * Copyright (C) 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.fabric8.verticalpodautoscaler.server.mock;


import io.fabric8.verticalpodautoscaler.client.VerticalPodAutoscalerClient;
import io.fabric8.verticalpodautoscaler.client.NamespacedVerticalPodAutoscalerClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesCrudDispatcher;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServerExtension;
import io.fabric8.mockwebserver.Context;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;

/**
 * The class that implements JUnit5 extension mechanism. You can use it directly in your JUnit test
 * by annotating it with <code>@ExtendWith(VerticalPodAutoscalerMockServerExtension.class)</code> or through
 * <code>@EnableVerticalPodAutoscalerMockClient</code> annotation
 */
public class VerticalPodAutoscalerMockServerExtension extends KubernetesMockServerExtension {
  private VerticalPodAutoscalerMockServer verticalPodAutoscalerMockServer;
  private NamespacedVerticalPodAutoscalerClient verticalPodAutoscalerClient;

  @Override
  protected void destroy() {
    verticalPodAutoscalerMockServer.destroy();
    verticalPodAutoscalerClient.close();
  }

  @Override
  protected Class<?> getClientType() {
    return VerticalPodAutoscalerClient.class;
  }

  @Override
  protected Class<?> getKubernetesMockServerType() {
    return VerticalPodAutoscalerMockServer.class;
  }

  @Override
  protected void initializeKubernetesClientAndMockServer(Class<?> testClass) {
    EnableVerticalPodAutoscalerMockClient a = testClass.getAnnotation(EnableVerticalPodAutoscalerMockClient.class);
    verticalPodAutoscalerMockServer = a.crud()
      ? new VerticalPodAutoscalerMockServer(new Context(), new MockWebServer(), new HashMap<>(), new KubernetesCrudDispatcher(Collections.emptyList()), a.https())
      : new VerticalPodAutoscalerMockServer(a.https());
    verticalPodAutoscalerMockServer.init();
    verticalPodAutoscalerClient = verticalPodAutoscalerMockServer.createVerticalPodAutoscaler();
  }

  @Override
  protected void setFieldIfKubernetesClientOrMockServer(ExtensionContext context, boolean isStatic, Field field) throws IllegalAccessException {
    setFieldIfEqualsToProvidedType(context, isStatic, field, getClientType(), (i, f) -> f.set(i, verticalPodAutoscalerClient));
    setFieldIfEqualsToProvidedType(context, isStatic, field, getKubernetesMockServerType(), (i, f) -> f.set(i, verticalPodAutoscalerMockServer));
  }
}
