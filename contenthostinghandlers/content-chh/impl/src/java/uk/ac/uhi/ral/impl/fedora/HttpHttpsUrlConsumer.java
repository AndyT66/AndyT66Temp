package uk.ac.uhi.ral.impl.fedora;

import java.io.InputStream;

public interface HttpHttpsUrlConsumer {

	InputStream  UrlPostAsStream(String url, String postParams);
	String  UrlPostAsString(String url, String postParams);

}
