package net.teknoraver.fineco;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Main extends Activity implements OnClickListener, Runnable {
	private Button lista;

	private TextView text;
	private String textv;

	private String err;
	private ProgressDialog wait;

	static final String MODO = "MODO";
	static final int CONTO = 0, CARTA = 1;
	private int modo;

	private final Handler handler = new Handler();
	private Runnable updater = new Runnable() {
		@Override
		public void run() {
			wait.dismiss();
			if(err == null)
				text.setText(textv);
			else
				new AlertDialog.Builder(Main.this)
					.setTitle(R.string.err)
					.setMessage(err)
					.setIcon(android.R.drawable.stat_notify_error)
					.show();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		text = (TextView)findViewById(R.id.text);
		lista = (Button)findViewById(R.id.lista);

		lista.setOnClickListener(this);

		modo = getIntent().getIntExtra(MODO, CONTO);

		new Thread(this).start();
		wait = ProgressDialog.show(this, getString(R.string.waitt), getString(R.string.waitm));
		err = null;
	}

	@Override
	public void onClick(View view) {
		startActivity(new Intent(this, ListaMovimenti.class).putExtra(MODO, modo));
	}

	@Override
	public void run() {
		try {
			if (modo == CONTO)
				textv = Fineco.conto();
			else if (modo == CARTA)
				textv = Fineco.carta();
		} catch (Exception e) {
			err = getString(R.string.connerr);
			e.printStackTrace();
		}
		handler.post(updater);
	}
}
