package mindwave.apps.joozey.mindwavedemo;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by mint on 11-4-15.
 */
public class DBMindClient
{
    private HttpsURLConnection conn;
    private URL url;

    public DBMindClient(final URL url)
    {
        this.url = url;
    }

    public void push( String message )
    {
        try
        {
            conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.connect();

            write(message);
            read();

            conn.disconnect();
        }

        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void write( String message ) throws IOException
    {
        OutputStream os = conn.getOutputStream();
        os.write(message.getBytes("US-ASCII") );
        Log.d("mind sender: ", message);
    }

    private void read() throws IOException
    {
        Log.d( "mind feedback code: ", conn.getResponseCode() + "" );

        InputStream is = conn.getInputStream();
        BufferedReader br = new BufferedReader( new InputStreamReader(is) );

        Log.d( "mind feedback line: ", br.readLine() );
    }
}
