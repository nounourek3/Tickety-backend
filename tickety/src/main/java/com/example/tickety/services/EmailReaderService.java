package com.example.tickety.services;

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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @Scheduled(fixedRate = 60000) // Every minute
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

            // ✅ Only get UNSEEN emails
            Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            System.out.println("📨 Found " + messages.length + " unread messages.");

            for (Message message : messages) {
                Address[] froms = message.getFrom();
                String sender = froms[0].toString();
                String subject = message.getSubject();
                Date sentDate = message.getSentDate();
                String content = extractContent(message);

                System.out.println("➡️ Processing email from: " + sender);
                System.out.println("📝 Subject: " + subject);
                System.out.println("📦 Content:\n" + content);

                User fallbackUser = userRepository.findById(1L).orElse(null);
                if (fallbackUser == null) {
                    System.out.println("❌ User ID 1 not found.");
                } else {
                    System.out.println("✅ Loaded user: " + fallbackUser.getEmail());
                }

                FlightEmail email = new FlightEmail();
                email.setSubject(subject);
                email.setSender(sender);
                email.setFlightDate(sentDate);
                email.setContent(content);
                email.setUser(fallbackUser);

                System.out.println("💾 Attempting to save...");
                flightEmailRepository.save(email);
                System.out.println("✅ Saved email with user ID: " + (fallbackUser != null ? fallbackUser.getId() : "null"));

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

    private String extractContent(Message message) throws Exception {
        Object content = message.getContent();
        if (content instanceof String) {
            return (String) content;
        } else if (content instanceof MimeMultipart) {
            return getTextFromMimeMultipart((MimeMultipart) content);
        }
        return "";
    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws Exception {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < mimeMultipart.getCount(); i++) {
            BodyPart part = mimeMultipart.getBodyPart(i);
            if (part.isMimeType("text/html") || part.isMimeType("text/plain")) {
                result.append(part.getContent());
            } else if (part.getContent() instanceof MimeMultipart) {
                result.append(getTextFromMimeMultipart((MimeMultipart) part.getContent()));
            }
        }
        return result.toString();
    }
    private LocalDate tryParseDate(String input) {
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH),
                DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("es")),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy")
        );
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(input, formatter);
            } catch (DateTimeParseException ignored) {}
        }
        return null;
    }



    public FlightEmail parseFlightDetailsFromContent(String content, User user, String subject, String sender) {
        FlightEmail flight = new FlightEmail();

        flight.setUser(user);
        flight.setSubject(subject);
        flight.setSender(sender);
        flight.setCreatedAt(new Date()); // java.util.Date


        // Booking Code — format like 1052-495-594
        Pattern bookingPattern = Pattern.compile("\\b\\d{3}-\\d{3}-\\d{3}\\b");
        Matcher bookingMatcher = bookingPattern.matcher(content);
        if (bookingMatcher.find()) {
            flight.setBookingCode(bookingMatcher.group());
        }

        // Airports — use 3-letter IATA codes
        Pattern airportPattern = Pattern.compile("\\b[A-Z]{3}\\b");
        Matcher airportMatcher = airportPattern.matcher(content);
        List<String> airports = new ArrayList<>();
        while (airportMatcher.find()) {
            airports.add(airportMatcher.group());
        }
        if (airports.size() >= 2) {
            flight.setDepartureAirport(airports.get(0));
            flight.setArrivalAirport(airports.get(1));
        }

        // Times — e.g., 18:25 or 20:40
        Pattern timePattern = Pattern.compile("\\b\\d{1,2}:\\d{2}\\b");
        Matcher timeMatcher = timePattern.matcher(content);
        List<String> times = new ArrayList<>();
        while (timeMatcher.find()) {
            times.add(timeMatcher.group());
        }
        if (times.size() >= 2) {
            flight.setDepartureTime(times.get(0));
            flight.setArrivalTime(times.get(1));
        }


        // Flight date — pick first DD MMMM YYYY or "dd/MM/yyyy"-like pattern
        Pattern datePattern = Pattern.compile("(\\d{1,2})[\\s/-](\\w+)[\\s/-](\\d{4})");
        Matcher dateMatcher = datePattern.matcher(content);
        if (dateMatcher.find()) {
            String rawDate = dateMatcher.group(0);
            LocalDate parsedDate = tryParseDate(rawDate);
            if (parsedDate != null) {
                flight.setFlightDate(java.sql.Date.valueOf(parsedDate));
            }

        }
        // Flight Number — format like UX1090, AF456, etc.
        Pattern flightNumberPattern = Pattern.compile("\\b[A-Z]{2}\\d{2,4}\\b");
        Matcher flightNumberMatcher = flightNumberPattern.matcher(content);
        if (flightNumberMatcher.find()) {
            flight.setFlightNumber(flightNumberMatcher.group());
        }

        return flight;
    }

}

