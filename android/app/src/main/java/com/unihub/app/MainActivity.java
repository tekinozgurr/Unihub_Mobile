package com.unihub.app; // <-- BURAYI KENDİ PAKET İSMİNLE DEĞİŞTİRMEYİ UNUTMA

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

    @Override
    public void onPause() {
        super.onPause();

        // UYGULAMA ALTA ALINDIĞINDA VEYA KAPANDIĞINDA WIDGET'A "GÜNCELLEN" EMRİ GÖNDER
        Intent intent = new Intent(this, UniHubWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(this).getAppWidgetIds(new ComponentName(this, UniHubWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }
}