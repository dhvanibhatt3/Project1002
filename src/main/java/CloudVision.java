import com.google.appengine.api.datastore.*;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import org.apache.commons.io.IOUtils;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@MultipartConfig
@WebServlet(
        name = "CloudVision",
        urlPatterns = {"/CloudVision"}
)
public class CloudVision extends HttpServlet {
    public CloudVision() {
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String url = request.getParameter("hiddenField");
        String user = request.getParameter("username");
        String FbPhotoId = request.getParameter("Fb_image_id");
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        if (checkIfImageExists(datastore, FbPhotoId) == false) {
            List<EntityAnnotation> imageLabels = getImageLabels(url);
            if (imageLabels != null) {
                List<String> lables = imageLabels.stream().filter(label -> label.getScore() * 100 > 75)
                        .map(EntityAnnotation::getDescription).collect(Collectors.toList());
                if (null != lables && !lables.isEmpty()) {
                    PhotoDetails photo = new PhotoDetails();
                    photo.setUrl(url);
                    photo.setName(user);
                    addImageDetailsToDataStore(photo, lables, FbPhotoId, datastore);
                    getImageFromStore(request, response, datastore, FbPhotoId);
                }
            }
        }else{
            getImageFromStore(request, response, datastore, FbPhotoId);
        }
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws
            ServletException, IOException {
    }
    public static void addImageDetailsToDataStore(PhotoDetails photo, List<String> labels, String FbPhotoId, DatastoreService
            datastore) {
        Entity User_Photos = new Entity("User_Photos");
        User_Photos.setProperty("fb_image_id", FbPhotoId);
        User_Photos.setProperty("image_url", photo.getUrl());
        User_Photos.setProperty("labels", labels);
        datastore.put(User_Photos);
    }
    public void getImageFromStore(HttpServletRequest request, HttpServletResponse response, DatastoreService datastore, String FbPhotoId) {

        Query query =
                new Query("User_Photos")
                        .setFilter(new Query.FilterPredicate("fb_image_id", Query.FilterOperator.EQUAL, FbPhotoId));
        PreparedQuery pq = datastore.prepare(query);
        List<Entity> results = pq.asList(FetchOptions.Builder.withDefaults());
        if(null != results) {
            results.forEach(user_Photos -> {
                List<String> labelsFromStore = (List<String>) user_Photos.getProperty("labels");
                System.out.println("labelsFromStore"+labelsFromStore);
                String image_url=user_Photos.getProperty("image_url").toString();
                request.setAttribute("imageUrl",image_url );
                request.setAttribute("imageLabels", labelsFromStore);
                RequestDispatcher dispatcher = getServletContext()
                        .getRequestDispatcher("/labels.jsp");
                try {
                    dispatcher.forward(request, response);
                } catch (ServletException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

    }

    public static boolean checkIfImageExists(DatastoreService datastore, String fbPhotoId) {
        Query q =
                new Query("User_Photos")
                        .setFilter(new Query.FilterPredicate("fb_image_id", Query.FilterOperator.EQUAL, fbPhotoId));
        PreparedQuery pq = datastore.prepare(q);
        Entity result = pq.asSingleEntity();
        if (result == null) {
            return false;
        }
        return true;
    }

    public static byte[] downloadFile(URL url) throws Exception {
        try (InputStream in = url.openStream()) {
            byte[] bytes = IOUtils.toByteArray(in);
            return bytes;
        }
    }
    private List<EntityAnnotation> getImageLabels(String imageUrl) {
        try {
            byte[] imgBytes = downloadFile(new URL(imageUrl));
            ByteString byteString = ByteString.copyFrom(imgBytes);
            Image image = Image.newBuilder().setContent(byteString).build();
            Feature feature = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
            AnnotateImageRequest request =
                    AnnotateImageRequest.newBuilder().addFeatures(feature).setImage(image).build();
            List<AnnotateImageRequest> requests = new ArrayList<>();
            requests.add(request);
            ImageAnnotatorClient client = ImageAnnotatorClient.create();
            BatchAnnotateImagesResponse batchResponse = client.batchAnnotateImages(requests);
            client.close();
            List<AnnotateImageResponse> imageResponses = batchResponse.getResponsesList();
            AnnotateImageResponse imageResponse = imageResponses.get(0);
            if (imageResponse.hasError()) {
                System.err.println("Error getting image labels: " + imageResponse.getError().getMessage());
                return null;
            }
            return imageResponse.getLabelAnnotationsList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

