import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import org.apache.commons.io.FileUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;

@MultipartConfig
    @WebServlet(name = "cloudvision")
    public class cloudvision extends HttpServlet {
        protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            System.out.println(request.getParameter("hiddenField"));
            String url = request.getParameter("hiddenField");
            String filePath = "temp";
            URL httpUrl = new URL(url);
            File imageFile = new File(filePath);
            OutputStream os = new FileOutputStream(imageFile);
            FileUtils.copyURLToFile(httpUrl, imageFile);
            List<AnnotateImageResponse> labelResponses = generateLabel(filePath);
            StringBuffer sb = new StringBuffer("");
            for (AnnotateImageResponse res : labelResponses) {
                if (res.hasError()) {
                    response.getWriter().println("Error: %s%n" + res.getError().getMessage());
                }

                for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
                    Map<Descriptors.FieldDescriptor, Object> fields = annotation.getAllFields();
                    for(Descriptors.FieldDescriptor fd: fields.keySet()){
                        if(!fd.getName().contains("mid") && !fd.getName().contains("topicality")) {
                            sb.append(fd.getJsonName() + ":" + fields.get(fd).toString());
                        }
                    }
                    sb.append("<br>");
                }
                response.setContentType("text/html");
                PrintWriter out = response.getWriter();
                byte[] base64 = Files.readAllBytes(imageFile.toPath());
                String imgEncode = Base64.getEncoder().encodeToString(base64);
                out.println("<h1 style='text-align:center;'> HashTag generator </h1>");
                out.println("<p style='text-align:center;'><img src='data:image/jpg;base64, "+ imgEncode + "' alt='pic img' style='width:304px;height:228px;'></p>");
                out.println("<p> "+ sb.toString() + "</p>");
            }

            imageFile.deleteOnExit();
        }

        private List<AnnotateImageResponse> generateLabel(String filePath) throws IOException {
            List<AnnotateImageRequest> requests = new ArrayList<>();


            System.out.println(System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));//give your path name

            ByteString imgBytes = ByteString.readFrom(new FileInputStream(filePath));

            Image img = Image.newBuilder().setContent(imgBytes).build();
            Feature feat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
            AnnotateImageRequest request =
                    AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
            requests.add(request);

            try {
                ImageAnnotatorClient client = ImageAnnotatorClient.create();
                BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
                List<AnnotateImageResponse> responses = response.getResponsesList();

                return responses;

            } catch (Exception e){
                System.out.println(e);
            }
            return null;
        }

        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        }
    }



