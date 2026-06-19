package requests;

import io.swagger.annotations.ApiModel;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@ApiModel(value = "PurchaseUpgradeRequest", description = "Compra de mejora de torreta")
public class PurchaseUpgradeRequest {

    private String userId;
    private String upgradeType;

    public PurchaseUpgradeRequest() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUpgradeType() {
        return upgradeType;
    }

    public void setUpgradeType(String upgradeType) {
        this.upgradeType = upgradeType;
    }
}
