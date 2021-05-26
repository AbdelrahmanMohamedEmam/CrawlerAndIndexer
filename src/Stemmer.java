

import ca.rmen.porterstemmer.PorterStemmer;

class Stemmer{
    public static String stem(String word){
        PorterStemmer porterStemmer = new PorterStemmer();
        String stem = porterStemmer.stemWord(word);

        return stem;
    }
}
