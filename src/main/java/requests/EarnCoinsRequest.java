package requests;

import io.swagger.annotations.ApiModel;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@ApiModel(value = "EarnCoinsRequest", description = "Monedas ganadas en una partida")
public class EarnCoinsRequest {

    private String userId;
    private int coinsEarned;

    public EarnCoinsRequest() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getCoinsEarned() {
        return coinsEarned;
    }

    public void setCoinsEarned(int coinsEarned) {
        this.coinsEarned = coinsEarned;
    }
}
