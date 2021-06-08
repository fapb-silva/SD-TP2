package tp1.impl.srv.rest;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.server.ResourceConfig;

import tp1.impl.proxy.SheetsResourcesProxy;
import tp1.impl.srv.Domain;

public class SheetsProxyServer extends AbstractRestServer {

	public static final int PORT = 9876;
	public static final String SERVICE_NAME = "sheets_proxy";
	public static boolean clean;
	
	private static Logger Log = Logger.getLogger(SheetsProxyServer.class.getName());

	
	SheetsProxyServer( int port ) {
		super(Log, SERVICE_NAME, port);
	}
	
	@Override
	void registerResources(ResourceConfig config) {
		SheetsResourcesProxy proxyResource = new SheetsResourcesProxy(clean);
		config.register( proxyResource ); 
		config.register( GenericExceptionMapper.class );
		config.register( CustomLoggingFilter.class);
	}
	
	public static void main(String[] args) throws Exception {
		Domain.set(args.length > 0 ? args[0] : "?");	
		//int port = args.length < 2 ? PORT : Integer.valueOf(args[1]);
		clean = args.length < 2 ? false : Boolean.parseBoolean(args[1]);//if args not length<2 then "false" else define "clean"
		
		Log.setLevel( Level.ALL );
//		if(clean)Log.info("*********BOOLEAN CAUGHT AS TRUE**********");
//		else Log.info("*********BOOLEAN CAUGHT AS FALSE**********");
//		Log.info("**********CLEAN VALUE IS "+args[1]+"********");
		new SheetsProxyServer(PORT).start();
		
	}

}
