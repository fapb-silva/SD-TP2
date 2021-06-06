package tp1.impl.proxy;

import java.util.List;

import tp1.api.User;
import tp1.api.service.rest.RestUsers;
import org.pac4j.scribe.builder.api.DropboxApi20;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;



public class UsersResourcesProxy implements RestUsers {

	private static final String apiKey = "qkwowwvw9cjgxcw";
	private static final String apiSecret = "ohnj1xpcazkvtg6";
	private static final String accessTokenStr = "P51dQLGFjpQAAAAAAAAAAe3lDMORWeu_Xpm48OQ7cZTwBNrNM5hwBN-sgpvgjU9B";
	private static final String CREATE_FOLDER_V2_URL = "https://api.dropboxapi.com/2/files/create_folder_v2";
	protected static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
	private OAuth20Service service;
	private OAuth2AccessToken accessToken;
	
	private Gson json;
	
	public UsersResourcesProxy() {
		service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(DropboxApi20.INSTANCE);
		accessToken = new OAuth2AccessToken(accessTokenStr);
		json = new Gson();
		
		
		
		
	}
	
	@Override
	
	public String createUser(User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User getUser(String userId, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User updateUser(String userId, String password, User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User deleteUser(String userId, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<User> searchUsers(String pattern) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User fetchUser(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

}
