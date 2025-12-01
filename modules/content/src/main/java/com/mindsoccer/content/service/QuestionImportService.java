package com.mindsoccer.content.service;

import com.mindsoccer.content.entity.QuestionEntity;
import com.mindsoccer.content.entity.ThemeEntity;
import com.mindsoccer.content.repository.QuestionRepository;
import com.mindsoccer.content.repository.ThemeRepository;
import com.mindsoccer.protocol.enums.Difficulty;
import com.mindsoccer.protocol.enums.QuestionFormat;
import com.mindsoccer.protocol.enums.RoundType;
import com.mindsoccer.shared.exception.ValidationException;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Service d'import de questions depuis CSV ou Excel.
 *
 * Format CSV/Excel attendu:
 * text_fr | text_en | answer | alt_answers | difficulty | round_type | theme_code | format | choices | hint_fr | explanation_fr
 */
@Service
public class QuestionImportService {

    private static final Logger log = LoggerFactory.getLogger(QuestionImportService.class);

    private static final int COL_TEXT_FR = 0;
    private static final int COL_TEXT_EN = 1;
    private static final int COL_ANSWER = 2;
    private static final int COL_ALT_ANSWERS = 3;
    private static final int COL_DIFFICULTY = 4;
    private static final int COL_ROUND_TYPE = 5;
    private static final int COL_THEME_CODE = 6;
    private static final int COL_FORMAT = 7;
    private static final int COL_CHOICES = 8;
    private static final int COL_HINT_FR = 9;
    private static final int COL_EXPLANATION_FR = 10;

    private final QuestionRepository questionRepository;
    private final ThemeRepository themeRepository;

    public QuestionImportService(QuestionRepository questionRepository, ThemeRepository themeRepository) {
        this.questionRepository = questionRepository;
        this.themeRepository = themeRepository;
    }

    @Transactional
    public ImportResult importFromCsv(InputStream inputStream) {
        List<QuestionEntity> imported = new ArrayList<>();
        List<ImportError> errors = new ArrayList<>();
        int lineNumber = 1;

        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String[] header = reader.readNext();
            if (header == null) {
                throw ValidationException.importFailed("Fichier CSV vide");
            }
            lineNumber++;

            String[] row;
            while ((row = reader.readNext()) != null) {
                try {
                    QuestionEntity question = parseRow(row);
                    imported.add(questionRepository.save(question));
                } catch (Exception e) {
                    errors.add(new ImportError(lineNumber, e.getMessage()));
                }
                lineNumber++;
            }
        } catch (IOException | CsvValidationException e) {
            log.error("Erreur lors de l'import CSV", e);
            throw ValidationException.importFailed("Erreur de lecture CSV: " + e.getMessage());
        }

        log.info("Import CSV terminé: {} questions importées, {} erreurs", imported.size(), errors.size());
        return new ImportResult(imported.size(), errors);
    }

    @Transactional
    public ImportResult importFromExcel(InputStream inputStream) {
        List<QuestionEntity> imported = new ArrayList<>();
        List<ImportError> errors = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (rowIterator.hasNext()) {
                rowIterator.next(); // Skip header
            }

            int rowNumber = 2;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                try {
                    QuestionEntity question = parseExcelRow(row);
                    if (question != null) {
                        imported.add(questionRepository.save(question));
                    }
                } catch (Exception e) {
                    errors.add(new ImportError(rowNumber, e.getMessage()));
                }
                rowNumber++;
            }
        } catch (IOException e) {
            log.error("Erreur lors de l'import Excel", e);
            throw ValidationException.importFailed("Erreur de lecture Excel: " + e.getMessage());
        }

        log.info("Import Excel terminé: {} questions importées, {} erreurs", imported.size(), errors.size());
        return new ImportResult(imported.size(), errors);
    }

    private QuestionEntity parseRow(String[] row) {
        if (row.length < 3) {
            throw new IllegalArgumentException("Ligne incomplète: au moins text_fr, text_en et answer requis");
        }

        String textFr = row[COL_TEXT_FR].trim();
        String textEn = row.length > COL_TEXT_EN ? row[COL_TEXT_EN].trim() : null;
        String answer = row[COL_ANSWER].trim();

        if (textFr.isEmpty() || answer.isEmpty()) {
            throw new IllegalArgumentException("text_fr et answer sont obligatoires");
        }

        QuestionEntity question = new QuestionEntity(textFr, answer);
        question.setTextEn(textEn);

        if (row.length > COL_ALT_ANSWERS && !row[COL_ALT_ANSWERS].isBlank()) {
            Set<String> altAnswers = new HashSet<>(Arrays.asList(row[COL_ALT_ANSWERS].split("\\|")));
            question.setAlternativeAnswers(altAnswers);
        }

        if (row.length > COL_DIFFICULTY && !row[COL_DIFFICULTY].isBlank()) {
            question.setDifficulty(Difficulty.valueOf(row[COL_DIFFICULTY].trim().toUpperCase()));
        }

        if (row.length > COL_ROUND_TYPE && !row[COL_ROUND_TYPE].isBlank()) {
            question.setRoundType(RoundType.valueOf(row[COL_ROUND_TYPE].trim().toUpperCase()));
        }

        if (row.length > COL_THEME_CODE && !row[COL_THEME_CODE].isBlank()) {
            String themeCode = row[COL_THEME_CODE].trim();
            themeRepository.findByCode(themeCode).ifPresent(question::setTheme);
        }

        if (row.length > COL_FORMAT && !row[COL_FORMAT].isBlank()) {
            question.setQuestionFormat(QuestionFormat.valueOf(row[COL_FORMAT].trim().toUpperCase()));
        }

        if (row.length > COL_CHOICES && !row[COL_CHOICES].isBlank()) {
            List<String> choices = Arrays.asList(row[COL_CHOICES].split("\\|"));
            question.setChoices(choices);
            question.setQuestionFormat(QuestionFormat.MULTIPLE_CHOICE);
        }

        if (row.length > COL_HINT_FR && !row[COL_HINT_FR].isBlank()) {
            question.setHintFr(row[COL_HINT_FR].trim());
        }

        if (row.length > COL_EXPLANATION_FR && !row[COL_EXPLANATION_FR].isBlank()) {
            question.setExplanationFr(row[COL_EXPLANATION_FR].trim());
        }

        return question;
    }

    private QuestionEntity parseExcelRow(Row row) {
        String textFr = getCellValueAsString(row.getCell(COL_TEXT_FR));
        String answer = getCellValueAsString(row.getCell(COL_ANSWER));

        if (textFr == null || textFr.isEmpty() || answer == null || answer.isEmpty()) {
            return null;
        }

        QuestionEntity question = new QuestionEntity(textFr, answer);
        question.setTextEn(getCellValueAsString(row.getCell(COL_TEXT_EN)));

        String altAnswers = getCellValueAsString(row.getCell(COL_ALT_ANSWERS));
        if (altAnswers != null && !altAnswers.isBlank()) {
            Set<String> altSet = new HashSet<>(Arrays.asList(altAnswers.split("\\|")));
            question.setAlternativeAnswers(altSet);
        }

        String difficulty = getCellValueAsString(row.getCell(COL_DIFFICULTY));
        if (difficulty != null && !difficulty.isBlank()) {
            try {
                question.setDifficulty(Difficulty.valueOf(difficulty.toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }

        String roundType = getCellValueAsString(row.getCell(COL_ROUND_TYPE));
        if (roundType != null && !roundType.isBlank()) {
            try {
                question.setRoundType(RoundType.valueOf(roundType.toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }

        String themeCode = getCellValueAsString(row.getCell(COL_THEME_CODE));
        if (themeCode != null && !themeCode.isBlank()) {
            themeRepository.findByCode(themeCode).ifPresent(question::setTheme);
        }

        String format = getCellValueAsString(row.getCell(COL_FORMAT));
        if (format != null && !format.isBlank()) {
            try {
                question.setQuestionFormat(QuestionFormat.valueOf(format.toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }

        String choices = getCellValueAsString(row.getCell(COL_CHOICES));
        if (choices != null && !choices.isBlank()) {
            List<String> choiceList = Arrays.asList(choices.split("\\|"));
            question.setChoices(choiceList);
            question.setQuestionFormat(QuestionFormat.MULTIPLE_CHOICE);
        }

        question.setHintFr(getCellValueAsString(row.getCell(COL_HINT_FR)));
        question.setExplanationFr(getCellValueAsString(row.getCell(COL_EXPLANATION_FR)));

        return question;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    public record ImportResult(int importedCount, List<ImportError> errors) {
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
    }

    public record ImportError(int lineNumber, String message) {
    }
}
