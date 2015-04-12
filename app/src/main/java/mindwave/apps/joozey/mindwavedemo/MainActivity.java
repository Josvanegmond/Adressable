package mindwave.apps.joozey.mindwavedemo;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.neurosky.thinkgear.TGDevice;
import com.neurosky.thinkgear.TGEegPower;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends ActionBarActivity {

    private MindSurface mindSurface;

    private TGDevice tgDevice;
    private BluetoothAdapter btAdapter;

    private TextView label, blinkLabel, meditationLabel, heartRateLabel, attentionLabel, signalLabel;

    private int blinks = 0;

    private JSONArray mindSampleJSONArray;

    DBMindClient mindClient = null;
    private AsyncTask<Void,Void,Void> dataPushTask = new AsyncTask<Void,Void,Void>()
    {
        @Override
        protected Void doInBackground(Void... params)
        {
            while( true )
            {
                if (Looper.myLooper() == null)
                {
                    Looper.prepare();
                }

                if (mindClient == null)
                {
                    try {
                        mindClient = new DBMindClient(new URL("https://d85ec49d-9565-4dea-b298-0ae7863b2f19-bluemix.cloudant.com/samples2/_bulk_docs"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (mindClient != null)
                {
                    try {
                        JSONObject jsonObject = new JSONObject();

                        synchronized ( mindSampleJSONArray )
                        {
                            jsonObject.put("docs", mindSampleJSONArray);
                            mindSampleJSONArray = new JSONArray();
                        }

                        mindClient.push(jsonObject.toString());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mindSurface = (MindSurface)findViewById(R.id.surfaceView );

        mindSampleJSONArray = new JSONArray();

        label = (TextView)findViewById(R.id.label);
        blinkLabel = (TextView)findViewById(R.id.blinklabel);
        heartRateLabel = (TextView)findViewById(R.id.heartratelabel);
        signalLabel = (TextView)findViewById(R.id.signallabel);
        meditationLabel = (TextView)findViewById(R.id.meditationlabel);
        attentionLabel = (TextView)findViewById(R.id.attentionlabel);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(btAdapter != null) {
            Log.d("bt", "adapter found");

            if (!btAdapter.isEnabled()) {
                int REQUEST_ENABLE_BT = 1;
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

            tgDevice = new TGDevice(btAdapter, getHandler() );
            tgDevice.connect(true);
        }

        else
        {
            Log.d("bt", "no adapter");
        }

        dataPushTask.execute();
    }


    private void bufferJSONMindData( String key, int value )
    {
        try
        {
            JSONObject jsonMindDataObject = new JSONObject();
            jsonMindDataObject.put( "key", key );
            jsonMindDataObject.put( "value", value );
            jsonMindDataObject.put( "time", System.currentTimeMillis() );

            synchronized( mindSampleJSONArray )
            {
                mindSampleJSONArray.put(jsonMindDataObject);
            }
        }

        catch (JSONException e) {
            e.printStackTrace();
        }
    };

    private Handler getHandler()
    {
        return new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {

                label.setText(msg.toString());

                switch (msg.what) {
                    case TGDevice.MSG_STATE_CHANGE:
                        switch (msg.arg1) {
                            case TGDevice.STATE_IDLE:
                                break;
                            case TGDevice.STATE_CONNECTING:
                                break;
                            case TGDevice.STATE_CONNECTED:
                                tgDevice.start();
                                break;
                            case TGDevice.STATE_DISCONNECTED:
                                break;
                            case TGDevice.STATE_NOT_FOUND:
                            case TGDevice.STATE_NOT_PAIRED:
                            default:
                                break;
                        }
                        break;
                    case TGDevice.MSG_POOR_SIGNAL:
                        bufferJSONMindData("signal", msg.arg1);
                        signalLabel.setText("signal data: " + msg.arg1);
                        break;
                    case TGDevice.MSG_ATTENTION:
                        bufferJSONMindData("attention", msg.arg1);
                        attentionLabel.setText("attention data: " + msg.arg1);
//                        mindSurface.pushGraphData(0, msg.arg1);
                        break;
                    case TGDevice.MSG_HEART_RATE:
                        bufferJSONMindData("heartrate", msg.arg1);
                        heartRateLabel.setText("heartrate data: " + msg.arg1);
                        break;
                    case TGDevice.MSG_MEDITATION:
                        bufferJSONMindData("meditation", msg.arg1);
                        meditationLabel.setText("meditation data: " + msg.arg1);
//                        mindSurface.pushGraphData( 1, msg.arg1 );
                        break;
                    case TGDevice.MSG_BLINK:
                        blinks++;
                        blinkLabel.setText("measured blink strength: " + msg.arg1 + "\tblinks: " + blinks);
                        bufferJSONMindData("blink", msg.arg1);
//                        mindSurface.pushGraphData( 2, msg.arg1 );
                        break;
                    case TGDevice.MSG_EEG_POWER:
                        TGEegPower power = (TGEegPower)msg.obj;
                        bufferJSONMindData("delta", power.delta);
                        bufferJSONMindData("highalpha", power.highAlpha);
                        bufferJSONMindData("lowalpha", power.lowAlpha);
                        bufferJSONMindData("highbeta", power.highBeta);
                        bufferJSONMindData("lowbeta", power.lowBeta);
                        bufferJSONMindData("midgamma", power.midGamma);
                        bufferJSONMindData("lowgamma", power.lowGamma);
                        bufferJSONMindData("theta", power.theta);

                        mindSurface.pushGraphData(0, Math.log(power.delta) / Math.log(2) * 10. );
                        mindSurface.pushGraphData(1, Math.log(power.highAlpha) / Math.log(2) * 10. );
                        mindSurface.pushGraphData(2, Math.log(power.lowAlpha) / Math.log(2) * 10. );
                        mindSurface.pushGraphData(3, Math.log(power.highBeta) / Math.log(2) * 10. );
                        mindSurface.pushGraphData(4, Math.log(power.lowBeta) / Math.log(2) * 10. );
                        mindSurface.pushGraphData(5, Math.log(power.midGamma) / Math.log(2) * 10. );
                        mindSurface.pushGraphData(6, Math.log(power.lowGamma) / Math.log(2) * 10. );
                        mindSurface.pushGraphData(7, Math.log(power.theta) / Math.log(2) * 10. );

                        mindSurface.repaint();
                        break;
                    case TGDevice.MSG_RAW_DATA:
                        break;
                    default:
                        break;
                }
            }
        };
    }


}
