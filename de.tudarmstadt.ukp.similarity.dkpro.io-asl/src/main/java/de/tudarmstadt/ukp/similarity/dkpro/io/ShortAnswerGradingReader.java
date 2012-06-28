package de.tudarmstadt.ukp.similarity.dkpro.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.similarity.dkpro.api.type.GoldTextSimilarityScore;
import de.tudarmstadt.ukp.similarity.dkpro.io.util.CombinationPair;

public class ShortAnswerGradingReader
	extends CombinationReader
{
	public static final String PARAM_INPUT_DIR = "InputDir";
	@ConfigurationParameter(name=PARAM_INPUT_DIR, mandatory=true)
	private File inputDir;
	
	public static final String PARAM_ENCODING = "Encoding";
	@ConfigurationParameter(name=PARAM_ENCODING, mandatory=false, defaultValue="UTF-8")
	private String encoding;

    private Map<String, Double> goldScoreMap = new HashMap<String, Double>();

	@Override
	public List<CombinationPair> getAlignedPairs()
		throws ResourceInitializationException
	{
		List<CombinationPair> pairs = new ArrayList<CombinationPair>();
		
		Collection<File> files = FileUtils.listFiles(inputDir, new String[]{ "txt" }, true);
		Iterator<File> iterator = files.iterator();
		
		try
		{
			while(iterator.hasNext())
			{
				// Process assignment
				File file = iterator.next();
				
				int assignment = Integer.parseInt(file.getName().substring(0, file.getName().length() - 4));
				
				List<String> lines = FileUtils.readLines(file, encoding);
				
				int questionIndex = 0;
				String refAnswer = "";
				
				for (int i = 0; i < lines.size(); i++)
				{
					String line = lines.get(i);
					
					if (line.startsWith("#"))
					{
						// A new question starts here
						i++;
						line = lines.get(i);
						line.substring(12);			// Skip "\t\tQuestion: " prefix
						questionIndex++;

						i++;									// Advance to perfect answer line
						refAnswer = lines.get(i).substring(10);	// Skip "\t\tAnswer: " prefix
						i++;									// Skip empty line
					}
					else if (!line.equals(""))
					{
						// Combine refAnswer with the answer on this line
						
						String[] lineSplit = line.split("\t");
						
						String answer = lineSplit[2];
						int studentID = Integer.parseInt(lineSplit[1].substring(1, lineSplit[1].length() - 1));		// Ignore surrounding braces
						
						String id1 = assignment + ":" + questionIndex;
						String id2 = assignment + ":" + questionIndex + ":" + studentID;
						
						// Add to combination pairs
						CombinationPair pair = new CombinationPair(inputDir.toString());
						pair.setID1(id1);
						pair.setID2(id2);
						pair.setText1(refAnswer);
						pair.setText2(answer);
						
						
						System.out.println(pair.getID2() + " # " + refAnswer + " # " + answer);
						
						pairs.add(pair);
						
						goldScoreMap.put(id2, Double.parseDouble(lineSplit[0]));
					}
				}			
			}
		}
		catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
		
		
		
		return pairs;
	}

    @Override
    public void getNext(CAS cas)
        throws IOException, CollectionException
    {
        super.getNext(cas);

        try {
            JCas jcas = cas.getJCas();
            JCas view2 = jcas.getView(VIEW_2);
            DocumentMetaData md2 = DocumentMetaData.get(view2);
            
            GoldTextSimilarityScore goldScore = new GoldTextSimilarityScore(jcas);
            goldScore.setScore(goldScoreMap.get(md2.getDocumentId()));
            goldScore.addToIndexes();
        }
        catch (CASException e) {
            throw new CollectionException(e);
        }
    }
}
