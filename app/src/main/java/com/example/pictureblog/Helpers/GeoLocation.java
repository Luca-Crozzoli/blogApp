package com.example.pictureblog.Helpers;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GeoLocation {
    public GeoLocation() {
    }

    public static void getAddress(String locationAddress, Context context, Handler handler) {

        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                Geocoder geocoder = new Geocoder( context, Locale.getDefault() );
                String result = null;
                try {
                    //retrieve the first address result from the loocationAdrress provided as an input by the user
                    List addressList = geocoder.getFromLocationName( locationAddress, 1 );
                    if (addressList != null && addressList.size() > 0) {
                        Address address = (Address) addressList.get( 0 );
                        StringBuilder stringBuilder = new StringBuilder();

                        //getting latitude and longitude from the address object
                        stringBuilder.append( address.getLatitude() ).append( "\n" );
                        stringBuilder.append( address.getLongitude() ).append( "\n" );

                        result = stringBuilder.toString();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    Message message = Message.obtain();
                    message.setTarget( handler );
                    if (result != null) {
                        message.what = 1; // this is a code unique inside the handler do not worry each handler will have a different code ****
                        Bundle bundle = new Bundle();
                        bundle.putString( "Address", result );
                        message.setData( bundle );
                    }
                    message.sendToTarget();
                }

            }
        };
        thread.start();

    }
}
