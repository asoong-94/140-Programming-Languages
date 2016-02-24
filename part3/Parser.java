/* *** This file is given as part of the programming assignment. *** */
import java.util.*;

public class Parser {
    // tok is global to all these parsing methods;
    // scan just calls the scanner's scan method and saves the result in tok.
    private Token tok; // the current token
    private void scan() {
	    tok = scanner.scan();
    }
    /* 
        table is created by using a list of array lists. 
        each layer of the outlist indicateds which what scope you are indicateds
        the outer linked list conatins a list of the variables decared 
    */
    
    // symbol table created in SymbolTable.java 
    private SymbolTable table; // symbol table created in SymbolTable.java 
    
    private Scan scanner;
    Parser(Scan scanner) {
	this.scanner = scanner;
	scan();
	table = new SymbolTable();
	program();
	if( tok.kind != TK.EOF )
	    parse_error("junk after logical end of program");
    }


     // is current token what we want?
    private boolean is(TK tk) {
        return tk == tok.kind;
    }

    // ensure current token is tk and skip over it.
    private void mustbe(TK tk) {
	if( tok.kind != tk ) {
	    System.err.println( "mustbe: want " + tk + ", got " +
				    tok);
	    parse_error( "missing token (mustbe)" );
	}
	scan();
    }
    
    
    private void parse_error(String msg) {
	System.err.println( "can't parse: line "
			    + tok.lineNumber + " " + msg );
	System.exit(1);
    }
    
    private void program() {
    	block();
    }

    private void block(){
        /* 
            each time a new block is created, so is a new scope
            following a block, there is either a statement list
            or declaration list, at the end, the scope is closed 
        */
        table.newscope();
    	declaration_list();
    	statement_list();
    	table.deletescope();
    }

    private void declaration_list() {
	// below checks whether tok is in first set of declaration.
	// here, that's easy since there's only one token kind in the set.
	// in other places, though, there might be more.
	// so, you might want to write a general function to handle that.
    	while( is(TK.DECLARE) ) {
    	    declaration();
    	    }
    }

    private void declaration() {
    	mustbe(TK.DECLARE);
    	if(is(TK.ID))
    	{
    	    if(!table.contains(tok.string))
        	    table.add(tok.string); // if table does not have var, then add
        	else // if table already has var, then print redeclared error
                System.err.println("redeclaration of variable " + tok.string);
            scan(); 
        }
        else
            mustbe(TK.ID);
            
    	while( is(TK.COMMA) ) { // if comma (declaring more than 1 var)
    	    scan();
        	if(is(TK.ID)) // after the comma must be another var 
    	    {
    	        if(!(table.contains(tok.string)))
            	    table.add(tok.string); // check table again, if its been declared
                else
                    System.err.println("redeclaration of variable " + tok.string);
                scan(); // if table already has var, then print redeclaration error 
            }
            else
                mustbe(TK.ID); // declaration must declare id no matter what 
    	}
    }

    private void statement_list()
    {
        while (is(TK.TILDE) || is(TK.ID) || is(TK.PRINT) || is(TK.DO) || is(TK.IF)) 
            statement();
    }

    private void statement()
    {
        if (is(TK.TILDE) || is(TK.ID))
            assignment();
        else if (is(TK.PRINT))
            print();
        else if (is(TK.DO))
            do_this();
        else if (is(TK.IF))
            if_this();
    }
    
    private void print()
    {
        mustbe(TK.PRINT);
        expr();
    }
    
    
    private void assignment()
    {
        ref_id();
        mustbe(TK.ASSIGN); 
        expr();
    }
    
    private void ref_id()
    {
        int scope;
        if (is(TK.TILDE)) //if there is a tilde
        {
            scan(); //must be tilde 
            if (is(TK.NUM))
            {
                scope = Integer.parseInt(tok.string); // tok.string starts of with numer indicating what scope var is in 
                scan();
                if (is(TK.ID))
                {
                    if(!table.containsinscope(scope,tok.string)) // if at current scope, table doesnt contain variable 
                    {
                        System.err.println("no such variable ~" + scope + tok.string + " on line " + tok.lineNumber);
                        System.exit(1);
                    } //if variable not previously declared in outer list   
                    scan();
                }
                else
                    mustbe(TK.ID);
            } //could or could not have num
            else if (is(TK.ID))
            {
                if(!table.containsinglobal(tok.string)) 
                {
                    System.err.println("no such variable ~" + tok.string + " on line " + tok.lineNumber);
                    System.exit(1);
                } // if variable not previosly declared in outer list  print error msg 
                scan();
            }
        }
        else if (is(TK.ID))
        {
            if(!table.containsinprogram(tok.string))
            {
                System.err.println(tok.string + " is an undeclared variable on line " + tok.lineNumber);
                System.exit(1);
            } // if variable not previously declared in current list 
            scan();
        }
        else
            mustbe(TK.ID); //ref_id must declare an id 
    }
    
    private void do_this ()
    {
        mustbe(TK.DO);
        guarded_command();
        mustbe(TK.ENDDO);
    }
    
    //not sure
    private void if_this ()
    {
        if (is(TK.IF))
        {
            scan();
            guarded_command();
            while (is(TK.ELSEIF))
            {
                mustbe(TK.ELSEIF);
                guarded_command();
            }
        }
        if(is(TK.ELSE))
        {
            mustbe(TK.ELSE);
            block();
        }
        mustbe(TK.ENDIF);
    }
    
    private void guarded_command()
    {
        expr();
        mustbe(TK.THEN);
        block();
    }
    
    private void expr()
    {
        term();
        while ( is(TK.PLUS) || is(TK.MINUS))
        {
            addop();
            term();
        }
    }
    
    private void term()
    {
        factor();
        while (is(TK.TIMES) || is(TK.DIVIDE))
        {
            multop();
            factor();
        }
    }
    
    //not sure
    private void factor()
    {
        if (is(TK.LPAREN))
        {
            scan();
            expr();
            mustbe(TK.RPAREN);
        }
         
        else if(is(TK.TILDE) || is(TK.ID))
            ref_id();
        else if(is(TK.NUM))
            scan();
        else
        {
            System.err.println( "mustbe: want factor, got " +
				    tok);
	        parse_error( "missing token (mustbe)" );
        }
    }
    
    private void addop()
    {
        if (is(TK.PLUS) || is(TK.MINUS))
            scan();
        else
        {
            System.err.println( "mustbe: want + or -, got " +
				    tok);
	        parse_error( "missing token (mustbe)" );
        }
    }       
    
    private void multop()
    {
        if (is(TK.TIMES) || is(TK.DIVIDE))
            scan();
        else
        {
            System.err.println( "mustbe: want * or /, got " +
				    tok);
	        parse_error( "missing token (mustbe)" );
        }
    }
}
