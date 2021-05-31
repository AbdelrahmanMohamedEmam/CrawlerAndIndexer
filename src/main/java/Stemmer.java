import opennlp.tools.stemmer.PorterStemmer;

class Stemmer{
    public static String stem(String word){
        PorterStemmer porterStemmer = new PorterStemmer();
        String stem = porterStemmer.stem(word);

        return stem;
    }
}
