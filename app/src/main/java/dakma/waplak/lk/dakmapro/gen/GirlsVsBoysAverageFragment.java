package dakma.waplak.lk.dakmapro.gen;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import dakma.waplak.lk.NevigationActivity;
import dakma.waplak.lk.dakmapro.R;
import dakma.waplak.lk.utility.AndroidUtill;
import dakma.waplak.lk.utility.DownloadedDataCenter;

/**
 * Created by admin on 5/21/2017.
 */

public class GirlsVsBoysAverageFragment extends Fragment {
    private LineChart mChart;
    private TextView selectedCenter;
    private Spinner spAlYear;
    private String alYear;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.girls_vs_boys_average, container, false);

        selectedCenter=(TextView)rootView.findViewById(R.id.selectedCenter);
        selectedCenter.setText(NevigationActivity.ExamCenter);
        spAlYear = (Spinner) rootView.findViewById(R.id.spAlYear);
        Button btView = (Button) rootView.findViewById(R.id.btView);
        mChart = (LineChart) rootView.findViewById(R.id.chart);
        loadSpinners();

        btView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AndroidUtill.isNetworkConnected(getActivity())) {
                    new SendfeedbackJob().execute();
                }else{
                    Toast.makeText(
                            getActivity(),
                            "No internet connection", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });
        return rootView;

    }
    public void loadSpinners(){
        ArrayAdapter<String> dataAdapterAlYear = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, DownloadedDataCenter.getInstance(getActivity()).getLoadAllALYears());
        dataAdapterAlYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        spAlYear.setAdapter(dataAdapterAlYear);
        spAlYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                alYear = (String) parent.getItemAtPosition(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if(NevigationActivity.ALYear!=null && !NevigationActivity.ALYear.equals("")) {
            spAlYear.setSelection(dataAdapterAlYear.getPosition(NevigationActivity.ALYear));
            alYear=NevigationActivity.ALYear;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        // getActivity().setTitle("Fragment 1");
    }



    private class SendfeedbackJob extends AsyncTask<String, Void, String> {
        private final ProgressDialog dialog = new ProgressDialog(
                getActivity(),ProgressDialog.THEME_HOLO_DARK);
        @Override
        protected void onPreExecute() {
            try {
                this.dialog.setMessage("Loading Chart....");
                this.dialog.setCanceledOnTouchOutside(false);
                this.dialog.show();
            } catch (Exception e) {
                Toast.makeText(
                        getActivity(),
                        "There are Some error in Loading Chart : "
                                + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
        @Override
        protected String doInBackground(String[] params) {
            HttpResponse response = null;
            String result = null;
            try {
                HttpParams httpParameters = new BasicHttpParams();
                int timeoutConnection = 60000;
                HttpConnectionParams.setConnectionTimeout(httpParameters,timeoutConnection);
                int timeoutSocket = 120000; // in milliseconds which is the timeout
                HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
                HttpClient httpclient = new DefaultHttpClient(httpParameters);
                HttpGet request = new HttpGet();
                String url = "http://api.dekma.edu.lk/api/Dashboard/LoadGenderWiseAverageLineChartDetails?Centre="+NevigationActivity.ExamCenter+"&AL_Year="+alYear;
                request.setURI(new URI(url.replace(" ", "%20")));
                response = httpclient.execute(request);
                if(response!= null && !response.equals("")) {

                    HttpEntity entity = response.getEntity();
                    if (entity != null) {

                        // A Simple JSON Response Read
                        InputStream instream = entity.getContent();
                        result = convertStreamToString(instream);
                        // now you have the string representation of the HTML request
                        System.out.println("RESPONSE: " + result);
                        instream.close();


                    }
                    org.apache.http.Header[] headers = response.getAllHeaders();
                    for (int i = 0; i < headers.length; i++) {
                        System.out.println(headers[i]);
                    }
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("log_tag", "Error in http connection " + e.toString());
            }
            return result;

        }

        @Override
        protected void onPostExecute(String message) {
            try {
                if(message!=null) {
                    JSONArray jsonarray = new JSONArray(message);
                    ArrayList<LineDataSet> dataSets = null;
                    ArrayList<Entry> valueSet1 = new ArrayList<>();
                    ArrayList<Entry> valueSet2 = new ArrayList<>();
                    ArrayList<String> xAxis = new ArrayList<>();
                    if (jsonarray != null && jsonarray.length() > 0) {
                        for (int i = 0; i < jsonarray.length(); i++) {
                            JSONObject jsonobject = jsonarray.getJSONObject(i);
                            JSONArray json =jsonobject.getJSONArray("Value");
                            if (json != null && json.length() > 0) {
                                Entry v1e1 = new Entry(Integer.parseInt(json.get(0).toString()),i);
                                valueSet1.add(v1e1);

                                Entry v1e2 = new Entry(Integer.parseInt(json.get(1).toString()),i);
                                valueSet2.add(v1e2);

                            }


                            xAxis.add(jsonobject.getString("Key"));
                        }
                        dataSets = new ArrayList<>();
                        LineDataSet LineDataSet1 = new LineDataSet(valueSet1, "Boys");
                        LineDataSet1.setColor(Color.rgb(0, 155, 0));
                        dataSets.add(LineDataSet1);
                        LineDataSet LineDataSet2 = new LineDataSet(valueSet2, "Girls");
                        LineDataSet2.setColor(Color.rgb(0, 0, 220));
                        dataSets.add(LineDataSet2);

                        LineData data = new LineData(xAxis, dataSets);
                        mChart.setData(data);
                        mChart.setDescription("");
                        mChart.animateXY(2000, 2000);
                        mChart.invalidate();
                    } else {
                        Toast.makeText(
                                getActivity(),
                                "Data Not found : ", Toast.LENGTH_LONG).show();
                        if (this.dialog.isShowing()) {
                            this.dialog.dismiss();
                        }
                    }
                    if (this.dialog.isShowing()) {
                        this.dialog.dismiss();
                    }
                }else {
                    if (this.dialog.isShowing()) {
                        this.dialog.dismiss();
                    }
                    Toast.makeText(
                            getActivity(),
                            "Data Not found ", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                if (this.dialog.isShowing()) {
                    this.dialog.dismiss();
                }
            }
        }
    }
    private static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

}
