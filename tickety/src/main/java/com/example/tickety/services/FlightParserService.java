package com.example.tickety.services;

import com.example.tickety.entities.EmailAttachment;
import com.example.tickety.entities.FlightEmail;
import com.example.tickety.entities.User;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.*;

@Service
public class FlightParserService {

    private final Map<String, List<String>> fieldKeywords = Map.of(
            "flightDate", List.of("flight date", "departing", "voyage aller", "viaje de ida", "fecha de vuelo", "date du vol", "le", "on"),
            "departureTime", List.of("departure", "heure de départ", "salida", "departs", "at"),
            "arrivalTime", List.of("arrival", "heure d'arrivée", "llegada", "arrives"),
            "departureAirport", List.of("from", "au départ de", "desde", "départ", "salida"),
            "arrivalAirport", List.of("to", "à destination de", "hacia", "arrivée", "llegada"),
            "bookingCode", List.of("booking", "référence", "reserva", "numéro de commande", "order number", "PNR", "confirmation", "code de réservation"),
            "flightNumber", List.of("flight", "vol", "vuelo", "n° de vol")
    );

    private final List<String> dateFormats = List.of("dd MMM yyyy", "dd MMMM yyyy", "dd/MM/yyyy", "MM/dd/yyyy", "dd MMM", "MMM dd", "EEEE dd MMMM yyyy");
    private final Map<String, String> frenchToEnglishMonths = Map.ofEntries(
            Map.entry("janvier", "Jan"),
            Map.entry("février", "Feb"),
            Map.entry("mars", "Mar"),
            Map.entry("avril", "Apr"),
            Map.entry("mai", "May"),
            Map.entry("juin", "Jun"),
            Map.entry("juillet", "Jul"),
            Map.entry("août", "Aug"),
            Map.entry("septembre", "Sep"),
            Map.entry("octobre", "Oct"),
            Map.entry("novembre", "Nov"),
            Map.entry("décembre", "Dec")
    );

    private final Pattern airportCodePattern = Pattern.compile("\\b[A-Z]{3}\\b");
    private final List<String> commonNonAirportCodes = List.of("AIR", "PDF", "EUR", "USD", "GMT", "VOYAGE", "TUN", "TUE", "APR");

    public Map<String, String> extractFlightInfo(String rawText, String subject, Date fallbackDate) {
        Map<String, String> result = new HashMap<>();

        // 🧹 Clean HTML to plain text
        String text = Jsoup.parse(rawText).text();
        System.out.println("📄 CLEANED TEXT:\n" + text); // Debug — remove later

        // ✈️ Flight Number (e.g. TO4781)
        String flightNumber = findValueNearKeywords(text, fieldKeywords.get("flightNumber"), "\\b[A-Z]{2}\\s?\\d{2,4}\\b");
        if (flightNumber != null) {
            result.put("flightNumber", flightNumber.replaceAll("\\s+", ""));
        }

        // 🔐 Booking Code (e.g. PFJKXD)
        String bookingCode = findValueNearKeywords(text, fieldKeywords.get("bookingCode"), "\\b[A-Z0-9]{5,8}\\b|\\b\\d{3}-\\d{3}-\\d{3}\\b");
        if (bookingCode != null && !fieldKeywords.get("bookingCode").contains(bookingCode.toLowerCase())) {
            result.put("bookingCode", bookingCode);
        }

        // 🛫🛬 Airports (general 3-letter codes, filtered)
        Matcher matcher = Pattern.compile("\\b([A-Z]{3})\\b[^A-Z\\n]{0,20}\\b([A-Z]{3})\\b").matcher(text);
        while (matcher.find()) {
            String code1 = matcher.group(1);
            String code2 = matcher.group(2);
            if (!commonNonAirportCodes.contains(code1) && !commonNonAirportCodes.contains(code2)) {
                result.put("departureAirport", code1);
                result.put("arrivalAirport", code2);
                break;
            }
        }

        // ⏰ Times (first two times → dep + arr)
        Matcher timeMatch = Pattern.compile("\\b\\d{1,2}[:h]\\d{2}(\\s?[APMapm]{2})?\\b").matcher(text);
        List<String> times = new ArrayList<>();
        while (timeMatch.find()) {
            times.add(timeMatch.group());
            if (times.size() >= 2) break;
        }
        if (times.size() >= 1) result.put("departureTime", times.get(0));
        if (times.size() >= 2) result.put("arrivalTime", times.get(1));

        // 📅 Date (e.g. Sunday 18 May 2025)
        Matcher dateMatcher = Pattern.compile("(Sunday|Monday|Tuesday|Wednesday|Thursday|Friday|Saturday)\\s+\\d{1,2}\\s+\\w+\\s+\\d{4}").matcher(text);
        if (dateMatcher.find()) {
            String rawDate = dateMatcher.group();
            Date parsed = tryParseDateWithYearFallback(rawDate, fallbackDate);
            if (parsed != null) {
                result.put("flightDate", new SimpleDateFormat("yyyy-MM-dd").format(parsed));
            }
        } else {
            // fallback: extract dd/MM or dd MMM
            String fallbackDateStr = findValueNearKeywords(text, fieldKeywords.get("flightDate"), "\\b(\\d{1,2})[\\s/-]?(\\w{3,}|\\w{4,})\\b");
            if (fallbackDateStr != null) {
                Date parsed = tryParseDateWithYearFallback(fallbackDateStr, fallbackDate);
                if (parsed != null) {
                    result.put("flightDate", new SimpleDateFormat("yyyy-MM-dd").format(parsed));
                } else {
                    Matcher dm = Pattern.compile("(\\d{1,2})[\\s/-]?(\\w+)").matcher(fallbackDateStr);
                    if (dm.find()) {
                        result.put("flightDay", dm.group(1));
                        result.put("flightMonth", dm.group(2));
                    }
                }
            }
        }

        return result;
    }

    public FlightEmail parse(
            String rawText,
            String subject,
            String sender,
            User user,
            Date fallbackDate,
            List<EmailAttachment> attachments
    ) {
        String text = Jsoup.parse(rawText).text(); // default to raw HTML text

        // 📎 Use PDF content if available
        for (EmailAttachment attachment : attachments) {
            if (attachment.getMimeType().toLowerCase().contains("pdf")) {
                try {
                    text = extractTextFromPdf(attachment.getContent());
                    System.out.println("✅ Using PDF content for rawText");
                    break;
                } catch (IOException e) {
                    System.out.println("⚠️ Failed to parse PDF, falling back to email HTML content");
                }
            }
        }

        // ✈️ Extract all fields
        Map<String, String> extracted = extractFlightInfo(text, subject, fallbackDate);

        // 💾 Create and fill FlightEmail
        FlightEmail email = new FlightEmail();
        email.setUser(user);
        email.setSubject(subject);
        email.setSender(sender);
        email.setCreatedAt(fallbackDate != null ? fallbackDate : new Date());
        email.setRawText(text);

        email.setFlightNumber(extracted.getOrDefault("flightNumber", null));
        email.setFlightDate(parseDate(extracted.get("flightDate")));
        email.setDepartureTime(extracted.getOrDefault("departureTime", null));
        email.setArrivalTime(extracted.getOrDefault("arrivalTime", null));
        email.setDepartureAirport(extracted.getOrDefault("departureAirport", null));
        email.setArrivalAirport(extracted.getOrDefault("arrivalAirport", null));
        email.setBookingCode(extracted.getOrDefault("bookingCode", null));

        return email;
    }
    private Date parseDate(String dateString) {
        if (dateString == null) return null;
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }





    private String findValueNearKeywords(String text, List<String> keywords, String regex) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        String bestMatch = null;
        int minDistance = Integer.MAX_VALUE;

        for (String keyword : keywords) {
            int keywordIndex = text.toLowerCase().indexOf(keyword.toLowerCase());
            if (keywordIndex != -1) {
                Matcher matcher = pattern.matcher(text);
                while (matcher.find()) {
                    int matchIndex = matcher.start();
                    int distance = Math.abs(matchIndex - keywordIndex);
                    if (distance < minDistance) {
                        minDistance = distance;
                        bestMatch = matcher.group();
                    }
                }
            }
        }
        return bestMatch;
    }

    private String findAirportNearKeywordStrict(String text, List<String> keywords) {
        for (String keyword : keywords) {
            int keywordIndex = text.toLowerCase().indexOf(keyword.toLowerCase());
            if (keywordIndex != -1) {
                Matcher matcher = airportCodePattern.matcher(text.substring(Math.max(0, keywordIndex), Math.min(text.length(), keywordIndex + 15)));
                if (matcher.find()) {
                    String code = matcher.group();
                    if (!commonNonAirportCodes.contains(code.toUpperCase())) {
                        return code;
                    }
                }
            }
        }
        return null;
    }

    private String findSingleTimeNearKeywords(String text, List<String> keywords) {
        String bestMatch = null;
        int minDistance = Integer.MAX_VALUE;
        Pattern timePattern = Pattern.compile("\\b\\d{1,2}[:h]\\d{2}(\\s?[APMapm]{2})?\\b", Pattern.CASE_INSENSITIVE);

        for (String keyword : keywords) {
            int keywordIndex = text.toLowerCase().indexOf(keyword.toLowerCase());
            if (keywordIndex != -1) {
                Matcher timeMatcher = timePattern.matcher(text);
                while (timeMatcher.find()) {
                    int timeIndex = timeMatcher.start();
                    int distance = Math.abs(timeIndex - keywordIndex);
                    if (distance < minDistance) {
                        minDistance = distance;
                        bestMatch = timeMatcher.group();
                    }
                }
            }
        }
        return bestMatch;
    }

    private Map<String, String> findDepartureArrivalTime(String text) {
        Map<String, String> times = new HashMap<>();
        Pattern depPattern = Pattern.compile("(?i)(departure|depart|salida|heure de départ)[^\\d\\n]{0,15}(\\d{1,2}[:h]\\d{2}(\\s?[APMapm]{2})?)", Pattern.DOTALL);
        Matcher depMatcher = depPattern.matcher(text);
        if (depMatcher.find()) {
            times.put("departureTime", depMatcher.group(2).trim());
        }

        Pattern arrPattern = Pattern.compile("(?i)(arrival|arrive|llegada|heure d'arrivée)[^\\d\\n]{0,15}(\\d{1,2}[:h]\\d{2}(\\s?[APMapm]{2})?)", Pattern.DOTALL);
        Matcher arrMatcher = arrPattern.matcher(text);
        if (arrMatcher.find()) {
            times.put("arrivalTime", arrMatcher.group(2).trim());
        }
        return times;
    }

    private Date tryParseDateWithYearFallback(String dateString, Date fallbackDate) {
        for (Map.Entry<String, String> entry : frenchToEnglishMonths.entrySet()) {
            dateString = dateString.toLowerCase().replace(entry.getKey(), entry.getValue());
        }

        for (String format : dateFormats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);
                Date parsedDate = sdf.parse(dateString);
                if (!format.contains("yyyy") && parsedDate != null) {
                    Calendar parsed = Calendar.getInstance();
                    parsed.setTime(parsedDate);
                    Calendar fallback = Calendar.getInstance();
                    fallback.setTime(fallbackDate);
                    parsed.set(Calendar.YEAR, fallback.get(Calendar.YEAR));
                    return parsed.getTime();
                }
                return parsedDate;
            } catch (ParseException ignored) {}
        }

        return null;
    }

    private String extractTextFromPdf(byte[] pdfBytes) throws IOException {
        try (PDDocument doc = PDDocument.load(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }
}
