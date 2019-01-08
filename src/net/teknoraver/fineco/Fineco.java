package net.teknoraver.fineco;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;

public class Fineco {
	static {
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));

		HttpParams params = new BasicHttpParams();

		httpclient = new DefaultHttpClient(new SingleClientConnManager(params, schemeRegistry), params);

		params.setParameter(ClientPNames.DEFAULT_HOST, new HttpHost("mobile.fineco.it", 443, "https"));
		params.setParameter(CoreProtocolPNames.USER_AGENT, "Mozilla/5.0 (Linux; U; Android 2.2.2; it-it; Nexus One Build/FRG83G) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
	}

	private static final Logger log = Logger.getLogger("Fineco");
	private static final DefaultHttpClient httpclient;

	// variabili conto
	private static String mid;
	private static String cc;

	// Patterns
	private static final Pattern midp = Pattern.compile("mid=(\\w{1,38})");
	private static final Pattern ccp = Pattern.compile("Conto</a>.(\\d+).EUR<br/>");
	private static final Pattern saldop = Pattern.compile("Disp\\.: (.+)<br/>");
	private static final Pattern movp = Pattern.compile("\">\\d+\\).(.+?).\n\t\t\t(.+?)</a><br/>", Pattern.MULTILINE);
	private static final Pattern contop = Pattern.compile("<p>(.+?)<br/>\\s*?<a", Pattern.DOTALL);
	private static final Pattern cartap = contop;
	private static final Pattern movcp = Pattern.compile("<a href=\\\"ic\\?func=ca/C_DETMOVIM.*?\\\">(.*?)\\s*([\\d.,]+)</a>");
	private static final Pattern dettcp = Pattern.compile("<p>(.*?)<a", Pattern.DOTALL);

	// URLs
	private static final String page = "/Provider/ic";

	static void login(String user, String pass) throws Exception {
		mid = grep(midp, page + "?func=CHK_LOGIN&userId=" + user + "&password=" + pass);

		if(mid == null)
			throw new FinecoException("Can't login");

		log.info("Got mid: " + mid);
	}

	static String saldo() throws Exception {
		return grep(saldop, page + "?func=ba/B_MENUB&mid=" + mid);
	}

	static String conto() throws Exception {
		return grep(contop, page +"?func=ba/B_DETCONTO&mid=" + mid + "&codicecc=" + cc() + "&valuta=EUR")
			.replaceAll("\\s*<br/>", "\n");
	}

	static String carta() throws Exception {
		return grep(cartap, page +"?func=ca/C_MENUC&mid=" + mid)
			.replace("<br/>", "\n").replace("\t", "");
	}

	static String cc() throws Exception {
		if(cc == null)
			cc = grep(ccp, page +"?func=ba/B_MENUB&mid=" + mid);
		return cc; 
	}

	static ArrayList<Movimento> lista(int pag) throws Exception {
		ArrayList<Movimento> movs = new ArrayList<Movimento>(); 

		Matcher m = movp.matcher(wget(page +"?func=ba/B_MOVIM&mid=" + mid + "&codicecc=" + cc() + "&valuta=EUR&start=" + (pag * 5 - 4)));
		while(m.find())
			movs.add(new Movimento(m.group(1), m.group(2)));

		return movs;
	}

	static ArrayList<Movimento> listac(int pag) throws Exception {
		ArrayList<Movimento> movs = new ArrayList<Movimento>(); 

		Matcher m = movcp.matcher(wget(page +"?func=ca/C_MOVIM&mid=" + mid + "&start=" + pag));
		while(m.find())
			movs.add(new Movimento(m.group(1), m.group(2)));

		return movs;
	}

	static Dettaglio dettaglio(int rif) throws Exception {
		return new Dettaglio(wget(page + "?func=ba/B_DETMOVIM&rif=" + rif + "&mid=" + mid + "&codicecc=" + cc() + "&valuta=EUR")
			.replace("\t", "").replace("&amp;#176;", "°"));
	}

	static String dettaglioc(int rif) throws Exception {
		 return grep(dettcp, page + "?func=ca/C_DETMOVIM&rif=" + rif + "&mid=" + mid)
		 	.replace("\t", "").replace("<br/>", "\n").replace("\n\n", "\n");
	}

	static String wget(String url) throws Exception {
		HttpResponse r = httpclient.execute(new HttpGet(url));
		if(r.getStatusLine().getStatusCode() != 200)
			throw new FinecoException("Bad Status code: " + r.getStatusLine().getStatusCode());
		HttpEntity e = r.getEntity();
		InputStream in = e.getContent();
		byte buf[] = new byte[4096];
		int readed;
		StringBuffer ret = new StringBuffer();
		while((readed = in.read(buf)) != -1) {
			for(int i = 0; i < readed; i++)
				if(buf[i] == -96) // unicode &nbsp;
					buf[i] = 32;
			ret.append(new String(buf, 0, readed));
		}
		e.consumeContent();
		return ret.toString();
	}

	private static String grep(Pattern p, String url) throws Exception {
		HttpResponse r = httpclient.execute(new HttpGet(url));
		if(r.getStatusLine().getStatusCode() != 200)
			throw new FinecoException("Bad Status code: " + r.getStatusLine().getStatusCode());
		HttpEntity e = r.getEntity();
		InputStream in = e.getContent();
		byte buf[] = new byte[4096];
		int readed;
		String ret = null;
		while((readed = in.read(buf)) != -1) {
			for(int i = 0; i < readed; i++)
				if(buf[i] == -96) // unicode &nbsp;
					buf[i] = 32;
			Matcher m = p.matcher(new String(buf, 0, readed));
			if(m.find()) {
				ret = m.group(1);
				break;
			}
		}
		e.consumeContent();
		return ret;
	}
}

class FinecoException extends Exception {
	private static final long serialVersionUID = -2434866595520064509L;

	FinecoException(String string) {
		super(string);
	}
}
