package social;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.util.IOUtils;
import facebook4j.Facebook;
import facebook4j.FacebookFactory;
import facebook4j.PictureSize;
import facebook4j.Reading;
import facebook4j.User;
import facebook4j.auth.AccessToken;
import rekog.FaceRekog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * Created by chimbs on 12/9/16.
 */
public class FacebookScrap {
    public static void main(String[] args) throws Exception {
        Facebook facebook = new FacebookFactory().getInstance();
        String appId = "484946345043929";
        String appSecret = "d11a5114479d28d097ddcbe97c45e2b2";
        facebook.setOAuthAppId(appId, appSecret);
        String commaSeparetedPermissions;
        String accessTokenString = "EAACEdEose0cBAH3V4k0XY2IXZBr21b0NRDwlPIEkXGu3qDter09n9SKgKNll4iVbfArpL5aAd55RHEtgrHwpQqyUtWCCeygG5UCId2kujbyUCqVfQ0NhNuy4ppHazPJuLpiUrMJChrqzd3tyNdkcE4H0VXhrI3DobIZAZANHQZDZD";
        AccessToken accessToken = new AccessToken(accessTokenString);
        facebook.setOAuthAccessToken(accessToken);

        facebook.getAccounts();
        facebook.getFriendlists();



        AWSCredentials credentials = new ProfileCredentialsProvider("chimbs").getCredentials();

        for(int i = 0;i<15;i++){

            String id = facebook.getFriends().get(i).getId();
            String name = facebook.getFriends().get(i).getName();
            URL picUrl = facebook.getPictureURL(id, PictureSize.large);
            File file  = saveImage(picUrl,name.replace(" ",""));
            FaceRekog.detectLabels(credentials, IOUtils.toByteArray(new FileInputStream(file)),name);

        }
        int a = 2;

    }
    public static File saveImage(URL url, String destinationFile) throws IOException {

        InputStream is = url.openStream();
        File file = File.createTempFile(destinationFile,"jpg");
        OutputStream os = new FileOutputStream(file);

        byte[] b = new byte[2048];
        int length;

        while ((length = is.read(b)) != -1) {
            os.write(b, 0, length);
        }

        is.close();
        os.close();
        return file;
    }

}
