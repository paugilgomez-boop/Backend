package responses;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FaqAssistantHealthResponse {
    private String llmUrl;
    private String llmModel;
    private boolean ok;
    private String detail;

    public FaqAssistantHealthResponse() {
    }

    public FaqAssistantHealthResponse(String llmUrl, String llmModel, boolean ok, String detail) {
        this.llmUrl = llmUrl;
        this.llmModel = llmModel;
        this.ok = ok;
        this.detail = detail;
    }

    public String getLlmUrl() {
        return llmUrl;
    }

    public void setLlmUrl(String llmUrl) {
        this.llmUrl = llmUrl;
    }

    public String getLlmModel() {
        return llmModel;
    }

    public void setLlmModel(String llmModel) {
        this.llmModel = llmModel;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
