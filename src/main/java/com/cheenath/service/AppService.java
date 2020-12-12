package com.cheenath.service;

import com.cheenath.AppManagementException;
import com.cheenath.data.AppDetails;
import com.cheenath.data.AppRepository;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.Arrays;

@Service
public class AppService {
    private final int PORT_NUMBER_BASE = 8085;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private AppRepository appRepository;

    //@Value("${jenkins.url.value}")
    private String jenkinsURL = "http://www.myvotes.in:8080/job/deploy_java_app/buildWithParameters?APP_GIT_REPO_URL=%s&GIT_BRANCH=%s&APP_ID=%d&APP_NAME=%s&DB_NAME=%s&DB_USER=%s&DB_PASSWORD=%s&PORT=%d";
    //private String jenkinsURL = "http://www.appgenservice.com:8080/job/deploy_java_app/buildWithParameters?APP_GIT_REPO_URL=%s&GIT_BRANCH=%s&APP_ID=%d&APP_NAME=%s&DB_NAME=%s&DB_USER=%s&DB_PASSWORD=%s&PORT=%d";

//    UriComponents uriComponents = UriComponentsBuilder.fromUriString(jenkinsURL).build();
    public void deploy(Integer appId) throws AppManagementException {

        AppDetails appDetails = appRepository.findById(appId).orElseThrow(() -> new AppManagementException(String.format("App id %d not found", appId)));
        String url = String.format(jenkinsURL.trim(), appDetails.getUrl(),appDetails.getBranch(), appDetails.getId(), appDetails.getAppName(), appDetails.getDbName(), appDetails.getDbUser(), appDetails.getDbPassword(), appDetails.getPort());
        System.out.println(jenkinsURL +"\n"+url);
        HttpHeaders headers = new HttpHeaders(){{
            String auth = "admin:1189efce18589fad059d8e51342c7d9775";
            byte[] encodedAuth = Base64.encodeBase64(
                    auth.getBytes(Charset.forName("US-ASCII")) );
            String authHeader = "Basic " + new String( encodedAuth );
            set( "Authorization", authHeader );
        }};
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<String>(headers);
        final String response = restTemplate.postForObject(url, entity, String.class);
    }

    public Iterable<AppDetails> findAll() {
        return appRepository.findAll();
    }

    public AppDetails add(AppDetails appRequest) {
        final int nextAppId = appRepository.findMaxAppId() == null ? 1 : appRepository.findMaxAppId().intValue() + 1;
        appRequest.setCreateDate(System.currentTimeMillis());
        appRequest.setPort(PORT_NUMBER_BASE + nextAppId);
        appRequest.setDbName("dbname_"+nextAppId);
        appRequest.setDbUser("dbuser_"+nextAppId);
        appRequest.setDbPassword("dbpassword_"+nextAppId);
        appRequest.setBranch("main");//TODO: later get branch name from user. For now fix it to main
        appRepository.save(appRequest);
        return appRequest;
    }
}
