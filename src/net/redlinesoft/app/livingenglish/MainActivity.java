package net.redlinesoft.app.livingenglish;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {
  
	String url = "http://query.yahooapis.com/v1/public/yql?q=select%20title%2Clink%20from%20feed%20where%20url%3D%22https%3A%2F%2Fgdata.youtube.com%2Ffeeds%2Fapi%2Fplaylists%2FSP0A00C7530C185317%3Fv%3D2%26max-results%3D50%22%20and%20link.rel%3D%22alternate%22&format=json&callback=";
 
	int data_size = 0;
	int attemp = 0;

	public ArrayList<HashMap<String, String>> MyArrList = new ArrayList<HashMap<String, String>>();
	public HashMap<String, String> map;
	

	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main); 

		// break policy
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		} 
		
		ListView listItem = (ListView) findViewById(R.id.listItem);	 
	 
		// TODO this is fucking thread		
		if (checkNetworkStatus()) {			
			
			// loadcontent
			loadContent();
			
			// check timeout
			while ((data_size<0) & (attemp < 30)) {
				try {
					attemp=attemp+1;
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();					 
				}
			}
			
			if (data_size>0) {
				LazyAdapter adapter = new LazyAdapter(this, MyArrList);
				listItem.setAdapter(adapter);	  
			} 		
			
		} else {
			Toast.makeText(getBaseContext(), "No network connection!",Toast.LENGTH_SHORT).show();
		}

		// OnClick Item
		listItem.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Intent fanPageIntent = new Intent(Intent.ACTION_VIEW);
				fanPageIntent.setType("text/url");
				fanPageIntent.setData(Uri
						.parse(MyArrList.get(arg2).get("link")));
				startActivity(fanPageIntent);
			}
		});

	}

	public void loadContent() {

		// load json data
		try {
			JSONObject json_data = new JSONObject(getJSONUrl(url));
			JSONObject json_query = json_data.getJSONObject("query");
			JSONObject json_result = json_query.getJSONObject("results");
			JSONArray json_entry = json_result.getJSONArray("entry");
			Log.d("JSON", String.valueOf(json_entry.length()));

			for (int i = 0; i < json_entry.length(); i++) {

				// parse json
				JSONObject c = json_entry.getJSONObject(i);
				Log.d("JSON", c.getString("title").toString());
				Log.d("JSON", c.getJSONObject("link").getString("href")
						.toString());
				String link = c.getJSONObject("link").getString("href")
						.toString();
				String[] fragments = link.split("&");
				String[] videoid = fragments[0].split("=");
				Log.d("JSON", videoid[1]);

				String title_data = c.getString("title").toString();
				String[] title_fragment = title_data.split("-");

				// put into hashmap
				map = new HashMap<String, String>();
				map.put("title", title_fragment[1].toString().trim());
				map.put("link", c.getJSONObject("link").getString("href"));
				map.put("videoid", videoid[1]);
				MyArrList.add(map);
			}

			data_size = json_entry.length();

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(getBaseContext(), "Cannot connect to server!",Toast.LENGTH_SHORT).show();
		}

	}

	public String getJSONUrl(String url) {
		StringBuilder str = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) { // Download OK
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					str.append(line);
				}
			} else {
				Log.e("Log", "Failed to download file..");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return str.toString();
	}

	public boolean checkNetworkStatus() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		/*
		 * switch (item.getItemId()) { case R.id.menu_share: Log.d("MENU",
		 * "select menu share"); Intent sharingIntent = new Intent(
		 * android.content.Intent.ACTION_SEND); sharingIntent.setType("text/*");
		 * sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
		 * getString(R.string.text_share_subject));
		 * sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
		 * getString(R.string.text_share_body) +
		 * getApplicationContext().getPackageName());
		 * startActivity(Intent.createChooser(sharingIntent,
		 * getString(R.string.menu_share))); startActivity(sharingIntent);
		 * break; case R.id.menu_update: Log.d("MENU", "select menu update");
		 * break; }
		 */
		return false;

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
}
