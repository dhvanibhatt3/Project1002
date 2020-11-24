public class PicInfo {

    String description;
    String score;

    public void setDescription(String description) {
        this.description = description;
    }

    public String getScore() {
        return score;
    }

    public String getDescription() {

        return description;
    }

    public void setScore(String score) {

        this.score = score;
    }

    @Override
    public String toString() {
        return "PicInfo{" +
                "description='" + description + '\'' +
                ", score='" + score + '\'' +
                '}';
    }
}
