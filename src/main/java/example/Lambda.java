/**
 * Created by chimbs on 12/9/16.
 */
package example;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.rekognition.AmazonRekognitionClient;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Properties;


public class Lambda {

    static final String TO = "gaurav.raje07@gmail.com"; // Replace with a "To" address. If your account is still in the
    static final String BODY = "Chimbs thinks this image contains %s";
    static final String SUBJECT = "chimbstest";


    public String myHandler(int myCount, Context context) throws Exception {

        LambdaLogger logger = context.getLogger();

        logger.log("received : " + myCount);
        lambda(logger);
        AmazonSimpleEmailService ses = new AmazonSimpleEmailServiceClient();
        AmazonS3Client s3Client = new AmazonS3Client();
        GetObjectRequest request = new GetObjectRequest("chimbsmail", "email/6ruv66uj5t0dk58t3kfvabr0ghig2rrtdfrmamo1");
        s3Client.getObject("chimbsmail", "email/6ruv66uj5t0dk58t3kfvabr0ghig2rrtdfrmamo1");
//        s3Client.getObject()
        return String.valueOf(myCount);
    }

    public static void main(String[] args) throws Exception {


        lambda(null);
    }

    private static void lambda(LambdaLogger logger) throws Exception {
        AWSCredentials credentials;
        try {
            credentials = new EnvironmentVariableCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new Exception("Cannot load the credentials from the credential profiles file. "
                    + "Please make sure that your credentials file is at the correct "
                    + "location (/Users/<userid>/.aws/credentials), and is in a valid format.", e);
        }

        AmazonS3Client s3Client = new AmazonS3Client();


        ObjectListing ol = s3Client.listObjects("chimbsmail");
        for (S3ObjectSummary os : ol.getObjectSummaries()) {
            String image = os.getKey();
            S3Object obj = s3Client.getObject("chimbsmail", image);
            obj.getObjectMetadata().getLastModified();

            logger.log(detect(credentials, obj));
        }

        s3Client.getObject("chimbsmail", "email/6ruv66uj5t0dk58t3kfvabr0ghig2rrtdfrmamo1");
    }

    private static String detect(AWSCredentials credentials, S3Object obj) throws MessagingException, IOException {
        Session s = Session.getDefaultInstance(new Properties());
String ret = "Found : ";
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
            ret = objectMapper.writeValueAsString(result);
            System.out.println("Result = " + objectMapper.writeValueAsString(result));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

}
