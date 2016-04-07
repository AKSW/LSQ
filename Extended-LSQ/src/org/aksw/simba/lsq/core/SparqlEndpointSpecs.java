package org.aksw.simba.lsq.core;

public class SparqlEndpointSpecs {
    protected String endpointVersion;
    protected String ram;
    protected String processor;
    protected String ep;
    protected String curTime;
    public String getEndpointVersion() {
        return endpointVersion;
    }
    public void setEndpointVersion(String endpointVersion) {
        this.endpointVersion = endpointVersion;
    }
    public String getRam() {
        return ram;
    }
    public void setRam(String ram) {
        this.ram = ram;
    }
    public String getProcessor() {
        return processor;
    }
    public void setProcessor(String processor) {
        this.processor = processor;
    }
    public String getEp() {
        return ep;
    }
    public void setEp(String ep) {
        this.ep = ep;
    }
    public String getCurTime() {
        return curTime;
    }
    public void setCurTime(String curTime) {
        this.curTime = curTime;
    }


}
