/**
 * 
 */
package tp1.impl.proxy.args;

/**
 * @author HP
 *
 */
public class UploadArgs {

	public String path, mode; 
	public boolean mute, strict_conflict, autorename;
	
	public UploadArgs(String path, String mode,boolean autorename, boolean mute, boolean strict_conflict){
		this.path = path;
		this.mode= mode;
		this.autorename = autorename;
		this.mute = mute;
		this.strict_conflict = strict_conflict;
	}
	
	
}
