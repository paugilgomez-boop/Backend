package requests;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@ApiModel(value = "FaqAssistantRequest", description = "Pregunta al asistente FAQ")
public class FaqAssistantRequest {
    private String question;

    public FaqAssistantRequest() {
    }

    @ApiModelProperty(value = "Pregunta del usuario", required = true)
    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
