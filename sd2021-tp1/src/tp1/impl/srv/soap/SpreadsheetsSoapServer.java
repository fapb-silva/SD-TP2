package tp1.impl.srv.soap;


import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.xml.ws.Endpoint;
import tp1.impl.discovery.Discovery;
import tp1.impl.srv.Domain;
import tp1.impl.utils.IP;


public class SpreadsheetsSoapServer {

	public static final int PORT = 14567;
	public static final String SERVICE_NAME = "sheets";
	public static String SERVER_BASE_URI = "http://%s:%s/soap";

	private static Logger Log = Logger.getLogger(SpreadsheetsSoapServer.class.getName());

	public static void main(String[] args) throws Exception {
		Domain.set(args.length > 0 ? args[0] : "?");

//		System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
//		System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
//		System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
//		System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");

		Log.setLevel(Level.FINER);

		String ip = IP.hostAddress();
		String serverURI = String.format(SERVER_BASE_URI, ip, PORT);
		String FullServiceName = String.format("%s:%s", Domain.get(), SERVICE_NAME);

		Endpoint.publish(serverURI, new SoapSpreadsheetsWebService());

		Discovery.getInstance().announce(FullServiceName, serverURI);

		Log.info(String.format("%s Soap Server ready @ %s\n", SERVICE_NAME, serverURI));

	}
}
