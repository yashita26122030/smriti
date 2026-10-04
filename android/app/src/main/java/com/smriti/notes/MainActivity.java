package com.smriti.notes;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        registerPlugin(SmritiAlarmPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
