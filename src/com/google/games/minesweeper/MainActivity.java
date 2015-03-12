package com.google.games.minesweeper;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	private GameView gameView;
	private Button reset,flag;
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
//		gameView = new GameView(this);
		setContentView(R.layout.activity_main);
		reset = (Button) findViewById(R.id.reset);
		flag = (Button) findViewById(R.id.flag);
		gameView = (GameView) findViewById(R.id.gameview);
		reset.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				gameView.gameState = GameView.STATE_PAUSE;
				gameView.reset();
				gameView.invalidate();
			}
		});
		flag.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				gameView.altKeyDown = !gameView.altKeyDown;
				gameView.invalidate();
			}
		});
		gameView.setFlag(flag);
		gameView.setReset(reset);
	}
	
	@Override
	protected void onPause() {
		gameView.gameState = GameView.STATE_LOST;
		super.onPause();
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
