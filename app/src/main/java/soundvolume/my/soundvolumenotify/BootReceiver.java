package soundvolume.my.soundvolumenotify;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static soundvolume.my.soundvolumenotify.AlarmManagerBroadcastReceiver.ONE_TIME;

public class BootReceiver extends BroadcastReceiver {
    public static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            NotificationFunc sn = new NotificationFunc() ;
            sn.sendNotification(context,NOTIFICATION_ID);
            AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            Intent ABRintent = new Intent(context, AlarmManagerBroadcastReceiver.class);
            ABRintent.putExtra("name","main".toString());
            ABRintent.putExtra(ONE_TIME, Boolean.FALSE);
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, ABRintent, PendingIntent.FLAG_IMMUTABLE);
            am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 , pi);
//  If you don't want to use setRepeating, you can use a chain of single timers.  Either AlarmManager.set  or  AlarmManager. setExact
//   am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+1000*60, pi);
//   am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+1000*60, pi);
//    Read related comments in SoundVolumeActivity code

        }
    }
}
