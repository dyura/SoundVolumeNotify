package soundvolume.my.soundvolumenotify;

/**
 * Created by yury on 19/02/18.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static android.content.ContentValues.TAG;

public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {
    public static final int NOTIFICATION_ID = 1;

    final public static String ONE_TIME = "onetime";
    NotificationFunc sn = new NotificationFunc() ;
    @Override
    public void onReceive(Context context, Intent intent) {
        String name=intent.getStringExtra("name");
        Log.d(TAG,name);
        SoundVolumeActivity.sn.sendNotification(context,SoundVolumeActivity.NOTIFICATION_ID);
    }
}
