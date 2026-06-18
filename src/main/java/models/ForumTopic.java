package models;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@ApiModel(value = "ForumTopic", description = "Temática del foro")
public class ForumTopic {

    private int id;
    private String title;
    private String description;
    private String author;
    private String createdAt;
    private int messageCount;

    public ForumTopic() {}

    public ForumTopic(int id, String title, String description, String author, String createdAt, int messageCount) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.author = author;
        this.createdAt = createdAt;
        this.messageCount = messageCount;
    }

    @ApiModelProperty(value = "ID único de la temática")
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @ApiModelProperty(value = "Título de la temática")
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    @ApiModelProperty(value = "Descripción de la temática")
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @ApiModelProperty(value = "Usuario que creó la temática")
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    @ApiModelProperty(value = "Fecha de creación (ISO 8601)")
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @ApiModelProperty(value = "Número de mensajes en la temática")
    public int getMessageCount() { return messageCount; }
    public void setMessageCount(int messageCount) { this.messageCount = messageCount; }
}
