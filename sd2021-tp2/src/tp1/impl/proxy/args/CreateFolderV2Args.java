package tp1.impl.proxy.args;

public class CreateFolderV2Args {
	final String path;
	final boolean autorename;

	public CreateFolderV2Args(String path, boolean autorename) {
		this.path = path;
		this.autorename = autorename;
	}
}