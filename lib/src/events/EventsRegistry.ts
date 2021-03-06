import { EmitterSubscription } from 'react-native';
import { NativeEventsReceiver } from '../adapters/NativeEventsReceiver';
import {
  Registered,
  RegistrationError,
  NotificationResponse
} from '../interfaces/NotificationEvents';
import { CompletionCallbackWrapper } from '../adapters/CompletionCallbackWrapper';
import { Notification } from '../DTO/Notification';
import { NotificationCompletion } from '../interfaces/NotificationCompletion';

let backgroundMessageHandler: Function;
let backgroundMessageHandlerVoip: Function;

export class EventsRegistry {
  constructor(
    private nativeEventsReceiver: NativeEventsReceiver,
    private completionCallbackWrapper: CompletionCallbackWrapper) 
  {}

  public registerRemoteNotificationsRegistered(callback: (event: Registered) => void): EmitterSubscription {
    return this.nativeEventsReceiver.registerRemoteNotificationsRegistered(callback);
  }
  
  public registerNotificationReceivedForeground(callback: (notification: Notification, completion: (response: NotificationCompletion) => void) => void): EmitterSubscription {
    return this.nativeEventsReceiver.registerNotificationReceived(this.completionCallbackWrapper.wrapReceivedForegroundCallback(callback));
  }

  public registerNotificationReceivedBackground(callback: (notification: Notification, completion: (response: NotificationCompletion) => void) => void): EmitterSubscription {
    return this.nativeEventsReceiver.registerNotificationReceived(this.completionCallbackWrapper.wrapReceivedBackgroundCallback(callback));
  }

  public registerNotificationReceivedSilent(
    callback: (notification: Notification) => void,
    callbackFromQuit: (notification: Notification) => void,
  ): EmitterSubscription {
    if (callbackFromQuit) {
      backgroundMessageHandler = callbackFromQuit;
    }
    return this.nativeEventsReceiver.registerNotificationReceived(this.completionCallbackWrapper.wrapReceivedSilentCallback(callback));
  }

  public registerNotificationReceivedVoip(
    callback: (notification: Notification) => void,
    callbackFromQuit: (notification: Notification) => void,
  ): EmitterSubscription {
    if (callbackFromQuit) {
      backgroundMessageHandlerVoip = callbackFromQuit;
    }
    return this.nativeEventsReceiver.registerNotificationReceived(this.completionCallbackWrapper.wrapReceivedVoipCallback(callback));
  }
  
  public registerNotificationOpened(callback: (response: NotificationResponse, completion: () => void) => void): EmitterSubscription {
    return this.nativeEventsReceiver.registerNotificationOpened(this.completionCallbackWrapper.wrapOpenedCallback(callback));
  }
  
  public registerRemoteNotificationsRegistrationFailed(callback: (event: RegistrationError) => void): EmitterSubscription {
    return this.nativeEventsReceiver.registerRemoteNotificationsRegistrationFailed(callback);
  }
}

export {
  backgroundMessageHandler,
  backgroundMessageHandlerVoip,
};
