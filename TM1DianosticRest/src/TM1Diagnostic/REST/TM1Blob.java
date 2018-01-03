package TM1Diagnostic.REST;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.OrderedJSONObject;

public class TM1Blob {

	public TM1Server tm1server;
	public String name;
	public String entity;
	public String entitySet;
	
	private byte[] bytes;

	public TM1Blob(String name, TM1Server tm1server) {
		this.name = name;
		this.tm1server = tm1server;
		
		entity = "Contents('Blobs')/Contents('" + name + "')";
		entitySet = "Contents('Blobs')/Contents";
	}
	
	public TM1Server getServer(){
		return tm1server;
	}

	public String getContent() {
		String s = "";
		try {
			s = new String(bytes, "UTF-8");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return s;
	}

	public boolean readBlobContentFromServer() throws TM1RestException, ClientProtocolException, URISyntaxException, IOException {
		String request = entity + "/Content";
		tm1server.get(request);
		bytes = tm1server.response.getBytes();
		return true;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void remove() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		String request = entity;
		tm1server.delete(request);
	}

	public void setContent(String content){
		bytes = content.getBytes();
		System.out.println("Set bytes to " + content);
		System.out.println("Set bytes to " + new String(bytes));
	}

	public void writeToServer() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		String request = entity + "/Content";
		String content = new String(bytes);
		tm1server.patch(request, content);
	}

	public void writeToServerAs(String blobName) throws JSONException, ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		String request = "Blobs";
		OrderedJSONObject payload = new OrderedJSONObject();
		//payload.put("@odata.context", ../$metadata#Contents('Blobs')/Contents/$entity);
		payload.put("@odata.type", "#ibm.tm1.api.v1.Document"); 
		payload.put("Name", blobName);
		payload.put("ID", blobName);
		//System.out.println("Write as " + payload.toString());
		tm1server.post(request, payload);
		request = "Blobs('" + blobName + "')/Content";
		String content = new String(bytes);
		//System.out.println("Write " + payload);
		tm1server.patch(request, content);
	}

	static public boolean isUTF8(final byte[] bytes) {
		int expectedLength = 0;
		for (int i = 0; i < bytes.length; i++) {
			if ((bytes[i] & 0b10000000) == 0b00000000) {
				expectedLength = 1;
			} else if ((bytes[i] & 0b11100000) == 0b11000000) {
				expectedLength = 2;
			} else if ((bytes[i] & 0b11110000) == 0b11100000) {
				expectedLength = 3;
			} else if ((bytes[i] & 0b11111000) == 0b11110000) {
				expectedLength = 4;
			} else if ((bytes[i] & 0b11111100) == 0b11111000) {
				expectedLength = 5;
			} else if ((bytes[i] & 0b11111110) == 0b11111100) {
				expectedLength = 6;
			} else {
				return false;
			}
			while (--expectedLength > 0) {
				if (++i >= bytes.length) {
					return false;
				}
				if ((bytes[i] & 0b11000000) != 0b10000000) {
					return false;
				}
			}
		}
		return true;
	}
}
