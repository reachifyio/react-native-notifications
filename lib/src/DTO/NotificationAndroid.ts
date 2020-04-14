import {Notification} from './Notification';

export class NotificationAndroid extends Notification {
  constructor(payload: object) {
    super(payload);
    this.identifier = this.payload["google.message_id"];
  }

  get title(): string {
    return this.payload.title;
  }

  get body(): string {
    return this.payload.body;
  }

  get sound(): string {
    return this.payload.sound;
  }

  get data(): any {
    return this.payload;
  }

  get isSilent(): boolean {
    return !(this.payload["google.notification.title"] || this.payload["google.notification.body"]);
  }
}
