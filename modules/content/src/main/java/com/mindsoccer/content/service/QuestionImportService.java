package com.mindsoccer.content.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindsoccer.content.dto.QuestionImportDto;
import com.mindsoccer.content.dto.QuestionImportFileDto;
import com.mindsoccer.content.entity.QuestionEntity;
import com.mindsoccer.content.entity.ThemeEntity;
import com.mindsoccer.content.repository.QuestionRepository;
import com.mindsoccer.content.repository.ThemeRepository;
import com.mindsoccer.protocol.enums.Country;
import com.mindsoccer.protocol.enums.Difficulty;
import com.mindsoccer.protocol.enums.QuestionFormat;
import com.mindsoccer.protocol.enums.QuestionType;
import com.mindsoccer.protocol.enums.RoundType;
import com.mindsoccer.shared.exception.ValidationException;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Service d'import de questions depuis CSV, Excel ou JSON.
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
    private final ObjectMapper objectMapper;

    private final Map<String, ThemeEntity> themeCache = new HashMap<>();

    public QuestionImportService(QuestionRepository questionRepository,
                                  ThemeRepository themeRepository,
                                  ObjectMapper objectMapper) {
        this.questionRepository = questionRepository;
        this.themeRepository = themeRepository;
        this.objectMapper = objectMapper;
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
            // Support multiple round types separated by |
            Set<RoundType> roundTypes = new HashSet<>();
            for (String rt : row[COL_ROUND_TYPE].split("\\|")) {
                try {
                    roundTypes.add(RoundType.valueOf(rt.trim().toUpperCase()));
                } catch (IllegalArgumentException ignored) {
                }
            }
            question.setRoundTypes(roundTypes);
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
            // Support multiple round types separated by |
            Set<RoundType> roundTypes = new HashSet<>();
            for (String rt : roundType.split("\\|")) {
                try {
                    roundTypes.add(RoundType.valueOf(rt.trim().toUpperCase()));
                } catch (IllegalArgumentException ignored) {
                }
            }
            question.setRoundTypes(roundTypes);
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

    // ==================== JSON Import Methods ====================

    /**
     * Importe des questions depuis un fichier JSON dans le classpath.
     *
     * @param resourcePath Le chemin du fichier dans le classpath (ex: "data/duel_questions.json")
     * @return Le rapport d'importation JSON
     */
    @Transactional
    public JsonImportReport importFromJsonClasspath(String resourcePath) {
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            return importFromJsonStream(resource.getInputStream(), resourcePath);
        } catch (IOException e) {
            log.error("Erreur lors de la lecture du fichier JSON: {}", resourcePath, e);
            return new JsonImportReport(resourcePath, 0, 0, List.of("Erreur de lecture: " + e.getMessage()));
        }
    }

    /**
     * Importe des questions depuis un InputStream JSON.
     */
    @Transactional
    public JsonImportReport importFromJsonStream(InputStream inputStream, String sourceName) {
        List<String> errors = new ArrayList<>();
        int imported = 0;
        int total = 0;

        try {
            QuestionImportFileDto fileDto = objectMapper.readValue(inputStream, QuestionImportFileDto.class);

            // Importer les questions principales
            if (fileDto.getQuestions() != null) {
                for (QuestionImportDto dto : fileDto.getQuestions()) {
                    total++;
                    try {
                        importJsonQuestion(dto);
                        imported++;
                    } catch (Exception e) {
                        errors.add("Question " + total + ": " + e.getMessage());
                        log.warn("Erreur import question {}: {}", total, e.getMessage());
                    }
                }
            }

            // Importer les peintures (si présentes)
            if (fileDto.getPaintings() != null) {
                for (QuestionImportDto dto : fileDto.getPaintings()) {
                    total++;
                    try {
                        if (dto.getTheme() == null) {
                            dto.setTheme("art");
                        }
                        if (dto.getCategory() == null) {
                            dto.setCategory("peinture");
                        }
                        if ((dto.getRoundTypes() == null || dto.getRoundTypes().isEmpty()) && dto.getRoundType() == null) {
                            dto.setRoundTypes(List.of("DUEL", "CASCADE", "MARATHON", "SPRINT_FINAL", "SMASH_A", "SMASH_B"));
                        }
                        importJsonQuestion(dto);
                        imported++;
                    } catch (Exception e) {
                        errors.add("Peinture " + total + ": " + e.getMessage());
                        log.warn("Erreur import peinture {}: {}", total, e.getMessage());
                    }
                }
            }

            log.info("Import JSON terminé: {}/{} questions importées depuis {}", imported, total, sourceName);

        } catch (IOException e) {
            log.error("Erreur lors du parsing JSON: {}", sourceName, e);
            errors.add("Erreur de parsing JSON: " + e.getMessage());
        }

        return new JsonImportReport(sourceName, total, imported, errors);
    }

    /**
     * Importe une seule question depuis un DTO JSON.
     */
    private void importJsonQuestion(QuestionImportDto dto) {
        QuestionEntity entity = new QuestionEntity();

        entity.setTextFr(dto.getTextFr());
        entity.setTextEn(dto.getTextEn());
        entity.setAnswer(dto.getAnswer());

        if (dto.getAlternativeAnswers() != null && !dto.getAlternativeAnswers().isEmpty()) {
            entity.setAlternativeAnswers(new HashSet<>(dto.getAlternativeAnswers()));
        }

        if (dto.getTheme() != null && !dto.getTheme().isBlank()) {
            ThemeEntity theme = getOrCreateTheme(dto.getTheme());
            entity.setTheme(theme);
        }

        if (dto.getDifficulty() != null && !dto.getDifficulty().isBlank()) {
            entity.setDifficulty(Difficulty.valueOf(dto.getDifficulty().toUpperCase()));
        }

        // Gérer roundTypes (liste) ou roundType (singulier pour rétrocompatibilité)
        Set<RoundType> roundTypes = new HashSet<>();
        if (dto.getRoundTypes() != null && !dto.getRoundTypes().isEmpty()) {
            for (String rt : dto.getRoundTypes()) {
                try {
                    roundTypes.add(RoundType.valueOf(rt.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    log.warn("RoundType inconnu: {}", rt);
                }
            }
        } else if (dto.getRoundType() != null && !dto.getRoundType().isBlank()) {
            try {
                roundTypes.add(RoundType.valueOf(dto.getRoundType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("RoundType inconnu: {}", dto.getRoundType());
            }
        }
        entity.setRoundTypes(roundTypes);

        // Gérer categories (liste) ou category (singulier pour rétrocompatibilité)
        Set<String> categories = new HashSet<>();
        if (dto.getCategories() != null && !dto.getCategories().isEmpty()) {
            for (String cat : dto.getCategories()) {
                categories.add(cat.toLowerCase().trim());
            }
        } else if (dto.getCategory() != null && !dto.getCategory().isBlank()) {
            categories.add(dto.getCategory().toLowerCase().trim());
        }
        entity.setCategories(categories);

        if (dto.getQuestionType() != null && !dto.getQuestionType().isBlank()) {
            entity.setQuestionType(QuestionType.valueOf(dto.getQuestionType().toUpperCase()));
        }

        if (dto.getCountry() != null && !dto.getCountry().isBlank()) {
            entity.setCountry(Country.valueOf(dto.getCountry().toUpperCase()));
        }

        if (dto.getImposedLetter() != null && !dto.getImposedLetter().isBlank()) {
            entity.setImposedLetter(dto.getImposedLetter().toUpperCase().substring(0, 1));
        }

        entity.setSource("import-json");
        questionRepository.save(entity);
    }

    /**
     * Récupère ou crée un thème par son code.
     */
    private ThemeEntity getOrCreateTheme(String themeCode) {
        String normalizedCode = themeCode.toLowerCase().trim();

        if (themeCache.containsKey(normalizedCode)) {
            return themeCache.get(normalizedCode);
        }

        Optional<ThemeEntity> existing = themeRepository.findByCode(normalizedCode);
        if (existing.isPresent()) {
            themeCache.put(normalizedCode, existing.get());
            return existing.get();
        }

        ThemeEntity newTheme = new ThemeEntity(normalizedCode, getThemeDisplayName(normalizedCode));
        newTheme = themeRepository.save(newTheme);
        themeCache.put(normalizedCode, newTheme);

        log.info("Nouveau thème créé: {} ({})", normalizedCode, newTheme.getNameFr());
        return newTheme;
    }

    /**
     * Retourne le nom d'affichage d'un thème basé sur son code.
     */
    private String getThemeDisplayName(String code) {
        return switch (code) {
            case "art" -> "Art";
            case "astronomie" -> "Astronomie";
            case "cinema" -> "Cinéma";
            case "geographie" -> "Géographie";
            case "histoire" -> "Histoire";
            case "langues" -> "Langues";
            case "litterature" -> "Littérature";
            case "mathematiques" -> "Mathématiques";
            case "medecine" -> "Médecine";
            case "mode" -> "Mode";
            case "musique" -> "Musique";
            case "mythologie" -> "Mythologie";
            case "nature" -> "Nature";
            case "philosophie" -> "Philosophie";
            case "politique" -> "Politique";
            case "religion" -> "Religion";
            case "sciences" -> "Sciences";
            case "societe" -> "Société";
            case "sport" -> "Sport";
            case "technologie" -> "Technologie";
            case "vocabulaire" -> "Vocabulaire";
            default -> capitalizeFirst(code);
        };
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Importe les questions générales (utilisables par plusieurs modes de jeu).
     */
    @Transactional
    public JsonImportReport importGeneralQuestions() {
        return importFromJsonClasspath("data/general_questions.json");
    }

    /**
     * Vide le cache des thèmes.
     */
    public void clearThemeCache() {
        themeCache.clear();
    }

    // ==================== Result Records ====================

    public record ImportResult(int importedCount, List<ImportError> errors) {
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
    }

    public record ImportError(int lineNumber, String message) {
    }

    public record JsonImportReport(
            String source,
            int totalQuestions,
            int importedQuestions,
            List<String> errors
    ) {
        public boolean hasErrors() {
            return errors != null && !errors.isEmpty();
        }

        public int getFailedCount() {
            return totalQuestions - importedQuestions;
        }
    }
}
