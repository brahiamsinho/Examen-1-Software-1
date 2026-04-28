package com.plataforma.tramites.modules.analitica.dto;

import java.util.List;

public class CuelloBotellaResponse {

    private String status;
    private String summary;
    private List<CuelloBotellaSignal> signals;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public List<CuelloBotellaSignal> getSignals() { return signals; }
    public void setSignals(List<CuelloBotellaSignal> signals) { this.signals = signals; }
}
