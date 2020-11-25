import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.FieldDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;

@MultipartConfig
@WebServlet(
        name = "CloudVision"
)
public class CloudVision extends HttpServlet {
    public CloudVision() {
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println(request.getParameter("hiddenField"));
        String url = request.getParameter("hiddenField");
        String user = request.getParameter("username");
        System.out.println(user);
        String filePath = "temp";
        URL httpUrl = new URL(url);
        File imageFile = new File(filePath);
        new FileOutputStream(imageFile);
        FileUtils.copyURLToFile(httpUrl, imageFile);
        List<AnnotateImageResponse> labelResponses = this.generateLabel(filePath);
        StringBuffer sb = new StringBuffer("");
        PhotoDetails photo = new PhotoDetails();
        photo.setUrl(url);
        photo.setName(user);
        List<PicInfo> picInfos = new ArrayList();
        Iterator var13 = labelResponses.iterator();

        while(var13.hasNext()) {
            AnnotateImageResponse res = (AnnotateImageResponse)var13.next();
            if (res.hasError()) {
                response.getWriter().println("Error: %s%n" + res.getError().getMessage());
            }

            Iterator var15 = res.getLabelAnnotationsList().iterator();

            while(var15.hasNext()) {
                EntityAnnotation annotation = (EntityAnnotation)var15.next();
                Map<FieldDescriptor, Object> fields = annotation.getAllFields();
                PicInfo pic = new PicInfo();
                Iterator var19 = fields.keySet().iterator();

                while(var19.hasNext()) {
                    FieldDescriptor fd = (FieldDescriptor)var19.next();
                    if (!fd.getName().contains("mid") && !fd.getName().contains("topicality")) {
                        sb.append(fd.getJsonName() + ":" + fields.get(fd).toString());
                        if (fd.getJsonName().equals("description")) {
                            pic.setDescription(fields.get(fd).toString());
                        }

                        if (fd.getJsonName().equals("score")) {
                            pic.setScore(fields.get(fd).toString());
                        }
                    }
                }

                picInfos.add(pic);
                sb.append("<br>");
            }

            photo.setInformation(picInfos);
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            byte[] base64 = Files.readAllBytes(imageFile.toPath());
            String imgEncode = Base64.getEncoder().encodeToString(base64);
            out.println("<h1 style='text-align:center;'> HashTag generator for " + user + "'s photo </h1>");
            out.println("<p style='text-align:center;'><img src='data:image/jpg;base64, " + imgEncode + "' alt='pic img' style='width:304px;height:228px;'></p>");
            out.println("<p> " + photo.toString() + "</p>");
        }

        imageFile.deleteOnExit();
    }

    private List<AnnotateImageResponse> generateLabel(String filePath) throws IOException {
        List<AnnotateImageRequest> requests = new ArrayList();
        System.out.println(System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));
        ByteString imgBytes = ByteString.readFrom(new FileInputStream(filePath));
        Image img = Image.newBuilder().setContent(imgBytes).build();
        Feature feat = Feature.newBuilder().setType(Type.LABEL_DETECTION).build();
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
        requests.add(request);

        try {
            ImageAnnotatorClient client = ImageAnnotatorClient.create();
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();
            return responses;
        } catch (Exception var10) {
            System.out.println(var10);
            return null;
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }
}
