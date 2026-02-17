package com.unihub.app; // <-- BURASI SENİN PAKET İSMİNLE AYNI OLMALI

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class UniHubWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // 1. Arayüzü oluştur
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.uni_hub_widget);

        // 2. Tarihi ve Günü Ayarla
        SimpleDateFormat sdfDay = new SimpleDateFormat("EEEE", new Locale("tr", "TR"));
        SimpleDateFormat sdfDate = new SimpleDateFormat("d MMMM", new Locale("tr", "TR"));
        Date now = new Date();

        views.setTextViewText(R.id.appwidget_title, sdfDay.format(now));
        views.setTextViewText(R.id.appwidget_date, sdfDate.format(now));

        // 3. Dosyadan Dersleri Oku
        String courseListText = getCoursesForToday(context);
        views.setTextViewText(R.id.appwidget_text, courseListText);

        // 4. Tıklayınca Uygulamayı Aç
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);
        views.setOnClickPendingIntent(R.id.appwidget_title, pendingIntent);

        // Widget'ı güncelle
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static String getCoursesForToday(Context context) {
        StringBuilder sb = new StringBuilder();
        try {
            File file = new File(context.getFilesDir(), "widget_data.json");
            if (!file.exists()) return "Program bulunamadı.\nUygulamayı açın.";

            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuilder text = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) text.append(line);
            br.close();

            JSONArray allCourses = new JSONArray(text.toString());
            Calendar calendar = Calendar.getInstance();

            // Java'da Pazar=1, Pzt=2... UniHub'da Pzt=1...Paz=7.
            int javaDay = calendar.get(Calendar.DAY_OF_WEEK);
            int todayUniId = (javaDay == Calendar.SUNDAY) ? 7 : (javaDay - 1);

            // 1. Önce Bugüne Bak
            ArrayList<JSONObject> foundCourses = findCoursesForDay(allCourses, todayUniId);

            if (!foundCourses.isEmpty()) {
                // BUGÜN DERS VAR
                // Saate göre sırala
                Collections.sort(foundCourses, (o1, o2) -> o1.optString("t").compareTo(o2.optString("t")));

                for (JSONObject c : foundCourses) {
                    String name = c.optString("n");
                    String time = c.optString("t");
                    String room = c.optString("r");
                    boolean isCancelled = c.optInt("ic", 0) == 1; // İptal kontrolü

                    sb.append("🕒 ").append(time).append(" - ");

                    if (isCancelled) {
                        sb.append(name).append(" ❌ (İPTAL)");
                    } else {
                        sb.append(name);
                        if (!room.isEmpty()) sb.append(" (").append(room).append(")");
                    }
                    sb.append("\n");
                }
                return sb.toString().trim();
            }

            // 2. Bugün Ders Yoksa -> Sonraki Günleri Tara (7 gün ileri git)
            else {
                for (int i = 1; i <= 7; i++) {
                    int nextDayId = (todayUniId + i);
                    if (nextDayId > 7) nextDayId = nextDayId % 7;
                    if (nextDayId == 0) nextDayId = 7;

                    ArrayList<JSONObject> nextCourses = findCoursesForDay(allCourses, nextDayId);

                    if (!nextCourses.isEmpty()) {
                        String[] dayNames = {"", "Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi", "Pazar"};
                        String dayName = dayNames[nextDayId];

                        Collections.sort(nextCourses, (o1, o2) -> o1.optString("t").compareTo(o2.optString("t")));
                        JSONObject firstClass = nextCourses.get(0);

                        return "Bugün ders yok 🎉\n\nSonraki Ders:\n" +
                                dayName + " " + firstClass.optString("t") + "\n" +
                                firstClass.optString("n");
                    }
                }

                return "Bu hafta hiç dersin yok! 🏝️";
            }

        } catch (Exception e) {
            return "Hata: " + e.getMessage();
        }
    }

    private static ArrayList<JSONObject> findCoursesForDay(JSONArray all, int dayId) throws Exception {
        ArrayList<JSONObject> list = new ArrayList<>();
        for (int i = 0; i < all.length(); i++) {
            JSONObject c = all.getJSONObject(i);
            if (c.getInt("d") == dayId) list.add(c);
        }
        return list;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
}