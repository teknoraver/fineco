package net.teknoraver.fineco;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Movimento {
	String nome, importo;
	public Movimento(String n, String i) {
		nome = n.replaceAll("\\s+", " ");
		importo = i;
	}
}

class Dettaglio {
	private static final Pattern p = Pattern.compile("([+-][\\d.,]+)\\s*(.+)$");
	String data1, data2, importo, testo;
	public Dettaglio(String s) {
		s = s.substring(s.indexOf("<p>") + 3, s.indexOf("<a"));
		String fields[] = s.split("<br/>");
		data1 = fields[0];
		data2 = fields[1];
		Matcher m = p.matcher(fields[2]);
		if(m.find()) {
			importo = m.group(1);
			testo = m.group(2);
		}
	}

	public String toString() {
		return data1 + "\n" + data2 + "\n" + importo + " €\n" + testo;
	}
}

