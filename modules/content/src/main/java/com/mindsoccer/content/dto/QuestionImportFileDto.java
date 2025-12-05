package com.mindsoccer.content.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO représentant le fichier JSON d'importation de questions.
 */
public class QuestionImportFileDto {

    @JsonProperty("metadata")
    private MetadataDto metadata;

    @JsonProperty("questions")
    private List<QuestionImportDto> questions;

    @JsonProperty("paintings")
    private List<QuestionImportDto> paintings;

    public MetadataDto getMetadata() {
        return metadata;
    }

    public void setMetadata(MetadataDto metadata) {
        this.metadata = metadata;
    }

    public List<QuestionImportDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionImportDto> questions) {
        this.questions = questions;
    }

    public List<QuestionImportDto> getPaintings() {
        return paintings;
    }

    public void setPaintings(List<QuestionImportDto> paintings) {
        this.paintings = paintings;
    }

    public static class MetadataDto {
        @JsonProperty("gameMode")
        private String gameMode;

        @JsonProperty("version")
        private String version;

        @JsonProperty("totalQuestions")
        private int totalQuestions;

        @JsonProperty("description")
        private String description;

        public String getGameMode() {
            return gameMode;
        }

        public void setGameMode(String gameMode) {
            this.gameMode = gameMode;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public int getTotalQuestions() {
            return totalQuestions;
        }

        public void setTotalQuestions(int totalQuestions) {
            this.totalQuestions = totalQuestions;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
