package com.lc.nlp4han.constituent.unlex;

import java.util.LinkedList;

/**
 * 通过不同父亲（原始符号相同）相同的孩子（原始符号和字符号的后缀也相同）的派生规则来平滑规则概率
 * 
 * @author 王宁
 */
public class SmoothByRuleOfSameChild extends Smoother
{
	public static int smoothType = 1;

	public SmoothByRuleOfSameChild(double same)
	{
		super(same);

	}

	@Override
	public BinaryRule smoothBRule(BinaryRule bRule, short nSubSymP, short nSubSymLC, short nSubSymRC)
	{
		if (nSubSymP == 1)
			return bRule;
		LinkedList<LinkedList<LinkedList<Double>>> scores = bRule.getScores();
		double[][] scoreAverage = new double[nSubSymLC][];
		for (short subLC = 0; subLC < nSubSymLC; subLC++)
		{
			for (short subRC = 0; subRC < nSubSymRC; subRC++)
			{
				for (short subP = 0; subP < nSubSymP; subP++)
				{
					if (scoreAverage[subLC] == null)
					{
						scoreAverage[subLC] = new double[nSubSymRC];
					}
					scoreAverage[subLC][subRC] += scores.get(subP).get(subLC).get(subRC) / nSubSymP;
				}
			}
		}
		for (short subP = 0; subP < nSubSymP; subP++)
		{
			for (short subLC = 0; subLC < nSubSymLC; subLC++)
			{
				for (short subRC = 0; subRC < nSubSymRC; subRC++)
				{
					scores.get(subP).get(subLC).set(subRC,
							scores.get(subP).get(subLC).get(subRC) * same + scoreAverage[subLC][subRC] * diff);
				}
			}
		}
		return bRule;
	}

	@Override
	public UnaryRule smoothURule(UnaryRule uRule, short nSubSymP, short nSubSymC)
	{
		if (nSubSymP == 1)
			return uRule;
		LinkedList<LinkedList<Double>> scores = uRule.getScores();
		double[] scoreAverage = new double[nSubSymC];
		for (short subC = 0; subC < nSubSymC; subC++)
		{
			for (short subP = 0; subP < nSubSymP; subP++)
			{
				scoreAverage[subC] += scores.get(subP).get(subC) / nSubSymP;
			}
		}
		for (short subP = 0; subP < nSubSymP; subP++)
		{
			for (short subC = 0; subC < nSubSymC; subC++)
			{
				scores.get(subP).set(subC,
						scores.get(subP).get(subC) * (1 - diff * 10) + scoreAverage[subC] * diff * 10);
			}
		}
		return uRule;
	}

	@Override
	public PreterminalRule smoothPreRule(PreterminalRule preRule, short nSubSymP)
	{
		if (nSubSymP == 1)
			return preRule;
		LinkedList<Double> scores = preRule.getScores();
		double scoreAverage = 0.0;
		for (short subP = 0; subP < nSubSymP; subP++)
		{
			scoreAverage += scores.get(subP) / nSubSymP;
		}
		for (short subP = 0; subP < nSubSymP; subP++)
		{
			scores.set(subP, scores.get(subP) * same + scoreAverage * diff);
		}
		return preRule;
	}
}