package com.uetty.common.tool.core.net.dns;

import org.xbill.DNS.CNAMERecord;
import org.xbill.DNS.DNAMERecord;
import org.xbill.DNS.RRset;

import java.util.ArrayList;
import java.util.List;

/**
 * copy of {@link org.xbill.DNS.SetResponse}
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class SetResponse  {

    static final int UNKNOWN = 0;
    static final int NXDOMAIN = 1;
    static final int NXRRSET = 2;
    static final int DELEGATION = 3;
    static final int CNAME = 4;
    static final int DNAME = 5;
    static final int SUCCESSFUL = 6;
    private static final SetResponse unknown = new SetResponse(0);
    private static final SetResponse nxdomain = new SetResponse(1);
    private static final SetResponse nxrrset = new SetResponse(2);
    private int type;
    private Object data;

    private SetResponse() {
    }

    SetResponse(int type, RRset rrset) {
        if (type >= 0 && type <= 6) {
            this.type = type;
            this.data = rrset;
        } else {
            throw new IllegalArgumentException("invalid type");
        }
    }

    SetResponse(int type) {
        if (type >= 0 && type <= 6) {
            this.type = type;
            this.data = null;
        } else {
            throw new IllegalArgumentException("invalid type");
        }
    }

    static SetResponse ofType(int type) {
        switch(type) {
            case 0:
                return unknown;
            case 1:
                return nxdomain;
            case 2:
                return nxrrset;
            case 3:
            case 4:
            case 5:
            case 6:
                SetResponse sr = new SetResponse();
                sr.type = type;
                sr.data = null;
                return sr;
            default:
                throw new IllegalArgumentException("invalid type");
        }
    }

    @SuppressWarnings("unchecked")
    void addRRset(RRset rrset) {
        if (this.data == null) {
            this.data = new ArrayList<>();
        }

        List<Object> l = (List<Object>)this.data;
        l.add(rrset);
    }

    public boolean isUnknown() {
        return this.type == 0;
    }

    public boolean isNXDOMAIN() {
        return this.type == 1;
    }

    public boolean isNXRRSET() {
        return this.type == 2;
    }

    public boolean isDelegation() {
        return this.type == 3;
    }

    public boolean isCNAME() {
        return this.type == 4;
    }

    public boolean isDNAME() {
        return this.type == 5;
    }

    public boolean isSuccessful() {
        return this.type == 6;
    }

    @SuppressWarnings({"RedundantCast", "unchecked"})
    public RRset[] answers() {
        if (this.type != 6) {
            return null;
        } else {
            List<RRset> l = (List<RRset>)this.data;
            return (RRset[])((RRset[])l.toArray(new RRset[0]));
        }
    }

    public CNAMERecord getCNAME() {
        return (CNAMERecord)((RRset)this.data).first();
    }

    public DNAMERecord getDNAME() {
        return (DNAMERecord)((RRset)this.data).first();
    }

    public RRset getNS() {
        return (RRset)this.data;
    }

    public String toString() {
        switch(this.type) {
            case 0:
                return "unknown";
            case 1:
                return "NXDOMAIN";
            case 2:
                return "NXRRSET";
            case 3:
                return "delegation: " + this.data;
            case 4:
                return "CNAME: " + this.data;
            case 5:
                return "DNAME: " + this.data;
            case 6:
                return "successful";
            default:
                throw new IllegalStateException();
        }
    }
}
