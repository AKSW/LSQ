package org.aksw.simba.lsq.parser;

import java.text.DateFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.aksw.jena_sparql_api.sparql.ext.datatypes.RDFDatatypeDelegate;
import org.apache.jena.datatypes.xsd.XSDDateTime;
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
        XSDDateTime dt = (XSDDateTime)value;
        Calendar cal = dt.asCalendar();

        //Calendar cal = (Calendar)value;
//      Calendar cal = new GregorianCalendar();
//      cal.setTime(date);
        //Date date = (Date) value;
        Date date = cal.getTime();
      String result = dateFormat.format(date);
//        XSDDateTime tmp = new XSDDateTime(cal);
//        String result = super.unparse(tmp);
        return result;
    }

    @Override
    public Object parse(String lexicalForm) {
        try {

            //DateTimeFormatter f = DateTimeFormatter.ofPattern("dd MM uuuu HH:mm:ss.SSS X");
            //OffsetDateTime odt = OffsetDateTime.parse ( input , f );

            Date date = dateFormat.parse(lexicalForm);
            //Object tmp = super.parse(lexicalForm);
            //XSDDateTime xsd = (XSDDateTime) tmp;
            //Calendar cal = xsd.asCalendar();
            Calendar calendar = new GregorianCalendar();//Calendar.getInstance();
            calendar.setTime(date);

            return calendar;
            //Date result = calendar.getTime();
            //return result;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}