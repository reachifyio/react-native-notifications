import { Commands } from './commands/Commands';
import { Platform, AppRegistry } from 'react-native';
import { NotificationChannel } from './interfaces/NotificationChannel';
import {
  backgroundMessageHandler,
  backgroundMessageHandlerVoip,
} from './events/EventsRegistry';

export class NotificationsAndroid {
  constructor(private readonly commands: Commands) {
    AppRegistry.registerHeadlessTask('RNNotificationsHeadlessTask', () => {
      return remoteMessage => {
        if (remoteMessage.data["call-id"] && backgroundMessageHandlerVoip) {
          return backgroundMessageHandlerVoip(remoteMessage);
        } else if (backgroundMessageHandler) {
          return backgroundMessageHandler(remoteMessage);
        }
      }
    });

    return new Proxy(this, {
      get(target, name) {
        if (Platform.OS === 'android') {
          return (target as any)[name];
        } else {
          return () => {};
        }
      }
    });
  }

  /**
  * Refresh FCM token
  */
  public registerRemoteNotifications() {
    this.commands.refreshToken();
  }

  /**
   * setNotificationChannel
   */
  public setNotificationChannel(notificationChannel: NotificationChannel) {
    return this.commands.setNotificationChannel(notificationChannel);
  }

  /**
   * cancelDeliveredNotification
   */
  public cancelDeliveredNotification(identifier: number) {
    return this.commands.cancelDeliveredNotification(identifier);
  }
}
