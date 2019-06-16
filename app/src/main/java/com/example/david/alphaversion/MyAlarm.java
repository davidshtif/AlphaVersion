package com.example.david.alphaversion;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.List;

import static com.example.david.alphaversion.Fridge.CHANNEL_1_ID;

public class MyAlarm extends BroadcastReceiver {

    DatabaseReference reference;
    String not;
    List<Product> productList;
    private NotificationManagerCompat notificationManager;

    @Override
    public void onReceive(final Context context, Intent intent) {
        reference = FirebaseDatabase.getInstance().getReference("products");
        /*intent.getExtras();
        String sDays=intent.getStringExtra("a");
        days= Integer.parseInt(sDays);
        not = null;*/


        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (productList != null) {
                    productList.clear();

                    for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                        Product product = productSnapshot.getValue(Product.class);

                        if (product.getExpired() != "") {
                            String expired = product.getExpired();

                            String dayS = expired.substring(0, 2);
                            String monthS = expired.substring(3, 5);
                            String yearS = expired.substring(6, 10);
                            int day = Integer.parseInt(dayS);
                            int month = Integer.parseInt(monthS);
                            int year = Integer.parseInt(yearS);

                            Calendar thatDay = Calendar.getInstance();
                            thatDay.set(Calendar.DAY_OF_MONTH, day);
                            thatDay.set(Calendar.MONTH, (month - 1));
                            thatDay.set(Calendar.YEAR, year);

                            Calendar today = Calendar.getInstance();

                            long curDiff = thatDay.getTimeInMillis() - today.getTimeInMillis();

                            if (curDiff == 0) {
                                //send notification and remove from list
                                not = "" + product.getName() + " is expired";
                                Notification notification = new NotificationCompat.Builder(context, CHANNEL_1_ID)
                                        .setSmallIcon(R.drawable.ic_stat_name)
                                        .setContentTitle("Expired product")
                                        .setContentText(not)
                                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                        .setStyle(new NotificationCompat.BigTextStyle().bigText(not))
                                        .build();
                                notificationManager.notify(1, notification);
                            } else {
                                productList.add(product);
                                if (curDiff <= (7 * 86400000)) {
                                    //send notification
                                    not = "" + product.getName() + " will expire in " + curDiff / 86400000 + " days";
                                    Notification notification = new NotificationCompat.Builder(context, CHANNEL_1_ID)
                                            .setSmallIcon(R.drawable.ic_priority_high_black_24dp)
                                            .setContentTitle("Expired product")
                                            .setContentText(not)
                                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                                            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                            .setStyle(new NotificationCompat.BigTextStyle().bigText(not))
                                            .build();
                                    notificationManager.notify(1, notification);
                                }
                            }
                        } else {
                            productList.add(product);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}