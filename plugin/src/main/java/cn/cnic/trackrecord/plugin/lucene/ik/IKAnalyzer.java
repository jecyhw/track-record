package cn.cnic.trackrecord.plugin.lucene.ik;

import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.IOUtils;

public class IKAnalyzer extends Analyzer {

    @Override
    protected TokenStreamComponents createComponents(String arg0) {
        Reader reader = null;
        try {
            reader = new StringReader(arg0);
            IKTokenizer it = new IKTokenizer(reader);
            return new Analyzer.TokenStreamComponents(it);
        } finally {
            IOUtils.closeWhileHandlingException(reader);
        }
    }

}