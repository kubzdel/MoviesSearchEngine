import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public class SimilarDocument  implements Comparable<SimilarDocument> {

    public int id;
    public String title="";
    public String director="";
    public String cast="";

    public SimilarDocument(int id,String title,float titleScore, float castScore, float directorScore, float plotScore) {
        this.id = id;
        this.title = title;
        this.titleScore = titleScore;
        this.castScore = castScore;
        this.directorScore = directorScore;
        this.plotScore = plotScore;
        this.totalScore = titleScore+castScore+directorScore+plotScore;
    }

    public String plot="";
    public float titleScore = 0;
    public float castScore = 0;
    public float directorScore = 0;
    public float plotScore = 0;
    public float totalScore = 0;




    @Override
    public int compareTo(@NotNull SimilarDocument o) {
        return Float.valueOf(o.totalScore).compareTo(Float.valueOf(this.totalScore));
    }
}
