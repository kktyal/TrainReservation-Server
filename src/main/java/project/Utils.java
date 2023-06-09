package project;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.Random;

public class Utils {

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
