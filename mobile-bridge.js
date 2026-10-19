import { Capacitor, registerPlugin } from '@capacitor/core';

window.SmritiMobile = {
  platform: Capacitor.getPlatform(),
  alarm: registerPlugin('SmritiAlarm'),
  voice: registerPlugin('SmritiVoice')
};
