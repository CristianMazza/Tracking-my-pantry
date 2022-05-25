package com.example.lamproject;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.room.Room;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class DateService extends Service {
    final int day = 60000 *60*24;

    Timer timer;
    TimerTask timerTask;
    Context context;
    Database db;
    ProductDao products;
    @Nullable
    @Override
    public IBinder onBind(@NotNull Intent intent) {
        return null;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        context = this;
        db = Room.databaseBuilder(getApplicationContext(), Database.class, "pantryDB").allowMainThreadQueries().build();
        products = db.productDao();
        timer = new Timer();
        createtimer();
        return START_STICKY;
    }
    private void createtimer(){
        timer = new Timer();
        timerTask = new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void run() {
                GregorianCalendar calendar = new GregorianCalendar();
                calendar.add(Calendar.DATE, 1);
                Date tomorrow = calendar.getTime();
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                String tom = format.format(tomorrow);
                int inScadenza = products.getCountByDate(tom);

                if (inScadenza > 0) {
                    showNotification("Attenzione", "Hai prodotti nella dispensa che scadranno domani!", 1);
                }

            }
        };
        timer.schedule(timerTask,day/2,day);
    }

    
            public void showNotification(String title, String message, int reqCode) {
                String CHANNEL_ID = "Channel";
                PendingIntent pendingIntent = PendingIntent.getActivity(context, reqCode, new Intent(), 0);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.hourglass)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message))
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setOngoing(true);

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, "Notifiche", NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(mChannel);
                }
                startForeground(reqCode, builder.build());
            }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}