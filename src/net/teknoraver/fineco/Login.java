package net.teknoraver.fineco;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class Login extends Activity implements OnClickListener, Runnable {
	private EditText user, pass;
	private CheckBox save;

	private ProgressDialog wait;
	private String err;

	private SharedPreferences sp;
	private static final String USER = "user";
	private static final String PASS = "pass";
	private static final String SAVE = "save";

	private final Handler handler = new Handler();
	private Runnable updater = new Runnable() {
		@Override
		public void run() {
			wait.dismiss();
			if(err == null) {
				if(save.isChecked()) {
					Editor e = sp.edit();
					e.putString(USER, user.getText().toString());
					e.putString(PASS, pass.getText().toString());
					e.putBoolean(SAVE, true);
					e.commit();
				}
//				startActivity(new Intent(Login.this, Main.class));
				startActivity(new Intent(Login.this, Tab.class));
			} else
				new AlertDialog.Builder(Login.this)
					.setTitle(R.string.err)
					.setMessage(err)
					.setIcon(android.R.drawable.stat_notify_error)
					.show();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		user = (EditText)findViewById(R.id.userid);
		pass = (EditText)findViewById(R.id.password);
		save = (CheckBox)findViewById(R.id.save);

		save.setOnClickListener(this);

		((Button)findViewById(R.id.login)).setOnClickListener(this);

		sp = PreferenceManager.getDefaultSharedPreferences(this);

		save.setChecked(sp.getBoolean(SAVE, false));
		if(save.isChecked()) {
			user.setText(sp.getString(USER, null));
			pass.setText(sp.getString(PASS, null));
			login();
		}
	}

	private void login() {
		wait = ProgressDialog.show(this, getString(R.string.loggingt), getString(R.string.loggingm));
		err = null;
		new Thread(this).start();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.save:
			CheckBox save = (CheckBox)view;
			if(save.isChecked())
				new AlertDialog.Builder(Login.this)
					.setTitle(R.string.savet)
					.setMessage(R.string.savem)
					.setIcon(android.R.drawable.ic_dialog_info)
					.show();
			else
				sp.edit().clear();
			break;
		case R.id.login:
			login();
			break;
		}
	}

	@Override
	public void run() {
		try {
			Fineco.login(user.getText().toString(), pass.getText().toString());
		} catch (FinecoException f) {
			err = f.getLocalizedMessage();
			f.printStackTrace();
		} catch (Exception e) {
			err = getString(R.string.connerr);
			e.printStackTrace();
		}
		handler.post(updater);
	}
}
