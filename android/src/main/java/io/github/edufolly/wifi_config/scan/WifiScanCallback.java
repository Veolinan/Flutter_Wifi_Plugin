package io.github.edufolly.wifi_config.scan;

import android.net.wifi.ScanResult;

import java.util.List;

import io.flutter.plugin.common.MethodChannel.Result;

public interface WifiScanCallback {
    void onScanResultsReady(List<ScanResult> scanResults, Result result);
}
