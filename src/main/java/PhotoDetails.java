import java.util.List;

public class PhotoDetails {

    String url;
    String name;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PicInfo> getInformation() {
        return information;
    }

    public void setInformation(List<PicInfo> information) {
        this.information = information;
    }

    List<PicInfo> information ;

    @Override
    public String toString() {
        return "PhotoDetails{" +
                "url='" + url + '\'' +
                ", name='" + name + '\'' +
                ", information=" + information +
                '}';
    }
}
