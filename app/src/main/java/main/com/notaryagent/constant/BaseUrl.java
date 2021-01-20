package main.com.notaryagent.constant;

/**
 * Created by technorizen on 5/6/17.
 */

public class BaseUrl {
    public static String LiveServer="https://www.notarymobility.com/";
    public static String DevServer="https://dev.notarymobility.com/";
    public static String TestServer="https://test.notarymobility.com/";
    public static String baseurl=DevServer+"notary/webservice/";
    public static String image_baseurl=DevServer+"notary/uploads/images/";
    public static String stripe_publish="pk_test_3oQpHM18Yv2mFAK6vSE5I1oz";
    public static String websiturl="http://www.notarymobility.com";
    public static String terms=LiveServer+"notary/terms.html";
    public static String privacy=LiveServer+"notary/Privacy.html";
    public static String STRIPE_OAUTH_URL="https://connect.stripe.com/express/oauth/authorize?redirect_uri=http://technorizen.com/notary/webservice/stripe_payment_form&client_id=ca_ENslgmlYpugASg6AVMMYzYMZwlrwDS6E&scope=read_write&always_prompt=false&stripe_landing=register&state=";
}
