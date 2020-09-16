package io.github.edufolly.wifi_config;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.github.edufolly.wifi_config.scan.WifiScanCallback;
import io.github.edufolly.wifi_config.scan.WifiScanReceiver;
//import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * @author Eduardo Folly
 */
public class WifiConfigPlugin implements FlutterPlugin, MethodCallHandler, WifiScanCallback {
    private MethodChannel channel;
    private Context context;
    private WifiManager wifiManager;
    private WifiScanReceiver wifiScanReceiver;
    private ConnectivityManager connectivityManager;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "wifi_config");
        channel.setMethodCallHandler(this);

        context = flutterPluginBinding.getApplicationContext();

        connectivityManager = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        wifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
    }


    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull final Result result) {
        switch (call.method) {
            case "getWifiList":
                if (wifiManager == null) {
                    result.error("NO_WIFI_MANAGER",
                            "Wifi Manager is null.", null);
                    return;
                }

                if (wifiScanReceiver != null) {
                    result.error("WIFI_LIST_IS_RUNNING",
                            "Wifi list is running.", null);
                    return;
                }

                wifiScanReceiver = new WifiScanReceiver(wifiManager, result, this);

                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

                context.registerReceiver(wifiScanReceiver, intentFilter);

                wifiManager.startScan();
                break;
            case "connect":
                if (wifiManager == null) {
                    result.error("NO_WIFI_MANAGER",
                            "Wifi Manager is null.", null);
                    return;
                }

                // TODO - Tratamento de erros.

                String ssid = call.argument("ssid");

                if (ssid == null) {
                    result.error("NO_SSID", "SSID is null.",
                            null);
                    return;
                }

                String password = call.argument("password");

                if (password == null) {
                    result.error("NO_PASSWORD", "Password is null.",
                            null);
                    return;
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    WifiNetworkSpecifier specifier = new WifiNetworkSpecifier.Builder()
                            .setSsid(ssid)
                            .setWpa2Passphrase(password)
                            .build();

                    NetworkRequest request = new NetworkRequest.Builder()
                            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            .setNetworkSpecifier(specifier)
                            .build();

                    if (connectivityManager != null) {
                        connectivityManager.requestNetwork(request,
                                new ConnectivityManager.NetworkCallback() {

                                    @Override
                                    public void onAvailable(@NonNull final Network network) {
                                        super.onAvailable(network);
                                        connectivityManager.bindProcessToNetwork(network);
                                        successOnUIThread(result, true);
                                    }

                                    @Override
                                    public void onUnavailable() {
                                        super.onUnavailable();
                                        errorOnUIThread(result, "NETWORK_UNAVAILABLE",
                                                "Network Unavailable");
                                    }
                                }, 30000);
                    } else {
                        result.error("NO_CONNECTIVITY_MANAGER",
                                "Connectivity Manager is null.", null);
                    }
                } else {
                    // TODO - Conectar no Andorid "Antigo".
                    result.notImplemented();
                }
                break;
            default:
                result.notImplemented();
                break;
        }


    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    @Override
    public void onScanResultsReady(List<ScanResult> scanResults, Result result) {
        List<Map<String, Object>> wifiTest = new ArrayList<>();

        for (ScanResult scanResult : scanResults) {
            Map<String, Object> wifi = new HashMap<>();

            wifi.put("ssid", scanResult.SSID);
            wifi.put("bssid", scanResult.BSSID);
            wifi.put("channel", frequencyToChannel(scanResult.frequency));
            wifi.put("frequency", scanResult.frequency);
            wifi.put("capabilities", scanResult.capabilities);
            wifi.put("level", scanResult.level);
            wifi.put("timestamp", scanResult.timestamp);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                wifi.put("channelWidth", scanResult.channelWidth);
                wifi.put("centerFreq0", scanResult.centerFreq0);
                wifi.put("centerFreq1", scanResult.centerFreq1);
                wifi.put("operatorFriendlyName", scanResult.operatorFriendlyName.toString());
                wifi.put("venueName", scanResult.venueName.toString());
            }

            wifiTest.add(wifi);
        }

        context.unregisterReceiver(wifiScanReceiver);

        wifiScanReceiver = null;

        result.success(wifiTest);
    }


    private void successOnUIThread(final Result result, final Object success) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                result.success(success);
            }
        });

    }

    private void errorOnUIThread(final Result result, final String errorCode,
                                 final String errorMessage) {

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                result.error(errorCode, errorMessage, null);
            }
        });

    }

    public static int frequencyToChannel(int freq) {
        if (2412 <= freq && freq <= 2484)
            return (freq - 2412) / 5 + 1;
        else if (5170 <= freq && freq <= 5825)
            return (freq - 5170) / 5 + 34;
        else
            return -1;
    }

}
