package net.teknoraver.fineco;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class Tab extends TabActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab);

		Resources res = getResources();
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;

		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("conto")
			.setIndicator(getText(R.string.listco), res.getDrawable(R.drawable.bank))
			.setContent(new Intent(this, Main.class)
			.putExtra(Main.MODO, Main.CONTO));
		tabHost.addTab(spec);
	
		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("carta")
			.setIndicator(getText(R.string.listca), res.getDrawable(R.drawable.card))
			.setContent(new Intent(this, Main.class)
			.putExtra(Main.MODO, Main.CARTA));
		tabHost.addTab(spec);
	}
}
