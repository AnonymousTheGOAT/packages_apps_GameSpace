/*
 * SPDX-FileCopyrightText: Chaldeaprjkt
 * SPDX-License-Identifier: Apache-2.0
 */
package com.android.systemui.screenrecord;

interface IRecordingCallback {
    void onRecordingStart();
    void onRecordingEnd();
}
