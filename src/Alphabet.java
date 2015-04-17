

import java.util.HashMap;

public class Alphabet {

	HashMap<String,Integer> Alphabet;
	HashMap<Integer,String> InvAlphabet;
	
	
	public Alphabet()
	{
		Alphabet = new HashMap<String,Integer>();
		InvAlphabet = new HashMap<Integer,String>();
	}
	
	public Integer getOrCreateFeature(String feature)
	{
		if (!Alphabet.containsKey(feature))
		{
			Alphabet.put(feature,new Integer(Alphabet.size())+1);
			InvAlphabet.put(new Integer(Alphabet.size())+1, feature);
		}
		
		return Alphabet.get(feature);
	}
	
	
	public String getFeature(Integer index)
	{
		if (InvAlphabet.containsKey(index)) return InvAlphabet.get(index);
		
		return null;
	}
	
	public boolean containsFeature(String feature)
	{
		return Alphabet.containsKey(feature);
	}
	
	public Integer getIndex(String feature)
	{
		return Alphabet.get(feature);
	}

	
}
