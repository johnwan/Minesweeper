package com.google.games.minesweeper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

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
//		gameView.gameState = GameView.STATE_LOST;
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
		switch(id){
		case (R.id.save):
			gameView.save();
			return true;
		case (R.id.load):
			gameView.load();
			gameView.invalidate();
			return true;
		case (R.id.setting):
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("Settings");
			alert.setMessage("Enter Size of Board and Mines Number");
			View dialoglayout = getLayoutInflater().inflate(R.layout.alert_dialog, null);
			final EditText width = (EditText) dialoglayout.findViewById(R.id.width);
			
			final EditText height = (EditText) dialoglayout.findViewById(R.id.height);
			
			final EditText mine = (EditText) dialoglayout.findViewById(R.id.mine);

			alert.setView(dialoglayout);
			alert.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					int widthNo,heightNo,mineNo;
					try{
					widthNo = Integer.parseInt(width.getText().toString());
					heightNo = Integer.parseInt(height.getText().toString());
					mineNo = Integer.parseInt(mine.getText().toString());

					if(widthNo < 8 || widthNo > 11){
						Toast.makeText(getBaseContext(), "Board Width should be in range 8-11", Toast.LENGTH_SHORT).show();
						return;
					}
					if(heightNo < 8 || heightNo > 14){
						Toast.makeText(getBaseContext(), "Board Height should be in range 8-14", Toast.LENGTH_SHORT).show();
						return;
					}
					if(mineNo < 10 || mineNo > 30){
						Toast.makeText(getBaseContext(), "Mines number should be in range 10-30", Toast.LENGTH_SHORT).show();
						return;
					}
					//reset a new game with new param
					gameView.setBoardWidth(widthNo);
					gameView.setBoardHeight(heightNo);
					gameView.setMinesNum(mineNo);
					gameView.initGame();
					gameView.invalidate();
					}catch(Exception e){
						Toast.makeText(getBaseContext(), "Field cannot be blank!", Toast.LENGTH_SHORT).show();
					}
				}
			  });
	
			alert.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
				}
			  });
			alert.show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
