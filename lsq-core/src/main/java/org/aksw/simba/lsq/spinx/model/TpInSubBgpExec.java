package org.aksw.simba.lsq.spinx.model;

public interface TpInSubBgpExec
    extends TpInBgpExec
{
    JoinVertexExec getBgpNodeExec();
    TpInSubBgpExec setBgpNodeExec(JoinVertexExec bpgNodeExec);
}
