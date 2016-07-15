package com.gososoft.wordmuddle;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "EGGA";


    private ArrayList<String> wDefList = new ArrayList<>();
    private ArrayList<String> wordFinalList = new ArrayList<>();
    private ArrayList<String> DefList = new ArrayList<>();
    private ArrayList<String> scrambledWordList = new ArrayList<>();

    private RecyclerView recyclerViewScrbWords;

    private wordsAdapter scrbWordsAdapter;

    ListView listview;

    ArrayAdapter adapterDefs;

    ListView listviewWords;

   // ArrayAdapter adapterWrds;

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

        recyclerViewScrbWords = (RecyclerView) findViewById(R.id.defRecyclerView);

        Resources res = getResources();
        InputStream inpStrm = res.openRawResource(R.raw.nounlist);

        BufferedReader reader = new BufferedReader(new InputStreamReader(inpStrm));


        String line = null;
/*        try {
            line = reader.readLine();
            Log.i(TAG, "onCreate: "+line);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        int count =0;
        try {
            while ((line = reader.readLine()) != null) {

                wDefList.add(line);
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }



        Log.i(TAG, "onCreate: size wDefList"+wDefList.size());

        //((TextView)findViewById(R.id.editTextWord)).setText(defList.get(6));
        listview = (ListView) findViewById(R.id.listViewDefs);

        listviewWords= (ListView) findViewById(R.id.listViewWords);

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






        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();


        if (networkInfo != null && networkInfo.isConnected()) {



                new GetWordTask(this).execute(wDefList);

        }else{
            Toast.makeText(getApplicationContext(), "There is no connection to the Internet!", Toast.LENGTH_LONG).show();
        }



        Log.i(TAG, "getWord: DefList size: "+DefList.toString());
    }

    private class GetWordTask extends AsyncTask<ArrayList<String>, Integer, ArrayList<String>> {
        private Activity context;
        private ArrayList<String> definitionsList = new ArrayList<>();

        ProgressDialog progressDialog;


        public GetWordTask(Activity context) {


            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(MainActivity.this, "WordMuddle", "Loading definitions...");
        }

        @Override
        protected ArrayList<String> doInBackground(ArrayList<String>... wordList) {
            String wordPalabra;
            int w = 0;

            Collections.shuffle(wordList[0]);

            wordFinalList.clear();
            scrambledWordList.clear();
            DefList.clear();



            while(DefList.size()<5) {

                StringBuffer output = new StringBuffer("");


                InputStream stream = null;
                URL url;

                wordPalabra = wordList[0].get(w);
                String urlPre = "http://www.dictionaryapi.com/api/v1/references/sd3/xml/" + wordPalabra + "?key=" + getResources().getString(R.string.apiKey);

                Log.i(TAG, "getWord: " + wordPalabra + " //" + urlPre);


                try {
                    url = new URL(urlPre);
                    URLConnection connection = url.openConnection();

                    HttpURLConnection httpConnection = (HttpURLConnection) connection;
                    httpConnection.setRequestMethod("GET");
                    httpConnection.connect();

                    if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        stream = httpConnection.getInputStream();
                        BufferedReader buffer = new BufferedReader(new InputStreamReader(stream));
                        String s = "";
                        while ((s = buffer.readLine()) != null)
                            output.append(s);
                    }

                    httpConnection.disconnect();

                } catch (MalformedURLException e) {
                    Log.e("Error", "Unable to parse URL", e);
                } catch (IOException e) {
                    Log.e("Error", "IO Exception", e);
                }

                Log.i("egga", "doInBackground: " + output.toString());

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

                    String wordDefinition = "";


                    for (int i = 0; i < nList.getLength(); i++) {
                        Node fl = nList.item(i);


                        if (fl.getTextContent().toString().contains("noun")) {

                            Log.i(TAG, "doInBackground: " + fl.getTextContent().toString());
                            Node sibling = fl;

                            //Log.i(TAG, "doInBackground: "+ sibling.getNextSibling().getNodeName().toString());

                            //egga = egga + sibling.getTextContent().toString();

                            while (sibling != null) {
                                //Log.i(TAG, "doInBackground: "+ sibling.getNextSibling().getNodeName().toString());


                                // egga = egga + sibling.getTextContent().toString();

                                if (sibling.getNodeName().toString().equals("def")) {

                                    NodeList sibChLst = sibling.getChildNodes();

                                    for (int sc = 0; sc < sibChLst.getLength(); sc++) {

                                        Node sibChild = sibChLst.item(sc);

                                        if (sibChild.getNodeName().toString().equals("dt")) {

                                            wordDefinition = wordDefinition + Html.fromHtml(sibChild.getTextContent().toString().substring(1));

                                            if (!wordDefinition.equals("")) {

                                                wordDefinition = wordDefinition.substring(0,1).toUpperCase() + wordDefinition.substring(1);

                                                Random r = new Random();



                                                wordFinalList.add(wordPalabra);

                                                wordPalabra = scramble(r,wordPalabra);

                                                scrambledWordList.add(wordPalabra);

                                                DefList.add(wordDefinition);
                                            }
                                            Log.i(TAG, "doInBackground: sucks" + wordDefinition);

                                            break;
                                        }
                                    }
                                }

                                sibling = sibling.getNextSibling();
                            }

                            break;
                        }
                    }


                } catch (Exception e) {

                    e.printStackTrace();
                }
                w++;
                //publishProgress(w);
            }

            return definitionsList;
        }

        public String scramble(Random random, String inputString)
        {
            // Convert your string into a simple char array:
            char a[] = inputString.toCharArray();

            // Scramble the letters using the standard Fisher-Yates shuffle,
            for( int i=0 ; i<a.length-1 ; i++ )
            {
                int j = random.nextInt(a.length-1);
                // Swap letters
                char temp = a[i]; a[i] = a[j];  a[j] = temp;
            }

            return new String( a );
        }



        @Override
        protected void onPostExecute(ArrayList<String> definitions) {

//            TextView txv = (TextView)findViewById(R.id.textViewDefinition);
            ListView lstv = (ListView)findViewById(R.id.listViewDefs);

//            txv.setText(definitions.get(definitions.size()-1));

            adapterDefs = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, DefList);
            //adapterDefs.notifyDataSetChanged();

            listview.setAdapter(adapterDefs);

/*            adapterWrds = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, wordFinalList);
            adapterWrds.notifyDataSetChanged();

            listviewWords.setAdapter(adapterWrds);*/

            scrbWordsAdapter = new wordsAdapter(MainActivity.this,wordFinalList,scrambledWordList);

            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerViewScrbWords.setLayoutManager(mLayoutManager);
            //recyclerViewScrbWords.setItemAnimator(new DefaultItemAnimator());
            recyclerViewScrbWords.setItemAnimator(new SlideInUpAnimator());

            recyclerViewScrbWords.setAdapter(scrbWordsAdapter);
            //scrbWordsAdapter.notifyDataSetChanged();


            progressDialog.dismiss();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            //progressDialog.setMessage(String.valueOf(values[0]));
        }
    }
}
