package com.example.tickety.services;

import com.example.tickety.entities.EmailAttachment;
import com.example.tickety.entities.FlightEmail;
import com.example.tickety.entities.User;
import com.example.tickety.repositories.FlightEmailRepository;
import com.example.tickety.repositories.UserRepository;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.FlagTerm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Service
public class EmailReaderService {

    private final FlightEmailRepository flightEmailRepository;
    private final UserRepository userRepository;
    private final FlightParserService flightParserService;

    @Autowired
    public EmailReaderService(
            FlightEmailRepository flightEmailRepository,
            UserRepository userRepository,
            FlightParserService flightParserService
    ) {
        this.flightEmailRepository = flightEmailRepository;
        this.userRepository = userRepository;
        this.flightParserService = flightParserService;
    }

    @Value("${email.username}")
    private String username;

    @Value("${email.password}")
    private String password;

    @Scheduled(fixedRate = 60000)
    public void checkInbox() {
        try {
            System.out.println("⏰ checkInbox() triggered at " + new Date());

            Properties props = new Properties();
            props.put("mail.store.protocol", "imaps");

            Session session = Session.getInstance(props);
            Store store = session.getStore();
            store.connect("imap.gmail.com", username, password);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            System.out.println("📨 Found " + messages.length + " unread messages.");

            for (Message message : messages) {
                Address[] froms = message.getFrom();
                String sender = froms[0].toString();
                String subject = message.getSubject();
                Date sentDate = message.getSentDate();
                String htmlContent = extractHtmlContent(message);
                List<EmailAttachment> attachments = extractAttachments(message);

                System.out.println("➡️ Processing email from: " + sender);
                System.out.println("📝 Subject: " + subject);
                System.out.println("📦 Content:\n" + htmlContent);

                String aliasEmail = extractAliasFromRecipient(message);
                Long userId = extractUserIdFromAlias(aliasEmail);
                User user = userRepository.findById(userId).orElse(null);

                if (user == null) {
                    System.out.println("❌ User ID " + userId + " not found. Skipping.");
                    continue;
                }

                System.out.println("✅ Loaded user: " + user.getEmail());

                FlightEmail email = flightParserService.parse(
                        htmlContent, subject, sender, user, sentDate, attachments
                );

                System.out.println("💾 Attempting to save...");
                flightEmailRepository.save(email);
                System.out.println("✅ Saved email with user ID: " + user.getId());

                message.setFlag(Flags.Flag.SEEN, true);
                System.out.println("✅ Marked message as SEEN.");
            }


            inbox.close(false);
            store.close();
            System.out.println("📬 Done checking inbox.\n");

        } catch (Exception e) {
            System.out.println("❌ Exception while reading inbox:");
            e.printStackTrace();
        }
    }


    private String extractHtmlContent(Part part) throws Exception {
        if (part.isMimeType("text/html")) {
            return part.getContent().toString();
        } else if (part.isMimeType("text/plain")) {
            return part.getContent().toString();
        } else if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                String result = extractHtmlContent(bodyPart);
                if (result != null && !result.isBlank()) {
                    return result;
                }
            }
        }
        return "";
    }

    private List<EmailAttachment> extractAttachments(Part part) throws Exception {
        List<EmailAttachment> attachments = new ArrayList<>();

        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);

                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())
                        && bodyPart.getFileName().toLowerCase().endsWith(".pdf")) {

                    byte[] content = bodyPart.getInputStream().readAllBytes();
                    attachments.add(new EmailAttachment(
                            bodyPart.getFileName(),
                            content,
                            bodyPart.getContentType()
                    ));
                }
            }
        }

        return attachments;
    }
    private String extractAliasFromRecipient(Message message) throws MessagingException {
        Address[] recipients = message.getRecipients(Message.RecipientType.TO);
        if (recipients == null || recipients.length == 0) return "";
        return recipients[0].toString(); // viajestickety+9@gmail.com
    }

    private Long extractUserIdFromAlias(String address) {
        try {
            int plusIndex = address.indexOf('+');
            int atIndex = address.indexOf('@');
            if (plusIndex >= 0 && atIndex > plusIndex) {
                String idStr = address.substring(plusIndex + 1, atIndex);
                return Long.parseLong(idStr);
            }
        } catch (Exception e) {
            System.out.println("⚠️ Failed to parse userId from alias: " + e.getMessage());
        }
        return 1L; // fallback
    }

}
