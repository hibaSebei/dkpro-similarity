package dkpro.similarity.algorithms.style.uima;

import java.util.Map;

import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import dkpro.similarity.algorithms.style.AvgCharactersPerTokenMeasure;
import dkpro.similarity.uima.resource.JCasTextSimilarityResourceBase;


public class AvgCharactersPerTokenResource
	extends JCasTextSimilarityResourceBase
{
	@SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }

        this.mode = TextSimilarityResourceMode.jcas;
        
        measure = new AvgCharactersPerTokenMeasure();
        
        return true;
    }
}
