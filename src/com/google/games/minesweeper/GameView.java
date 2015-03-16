package com.google.games.minesweeper;

import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;

import com.google.games.minesweeper.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
/**
 * The Main functions of the Minesweeper Game
 * @author Long
 */
public class GameView extends View {
	private static final String TAG = "GameView";
	// handler for refresh the UI
	private RefreshHandler mRedrawHandler = new RefreshHandler();

	class RefreshHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			GameView.this.updateView();
			GameView.this.invalidate();
		}

		public void sleep(long delayMillis) {
			this.removeMessages(0);
			sendMessageDelayed(obtainMessage(0), delayMillis);
		}
	};

	// states: start,win,lose,pause.
	public static final int STATE_PLAYING = 0;
	public static final int STATE_WIN = 1;
	public static final int STATE_LOSE = 2;
	public static final int STATE_PAUSE = 3;
	public int gameState;

	private static final Random random = new Random();
	private Paint paint;
	private String message;
	//width and height of each tile
	private static final int tileWidth = 80; // width of each tile
	private static final int tileHeight = 80; // height of each tile
	private static final int tilesCount = 19; // 19 images
	private static final int margin = 50;
	private static final int titleHeight = 30;
	//width and height of the board
	private int boardWidth = 11, boardHeight = 14;
	//mines number from setting
	private int minesNum = 18;
	
	public int getBoardWidth() {
		return boardWidth;
	}

	public void setBoardWidth(int boardWidth) {
		this.boardWidth = boardWidth;
	}

	public int getBoardHeight() {
		return boardHeight;
	}

	public void setBoardHeight(int boardHeight) {
		this.boardHeight = boardHeight;
	}

	public int getMinesNum() {
		return minesNum;
	}

	public void setMinesNum(int minesNum) {
		this.minesNum = minesNum;
	}

	private Bitmap[] tiles;
	
	private int[][] mapSky;
	private int[][] mapGround;
	
	private int tileCountX, tileCountY;
	private int offsetX, offsetY;

	int mineCount;
	int safeCount;

	private int mapX, mapY;
	public boolean altKeyDown = false;

	private static final int MILLIS_PER_TICK = 500;
	long startTime, lastTime;
	long time, remain;
	private Context context;
	private Button reset,flag;

	public GameView(Context context) {
		super(context);
		this.context = context;
		//paint the tile
		paint = new Paint();
		paint.setARGB(255, 60, 60, 200);
		paint.setTextSize(45);
		tiles = new Bitmap[tilesCount];//tilesCount = 19;
		loadTiles();		
		setFocusable(true);//get focus
	}
	
	 public GameView(Context context, AttributeSet attrs) {
	    super(context, attrs);
		this.context = context;
		//paint the tile
		paint = new Paint();
		paint.setARGB(255, 60, 60, 200);
		paint.setTextSize(45);
		tiles = new Bitmap[tilesCount];//tilesCount = 19;
		loadTiles();		
		setFocusable(true);//get focus
	 }
/***
 *  put resources into tiles array
 */
	private void loadTiles() {
		Resources r = this.getContext().getResources();
		for (int i = 0; i < tilesCount; i++) {//store all the 19 images.
			tiles[i] = BitmapFactory.decodeResource(r, R.drawable.i00 + i);
		}
	}
	
/***
 * initial game layout based on setting/screen size
 */
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		// TODO Auto-generated method stub
		super.onLayout(changed, left, top, right, bottom);
		int width = right - left;
		int height = bottom - top;
		initGame();

	}
	
	public void initGame(){
		init(tileWidth*boardWidth+margin*2,tileHeight*boardHeight+titleHeight+margin*2);
//		init(width,height); // used for largest board
		startTime = System.currentTimeMillis();
		updateView();
	}
	
   /***
    * initial the map, calculate the number of tiles.
    * @param width
    * @param height
    */
	private void init(int w, int h) {
		mapX = -1;
		mapY = -1;
		tileCountX = (int) Math.floor((w - margin * 2) / tileWidth);
		tileCountY = (int) Math.floor((h - margin * 2 - titleHeight) / tileHeight);

		offsetX = (w - (tileWidth * tileCountX)) / 2;
		offsetY = (h - (tileHeight * tileCountY) + titleHeight) / 2;

//		mineCount = (int) Math.sqrt(tileCountX * tileCountY) * tileCountX
//				* tileCountY / 100;//number of mines
		mineCount = minesNum;
		reset();
	}
/***
 * initial the two layer of map
 */
	public void reset() {
		int x, y;
		mapSky = new int[tileCountX][tileCountY];
		mapGround = new int[tileCountX][tileCountY];

		for (int i = 0; i < mineCount; i++) {
			// put down mines
			do {
				x = random.nextInt(tileCountX);
				y = random.nextInt(tileCountY);
			} while (mapGround[x][y] == 12);//avoid set mine in same position
			mapGround[x][y] = 12;//12 is the image of mine
           
			increase(x - 1, y - 1);
			increase(x - 1, y);
			increase(x - 1, y + 1);
			increase(x, y + 1);
			increase(x + 1, y + 1);
			increase(x + 1, y);
			increase(x + 1, y - 1);
			increase(x, y - 1);
		}
		// set empty tile to image i09 
		for (x = 0; x < tileCountX; x++) {
			for (y = 0; y < tileCountY; y++) {
				if (mapGround[x][y] == 0)
					mapGround[x][y] = 9;
			}
		}
		safeCount = tileCountX * tileCountY - mineCount;//safe tiles
		time = 0;
		remain = mineCount;//remains mines
		altKeyDown = false;
		// shuffle();
	}
	// save all data into shared preference.
	// Cause SharedPreferences can't handle arrays I implement json object to convert array into a string.
	public void save(){
		int x, y;
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = pref.edit();
		JSONArray groundArray = new JSONArray();
		JSONArray skyArray = new JSONArray();
		for (x = 0; x < tileCountX; x++){
			for (y = 0; y < tileCountY; y++){
				groundArray.put(mapGround[x][y]);
				skyArray.put(mapSky[x][y]);
			}
		}
		editor.putString("ground", groundArray.toString());
		editor.putString("sky", skyArray.toString());
		editor.putLong("remain", remain);
		editor.putInt("state", gameState);
		editor.putInt("safecount", safeCount);
		editor.putBoolean("altkey", altKeyDown);
		editor.putLong("time", time);
		editor.commit();
		Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show();
	}
	// load data from SharedPreferences
	public void load(){
		int x, y;
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		try {
			JSONArray groundArray = new JSONArray(pref.getString("ground", ""));
			JSONArray skyArray = new JSONArray(pref.getString("sky", ""));
			int index = 0;
			for (x = 0; x < tileCountX; x++){
				for (y = 0; y < tileCountY; y++){
					mapGround[x][y] = (Integer) groundArray.get(index);
					mapSky[x][y] = (Integer) skyArray.get(index);
					index++;
				}
			}
			remain = pref.getLong("remain", 0);
			gameState = pref.getInt("state", 0);
			safeCount = pref.getInt("safecount", -1);
			altKeyDown = pref.getBoolean("altkey", false);
			time = pref.getLong("time", 0);
			startTime = System.currentTimeMillis() - time * 1000;
		} catch (JSONException e) {
			Toast.makeText(context, "Loading Failed!", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		Toast.makeText(context, "Loaded!", Toast.LENGTH_SHORT).show();
	}
	private void increase(int x, int y) {
		if (x > -1 && x < tileCountX && y > -1 && y < tileCountY) {
			if (mapGround[x][y] != 12)
				mapGround[x][y]++;
		}
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// Log.v(TAG, "onDraw");
		if (altKeyDown) {//if flag button is pressed
			canvas.drawARGB(255, 255, 0, 0);
			flag.setBackground(context.getResources().getDrawable(R.drawable.i_flag_down));
//			canvas.drawBitmap(tiles[17], 80, 0, paint);
		} else {
			flag.setBackground(context.getResources().getDrawable(R.drawable.i_flag));
//			canvas.drawBitmap(tiles[16], 80, 0, paint);
		}

		if (gameState != STATE_LOSE) {//if it is lost
			reset.setBackground(context.getResources().getDrawable(R.drawable.i_happy));
//			canvas.drawBitmap(tiles[18], 0, 0, paint);
		} else {
			reset.setBackground(context.getResources().getDrawable(R.drawable.i_cry));
//			canvas.drawBitmap(tiles[15], 0, 0, paint);
		}
		
		// updating remaining mines number and time
		message = "Remain£º" + remain + "  Time:" + time + "sec";
		
		canvas.drawText(message, 0, message.length(), 150, 45, paint);
		// draw the tiles
		for (int x = 0; x < tileCountX; x += 1) {
			for (int y = 0; y < tileCountY; y += 1) {
				Rect rDst = new Rect(offsetX + x * tileWidth, offsetY + y
						* tileHeight, offsetX + (x + 1) * tileWidth, offsetY
						+ (y + 1) * tileHeight);
				canvas.drawBitmap(tiles[mapGround[x][y]], null, rDst, paint);
				if (gameState != STATE_LOSE) {
					if (mapSky[x][y] > -1) {
						canvas.drawBitmap(tiles[mapSky[x][y]], null, rDst,
								paint);
						// canvas.drawPoint(rDst.left, rDst.top, p);
					}
				} else {
					if (mapGround[x][y] != 12 && mapSky[x][y] == 10) {
						mapSky[x][y] = 14;//if lose, mark all the mines
					}
					if (mapSky[x][y] > -1 && mapGround[x][y] != 12
							|| mapSky[x][y] == 13 || mapSky[x][y] == 10) {
						canvas.drawBitmap(tiles[mapSky[x][y]], null, rDst,
								paint);//keep all the others tiles
						// canvas.drawPoint(rDst.left, rDst.top, p);
					}
				}
			}
		}

	}

	public Button getReset() {
		return reset;
	}

	public void setReset(Button reset) {
		this.reset = reset;
	}

	public Button getFlag() {
		return flag;
	}

	public void setFlag(Button flag) {
		this.flag = flag;
	}

	private void updateView() {
//		Log.v(TAG, "updateView");
		if (gameState == STATE_PLAYING) {
			time = (System.currentTimeMillis() - startTime) / 1000;
			mRedrawHandler.sleep(MILLIS_PER_TICK);
		}
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int action = event.getAction();
		int x = (int) event.getX();
		int y = (int) event.getY();
		// Log.v(LOG_TAG, "action:" + action + " x:" + x + " y:" + y);

		if (action == MotionEvent.ACTION_DOWN) {
			// Log.v(TAG, "Checking start ********");
			// get which tile has been touched
			mapX = screenX2mapX(x);
			mapY = screenY2mapY(y);

			if (gameState != STATE_LOSE && mapX > -1 && mapY > -1) {
				if (gameState == STATE_PAUSE) {
					gameState = STATE_PLAYING;
					startTime = System.currentTimeMillis();
					updateView();
				}

				if (altKeyDown) {// mark flags state
					if (mapSky[mapX][mapY] == 0) {
						mapSky[mapX][mapY] = 10;
						remain--;
					} else if (mapSky[mapX][mapY] == 10) {
						remain++;
						mapSky[mapX][mapY] = 11;
					} else if (mapSky[mapX][mapY] == 11) {
						mapSky[mapX][mapY] = 0;
					} else if (mapSky[mapX][mapY] == -1) {
						int flags = flag(mapX - 1, mapY - 1)
								+ flag(mapX - 1, mapY)
								+ flag(mapX - 1, mapY + 1)
								+ flag(mapX, mapY + 1)
								+ flag(mapX + 1, mapY + 1)
								+ flag(mapX + 1, mapY)
								+ flag(mapX + 1, mapY - 1)
								+ flag(mapX, mapY - 1);
						if (flags == mapGround[mapX][mapY]) {
							Log.v(TAG, "flags:" + flags);
							open(mapX - 1, mapY - 1);
							open(mapX - 1, mapY);
							open(mapX - 1, mapY + 1);
							open(mapX, mapY + 1);
							open(mapX + 1, mapY + 1);
							open(mapX + 1, mapY);
							open(mapX + 1, mapY - 1);
							open(mapX, mapY - 1);
						}
					}
				} else {
					open(mapX, mapY);
				}
				invalidate();
			} 
		}
		return true;
	}

	private int flag(int x, int y) {
		if (x > -1 && x < tileCountX && y > -1 && y < tileCountY) {
			if (mapSky[x][y] == 10)
				return 1;
		}
		return 0;
	}

	public void open(int x, int y) {// open the tile
		if (x > -1 && x < tileCountX && y > -1 && y < tileCountY) {
			if (mapSky[x][y] == -1)
				return;
			if (mapSky[x][y] == 0 || mapSky[x][y] == 11) {
				if (mapGround[x][y] == 12) {
					mapSky[x][y] = 13;
					gameState = STATE_LOSE;
				} else {
					mapSky[x][y] = -1;
					safeCount--;
					if (safeCount == 0) {
						gameState = STATE_WIN;
						
//						Toast.makeText(getContext(), "Win", Toast.LENGTH_SHORT).show();
						AlertDialog.Builder alert = new AlertDialog.Builder(context);
						alert.setTitle("You win!");
						alert.setMessage("Do you want start a new game?");
						alert.setPositiveButton("New Game",new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
								gameState = GameView.STATE_PAUSE;
								reset();
								invalidate();
							}
						  });
				
						alert.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
							}
						  });
						alert.show();
					
					}
					if (mapGround[x][y] == 9) { // if it is empty, open its neighbor
						open(x - 1, y - 1);
						open(x - 1, y);
						open(x - 1, y + 1);
						open(x, y + 1);
						open(x + 1, y + 1);
						open(x + 1, y);
						open(x + 1, y - 1);
						open(x, y - 1);
					}
				}
			}
		}
	}


	private int screenX2mapX(int c) {//get position of column
		if (c - offsetX < 0)
			return -1;
		int rtn = (c - offsetX) / tileWidth;
		if (rtn >= tileCountX)
			return -1;
		return rtn;
	}

	private int screenY2mapY(int c) {//get position of row
		if (c - offsetY < 0)
			return -1;
		int rtn = (c - offsetY) / tileHeight;
		if (rtn >= tileCountY)
			return -1;
		return rtn;
	}

}
