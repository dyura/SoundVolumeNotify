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
 * Created by yury on 12/08/18.
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

        String next_tag =String.valueOf(currentVolumeRING) +  String.valueOf(currentVolumeALARM) + String.valueOf(currentVolumeMUSIC);
        Log.d("notification:", "next_tag: "+next_tag.trim());
        Log.d("notification:", "next_tag no trim: "+next_tag);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();
        Log.d(TAG, "# notifications "+Integer.toString(activeNotifications.length));
//        for(int i = 0; i < activeNotifications.length; i++) {
//            Log.d("notification:", activeNotifications[i].toString());
//            Log.d("notification:", activeNotifications[i].getTag());
//            Log.d("notification:", String.valueOf(currentVolumeRING) +  String.valueOf(currentVolumeALARM) + String.valueOf(currentVolumeMUSIC));
//        }

        String current_tag="";
        switch (activeNotifications.length){
            case  0:
                break;
            case 1:
                current_tag=activeNotifications[0].getTag();
                break;
            default:
                Log.e("notification:","Multiple notificatons");
                Toast.makeText(context,"Multiple notificatons, report the problem",Toast.LENGTH_LONG).show();
                current_tag=activeNotifications[0].getTag();
        }
        Log.d("notification:", "current_tag: "+current_tag.trim());
        Log.d("notification:", "current_tag no trim: "+current_tag);

        if (current_tag.compareTo(next_tag)!=0){
//            Toast.makeText(context, "Volume now " + currentVolumeMUSIC + " "
//                    + currentVolumeRING + " " + currentVolumeALARM, Toast.LENGTH_LONG).show();
            Log.d(TAG, "Volume now " + currentVolumeMUSIC + " "
                    + currentVolumeRING + " " + currentVolumeALARM);

            if (current_tag.length()!=0)
                 cancelNotification(context,current_tag,NOTIFICATION_ID);
            sendNotification(context, currentVolumeRING, currentVolumeALARM, currentVolumeMUSIC, NOTIFICATION_ID);
        }
    }
    public void sendNotification(Context context, int currentVolume, int currentAlarm, int currentMusic, int NOTIFICATION_ID )  {


        Intent intent = new Intent(context, SoundVolumeActivity.class); //try
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        Notification.Builder builder = new Notification.Builder(context);
        String str;
        if (currentMusic>3)
            str=currentVolume+"|"+currentAlarm;
        else
            str=currentVolume+"!!"+currentAlarm;
        Bitmap bitmap =createBitmapFromString(str);
        Icon icn1;
        /* temporary commented out */
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            int height = bitmap.getHeight();
            int width = bitmap.getWidth();
            float scaleWidth = ((float) 32) / width;
            float scaleHeight = ((float) 21) / height;
            // CREATE A MATRIX FOR THE MANIPULATION
            Matrix matrix = new Matrix();
            // RESIZE THE BIT MAP
            matrix.postScale(scaleWidth, scaleHeight);
            Bitmap bitmap2 = Bitmap.createBitmap(
                    bitmap, 0, 0, 32, 21, matrix, false);

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 32, 32, false);

            icn1 = Icon.createWithBitmap(bitmap);  // set samll icon
            bitmap= scaledBitmap;   // prepare for the large icon
        }
        else {
            icn1 = Icon.createWithBitmap(bitmap);
        }
        builder.setSmallIcon(icn1);

        builder.setAutoCancel(true);

        builder.setLargeIcon(bitmap);
        Log.d(TAG, " notify Volume now " + currentMusic + " "
                + currentVolume + " " + currentAlarm);
        int id = Process.myPid();
        builder.setContentTitle("SVN:Ring: "+currentVolume+" Alarm: "+currentAlarm+" Media: "+currentMusic);
        builder.setContentText("Be aware\n");

        builder.setPriority(Notification.PRIORITY_MAX);
        builder.setOnlyAlertOnce(true);
        builder.setOngoing(true);
        builder.setContentIntent(pendingIntent);


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String CHANNEL_ID = "volume_ind_01";
            CharSequence name = "volume notification";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setSound(null, null);
            builder.setChannelId(CHANNEL_ID);
            notificationManager.createNotificationChannel(mChannel);
        }
//        notificationManager.notify(NOTIFICATION_ID, builder.build());
        String tag=String.valueOf(currentVolume) +  String.valueOf(currentAlarm) + String.valueOf(currentMusic);
        notificationManager.notify(tag,NOTIFICATION_ID, builder.build());
    }
    public Bitmap createBitmapFromString(String string) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(50); // size is in pixels

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
//        notificationManager.cancel(NOTIFICATION_ID);
        notificationManager.cancel(tag,NOTIFICATION_ID);
    }
}
