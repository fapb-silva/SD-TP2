package tp1.impl.clt;

import java.net.URI;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import tp1.api.service.java.RepSpreadsheets;
import tp1.impl.clt.common.RetrySheetsRepClient;
import tp1.impl.clt.rest.RestSheetsRepClient;
import tp1.impl.discovery.Discovery;
import tp1.impl.srv.Domain;

public class RepSheetsClientFactory {
	private static final String SERVICE = "sheets_rep";

	private static final String REST = "/rest";

	private static final long CACHE_CAPACITY = 10;
	
	static LoadingCache<URI, RepSpreadsheets> sheets = CacheBuilder.newBuilder().maximumSize(CACHE_CAPACITY)
			.build(new CacheLoader<>() {
				@Override
				public RepSpreadsheets load(URI uri) throws Exception {
					RepSpreadsheets client;
					if (uri.toString().endsWith(REST))
						client = new RestSheetsRepClient(uri);
					else
						throw new RuntimeException("Unknown service type..." + uri);
					
					return new RetrySheetsRepClient(client);
				}
			});
	
	public static RepSpreadsheets get() {
		return get(String.format("%s:%s", Domain.get(), SERVICE));
	}
	
	public static RepSpreadsheets get( String fullname ) {
		URI[] uris = Discovery.getInstance().findUrisOf(String.format("%s:%s", Domain.get(), SERVICE), 1);
		return with(uris[0].toString());
	}
	
	public static RepSpreadsheets with(String uriString) {
		return sheets.getUnchecked( URI.create(uriString));					
	}

}
