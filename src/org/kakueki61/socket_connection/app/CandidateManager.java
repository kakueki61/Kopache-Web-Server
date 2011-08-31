package org.kakueki61.socket_connection.app;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class CandidateManager implements Serializable{
	private static Map<String, String> candidates = new TreeMap<String, String>();
	static{
		candidates.put("Xさん", "");
		candidates.put("Qさん", "Cheerzのこと");
		candidates.put("@さん", "JavaScript関連");
		candidates.put("Pさん", "新製品の発表");
		candidates.put("Cさん", "");
		candidates.put("Z", "");
		candidates.put("$さん", "");
		candidates.put("Vさん", "");
		candidates.put("Gさん", "独自Webサーバーと投票システムの作成に関して");
	}
	
	public static Map<String, String> getCandidates(){
		return candidates;
	} 
	
	public static boolean isValidCandidates(String name){
		if(candidates.containsKey(name)){
			return true;
		}
		return false;
	}
}
