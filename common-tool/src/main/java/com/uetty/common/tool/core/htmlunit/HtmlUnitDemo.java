package com.uetty.common.tool.core.htmlunit;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.net.MalformedURLException;

public class HtmlUnitDemo {

	
	
	public static WebClient createWebClient() {
		return new WebClient(BrowserVersion.CHROME);
	}
	
	public static Page connectWebClient(WebClient webClient, String url) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		webClient.getOptions().setJavaScriptEnabled(true);
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setUseInsecureSSL(false);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setTimeout(30000);
		return webClient.getPage(url);
	}
	
	public static HtmlDivision readDiv(HtmlPage page, String username, String password) {
		assert page != null;
//		page.querySelector("#id");
		return page.querySelector(".class");
	}
	
	public static String getText(HtmlPage page) {
		return page.getTextContent();
	}
	
	public static void main(String[] args) throws FailingHttpStatusCodeException, MalformedURLException, IOException, InterruptedException {
		WebClient client = createWebClient();
		HtmlPage htmlPage = (HtmlPage) connectWebClient(client, "http://127.0.0.1:12800/bootstrap3.html");
		Thread.sleep(5000);
		
		DomNode querySelector = htmlPage.querySelector("body>div.container");
		System.out.println(querySelector.asXml());
		System.out.println("---------------------------------------------------------------------------");
		System.out.println(querySelector.asText());
		System.out.println("---------------------------------------------------------------------------");
		System.out.println(querySelector.getTextContent());
		
		
	}
}
