package edu.jsu.mcis.cs425.Lab4;

import com.opencsv.CSVReader;
import static com.sun.corba.se.spi.presentation.rmi.StubAdapter.request;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Rates {
    
    public static final String RATE_FILENAME = "rates.csv";
    
    public static List<String[]> getRates(String path) {
        
        StringBuilder s = new StringBuilder();
        List<String[]> data = null;
        String line;
        
        try {
            
            /* Open Rates File; Attach BufferedReader */

            BufferedReader reader = new BufferedReader(new FileReader(path));
            
            /* Get File Data */
            
            while((line = reader.readLine()) != null) {
                s.append(line).append('\n');
            }
            
            reader.close();
            
            /* Attach CSVReader; Parse File Data to List */
            
            CSVReader csvreader = new CSVReader(new StringReader(s.toString()));
            data = csvreader.readAll();
            
        }
        catch (Exception e) { System.err.println( e.toString() ); }
        
        /* Return List */
        
        return data;
        
    }
    
    public static String getRatesAsTable(List<String[]> csv) {
        
        StringBuilder s = new StringBuilder();
        String[] row;
        
        try {
            
            /* Create Iterator */
            
            Iterator<String[]> iterator = csv.iterator();
            
            /* Create HTML Table */
            
            s.append("<table>");
            
            while (iterator.hasNext()) {
                
                /* Create Row */
            
                row = iterator.next();
                s.append("<tr>");
                
                for (int i = 0; i < row.length; ++i) {
                    s.append("<td>").append(row[i]).append("</td>");
                }
                
                /* Close Row */
                
                s.append("</tr>");
            
            }
            
            /* Close Table */
            
            s.append("</table>");
            
        }
        catch (Exception e) { System.err.println( e.toString() ); }
        
        /* Return Table */
        
        return (s.toString());
        
    }
    
    public static String getRatesAsJson(List<String[]> csv) {
        
        String results = "";
        String[] row;
        
        try {
            
            /* Create Iterator */
            
            Iterator<String[]> iterator = csv.iterator();
            
            /* Create JSON Containers */
            
            JSONObject json = new JSONObject();
            JSONObject rates = new JSONObject();            
            
            row = iterator.next();
            while(iterator.hasNext()){
                row = iterator.next();
                Double val;
                String code;

                for (int i = 0; i < row.length; ++i){
                    
                    code= row[1];
                    val = Double.parseDouble(row[2]);
                    
                    rates.put(code,val );
                }
         
                        
                     
            
            }
            String pattern = "MM-dd-yyyy";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String date = simpleDateFormat.format(new Date());

            json.put("rates", rates);
            json.put("date", date);
            
            /* Parse top-level container to a JSON string */
            
            results = JSONValue.toJSONString(json);
            
        }
        catch (Exception e) { System.err.println( e.toString() ); }
        
        /* Return JSON string */
        
        return (results.trim());
        
    }
    public static String getRatesAsJson(String code) throws NamingException, SQLException{
        Connection connection = null;
        PreparedStatement pstatement = null;
        ResultSet resultset = null;
        Context envContext = null, initContext = null;
        DataSource ds = null;
        
        try {
            
            envContext = new InitialContext();
            initContext  = (Context)envContext.lookup("java:/comp/env");
            ds = (DataSource)initContext.lookup("jdbc/db_pool");
            connection = ds.getConnection();
            
        }
        
        catch (SQLException e) {}
        String results = "";
        String query;
        String parameter;
        String table = "";
        boolean hasresults;
        parameter = code; 
        
        query = "SELECT * FROM rates WHERE code = ?"; 
        pstatement = connection.prepareStatement(query);
        pstatement.setString(1, parameter);
            
        hasresults = pstatement.execute();
        while ( hasresults || pstatement.getUpdateCount() != -1 ) {
                
                if ( hasresults ) {
                    resultset = pstatement.getResultSet();
                    
                }
                
                else {
                    
                    if ( pstatement.getUpdateCount() == -1 ) {
                        break;
                    }
                    
                }

                hasresults = pstatement.getMoreResults();
            
            }
            JSONObject json = new JSONObject();
            JSONObject rates = new JSONObject();            
            
            code= parameter;
            int val = pstatement.getFetchSize();
            rates.put(code,val);
            json.put("rates", rates);
            
            
            
            results = JSONValue.toJSONString(json);
        return (results.trim());
    }
    

}