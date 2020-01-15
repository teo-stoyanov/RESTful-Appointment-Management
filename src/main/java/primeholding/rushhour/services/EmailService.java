package primeholding.rushhour.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${spring.mail.host}")
    private String to;

    public void sendEmail(JavaMailSender javaMailSender) {

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(this.to);

        msg.setSubject("Testing from Spring Boot");
        msg.setText("Hello World \n You registered successfully");

        javaMailSender.send(msg);

    }
}
