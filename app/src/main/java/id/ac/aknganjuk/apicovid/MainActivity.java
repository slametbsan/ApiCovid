package id.ac.aknganjuk.apicovid;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    private String TAG = MainActivity.class.getSimpleName();
    private ListView listView;

    ArrayList<HashMap<String, String>> covidList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        covidList = new ArrayList<>();

        listView = findViewById(R.id.listView);

        new GetSummary().execute();
    }

    private class GetSummary extends AsyncTask<Void, Void, Void> {
        ProgressBar progressBar = findViewById(R.id.progressBar);

        String tanggal = "";
        String globalConfirmed = "";
        String globalNewConfirmed = "";
        String globalDeaths = "";
        String globalNewDeaths = "";
        String globalRecovered = "";
        String globalNewRecovered = "";

        TextView tvGlobalConfirmed = findViewById(R.id.tvGlobalConfirmed);
        TextView tvGlobalNewConfirmed = findViewById(R.id.tvGlobalNewConfirmed);
        TextView tvGlobalDeaths = findViewById(R.id.tvGlobalDeaths);
        TextView tvGlobalNewDeaths = findViewById(R.id.tvGlobalNewDeaths);
        TextView tvGlobalRecovered = findViewById(R.id.tvGlobalRecovered);
        TextView tvGlobalNewRecovered = findViewById(R.id.tvGlobalNewRecovered);
        TextView tvLastUpdate = findViewById(R.id.tvLastUpdate);

        DecimalFormat thousand = new DecimalFormat("#,###");

        //format string tanggal jadi date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String tgl = null;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @SuppressLint("WrongThread")
        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler handler = new HttpHandler();

            //making request and getting response
            //URL untuk ambil data
            String url = "https://api.covid19api.com/summary";
            String jsonStr = handler.makeServiceCall(url);

            if( jsonStr != null ){
                try {
                    JSONObject jsonObject = new JSONObject(jsonStr);

                    JSONObject global = jsonObject.getJSONObject("Global");

                    tanggal = jsonObject.getString("Date");
                    globalConfirmed = global.getString("TotalConfirmed");
                    globalNewConfirmed = global.getString("NewConfirmed");
                    globalDeaths = global.getString("TotalDeaths");
                    globalNewDeaths = global.getString("NewDeaths");
                    globalRecovered = global.getString("TotalRecovered");
                    globalNewRecovered = global.getString("NewRecovered");

                    //ambil array Countries
                    JSONArray countries = jsonObject.getJSONArray("Countries");

                    Log.e(TAG, "JSON Country: " + countries);

                    //looping semua countries
                    for(int i = 0; i < countries.length(); i++){
                        JSONObject c = countries.getJSONObject(i);

                        String country = c.getString("Country");
                        String totalConfirmed = c.getString("TotalConfirmed");
                        String totalDeaths = c.getString("TotalDeaths");
                        String totalRecovered = c.getString("TotalRecovered");
                        //String date = c.getString("Date");

                        HashMap<String, String> covid = new HashMap<>();

                        covid.put("country", country);
                        covid.put("totalConfirmed", thousand.format(Double.valueOf(totalConfirmed)) + " Kasus");
                        covid.put("totalDeaths", thousand.format(Double.valueOf(totalDeaths)) + " Meninggal");
                        covid.put("totalRecovered", thousand.format(Double.valueOf(totalRecovered)) + " Sembuh");
                        //covid.put("date", date);

                        covidList.add(covid);
                    }
                } catch (JSONException e) {
                    //e.printStackTrace();
                    Log.e(TAG, "JSON parsing error: " + e.getMessage());
                }
            } else {
                Log.e(TAG, "Couldn't get JSON from server. Check LogCat for possible errors!");
            }

            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            try {
                Date date = dateFormat.parse(tanggal);
                SimpleDateFormat dateLocal = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss");
                dateLocal.setTimeZone(TimeZone.getDefault());
                tgl = dateLocal.format(date);
                Log.e("Cek Tanggal", tgl);

            } catch (ParseException e) {
                e.printStackTrace();
            }

            tvLastUpdate.setText(tgl);
            tvGlobalConfirmed.setText(thousand.format(Double.valueOf(globalConfirmed)));
            tvGlobalDeaths.setText(thousand.format(Double.valueOf(globalDeaths)));
            tvGlobalRecovered.setText(thousand.format(Double.valueOf(globalRecovered)));
            tvGlobalNewConfirmed.setText("+ " + thousand.format(Double.valueOf(globalNewConfirmed)));
            tvGlobalNewDeaths.setText("+ " + thousand.format(Double.valueOf(globalNewDeaths)));
            tvGlobalNewRecovered.setText("+ " + thousand.format(Double.valueOf(globalNewRecovered)));

            if(progressBar.isShown()){
                progressBar.setVisibility(View.GONE);

                // ini skrip aslinya, "date" dan R.id.tvTanggal dihapus karena semua tanggal ternyata sama
//                ListAdapter listAdapter = new SimpleAdapter(
//                        MainActivity.this, covidList,
//                        R.layout.list_item, new String[]{"country","totalConfirmed","totalDeaths","totalRecovered","date"},
//                        new int[]{R.id.tvNegara, R.id.tvKasus, R.id.tvMeninggal, R.id.tvSembuh, R.id.tvTanggal}
//                );

                ListAdapter listAdapter = new SimpleAdapter(
                        MainActivity.this, covidList,
                        R.layout.list_item, new String[]{"country","totalConfirmed","totalDeaths","totalRecovered"},
                        new int[]{R.id.tvNegara, R.id.tvKasus, R.id.tvMeninggal, R.id.tvSembuh}
                );
                listView.setAdapter(listAdapter);
            }
        }
    }
}
