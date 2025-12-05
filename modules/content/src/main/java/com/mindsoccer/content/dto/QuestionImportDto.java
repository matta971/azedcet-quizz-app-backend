package com.mindsoccer.content.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO pour l'importation de questions depuis un fichier JSON.
 */
public class QuestionImportDto {

    @JsonProperty("textFr")
    private String textFr;

    @JsonProperty("textEn")
    private String textEn;

    @JsonProperty("answer")
    private String answer;

    @JsonProperty("alternativeAnswers")
    private List<String> alternativeAnswers;

    @JsonProperty("theme")
    private String theme;

    @JsonProperty("category")
    private String category;

    @JsonProperty("categories")
    private List<String> categories;

    @JsonProperty("difficulty")
    private String difficulty;

    @JsonProperty("roundType")
    private String roundType;

    @JsonProperty("roundTypes")
    private List<String> roundTypes;

    @JsonProperty("imposedLetter")
    private String imposedLetter;

    @JsonProperty("questionType")
    private String questionType;

    @JsonProperty("country")
    private String country;

    // Getters and Setters
    public String getTextFr() {
        return textFr;
    }

    public void setTextFr(String textFr) {
        this.textFr = textFr;
    }

    public String getTextEn() {
        return textEn;
    }

    public void setTextEn(String textEn) {
        this.textEn = textEn;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<String> getAlternativeAnswers() {
        return alternativeAnswers;
    }

    public void setAlternativeAnswers(List<String> alternativeAnswers) {
        this.alternativeAnswers = alternativeAnswers;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getRoundType() {
        return roundType;
    }

    public void setRoundType(String roundType) {
        this.roundType = roundType;
    }

    public List<String> getRoundTypes() {
        return roundTypes;
    }

    public void setRoundTypes(List<String> roundTypes) {
        this.roundTypes = roundTypes;
    }

    public String getImposedLetter() {
        return imposedLetter;
    }

    public void setImposedLetter(String imposedLetter) {
        this.imposedLetter = imposedLetter;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
