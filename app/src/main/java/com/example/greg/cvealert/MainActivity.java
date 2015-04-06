package com.example.greg.cvealert;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//Initial commit

public class MainActivity extends ActionBarActivity {

    //initialize placeholder fragment no-ui
    PlaceholderFragment taskFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //If the fragment has not been created before, create fragment
        if (savedInstanceState == null) {
            taskFragment = new PlaceholderFragment();
            getFragmentManager().beginTransaction().add(taskFragment, "MyFragment").commit();
        //Else find the fragment from the manager
        } else {

            taskFragment = (PlaceholderFragment) getFragmentManager().findFragmentByTag("MyFragment");
        }
        //start downloadTask
        taskFragment.startTask();
    }



    public static class PlaceholderFragment extends Fragment {
    //Create Fragment to run onCreate


        //create an instance of RssTask to download the initial RSS
        RssTask downloadTask;

        public PlaceholderFragment() {
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            // TODO Auto generate method stub
            super.onActivityCreated(savedInstanceState);
            setRetainInstance(true);
        }

        public void startTask()
        {
            if(downloadTask !=null)
            {
                downloadTask.cancel(true);
            }
            else
            {
                downloadTask=new RssTask();
                downloadTask.execute();
            }
        }
    }

    public static class RssTask extends AsyncTask<Void,Void,ArrayList<HashMap<String, String>>>{
    //Async class allows running in the background without stalling the GUI thread

        @Override
        protected ArrayList<HashMap<String, String>> doInBackground(Void... params){

            //String defines location to look for RSS feed
            String downloadURL="http://www.cvedetails.com/vulnerability-feed.php?vendor_id=0&product_id=0&version_id=0&orderby=3&cvssscoremin=0";

            ArrayList<HashMap<String,String>> results = new ArrayList<>();

            //Make GET request and create input stream from returned data
            try {
                URL url = new URL(downloadURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod(("GET"));
                InputStream inputStream = connection.getInputStream();
                //Call class to process the input stream as XML
                results = processXML(inputStream);
            }catch (Exception e) {
                //print exception if cannot fetch RSS from URL
                Log.v("doInBackground", e + "");
            }
            return results;
        }

        @Override
       protected void onPostExecute(ArrayList<HashMap<String, String>> result){
            Log.v("OUTPUT", "" + result);
        }

        public ArrayList<HashMap<String,String>> processXML(InputStream inputStream) throws Exception {

            //Sets up the needed DocumentBuilder Factory and Object to parse the RSS Feed

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document xmlDocument = documentBuilder.parse(inputStream);
            Element rootElement = xmlDocument.getDocumentElement();
            Log.v("processXML", rootElement.getTagName());

            NodeList itemsList = rootElement.getElementsByTagName("item");
            NodeList itemChildren = null;

            Node currentItem = null;
            Node currentChild = null;

            HashMap<String, String> currentMap = null;
            ArrayList<HashMap<String, String>> results;
            results = new ArrayList<>();

            //Loops through the XML tags printing the title contents of each item
            for(int i = 0; i < itemsList.getLength(); i++) {

                currentMap = new HashMap<>();

                currentItem = itemsList.item(i);

                //Log.v("processXML", currentItem.getNodeName());
                itemChildren = currentItem.getChildNodes();

                for (int j = 0; j < itemChildren.getLength(); j++) {

                    currentChild = itemChildren.item(j);

                    if (currentChild.getNodeName().equalsIgnoreCase("title")){
//                        Log.v("processXML", currentChild.getTextContent());
                          currentMap.put("title", currentChild.getTextContent());
                    }
                    if (currentChild.getNodeName().equalsIgnoreCase("description")){
//                        Log.v("processXML", currentChild.getTextContent());
                        currentMap.put("description", currentChild.getTextContent());
                    }
                    if (currentChild.getNodeName().equalsIgnoreCase("link")){
//                        Log.v("processXML", currentChild.getTextContent());
                        currentMap.put("link", currentChild.getTextContent());
                    }
                    if (currentChild.getNodeName().equalsIgnoreCase("pubDate")){
//                        Log.v("processXML", currentChild.getTextContent());
                        currentMap.put("pubDate", currentChild.getTextContent());
                    }
                }

                if(currentMap != null && !currentMap.isEmpty()) {
                    results.add(currentMap);
//                    Log.v("inif", "adding results to array");
                }

            }
//            Log.v("Results:", "" + results);
            return results;

        }

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
}
