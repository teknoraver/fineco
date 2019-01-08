package net.teknoraver.fineco;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ListaMovimenti extends ListActivity implements OnClickListener, Runnable {
	private MovAdapter adapter;
	private int pagina = 1;

	private String err;
//	private ProgressDialog wait;

	private int modo;

	private ArrayList<Movimento> movim;

	private final Handler handler = new Handler();
	private Runnable updater = new Runnable() {
		@Override
		public void run() {
//			wait.dismiss();
			findViewById(R.id.loading).setVisibility(View.GONE);
			if(err == null && movim != null) {
				for(Movimento s : movim)
					adapter.add(s);
				if(movim.size() == 5)
					findViewById(R.id.loadmore).setVisibility(View.VISIBLE);
				pagina++;
			} else
				new AlertDialog.Builder(ListaMovimenti.this)
					.setTitle(R.string.err)
					.setMessage(err)
					.setIcon(android.R.drawable.stat_notify_error)
					.show();
		}
	};

	private class MovAdapter extends ArrayAdapter<Movimento> {
		public MovAdapter(ListActivity l) {
			super(l, R.layout.row);
		}

		@Override
		public View getView (int position, View convertView, ViewGroup parent) {
			if (convertView == null)
				convertView = getLayoutInflater().inflate(R.layout.row, null);
			Movimento m = getItem(position);
			((TextView)convertView.findViewById(R.id.movtxt)).setText(m.nome);
			((TextView)convertView.findViewById(R.id.movimp)).setText(m.importo);
			return convertView;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listamov);

		((Button)findViewById(R.id.loadmore)).setOnClickListener(this);

		adapter = new MovAdapter(this);
		setListAdapter(adapter);

		modo = getIntent().getIntExtra(Main.MODO, Main.CONTO);

		loadMore();
	}

	private void loadMore() {
		findViewById(R.id.loadmore).setVisibility(View.GONE);
		findViewById(R.id.loading).setVisibility(View.VISIBLE);

		new Thread(this).start();
	}

	@Override
	public void onClick(View view) {
		if(view.getId() == R.id.loadmore)
			loadMore();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		try {
			new AlertDialog.Builder(this)
				.setTitle("Movimento #" + ++position)
				.setMessage((modo == Main.CONTO ? Fineco.dettaglio(position) : Fineco.dettaglioc(position)).toString())
				.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		err = null;
		try {
			if(modo == Main.CONTO)
				movim = Fineco.lista(pagina);
			else if(modo == Main.CARTA)
				movim = Fineco.listac(pagina);
		} catch (Exception e) {
			e.printStackTrace();
			err = getString(R.string.connerr);
		}
		handler.post(updater);
	}
}
