import 'dart:async';

import 'package:flutter/material.dart';

import 'package:wifi_config/wifi_config.dart';
import 'package:wifi_config/wifi_network.dart';

///
///
///
void main() {
  runApp(MyApp());
}

///
///
///
class MyApp extends StatefulWidget {
  ///
  ///
  ///
  @override
  _MyAppState createState() => _MyAppState();
}

///
///
///
class _MyAppState extends State<MyApp> {
  StreamController<List<WifiNetwork>> _controller;
  StreamController<bool> _searching;

  ///
  ///
  ///
  @override
  void initState() {
    _controller = StreamController();
    _searching = StreamController();
    super.initState();
  }

  ///
  ///
  ///
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Wifi Config App',
      theme: ThemeData(
        primarySwatch: Colors.deepPurple,
        visualDensity: VisualDensity.adaptivePlatformDensity,
      ),
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Wifi Config App'),
          actions: [
            StreamBuilder<bool>(
              stream: _searching.stream,
              initialData: false,
              builder: (context, snapshot) {
                return IconButton(
                  icon: Icon(Icons.refresh),
                  onPressed: snapshot.data ? null : _wifiSearch,
                );
              },
            ),
          ],
        ),
        body: Column(
          children: [
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 16.0),
              child: Text(
                'Wifi Config App',
                style: Theme.of(context).textTheme.headline5,
              ),
            ),
            StreamBuilder<Object>(
              stream: _controller.stream,
              initialData: <WifiNetwork>[],
              builder: (context, snapshot) {
                if (snapshot.hasData) {
                  List<WifiNetwork> networks = snapshot.data;

                  if (networks.isEmpty) {
                    return Center(
                      child: RaisedButton.icon(
                        onPressed: _wifiSearch,
                        icon: Icon(Icons.wifi),
                        label: Text('Wifi Search'),
                      ),
                    );
                  } else {
                    return Expanded(
                      child: ListView.separated(
                        itemBuilder: (context, index) {
                          WifiNetwork wifi = networks[index];

                          Color color = Colors.green;
                          IconData icon = Icons.wifi;

                          if (wifi.level < -67) {
                            color = Colors.amber;
                            if (wifi.level < -70) {
                              color = Colors.red;
                              if (wifi.level < -80) {
                                color = Colors.black;
                                icon = Icons.wifi_off;
                              }
                            }
                          }

                          return ListTile(
                            leading: Column(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                Icon(
                                  icon,
                                  size: 32.0,
                                  color: color,
                                ),
                              ],
                            ),
                            title: Text(wifi.ssid),
                            subtitle: Text(wifi.bssid),
                            trailing: Chip(
                              label: Text(wifi.channel.toString()),
                            ),
                            onTap: () => _connect(wifi),
                          );
                        },
                        separatorBuilder: (context, index) => Divider(),
                        itemCount: networks.length,
                      ),
                    );
                  }
                }
                // TODO - Error

                return Center(
                  child: CircularProgressIndicator(),
                );
              },
            ),
          ],
        ),
      ),
    );
  }

  ///
  ///
  ///
  void _wifiSearch() async {
    _controller.add(null);
    _searching.add(true);

    try {
      List<WifiNetwork> networks = await WifiConfig.getWifiList();
      _controller.add(networks);
    } catch (e) {
      print(e);
      _controller.add(<WifiNetwork>[]);
    }

    _searching.add(false);
  }

  ///
  ///
  ///
  void _connect(WifiNetwork network) async {
    String password = 'testeinfinito';

    var result = await WifiConfig.connect(network, password);

    print(result);
    print(result.runtimeType);
  }

  ///
  ///
  ///
  @override
  void dispose() {
    _controller.close();
    _searching.close();
    super.dispose();
  }
}
