package com.villavredestein.service;

import com.villavredestein.model.Invoice;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public class InvoiceReminderFormatter {

    private static final Locale NL = new Locale("nl", "NL");
    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("d MMMM yyyy", NL);

    private static String maandNaam(int maand, int jaar) {
        try {
            Month m = Month.of(maand);
            return m.getDisplayName(TextStyle.FULL, NL) + " " + jaar;
        } catch (Exception e) {
            return "onbekende maand " + jaar;
        }
    }

    public static String subjectDueSoon(Invoice i) {
        String maandTekst = maandNaam(i.getMonth(), i.getYear());
        String datum = formatDate(i.getDueDate());
        return "Herinnering: huur " + maandTekst + " vervalt op " + datum;
    }

    public static String subjectOverdue(Invoice i) {
        String maandTekst = maandNaam(i.getMonth(), i.getYear());
        String datum = formatDate(i.getDueDate());
        return "Achterstand: huur " + maandTekst + " (vervallen op " + datum + ")";
    }

    public static String bodyDueSoon(Invoice i, String studentNaam) {
        String naam = studentNaam != null ? studentNaam : "student";
        String maandTekst = maandNaam(i.getMonth(), i.getYear());
        String vervalDatum = formatDate(i.getDueDate());
        String beschrijving = i.getDescription() != null ? i.getDescription() : "Huurbetaling";
        String status = i.getStatus() != null ? i.getStatus() : "OPEN";

        return """
                Beste %s,

                Dit is een vriendelijke herinnering dat je huurfactuur voor %s (€%.2f) vervalt op %s.

                Beschrijving: %s
                Huidige status: %s

                Zou je deze vóór de vervaldatum willen voldoen?
                Dank je wel alvast voor de betaling en je zorgvuldigheid.

                Met vriendelijke groet,
                
                De beheerder
                Villa Vredestein
                """.formatted(naam, maandTekst, i.getAmount(), vervalDatum, beschrijving, status);
    }

    public static String bodyOverdue(Invoice i, String studentNaam) {
        String naam = studentNaam != null ? studentNaam : "student";
        String maandTekst = maandNaam(i.getMonth(), i.getYear());
        String vervalDatum = formatDate(i.getDueDate());
        String beschrijving = i.getDescription() != null ? i.getDescription() : "Huurbetaling";
        String status = i.getStatus() != null ? i.getStatus() : "OPEN";

        return """
                Beste %s,

                Volgens onze administratie staat de huurfactuur voor %s (€%.2f), vervallen op %s, nog open.

                Beschrijving: %s
                Huidige status: %s

                Wil je de betaling zo snel mogelijk uitvoeren of laten weten wanneer deze gepland staat?
                Als er iets aan de hand is, neem dan gerust even contact op.

                Met vriendelijke groet,
                
                De beheerder
                Villa Vredestein
                """.formatted(naam, maandTekst, i.getAmount(), vervalDatum, beschrijving, status);
    }

    private static String formatDate(LocalDate date) {
        if (date == null) return "onbekende datum";
        return date.format(DMY);
    }
}