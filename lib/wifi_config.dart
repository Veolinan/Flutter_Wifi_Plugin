import 'dart:async';

import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:wifi_config/wifi_network.dart';

///
///
///
enum WifiConnectionStatus {
  connected,
  alreadyConnected,
  notConnected,
  platformNotSupported,
  profileAlreadyInstalled,
  locationNotAllowed,
}

class WifiConfig {
  static const MethodChannel _channel = const MethodChannel('wifi_config');

  // static Future<WifiConnectionStatus> connectToWifi(String ssid,
  //     String password, String packageName) async {
  //   final String isConnected = await _channel.invokeMethod(
  //     'connectToWifi', {
  //     'ssid': ssid,
  //     'password': password,
  //     'packageName': packageName,
  //   },);
  //
  //   switch (isConnected) {
  //     case "connected":
  //       return WifiConnectionStatus.connected;
  //       break;
  //
  //     case "alreadyConnected":
  //       return WifiConnectionStatus.alreadyConnected;
  //       break;
  //
  //     case "notConnected":
  //       return WifiConnectionStatus.notConnected;
  //       break;
  //
  //     case "platformNotSupported":
  //       return WifiConnectionStatus.platformNotSupported;
  //       break;
  //
  //     case "profileAlreadyInstalled":
  //       return WifiConnectionStatus.profileAlreadyInstalled;
  //       break;
  //
  //     case "locationNotAllowed":
  //       return WifiConnectionStatus.locationNotAllowed;
  //       break;
  //   }
  //
  //   return null;
  // }

  ///
  /// 
  /// 
  static Future<List<WifiNetwork>> getWifiList() async {
    if (!await Permission.locationWhenInUse.request().isGranted) {
      throw Exception('Without permission');
    }

    final List<dynamic> wifiList = await _channel.invokeMethod('getWifiList');

    List<WifiNetwork> wifiNetworks = [];

    for (dynamic wifi in wifiList) {
      wifiNetworks.add(
        WifiNetwork.fromMap(wifi),
      );
    }

    return wifiNetworks;
  }
  
  ///
  /// 
  /// 
  static Future<dynamic> connect(WifiNetwork wifiNetwork, String password,) async {
    if (!await Permission.locationWhenInUse.request().isGranted) {
    throw Exception('Without permission');
    }

    Map<String, dynamic> network = wifiNetwork.toMap();
    network['password'] = password;

    return await _channel.invokeMethod('connect', network);
  }

// static Future<bool> isConnectedToWifi(String ssid) async {
//   final bool isConnected = await _channel
//       .invokeMethod('isConnectedToWifi', <String, dynamic>{"ssid": ssid});
//   return isConnected;
// }

// static Future<String> connectedToWifi() async {
//   final String connectedWifiName =
//   await _channel.invokeMethod('connectedToWifi');
//   return connectedWifiName;
// }
}
