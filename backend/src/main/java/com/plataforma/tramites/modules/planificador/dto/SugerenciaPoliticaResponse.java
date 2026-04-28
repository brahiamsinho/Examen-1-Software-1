package com.plataforma.tramites.modules.planificador.dto;

public class SugerenciaPoliticaResponse {

    private String policyId;
    private double scoreConfianza;
    private String explanation;
    private boolean humanReviewRequired;

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public double getScoreConfianza() {
        return scoreConfianza;
    }

    public void setScoreConfianza(double scoreConfianza) {
        this.scoreConfianza = scoreConfianza;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public boolean isHumanReviewRequired() {
        return humanReviewRequired;
    }

    public void setHumanReviewRequired(boolean humanReviewRequired) {
        this.humanReviewRequired = humanReviewRequired;
    }
}
