package org.aksw.simba.lsq.util;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.aksw.jena_sparql_api.sparql.ext.datatypes.RDFDatatypeDelegate;
import org.apache.jena.datatypes.xsd.impl.XSDDateTimeType;

public class RDFDatatypeDateFormat
    extends RDFDatatypeDelegate
{
    protected Class<?> clazz;
    protected DateFormat dateFormat;

    public RDFDatatypeDateFormat(DateFormat dateFormat) {
        super(new XSDDateTimeType("dateTime"));
        this.clazz = Date.class;
        this.dateFormat = dateFormat;
    }

    @Override
    public Class<?> getJavaClass() {
        return clazz;
    }

    public String unparse(Object value) {
        Date date = (Date) value;
        String result = dateFormat.format(date);
//        Calendar cal = new GregorianCalendar();
//        cal.setTime(date);
//        XSDDateTime tmp = new XSDDateTime(cal);
//        String result = super.unparse(tmp);
        return result;
    }

    @Override
    public Object parse(String lexicalForm) {
        try {
        Date date = dateFormat.parse(lexicalForm);
            //Object tmp = super.parse(lexicalForm);
            //XSDDateTime xsd = (XSDDateTime) tmp;
            //Calendar cal = xsd.asCalendar();
            Calendar calendar = new GregorianCalendar();//Calendar.getInstance();
            calendar.setTime(date);

            Date result = calendar.getTime();
            return result;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}