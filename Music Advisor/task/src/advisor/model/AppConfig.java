package advisor.model;
import lombok.Getter;
import lombok.NonNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Getter
public class AppConfig {
    private static AppConfig appConfig;
    private final String CLIENT_ID;
    private final String CLIENT_SECRET;
    private final String REDIRECT_URI;
    private final String RESPONSE_TYPE;
    private String AUTHORIZATION_SERVER;
    private String API_SERVER;
    private int PAGE_SIZE;
    private final String AUTH_LINK;

    public static AppConfig getAppConfig() {
        if (appConfig == null) {
            appConfig = new AppConfig();
        }
        return appConfig;
    }

    private AppConfig() {
        Properties configParams = new Properties();
        try {
            configParams.load(new FileInputStream("app.properties"));
        } catch (IOException e) {
            throw new RuntimeException("Include app.properties in /advisor");
        }

        CLIENT_ID = getValue(configParams, "client_id");
        CLIENT_SECRET = getValue(configParams, "client_secret");
        REDIRECT_URI = getValue(configParams, "redirect_uri");
        RESPONSE_TYPE = getValue(configParams, "response_type");
        AUTHORIZATION_SERVER = getValue(configParams, "default_authorization_server");
        API_SERVER = getValue(configParams, "default_api_server");
        PAGE_SIZE = Integer.parseInt(getValue(configParams, "default_page_size"));
        AUTH_LINK = AUTHORIZATION_SERVER + "/authorize?"
                                         + "client_id=" + CLIENT_ID
                                         + "&redirect_uri=" + REDIRECT_URI
                                         + "&response_type=" + RESPONSE_TYPE;
    }

    @NonNull
    private String getValue(Properties params, String key) {
        String value =  params.getProperty(key);
        if (value == null) throw new RuntimeException("Include " + key + " in app.properties");
        return value;
    }

    public void setCustomAuthorizationServer(String serverPath) {
        AUTHORIZATION_SERVER = serverPath;
    }

    public void setCustomApiServer(String serverPath) {
        API_SERVER = serverPath;
    }

    public void setCustomPageSize(int pageSize) {
        PAGE_SIZE = pageSize;
    }
}
