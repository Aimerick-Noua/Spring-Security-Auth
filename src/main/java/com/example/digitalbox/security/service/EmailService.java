package com.example.digitalbox.security.service;

import com.example.digitalbox.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${spring.mail.username}")
    private String email;
    @Value("${spring.mail.password}")
    private String password;

    public static String getFirstName(String fullName) {
        if (fullName != null && !fullName.isEmpty()) {
            String[] nameParts = fullName.split("\\s+");
            return nameParts[0];
        } else {
            return null;
        }
    }

    public boolean sendAccountActivationLink(User user) {
        String firstname = getFirstName(user.getFullName());
        String subject = "Activate Your Account - [DIGITAL BOX]";
        String activationLink = "http://localhost:8087/api/auth/verify?email=" + user.getEmail() + "&code=" + user.getVerificationToken();
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(email));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(user.getEmail()));
            message.setSubject(subject);

            // Create the HTML content
            String htmlContent = "<html>" +
                    "<head>" +
                    "<style>" +
                    "  body {" +
                    "    font-family: 'Arial', sans-serif;" +
                    "    background-color: #f4f4f4;" +
                    "    margin: 0;" +
                    "    padding: 0;" +
                    "  }" +
                    "  .container {" +
                    "    width: 80%;" +
                    "    margin: auto;" +
                    "    overflow: hidden;" +
                    "  }" +
                    "  .card {" +
                    "    background: #ffffff;" +
                    "    border-radius: 10px;" +
                    "    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);" +
                    "    margin-top: 20px;" +
                    "    padding: 20px;" +
                    "  }" +
                    "  .header {" +
                    "    text-align: center;" +
                    "    padding: 20px 0;" +
                    "  }" +
                    "  .content {" +
                    "    text-align: center;" +
                    "    padding: 20px 0;" +
                    "  }" +
                    "  .button {" +
                    "    display: inline-block;" +
                    "    padding: 10px 20px;" +
                    "    text-decoration: none;" +
                    "    background-color: #007bff;" +
                    "    color: #ffffff;" +
                    "    border-radius: 5px;" +
                    "  }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<div class='container'>" +
                    "  <div class='card'>" +
                    "    <div class='header'>" +
                    "      <h2>Account Activation</h2>" +
                    "    </div>" +
                    "    <div class='content'>" +
                    "      <p>Dear "+firstname+",</p>" +
                    "      <br/><p>Thank you for registering. To activate your account, click the button below:</p>" +
                    "      <a href='" + activationLink + "' class='button'>Activate Account</a>" +
                    "    </div>" +
                    "  </div>" +
                    "</div>" +
                    "</body>" +
                    "</html>";

            message.setContent(htmlContent, "text/html");

            Transport.send(message);
            System.out.println("Email sent successfully.");
            return true;
        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
            return false;
        }


    }

}