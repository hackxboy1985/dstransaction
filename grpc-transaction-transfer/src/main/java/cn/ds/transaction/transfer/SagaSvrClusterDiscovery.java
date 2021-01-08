

package cn.ds.transaction.transfer;

public class SagaSvrClusterDiscovery {

    private DiscoveryType discoveryType = DiscoveryType.DEFAULT;;

    private String[] addresses;

    private String discoveryInfo;

    public DiscoveryType getDiscoveryType() {
        return discoveryType;
    }

    public void setDiscoveryType(DiscoveryType discoveryType) {
        this.discoveryType = discoveryType;
    }

    public String[] getAddresses() {
        return addresses;
    }

    public void setAddresses(String[] addresses) {
        this.addresses = addresses;
    }

    public String getDiscoveryInfo() {
        return discoveryInfo;
    }

    public void setDiscoveryInfo(String discoveryInfo) {
        this.discoveryInfo = discoveryInfo;
    }

    public enum DiscoveryType{
        DEFAULT,EUREKA,CONSUL,ZOOKEEPER, NACOS
    }

    public static final Builder builder(){
        return new Builder();
    }

    public static final class Builder {
        private DiscoveryType discoveryType = DiscoveryType.DEFAULT;;
        private String[] addresses;
        private String discoveryInfo;

        public Builder discoveryType(DiscoveryType discoveryType) {
            this.discoveryType = discoveryType;
            return this;
        }

        public Builder discoveryInfo(String discoveryInfo) {
            this.discoveryInfo = discoveryInfo;
            return this;
        }

        public Builder addresses(String[] addresses) {
            this.addresses = addresses;
            return this;
        }

        public SagaSvrClusterDiscovery build() {
            SagaSvrClusterDiscovery sagaClusterDiscovery = new SagaSvrClusterDiscovery();
            sagaClusterDiscovery.setDiscoveryType(discoveryType);
            sagaClusterDiscovery.setAddresses(addresses);
            sagaClusterDiscovery.setDiscoveryInfo(discoveryInfo);
            return sagaClusterDiscovery;
        }
    }
}
