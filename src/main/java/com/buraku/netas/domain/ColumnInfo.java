package com.buraku.netas.domain;

import java.io.Serializable;

public class ColumnInfo implements Serializable{

	private static final long serialVersionUID = -7860243025833384447L;
	
	private String header;
    private String property;

    public ColumnInfo(String header, String property) {
        this.header = header;
        this.property = property;
    }

    public String getHeader() {
        return header;
    }

    public String getProperty() {
        return property;
    }

}
