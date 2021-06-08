/**
 * 
 */
package tp1.impl.srv.rest;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.glassfish.jersey.server.ResourceConfig;

import tp1.impl.srv.Domain;
import tp1.impl.utils.IP;

/**
 * @author HP
 *
 */
public class RepRestServer extends AbstractRestServer {
	private static final String SON = "/son_";
	private static final String ORIGIN = "/origin";
	public static final int PORT = 1212;
	public static final String SERVICE_NAME = "sheets_rep";

	private static Logger Log = Logger.getLogger(RepRestServer.class.getName());

	private static int isPrimary;
	private static String PrimaryURL;
	
	RepRestServer(int port) {
		super(Log, SERVICE_NAME, port);
	}

	@Override
	void registerResources(ResourceConfig config) {
		config.register(new SheetsRepResources(isPrimary, PrimaryURL));
		config.register(GenericExceptionMapper.class);
		config.register(CustomLoggingFilter.class);
	}

	public static void main(String[] args) throws Exception {
		Domain.set(args.length > 0 ? args[0] : "?");
		// int port = args.length < 2 ? PORT : Integer.valueOf(args[1]);
		isPrimary = args.length < 2 ? 3 : Integer.valueOf(args[1]);
		Log.setLevel(Level.ALL);
		
		new RepRestServer(PORT).start();
		
		//creating znodes
		ZookeeperProcessor zk = new ZookeeperProcessor("localhost:2181,kafka:2181");
		String newPath = null;
		if (isPrimary == 1) {
			PrimaryURL = "";
			newPath = zk.write(ORIGIN, IP.hostAddress(), CreateMode.PERSISTENT);//if first
		}else if(isPrimary == 2) {
			newPath = zk.write(ORIGIN +SON,IP.hostAddress(), CreateMode.EPHEMERAL_SEQUENTIAL);//if second
		}else
			Log.info("******************arguments on discovery are weird args.length<2*******************************");
		
		if(newPath == null)
			Log.info("newPath is null****************************************");
		
		//managing znodes-events on delete or data changes
		zk.getChildren( ORIGIN, new Watcher() {
			@Override
			public void process(WatchedEvent event) {
				int minSon=Integer.MAX_VALUE;
				int sonNumber;
				String newPrimaryPath = null;
				List<String> lst = zk.getChildren( ORIGIN, this);//keep watch
//				lst.stream().forEach( e -> e.split("_")[1] < newPrimary ? newPrimary=e.split("_")[1] : null);
				
				for(String sonPath : lst) {
					sonNumber = Integer.parseInt(sonPath.split("_")[1]);
					if(sonNumber < minSon) {
						minSon = sonNumber;
						newPrimaryPath = sonPath;
					}
				}
				
				turnIntoPrimary(newPrimaryPath);
			}

			private void turnIntoPrimary(String newPrimaryPath) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		
		
	}
	
	
}
