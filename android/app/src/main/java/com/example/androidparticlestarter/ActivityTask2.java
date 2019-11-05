package com.example.androidparticlestarter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;
import io.particle.android.sdk.cloud.exceptions.ParticleCloudException;
import io.particle.android.sdk.utils.Async;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ActivityTask2 extends AppCompatActivity {
    private final String TAG="Pardeep";

    Random random_genrator = new Random();
    String particleId;
    TextView text_v;
    public String url= "https://api.openweathermap.org/data/2.5/weather?q=Toronto&units=metric&appid=4bfbb5399fa9602a23cd1718fb00c808";
    String answer = "";
    double tempr;
    private final String PARTICLE_USERNAME = "pardeepvirk18@gmail.com";
    private final String PARTICLE_PASSWORD = "Lager@5678";
    private final String DEVICE_ID = "1c002d001247363333343437";
    private long subscriptionId;
    private ParticleDevice mDevice;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task2);
        text_v = findViewById(R.id.text_v);
        try {
            run();
            getDeviceFromCloud();
            setImage();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void run() throws IOException {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String myResponse = response.body().string();

                ActivityTask2.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObj = new JSONObject(myResponse);
                            JSONObject main = jsonObj.getJSONObject("main");
                            JSONObject sys = jsonObj.getJSONObject("sys");
                            JSONObject wind = jsonObj.getJSONObject("wind");
                            JSONObject weather = jsonObj.getJSONArray("weather").getJSONObject(0);

                            Long updatedAt = jsonObj.getLong("dt");
                            String temp = main.getString("temp") + "°C";
                            double temp2 = main.getDouble("temp");
                            tempr = temp2;
                            String tempMin = "Min Temp: " + main.getString("temp_min") + "°C";
                            String tempMax = "Max Temp: " + main.getString("temp_max") + "°C";
                            String pressure = main.getString("pressure");
                            String humidity = main.getString("humidity");

                            Long sunrise = sys.getLong("sunrise");
                            Long sunset = sys.getLong("sunset");
                            String windSpeed = wind.getString("speed");
                            String weatherDescription = weather.getString("description");

                            String address = jsonObj.getString("name") + ", " + sys.getString("country");
                            text_v.setText(temp);

                        }
                        catch(Exception e){}

                    }
                });

            }
        });
        ParticleCloudSDK.init(this.getApplicationContext());
        getDeviceFromCloud();
     //   setImage();
        getEvents();
    }

    public void changeCity(View view){
         url= "https://api.openweathermap.org/data/2.5/weather?q=Mississauga&units=metric&appid=4bfbb5399fa9602a23cd1718fb00c808";
        try {
            run();
            getDeviceFromCloud();
            setImage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getEvents()
    {
        getDeviceFromCloud();
        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {
            @Override
            public Object callApi(@NonNull ParticleCloud particleCloud) throws ParticleCloudException, IOException {
                try {
                    subscriptionId = ParticleCloudSDK.getCloud().subscribeToAllEvents("weather", new ParticleEventHandler() {
                        @Override
                        public void onEventError(Exception e) {
                        }
                        @Override
                        public void onEvent(String eventName, ParticleEvent particleEvent) {
                            particleId = particleEvent.deviceId;
                            answer = particleEvent.dataPayload;
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return -1;
            }

            @Override
            public void onSuccess(Object o) {

            }

            @Override
            public void onFailure(ParticleCloudException exception) {
            }
        });
    }
    public void start(View v)
    {
        setImage();
        getDeviceFromCloud();
    }

    public void setImage() {
        getDeviceFromCloud();

        int number = random_genrator.nextInt(2);
        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {
            @Override
            public Object callApi(@NonNull ParticleCloud particleCloud) throws ParticleCloudException, IOException {
                List<String> functionParameters = new ArrayList<String>();
                if(tempr<10)
                {
                    functionParameters.add("cold");
                }
                else if(tempr>10 && tempr<25)
                {
                    functionParameters.add("warm");
                }

                else if(tempr>25)
                {
                    functionParameters.add("hot");
                }


                try {
                    mDevice.callFunction("myAnswer2", functionParameters);
                } catch (ParticleDevice.FunctionDoesNotExistException e) {
                    e.printStackTrace();
                }
                return -1;
            }
            @Override
            public void onSuccess(Object o) {

            }
            @Override
            public void onFailure(ParticleCloudException exception) {
                Log.d(TAG, exception.getBestMessage());
            }
        });

    }

    public void getDeviceFromCloud() {
        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {
            @Override
            public Object callApi(@NonNull ParticleCloud particleCloud) throws ParticleCloudException, IOException {
                particleCloud.logIn(PARTICLE_USERNAME, PARTICLE_PASSWORD);
                mDevice = particleCloud.getDevice(DEVICE_ID);
                try {
                }
                catch(Exception e)
                {
                }

                return -1;
            }
            @Override
            public void onSuccess(Object o) {
            }

            @Override public void onFailure(ParticleCloudException exception) {
            }
        });
    }


}
