package com.nstech.oa.signalgen;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class MainActivity extends Activity {

	private WebView webView;

	private Button loadButton;

	private WebViewClient client;

	private int[] presetLabels;

	public void checkSoundFiles() {
		File iDir = getFilesDir();
		File[] waves = new File[4];
		int[] fs = getResources().getIntArray(R.array.frequency_presets);
		for (int i = 0; i < 4; i++) {
			waves[i] = new File(iDir, "wave" + i + ".wav");
			if (!waves[i].exists()) {
				int len = 1000 / fs[i];
				len += len % 2;
				double[] sample = new double[len / 2];
				for (int j = 0; j < sample.length; j++)
					sample[j] = Math.sin(fs[i] * Math.PI * j / 8000);
				// convert to 16 bit pcm sound array
				// assumes the sample buffer is normalised.
				int index = 0;
				WavIO wio = new WavIO();
				for (final double dVal : sample) { // scale to maximum amplitude
					final short val = (short) ((dVal * Short.MAX_VALUE));
					wio.myData[index++] = (byte) (val & 0x00ff);
					wio.myData[index++] = (byte) ((val & 0xff00) >>> 8);
				}
				wio.save(waves[i]);
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web);
		webView = (WebView) findViewById(R.id.WebView);
		loadButton = (Button) findViewById(R.id.Load);
		presetLabels = new int[] { R.string.preset1_name,
				R.string.preset2_name, R.string.preset3_name,
				R.string.preset4_name };
		webView.setWebViewClient(client = new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				if (url.equals("file:///android_asset/ui_000.html")) {

				}
			}

		});
		webView.loadUrl("file:///android_asset/ui_000.html");
		loadButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				webView.loadUrl("JavaScript:setwave('file://" + getFilesDir()
						+ "')");
				String js = "alert('Alert from Java');";
				webView.loadUrl("JavaScript:" + js);
			}

		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}