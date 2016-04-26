import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Model
{
    private ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();
    ArrayList<String[]> hierarchy = new ArrayList<String[]>();
    private static Connection conn;
    private static Statement stmt;
    Object rowData[][];
    Object columnNames[];
    String[] measures = new String[] { "dollar_sales", "unit_sales",
    "dollar_cost", "customer_count" };
    int measureIndex = 0;
    
    // basecube
    String[] a = { "store_state", "category" };
    String m = measures[measureIndex];
    String c = ""; // "WHERE  ";
    
    public Model()
    {
        initializeDatabase();
    }
    
    private void initializeDatabase()
    {
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(
                                               "jdbc:mysql://localhost:3306/grocerydb", "root", "");
            stmt = conn.createStatement();
            
        } catch (SQLException e)
        {
            e.printStackTrace();
        } catch (ClassNotFoundException e)
        {
            System.out.println("Where is your MySQL JDBC Driver?");
            e.printStackTrace();
        }
        
        setTableData(a, m, c);
        
        setHierarchyData();
    }
    
    public void setTableData(String[] a, String m, String c)
    {
        ResultSet rs = query(a, m, c);
        if (rs == null)
        {
            columnNames = new Object[] { "Select Dimension" };
            rowData = new Object[][] { { "Select Dimension" } };
            return;
        }
        columnNames = new Object[a.length + 1];
        int i;
        for (i = 0; i < a.length; i++)
        {
            columnNames[i] = a[i];
        }
        columnNames[i] = m;
        ArrayList<String[]> rows = new ArrayList<String[]>();
        
        try
        {
            while (rs.next())
            {
                String[] temp = new String[a.length + 1];
                for (i = 0; i < a.length; i++)
                {
                    temp[i] = rs.getString(a[i]);
                }
                temp[i] = "" + rs.getDouble(m);
                rows.add(temp);
            }
            rowData = new Object[rows.size()][a.length + 1];
            for (i = 0; i < rows.size(); i++)
            {
                rowData[i] = rows.get(i);
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    
    public ResultSet query(String[] attributes, String measure, String condition)
    {
        if (measure.isEmpty())
        {
            return null;
        }
        String attributeString = "";
        for (String a : attributes)
        {
            attributeString += a + ",";
        }
        if (!attributeString.isEmpty())
        {
            attributeString = attributeString.substring(0,
                                                        attributeString.length() - 1);
        }
        String QUERY = "SELECT " + attributeString + ", SUM(" + measure + ") "
        + measure + " FROM all_cube " + condition;
        if (!attributeString.isEmpty())
            QUERY += " GROUP BY " + attributeString;
        else
            QUERY = "SELECT " + QUERY.substring(9);
        System.out.println(QUERY);
        ResultSet rs = null;
        try
        {
            rs = stmt.executeQuery(QUERY);
            
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return rs;
    }
    
    public void setHierarchyData()
    {
        // hierarchy
        String store[] = { "sales_region", "store_state", "city" };
        String product[] = { "category", "subcategory", "brand" };
        String time[] = { "year", "month" };
        String promotion[] = { "display_type" };
        
        hierarchy.add(store);
        hierarchy.add(product);
        hierarchy.add(time);
        hierarchy.add(promotion);
        // tree.addSubtree(createTree(store, "store"));
        
    }
    
    public void attach(ChangeListener c)
    {
        listeners.add(c);
    }
    
    public void update(String[] a, String m, String c)
    {
        this.a = a;
        this.m = m;
        this.c = c;
        setTableData(a, m, c);
        for (ChangeListener l : listeners)
        {
            l.stateChanged(new ChangeEvent(this));
        }
    }
    
    public void update()
    {
        for (ChangeListener l : listeners)
        {
            l.stateChanged(new ChangeEvent(this));
        }
    }
}