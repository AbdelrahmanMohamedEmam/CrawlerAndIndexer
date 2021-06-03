import opennlp.tools.stemmer.PorterStemmer;

class Stemmer{
    public static String stem(String word){
        PorterStemmer porterStemmer = new PorterStemmer();
        String wordToStem = word.toLowerCase();
        String stem = porterStemmer.stem(wordToStem);

        return stem;
    }
}
