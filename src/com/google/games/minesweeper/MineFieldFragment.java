package com.google.games.minesweeper;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class MineFieldFragment extends Fragment {

	public MineFieldFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		GameView gameView = new GameView(getActivity());
		return gameView;
	}

}
