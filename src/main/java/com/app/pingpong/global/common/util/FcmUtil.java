package com.app.pingpong.global.common.util;

import com.app.pingpong.domain.notification.dto.request.FcmMessage;
import com.app.pingpong.domain.notification.dto.request.PushMessage;
import com.app.pingpong.global.common.exception.StatusCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.net.HttpHeaders;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

import static com.app.pingpong.global.common.exception.StatusCode.SUCCESS_PUSH_ALARM;

@Component
@RequiredArgsConstructor
public class FcmUtil {

    private final String API_URL = "https://fcm.googleapis.com/v1/projects/ping-pong-410913/messages:send";
    private final ObjectMapper objectMapper;

    public StatusCode sendMessageTo(String targetToken, int type) throws IOException {
        String title = null;
        String body = null;

        if (type == 1) {
            title = PushMessage.TODO.getTitle();
            body = PushMessage.TODO.getBody();
        }
        if (type == 2) {
            title = PushMessage.TEAM.getTitle();
            body = PushMessage.TEAM.getBody();
        }
        if (type == 3) {
            title = PushMessage.FRIEND.getTitle();
            body = PushMessage.FRIEND.getBody();
        }
        if (type == 4) {
            title = PushMessage.HOST.getTitle();
            body = PushMessage.HOST.getBody();
        }

        String message = makeMessage(targetToken, title, body);
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(message, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(API_URL)
                .post(requestBody)
                .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                .build();

        Response response = client.newCall(request).execute();
        System.out.println(response.body().string());

        return SUCCESS_PUSH_ALARM;
    }

    public String getAccessToken() throws IOException {
        String firebaseConfigPath = "firebase/ping-pong-410913-firebase-adminsdk-ihk7h-917c894f1b.json";
        GoogleCredentials googleCredential = GoogleCredentials.fromStream(
                        new ClassPathResource(firebaseConfigPath).getInputStream())
                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

        googleCredential.refreshIfExpired();
        return googleCredential.getAccessToken().getTokenValue();
    }

    private String makeMessage(String targetToken, String title, String body) throws JsonProcessingException {
        FcmMessage fcmMessage = FcmMessage.builder()
                .message(FcmMessage.Message.builder()
                        .token(targetToken)
                        .notification(FcmMessage.Notification.builder()
                                .title(title)
                                .body(body)
                                .image(null)
                                .build()
                        )
                        .build()
                )
                .validate_only(false)
                .build();
        return objectMapper.writeValueAsString(fcmMessage);
    }
}
