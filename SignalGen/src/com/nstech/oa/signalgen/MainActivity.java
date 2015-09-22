package com.nstech.oa.signalgen;

import com.nstech.oa.signalgen.ui.Slider;
import com.nstech.oa.signalgen.ui.Slider.OnSlidervalueChangedListener;
import com.nstech.oa.signalgen.ui.Slider.SliderValueChangedEvent;

import android.app.Activity;
//import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
//import android.media.AudioTrack.OnPlaybackPositionUpdateListener;
import android.os.Bundle;
//import android.os.Handler;
//import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class MainActivity extends Activity {

	private class Generator implements Runnable {

		@Override
		public void run() {
			while (playing) {
				double[] sample = new double[buffer.getCapacity() / 2];
				for (int i = 0; i < sample.length; i++) {
					sample[i] = Math.sin(frequency * Math.PI * time / 8000);
					time++;
					if (time > (1 << 16))
						time %= (1 << 16);
				}
				console("generator generated");
				// convert to 16 bit pcm sound array
				// assumes the sample buffer is normalised.
				for (final double dVal : sample) { // scale to maximum amplitude
					final short val = (short) ((dVal
							* volumeSlider.getSliderValue() * Short.MAX_VALUE));
					buffer.getoStream().write((byte) (val & 0x00ff));
					buffer.getoStream().write((byte) ((val & 0xff00) >>> 8));

				}
				console("generator written");
			}
		}

	}

	private class Player implements Runnable {

		public AudioTrack audioTrack;

		public void run() {
			// handler.post(new Runnable() {
			//
			// public void run() {
			console("player starting");
			audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
					AudioFormat.CHANNEL_OUT_MONO,
					AudioFormat.ENCODING_PCM_16BIT, buffer.getCapacity(),
					AudioTrack.MODE_STREAM);
			console("audio track created");

			// audioTrack.setLoopPoints(0, buffer.getCapacity() / 4, -1);
			console("player attempting first read");
			for (int i = 0; i < 400; i++) {
				stage[0] = (byte) buffer.getiStream().read();
				audioTrack.write(stage, 0, 1);
			}
			audioTrack.play();
			try {
				Thread.sleep(350);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			while (playing) {
				console("player iteration");
				int n = buffer.getiStream().available();
				for (int i = 0; i < n; i++) {
					stage[0] = (byte) buffer.getiStream().read();
					audioTrack.write(stage, 0, 1);
				}
				console("player read");
				try {
					Thread.sleep(n - 50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// synchronized (signal) {
			// console("player waiting");
			// while (playing) {
			// try {
			// signal.wait();
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }
			// }
			// }
			audioTrack.pause();
			audioTrack.flush();
			audioTrack.release();
			console("player exiting");

		}
		// });
		// }
	}

	private int frequency;
	private boolean playing = false;

	private Button playButton;
	private Button[] presets;

	private TextView frequencyState;
	private TextView playState;

	private LinearLayout console;

	private Slider volumeSlider;

	// private Handler handler = new Handler();

	private Generator generator;
	private Player player;
	private Thread generatorThread;
	private Thread playerThread;
	private Object signal = new Object();

	// private Handler handler;

	private BlockingBuffer buffer = new BlockingBuffer(800, 100);
	private byte[] stage = new byte[800];
	private double time = 0;

	// private int position = 0;

	protected void console(final String text) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				TextView view = new TextView(MainActivity.this);
				LayoutParams params = new LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				params.setMargins(1, 1, 1, 1);
				view.setLayoutParams(params);
				view.setText(text);
				view.setTextSize(10f);
				console.addView(view, 0);
			}

		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// handler = new Handler(this.getMainLooper());
		setContentView(R.layout.activity_main);
		frequency = getResources().getIntArray(R.array.frequency_presets)[0];
		generator = new Generator();
		player = new Player();
		playButton = (Button) findViewById(R.id.PlayButton);
		presets = new Button[] { (Button) findViewById(R.id.Preset1),
				(Button) findViewById(R.id.Preset2),
				(Button) findViewById(R.id.Preset3),
				(Button) findViewById(R.id.Preset4) };
		frequencyState = (TextView) findViewById(R.id.FrequencyState);
		playState = (TextView) findViewById(R.id.Status);
		console = (LinearLayout) findViewById(R.id.Console);
		volumeSlider = (Slider) findViewById(R.id.VolumeSlider);
		// volumeSlider.setEnabled(false);
		volumeSlider.setSliderMin(0);
		volumeSlider.setSliderMax(1f);
		volumeSlider.setSliderValue(1f);
		console("AudioTrack.getMinVolume(): " + AudioTrack.getMinVolume());
		console("AudioTrack.getMaxVolume(): " + AudioTrack.getMaxVolume());
		final int[] presetLabels = { R.string.preset1_name,
				R.string.preset2_name, R.string.preset3_name,
				R.string.preset4_name };
		playButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				playing = !playing;
				for (int i = 0; i < 4; i++)
					presets[i].setEnabled(!playing);
				playButton.setText(!playing ? R.string.play_label
						: R.string.stop_label);
				playState.setText(!playing ? R.string.status_idle
						: R.string.status_playing);
				// volumeSlider.setEnabled(playing);
				if (playing) {
					generatorThread = new Thread(generator);
					generatorThread.start();
					playerThread = new Thread(player);
					playerThread.start();
				} else {
					synchronized (signal) {
						generatorThread.interrupt();
						playerThread.interrupt();
						signal.notify();
					}
					if (generatorThread.isAlive())
						try {
							generatorThread.join();
							generatorThread = null;
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					if (playerThread.isAlive())
						try {
							playerThread.join();
							playerThread = null;
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				}
			}

		});
		for (int i = 0; i < 4; i++) {
			final int j = i;
			presets[j].setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					frequency = getResources().getIntArray(
							R.array.frequency_presets)[j];
					frequencyState.setText(presetLabels[j]);
				}

			});
		}
		volumeSlider
				.setOnSlidervalueChangedListener(new OnSlidervalueChangedListener() {

					@Override
					public void onSliderChanged(SliderValueChangedEvent args) {
						// ((AudioManager)
						// getSystemService(Context.AUDIO_SERVICE)).
						// if (playing)
						// // player.audioTrack.setVolume(args.getNewValue());
						// ((AudioManager)
						// getSystemService(Context.AUDIO_SERVICE))
						// .setStreamVolume(AudioManager.STREAM_MUSIC,
						// (int) args.getNewValue(), 0);
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
