package ai.startree.thirdeye.notification;

import ai.startree.thirdeye.notification.email.EmailNotificationServiceFactory;
import ai.startree.thirdeye.notification.webhook.WebhookNotificationServiceFactory;
import ai.startree.thirdeye.spi.Plugin;
import ai.startree.thirdeye.spi.notification.NotificationServiceFactory;
import java.util.Arrays;

public class DefaultNotificationsPlugin implements Plugin {

  @Override
  public Iterable<NotificationServiceFactory> getNotificationServiceFactories() {
    return Arrays.asList(
        new WebhookNotificationServiceFactory(),
        new EmailNotificationServiceFactory()
    );
  }
}