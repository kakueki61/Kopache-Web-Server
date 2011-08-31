package org.kakueki61.socket_connection.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.kakueki61.socket_connection.Dispatcher;
import org.kakueki61.socket_connection.PrintFileContentThread;
import org.kakueki61.socket_connection.util.Constants;

public class KodDispatcher implements Dispatcher {

	@Override
	public String getHtmlPage(String requestPath,
			Map<String, List<String>> requestParams) throws IOException, ClassNotFoundException {
		if(requestPath.endsWith("vote.kod")){
			return getVotePage(requestPath, requestParams);
			
		}else if(requestPath.endsWith("vote_complete.kod")){
			return getVoteCompletePage(requestPath, requestParams);
			
		}else if(requestPath.endsWith("result.kod")){
			return getResultPage(requestPath, requestParams);
		}
		return null;
	}
	
	public String getVotePage(String requestPath,
			Map<String, List<String>> requestParams) {
		String voteTemplate = readFile(requestPath);
		String temp = makeCandidatesRadioTags(CandidateManager.getCandidates());
		voteTemplate = voteTemplate.replace("[REPLACE_ME]", temp);
		return voteTemplate;
	}
	
	public String getVoteCompletePage(String requestPath,
			Map<String, List<String>> requestParams) throws IOException, ClassNotFoundException {
		//マップに入れてファイルに書き出す
		String voter = null;
		String candidate = null;
		List<String> valueList;
		
		Iterator<String> iterator = requestParams.keySet().iterator();
		while(iterator.hasNext()){
			String key = iterator.next();
			if("voter".equals(key)){
				valueList = requestParams.get("voter");
				if(valueList.size() == 1){
					voter = URLDecoder.decode(valueList.get(0), "UTF-8");
				}else{
					//TODO
				}
			}else if("candidate".equals(key)){
				valueList = requestParams.get("candidate");
				if(valueList.size() == 1){
					String tmpCandidate = URLDecoder.decode(valueList.get(0), "UTF-8");
					if(CandidateManager.isValidCandidates(tmpCandidate)){
						candidate = tmpCandidate;
					}
				}else{
					//TODO
				}
			}
		}
		System.out.println("voter : " + voter + "---candidate : " + candidate);
		DataStoreManager.put(voter, candidate);
		
		return readFile(requestPath);
	}
	
	public String getResultPage(String requestPath,
			Map<String, List<String>> requestParams) throws IOException, ClassNotFoundException {
		//ファイルからマップを取り出して集計
		//マップには、<誰が, 誰に>投票したのかが詰まっている
		Map<String, String> result = DataStoreManager.getResult();
		Map<String, Integer> counting = new HashMap<String, Integer>();
		
		//とりあえず出す
		String resultStr = "";
		String resultVoterStr = "";
		String voter;
		String candidate;
//		Iterator<String> iterator = result.keySet().iterator();
//		while(iterator.hasNext()){
//			key = iterator.next();
//			resultStr += URLDecoder.decode(key, "UTF-8") + " : " + result.get(key) + "<br />";
//		}
		
		Iterator<String> iterator = result.keySet().iterator();
		while(iterator.hasNext()){
			voter = iterator.next();
			candidate = result.get(voter);
			
			if(counting.containsKey(candidate)){
				int count = counting.get(candidate);
				counting.put(candidate, count + 1);
			}else{
				counting.put(candidate, 1);
			}
			resultVoterStr += voter + "<br />";
		}
		
		//ソートする
		List<Map.Entry<String, Integer>> sortedList = new ArrayList<Map.Entry<String, Integer>>();
		sortedList = sortMap(counting);
		
		int i=1;
		int j=0;
		int lastValue = 0;
		int value = 0;
		Iterator sortedIterator = sortedList.iterator();
		while(sortedIterator.hasNext()){
			j++;
			Map.Entry entry = (Map.Entry)sortedIterator.next();
			value = (Integer) entry.getValue();
			if(j > 5){
				if(value < lastValue){
					break;
				}
			}
			
			if(value < lastValue){
				i++;
			}
			resultStr += "<h" + i + ">" + entry.getKey() + " : " + value + "票<br /></h" + i + ">";
			lastValue = (Integer) entry.getValue();
		}
		String body = "<html><head>" +
				"<title>8月度ALM　投票ページ</title>" +
				"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />" +
				"<META HTTP-EQUIV=\"Refresh\" CONTENT=\"5\"></head>" +
				"<body>" +
				"<font color=\"#FF0000\"><h1>TOP 5</h1></font>" +  
				resultStr + 
				"<br /><br /><br />--------投票者一覧--------<br />" + resultVoterStr +
				"</body>" +
				"</html>";
		
		return body;
	}
	
	
	public static String readFile(String requestPath){
		String filepath = PrintFileContentThread.DocumentRoot + requestPath;
		try{
			FileInputStream fis = new FileInputStream(new File(filepath));
			String text = new Scanner(fis, "UTF-8").useDelimiter("\\A").next();
			return text;
		}catch(FileNotFoundException e){
			System.err.println("Not Found !!");
		}
		return null;
	}
	
	public static String makeCandidatesRadioTags(Map<String, String> candidates){
		String radioTags = "";
		Iterator<String> iterator = candidates.keySet().iterator();
		while(iterator.hasNext()){
			String candidate = iterator.next();
			radioTags += String.format(Constants.RadioTag, candidate, candidate) + "<br />";
		}
		return radioTags;
	}	
	
	public static List<Map.Entry<String, Integer>> sortMap(Map<String, Integer> map){
		List<Map.Entry<String, Integer>> sortedList = new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
		
		Collections.sort(sortedList, new Comparator<Object>() {
			public int compare(Object o1, Object o2){
				Map.Entry<String, Integer> e1 = (Map.Entry<String, Integer>)o1;
				Map.Entry<String, Integer> e2 = (Map.Entry<String, Integer>)o2;
				Integer e1Value = e1.getValue();
				Integer e2Value = e2.getValue();
				return (e2Value.compareTo(e1Value));
			}
		});
		
		return sortedList;
	}
}
