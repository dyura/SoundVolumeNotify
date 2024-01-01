package soundvolume.my.soundvolumenotify;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.os.Process;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import static android.content.Context.NOTIFICATION_SERVICE;


/**
 * Created by yury on 12/08/18. August 2018
 */

public class NotificationFunc {
    public int previousVolumeMUSIC=0;
    public int previousVolumeRING=0;
    public int previousVolumeALARM=0;
    public String TAG="NotificationFunc";

    public NotificationFunc(){};
    public void sendNotification(Context context, int NOTIFICATION_ID) {
        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        final int currentVolumeRING = audio.getStreamVolume(AudioManager.STREAM_RING);
        final int currentVolumeALARM = audio.getStreamVolume(AudioManager.STREAM_ALARM);
        final int currentVolumeMUSIC = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        final int currentVolumeNOTIF = audio.getStreamVolume(AudioManager.STREAM_NOTIFICATION);

//        String next_tag =String.valueOf(currentVolumeRING) +  String.valueOf(currentVolumeALARM) + String.valueOf(currentVolumeMUSIC);
        String next_title ="Ring: "+currentVolumeRING+" Alarm: "+currentVolumeALARM+" Media: "+currentVolumeMUSIC+
                " Notif: "+currentVolumeNOTIF;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();
//        Log.d(TAG, "# notifications "+Integer.toString(activeNotifications.length));

//        String current_tag="";
        String current_title="";
        switch (activeNotifications.length){
            case  0:
                break;
            case 1:
//                current_tag=activeNotifications[0].getTag();
                current_title = activeNotifications[0].getNotification().extras.getString("android.title");
//                Log.d("notification:",current_title);//getNotification().toString();
                break;
            default:
                Log.e("notification:","Multiple notificatons");
                Toast.makeText(context,"Multiple notificatons, report the problem",Toast.LENGTH_LONG).show();
                current_title = activeNotifications[0].getNotification().extras.getString("android.title");

        }
//        Log.d("notification:", "current_title: "+current_title.trim());
//        Log.d("notification:", "current_title no trim: "+current_title);
//        Log.d("notification:", "next_title no trim: "+next_title);

        if (current_title.compareTo(next_title)!=0){
//            Toast.makeText(context, "Volume now " + currentVolumeMUSIC + " "
//                    + currentVolumeRING + " " + currentVolumeALARM, Toast.LENGTH_LONG).show();
            Log.d(TAG, "Volume now " + currentVolumeMUSIC + " "
                    + currentVolumeRING + " " + currentVolumeALARM);

            sendNotification(context, currentVolumeRING, currentVolumeALARM, currentVolumeMUSIC, currentVolumeNOTIF,  NOTIFICATION_ID);
        }
    }
    public void sendNotification(Context context, int currentVolume, int currentAlarm, int currentMusic, int currentNotif, int NOTIFICATION_ID )  {


        Intent intent = new Intent(context, SoundVolumeActivity.class); //try
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        Notification.Builder builder = new Notification.Builder(context);
        String str;
        if (currentMusic>3)
            str=currentVolume+":"+currentAlarm;
        else
            str=currentVolume+"_"+currentAlarm;
        Bitmap bitmap =createBitmapFromString(str);
        Icon icn1;
        /* temporary commented out */
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            int height = bitmap.getHeight();
            int width = bitmap.getWidth();

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 40, 40, false);
            Bitmap scaledBitmap2 = Bitmap.createScaledBitmap(bitmap, 70, 70, false);

            icn1 = Icon.createWithBitmap(scaledBitmap2);  // set samll icon
            bitmap= scaledBitmap;   // prepare for the large icon
        }
        else {
            icn1 = Icon.createWithBitmap(bitmap);
        }
        builder.setSmallIcon(icn1);

        builder.setAutoCancel(true);

        builder.setLargeIcon(bitmap);
//        Log.d(TAG, " notify Volume now " + currentMusic + " "
//                + currentVolume + " " + currentAlarm);
        int id = Process.myPid();
        builder.setContentTitle("Ring: "+currentVolume+" Alarm: "+currentAlarm+" Media: "+currentMusic+
                " Notif: "+currentNotif);
        builder.setContentText("Ring: "+currentVolume+" Alarm: "+currentAlarm+" Media: "+currentMusic+
                " Notif: "+currentNotif + " !!Be aware!!");
//       builder.setContentTitle("Ring: "+currentVolume+" Alarm: "+currentAlarm);
//        builder.setContentText(" Media: "+currentMusic+
//                " Notif: "+currentNotif);

        builder.setPriority(Notification.PRIORITY_MAX);
//        builder.setPriority(Notification.PRIORITY_DEFAULT);
        builder.setOngoing(true);
        builder.setOnlyAlertOnce(true);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String CHANNEL_ID = "volume_ind_01";
            CharSequence name = "volume notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT  ; //IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setSound(null, null);
            builder.setChannelId(CHANNEL_ID);
            notificationManager.createNotificationChannel(mChannel);
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
    public Bitmap createBitmapFromString(String string) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
//        paint.setTextSize(50); // size is in pixels
        paint.setTextSize(70); // size is in pixels

        Rect textBounds = new Rect();
        paint.getTextBounds(string, 0, string.length(), textBounds);

        Bitmap bitmap = Bitmap.createBitmap((textBounds.width()), (textBounds.height()),
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawText(string, -textBounds.left,
                textBounds.height() - textBounds.bottom, paint);

        return bitmap;
    }
    public void cancelNotification(Context context, String tag,int NOTIFICATION_ID) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(tag,NOTIFICATION_ID);
    }
}
