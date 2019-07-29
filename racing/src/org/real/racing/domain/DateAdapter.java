package org.real.racing.domain;

import java.util.Date;
import java.text.SimpleDateFormat;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class DateAdapter extends XmlAdapter<String, Date> {

    @Override
    public String marshal(Date d) throws Exception {
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM=dd");
        return dateFormat.format(d);
    }

    @Override
    public Date unmarshal(String s) throws Exception {
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.parse(s) ;
    }

}
