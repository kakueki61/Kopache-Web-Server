package org.kakueki61.socket_connection;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface Dispatcher {
	String getHtmlPage(String requestPath, Map<String, List<String>> requestParams) throws IOException, ClassNotFoundException;
}
