package com.lc.nlp4han.constituent.unlex;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class RuleTest
{
	@Test
	public void ruleSplitTest()
	{

		Boolean addParentLabel = false;
		String[] sentences = { "(ROOT(IP(NP (PN i))(VP (VV like)(NP (DT the) (NN book)))))" };
		TreeBank treeBank = new TreeBank();
		for (int i = 0; i < sentences.length; i++)
		{
			treeBank.addTree(sentences[i], addParentLabel);
		}
		GrammarExtractor gExtractor = new GrammarExtractor(treeBank);
		Grammar grammar = gExtractor.getGrammar(1);
		TreeMap<String, Double> sameParentRuleScoreSum;

		try
		{
			sameParentRuleScoreSum = grammar.calculateSameParentRuleScoreSum();
			for (Map.Entry<String, Double> entry : sameParentRuleScoreSum.entrySet())
			{
				assertTrue("相同规则左侧的规则概率之和不为1。", entry.getValue() - 1 < Math.pow(10, -10));
				System.out.println(entry.getKey() + " " + entry.getValue());
			}
			GrammarWriter.writeToFile(grammar, "C:\\Users\\hp\\Desktop\\berforSplit", false);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		GrammarSpliter.splitGrammar(grammar, treeBank);
		try
		{
			sameParentRuleScoreSum = grammar.calculateSameParentRuleScoreSum();
			for (Map.Entry<String, Double> entry : sameParentRuleScoreSum.entrySet())
			{
				assertTrue("相同规则左侧的规则概率之和不为1。", entry.getValue() - 1 < Math.pow(10, -10));
				System.out.println(entry.getKey() + " " + entry.getValue());
			}
			GrammarWriter.writeToFile(grammar, "C:\\Users\\hp\\Desktop\\afterSplit", false);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		GrammarTrainer.EM(grammar, treeBank, 50);
		try
		{
			sameParentRuleScoreSum = grammar.calculateSameParentRuleScoreSum();
			for (Map.Entry<String, Double> entry : sameParentRuleScoreSum.entrySet())
			{
				assertTrue("相同规则左侧的规则概率之和不为1。", entry.getValue() - 1 < Math.pow(10, -10));
				System.out.println(entry.getKey() + " " + entry.getValue());
			}
			GrammarWriter.writeToFile(grammar, "C:\\Users\\hp\\Desktop\\afterEM", false);
			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}