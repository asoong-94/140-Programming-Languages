import java.util.*;

public class SymbolTable{
    private ArrayList<ArrayList<String>> list;
    
    
    SymbolTable (){
        list = new ArrayList<ArrayList<String>>();
    }
    
    public void newscope()
    {
        list.add(new ArrayList<String>());
    }
    
    public void deletescope()
    {
        list.remove(list.size()-1);
    }
    
    public boolean contains(String string)
    {
        return list.get(list.size()-1).contains(string);
    }
    
    public boolean containsinprogram(String string)
    {
        for (int i = 0; i < list.size(); i++)
        {
            if (list.get(i).contains(string))
                return true;
        }
        return false;
    }
    
    public boolean containsinscope(int scope, String string)
    {
        if (scope > list.size()-1)
            return false;
        if (list.get(list.size()-1-scope).contains(string))
            return true;
        else
            return false; 
    }
    
    public boolean containsinglobal(String string)
    {
        return list.get(0).contains(string);
    }
    
    public void add(String string)
    {
        list.get(list.size()-1).add(string);
    }
    
    public int getscope()
    {
        return list.size()-1;
    }
    
}