package models;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@ApiModel(value = "ForumMessage", description = "Mensaje del foro")
public class ForumMessage {

    private int id;
    private int topicId;
    private String author;
    private String content;
    private String createdAt;

    public ForumMessage() {}

    public ForumMessage(int id, int topicId, String author, String content, String createdAt) {
        this.id = id;
        this.topicId = topicId;
        this.author = author;
        this.content = content;
        this.createdAt = createdAt;
    }

    @ApiModelProperty(value = "ID único del mensaje")
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @ApiModelProperty(value = "ID de la temática asociada")
    public int getTopicId() { return topicId; }
    public void setTopicId(int topicId) { this.topicId = topicId; }

    @ApiModelProperty(value = "Usuario autor del mensaje")
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    @ApiModelProperty(value = "Contenido del mensaje")
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    @ApiModelProperty(value = "Fecha de creación (ISO 8601)")
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
