package ninja.siden.sirusi;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Request {

	static final Set<String> supported;

	static final Map<String, String> types = new HashMap<>();

	static {
		types.put("jpg", "jpeg");
		types.put("jpeg", "jpeg");
		types.put("bmp", "bmp");
		types.put("gif", "gif");
		types.put("png", "png");
		types.put("wbmp", "vnd.wap.wbmp");
		supported = types.keySet();
	}

	String imgroot = "";
	String bucket = "";
	String seed = "0000000";
	String type = "png";

	int size = 48;

	public String getImgroot() {
		return imgroot;
	}

	public void setImgroot(String imgroot) {
		this.imgroot = imgroot;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	public String getBucket() {
		return this.bucket;
	}

	public void setSeedtype(String seedtype) {
		if (4096 < seedtype.length()) {
			return;
		}
		this.seed = seedtype;
		int index = seedtype.lastIndexOf('.');
		if (0 < index) {
			String t = seedtype.substring(index + 1, seedtype.length());
			if (2 < t.length() && supported.contains(t)) {
				this.type = t;
				this.seed = seedtype.substring(0, index);
			}
		}
	}

	public String getSeedtype() {
		return this.seed + "." + this.type;
	}

	public String getContentType() {
		String t = types.get(this.getType());
		if (t != null) {
			return "image/" + t;
		}
		return "application/octet-stream";
	}

	public void setS(String s) {
		if (s != null && 0 < s.length() && s.matches("\\d{1,3}")) {
			int i = Integer.parseInt(s);
			if (i < 4097) {
				this.size = i;
			}
		}
	}

	public String getS() {
		return String.valueOf(this.size);
	}

	public String getSeed() {
		return this.seed;
	}

	public String getType() {
		return this.type;
	}

	public int getSize() {
		return this.size;
	}
}
