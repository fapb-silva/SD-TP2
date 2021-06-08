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

import tp1.impl.clt.RepSheetsClientFactory;
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

	private static int isOG;
	private static int isPrimary;
	private static String PrimaryURL;

	RepRestServer(int port) {
		super(Log, SERVICE_NAME, port);
		isPrimary = 0;
		isOG = 0;
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
		isOG = args.length < 2 ? 3 : Integer.valueOf(args[1]);

		Log.setLevel(Level.ALL);

		String ip = IP.hostAddress();
		String serverURI = String.format(SERVER_BASE_URI, ip, PORT);

		// creating znodes
		ZookeeperProcessor zk = new ZookeeperProcessor("localhost:2181,kafka:2181");
		String newPath = null;
		if (isOG == 1) {
			newPath = zk.write(ORIGIN, CreateMode.PERSISTENT);// if first
			newPath = zk.write(ORIGIN + SON, serverURI, CreateMode.EPHEMERAL_SEQUENTIAL);// if second
			isPrimary = 1;
			PrimaryURL = serverURI;
		} else if (isOG > 1) {
			newPath = zk.write(ORIGIN + SON, serverURI, CreateMode.EPHEMERAL_SEQUENTIAL);// if second
		}

		if (newPath == null)
			Log.info("newPath is null****************************************");

		new RepRestServer(PORT).start();

		// managing znodes-events on delete or data changes
		zk.getChildren(ORIGIN, new Watcher() {
			@Override
			public void process(WatchedEvent event) {// decide newPrimary
				int minSon = Integer.MAX_VALUE;
				int sonNumber;
				String newPrimaryPath = null;
				List<String> lst = zk.getChildren(ORIGIN, this);// keep watch
//				lst.stream().forEach( e -> e.split("_")[1] < newPrimary ? newPrimary=e.split("_")[1] : null);

				for (String sonPath : lst) {
					sonNumber = Integer.parseInt(sonPath.substring(sonPath.lastIndexOf("_") + 1));
					if (sonNumber < minSon) {
						minSon = sonNumber;
						newPrimaryPath = sonPath;
					}
				}

				turnIntoPrimary(lst, newPrimaryPath);

			}

			private void signalIsPrimary(List<String> lst, String newPrimaryPath) {
				for (String secondary : lst) {
					if (!secondary.equals(newPrimaryPath)) {
						String URIsec = new String(zk.getData(secondary));
						try {
							var res = RepSheetsClientFactory.with(URIsec).turnIntoPrimary(newPrimaryPath);
						} catch (Exception e) {
							e.getMessage();
						}
					}
				}

			}// I dont think I need spread cause only needs to affect self

			private void turnIntoPrimary(List<String> lst, String newPrimaryPath) {
				String newPrimaryURI = new String(zk.getData(newPrimaryPath));
				try {
					var res = RepSheetsClientFactory.with(newPrimaryURI).turnIntoPrimary(newPrimaryPath);
				} catch (Exception e) {
					e.getMessage();
				}
				signalIsPrimary(lst, newPrimaryPath);
			}

		});

	}

}
