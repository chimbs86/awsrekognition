import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.rekognition.AmazonRekognitionClient;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.nio.ByteBuffer;
import java.util.Properties;


/**
 * Created by chimbs on 12/3/16.
 */
public class FaceRekog {
    public static void main(String[] args) throws Exception {

        AWSCredentials credentials;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
                    + "Please make sure that your credentials file is at the correct "
                    + "location (/Users/<userid>/.aws/credentials), and is in a valid format.", e);
        }

        AmazonS3Client s3Client = new AmazonS3Client();

        com.amazonaws.services.s3.model.S3Object obj = s3Client.getObject("chimbsmail", "email/6ruv66uj5t0dk58t3kfvabr0ghig2rrtdfrmamo1");
        ObjectListing ol = s3Client.listObjects("chimbsmail");
        for (S3ObjectSummary os : ol.getObjectSummaries()) {
            os.getKey();
        }
        Session s = Session.getDefaultInstance(new Properties());

        MimeMessage msg = new MimeMessage(s, obj.getObjectContent());
        MimeMultipart mimeMultipart = (MimeMultipart) msg.getContent();
        mimeMultipart.getBodyPart(1);

        byte[] bytes = IOUtils.toByteArray(mimeMultipart.getBodyPart(1).getInputStream());


        ByteBuffer bt;
        DetectLabelsRequest request = new DetectLabelsRequest()
                .withImage(new Image().withBytes(ByteBuffer.wrap(bytes)))
                .withMaxLabels(10)
                .withMinConfidence(77F);

        AmazonRekognitionClient rekognitionClient = new AmazonRekognitionClient(credentials);

        rekognitionClient.setSignerRegionOverride("us-east-1");
        try {
            DetectLabelsResult result = rekognitionClient.detectLabels(request);
            ObjectMapper objectMapper = new ObjectMapper();
            System.out.println("Result = " + objectMapper.writeValueAsString(result));
        } catch (AmazonRekognitionException e) {
            e.printStackTrace();
        }
    }
}
