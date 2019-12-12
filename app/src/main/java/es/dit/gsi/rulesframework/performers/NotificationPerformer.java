package es.dit.gsi.rulesframework.performers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import es.dit.gsi.rulesframework.ListRulesActivity;
import es.dit.gsi.rulesframework.R;
import es.dit.gsi.rulesframework.RecognitionActivity;

/**
 * Created by afernandez on 25/01/16.
 */
public class NotificationPerformer {
    Context context;
    String CHANNEL_ID = "Channel 1";
    public NotificationPerformer(Context context){
        this.context = context;
    }

    public void show (String parameter){
// Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(context, RecognitionActivity.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(RecognitionActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        //android.support.v4.app.NotificationCompat.Builder builder = new android.support.v4.app.NotificationCompat.Builder(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);


        System.out.println("Define notification setting");
        // Define the notification settings.
        builder.setSmallIcon(R.mipmap.logo)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.favicon))
                .setColor(Color.parseColor("#00a9e0"))
                .setContentTitle("Notification")
                .setContentText(parameter)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
        System.out.println("Issue the notification");

    }
}
