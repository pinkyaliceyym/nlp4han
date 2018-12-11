package com.lc.nlp4han.clustering;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.lc.nlp4han.segment.WordSegFactory;
import com.lc.nlp4han.segment.WordSegmenter;
import com.lc.nlp4han.util.CharTypeUtil;

public class WordBasedFeatureGenerator implements FeatureGenerator
{
	private Map<String, Count> wordInfo = new HashMap<String, Count>();
	//private Map<String, Count> prunedWordInfo = null;
	private int totalDocumentNumber = 0;
	
	@Override
	public List<Feature> getFeatures(Text text)
	{
		List<Feature> result = new ArrayList<Feature>();
		Map<String, Integer> words = getWords(text);
		words = pruning(words);
		Set<Entry<String, Integer>> es = words.entrySet();
		
		for (Entry<String, Integer> e : es)
		{
			Feature f = getFeature(e.getKey(), e.getValue());
			result.add(f);
		}
		return result;
	}
	
	/**
	 * 对文本进行分词
	 * @param text 待分词的文本
	 * @return 所有词条及其个数
	 */
	private Map<String, Integer> getWords(Text text)
	{
		Map<String, Integer> fm = new HashMap<String, Integer>();
		WordSegmenter segmenter;
		String[] words;
		try
		{
			segmenter = WordSegFactory.getWordSegmenter();
			words = segmenter.segment(text.getContent());  //分词
			
			for (int i=0 ; i<words.length ; i++)  //统计各词的个数
			{
				if (fm.containsKey(words[i]))
				{
					int v = fm.get(words[i]);
					v++;
					fm.put(words[i], v);
				}
				else
				{
					fm.put(words[i], 1);
				}
			}
			
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return fm;
	}
	
	private Feature getFeature(String word, int tn)
	{
		Feature result = new Feature();
		result.setKey(word);
		if (wordInfo.containsKey(word))
		{
			Count c = wordInfo.get(word);
			double tf = tn*1.0 / c.tn;
			double idf = Math.log(totalDocumentNumber*1.0/(c.dn+1));
			result.setValue(tf*idf);
			
			//result.setValue(1);
		}
		else
			result.setValue(0);
		return result;
	}
	
	private <T> Map<String, T> pruning(Map<String, T> words)
	{
		Map<String, T> result = new HashMap<String, T>();
		Set<Entry<String, T>> es = words.entrySet();
		
		for (Entry<String, T> e : es)
		{
			if (isPruned(e.getKey()))
				continue;
			else
				result.put(e.getKey(), e.getValue());
		}
		
		return result;
	}
	
	private boolean isPruned(String word)
	{
		if (CharTypeUtil.isChinesePunctuation(word))
			return true;
		for (int i=0 ; i<word.length() ; i++)
		{
			if (isChinese(word.charAt(i)))
				return false;
			if (CharTypeUtil.isLetter(word.charAt(i)))
				return false;
		}
		return true;
	}
	
	private boolean isChinese(char c) {
	    Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
	    if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS  
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A  
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B  
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C//jdk1.7  
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D//jdk1.7  
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS  
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT) {  
            return true;  
        }
	    return false;
	}

	@Override
	public void init(List<Text> texts)
	{
		for (int i=0 ; i<texts.size() ; i++)
		{
			Text t = texts.get(i);
			Map<String, Integer> m = getWords(t);
			Set<Entry<String, Integer>> es = m.entrySet();
			for (Entry<String, Integer> e : es)
			{
				String key = e.getKey();
				if (wordInfo.containsKey(key))
				{
					Count c = wordInfo.get(key);
					c.tn += e.getValue();
					c.dn++;
				}
				else
				{
					Count c= new Count(e.getValue(), 1);
					wordInfo.put(key, c);
				}
			}
		}
		wordInfo = pruning(wordInfo);
		totalDocumentNumber = texts.size();
	}
	
	/**
	 * 获取词在所有文档中的数量
	 * @param word 被检测的词
	 * @return 词所有文档中出现的数量
	 */
	public int getTermNumber(String word)
	{
		if (wordInfo.containsKey(word))
		{
			return wordInfo.get(word).tn;
		}
		else
		{
			return 0;
		}
	}
	
	/**
	 * 获取词的文档数
	 * @param word 被检测的词
	 * @return 词的文档数
	 */
	public int getDocumentNumber(String word)
	{
		if (wordInfo.containsKey(word))
		{
			return wordInfo.get(word).dn;
		}
		else
		{
			return 0;
		}
	}
	
	/**
	 * 是否为登录词
	 * @param word 被检测词
	 * @return 若存在，返回true；否则，返回false。
	 */
	public boolean containsWord(String word)
	{
		return wordInfo.containsKey(word);
	}

	@Override
	public boolean isInitialized()
	{
		if (wordInfo == null || wordInfo.size()<1)
			return false;
		else
			return true;
	}
	
	/**
	 * 获取所有分词信息
	 * @return 分词信息
	 */
	@Override
	public Map<String, Count> getWordInfo()
	{
		return wordInfo;
	}

	/**
	 * 获取所有文档数
	 * @return 所有文档数
	 */
	public int getTotalDocumentNumber()
	{
		return totalDocumentNumber;
	}

	@Override
	public String toString()
	{
		return  wordInfo + "";
	}
}
