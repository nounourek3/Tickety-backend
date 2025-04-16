package com.example.tickety.services;

import com.example.tickety.entities.FlightEmail;
import com.example.tickety.entities.User;
import com.example.tickety.repositories.FlightEmailRepository;
import com.example.tickety.repositories.UserRepository;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMultipart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Properties;

@Service
public class EmailReaderService {

    @Autowired
    private final FlightEmailRepository flightEmailRepository;

    @Autowired
    private final UserRepository userRepository;

    public EmailReaderService(FlightEmailRepository flightEmailRepository, UserRepository userRepository) {
        this.flightEmailRepository = flightEmailRepository;
        this.userRepository = userRepository;
    }

    @Value("${email.username}")
    private String username;

    @Value("${email.password}")
    private String password;

    @Scheduled(fixedRate = 60000)
    public void checkInbox() {
        try {
            Properties props = new Properties();
            props.put("mail.store.protocol", "imaps");

            Session session = Session.getInstance(props);
            Store store = session.getStore();
            store.connect("imap.gmail.com", username, password);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Message[] messages = inbox.getMessages();
            for (Message message : messages) {
                Address[] froms = message.getFrom();
                String sender = froms[0].toString();
                String subject = message.getSubject();
                Date sentDate = message.getSentDate();
                String content = extractContent(message);

                Address[] recipients = message.getRecipients(Message.RecipientType.TO);
                if (recipients != null && recipients.length > 0) {
                    String toEmail = recipients[0].toString();
                    String aliasId = extractUserIdFromAlias(toEmail);

                    if (aliasId != null) {
                        try {
                            Long userId = Long.parseLong(aliasId);
                            User user = userRepository.findById(userId).orElse(null);
                            if (user != null) {
                                FlightEmail email = new FlightEmail();
                                email.setSubject(subject);
                                email.setSender(sender);
                                email.setFlightDate(sentDate);
                                email.setUser(user);

                                flightEmailRepository.save(email);
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }
                message.setFlag(Flags.Flag.SEEN, true);
            }
            inbox.close(false);
            store.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String extractUserIdFromAlias(String email) {
        try {
            int plus = email.indexOf('+');
            int at = email.indexOf('@');
            if (plus != -1 && at != -1) {
                return email.substring(plus + 1, at);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String extractContent(Message message) throws Exception {
        Object content = message.getContent();
        if (content instanceof String) {
            return (String) content;
        } else if (content instanceof MimeMultipart) {
            MimeMultipart multipart = (MimeMultipart) content;
            return multipart.getBodyPart(0).getContent().toString();
        }
        return "";
    }
}
