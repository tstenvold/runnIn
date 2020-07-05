package t00212844.comp2161.afinal;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.Calendar;

public class WelcomeFragment extends Fragment {

    private final double KELVIN = 273.15;
    TextView greeting;
    TextView status;
    ImageView icon;

    public WelcomeFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static WelcomeFragment newInstance(String param1, String param2) {
        WelcomeFragment fragment = new WelcomeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_welcome, container, false);

        greeting = view.findViewById(R.id.welcometvGreeting);
        //status = view.findViewById(R.id.welcometvweather);
        //icon = view.findViewById(R.id.welcomeivweather);

        greeting.setText(generateGreeting());
        //Get current weather for current location
        //setWeather();

        return view;
    }

    private String generateGreeting() {
        String greeting = "";
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String name = pref.getString(getString(R.string.name), "");
        if (name.contains(" ") && name.length() > 1) {
            name = name.substring(0, name.indexOf(" "));
        }
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour >= 6 && hour < 12) {
            greeting = "Rise and Shine " + name;
        } else if (hour >= 12 && hour < 16) {
            greeting = name + ", Lets Run this Afternoon";
        } else if (hour >= 16 && hour < 21) {
            greeting = "Good Evening " + name;
        } else if (hour >= 21) {
            greeting = "Don't Forget your Headlamp " + name;
        } else if (hour > 0 && hour < 6) {
            greeting = "The Early Bird Catches the Worm " + name;
        }
        return greeting;
    }

    /*private void setWeather() {
        String apiKey = "20b4dd37224fd777c9480e6392f45636";
        String baseUrl = "https://api.openweathermap.org/data/2.5/weather?";
        //TODO get lat and lon dynamically
        String lat = "48.139130";
        String lon = "11.580220";

        String builtUrl = baseUrl + "lat=" + lat + "&lon=" + lon + "&appid=" + apiKey;
        new JsonTask().execute(builtUrl);
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected String doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            JSONObject json = null;
            try {
                json = new JSONObject(result);
                JSONArray jsonWeather = json.getJSONArray("weather");
                JSONObject jsonMain = json.getJSONObject("main");
                String jsonDesc = jsonWeather.getJSONObject(0).getString("description");
                String iconString = jsonWeather.getJSONObject(0).getString("icon");
                int jsonDegree = (int) (jsonMain.getDouble("temp") - KELVIN);

                String statusString = jsonDesc + " " + jsonDegree + "°C";
                status.setText(statusString);

                String imageUri = "https://openweathermap.org/img/wn/" + iconString + "@2x.png";
                Picasso.with(getContext()).load(imageUri).into(icon);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }*/
}