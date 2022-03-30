/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification.webhook;

import ai.startree.thirdeye.spi.Plugin;
import ai.startree.thirdeye.spi.notification.NotificationServiceFactory;
import com.google.auto.service.AutoService;
import java.util.Arrays;

@AutoService(Plugin.class)
public class WebhookNotificationPlugin implements Plugin {

  @Override
  public Iterable<NotificationServiceFactory> getNotificationServiceFactories() {
    return Arrays.asList(
        new WebhookNotificationServiceFactory()
    );
  }
}
