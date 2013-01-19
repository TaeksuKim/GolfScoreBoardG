package org.dolicoli.android.golfscoreboardg.fragments;

import org.dolicoli.android.golfscoreboardg.MainActivity;
import org.dolicoli.android.golfscoreboardg.R;
import org.dolicoli.android.golfscoreboardg.fragments.main.CurrentGameFragment;
import org.dolicoli.android.golfscoreboardg.fragments.main.GameResultHistoryFragment;
import org.dolicoli.android.golfscoreboardg.fragments.main.HandicapSimulatorFragment;
import org.dolicoli.android.golfscoreboardg.fragments.main.PlayerRankingFragment;
import org.holoeverywhere.ArrayAdapter;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.app.ListFragment;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class MenuFragment extends ListFragment {
	public MenuFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.menu_fragment, null);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		MenuAdapter colorAdapter = new MenuAdapter(
				getActivity(),
				R.layout.menu_list_item,
				new MenuBarItem[] {
						new MenuBarItem(R.drawable.ic_menubar_current, "���� ����"),
						new MenuBarItem(R.drawable.ic_menubar_history, "���� ����"),
						new MenuBarItem(R.drawable.ic_menubar_player, "���� ���"),
						new MenuBarItem(R.drawable.ic_menubar_handicap,
								"�ڵ�ĸ ���") });
		setListAdapter(colorAdapter);
	}

	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {
		Fragment newContent = null;
		switch (position) {
		case 0:
			newContent = new CurrentGameFragment();
			break;
		case 1:
			newContent = new GameResultHistoryFragment();
			break;
		case 2:
			newContent = new PlayerRankingFragment();
			break;
		case 3:
			newContent = new HandicapSimulatorFragment();
			break;
		}
		if (newContent != null)
			switchFragment(newContent);
	}

	private void switchFragment(Fragment fragment) {
		if (getActivity() == null)
			return;

		if (getActivity() instanceof MainActivity) {
			MainActivity fca = (MainActivity) getActivity();
			fca.switchContent(fragment);
		}
	}

	private static class MenuBarItem {
		int icon;
		String text;

		public MenuBarItem(int icon, String text) {
			this.icon = icon;
			this.text = text;
		}
	}

	private static class MenuViewHolder {
		ImageView imageView;
		TextView titleTextView;
	}

	private static class MenuAdapter extends ArrayAdapter<MenuBarItem> {

		private static int TYPE_SECTION_HEADER = 1;

		private int textViewResourceId = 0;
		private MenuViewHolder holder;

		public MenuAdapter(Context context, int textViewResourceId,
				MenuBarItem[] objects) {
			super(context, textViewResourceId, objects);
			this.textViewResourceId = textViewResourceId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(textViewResourceId, null);
				holder = new MenuViewHolder();

				holder.imageView = (ImageView) v.findViewById(R.id.ImageView);
				holder.titleTextView = (TextView) v
						.findViewById(R.id.TitleTextView);

				v.setTag(holder);
			} else {
				holder = (MenuViewHolder) v.getTag();
			}

			int count = getCount();
			if (count < 1)
				return v;

			if (position < 0 || position > count - 1)
				return v;

			MenuBarItem menuBarItem = getItem(position);
			if (menuBarItem == null)
				return v;

			holder.imageView.setImageResource(menuBarItem.icon);
			holder.titleTextView.setText(menuBarItem.text);

			return v;
		}

		@Override
		public int getItemViewType(int position) {
			if (position == 1) {
				return TYPE_SECTION_HEADER;
			}
			return super.getItemViewType(position);
		}

	}
}
