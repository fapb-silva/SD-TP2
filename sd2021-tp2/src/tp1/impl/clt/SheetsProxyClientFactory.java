package tp1.impl.clt;

import java.net.URI;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import tp1.api.service.java.Spreadsheets;
import tp1.impl.clt.common.RetrySheetsProxyClient;
import tp1.impl.clt.rest.RestSheetsProxyClient;
import tp1.impl.discovery.Discovery;
import tp1.impl.srv.Domain;

public class SheetsProxyClientFactory {
	private static final String SERVICE = "sheets_proxy";

	private static final String REST = "/rest";

	private static final long CACHE_CAPACITY = 10;
	
	static LoadingCache<URI, Spreadsheets> sheets = CacheBuilder.newBuilder().maximumSize(CACHE_CAPACITY)
			.build(new CacheLoader<>() {
				@Override
				public Spreadsheets load(URI uri) throws Exception {
					Spreadsheets client;
					if (uri.toString().endsWith(REST))
						client = new RestSheetsProxyClient(uri);
					else
						throw new RuntimeException("Unknown service type..." + uri);
					
					return new RetrySheetsProxyClient(client);
				}
			});
	
	public static Spreadsheets get() {
		return get(String.format("%s:%s", Domain.get(), SERVICE));
	}
	
	public static Spreadsheets get( String fullname ) {
		URI[] uris = Discovery.getInstance().findUrisOf(String.format("%s:%s", Domain.get(), SERVICE), 1);
		return with(uris[0].toString());
	}
	
	public static Spreadsheets with(String uriString) {
		return sheets.getUnchecked( URI.create(uriString));					
	}
}
