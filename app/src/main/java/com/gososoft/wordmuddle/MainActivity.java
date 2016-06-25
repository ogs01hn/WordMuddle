package com.gososoft.wordmuddle;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "EGGA";
    String url;

    private List<String> defList = new ArrayList<>();
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getWord(View v){


        assert ((EditText)findViewById(R.id.editTextWord)) != null;
        url = "http://www.dictionaryapi.com/api/v1/references/sd3/xml/"+ ((EditText)findViewById(R.id.editTextWord)).getText()  +"?key=" + getResources().getString(R.string.apiKey);

        Log.i(TAG, "getWord: "+((EditText)findViewById(R.id.editTextWord)).getText()+" //"+url) ;
        GetWordTask gWordATask = new GetWordTask(this);
        gWordATask.execute(url);
    }

    private class GetWordTask extends AsyncTask<String, Void, String> {
        private Activity context;

        public GetWordTask(Activity context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... urls) {

            StringBuffer output = new StringBuffer("");



            InputStream stream = null;
            URL url;
            try {
                url = new URL(urls[0]);
                URLConnection connection = url.openConnection();

                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                httpConnection.setRequestMethod("GET");
                httpConnection.connect();

                if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    stream = httpConnection.getInputStream();
                    BufferedReader buffer = new BufferedReader( new InputStreamReader(stream));
                    String s = "";
                    while ((s = buffer.readLine()) != null)
                        output.append(s);
                }
            } catch (MalformedURLException e) {
                Log.e("Error", "Unable to parse URL", e);
            } catch (IOException e) {
                Log.e("Error", "IO Exception", e);
            }

            Log.i("egga", "doInBackground: "+output.toString());

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = null;
            try {
                dBuilder = dbFactory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }

            InputSource is = new InputSource(new StringReader(output.toString()));
            Document doc = null;
            try {
                doc = dBuilder.parse(is);
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                doc.getDocumentElement().normalize();

                NodeList nList = doc.getElementsByTagName("fl");
                NodeList cList;

                String egga="";


                for(int i =0; i<nList.getLength(); i++){
                    Node fl = nList.item(i);


                    if(fl.getTextContent().toString().equals("noun")) {

                        Node sibling = fl;

                        Log.i(TAG, "doInBackground: "+ sibling.getNextSibling().getNodeName().toString());

                        //egga = egga + sibling.getTextContent().toString();

                        while ( sibling.getNextSibling() != null) {
                            Log.i(TAG, "doInBackground: "+ sibling.getNextSibling().getNodeName().toString());

                                sibling = sibling.getNextSibling();
                           // egga = egga + sibling.getTextContent().toString();

                            if(sibling.getNodeName().toString().equals("def")){
                                egga = egga + sibling.getTextContent().toString();
                            }
                        }

                        break;
                    }
                }



                return egga;

            } catch (Exception e) {

                e.printStackTrace();
            }

            return "";
        }


        @Override
        protected void onPostExecute(String xml) {

            TextView txv = (TextView)findViewById(R.id.textViewDefinition);

            txv.setText(xml);


        }



    }
}
