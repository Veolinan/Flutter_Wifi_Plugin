///
///
///
class WifiNetwork {
  final String ssid;
  final String bssid;
  final int channel;
  final int frequency;
  final String capabilities;
  final int level;
  final int timestamp;

  ///
  ///
  ///
  WifiNetwork({
    this.ssid,
    this.bssid,
    this.channel,
    this.frequency,
    this.capabilities,
    this.level,
    this.timestamp,
  });

  ///
  ///
  ///
  static WifiNetwork fromMap(dynamic map) {
    return WifiNetwork(
      ssid: map['ssid'],
      bssid: map['bssid'],
      channel: map['channel'],
      frequency: map['frequency'],
      capabilities: map['capabilities'],
      level: map['level'],
      timestamp: map['timestamp'],
    );
  }

  ///
  ///
  ///
  Map<String, dynamic> toMap() {
    return {
      'ssid': ssid,
      'bssid': bssid,
      'channel': channel,
      'frequency': frequency,
      'capabilities': capabilities,
      'level': level,
      'timestamp': timestamp,
    };
  }
}
