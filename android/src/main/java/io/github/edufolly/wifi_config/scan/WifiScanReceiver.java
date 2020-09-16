package io.github.edufolly.wifi_config.scan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

import io.flutter.plugin.common.MethodChannel.Result;

/**
 * @author Eduardo Folly
 */
public class WifiScanReceiver extends BroadcastReceiver {
    private final WifiManager wifiManager;
    private final Result result;
    private final WifiScanCallback callback;

    public WifiScanReceiver(WifiManager wifiManager, Result result, WifiScanCallback callback) {
        this.wifiManager = wifiManager;
        this.result = result;
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
            callback.onScanResultsReady(wifiManager.getScanResults(), result);
        }
    }
}
