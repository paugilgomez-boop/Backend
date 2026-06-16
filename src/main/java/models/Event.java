package models;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Event", description = "Evento al que pueden apuntarse los usuarios")
public class Event {
    private int id;
    private String name;
    private String description;

    public Event() {}

    public Event(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    @ApiModelProperty(value = "Identificador del evento")
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @ApiModelProperty(value = "Nombre del evento")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @ApiModelProperty(value = "Descripcion del evento")
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}