package com.uetty.common.tool.core;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;

public class HttpMultipartUtil {

	static Logger logger = Logger.getLogger(HttpMultipartUtil.class);
	
	public static String simpleMultipartPost(String url, Map<String, File> files, Map<String, Object> params) {
		final CloseableHttpClient client = HttpClients.createDefault();
		HttpPost post = new HttpPost(url);
		final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		
		String boundary = "-----commonToolMultipartBoundary";
		builder.setBoundary(boundary);
		
		String charset = "UTF-8";
		builder.setCharset(Charset.forName(charset));
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		
		ContentType textType = ContentType.create("text/plain", "UTF-8");
		params.forEach((key, value) -> {
			builder.addTextBody(key, String.valueOf(value), textType);
		});
		
		ContentType fileType = ContentType.create("multipart/form-data", "UTF-8");
		files.forEach((key, file) -> {
			if (file == null || !file.exists()) {
				return;
			}
			final FileBody fileBody = new FileBody(file, fileType);
			builder.addPart(key, fileBody);
		});
		
		final HttpEntity entity = builder.build();
		
		String headerContentType = "multipart/form-data; boundary=" + boundary;
		post.setHeader("Content-Type", headerContentType);
		post.setEntity(entity);

		HttpResponse response = null;
		try {
			response = client.execute(post);
			
			int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode != 200){
				throw new RuntimeException("error with statusCode " + statusCode + "[" + response.getStatusLine().getReasonPhrase() + "]");
			}
			
			InputStream content = response.getEntity().getContent();
			BufferedReader br = new BufferedReader(new InputStreamReader(content, "utf-8"));
			StringBuffer sb = new StringBuffer();
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			if (sb.length() > 0) {
				return sb.substring(0, sb.length());
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
	}
}
