/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.wifitrackerlib;

import static com.android.wifitrackerlib.WifiEntry.SECURITY_EAP;
import static com.android.wifitrackerlib.WifiEntry.SECURITY_EAP_SUITE_B;
import static com.android.wifitrackerlib.WifiEntry.SECURITY_NONE;
import static com.android.wifitrackerlib.WifiEntry.SECURITY_OWE;
import static com.android.wifitrackerlib.WifiEntry.SECURITY_OWE_TRANSITION;
import static com.android.wifitrackerlib.WifiEntry.SECURITY_PSK;
import static com.android.wifitrackerlib.WifiEntry.SECURITY_PSK_SAE_TRANSITION;
import static com.android.wifitrackerlib.WifiEntry.SECURITY_SAE;
import static com.android.wifitrackerlib.WifiEntry.SECURITY_WEP;

import static java.util.Comparator.comparingInt;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility methods for WifiTrackerLib.
 */
class Utils {
    // Returns the ScanResult with the best RSSI from a list of ScanResults.
    @Nullable
    static ScanResult getBestScanResultByLevel(@NonNull List<ScanResult> scanResults) {
        if (scanResults.isEmpty()) return null;

        return Collections.max(scanResults, comparingInt(scanResult -> scanResult.level));
    }

    // Returns the SECURITY type of a ScanResult
    @WifiEntry.Security
    static int getSecurityFromScanResult(@NonNull ScanResult result) {
        if (result.capabilities == null) {
            return SECURITY_NONE;
        }

        if (result.capabilities.contains("WEP")) {
            return SECURITY_WEP;
        } else if (result.capabilities.contains("PSK+SAE")) {
            return SECURITY_PSK_SAE_TRANSITION;
        } else if (result.capabilities.contains("SAE")) {
            return SECURITY_SAE;
        } else if (result.capabilities.contains("PSK")) {
            return SECURITY_PSK;
        } else if (result.capabilities.contains("EAP_SUITE_B_192")) {
            return SECURITY_EAP_SUITE_B;
        } else if (result.capabilities.contains("EAP")) {
            return SECURITY_EAP;
        } else if (result.capabilities.contains("OWE_TRANSITION")) {
            return SECURITY_OWE_TRANSITION;
        } else if (result.capabilities.contains("OWE")) {
            return SECURITY_OWE;
        }
        return SECURITY_NONE;
    }

    @WifiEntry.Security
    static int getSecurityFromWifiConfiguration(@NonNull WifiConfiguration config) {
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.SAE)) {
            return SECURITY_SAE;
        }
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            return SECURITY_PSK;
        }
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.SUITE_B_192)) {
            return SECURITY_EAP_SUITE_B;
        }
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP)
                || config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
            return SECURITY_EAP;
        }
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.OWE)) {
            return SECURITY_OWE;
        }
        return (config.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
    }

    // Returns a list of scan results filtering out unsupported capabilities
    static List<ScanResult> filterScanResultsByCapabilities(@NonNull List<ScanResult> scanResults,
            boolean isWpa3SaeSupported,
            boolean isWpa3SuiteBSupported,
            boolean isEnhancedOpenSupported) {
        List<ScanResult> filteredScanResultList = new ArrayList<>();
        for (ScanResult scanResult : scanResults) {
            // Add capabilities that are always supported
            if (scanResult.capabilities == null
                    || scanResult.capabilities.contains("PSK")
                    || scanResult.capabilities.contains("OWE_TRANSITION")) {
                filteredScanResultList.add(scanResult);
                continue;
            }
            // Skip unsupported capabilities
            if ((scanResult.capabilities.contains("EAP_SUITE_B_192") && !isWpa3SuiteBSupported)
                    || (scanResult.capabilities.contains("SAE") && !isWpa3SaeSupported)
                    || (scanResult.capabilities.contains("OWE") && !isEnhancedOpenSupported)) {
                continue;
            }
            // Safe to add
            filteredScanResultList.add(scanResult);
        }
        return filteredScanResultList;
    }
}