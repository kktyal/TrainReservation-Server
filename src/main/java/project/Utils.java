package project;

import org.json.JSONObject;
import project.server.enums.CommonResult;
import project.server.enums.interfaces.IResult;
import project.server.enums.trainResult.TrainResult;
import project.server.lang.Pair;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Utils {

    public static String ADMIN = "admin.com";

    public static Pair<Enum<? extends IResult>, List<?>> getListPair(List<?> result) {
        Pair<Enum<? extends IResult>, List<?>> pair = new Pair<>(null, null);
        if (result.size() == 0) {
            pair.setKey(TrainResult.NO_SEARCH_DATA);
        } else {
            pair.setKey(CommonResult.SUCCESS);
            pair.setValue(result);
        }
        return pair;
    }
    public static Pair<Enum<? extends IResult>, Map<String,List<String>>> getListPair(List<String> premium,List<String> standard) {
        Pair<Enum<? extends IResult>, Map<String,List<String>>> pair = new Pair<>(null, null);
        if (premium.size() == 0 || standard.size()==0) {
            pair.setKey(TrainResult.NO_SEARCH_DATA);
        } else {
            pair.setKey(CommonResult.SUCCESS);
            Map<String,List<String>> result = new HashMap<>();

            result.put("premium",premium);
            result.put("standard",standard);

            pair.setValue(result);

        }
        return pair;
    }

    public static Pair<Enum<? extends IResult>, Integer> getIngegerPair(Integer result) {
        Pair<Enum<? extends IResult>, Integer> pair = new Pair<>(null, null);

        if (result == 0) {
            pair.setKey(TrainResult.NO_SEARCH_DATA);
        } else {
            pair.setKey(CommonResult.SUCCESS);
            pair.setValue(result);
        }
        return pair;
    }
    public static JSONObject getJsonObject(Enum<? extends IResult> result) {
        JSONObject object = new JSONObject();

        if (result.equals(CommonResult.SUCCESS)) {
            object.put("result", result.name().toLowerCase());
        } else {
            object.put("result", CommonResult.FAILURE.name().toLowerCase());
            object.put("message", result.name().toLowerCase());
        }
        return object;
    }



    public static JSONObject getJsonObject(Enum<? extends IResult> result, List<?> data) {
        JSONObject object = new JSONObject();

        if (result.equals(CommonResult.SUCCESS)) {
            object.put("result", result.name().toLowerCase());
            object.put("data", data);
        } else {
            object.put("result", CommonResult.FAILURE.name().toLowerCase());
            object.put("message", result.name().toLowerCase());
        }
        return object;
    }


    public static JSONObject getJsonObject(Enum<? extends IResult> result, Object data) {
        JSONObject jsonData = new JSONObject();

        if (result.equals(CommonResult.SUCCESS)) {
            jsonData.put("result", result.name().toLowerCase());
            jsonData.put("data", data);
        } else {
            jsonData.put("result", CommonResult.FAILURE.name().toLowerCase());
            jsonData.put("message", result.name().toLowerCase());
        }
        return jsonData;
    }
    public static JSONObject getJsonObject(Enum<? extends IResult> result, Map<?,?> data) {
        JSONObject jsonData = new JSONObject();

        if (result.equals(CommonResult.SUCCESS)) {
            jsonData.put("result", result.name().toLowerCase());
            jsonData.put("data", data);
        } else {
            jsonData.put("result", CommonResult.FAILURE.name().toLowerCase());
            jsonData.put("message", result.name().toLowerCase());
        }
        return jsonData;
    }




    public static String md5(String s) {
        String MD5 = "MD5";
        try {
            // MD5 해시 생성
            MessageDigest digest = MessageDigest.getInstance(MD5);
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();

            // 16진수로 변환하여 문자열 생성
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }


    public static String createAuthCode() {
        // 비밀번호 생성 (MD5 해시를 사용)
        String regex = "[^A-Za-z0-9]*";
        String md5Result = md5(new StringBuilder()
                .append(regex.replaceAll("\\\\", "\\\\\\\\").replaceAll("\\[\\^", "[^\\\\"))
                .toString());

        // 생성된 해시에서 랜덤한 8자리 비밀번호 생성
        StringBuilder stringBuffer = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            int randomNumber = random.nextInt() % md5Result.length();
            if (randomNumber < 0) {
                randomNumber += md5Result.length();
            }
            stringBuffer.append(md5Result.charAt(randomNumber));
        }
        String token = stringBuffer.toString();
        return token;
    }
    public static String hashSha512(String input) {
        try {
            StringBuilder passwordSaltHashBuilder = new StringBuilder();
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(input.getBytes(StandardCharsets.UTF_8));
            for (byte hashByte : md.digest()) {
                passwordSaltHashBuilder.append(String.format("%02x", hashByte));
            }
            return passwordSaltHashBuilder.toString();
        } catch (NoSuchAlgorithmException ignored) {
            return null;
        }

    }


    public static class EmailSender {

        public void sendEmail(String host, String port, String username, String password, String recipientEmail,
                              String subject, String content) throws MessagingException {
            // SMTP 서버 설정
            Properties properties = new Properties();
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.port", port);

            // 인증 정보 설정
            Authenticator authenticator = new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            };

            // 세션 생성
            Session session = Session.getInstance(properties, authenticator);

            // 메시지 생성
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(content);

            // 이메일 전송
            Transport.send(message);

        }
    }

}
