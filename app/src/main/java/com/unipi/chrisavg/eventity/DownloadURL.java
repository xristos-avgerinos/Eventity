package com.unipi.chrisavg.eventity;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class DownloadURL {

    public String retrieveUrl(String url) throws IOException {
        String urlData="";
        HttpsURLConnection httpsURLConnection = null;
        InputStream inputStream = null;
        try{
            URL getUrl = new URL(url);
            httpsURLConnection = (HttpsURLConnection) getUrl.openConnection();
            httpsURLConnection.connect();

            inputStream=httpsURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer sb = new StringBuffer();
            String line ="";
            while((line = bufferedReader.readLine())!=null){
                sb.append(line);
            }
            urlData = sb.toString();
            bufferedReader.close();
        }catch(Exception e){
            Log.d("Exception", e.toString());
        }finally{
            inputStream.close();
            httpsURLConnection.disconnect();
        }
        return urlData;
    }
}
