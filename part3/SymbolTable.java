import java.util.*;

public class SymbolTable{
    //symbolTable is an arraylist of arraylist of strings(tokens)
    private ArrayList<ArrayList<String>> list; //SymbolTable called list 
    
    
    SymbolTable (){
        list = new ArrayList<ArrayList<String>>();
    } // constructor, the tokens are the <String> for inner arrayList 
    
    public void newscope()
    {
        list.add(new ArrayList<String>());
    } // append a new arraylist to current scope 
    
    public void deletescope()
    {
        list.remove(list.size()-1);
    } // delete most recent scope 
    
    public boolean contains(String string)
    {
        return list.get(list.size()-1).contains(string);
    } // go through list and check for declaration var
    
    public boolean containsinprogram(String string)
    {
        for (int i = 0; i < list.size(); i++)
        {
            if (list.get(i).contains(string))
                return true;
        } // go through outerlist(whole program) and check for declaration of car 
        return false;
        //if not found, return false 
    }
    
    public boolean containsinscope(int scope, String string)
    {
        if (scope > list.size()-1)
            return false; //check for proper scoping 
        if (list.get(list.size()-1-scope).contains(string))
            return true; // check current scope for declaration of variable 
        else
            return false; 
    }
    
    public boolean containsinglobal(String string)
    {
        return list.get(0).contains(string);
    }//global check for var 
    
    public void add(String string)
    {
        list.get(list.size()-1).add(string);
    } // append var to table 
}